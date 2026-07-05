package com.example.aims.repository;

import com.example.aims.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minPrice IS NULL OR p.currentPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)")
    List<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice);
}
