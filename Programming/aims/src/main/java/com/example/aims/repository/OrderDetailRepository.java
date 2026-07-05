package com.example.aims.repository;

import com.example.aims.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    boolean existsByProduct_Id(Integer productId);

    void deleteByOrder_OrderId(Integer orderId);
}
