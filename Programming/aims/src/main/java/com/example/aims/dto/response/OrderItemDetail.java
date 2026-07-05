package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/*
 * Coupling/Cohesion:
 * - Low coupling, high cohesion.
 * Reason why:
 * - This class only represents one line item in the PlaceOrder response.
 * - It is focused on output data and remains independent from calculation and persistence logic.
 */
public class OrderItemDetail {

    Integer productId;
    String productTitle;
    String imageUrl;
    Integer quantity;
    BigDecimal price;
    BigDecimal itemTotal;
}
