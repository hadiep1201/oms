/**
 * ProductQueryService
 *
 * Responsibilities:
 *   Handles read-only use cases: View Product Detail and Search for Product.
 *
 * Cohesion: Communicational Cohesion
 *   Both methods (getProductDetail, searchProducts) operate on the same
 *   entity (Product) and share the same data source (ProductRepository).
 *   They are grouped together because they work on the same data,
 *   not merely because they belong to the same feature category.
 *
 * Coupling: Data Coupling (with ProductRepository)
 *   This class depends solely on ProductRepository. All interactions
 *   with the repository use only primitive or simple value types
 *   (Integer id, String keyword, BigDecimal minPrice/maxPrice) as
 *   parameters — no control flags, no shared global state, no access
 *   to internal fields of other classes.
 *
 * SOLID Principles:
 *   - OCP: Compliant. The `getProductDetail` uses a polymorphically selected mapper registry (`ProductMapperRegistry`) to convert products to DTOs.
 *   - LSP: Compliant. Clients and the service interact with abstractions, not concrete subclasses directly.
 *   - ISP: Compliant. No unused interfaces are implemented.
 *   - DIP: Compliant for detail mapping. Depends on `ProductRepository` and `ProductMapperRegistry` abstractions.
 *   - SRP: Compliant. The class solely handles retrieving data from the repository. Parsing, validation, and DTO mapping are delegated to other components.
 */

package com.example.aims.service;

import com.example.aims.dto.response.*;
import com.example.aims.entity.*;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.ProductRepository;
import com.example.aims.mapper.ProductMapperRegistry;
import com.example.aims.mapper.ProductListMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductQueryService {

    static String STATUS_DELETED = "DELETED";

    ProductRepository productRepository;
    ProductMapperRegistry mapperRegistry;
    ProductListMapper productListMapper;

    /**
     * SOLID Review (manager list for CUD):
     * - SRP risk: query, status filter, and DTO mapping are inlined in this method rather than a dedicated mapper/filter.
     * - OCP risk: new manager list columns or filter rules require editing this method.
     * - LSP/ISP/DIP: no additional violation beyond class-level review (repository abstraction is acceptable).
     */
    public List<ManagerProductListResponse> getManagerProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .filter(p -> p.getStatus() == null
                        || !STATUS_DELETED.equalsIgnoreCase(p.getStatus()))
                .map(productListMapper::toManagerListResponse)
                .collect(Collectors.toList());
    }

    public List<ProductHomepageResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findAll();
        Collections.shuffle(products);
        return products.stream()
                .limit(20)
                .map(productListMapper::toHomepageResponse)
                .collect(Collectors.toList());
    }

    public ProductDetailResponse getProductDetail(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + id));

        return mapperRegistry.map(product);
    }

    public List<ProductSearchResponse> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products = productRepository.searchProducts(keyword.trim(), minPrice, maxPrice);

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No related products were found matching the keyword: " + keyword);
        }

        return products.stream()
                .map(productListMapper::toSearchResponse)
                .collect(Collectors.toList());
    }
}
