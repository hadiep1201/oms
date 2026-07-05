package com.example.aims.repository;

import com.example.aims.entity.DiscProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscProductRepository extends JpaRepository<DiscProduct, Integer> {
}
