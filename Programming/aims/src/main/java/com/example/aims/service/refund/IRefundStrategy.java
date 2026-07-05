package com.example.aims.service.refund;

import com.example.aims.entity.Refund;

public interface IRefundStrategy {

    RefundMethod getMethod();

    Refund processRefund(RefundContext context);

    default String composeNote(String prefix, String reason) {
        if (reason == null || reason.isBlank()) {
            return prefix;
        }
        return prefix + ". Reason: " + reason.trim();
    }
}
