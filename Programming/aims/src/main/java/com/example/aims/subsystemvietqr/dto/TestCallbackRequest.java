package com.example.aims.subsystemvietqr.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestCallbackRequest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TRANS_TYPE_CREDIT = "C";

    private final String bankAccount;
    private final String bankCode;
    private final String content;
    private final long amount;

    public TestCallbackRequest(String bankAccount, String bankCode, String content, long amount) {
        this.bankAccount = bankAccount;
        this.bankCode = bankCode;
        this.content = content;
        this.amount = amount;
    }

    public String buildRequestString() {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("bankAccount", bankAccount);
        node.put("content", content);
        node.put("amount", amount);
        node.put("bankCode", bankCode);
        node.put("transType", TRANS_TYPE_CREDIT);
        return node.toString();
    }
}