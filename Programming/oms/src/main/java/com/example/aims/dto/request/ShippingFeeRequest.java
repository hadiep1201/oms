package com.example.aims.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
 * - This class carries only the data needed to calculate shipping fee for PlaceOrder.
 * - Its single purpose is to define the request contract for shipping calculation, with no extra behavior.
 *
 * SOLID Review:
 * - No clear SOLID violation is identified in this DTO.
 * Reason why:
 * - It only describes the shipping-fee request contract and does not implement shipping policy.
 * Improvement direction:
 * - Keep calculation rules out of this DTO; put future shipping policies behind a ShippingFeeCalculator interface.
 */
public class ShippingFeeRequest {

    @NotEmpty(message = "Items list is required")
    @Valid
    List<OrderItemRequest> items;

    String address;

    @NotBlank(message = "City is required")
    String city;

}
