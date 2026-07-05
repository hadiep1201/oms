package com.example.aims.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/*
 * Coupling/Cohesion:
 * - Low coupling, high cohesion.
 * Reason why:
 * - This class only represents delivery input fields and validation constraints.
 * - It does not depend on domain behavior or persistence details, so its responsibility is narrow and clear.
 *
 * SOLID Review:
 * - No clear SOLID violation is identified in this DTO.
 * Reason why:
 * - It contains delivery data and validation annotations only, with no business workflow logic.
 * Improvement direction:
 * - Keep this class as a request contract; do not add shipping fee or notification behavior here.
 */
public class DeliveryInfoRequest {

    @NotBlank(message = "Receiver name is required")
    String receiverName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email;

    @NotBlank(message = "Phone number is required")
    String phoneNumber;

    @NotBlank(message = "Address is required")
    String address;

    @NotBlank(message = "City is required")
    String city;

}
