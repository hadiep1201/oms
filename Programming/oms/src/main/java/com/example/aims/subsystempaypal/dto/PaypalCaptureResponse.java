package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PayPalApiGateway and PayThroughPaymentGatewayService;
 * its status field also creates control coupling in PayThroughPaymentGatewayService.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO carries the capture identifier and status extracted from a PayPal capture response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalCaptureResponse {

    @JsonProperty("id")
    String captureId;

    @JsonProperty("status")
    String status;
}
