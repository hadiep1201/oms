package com.example.aims.subsystempaypal.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling level(s): Data coupling.
 * Related class(es): data coupling with PayThroughPaymentGatewayService and PaymentGatewayController,
 * which return this approval URL response to the client.
 * Cohesion level: Functional cohesion.
 * Reason: This DTO returns only the approval URL needed by the caller after PayPal order creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UrlResponse {

    String url;
}
