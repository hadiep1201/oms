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
 * - This DTO exists only to return shipping fee calculation results.
 * - It has a single, narrow responsibility and no dependency on domain workflows.
 */
public class ShippingFeeResponse {

    BigDecimal shippingFee;
    boolean freeShippingApplied;
}
