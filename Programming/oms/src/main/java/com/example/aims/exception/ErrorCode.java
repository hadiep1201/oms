package com.example.aims.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(4000, "Invalid request"),
    UNAUTHORIZED(4007, "Unauthorized"),
    INVALID_CREDENTIALS(4008, "Invalid username or password"),
    USER_INACTIVE(4009, "User account is not active"),
    FORBIDDEN(4010, "Access denied"),
    PRODUCT_VALIDATION_FAILED(4001, "Product validation failed"),
    PRODUCT_NOT_FOUND(4002, "Product not found"),
    PRODUCT_TYPE_CHANGE_NOT_ALLOWED(4003, "Cannot change product type"),
    USER_NOT_FOUND(4004, "User not found"),
    PRODUCT_IN_ORDER(4005, "Cannot delete product that exists in orders"),
    PRODUCT_ALREADY_DELETED(4006, "Product already deleted"),
    ORDER_NOT_FOUND(4100, "Order not found"),
    ORDER_EXPIRED(4101, "Order has expired and can no longer be recalculated"),
    ORDER_ALREADY_PAID(4102, "Order can no longer be recalculated because payment has already progressed"),
    INVOICE_NOT_FOUND(4103, "Invoice not found"),
    INVOICE_VOID(4104, "Invoice has been voided and can no longer be recalculated"),
    INVOICE_FINALIZED(4105, "Invoice has already been finalized and can no longer be recalculated"),
    CANNOT_SEND_EMAIL(5100, "Cannot send email"),
    UNCATEGORIZED(5000, "Uncategorized error");

    private final int code;
    private final String message;
}
