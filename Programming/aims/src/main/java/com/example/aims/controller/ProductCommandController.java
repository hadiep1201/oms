package com.example.aims.controller;

import com.example.aims.dto.request.CreateProductRequest;
import com.example.aims.dto.request.DeleteProductRequest;
import com.example.aims.dto.request.UpdateProductRequest;
import com.example.aims.dto.response.ApiResponse;
import com.example.aims.dto.response.DeleteProductResponse;
import com.example.aims.dto.response.ProductResponse;
import com.example.aims.service.ProductCommandService;
import com.example.aims.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

/**
 * Coupling:
 * - Data coupling with ProductCommandService (passes id + CreateProductRequest / UpdateProductRequest / DeleteProductRequest)
 * - Data coupling with ApiResponse wrapper (returns structured HTTP response only)
 * Cohesion: Functional cohesion
 * Reason: Each public method maps to exactly one CUD use case (Create / Update / Delete Product).
 *         Controller only handles HTTP routing and delegates all business logic to ProductCommandService.
 *
 * SOLID Review:
 * - SRP: No clear violation. Each endpoint maps to one CUD use case; business rules stay in ProductCommandService.
 * - OCP: No clear violation. New CUD endpoints can be added without changing existing handler logic.
 * - LSP: Not applicable — no inheritance hierarchy in this controller.
 * - ISP: Not applicable — the controller does not implement a broad interface with unused methods.
 * - DIP: No clear violation. Depends on ProductCommandService abstraction injected via constructor
 *        (@RequiredArgsConstructor) rather than instantiating concrete dependencies directly.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCommandController {

    ProductCommandService productCommandService;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(
            @RequestBody @Valid CreateProductRequest request) {
        request.setCreatedByUserId(SecurityUtils.requireCurrentUserId());
        ProductResponse result = productCommandService.createProduct(request);
        return ApiResponse.<ProductResponse>builder()
                .result(result)
                .message(result.getMessage())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateProductRequest request) {
        request.setUpdatedByUserId(SecurityUtils.requireCurrentUserId());
        ProductResponse result = productCommandService.updateProduct(id, request);
        return ApiResponse.<ProductResponse>builder()
                .result(result)
                .message(result.getMessage())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DeleteProductResponse> deleteProduct(
            @PathVariable Integer id,
            @RequestBody @Valid DeleteProductRequest request) {
        request.setDeletedByUserId(SecurityUtils.requireCurrentUserId());
        DeleteProductResponse result = productCommandService.deleteProduct(id, request);
        return ApiResponse.<DeleteProductResponse>builder()
                .result(result)
                .message(result.getMessage())
                .build();
    }
}
