package com.example.aims.mapper;

import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapperRegistry {

    private final List<ProductDetailMapper> mappers;
    private final DefaultProductMapper defaultMapper;

    @SuppressWarnings("unchecked")
    public ProductDetailResponse map(Product product) {
        for (ProductDetailMapper mapper : mappers) {
            if (mapper.supports(product.getClass()) && !(mapper instanceof DefaultProductMapper)) {
                return mapper.mapToResponse(product);
            }
        }
        return defaultMapper.mapToResponse(product);
    }
}
