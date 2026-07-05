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
public class QRAccessTokenResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    String accessToken;
    String tokenType;
    int expiresIn;

    public void parseResponseString(String response) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(response);

            if (root.has("access_token")) {
                this.accessToken = root.get("access_token").asText();
            } else if (root.has("accessToken")) {
                this.accessToken = root.get("accessToken").asText();
            }

            if (root.has("token_type")) {
                this.tokenType = root.get("token_type").asText();
            } else if (root.has("tokenType")) {
                this.tokenType = root.get("tokenType").asText();
            }

            if (root.has("expires_in")) {
                this.expiresIn = root.get("expires_in").asInt();
            } else if (root.has("expiresIn")) {
                this.expiresIn = root.get("expiresIn").asInt();
            }

        } catch (Exception e) {
            throw new PaymentException(PaymentErrorCode.GATEWAY_RESPONSE_UNEXPECTED,
                    "Failed to parse access token response: " + e.getMessage(), e);
        }
    }
}