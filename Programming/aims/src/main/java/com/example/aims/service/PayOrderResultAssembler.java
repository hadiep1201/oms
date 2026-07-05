package com.example.aims.service;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.entity.DeliveryInfo;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

/**
 * Assembles a PayOrderResponse from the order aggregate for the Pay Order result
 * (UC007), feeding PayOrderService.getPayOrderResult.
 *
 * Extracted from PayOrderService so the service no longer mixes DTO-shaping with its
 * payment operations, and so each related object is dereferenced once into a local
 * (taming the Law of Demeter chains the inline version had,
 * e.g. order.getDeliveryInfo().getReceiverName()).
 *
 * Cohesion: Functional - one responsibility: map the order aggregate to PayOrderResponse.
 * Coupling: Data/Stamp coupling with Order and its parts; no shared mutable state.
 */
@Component
public class PayOrderResultAssembler {

    public PayOrderResponse toPayOrderResponse(Order order) {
        PayOrderResponse.PayOrderResponseBuilder builder = PayOrderResponse.builder()
                .orderId(order.getOrderId())
                .orderStatus(order.getStatus().name());

        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        if (deliveryInfo != null) {
            builder.customerName(deliveryInfo.getReceiverName())
                   .email(deliveryInfo.getEmail())
                   .phoneNumber(deliveryInfo.getPhoneNumber())
                   .shippingAddress(deliveryInfo.getAddress())
                   .city(deliveryInfo.getCity());
        }

        Invoice invoice = order.getInvoice();
        if (invoice != null) {
            builder.totalAmount(invoice.getTotalAmount());
            PaymentTransaction transaction = invoice.getPaymentTransaction();
            if (transaction != null) {
                builder.transactionId(transaction.getTransactionId())
                       .transactionContent(transaction.getTransactionContent())
                       .transactionDatetime(transaction.getTransactionDatetime())
                       .paymentMethod(transaction.getMethod());
            }
        }

        return builder.build();
    }
}