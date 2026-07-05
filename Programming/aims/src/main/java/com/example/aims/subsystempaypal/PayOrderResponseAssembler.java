package com.example.aims.subsystempaypal;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.entity.DeliveryInfo;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.subsystempaypal.dto.PaypalOrderResponse;
import com.example.aims.subsystempaypal.dto.UrlResponse;
import org.springframework.stereotype.Component;

@Component
public class PayOrderResponseAssembler {

    public PayOrderResponse toPayOrderResponse(Order order, Invoice invoice, PaymentTransaction transaction) {
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        return PayOrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(deliveryInfo != null ? deliveryInfo.getReceiverName() : null)
                .phoneNumber(deliveryInfo != null ? deliveryInfo.getPhoneNumber() : null)
                .shippingAddress(deliveryInfo != null ? deliveryInfo.getAddress() : null)
                .city(deliveryInfo != null ? deliveryInfo.getCity() : null)
                .totalAmount(invoice.getTotalAmount())
                .orderStatus(order.getStatus().name())
                .transactionId(transaction.getTransactionId())
                .transactionContent(transaction.getTransactionContent())
                .transactionDatetime(transaction.getTransactionDatetime())
                .paymentMethod(transaction.getMethod())
                .build();
    }

    public UrlResponse toUrlResponse(PaypalOrderResponse orderResponse) {
        return UrlResponse.builder()
                .url(orderResponse.getApproveLink())
                .build();
    }
}
