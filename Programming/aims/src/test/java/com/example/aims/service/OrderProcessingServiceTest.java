package com.example.aims.service;

import com.example.aims.dto.response.ManagerOrderResponse;
import com.example.aims.entity.DeliveryInfo;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.OrderDetail;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.entity.Product;
import com.example.aims.entity.Refund;
import com.example.aims.entity.User;
import com.example.aims.enums.InvoiceStatus;
import com.example.aims.enums.OrderStatus;
import com.example.aims.repository.OrderRepository;
import com.example.aims.repository.ProductRepository;
import com.example.aims.repository.RefundRepository;
import com.example.aims.repository.UserRepository;
import com.example.aims.service.notification.OrderProcessingNotificationService;
import com.example.aims.subsystempaypal.PayThroughPaymentGatewayService;
import com.example.aims.subsystempaypal.dto.PayPalRefundResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PayThroughPaymentGatewayService payThroughPaymentGatewayService;

    @Mock
    private OrderProcessingNotificationService orderProcessingNotificationService;

    @InjectMocks
    private OrderProcessingService orderProcessingService;

    @Test
    void approveOrder_WhenPendingProcessing_DeductsStockAndApprovesOrder() {
        Order order = buildOrder("PAYPAL", OrderStatus.PENDING_PROCESSING, 10, 2);
        given(userRepository.findById(7)).willReturn(Optional.of(buildManager()));
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        ManagerOrderResponse response = orderProcessingService.approveOrder(100, 7);

        Product product = order.getOrderDetails().iterator().next().getProduct();
        assertThat(product.getStockQuantity()).isEqualTo(8);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
        assertThat(response.getOrderStatus()).isEqualTo("APPROVED");
        then(productRepository).should().saveAll(any());
        then(orderRepository).should().save(order);
        then(orderProcessingNotificationService).should().sendOrderApproved(order);
    }

    @Test
    void approveOrder_WhenStockIsInsufficient_ThrowsConflictWithoutSavingOrder() {
        Order order = buildOrder("PAYPAL", OrderStatus.PENDING_PROCESSING, 1, 2);
        given(userRepository.findById(7)).willReturn(Optional.of(buildManager()));
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderProcessingService.approveOrder(100, 7))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("insufficient stock");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PROCESSING);
        then(productRepository).should(never()).saveAll(any());
        then(orderRepository).should(never()).save(any(Order.class));
        then(orderProcessingNotificationService).should(never()).sendOrderApproved(any(Order.class));
    }

    @Test
    void rejectOrder_WhenPaypalPayment_RefundsThroughPaypalAndRejectsOrder() {
        Order order = buildOrder("PAYPAL", OrderStatus.PENDING_PROCESSING, 10, 2);
        User manager = buildManager();
        given(userRepository.findById(7)).willReturn(Optional.of(manager));
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));
        given(refundRepository.existsByPaymentTransaction_TransactionId(500)).willReturn(false);
        given(payThroughPaymentGatewayService.refundCapture("CAPTURE-1", new BigDecimal("120000")))
                .willReturn(PayPalRefundResponse.builder()
                        .refundId("REFUND-1")
                        .status("COMPLETED")
                        .build());
        given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        ManagerOrderResponse response = orderProcessingService.rejectOrder(100, 7, "Out of stock");

        ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
        then(refundRepository).should().save(refundCaptor.capture());
        Refund refund = refundCaptor.getValue();
        assertThat(refund.getStatus()).isEqualTo("COMPLETED");
        assertThat(refund.getRefundType()).isEqualTo("PAYPAL");
        assertThat(refund.getAmount()).isEqualByComparingTo("120000");
        assertThat(refund.getNote()).contains("REFUND-1").contains("Out of stock");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.getOrderStatus()).isEqualTo("REJECTED");
        assertThat(response.getRefundStatus()).isEqualTo("COMPLETED");
        then(orderProcessingNotificationService).should().sendOrderRejected(order, refund);
    }

    @Test
    void rejectOrder_WhenVietQrPayment_CreatesManualRefundAndRejectsOrder() {
        Order order = buildOrder("VIETQR", OrderStatus.PENDING_PROCESSING, 10, 2);
        User manager = buildManager();
        given(userRepository.findById(7)).willReturn(Optional.of(manager));
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));
        given(refundRepository.existsByPaymentTransaction_TransactionId(500)).willReturn(false);
        given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        ManagerOrderResponse response = orderProcessingService.rejectOrder(100, 7, "Cannot fulfill");

        ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
        then(refundRepository).should().save(refundCaptor.capture());
        Refund refund = refundCaptor.getValue();
        assertThat(refund.getStatus()).isEqualTo("MANUAL_REQUIRED");
        assertThat(refund.getRefundType()).isEqualTo("MANUAL_VIETQR");
        assertThat(refund.getNote()).contains("Manual refund required").contains("Cannot fulfill");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.getRefundType()).isEqualTo("MANUAL_VIETQR");
        then(payThroughPaymentGatewayService).should(never()).refundCapture(any(), any());
        then(orderProcessingNotificationService).should().sendOrderRejected(order, refund);
    }

    @Test
    void rejectOrder_WhenOrderIsNotPendingProcessing_ThrowsConflict() {
        Order order = buildOrder("PAYPAL", OrderStatus.APPROVED, 10, 2);
        given(userRepository.findById(7)).willReturn(Optional.of(buildManager()));
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderProcessingService.rejectOrder(100, 7, "Late reject"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only PENDING_PROCESSING orders can be processed");

        then(refundRepository).should(never()).save(any(Refund.class));
        then(orderRepository).should(never()).save(any(Order.class));
    }

    private Order buildOrder(String paymentMethod, OrderStatus status, int stockQuantity, int requestedQuantity) {
        Product product = Product.builder()
                .id(1)
                .title("Clean Architecture")
                .imageUrl("book.jpg")
                .stockQuantity(stockQuantity)
                .currentPrice(new BigDecimal("50000"))
                .build();

        Order order = Order.builder()
                .orderId(100)
                .status(status)
                .createdDate(Timestamp.from(Instant.parse("2026-06-18T08:00:00Z")))
                .shippingFee(new BigDecimal("20000"))
                .orderDetails(new HashSet<>())
                .build();

        OrderDetail detail = OrderDetail.builder()
                .id(10)
                .order(order)
                .product(product)
                .quantity(requestedQuantity)
                .price(new BigDecimal("50000"))
                .build();
        order.getOrderDetails().add(detail);

        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
                .order(order)
                .receiverName("Customer")
                .email("customer@example.com")
                .phoneNumber("0123456789")
                .address("1 Test Street")
                .city("Ha Noi")
                .build();
        order.setDeliveryInfo(deliveryInfo);

        Invoice invoice = Invoice.builder()
                .invoiceId(300)
                .order(order)
                .subTotal(new BigDecimal("100000"))
                .shippingFee(new BigDecimal("20000"))
                .vatAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("120000"))
                .status(InvoiceStatus.FINALIZED)
                .build();
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionId(500)
                .invoice(invoice)
                .method(paymentMethod)
                .transactionStatus("SUCCESS")
                .externalCaptureId("CAPTURE-1")
                .refunds(new HashSet<>())
                .build();
        invoice.setPaymentTransaction(transaction);
        order.setInvoice(invoice);

        return order;
    }

    private User buildManager() {
        return User.builder()
                .userId(7)
                .userName("manager")
                .email("manager@example.com")
                .hashedPassword("secret")
                .build();
    }
}
