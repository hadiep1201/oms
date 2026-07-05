package com.example.aims.payment.provider;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.dto.response.PaymentCompletionResponse;
import com.example.aims.dto.response.PaymentInitiationResponse;
import com.example.aims.entity.QRCode;
import com.example.aims.payment.IPayOrderResult;
import com.example.aims.payment.PaymentCompletionCommand;
import com.example.aims.payment.PaymentFlowType;
import com.example.aims.payment.PaymentMethod;
import com.example.aims.payment.PaymentProvider;
import com.example.aims.subsystemvietqr.IVietQRPayment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * VietQR strategy of the common PaymentProvider abstraction.
 *
 * initiate(): generate a QR for the order (flowType QR) via the VietQR-specific port.
 * complete(): confirm the payment via the VietQrPaymentConfirmation strategy (sandbox
 *             triggers the Test Callback; production relies on the real webhook), then
 *             read the pay-order view via the gateway-neutral IPayOrderResult port.
 *
 * SOLID:
 * - DIP: depends on IVietQRPayment, IPayOrderResult and VietQrPaymentConfirmation
 *   abstractions; no dependency on any concrete adapter or simulator.
 * - SRP: only maps the common payment contract onto the VietQR operations.
 *
 * Cohesion: Functional - VietQR's view of initiate/complete.
 * Coupling: Data coupling - exchanges orderId / value objects / DTOs.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietQrPaymentProvider implements PaymentProvider {

    IVietQRPayment vietQRPayment;
    IPayOrderResult payOrderResult;
    VietQrPaymentConfirmation paymentConfirmation;

    @Override
    public PaymentMethod method() {
        return PaymentMethod.VIETQR;
    }

    @Override
    public PaymentInitiationResponse initiate(Integer orderId) {
        QRCode qrCode = vietQRPayment.payOrder(orderId);
        return PaymentInitiationResponse.builder()
                .method(method())
                .flowType(PaymentFlowType.QR)
                .qrCode(qrCode)
                .build();
    }

    @Override
    public PaymentCompletionResponse complete(PaymentCompletionCommand command) {
        String verdict = paymentConfirmation.confirm(command.getOrderId());
        PayOrderResponse order = payOrderResult.getPayOrderResult(command.getOrderId());
        return PaymentCompletionResponse.builder()
                .method(method())
                .order(order)
                .providerVerdict(verdict)
                .build();
    }
}