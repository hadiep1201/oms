package com.example.aims.service.product.command;

import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.request.ProductWriteRequest;
import com.example.aims.entity.Product;
import com.example.aims.enums.ProductType;

/**
 * Strategy for type-specific product write operations (validate, build, update).
 * OCP: new product types add a new handler without modifying existing handlers.
 * LSP: each handler operates on its concrete subtype safely.
 */
public interface ProductTypeWriteHandler {

    ProductType getProductType();

    boolean supports(Product product);

    void validate(ProductWriteRequest request);

    Product build(CreateProductRequest request);

    void update(Product product, ProductWriteRequest request);
}
