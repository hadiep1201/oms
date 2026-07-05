package com.example.aims.service;

import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.request.DeleteProductRequest;
import com.example.aims.dto.request.UpdateProductRequest;
import com.example.aims.dto.response.DeleteProductResponse;
import com.example.aims.dto.response.ProductResponse;
import com.example.aims.entity.Product;
import com.example.aims.enums.ProductType;
import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.service.product.command.ProductCommandResponseMapper;
import com.example.aims.service.product.command.ProductHistoryRecorder;
import com.example.aims.service.product.command.ProductPriceValidator;
import com.example.aims.service.product.command.ProductTypeWriteHandler;
import com.example.aims.service.product.command.ProductTypeWriteHandlerRegistry;
import com.example.aims.repository.OrderDetailRepository;
import com.example.aims.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coupling:
 * - Data coupling with ProductRepository and OrderDetailRepository
 * - Data coupling with extracted command collaborators (validator, handlers, history, mapper)
 * Cohesion: functional cohesion — orchestrates Create / Update / Delete Product use cases only.
 *
 * SOLID Review:
 * - SRP: compliant — validation, type-specific logic, history, and response mapping are delegated.
 * - OCP: compliant — new product types extend via ProductTypeWriteHandler without editing this class.
 * - LSP: compliant — handlers operate on concrete subtypes without caller downcasting.
 * - ISP: partial — still accepts ProductWriteRequest at handler boundary; type-specific DTO split remains optional.
 * - DIP: compliant — depends on handler registry and command abstractions injected by Spring.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCommandService {

    static String STATUS_DEACTIVATED = "DEACTIVATED";
    static String STATUS_DELETED = "DELETED";

    ProductRepository productRepository;
    OrderDetailRepository orderDetailRepository;
    ProductPriceValidator priceValidator;
    ProductTypeWriteHandlerRegistry handlerRegistry;
    ProductHistoryRecorder historyRecorder;
    ProductCommandResponseMapper responseMapper;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        priceValidator.validate(request.getOriginalValue(), request.getCurrentPrice());

        ProductTypeWriteHandler handler = handlerRegistry.requireHandler(request.getProductType());
        handler.validate(request);

        Product product = handler.build(request);
        Product saved = productRepository.save(product);

        historyRecorder.record(saved, request.getActorUserId(), "ADD_PRODUCT");

        return responseMapper.toResponse(saved, request.getProductType(), "Product created successfully");
    }

    @Transactional
    public ProductResponse updateProduct(Integer id, UpdateProductRequest request) {
        priceValidator.validate(request.getOriginalValue(), request.getCurrentPrice());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.PRODUCT_NOT_FOUND.getCode(),
                        ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

        ensureProductNotDeleted(product);

        ProductTypeWriteHandler existingHandler = handlerRegistry.requireHandler(product);
        ProductType existingType = existingHandler.getProductType();
        if (existingType != request.getProductType()) {
            throw new AppException(
                    ErrorCode.PRODUCT_TYPE_CHANGE_NOT_ALLOWED.getCode(),
                    ErrorCode.PRODUCT_TYPE_CHANGE_NOT_ALLOWED.getMessage());
        }

        ProductTypeWriteHandler updateHandler = handlerRegistry.requireHandler(request.getProductType());
        updateHandler.validate(request);
        updateHandler.update(product, request);

        Product saved = productRepository.save(product);
        historyRecorder.record(saved, request.getActorUserId(), "UPDATE_PRODUCT");

        return responseMapper.toResponse(saved, existingType, "Product updated successfully");
    }

    @Transactional
    public DeleteProductResponse deleteProduct(Integer id, DeleteProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.PRODUCT_NOT_FOUND.getCode(),
                        ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

        String currentStatus = product.getStatus();
        if (STATUS_DELETED.equalsIgnoreCase(currentStatus)) {
            throw new AppException(
                    ErrorCode.PRODUCT_ALREADY_DELETED.getCode(),
                    ErrorCode.PRODUCT_ALREADY_DELETED.getMessage());
        }
        if (STATUS_DEACTIVATED.equalsIgnoreCase(currentStatus)) {
            throw new AppException(
                    ErrorCode.PRODUCT_ALREADY_DELETED.getCode(),
                    "Product is already deactivated");
        }

        if (orderDetailRepository.existsByProduct_Id(id)) {
            throw new AppException(
                    ErrorCode.PRODUCT_IN_ORDER.getCode(),
                    ErrorCode.PRODUCT_IN_ORDER.getMessage());
        }

        boolean outOfStock = product.getStockQuantity() != null && product.getStockQuantity() == 0;
        product.setStatus(outOfStock ? STATUS_DELETED : STATUS_DEACTIVATED);
        Product saved = productRepository.save(product);

        historyRecorder.record(saved, request.getDeletedByUserId(), "DELETE_PRODUCT");

        return DeleteProductResponse.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .message(outOfStock
                        ? "Product deleted successfully"
                        : "Product deactivated successfully")
                .build();
    }

    void ensureProductNotDeleted(Product product) {
        if (STATUS_DELETED.equalsIgnoreCase(product.getStatus())) {
            throw new AppException(
                    ErrorCode.PRODUCT_ALREADY_DELETED.getCode(),
                    ErrorCode.PRODUCT_ALREADY_DELETED.getMessage());
        }
    }
}
