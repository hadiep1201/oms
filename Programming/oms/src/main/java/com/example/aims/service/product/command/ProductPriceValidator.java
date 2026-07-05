package com.example.aims.service.product.command;

import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SRP: validates product price ratio rules only.
 * Coupling: data coupling (BigDecimal inputs).
 * Cohesion: functional cohesion.
 */
@Component
public class ProductPriceValidator {

    private static final BigDecimal MIN_PRICE_RATIO = new BigDecimal("0.30");
    private static final BigDecimal MAX_PRICE_RATIO = new BigDecimal("1.50");

    public void validate(BigDecimal originalValue, BigDecimal currentPrice) {
        BigDecimal min = originalValue.multiply(MIN_PRICE_RATIO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal max = originalValue.multiply(MAX_PRICE_RATIO).setScale(2, RoundingMode.HALF_UP);

        if (currentPrice.compareTo(min) < 0 || currentPrice.compareTo(max) > 0) {
            throw new AppException(
                    ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(),
                    "Current price must be between 30% and 150% of original value");
        }
    }
}
