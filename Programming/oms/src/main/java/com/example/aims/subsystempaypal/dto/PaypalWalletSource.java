package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PaypalPaymentSource, PaypalExperienceContext, and
 * PayThroughPaymentGatewayService.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO groups wallet-specific checkout settings for the PayPal payment source.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalWalletSource {

    @JsonProperty("experience_context")
    PaypalExperienceContext experienceContext;
}
