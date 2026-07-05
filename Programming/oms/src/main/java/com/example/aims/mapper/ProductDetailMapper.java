package com.example.aims.mapper;

import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.entity.Product;

public interface ProductDetailMapper<T extends Product> {
    
    /**
     * Checks if this mapper supports mapping the given product class.
     *
     * @param productClass the class of the product to check
     * @return true if supported, false otherwise
     */
    boolean supports(Class<?> productClass);

    /**
     * Maps the product to its specific detail response.
     *
     * @param product the product entity
     * @return the mapped response DTO
     */
    ProductDetailResponse mapToResponse(T product);
}
