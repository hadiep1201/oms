package com.example.aims.subsystemvietqr.dto;


import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRGenerateResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    String qrCode;
    String qrLink;
    String bankCode;
    String bankName;
    String bankAccount;
    String userBankName;
    long   amount;
    String content;
    String transactionId;

    public void parseResponseString(String response) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(response);

            JsonNode data = root.has("data") && !root.get("data").isNull()
                    ? root.get("data")
                    : root;

            this.qrCode       = textOrNull(data, "qrCode");
            this.qrLink        = textOrNull(data, "qrLink");
            this.bankCode      = textOrNull(data, "bankCode");
            this.bankName      = textOrNull(data, "bankName");
            this.bankAccount   = textOrNull(data, "bankAccount");
            this.userBankName  = textOrNull(data, "userBankName");
            this.amount        = data.has("amount") ? data.get("amount").asLong() : 0;
            this.content       = textOrNull(data, "content");
            this.transactionId = textOrNull(data, "transactionId");

        } catch (Exception e) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_RESPONSE_UNEXPECTED,
                    "Failed to parse QR generate response: " + e.getMessage(), e);
        }
    }

    private String textOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull()
                ? node.get(field).asText()
                : null;
    }
}