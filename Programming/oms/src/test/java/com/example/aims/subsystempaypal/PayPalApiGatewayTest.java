package com.example.aims.subsystempaypal;

import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.example.aims.subsystempaypal.dto.AmountDTO;
import com.example.aims.subsystempaypal.dto.PaypalCaptureResponse;
import com.example.aims.subsystempaypal.dto.PaypalOrderRequest;
import com.example.aims.subsystempaypal.dto.PaypalOrderResponse;
import com.example.aims.subsystempaypal.dto.PaypalTokenResponse;
import com.example.aims.subsystempaypal.dto.PurchaseUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PayPalApiGatewayTest {

    private static final String BASE_URL = "https://api.paypal.test";
    private static final String CLIENT_ID = "client-id";
    private static final String SECRET_KEY = "secret-key";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String PAYPAL_ORDER_ID = "paypal-order-123";
    private static final String PAYPAL_CAPTURE_ID = "paypal-capture-456";
    private static final String APPROVE_URL = "https://paypal.test/checkoutnow?token=paypal-order-123";
    private static final String PAYER_ACTION_URL = "https://paypal.test/payer-action?token=paypal-order-123";

    private MockRestServiceServer server;
    private PayPalApiGateway gateway;

    @BeforeEach
    void setUp() {
        PaypalConfig config = new PaypalConfig();
        config.setClientId(CLIENT_ID);
        config.setSecretKey(SECRET_KEY);
        config.setBaseUrl(BASE_URL);

        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        gateway = new PayPalApiGateway(config, restClientBuilder.build());
    }

    @Test
    void getAccessToken_validPaypalCredentials_returnsTokenResponse() {
        server.expect(requestTo(BASE_URL + "/v1/oauth2/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, basicAuthHeader()))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andRespond(withSuccess("""
                        {
                          "access_token": "access-token",
                          "token_type": "Bearer",
                          "expires_in": 3600
                        }
                        """, MediaType.APPLICATION_JSON));

        PaypalTokenResponse result = gateway.getAccessToken();

        assertNotNull(result.getAccessToken());
        assertEquals(ACCESS_TOKEN, result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertTrue(result.getExpiresIn() > 0);
        server.verify();
    }

    @Test
    void getAccessToken_authenticationError_throwsPaymentException() {
        server.expect(requestTo(BASE_URL + "/v1/oauth2/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> gateway.getAccessToken()
        );

        assertEquals(PaymentErrorCode.GATEWAY_UNAVAILABLE, exception.getTypedErrorCode());
        server.verify();
    }

    @Test
    void createOrder_validPaypalOrderResponse_returnsOrderIdStatusAndApproveLink() {
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess("""
                        {
                          "id": "paypal-order-123",
                          "status": "CREATED",
                          "links": [
                            {
                              "href": "https://paypal.test/checkoutnow?token=paypal-order-123",
                              "rel": "approve"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PaypalOrderResponse result = gateway.createOrder(validOrderRequest(), ACCESS_TOKEN);

        assertEquals(PAYPAL_ORDER_ID, result.getId());
        assertEquals("CREATED", result.getStatus());
        assertEquals(APPROVE_URL, result.getApproveLink());
        server.verify();
    }

    @Test
    void createOrder_payerActionRequiredResponse_returnsPayerActionLink() {
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess("""
                        {
                          "id": "paypal-order-123",
                          "status": "PAYER_ACTION_REQUIRED",
                          "links": [
                            {
                              "href": "https://paypal.test/self/paypal-order-123",
                              "rel": "self"
                            },
                            {
                              "href": "https://paypal.test/payer-action?token=paypal-order-123",
                              "rel": "payer-action"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PaypalOrderResponse result = gateway.createOrder(validOrderRequest(), ACCESS_TOKEN);

        assertEquals(PAYPAL_ORDER_ID, result.getId());
        assertEquals("PAYER_ACTION_REQUIRED", result.getStatus());
        assertEquals(PAYER_ACTION_URL, result.getApproveLink());
        server.verify();
    }

    @Test
    void createOrder_missingRedirectLink_throwsPaymentExceptionWithReturnedRels() {
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "id": "paypal-order-123",
                          "status": "CREATED",
                          "links": [
                            {
                              "href": "https://paypal.test/self/paypal-order-123",
                              "rel": "self"
                            },
                            {
                              "href": "https://paypal.test/update/paypal-order-123",
                              "rel": "update"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> gateway.createOrder(validOrderRequest(), ACCESS_TOKEN)
        );

        assertEquals(PaymentErrorCode.GATEWAY_RESPONSE_UNEXPECTED, exception.getTypedErrorCode());
        assertTrue(exception.getMessage().contains("approve or payer-action URL"));
        assertTrue(exception.getMessage().contains("self, update"));
        server.verify();
    }

    @Test
    void createOrder_paypalApiError_throwsPaymentException() {
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> gateway.createOrder(validOrderRequest(), ACCESS_TOKEN)
        );

        assertEquals(PaymentErrorCode.GATEWAY_UNAVAILABLE, exception.getTypedErrorCode());
        server.verify();
    }

    @Test
    void capturePayment_orderAlreadyCaptured_fetchesOrderDetailsAndReturnsCapture() {
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders/" + PAYPAL_ORDER_ID + "/capture"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "name": "ORDER_ALREADY_CAPTURED",
                                  "message": "Order has already been captured."
                                }
                                """));
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders/" + PAYPAL_ORDER_ID))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess(completedCaptureResponse(), MediaType.APPLICATION_JSON));

        PaypalCaptureResponse result = gateway.capturePayment(PAYPAL_ORDER_ID, ACCESS_TOKEN);

        assertEquals(PAYPAL_CAPTURE_ID, result.getCaptureId());
        assertEquals("COMPLETED", result.getStatus());
        server.verify();
    }

    @Test
    void capturePayment_paypalApprovalNotVisibleYet_retriesAndReturnsCompletedCapture() {
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders/" + PAYPAL_ORDER_ID + "/capture"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{}"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "name": "ORDER_NOT_APPROVED",
                                  "message": "Payer has not approved the order for payment."
                                }
                                """));
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders/" + PAYPAL_ORDER_ID + "/capture"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{}"))
                .andRespond(withSuccess(completedCaptureResponse(), MediaType.APPLICATION_JSON));

        PaypalCaptureResponse result = gateway.capturePayment(PAYPAL_ORDER_ID, ACCESS_TOKEN);

        assertEquals(PAYPAL_CAPTURE_ID, result.getCaptureId());
        assertEquals("COMPLETED", result.getStatus());
        server.verify();
    }

    @Test
    void capturePayment_completedPaypalCapture_returnsCaptureIdAndCompletedStatus() {
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders/" + PAYPAL_ORDER_ID + "/capture"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andExpect(header("Prefer", "return=representation"))
                .andExpect(content().json("{}"))
                .andRespond(withSuccess(completedCaptureResponse(), MediaType.APPLICATION_JSON));

        PaypalCaptureResponse result = gateway.capturePayment(PAYPAL_ORDER_ID, ACCESS_TOKEN);

        assertEquals(PAYPAL_CAPTURE_ID, result.getCaptureId());
        assertEquals("COMPLETED", result.getStatus());
        server.verify();
    }

    private PaypalOrderRequest validOrderRequest() {
        return PaypalOrderRequest.builder()
                .intent("CAPTURE")
                .purchaseUnits(List.of(
                        PurchaseUnit.builder()
                                .customId("1001")
                                .invoiceId("2002")
                                .amount(AmountDTO.builder()
                                        .currencyCode("USD")
                                        .value("10.00")
                                        .build())
                                .build()
                ))
                .build();
    }

    private String completedCaptureResponse() {
        return """
                {
                  "status": "COMPLETED",
                  "purchase_units": [
                    {
                      "payments": {
                        "captures": [
                          {
                            "id": "paypal-capture-456",
                            "status": "COMPLETED"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;
    }

    private String basicAuthHeader() {
        String credentials = CLIENT_ID + ":" + SECRET_KEY;
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
