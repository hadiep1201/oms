package com.example.aims.service.product.command;

import com.example.aims.dto.response.ProductResponse;
import com.example.aims.entity.PhysicalProduct;
import com.example.aims.entity.Product;
import com.example.aims.enums.ProductType;
import org.springframework.stereotype.Component;

/**
 * SRP: maps Product entity to CUD response DTO only.
 * Coupling: data coupling with Product entity.
 * Cohesion: functional cohesion.
 */
@Component
public class ProductCommandResponseMapper {

    public ProductResponse toResponse(Product product, ProductType productType, String message) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .productType(productType)
                .title(product.getTitle())
                .category(product.getCategory())
                .generalDescription(product.getGeneralDescription())
                .imageUrl(product.getImageUrl())
                .originalValue(product.getOriginalValue())
                .currentPrice(product.getCurrentPrice())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .message(message);

        if (product instanceof PhysicalProduct pp) {
            builder.barcode(pp.getBarcode())
                    .weight(pp.getWeight())
                    .length(pp.getLength())
                    .height(pp.getHeight())
                    .width(pp.getWidth());
        }

        return builder.build();
    }
}
