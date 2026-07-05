package com.example.aims.subsystemvietqr;

import com.example.aims.payment.IPaymentConfirmation;
import com.example.aims.payment.PaymentConfirmationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * Testing strategy for VietQRCallbackAdapter (VietQR callback protocol translation).
 *
 * Units under test:
 *   - onTransactionReceived(String) : parse + resolve, delegate to IPaymentConfirmation,
 *                                     map the neutral result to a VietQR SyncResponse ack
 *   - provideToken()                : returns a token JSON for VietQR and records it
 *
 * Strategy: black-box. IPaymentConfirmation and IWebhookTokenStore are mocked so the test
 * exercises only the protocol mapping, not the business rules.
 *
 * Partitions:
 *   onTransactionReceived: [valid + SUCCESS]      -> ack error:false + reftransactionid
 *                          [valid + AMOUNT_MISMATCH] -> ack error:true (INVALID_AMOUNT)
 *                          [malformed JSON]        -> ack error:true (INVALID_BODY)
 *                          [order not resolvable]  -> ack error:true (INVALID_BODY)
 *   provideToken:          returns JSON with token fields, records the token
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VietQRCallbackAdapterTest {

    @Mock
    private IPaymentConfirmation paymentConfirmation;

    @Mock
    private IWebhookTokenStore tokenStore;

    @InjectMocks
    private VietQRCallbackAdapter adapter;

    // UT047
    @Test
    @DisplayName("UT047: onTransactionReceived - SUCCESS result is acked error:false with a reftransactionid")
    void onTransactionReceived_success_returnsSuccessAck() {
        when(paymentConfirmation.confirm(any()))
                .thenReturn(PaymentConfirmationResult.success("AIMS-TX-1"));

        String payload = "{\"orderId\":\"1\",\"content\":\"AIMS1\",\"amount\":122000}";

        String result = adapter.onTransactionReceived(payload);

        assertTrue(result.contains("\"error\":false"), "Success should ack error:false");
        assertTrue(result.contains("reftransactionid"), "Success ack must carry reftransactionid");
        assertTrue(result.contains("AIMS-TX-1"));
        verify(paymentConfirmation, times(1)).confirm(any());
    }

    // UT055
    @Test
    @DisplayName("UT055: onTransactionReceived - AMOUNT_MISMATCH result maps to error ack INVALID_AMOUNT")
    void onTransactionReceived_amountMismatch_returnsInvalidAmountAck() {
        when(paymentConfirmation.confirm(any()))
                .thenReturn(PaymentConfirmationResult.amountMismatch("Paid amount does not match order total for order 1"));

        String payload = "{\"orderId\":\"1\",\"content\":\"AIMS1\",\"amount\":999}";

        String result = adapter.onTransactionReceived(payload);

        assertTrue(result.contains("\"error\":true"));
        assertTrue(result.contains("INVALID_AMOUNT"));
    }

    // UT048
    @Test
    @DisplayName("UT048: onTransactionReceived - malformed JSON returns error ack (INVALID_BODY), no delegation")
    void onTransactionReceived_invalidJson_returnsErrorResponse() {
        String result = adapter.onTransactionReceived("NOT_VALID_JSON{{{{");

        assertTrue(result.contains("\"error\":true"));
        assertTrue(result.contains("INVALID_BODY"));
        verify(paymentConfirmation, never()).confirm(any());
    }

    // UT056
    @Test
    @DisplayName("UT056: onTransactionReceived - unresolvable order returns error ack (INVALID_BODY), no delegation")
    void onTransactionReceived_noOrderReference_returnsErrorResponse() {
        String result = adapter.onTransactionReceived("{\"amount\":122000}");

        assertTrue(result.contains("\"error\":true"));
        assertTrue(result.contains("INVALID_BODY"));
        verify(paymentConfirmation, never()).confirm(any());
    }

    // UT049
    @Test
    @DisplayName("UT049: provideToken - returns valid JSON with required token fields and records the token")
    void provideToken_returnsValidTokenJson() {
        String result = adapter.provideToken();

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("access_token"), "Response must contain access_token (snake_case)");
        assertTrue(result.contains("Bearer"), "Token type should be Bearer");
        assertTrue(result.contains("3600"), "Token should have 3600s expiry");
        verify(tokenStore, times(1)).issue(anyString(), eq(3600L));
    }
}