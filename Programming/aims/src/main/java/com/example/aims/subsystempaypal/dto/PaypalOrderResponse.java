package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PayPalApiGateway and PayThroughPaymentGatewayService, which
 * parse the PayPal order response and return its approve URL to callers.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO contains the PayPal order fields needed by the application after order creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalOrderResponse {

    @JsonProperty("id")
    String id;

    @JsonProperty("status")
    String status;

    @JsonProperty("links")
    List<PaypalLink> links;

    String approveLink;
}
