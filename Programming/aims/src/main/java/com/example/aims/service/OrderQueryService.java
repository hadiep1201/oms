package com.example.aims.service;

import com.example.aims.dto.response.ManagerOrderPageResponse;
import com.example.aims.dto.response.ManagerOrderResponse;
import com.example.aims.entity.Order;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderQueryService {

    OrderRepository orderRepository;
    OrderResponseMapper responseMapper;

    @Transactional(readOnly = true)
    public ManagerOrderPageResponse getPendingOrders(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 30 : Math.min(size, 30);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING_PROCESSING, pageable);

        return ManagerOrderPageResponse.builder()
                .orders(orders.getContent().stream().map(responseMapper::toResponse).toList())
                .page(orders.getNumber())
                .size(orders.getSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public ManagerOrderResponse getOrder(Integer orderId) {
        return responseMapper.toResponse(loadDetailedOrder(orderId));
    }

    Order loadDetailedOrder(Integer orderId) {
        return orderRepository.findDetailedByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
