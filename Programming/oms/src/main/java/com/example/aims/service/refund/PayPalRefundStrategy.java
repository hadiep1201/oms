package com.example.aims.service.refund;

import com.example.aims.entity.Refund;
import com.example.aims.repository.RefundRepository;
import com.example.aims.subsystempaypal.IPayPalPaymentService;
import com.example.aims.subsystempaypal.dto.PayPalRefundResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayPalRefundStrategy implements IRefundStrategy {

    IPayPalPaymentService payPalPaymentService;
    RefundRepository refundRepository;

    @Override
    public RefundMethod getMethod() {
        return RefundMethod.PAYPAL;
    }

    @Override
    public Refund processRefund(RefundContext context) {
        PayPalRefundResponse response = payPalPaymentService.refundCapture(
                context.getTransaction().getExternalCaptureId(),
                context.getInvoice().getTotalAmount()
        );

        return refundRepository.save(Refund.builder()
                .paymentTransaction(context.getTransaction())
                .user(context.getManager())
                .status(response.getStatus())
                .refundType("PAYPAL")
                .amount(context.getInvoice().getTotalAmount())
                .note(composeNote("PayPal refund id: " + response.getRefundId(), context.getReason()))
                .build());
    }
}
