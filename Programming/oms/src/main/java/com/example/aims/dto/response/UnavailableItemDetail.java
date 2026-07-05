package com.example.aims.dto.response;

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
 * - This DTO only describes one unavailable item in the stock validation result.
 * - It is a small, focused data structure with no business or infrastructure dependency.
 */
public class UnavailableItemDetail {

    Integer productId;
    String productTitle;
    Integer requestedQuantity;
    Integer availableQuantity;
}
