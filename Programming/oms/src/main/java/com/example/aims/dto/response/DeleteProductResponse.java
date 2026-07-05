package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Coupling: Data coupling with ProductCommandController and ProductCommandService (returns delete result)
 * Cohesion: Functional cohesion
 * Reason: Contains only id, status, and message for the Delete Product use case.
 *
 * SOLID Review:
 * - No clear SOLID violation. Minimal response DTO focused on delete outcome only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteProductResponse {

    Integer id;
    String status;
    String message;
}
