package com.example.aims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSearchRequest {
    @NotBlank(message = "Invalid search keyword. Please re-enter a valid keyword.")
    @Pattern(regexp = "^[\\p{L}\\p{N} ]+$", message = "Invalid search keyword. Please re-enter a valid keyword.")
    private String keyword;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
