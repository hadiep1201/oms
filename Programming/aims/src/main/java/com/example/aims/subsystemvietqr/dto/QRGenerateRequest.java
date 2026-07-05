package com.example.aims.subsystemvietqr.dto;

import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.example.aims.subsystemvietqr.OrderReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRGenerateRequest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    String bankCode;
    String bankAccount;
    String userBankName;

    String content;
    int qrType = 0;
    long amount;
    String transType = "C";
    String orderId;

    public QRGenerateRequest(String bankCode, String bankAccount, String userBankName) {
        this.bankCode     = bankCode;
        this.bankAccount  = bankAccount;
        this.userBankName = userBankName;
    }

    public void validateRequestData() {
        if (bankCode == null || bankCode.isBlank()) {
            throw new PaymentException(PaymentErrorCode.CONFIG_INCOMPLETE, "bankCode must not be blank");
        }
        if (bankAccount == null || bankAccount.isBlank()) {
            throw new PaymentException(PaymentErrorCode.CONFIG_INCOMPLETE, "bankAccount must not be blank");
        }
        if (userBankName == null || userBankName.isBlank()) {
            throw new PaymentException(PaymentErrorCode.CONFIG_INCOMPLETE, "userBankName must not be blank");
        }
    }

    public String buildRequestString(Order order) {
        Invoice invoice = order.getInvoice();
        if (invoice == null) {
            throw new PaymentException(PaymentErrorCode.INVOICE_MISSING,
                    "Invoice not available for order " + order.getOrderId());
        }
        if (invoice.getTotalAmount() == null) {
            throw new PaymentException(PaymentErrorCode.INVOICE_AMOUNT_MISSING,
                    "Invoice total not available for order " + order.getOrderId());
        }

        this.orderId = order.getOrderId().toString();
        this.content = OrderReference.format(order.getOrderId());
        this.amount  = invoice.getTotalAmount().longValue();

        try {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put("bankCode",     bankCode);
            node.put("bankAccount",  bankAccount);
            node.put("content",      content);
            node.put("qrType",       qrType);
            node.put("userBankName", userBankName);
            node.put("amount",       amount);
            node.put("transType",    transType);
            node.put("orderId",      orderId);
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_RESPONSE_UNEXPECTED,
                    "Failed to serialize QR generate request: " + e.getMessage(), e);
        }
    }
}