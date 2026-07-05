package com.example.aims.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManagerOrderItemResponse {

    Integer productId;
    String productTitle;
    String imageUrl;
    Integer quantity;
    Integer availableQuantity;
    BigDecimal price;
    BigDecimal itemTotal;
}
