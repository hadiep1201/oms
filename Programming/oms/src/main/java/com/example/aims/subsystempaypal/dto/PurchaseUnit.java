package com.example.aims.subsystempaypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PaypalOrderRequest, AmountDTO, and
 * PayThroughPaymentGatewayService.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO contains the purchase unit fields required to identify an order, invoice, and amount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseUnit {

    @JsonProperty("amount")
    AmountDTO amount;

    @JsonProperty("custom_id")
    String customId;

    @JsonProperty("invoice_id")
    String invoiceId;
}
