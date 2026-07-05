/**
 * ProductQueryController
 *
 * Responsibilities:
 *   REST controller exposing HTTP endpoints for read-only product
 *   operations: View Product Detail and Search for Product.
 *
 * Cohesion: Communicational Cohesion
 *   Both handler methods (getProductDetail, searchProducts) delegate to
 *   the same service (ProductQueryService) and operate on the same
 *   resource type (Product). They are grouped because they share the
 *   same data source and responsibility scope, not just by coincidence.
 *
 * Coupling: Data Coupling (with ProductQueryService)
 *   This class depends solely on ProductQueryService. It passes only
 *   simple request parameters (Integer id, String keyword, String
 *   priceRange) to the service and receives DTO objects in return.
 *   No control flags, no shared mutable state, no access to internal
 *   implementation details of the service layer.
 *
 * SOLID Principles:
 *   - SRP: Compliant. The class only handles HTTP routing and parameter mapping.
 *   - OCP: Compliant. Adding new types of products does not require modifying this controller.
 *   - LSP: Compliant. No inheritance or type downcasting is performed here.
 *   - ISP: Compliant. No unused interfaces are implemented.
 *   - DIP: Violated. It depends directly on the concrete `ProductQueryService` instead of an interface abstraction.
 *     - Impact: Tight coupling makes unit testing difficult and limits flexibility to change service implementations.
 *     - Refactoring: Introduce an interface (e.g., `IProductQueryService`) and inject it here instead of the concrete class.
 *
 * SOLID Review (GET /api/products/manager — supports CUD manager list):
 * - SRP: No clear violation. getManagerProducts() only delegates to ProductQueryService.
 * - OCP: No clear violation. New read endpoints can be added without changing existing handlers.
 * - LSP/ISP: not applicable.
 * - DIP: Same as above — concrete ProductQueryService injection (acceptable for Spring prototype, weak for unit tests).
 */


package com.example.aims.controller;

import com.example.aims.dto.response.ApiResponse;
import com.example.aims.dto.response.ManagerProductListResponse;
import com.example.aims.dto.response.ProductDetailResponse;
import com.example.aims.dto.response.ProductHomepageResponse;
import com.example.aims.dto.response.ProductSearchResponse;
import com.example.aims.service.ProductQueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.Valid;
import com.example.aims.dto.request.ProductSearchRequest;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductQueryController {

    ProductQueryService productQueryService;

    @GetMapping("/featured")
    public ApiResponse<List<ProductHomepageResponse>> getFeaturedProducts() {
        List<ProductHomepageResponse> result = productQueryService.getFeaturedProducts();
        return ApiResponse.<List<ProductHomepageResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductSearchResponse>> searchProducts(@Valid @ModelAttribute ProductSearchRequest request) {
        List<ProductSearchResponse> result = productQueryService.searchProducts(
                request.getKeyword(), 
                request.getMinPrice(), 
                request.getMaxPrice()
        );
        return ApiResponse.<List<ProductSearchResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/manager")
    public ApiResponse<List<ManagerProductListResponse>> getManagerProducts() {
        List<ManagerProductListResponse> result = productQueryService.getManagerProducts();
        return ApiResponse.<List<ManagerProductListResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/{id:\\d+}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Integer id) {
        ProductDetailResponse result = productQueryService.getProductDetail(id);
        return ApiResponse.<ProductDetailResponse>builder()
                .result(result)
                .build();
    }
}
