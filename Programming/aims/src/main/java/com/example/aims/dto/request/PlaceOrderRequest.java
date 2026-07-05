package com.example.aims.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/*
 * Coupling/Cohesion:
 * - Low coupling, high cohesion.
 * Reason why:
 * - This DTO only carries the input data required by the PlaceOrder use case.
 * - It groups related request fields for the API boundary without embedding business logic or infrastructure concerns.
 *
 * SOLID Review:
 * - No clear SOLID violation is identified.
 * Reason why:
 * - The class is a narrow request DTO and does not contain business logic.
 * - It avoids depending on payment implementation details because payment method selection belongs to the payment flow.
 * Improvement direction:
 * - Keep this request focused on order items and delivery information only.
 */
public class PlaceOrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    List<OrderItemRequest> items;

    @NotNull(message = "Delivery info is required")
    @Valid
    DeliveryInfoRequest deliveryInfo;
}
