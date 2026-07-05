package com.example.aims.mapper;

import com.example.aims.dto.response.ManagerProductListResponse;
import com.example.aims.dto.response.ProductHomepageResponse;
import com.example.aims.dto.response.ProductSearchResponse;
import com.example.aims.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductListMapper {

    public ManagerProductListResponse toManagerListResponse(Product p) {
        return ManagerProductListResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .category(p.getCategory())
                .currentPrice(p.getCurrentPrice())
                .stockQuantity(p.getStockQuantity())
                .status(p.getStatus())
                .build();
    }

    public ProductHomepageResponse toHomepageResponse(Product p) {
        return ProductHomepageResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .category(p.getCategory())
                .imageUrl(p.getImageUrl())
                .originalValue(p.getOriginalValue())
                .currentPrice(p.getCurrentPrice())
                .build();
    }

    public ProductSearchResponse toSearchResponse(Product p) {
        return ProductSearchResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .category(p.getCategory())
                .currentPrice(p.getCurrentPrice())
                .imageUrl(p.getImageUrl())
                .originalValue(p.getOriginalValue())
                .stockQuantity(p.getStockQuantity())
                .status(p.getStatus())
                .build();
    }
}
