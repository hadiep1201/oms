package com.example.aims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackRequest {

    @NotBlank(message = "Track title is required")
    String title;

    @NotNull(message = "Track length is required")
    @Positive(message = "Track length must be positive")
    Integer length;
}
