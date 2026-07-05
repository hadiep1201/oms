package com.example.aims.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManagerOrderResponse {

    Integer orderId;
    String customerName;
    String email;
    String phoneNumber;
    String shippingAddress;
    String city;
    BigDecimal totalAmount;
    String orderStatus;
    Timestamp createdDate;
    String paymentMethod;
    Integer transactionId;
    String transactionStatus;
    String refundStatus;
    String refundType;
    String refundNote;
    List<ManagerOrderItemResponse> orderItems;
}
