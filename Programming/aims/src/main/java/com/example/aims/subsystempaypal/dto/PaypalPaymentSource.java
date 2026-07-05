package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PaypalOrderRequest, PaypalWalletSource, and
 * PayThroughPaymentGatewayService.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO wraps the PayPal wallet source object required by the PayPal order payload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalPaymentSource {

    @JsonProperty("paypal")
    PaypalWalletSource paypal;
}
