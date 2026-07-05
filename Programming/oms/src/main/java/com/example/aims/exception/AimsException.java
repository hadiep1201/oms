package com.example.aims.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AimsException extends RuntimeException {

    private final int code;
    private final HttpStatus status;

    public AimsException(int code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
