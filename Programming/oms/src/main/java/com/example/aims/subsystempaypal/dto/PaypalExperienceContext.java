package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PaypalWalletSource and PayThroughPaymentGatewayService,
 * which provide checkout return and cancel URLs.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO groups redirect URLs used by the PayPal checkout experience context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalExperienceContext {

    @JsonProperty("return_url")
    String returnUrl;

    @JsonProperty("cancel_url")
    String cancelUrl;
}
