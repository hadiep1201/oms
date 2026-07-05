package com.example.aims.exception;

import org.springframework.http.HttpStatus;

public class ProductNotAvailableException extends PlaceOrderException {

    public ProductNotAvailableException(String message) {
        super(409, HttpStatus.CONFLICT, message);
    }
}
