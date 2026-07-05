package com.example.aims.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ProductDetailResponse {
    Integer id;
    String title;
    String category;
    BigDecimal originalValue;
    BigDecimal currentPrice;
    String imageUrl;
    Integer stockQuantity;
    String generalDescription;
    String status;
    BigDecimal weight;
    BigDecimal length;
    BigDecimal height;
    BigDecimal width;
    String barcode;
}
