package com.example.aims.service.product.command;

import com.example.aims.dto.request.ProductWriteRequest;
import com.example.aims.entity.PhysicalProduct;
import com.example.aims.entity.Product;
import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;

abstract class AbstractProductWriteHandler implements ProductTypeWriteHandler {

    protected void applyCommonFields(Product product, ProductWriteRequest request) {
        product.setTitle(request.getTitle());
        product.setCategory(request.getCategory());
        product.setGeneralDescription(request.getGeneralDescription());
        product.setImageUrl(request.getImageUrl());
        product.setOriginalValue(request.getOriginalValue());
        product.setCurrentPrice(request.getCurrentPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(request.getStatus());

        if (product instanceof PhysicalProduct pp) {
            pp.setBarcode(request.getBarcode());
            pp.setWeight(request.getWeight());
            pp.setLength(request.getLength());
            pp.setHeight(request.getHeight());
            pp.setWidth(request.getWidth());
        }
    }

    protected <T extends Product> T requireSubtype(Product product, Class<T> type) {
        if (!type.isInstance(product)) {
            throw new AppException(
                    ErrorCode.PRODUCT_VALIDATION_FAILED.getCode(),
                    "Product type mismatch for " + getProductType());
        }
        return type.cast(product);
    }
}
