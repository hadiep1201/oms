package com.example.aims.exception;

import org.springframework.http.HttpStatus;

public class PlaceOrderException extends AimsException {

    public PlaceOrderException(int code, HttpStatus status, String message) {
        super(code, status, message);
    }
}
