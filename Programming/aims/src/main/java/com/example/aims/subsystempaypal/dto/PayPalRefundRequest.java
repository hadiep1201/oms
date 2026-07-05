package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with AmountDTO, PayThroughPaymentGatewayService, and
 * PayPalApiGateway, which build and send this refund request.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO represents the refund request body and depends only on AmountDTO as structured data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayPalRefundRequest {

    @JsonProperty("amount")
    AmountDTO amount;
}
