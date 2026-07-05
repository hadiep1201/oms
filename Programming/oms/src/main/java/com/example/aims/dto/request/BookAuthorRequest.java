package com.example.aims.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookAuthorRequest {

    @NotBlank(message = "Author name is required")
    String name;

    /** Format: yyyy-MM-dd */
    String dob;
}
