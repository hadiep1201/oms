package com.example.aims.service.notification;

import com.example.aims.entity.DeliveryInfo;
import com.example.aims.entity.Order;
import com.example.aims.entity.Refund;
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
public class OrderProcessingNotificationService {

    NotificationSenderResolver notificationSenderResolver;

    public void sendOrderApproved(Order order) {
        send(order, "AIMS Order Approved #" + order.getOrderId(), """
                <html>
                    <body>
                        <h2>AIMS Order Approved</h2>
                        <p>Xin chao %s,</p>
                        <p>Don hang #%s cua ban da duoc Product Manager chap nhan va se duoc xu ly giao hang.</p>
                    </body>
                </html>
                """.formatted(customerName(order), order.getOrderId()));
    }

    public void sendOrderRejected(Order order, Refund refund) {
        String refundMessage = refund != null
                ? "Refund status: " + safeHtml(refund.getStatus()) + ". " + safeHtml(refund.getNote())
                : "Refund information is not available.";
        send(order, "AIMS Order Rejected #" + order.getOrderId(), """
                <html>
                    <body>
                        <h2>AIMS Order Rejected</h2>
                        <p>Xin chao %s,</p>
                        <p>Don hang #%s cua ban da bi tu choi.</p>
                        <p>%s</p>
                    </body>
                </html>
                """.formatted(customerName(order), order.getOrderId(), refundMessage));
    }

    private void send(Order order, String subject, String content) {
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        if (deliveryInfo == null || deliveryInfo.getEmail() == null || deliveryInfo.getEmail().isBlank()) {
            log.warn("Skip sending order processing email for order #{} because recipient email is missing", order.getOrderId());
            return;
        }

        NotificationMessage message = NotificationMessage.builder()
                .recipientName(deliveryInfo.getReceiverName())
                .recipientAddress(deliveryInfo.getEmail())
                .subject(subject)
                .content(content)
                .build();
        try {
            notificationSenderResolver.resolve(NotificationChannelType.EMAIL).send(message);
        } catch (AppException ex) {
            log.error("Failed to send order processing email for order #{}: {}", order.getOrderId(), ex.getMessage());
        }
    }

    private String customerName(Order order) {
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        return deliveryInfo != null ? safeHtml(deliveryInfo.getReceiverName()) : "";
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
