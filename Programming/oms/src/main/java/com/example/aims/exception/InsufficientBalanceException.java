package com.example.aims.exception;

public class InsufficientBalanceException extends PaymentException {

    public InsufficientBalanceException(String errorCode, String message) {
        super(errorCode, message);
    }
}
