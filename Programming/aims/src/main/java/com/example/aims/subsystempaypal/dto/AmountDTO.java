package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PurchaseUnit and PayPalRefundRequest, which embed AmountDTO
 * as their amount value.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO carries only the currency and amount fields required by PayPal amount payloads.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AmountDTO {

    @JsonProperty("currency_code")
    String currencyCode;

    @JsonProperty("value")
    String value;
}
