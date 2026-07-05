package com.example.aims.service.refund;

public enum RefundMethod {
    PAYPAL,
    VIETQR;

    public static RefundMethod from(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
