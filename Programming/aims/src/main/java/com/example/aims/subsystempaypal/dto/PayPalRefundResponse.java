package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PayPalApiGateway and PayThroughPaymentGatewayService, which
 * parse and consume the PayPal refund result.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO contains only the PayPal refund response fields consumed by the subsystem.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayPalRefundResponse {

    @JsonProperty("id")
    String refundId;

    @JsonProperty("status")
    String status;
}
