package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOrderResponse {

    // Order info
    Integer orderId;
    String customerName;
    String email;
    String phoneNumber;
    String shippingAddress;
    String city;
    BigDecimal totalAmount;
    String orderStatus;

    // Transaction info — chỉ có sau khi thanh toán thành công
    Integer transactionId;
    String transactionContent;
    Timestamp transactionDatetime;
    String paymentMethod;
}
