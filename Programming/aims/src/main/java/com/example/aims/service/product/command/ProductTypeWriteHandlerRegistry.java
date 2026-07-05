package com.example.aims.service.product.command;

import com.example.aims.entity.Product;
import com.example.aims.enums.ProductType;
import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OCP: resolves handler by product type without switch in callers.
 * DIP: depends on ProductTypeWriteHandler abstraction list.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypeWriteHandlerRegistry {

    List<ProductTypeWriteHandler> handlers;

    public ProductTypeWriteHandler requireHandler(ProductType productType) {
        return handlers.stream()
                .filter(handler -> handler.getProductType() == productType)
                .findFirst()
                .orElseThrow(() -> new AppException(
                        ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(),
                        "Unsupported product type: " + productType));
    }

    public ProductTypeWriteHandler requireHandler(Product product) {
        return handlers.stream()
                .filter(handler -> handler.supports(product))
                .findFirst()
                .orElseThrow(() -> new AppException(
                        ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(),
                        "Unknown product type"));
    }
}
