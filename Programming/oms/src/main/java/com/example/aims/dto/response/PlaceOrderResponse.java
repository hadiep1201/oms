package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/*
 * Coupling/Cohesion:
 * - Low coupling, high cohesion.
 * Reason why:
 * - This response DTO is focused on returning order summary and invoice information after PlaceOrder completes.
 * - It does not know how data is calculated or persisted, so it stays loosely coupled to business implementation.
 */
public class PlaceOrderResponse {

    // Order info
    Integer orderId;
    String customerName;
    String phoneNumber;
    String shippingAddress;
    String city;
    String orderStatus;

    // Invoice info — hiển thị cho user xem trước khi thanh toán
    BigDecimal subTotal;         // Tổng tiền hàng (chưa VAT)
    BigDecimal vatAmount;        // Thuế VAT 10%
    BigDecimal shippingFee;      // Phí ship
    BigDecimal totalAmount;      // Tổng cộng phải trả
    boolean freeShippingApplied; // Có được miễn phí ship không

    // Danh sách sản phẩm trong đơn
    List<OrderItemDetail> orderItems;
}
