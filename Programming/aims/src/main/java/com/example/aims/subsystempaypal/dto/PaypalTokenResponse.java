package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PayPalApiGateway and PayThroughPaymentGatewayService, which
 * obtain and pass the access token to PayPal API calls.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO carries OAuth token fields returned by PayPal and used by gateway calls.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalTokenResponse {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("expires_in")
    int expiresIn;
}
