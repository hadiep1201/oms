package com.example.aims.exception;

import com.example.aims.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    @Test
    void handleResponseStatusException_preservesStatusCodeAndReason() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "PayPal response does not contain approve or payer-action URL"
        );

        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(exception);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getBody().getCode());
        assertEquals("PayPal response does not contain approve or payer-action URL", response.getBody().getMessage());
    }
}
