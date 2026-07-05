package com.example.aims.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling: Data coupling with ProductCommandController and ProductCommandService (passes deletedByUserId only)
 * Cohesion: Functional cohesion
 * Reason: Single-purpose DTO carrying only the data needed for Delete Product audit/history.
 *
 * SOLID Review:
 * - No clear SOLID violation. Single field (deletedByUserId) with one responsibility.
 * - Follows SRP and ISP naturally as a minimal, focused request DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteProductRequest {

    @NotNull(message = "Deleted by user ID is required")
    Integer deletedByUserId;
}
