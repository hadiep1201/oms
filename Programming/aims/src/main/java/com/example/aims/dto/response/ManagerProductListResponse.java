package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * SOLID Review:
 * - No clear SOLID violation. Read-only DTO for manager product list rows (id, title, category, price, stock, status).
 * - Follows SRP as a single-purpose data carrier between ProductQueryService and the manager UI.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManagerProductListResponse {
    Integer id;
    String title;
    String category;
    BigDecimal currentPrice;
    Integer stockQuantity;
    String status;
}
