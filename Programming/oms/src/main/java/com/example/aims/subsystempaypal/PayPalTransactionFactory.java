package com.example.aims.subsystempaypal;

import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.subsystempaypal.dto.PaypalCaptureResponse;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
public class PayPalTransactionFactory {

    public PaymentTransaction createCaptureTransaction(
            Order order,
            Invoice invoice,
            String paypalOrderId,
            PaypalCaptureResponse captureResponse
    ) {
        return PaymentTransaction.builder()
                .invoice(invoice)
                .method("PAYPAL")
                .transactionDatetime(Timestamp.from(Instant.now()))
                .transactionStatus("PAID")
                .transactionContent("Payment for Order #" + order.getOrderId())
                .externalOrderId(paypalOrderId)
                .externalCaptureId(captureResponse.getCaptureId())
                .build();
    }
}
