package com.example.aims.dto.response;

import com.example.aims.enums.ProductType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Coupling: Data coupling with ProductCommandController and ProductCommandService (returns create/update result)
 * Cohesion: Functional cohesion
 * Reason: Carries only the fields needed to confirm a successful Create or Update Product operation.
 *
 * SOLID Review:
 * - No clear SOLID violation. Response DTO with no behavior; maps persisted product state for CUD confirmation.
 * - Follows SRP as a single-purpose data carrier for create/update outcomes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    Integer id;
    ProductType productType;
    String title;
    String category;
    String generalDescription;
    String barcode;
    String imageUrl;
    BigDecimal originalValue;
    BigDecimal currentPrice;
    BigDecimal weight;
    BigDecimal length;
    BigDecimal height;
    BigDecimal width;
    Integer stockQuantity;
    String status;
    String message;
}
