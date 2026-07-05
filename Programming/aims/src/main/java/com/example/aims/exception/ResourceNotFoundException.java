package com.example.aims.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AimsException {
    public ResourceNotFoundException(String message) {
        super(404, HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(int code, String message) {
        super(code, HttpStatus.NOT_FOUND, message);
    }
}
