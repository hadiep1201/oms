package com.example.aims.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/*
 * Coupling/Cohesion:
 * - Low coupling, high cohesion.
 * Reason why:
 * - This DTO contains only the minimal item input needed for PlaceOrder: product id and quantity.
 * - Passing only required fields helps avoid unnecessary stamp coupling at the request boundary.
 */
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    Integer productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity;
}
