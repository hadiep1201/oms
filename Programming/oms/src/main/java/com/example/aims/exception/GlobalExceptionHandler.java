package com.example.aims.exception;

import com.example.aims.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    ResponseEntity<ApiResponse<Void>> handlePaymentException(PaymentException ex) {
        PaymentErrorCode code = ex.getTypedErrorCode();
        HttpStatus status;
        if (code != null) {
            status = switch (code) {
                case ORDER_NOT_FOUND -> HttpStatus.NOT_FOUND;
                case UNSUPPORTED_PAYMENT_METHOD -> HttpStatus.BAD_REQUEST;
                case ORDER_NOT_PENDING_PAYMENT, INVOICE_MISSING, DUPLICATE_TRANSACTION,
                     INVOICE_AMOUNT_MISSING, REFUND_CAPTURE_ID_MISSING, REFUND_AMOUNT_INVALID,
                     CAPTURE_FAILED -> HttpStatus.CONFLICT;
                case CONFIG_INCOMPLETE, GATEWAY_UNAVAILABLE,
                     GATEWAY_RESPONSE_UNEXPECTED, RETRY_INTERRUPTED -> HttpStatus.BAD_GATEWAY;
            };
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(status.value())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AimsException.class)
    ResponseEntity<ApiResponse<Void>> handleAimsException(AimsException ex) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(ErrorCode.INVALID_REQUEST.getCode())
                .message(message)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponse<Object> body = ApiResponse.<Object>builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Object> body = ApiResponse.<Object>builder()
                .code(400)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(ex.getStatusCode().value())
                .message(ex.getReason() != null ? ex.getReason() : ex.getMessage())
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(ErrorCode.UNCATEGORIZED.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.internalServerError().body(body);
    }
}