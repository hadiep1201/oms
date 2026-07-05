package com.example.aims.service;

import com.example.aims.dto.response.ManagerOrderItemResponse;
import com.example.aims.dto.response.ManagerOrderResponse;
import com.example.aims.entity.DeliveryInfo;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.OrderDetail;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.entity.Product;
import com.example.aims.entity.Refund;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderResponseMapper {

    public ManagerOrderResponse toResponse(Order order) {
        Invoice invoice = order.getInvoice();
        PaymentTransaction transaction = invoice != null ? invoice.getPaymentTransaction() : null;
        Refund refund = null;
        if (transaction != null && transaction.getRefunds() != null && !transaction.getRefunds().isEmpty()) {
            refund = transaction.getRefunds().iterator().next();
        }
        return toResponse(order, refund);
    }

    public ManagerOrderResponse toResponse(Order order, Refund refund) {
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        Invoice invoice = order.getInvoice();
        PaymentTransaction transaction = invoice != null ? invoice.getPaymentTransaction() : null;

        return ManagerOrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(deliveryInfo != null ? deliveryInfo.getReceiverName() : null)
                .email(deliveryInfo != null ? deliveryInfo.getEmail() : null)
                .phoneNumber(deliveryInfo != null ? deliveryInfo.getPhoneNumber() : null)
                .shippingAddress(deliveryInfo != null ? deliveryInfo.getAddress() : null)
                .city(deliveryInfo != null ? deliveryInfo.getCity() : null)
                .totalAmount(invoice != null ? invoice.getTotalAmount() : null)
                .orderStatus(order.getStatus() != null ? order.getStatus().name() : null)
                .createdDate(order.getCreatedDate())
                .paymentMethod(transaction != null ? transaction.getMethod() : null)
                .transactionId(transaction != null ? transaction.getTransactionId() : null)
                .transactionStatus(transaction != null ? transaction.getTransactionStatus() : null)
                .refundStatus(refund != null ? refund.getStatus() : null)
                .refundType(refund != null ? refund.getRefundType() : null)
                .refundNote(refund != null ? refund.getNote() : null)
                .orderItems(safeDetails(order).stream().map(this::toItemResponse).toList())
                .build();
    }

    private ManagerOrderItemResponse toItemResponse(OrderDetail detail) {
        Product product = detail.getProduct();
        BigDecimal itemTotal = detail.getPrice() == null || detail.getQuantity() == null
                ? null
                : detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
        return ManagerOrderItemResponse.builder()
                .productId(product != null ? product.getId() : null)
                .productTitle(product != null ? product.getTitle() : null)
                .imageUrl(product != null ? product.getImageUrl() : null)
                .availableQuantity(product != null ? product.getStockQuantity() : null)
                .quantity(detail.getQuantity())
                .price(detail.getPrice())
                .itemTotal(itemTotal)
                .build();
    }

    private List<OrderDetail> safeDetails(Order order) {
        if (order.getOrderDetails() == null) {
            return List.of();
        }
        return order.getOrderDetails().stream().toList();
    }
}
