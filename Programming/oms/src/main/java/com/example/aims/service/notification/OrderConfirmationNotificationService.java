package com.example.aims.service.notification;

import com.example.aims.entity.DeliveryInfo;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderConfirmationNotificationService {

    NotificationSenderResolver notificationSenderResolver;

    public void sendOrderConfirmation(Order order, Invoice invoice, PaymentTransaction transaction) {
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        if (deliveryInfo == null || deliveryInfo.getEmail() == null || deliveryInfo.getEmail().isBlank()) {
            log.warn("Skip sending order confirmation email for order #{} because recipient email is missing", order.getOrderId());
            return;
        }

        NotificationMessage message = NotificationMessage.builder()
                .recipientName(deliveryInfo.getReceiverName())
                .recipientAddress(deliveryInfo.getEmail())
                .subject("AIMS Order Confirmation #" + order.getOrderId())
                .content(buildOrderConfirmationEmail(order, invoice, transaction))
                .build();

        try {
            notificationSenderResolver.resolve(NotificationChannelType.EMAIL).send(message);
            log.info("Sent order confirmation email for order #{} to {}", order.getOrderId(), deliveryInfo.getEmail());
        } catch (AppException ex) {
            log.error("Failed to send order confirmation email for order #{}: {}", order.getOrderId(), ex.getMessage());
        }
    }

    private String buildOrderConfirmationEmail(Order order, Invoice invoice, PaymentTransaction transaction) {
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        return """
                <html>
                    <body>
                        <h2>AIMS Order Confirmation</h2>
                        <p>Xin chao %s,</p>
                        <p>Don hang cua ban da duoc thanh toan thanh cong va dang cho xu ly.</p>
                        <p><strong>Order ID:</strong> %s</p>
                        <p><strong>Order Status:</strong> %s</p>
                        <p><strong>SubTotal:</strong> %s</p>
                        <p><strong>VAT:</strong> %s</p>
                        <p><strong>Shipping Fee:</strong> %s</p>
                        <p><strong>Total Amount:</strong> %s</p>
                        <p><strong>Transaction ID:</strong> %s</p>
                        <p><strong>Transaction Content:</strong> %s</p>
                        <p><strong>Transaction Datetime:</strong> %s</p>
                        <p><strong>Payment Method:</strong> %s</p>
                        <p><strong>Shipping Address:</strong> %s, %s</p>
                    </body>
                </html>
                """.formatted(
                deliveryInfo != null ? safeHtml(deliveryInfo.getReceiverName()) : "",
                order.getOrderId(),
                order.getStatus().name(),
                invoice.getSubTotal(),
                invoice.getVatAmount(),
                invoice.getShippingFee(),
                invoice.getTotalAmount(),
                transaction.getTransactionId(),
                safeHtml(transaction.getTransactionContent()),
                transaction.getTransactionDatetime(),
                safeHtml(transaction.getMethod()),
                deliveryInfo != null ? safeHtml(deliveryInfo.getAddress()) : "",
                deliveryInfo != null ? safeHtml(deliveryInfo.getCity()) : ""
        );
    }

    private String safeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
