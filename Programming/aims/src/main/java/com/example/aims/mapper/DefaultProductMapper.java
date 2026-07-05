package com.example.aims.mapper;

import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class DefaultProductMapper implements ProductDetailMapper<Product> {

    @Override
    public boolean supports(Class<?> productClass) {
        return Product.class.equals(productClass);
    }

    @Override
    public ProductDetailResponse mapToResponse(Product product) {
        ProductDetailResponse.ProductDetailResponseBuilder<?, ?> builder = ProductDetailResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .category(product.getCategory())
                .originalValue(product.getOriginalValue())
                .currentPrice(product.getCurrentPrice())
                .imageUrl(product.getImageUrl())
                .stockQuantity(product.getStockQuantity())
                .generalDescription(product.getGeneralDescription())
                .status(product.getStatus());

        if (product instanceof com.example.aims.entity.PhysicalProduct pp) {
            builder.weight(pp.getWeight())
                   .length(pp.getLength())
                   .height(pp.getHeight())
                   .width(pp.getWidth())
                   .barcode(pp.getBarcode());
        }

        return builder.build();
    }
}
