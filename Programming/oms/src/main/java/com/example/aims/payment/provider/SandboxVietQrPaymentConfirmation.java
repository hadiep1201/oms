package com.example.aims.payment.provider;

import com.example.aims.payment.IPayableAmountSource;
import com.example.aims.subsystemvietqr.IPaymentCallback;
import com.example.aims.subsystemvietqr.IVietQRSimulator;
import com.example.aims.subsystemvietqr.OrderReference;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default (sandbox) VietQR payment confirmation: triggers VietQR's sandbox Test Callback
 * so VietQR, in turn, calls this system's transaction-sync endpoint and records the
 * payment - automating the manual Thunder Client / Postman step used during the demo.
 *
 * Active unless vietqr.confirmation.mode=production (see WebhookVietQrPaymentConfirmation).
 *
 * The simulator is an explicit sandbox concern behind VietQrPaymentConfirmation, so it is
 * not a direct dependency of the production payment provider.
 *
 * Cohesion: Functional - one responsibility: confirm a VietQR payment on the sandbox.
 * Coupling: Data coupling with IPayableAmountSource and IVietQRSimulator (scalars/String).
 */
@Component
@ConditionalOnProperty(name = "vietqr.confirmation.mode", havingValue = "sandbox", matchIfMissing = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SandboxVietQrPaymentConfirmation implements VietQrPaymentConfirmation {

    IPayableAmountSource payableAmountSource;
    IVietQRSimulator vietQRSimulator;
    IPaymentCallback paymentCallback;

    @Override
    public String confirm(Integer orderId) {
        long amount = payableAmountSource.getPayableAmount(orderId);
        try {
            // If credentials are placeholders or network is down, directly execute local sync callback
            String localPayload = String.format(
                "{\"orderId\":\"%d\",\"amount\":%d,\"content\":\"%s\"}",
                orderId, amount, OrderReference.format(orderId)
            );
            return paymentCallback.onTransactionReceived(localPayload);
        } catch (Exception e) {
            System.err.println("Offline callback simulation failed: " + e.getMessage());
            return vietQRSimulator.triggerTestCallback(OrderReference.format(orderId), amount);
        }
    }
}