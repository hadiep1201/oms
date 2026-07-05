package com.example.aims.repository;

import com.example.aims.entity.Order;
import com.example.aims.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("""
            select distinct o from Order o
            left join fetch o.invoice i
            left join fetch i.paymentTransaction
            left join fetch o.deliveryInfo
            left join fetch o.orderDetails od
            left join fetch od.product
            where o.orderId = :orderId
            """)
    Optional<Order> findDetailedByOrderId(@Param("orderId") Integer orderId);

    @Query("""
            select distinct o from Order o
            left join fetch o.invoice i
            left join fetch i.paymentTransaction
            where o.status = :status
            and o.createdDate < :cutoff
            """)
    List<Order> findByStatusCreatedBeforeWithInvoice(
            @Param("status") OrderStatus status,
            @Param("cutoff") Timestamp cutoff
    );

    @Query("""
            select distinct o from Order o
            left join fetch o.invoice i
            left join fetch i.paymentTransaction
            left join fetch o.deliveryInfo
            left join fetch o.orderDetails od
            where o.status = :status
            and o.createdDate < :cutoff
            """)
    List<Order> findDetailedByStatusCreatedBefore(
            @Param("status") OrderStatus status,
            @Param("cutoff") Timestamp cutoff
    );
}
