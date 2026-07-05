package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PayThroughPaymentGatewayService, PayPalApiGateway,
 * PurchaseUnit, and PaypalPaymentSource.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO represents the PayPal order creation payload and composes only the nested request
 * data required by the PayPal API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalOrderRequest {

    @JsonProperty("intent")
    String intent;

    @JsonProperty("purchase_units")
    List<PurchaseUnit> purchaseUnits;

    @JsonProperty("payment_source")
    PaypalPaymentSource paymentSource;
}
