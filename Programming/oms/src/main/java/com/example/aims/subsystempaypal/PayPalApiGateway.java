package com.example.aims.subsystempaypal;

import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.example.aims.subsystempaypal.dto.PayPalRefundRequest;
import com.example.aims.subsystempaypal.dto.PayPalRefundResponse;
import com.example.aims.subsystempaypal.dto.PaypalCaptureDetail;
import com.example.aims.subsystempaypal.dto.PaypalCaptureResponse;
import com.example.aims.subsystempaypal.dto.PaypalLink;
import com.example.aims.subsystempaypal.dto.PaypalOrderCapture;
import com.example.aims.subsystempaypal.dto.PaypalOrderRequest;
import com.example.aims.subsystempaypal.dto.PaypalOrderResponse;
import com.example.aims.subsystempaypal.dto.PaypalPurchaseUnitResult;
import com.example.aims.subsystempaypal.dto.PaypalTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PayPalApiGateway implements IPayPalApiGateway {

    private static final int CAPTURE_MAX_ATTEMPTS = 10;
    private static final long CAPTURE_RETRY_DELAY_MILLIS = 1000L;

    private final PaypalConfig config;
    private final RestClient restClient;

    public PayPalApiGateway(PaypalConfig config,
                            @Qualifier("paypalRestClient") RestClient restClient) {
        this.config = config;
        this.restClient = restClient;
    }

    @Override
    public PaypalTokenResponse getAccessToken() {
        validateConfig();
        try {
            return restClient.post()
                    .uri(config.getBaseUrl() + "/v1/oauth2/token")
                    .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(PaypalTokenResponse.class);
        } catch (RestClientException ex) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_UNAVAILABLE,
                    "Unable to obtain PayPal access token", ex);
        }
    }

    @Override
    public PaypalOrderResponse createOrder(PaypalOrderRequest request, String token) {
        try {
            PaypalOrderResponse orderResponse = restClient.post()
                    .uri(config.getBaseUrl() + "/v2/checkout/orders")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PaypalOrderResponse.class);

            return extractApproveLink(orderResponse);
        } catch (PaymentException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_UNAVAILABLE,
                    "Unable to create PayPal order", ex);
        }
    }

    @Override
    public PaypalCaptureResponse capturePayment(String paypalOrderId, String token) {
        RestClientResponseException lastPaypalException = null;

        for (int attempt = 1; attempt <= CAPTURE_MAX_ATTEMPTS; attempt++) {
            try {
                return sendCapturePaymentRequest(paypalOrderId, token);
            } catch (RestClientResponseException ex) {
                lastPaypalException = ex;
                String paypalError = summarizePaypalError(ex);
                if (!isRetryableCaptureRejection(ex) || attempt == CAPTURE_MAX_ATTEMPTS) {
                    if (isAlreadyCaptured(ex)) {
                        log.info("PayPal order {} was already captured. Fetching order details to recover capture result.",
                                paypalOrderId);
                        return getCapturedOrder(paypalOrderId, token);
                    }
                    log.warn("PayPal capture rejected order {} with status {} and body {}",
                            paypalOrderId, ex.getStatusCode(), paypalError);
                    throw new PaymentException(PaymentErrorCode.GATEWAY_UNAVAILABLE,
                            "Unable to capture PayPal payment: " + paypalError, ex);
                }
                log.info("PayPal capture for order {} was not approved yet on attempt {}/{}. Retrying after {} ms. Body: {}",
                        paypalOrderId, attempt, CAPTURE_MAX_ATTEMPTS, CAPTURE_RETRY_DELAY_MILLIS, paypalError);
                sleepBeforeRetry();
            } catch (RestClientException ex) {
                log.warn("PayPal capture request failed for order {} before receiving a PayPal error response",
                        paypalOrderId, ex);
                throw new PaymentException(PaymentErrorCode.GATEWAY_UNAVAILABLE,
                        "Unable to capture PayPal payment: " + summarizeClientException(ex), ex);
            }
        }

        throw new PaymentException(PaymentErrorCode.GATEWAY_UNAVAILABLE,
                "Unable to capture PayPal payment: " + summarizePaypalError(lastPaypalException),
                lastPaypalException);
    }

    @Override
    public PayPalRefundResponse refundPayment(String captureId, PayPalRefundRequest request, String token) {
        try {
            return restClient.post()
                    .uri(config.getBaseUrl() + "/v2/payments/captures/{captureId}/refund", captureId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PayPalRefundResponse.class);
        } catch (RestClientException ex) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_UNAVAILABLE,
                    "Unable to refund PayPal payment", ex);
        }
    }

    private PaypalOrderResponse extractApproveLink(PaypalOrderResponse orderResponse) {
        List<PaypalLink> links = orderResponse.getLinks();
        String link = (links != null ? links.stream() : java.util.stream.Stream.<PaypalLink>empty())
                .filter(l -> "approve".equalsIgnoreCase(l.getRel())
                          || "payer-action".equalsIgnoreCase(l.getRel()))
                .map(PaypalLink::getHref)
                .filter(h -> h != null && !h.isBlank())
                .findFirst()
                .orElse(null);

        if (link == null) {
            String rels = links == null ? "<none>"
                    : links.stream().map(PaypalLink::getRel).collect(Collectors.joining(", "));
            throw new PaymentException(PaymentErrorCode.GATEWAY_RESPONSE_UNEXPECTED,
                    "PayPal response has no approve or payer-action URL. Returned link rels: " + rels);
        }

        orderResponse.setApproveLink(link);
        return orderResponse;
    }

    private PaypalCaptureResponse sendCapturePaymentRequest(String paypalOrderId, String token) {
        PaypalOrderCapture orderCapture = restClient.post()
                .uri(config.getBaseUrl() + "/v2/checkout/orders/{orderId}/capture", paypalOrderId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                .header("Prefer", "return=representation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of())
                .retrieve()
                .body(PaypalOrderCapture.class);

        return extractCapture(orderCapture);
    }

    private PaypalCaptureResponse getCapturedOrder(String paypalOrderId, String token) {
        try {
            PaypalOrderCapture orderCapture = restClient.get()
                    .uri(config.getBaseUrl() + "/v2/checkout/orders/{orderId}", paypalOrderId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(PaypalOrderCapture.class);

            return extractCapture(orderCapture);
        } catch (RestClientException ex) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_UNAVAILABLE,
                    "Unable to recover already captured PayPal payment: " + summarizeClientException(ex), ex);
        }
    }

    private PaypalCaptureResponse extractCapture(PaypalOrderCapture orderCapture) {
        List<PaypalPurchaseUnitResult> units = orderCapture != null ? orderCapture.getPurchaseUnits() : null;
        if (units == null || units.isEmpty() || units.get(0).getPayments() == null) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_RESPONSE_UNEXPECTED,
                    "PayPal capture response does not contain purchase units");
        }
        List<PaypalCaptureDetail> captures = units.get(0).getPayments().getCaptures();
        if (captures == null || captures.isEmpty() || captures.get(0).getId() == null) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_RESPONSE_UNEXPECTED,
                    "PayPal capture response does not contain capture ID");
        }
        return PaypalCaptureResponse.builder()
                .captureId(captures.get(0).getId())
                .status(orderCapture.getStatus())
                .build();
    }

    private void validateConfig() {
        if (isBlank(config.getClientId()) || isBlank(config.getSecretKey()) || isBlank(config.getBaseUrl())) {
            throw new PaymentException(PaymentErrorCode.CONFIG_INCOMPLETE,
                    "PayPal configuration is incomplete");
        }
    }

    private String bearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String buildBasicAuthHeader() {
        String credentials = config.getClientId() + ":" + config.getSecretKey();
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String summarizePaypalError(RestClientResponseException ex) {
        if (ex == null) return "No PayPal response was received";
        String body = ex.getResponseBodyAsString();
        if (isBlank(body)) return ex.getStatusCode().toString();
        String compact = body.replaceAll("\\s+", " ").trim();
        return compact.length() <= 500 ? compact : compact.substring(0, 500);
    }

    private String summarizeClientException(RestClientException ex) {
        String message = ex.getMessage();
        if (isBlank(message) && ex.getCause() != null) message = ex.getCause().getMessage();
        if (isBlank(message)) return ex.getClass().getSimpleName();
        String compact = message.replaceAll("\\s+", " ").trim();
        return compact.length() <= 500 ? compact : compact.substring(0, 500);
    }

    private boolean isRetryableCaptureRejection(RestClientResponseException ex) {
        if (ex.getStatusCode().value() != 422) return false;
        String body = ex.getResponseBodyAsString();
        return body != null && body.contains("ORDER_NOT_APPROVED");
    }

    private boolean isAlreadyCaptured(RestClientResponseException ex) {
        if (ex.getStatusCode().value() != 422) return false;
        String body = ex.getResponseBodyAsString();
        return body != null && body.contains("ORDER_ALREADY_CAPTURED");
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(CAPTURE_RETRY_DELAY_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PaymentException(PaymentErrorCode.RETRY_INTERRUPTED,
                    "Unable to capture PayPal payment: retry interrupted", ex);
        }
    }
}
