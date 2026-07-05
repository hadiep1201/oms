package com.example.aims.service.product.command;

import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class ProductWriteSupport {

    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public void requireNonBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(
                    ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(),
                    fieldName + " is required");
        }
    }

    public void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new AppException(
                    ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(),
                    fieldName + " is required");
        }
    }

    public void validateDateFormat(String value, String fieldName) {
        try {
            LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new AppException(
                    ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(),
                    "Invalid date format for " + fieldName + ". Use yyyy-MM-dd");
        }
    }

    public java.sql.Date parseSqlDate(String value) {
        return java.sql.Date.valueOf(LocalDate.parse(value, DATE_FORMAT));
    }
}
