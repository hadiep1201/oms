package com.example.aims.controller;

import com.example.aims.dto.request.ApproveOrderRequest;
import com.example.aims.dto.request.RejectOrderRequest;
import com.example.aims.dto.response.ApiResponse;
import com.example.aims.dto.response.ManagerOrderPageResponse;
import com.example.aims.dto.response.ManagerOrderResponse;
import com.example.aims.service.OrderProcessingService;
import com.example.aims.service.OrderQueryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ManagerOrderController {

    OrderQueryService orderQueryService;
    OrderProcessingService orderProcessingService;

    @GetMapping
    public ApiResponse<ManagerOrderPageResponse> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        ManagerOrderPageResponse result = orderQueryService.getPendingOrders(page, size);
        return ApiResponse.<ManagerOrderPageResponse>builder().result(result).build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<ManagerOrderResponse> getOrder(@PathVariable Integer orderId) {
        ManagerOrderResponse result = orderQueryService.getOrder(orderId);
        return ApiResponse.<ManagerOrderResponse>builder().result(result).build();
    }

    @PostMapping("/{orderId}/approve")
    public ApiResponse<ManagerOrderResponse> approveOrder(
            @PathVariable Integer orderId,
            @RequestBody @Valid ApproveOrderRequest request
    ) {
        ManagerOrderResponse result = orderProcessingService.approveOrder(orderId, request.getManagerUserId());
        return ApiResponse.<ManagerOrderResponse>builder().result(result).build();
    }

    @PostMapping("/{orderId}/reject")
    public ApiResponse<ManagerOrderResponse> rejectOrder(
            @PathVariable Integer orderId,
            @RequestBody @Valid RejectOrderRequest request
    ) {
        ManagerOrderResponse result = orderProcessingService.rejectOrder(
                orderId,
                request.getManagerUserId(),
                request.getReason()
        );
        return ApiResponse.<ManagerOrderResponse>builder().result(result).build();
    }
}
