package com.example.aims.exception;

public class UserCancelledException extends PaymentException {

    public UserCancelledException(String errorCode, String message) {
        super(errorCode, message);
    }
}
