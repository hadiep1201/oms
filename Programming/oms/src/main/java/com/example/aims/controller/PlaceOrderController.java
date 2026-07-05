package com.example.aims.controller;

import com.example.aims.dto.request.OrderItemRequest;
import com.example.aims.dto.request.PlaceOrderRequest;
import com.example.aims.dto.request.ShippingFeeRequest;
import com.example.aims.dto.response.*;
import com.example.aims.service.PlaceOrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
/*
 * Coupling/Cohesion:
 * - Low coupling, high cohesion.
 * Reason why:
 * - This controller only accepts HTTP requests and delegates PlaceOrder use-case work to PlaceOrderService.
 * - It does not contain persistence or pricing logic, so its responsibility stays focused on API orchestration.
 *
 * SOLID Review:
 * - No clear SOLID violation is identified in this controller.
 * Reason why:
 * - It follows SRP by handling HTTP routing only and delegates business rules to PlaceOrderService.
 * - It depends on one service abstraction at the application layer instead of repositories/entities directly.
 * Improvement direction:
 * - Keep controller methods thin; do not move validation, shipping, payment, or notification rules here.
 */
public class PlaceOrderController {

    PlaceOrderService placeOrderService;

    // 1. Kiểm tra tồn kho — FE gọi khi user bấm "Place Order"
    @PostMapping("/validate-stock")
    public ApiResponse<StockValidationResponse> validateStock(
            @RequestBody @Valid List<OrderItemRequest> items) {
        StockValidationResponse result = placeOrderService.validateOrderItems(items);
        return ApiResponse.<StockValidationResponse>builder()
                .result(result)
                .build();
    }

    // 2. Tính phí ship — FE gọi mỗi khi user thay đổi city
    @PostMapping("/shipping-fee")
    public ApiResponse<ShippingFeeResponse> calculateShippingFee(
            @RequestBody @Valid ShippingFeeRequest request) {
        ShippingFeeResponse result = placeOrderService.calculateShippingFee(request);
        return ApiResponse.<ShippingFeeResponse>builder()
                .result(result)
                .build();
    }

    // 3. Tạo đơn hàng + Invoice — FE gọi khi user xác nhận thông tin giao hàng
    //    Trả về Invoice để FE hiển thị trước khi thanh toán
    @PostMapping("/place")
    public ApiResponse<PlaceOrderResponse> placeOrder(
            @RequestBody @Valid PlaceOrderRequest request) {
        PlaceOrderResponse result = placeOrderService.placeOrder(request);
        return ApiResponse.<PlaceOrderResponse>builder()
                .result(result)
                .build();
    }

    // 4. Hoàn tất thanh toán — FE gọi sau khi payment gateway trả về PAYMENT_SUCCESS
    //    API này xử lý: cập nhật status sang PENDING_PROCESSING và gửi thông tin xác nhận
    @PutMapping("/{orderId}/recalculate")
    public ApiResponse<PlaceOrderResponse> recalculateDraftOrder(
            @PathVariable Integer orderId,
            @RequestBody @Valid PlaceOrderRequest request) {
        PlaceOrderResponse result = placeOrderService.recalculateDraftOrder(orderId, request);
        return ApiResponse.<PlaceOrderResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/{orderId}/finalize-payment")
    public ApiResponse<PayOrderResponse> finalizePaidOrder(
            @PathVariable Integer orderId) {
        PayOrderResponse result = placeOrderService.finalizePaidOrder(orderId);
        return ApiResponse.<PayOrderResponse>builder()
                .result(result)
                .build();
    }

}
