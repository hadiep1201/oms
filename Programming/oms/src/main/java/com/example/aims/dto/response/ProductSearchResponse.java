package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchResponse {
    Integer id;
    String title;
    String category;
    BigDecimal currentPrice;
    String imageUrl;
    BigDecimal originalValue;
    Integer stockQuantity;
    String status;
}
