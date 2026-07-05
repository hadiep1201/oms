package com.example.aims.dto.response;

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
 * - This class only reports whether requested items are available and which items fail validation.
 * - The structure is dedicated to one response purpose and does not mix in persistence or pricing behavior.
 */
public class StockValidationResponse {

    boolean valid;
    List<UnavailableItemDetail> unavailableItems;
}
