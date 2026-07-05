package com.example.aims.payment;

import lombok.Getter;

@Getter
public class PaymentConfirmationResult {

    public enum Outcome { SUCCESS, ORDER_NOT_FOUND, INVALID_ORDER_STATE, AMOUNT_MISMATCH }

    private final Outcome outcome;
    private final String refTransactionId;
    private final String message;

    private PaymentConfirmationResult(Outcome outcome, String refTransactionId, String message) {
        this.outcome = outcome;
        this.refTransactionId = refTransactionId;
        this.message = message;
    }

    public static PaymentConfirmationResult success(String refTransactionId) {
        return new PaymentConfirmationResult(Outcome.SUCCESS, refTransactionId, null);
    }

    public static PaymentConfirmationResult orderNotFound(String message) {
        return new PaymentConfirmationResult(Outcome.ORDER_NOT_FOUND, null, message);
    }

    public static PaymentConfirmationResult invalidOrderState(String message) {
        return new PaymentConfirmationResult(Outcome.INVALID_ORDER_STATE, null, message);
    }

    public static PaymentConfirmationResult amountMismatch(String message) {
        return new PaymentConfirmationResult(Outcome.AMOUNT_MISMATCH, null, message);
    }
}