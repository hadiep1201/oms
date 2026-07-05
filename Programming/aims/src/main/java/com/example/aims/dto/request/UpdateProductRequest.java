package com.example.aims.dto.request;

import com.example.aims.enums.ProductType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Coupling: Data coupling with ProductCommandController and ProductCommandService (transfers update payload only)
 * Cohesion: Communicational cohesion
 * Reason: Fields revolve around the same product data set required to update an existing product.
 *
 * SOLID Review:
 * - SRP: No clear violation. DTO carries update payload and actorUserId only.
 * - OCP: No clear violation at DTO level.
 * - LSP: Not applicable.
 * - ISP risk: same as CreateProductRequest — implements fat ProductWriteRequest with fields for all types.
 * - DIP: Not applicable.
 * Improvement direction: segregate update contracts by product type to avoid unused field exposure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProductRequest implements ProductWriteRequest {

    @NotNull(message = "Product type is required")
    ProductType productType;

    @NotBlank(message = "Title is required")
    String title;

    @NotBlank(message = "Category is required")
    String category;

    String generalDescription;

    String barcode;

    String imageUrl;

    @NotNull(message = "Original value is required")
    @DecimalMin(value = "0.01", message = "Original value must be greater than 0")
    BigDecimal originalValue;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.01", message = "Current price must be greater than 0")
    BigDecimal currentPrice;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    BigDecimal weight;

    @NotNull(message = "Length is required")
    @DecimalMin(value = "0.01", message = "Length must be greater than 0")
    BigDecimal length;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "0.01", message = "Height must be greater than 0")
    BigDecimal height;

    @NotNull(message = "Width is required")
    @DecimalMin(value = "0.01", message = "Width must be greater than 0")
    BigDecimal width;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity;

    @Builder.Default
    String status = "ACTIVE";

    @NotNull(message = "Updated by user ID is required")
    Integer updatedByUserId;

    String publicationDate;

    String language;

    String publisher;

    String coverType;

    @Min(value = 1, message = "Number of pages must be at least 1")
    Integer nbPages;

    String genre;

    @Valid
    List<BookAuthorRequest> authors;

    String editorInChief;

    String issueNumber;

    String publicationFrequency;

    String issn;

    @Valid
    List<SectionRequest> sections;

    String releaseDate;

    String artists;

    String recordLabel;

    @Valid
    List<TrackRequest> tracks;

    String discType;

    String director;

    @Min(value = 1, message = "Runtime must be at least 1 minute")
    Integer runtime;

    String studio;

    String subtitles;

    @Override
    public Integer getActorUserId() {
        return updatedByUserId;
    }
}
