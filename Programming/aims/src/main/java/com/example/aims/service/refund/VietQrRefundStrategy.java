package com.example.aims.service.refund;

import com.example.aims.entity.Refund;
import com.example.aims.repository.RefundRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietQrRefundStrategy implements IRefundStrategy {

    RefundRepository refundRepository;

    @Override
    public RefundMethod getMethod() {
        return RefundMethod.VIETQR;
    }

    @Override
    public Refund processRefund(RefundContext context) {
        return refundRepository.save(Refund.builder()
                .paymentTransaction(context.getTransaction())
                .user(context.getManager())
                .status("MANUAL_REQUIRED")
                .refundType("MANUAL_VIETQR")
                .amount(context.getInvoice().getTotalAmount())
                .note(composeNote("Manual refund required for VietQR payment", context.getReason()))
                .build());
    }
}
