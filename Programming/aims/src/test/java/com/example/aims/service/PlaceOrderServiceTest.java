package com.example.aims.service;

import com.example.aims.dto.request.DeliveryInfoRequest;
import com.example.aims.dto.request.OrderItemRequest;
import com.example.aims.dto.request.PlaceOrderRequest;
import com.example.aims.dto.request.ShippingFeeRequest;
import com.example.aims.dto.response.PlaceOrderResponse;
import com.example.aims.dto.response.ShippingFeeResponse;
import com.example.aims.dto.response.StockValidationResponse;
import com.example.aims.entity.*;
import com.example.aims.enums.InvoiceStatus;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.PaymentException;
import com.example.aims.exception.PlaceOrderException;
import com.example.aims.exception.ProductNotAvailableException;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DeliveryInfoRepository deliveryInfoRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private com.example.aims.service.notification.OrderConfirmationNotificationService orderConfirmationNotificationService;

    @Mock
    private com.example.aims.service.shipping.ShippingFeeStrategyResolver shippingFeeStrategyResolver;

    @InjectMocks
    private PlaceOrderService placeOrderService;

    private com.example.aims.entity.Book testProduct;
    private OrderItemRequest testItem;
    private DeliveryInfoRequest testDeliveryInfo;

    @BeforeEach
    void setUp() {
        testProduct = com.example.aims.entity.Book.builder()
                .id(1)
                .title("Test Product")
                .stockQuantity(10)
                .weight(new BigDecimal("1.0"))
                .currentPrice(new BigDecimal("50000"))
                .build();

        testItem = OrderItemRequest.builder()
                .productId(1)
                .quantity(2)
                .build();

        testDeliveryInfo = DeliveryInfoRequest.builder()
                .receiverName("Test User")
                .email("test@example.com")
                .phoneNumber("0123456789")
                .address("123 Test St")
                .city("Hà Nội")
                .build();
    }

    @Test
    @DisplayName("Validate stock with sufficient quantity (qty=2, stock=10)")
    void validateOrderItems_WhenStockIsEnough_ReturnsValid() {
        // given
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        // when
        StockValidationResponse response = placeOrderService.validateOrderItems(List.of(testItem));

        // then
        assertThat(response.isValid()).isTrue();
        assertThat(response.getUnavailableItems()).isEmpty();
    }

    @Test
    @DisplayName("Validate stock with insufficient quantity (qty=15, stock=10)")
    void validateOrderItems_WhenStockIsNotEnough_ReturnsInvalid() {
        // given
        testItem.setQuantity(15);
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        // when
        StockValidationResponse response = placeOrderService.validateOrderItems(List.of(testItem));

        // then
        assertThat(response.isValid()).isFalse();
        assertThat(response.getUnavailableItems()).hasSize(1);
        assertThat(response.getUnavailableItems().getFirst().getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("Validate stock with non-existent product (productId=1)")
    void validateOrderItems_WhenProductNotFound_ReturnsInvalid() {
        // given
        given(productRepository.findById(1)).willReturn(Optional.empty());

        // when
        StockValidationResponse response = placeOrderService.validateOrderItems(List.of(testItem));

        // then
        assertThat(response.isValid()).isFalse();
        assertThat(response.getUnavailableItems()).hasSize(1);
        assertThat(response.getUnavailableItems().getFirst().getProductTitle()).isEqualTo("Product not found");
    }

    @Test
    @DisplayName("Calculate shipping fee with Hanoi city (weight=2.0kg)")
    void calculateShippingFee_ForHanoi_ReturnsCorrectFee() {
        // given
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        ShippingFeeRequest request = ShippingFeeRequest.builder()
                .items(List.of(testItem))
                .city("Hà Nội")
                .build();
        com.example.aims.service.shipping.ShippingFeeCalculatorStrategy mockStrategy = org.mockito.Mockito.mock(com.example.aims.service.shipping.ShippingFeeCalculatorStrategy.class);
        given(shippingFeeStrategyResolver.resolve(any())).willReturn(mockStrategy);
        given(mockStrategy.calculate(any())).willReturn(ShippingFeeResponse.builder().shippingFee(new BigDecimal("22000")).freeShippingApplied(false).build());

        // when
        ShippingFeeResponse response = placeOrderService.calculateShippingFee(request);

        // then
        assertThat(response.getShippingFee()).isEqualTo(new BigDecimal("22000"));
        assertThat(response.isFreeShippingApplied()).isFalse();
    }

    @Test
    @DisplayName("Calculate shipping fee with subtotal above threshold (subtotal=120000)")
    void calculateShippingFee_WhenSubtotalAboveThreshold_AppliesFreeShipping() {
        // given
        testProduct.setCurrentPrice(new BigDecimal("60000")); 
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        ShippingFeeRequest request = ShippingFeeRequest.builder()
                .items(List.of(testItem))
                .city("Hà Nội")
                .build();
        com.example.aims.service.shipping.ShippingFeeCalculatorStrategy mockStrategy = org.mockito.Mockito.mock(com.example.aims.service.shipping.ShippingFeeCalculatorStrategy.class);
        given(shippingFeeStrategyResolver.resolve(any())).willReturn(mockStrategy);
        given(mockStrategy.calculate(any())).willReturn(ShippingFeeResponse.builder().shippingFee(new BigDecimal("0")).freeShippingApplied(true).build());

        // when
        ShippingFeeResponse response = placeOrderService.calculateShippingFee(request);

        // then
        assertThat(response.getShippingFee()).isEqualTo(new BigDecimal("0"));
        assertThat(response.isFreeShippingApplied()).isTrue();
    }

    @Test
    @DisplayName("Place order with valid request (qty=2, city=\"Hà Nội\")")
    void placeOrder_WhenValidRequest_ReturnsResponse() {
        // given
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        Order mockOrder = Order.builder().orderId(100).status(com.example.aims.enums.OrderStatus.PENDING_PAYMENT).build();
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);

        DeliveryInfo mockDeliveryInfo = DeliveryInfo.builder()
                .receiverName("Test User")
                .city("Hà Nội")
                .build();
        given(deliveryInfoRepository.save(any(DeliveryInfo.class))).willReturn(mockDeliveryInfo);

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(List.of(testItem))
                .deliveryInfo(testDeliveryInfo)
                .build();
        com.example.aims.service.shipping.ShippingFeeCalculatorStrategy mockStrategy = org.mockito.Mockito.mock(com.example.aims.service.shipping.ShippingFeeCalculatorStrategy.class);
        given(shippingFeeStrategyResolver.resolve(any())).willReturn(mockStrategy);
        given(mockStrategy.calculate(any())).willReturn(ShippingFeeResponse.builder().shippingFee(new BigDecimal("22000")).freeShippingApplied(false).build());

        // when
        PlaceOrderResponse response = placeOrderService.placeOrder(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(100);
        assertThat(response.getCustomerName()).isEqualTo("Test User");
        assertThat(response.getShippingFee()).isEqualTo(new BigDecimal("22000"));
        assertThat(response.getSubTotal()).isEqualTo(new BigDecimal("100000"));

        then(orderRepository).should(times(2)).save(any(Order.class));
    }


    @Test
    @DisplayName("Place order with null delivery info (deliveryInfo=null)")
    void placeOrder_WhenDeliveryInfoIsNull_ThrowsNullPointerException() {
        // given
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        Order mockOrder = Order.builder().orderId(99).status(com.example.aims.enums.OrderStatus.PENDING_PAYMENT).build();
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(List.of(testItem))
                .deliveryInfo(null) // DeliveryInfo bị null
                .build();

        // when & then
        assertThatThrownBy(() -> placeOrderService.placeOrder(request))
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    @DisplayName("Place order with non-existent product in database (productId=1)")
    void placeOrder_WhenProductNotFoundInDB_ThrowsRuntimeException() {
        // given
        given(productRepository.findById(1)).willReturn(Optional.empty()); // simulate product not in DB
        
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(List.of(testItem))
                .deliveryInfo(testDeliveryInfo)
                .build();

        // when & then
        assertThatThrownBy(() -> placeOrderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("One or more products are not available in the requested quantity");
    }

    @Test
    @DisplayName("Place order with database connection failure (orderRepository.save)")
    void placeOrder_WhenDatabaseThrowsException_PropagatesException() {
        // given
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));
        given(orderRepository.save(any(Order.class)))
                .willThrow(new RuntimeException("Database connection error")); // simulate DB lỗi

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(List.of(testItem))
                .deliveryInfo(testDeliveryInfo)
                .build();

        // when & then
        assertThatThrownBy(() -> placeOrderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection error");
    }

    // ==========================================
    // recalculateDraftOrder Tests (Keep 2)
    // ==========================================

    @Test
    @DisplayName("Recalculate draft order - stock insufficient")
    void recalculateDraftOrder_WhenStockInsufficient_ThrowsProductNotAvailableException() {
        // given
        Invoice draftInvoice = Invoice.builder().invoiceId(200).status(InvoiceStatus.DRAFT).build();
        Order order = Order.builder().orderId(100).status(OrderStatus.PENDING_PAYMENT).invoice(draftInvoice).build();
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));
        
        testItem.setQuantity(15);
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(List.of(testItem))
                .deliveryInfo(testDeliveryInfo)
                .build();

        // when & then
        assertThatThrownBy(() -> placeOrderService.recalculateDraftOrder(100, request))
                .isInstanceOf(ProductNotAvailableException.class)
                .hasMessageContaining("One or more products are not available in the requested quantity");
    }

    @Test
    @DisplayName("Recalculate draft order - valid request")
    void recalculateDraftOrder_WhenValidRequest_UpdatesAndReturnsResponse() {
        // given
        Invoice draftInvoice = Invoice.builder().invoiceId(200).status(InvoiceStatus.DRAFT).build();
        Order order = Order.builder()
                .orderId(100)
                .status(OrderStatus.PENDING_PAYMENT)
                .invoice(draftInvoice)
                .orderDetails(new java.util.HashSet<>())
                .build();
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));
        given(productRepository.findById(1)).willReturn(Optional.of(testProduct));

        DeliveryInfo mockDeliveryInfo = DeliveryInfo.builder()
                .receiverName("Test User")
                .city("Hà Nội")
                .build();
        given(deliveryInfoRepository.save(any(DeliveryInfo.class))).willReturn(mockDeliveryInfo);

        // For shipping fee calculation
        com.example.aims.service.shipping.ShippingFeeCalculatorStrategy mockStrategy = org.mockito.Mockito.mock(com.example.aims.service.shipping.ShippingFeeCalculatorStrategy.class);
        given(shippingFeeStrategyResolver.resolve(any())).willReturn(mockStrategy);
        given(mockStrategy.calculate(any())).willReturn(ShippingFeeResponse.builder().shippingFee(new BigDecimal("22000")).freeShippingApplied(false).build());

        // For orderRepository.save
        given(orderRepository.save(any(Order.class))).willReturn(order);

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .items(List.of(testItem))
                .deliveryInfo(testDeliveryInfo)
                .build();

        // when
        PlaceOrderResponse response = placeOrderService.recalculateDraftOrder(100, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(100);
        assertThat(response.getCustomerName()).isEqualTo("Test User");
        assertThat(response.getShippingFee()).isEqualTo(new BigDecimal("22000"));
        assertThat(response.getSubTotal()).isEqualTo(new BigDecimal("100000"));
        assertThat(order.getInvoice().getTotalAmount()).isEqualTo(new BigDecimal("132000.00"));
    }

    // ==========================================
    // expirePendingPaymentOrders Tests (Keep 1)
    // ==========================================

    @Test
    @DisplayName("Expire pending payment orders - with mixed orders (one with transaction, one without)")
    void expirePendingPaymentOrders_WhenExpiredOrdersExist_ExpiresOnlyThoseWithoutTransaction() {
        // given
        PaymentTransaction tx = PaymentTransaction.builder().transactionId(500).build();
        Invoice invoiceWithTx = Invoice.builder().invoiceId(200).paymentTransaction(tx).build();
        Order orderWithTx = Order.builder().orderId(101).status(OrderStatus.PENDING_PAYMENT).invoice(invoiceWithTx).build();

        Invoice invoiceNoTx = Invoice.builder().invoiceId(201).status(InvoiceStatus.DRAFT).build();
        Order orderNoTx = Order.builder().orderId(102).status(OrderStatus.PENDING_PAYMENT).invoice(invoiceNoTx).build();

        given(orderRepository.findByStatusCreatedBeforeWithInvoice(any(), any()))
                .willReturn(List.of(orderWithTx, orderNoTx));

        // when
        placeOrderService.expirePendingPaymentOrders();

        // then
        assertThat(orderWithTx.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(orderNoTx.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        assertThat(invoiceNoTx.getStatus()).isEqualTo(InvoiceStatus.VOID);
        then(invoiceRepository).should(times(1)).save(invoiceNoTx);
        then(orderRepository).should(times(1)).saveAll(any());
    }

    // ==========================================
    // deleteExpiredOrdersPastRetention Tests (Keep 1)
    // ==========================================

    @Test
    @DisplayName("Delete expired orders past retention - mixed orders (one has transaction, one does not)")
    void deleteExpiredOrdersPastRetention_WhenExpiredOrdersExist_DeletesOnlySafeOrders() {
        // given
        PaymentTransaction tx = PaymentTransaction.builder().transactionId(500).build();
        Invoice invoiceWithTx = Invoice.builder().invoiceId(200).paymentTransaction(tx).build();
        Order orderWithTx = Order.builder().orderId(101).status(OrderStatus.EXPIRED).invoice(invoiceWithTx).build();

        Invoice invoiceNoTx = Invoice.builder().invoiceId(201).build();
        Order orderNoTx = Order.builder().orderId(102).status(OrderStatus.EXPIRED).invoice(invoiceNoTx).build();

        given(orderRepository.findDetailedByStatusCreatedBefore(any(), any()))
                .willReturn(List.of(orderWithTx, orderNoTx));

        // when
        placeOrderService.deleteExpiredOrdersPastRetention();

        // then
        then(orderRepository).should(times(1)).deleteAll(List.of(orderNoTx));
    }

    // ==========================================
    // finalizePaidOrder Tests (Keep 1)
    // ==========================================

    @Test
    @DisplayName("Finalize paid order - order status is PAYMENT_SUCCESS")
    void finalizePaidOrder_WhenOrderStatusPaymentSuccess_UpdatesToPendingProcessingAndSendsNotification() {
        // given
        Invoice invoice = Invoice.builder().invoiceId(200).status(InvoiceStatus.DRAFT).totalAmount(new BigDecimal("150000")).build();
        Order order = Order.builder()
                .orderId(100)
                .invoice(invoice)
                .status(OrderStatus.PAYMENT_SUCCESS)
                .deliveryInfo(DeliveryInfo.builder().receiverName("John").email("john@example.com").build())
                .build();
        PaymentTransaction tx = PaymentTransaction.builder().transactionId(300).transactionStatus("PAYMENT_SUCCESS").build();
        given(orderRepository.findDetailedByOrderId(100)).willReturn(Optional.of(order));
        given(paymentTransactionRepository.findByInvoice_InvoiceId(200)).willReturn(Optional.of(tx));

        // when
        var response = placeOrderService.finalizePaidOrder(100);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(100);
        assertThat(response.getOrderStatus()).isEqualTo("PENDING_PROCESSING");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PROCESSING);
        then(invoiceRepository).should(times(1)).save(invoice);
        then(orderRepository).should(times(1)).save(order);
        then(orderConfirmationNotificationService).should(times(1)).sendOrderConfirmation(order, invoice, tx);
    }
}
