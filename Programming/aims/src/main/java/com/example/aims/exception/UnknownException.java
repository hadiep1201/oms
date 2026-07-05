package com.example.aims.exception;

public class UnknownException extends PaymentException {

    public UnknownException(String errorCode, String message) {
        super(errorCode, message);
    }
}
