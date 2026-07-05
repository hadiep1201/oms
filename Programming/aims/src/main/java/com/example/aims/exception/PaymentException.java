package com.example.aims.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final String errorCode;
    private final PaymentErrorCode typedErrorCode;

    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.typedErrorCode = null;
    }

    public PaymentException(PaymentErrorCode code, String message) {
        super(message);
        this.errorCode = code.name();
        this.typedErrorCode = code;
    }

    public PaymentException(PaymentErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code.name();
        this.typedErrorCode = code;
    }
}
