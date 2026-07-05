package com.example.aims.subsystemvietqr;

import com.example.aims.payment.IPaymentConfirmation;
import com.example.aims.payment.PaymentConfirmationCommand;
import com.example.aims.payment.PaymentConfirmationResult;
import com.example.aims.payment.PaymentMethod;
import com.example.aims.subsystemvietqr.dto.SyncResponse;
import com.example.aims.subsystemvietqr.dto.TokenResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter realizing IPaymentCallback for the VietQR subsystem.
 *
 * This is the ONLY place that knows VietQR's callback protocol: it parses the webhook
 * JSON, resolves the order reference, normalizes the data into a gateway-neutral
 * PaymentConfirmationCommand, delegates the business rules to IPaymentConfirmation, and
 * maps the neutral result back into VietQR's required acknowledgement shape (SyncResponse).
 * It also serves VietQR's token request.
 *
 * Keeping the protocol here means the application service (PayOrderConfirmationService)
 * stays gateway-neutral and reusable; adding another gateway adds another adapter.
 *
 * Cohesion: Functional - translate between the VietQR callback protocol and the neutral
 *           confirmation port.
 *
 * Coupling:
 * - Data coupling with IPaymentConfirmation (neutral command/result).
 * - Data coupling with IWebhookTokenStore (records the issued callback token).
 *
 * SOLID:
 * - SRP: VietQR callback protocol translation only; business rules live in the service.
 * - DIP: depends on the IPaymentConfirmation and IWebhookTokenStore abstractions.
 * - ISP/LSP: implements exactly IPaymentCallback, fully.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietQRCallbackAdapter implements IPaymentCallback {

    IPaymentConfirmation paymentConfirmation;
    IWebhookTokenStore tokenStore;

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static final long TOKEN_TTL_SECONDS = 3600L;

    // VietQR acknowledgement reason codes (protocol vocabulary).
    static final String ACK_INVALID_BODY = "INVALID_BODY";
    static final String ACK_ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    static final String ACK_INVALID_ORDER_STATE = "INVALID_ORDER_STATE";
    static final String ACK_INVALID_AMOUNT = "INVALID_AMOUNT";
    static final String ACK_TRANSACTION_FAILED = "TRANSACTION_FAILED";

    @Override
    public String onTransactionReceived(String payload) {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(payload);
        } catch (Exception e) {
            return buildErrorAck(ACK_INVALID_BODY, "Malformed webhook payload");
        }

        try {
            Integer orderId = resolveOrderId(root);
            if (orderId == null) {
                return buildErrorAck(ACK_INVALID_BODY, "Cannot resolve order from webhook payload");
            }

            Long paidAmount = extractAmount(root);
            String reference = root.hasNonNull("content")
                    ? root.get("content").asText()
                    : OrderReference.format(orderId);

            PaymentConfirmationCommand command = PaymentConfirmationCommand.builder()
                    .orderId(orderId)
                    .paidAmount(paidAmount)
                    .reference(reference)
                    .method(PaymentMethod.VIETQR.name())
                    .build();

            return toAck(paymentConfirmation.confirm(command));
        } catch (Exception e) {
            return buildErrorAck(ACK_TRANSACTION_FAILED, e.getMessage());
        }
    }

    @Override
    public String provideToken() {
        try {
            String token = UUID.randomUUID().toString().replace("-", "");
            tokenStore.issue(token, TOKEN_TTL_SECONDS);
            TokenResponse response = TokenResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .toastMessage("Token generated successfully")
                    .expiresIn((int) TOKEN_TTL_SECONDS)
                    .build();
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate token\"}";
        }
    }

    private String toAck(PaymentConfirmationResult result) {
        return switch (result.getOutcome()) {
            case SUCCESS -> buildSuccessAck(result.getRefTransactionId());
            case ORDER_NOT_FOUND -> buildErrorAck(ACK_ORDER_NOT_FOUND, result.getMessage());
            case INVALID_ORDER_STATE -> buildErrorAck(ACK_INVALID_ORDER_STATE, result.getMessage());
            case AMOUNT_MISMATCH -> buildErrorAck(ACK_INVALID_AMOUNT, result.getMessage());
        };
    }

    private Integer resolveOrderId(JsonNode root) {
        if (root.hasNonNull("orderId")) {
            String raw = root.get("orderId").asText().trim();
            try {
                return Integer.valueOf(raw);
            } catch (NumberFormatException ignored) {
                Integer parsed = OrderReference.parse(raw);
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        if (root.hasNonNull("content")) {
            return OrderReference.parse(root.get("content").asText());
        }
        return null;
    }

    private Long extractAmount(JsonNode root) {
        if (!root.hasNonNull("amount")) {
            return null;
        }
        JsonNode amountNode = root.get("amount");
        if (amountNode.isNumber()) {
            return amountNode.asLong();
        }
        try {
            return Long.parseLong(amountNode.asText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildSuccessAck(String refTransactionId) {
        try {
            return OBJECT_MAPPER.writeValueAsString(
                    SyncResponse.builder()
                            .error(false)
                            .errorReason(null)
                            .toastMessage("Transaction processed successfully")
                            .object(SyncResponse.TransactionObject.builder()
                                    .reftransactionid(refTransactionId)
                                    .build())
                            .build()
            );
        } catch (Exception e) {
            return "{\"error\":false,\"errorReason\":null,\"object\":{\"reftransactionid\":\""
                    + refTransactionId + "\"}}";
        }
    }

    private String buildErrorAck(String errorReason, String toastMessage) {
        try {
            return OBJECT_MAPPER.writeValueAsString(
                    SyncResponse.builder()
                            .error(true)
                            .errorReason(errorReason)
                            .toastMessage(toastMessage)
                            .object(null)
                            .build()
            );
        } catch (Exception ex) {
            return "{\"error\":true,\"errorReason\":\"INTERNAL\",\"object\":null}";
        }
    }
}