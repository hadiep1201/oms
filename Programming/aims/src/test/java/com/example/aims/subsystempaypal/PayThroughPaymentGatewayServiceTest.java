package com.example.aims.subsystempaypal;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.entity.DeliveryInfo;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.example.aims.repository.InvoiceRepository;
import com.example.aims.repository.OrderRepository;
import com.example.aims.repository.PaymentTransactionRepository;
import com.example.aims.subsystempaypal.dto.PaypalCaptureResponse;
import com.example.aims.subsystempaypal.dto.PaypalOrderResponse;
import com.example.aims.subsystempaypal.dto.PaypalTokenResponse;
import com.example.aims.subsystempaypal.dto.UrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayThroughPaymentGatewayServiceTest {

    private static final Integer ORDER_ID = 1001;
    private static final Integer INVOICE_ID = 2002;
    private static final String ACCESS_TOKEN = "access-token";
    private static final String PAYPAL_ORDER_ID = "paypal-order-123";
    private static final String PAYPAL_CAPTURE_ID = "paypal-capture-456";

    @Mock private OrderRepository orderRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private IPayPalApiGateway apiGateway;

    private PaypalConfig paypalConfig;
    private PayThroughPaymentGatewayService service;

    @BeforeEach
    void setUp() {
        paypalConfig = new PaypalConfig();
        paypalConfig.setClientId("client-id");
        paypalConfig.setSecretKey("secret-key");
        paypalConfig.setBaseUrl("https://api.paypal.test");
        paypalConfig.setReturnUrl("https://aims.test/paypal/return");
        paypalConfig.setCancelUrl("https://aims.test/paypal/cancel");
        paypalConfig.setVndToUsdRate(new BigDecimal("25000"));

        service = buildService(paypalConfig);
    }

    @ParameterizedTest
    @CsvSource({"PENDING", "DECLINED", "FAILED"})
    void capturePayment_rejectedPaypalStatus_keepsOrderUnpaidAndDoesNotPersist(String captureStatus) {
        Order order = payableOrderWithInvoice(new BigDecimal("250000"));
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.findByInvoice_InvoiceId(INVOICE_ID)).thenReturn(Optional.empty());
        when(apiGateway.getAccessToken()).thenReturn(tokenResponse());
        when(apiGateway.capturePayment(PAYPAL_ORDER_ID, ACCESS_TOKEN))
                .thenReturn(PaypalCaptureResponse.builder()
                        .captureId(PAYPAL_CAPTURE_ID)
                        .status(captureStatus)
                        .build());

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.capturePayment(ORDER_ID, PAYPAL_ORDER_ID)
        );

        assertEquals(PaymentErrorCode.CAPTURE_FAILED, exception.getTypedErrorCode());
        assertEquals(com.example.aims.enums.OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertNull(order.getInvoice().getPaymentTransaction());
        verify(invoiceRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void capturePayment_completedPaypalCapture_savesTransactionUpdatesOrderAndReturnsResponse() {
        Order order = payableOrderWithInvoice(new BigDecimal("250000"));
        Invoice invoice = order.getInvoice();
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.findByInvoice_InvoiceId(INVOICE_ID)).thenReturn(Optional.empty());
        when(apiGateway.getAccessToken()).thenReturn(tokenResponse());
        when(apiGateway.capturePayment(PAYPAL_ORDER_ID, ACCESS_TOKEN))
                .thenReturn(PaypalCaptureResponse.builder()
                        .captureId(PAYPAL_CAPTURE_ID)
                        .status("COMPLETED")
                        .build());

        PayOrderResponse result = service.capturePayment(ORDER_ID, PAYPAL_ORDER_ID);

        assertEquals(ORDER_ID, result.getOrderId());
        assertEquals("Nguyen Van A", result.getCustomerName());
        assertEquals("0901234567", result.getPhoneNumber());
        assertEquals("1 Dai Co Viet", result.getShippingAddress());
        assertEquals("Ha Noi", result.getCity());
        assertEquals(new BigDecimal("250000"), result.getTotalAmount());
        assertEquals("PAYMENT_SUCCESS", result.getOrderStatus());
        assertEquals("PAYPAL", result.getPaymentMethod());
        assertEquals("Payment for Order #" + ORDER_ID, result.getTransactionContent());
        assertNotNull(result.getTransactionDatetime());

        PaymentTransaction transaction = invoice.getPaymentTransaction();
        assertNotNull(transaction);
        assertEquals(invoice, transaction.getInvoice());
        assertEquals("PAYPAL", transaction.getMethod());
        assertEquals("PAID", transaction.getTransactionStatus());
        assertEquals(PAYPAL_ORDER_ID, transaction.getExternalOrderId());
        assertEquals(PAYPAL_CAPTURE_ID, transaction.getExternalCaptureId());
        assertEquals(result.getTransactionDatetime(), transaction.getTransactionDatetime());
        assertEquals(com.example.aims.enums.OrderStatus.PAYMENT_SUCCESS, order.getStatus());

        verify(invoiceRepository).save(invoice);
        verify(orderRepository).save(order);
        verify(apiGateway).capturePayment(PAYPAL_ORDER_ID, ACCESS_TOKEN);
    }

    @Test
    void capturePayment_paypalGatewayThrows_keepsOrderUnpaidAndDoesNotPersistTransaction() {
        Order order = payableOrderWithInvoice(new BigDecimal("250000"));
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.findByInvoice_InvoiceId(INVOICE_ID)).thenReturn(Optional.empty());
        when(apiGateway.getAccessToken()).thenReturn(tokenResponse());
        when(apiGateway.capturePayment(PAYPAL_ORDER_ID, ACCESS_TOKEN))
                .thenThrow(new RuntimeException("PayPal service unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.capturePayment(ORDER_ID, PAYPAL_ORDER_ID)
        );

        assertEquals("PayPal service unavailable", exception.getMessage());
        assertEquals(com.example.aims.enums.OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertNull(order.getInvoice().getPaymentTransaction());
        verify(invoiceRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void payOrder_validOrderAndGatewayResponds_returnsApproveUrl() {
        String approveUrl = "https://www.paypal.test/checkoutnow?token=" + PAYPAL_ORDER_ID;
        Order order = payableOrderWithInvoice(new BigDecimal("250000"));
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.findByInvoice_InvoiceId(INVOICE_ID)).thenReturn(Optional.empty());
        when(apiGateway.getAccessToken()).thenReturn(tokenResponse());
        when(apiGateway.createOrder(any(), eq(ACCESS_TOKEN))).thenReturn(
                PaypalOrderResponse.builder()
                        .id(PAYPAL_ORDER_ID)
                        .status("CREATED")
                        .approveLink(approveUrl)
                        .build()
        );

        UrlResponse result = service.payOrder(ORDER_ID);

        assertEquals(approveUrl, result.getUrl());
        verify(apiGateway).getAccessToken();
        verify(apiGateway).createOrder(any(), eq(ACCESS_TOKEN));
    }

    @Test
    void capturePayment_missingPaypalConfig_throwsPaymentException() {
        PaypalConfig incompleteConfig = new PaypalConfig();
        incompleteConfig.setClientId("");
        incompleteConfig.setSecretKey("secret-key");
        incompleteConfig.setBaseUrl("https://api.paypal.test");
        incompleteConfig.setReturnUrl("https://aims.test/paypal/return");
        incompleteConfig.setCancelUrl("https://aims.test/paypal/cancel");
        PayThroughPaymentGatewayService serviceWithBadConfig = buildService(incompleteConfig);

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> serviceWithBadConfig.capturePayment(ORDER_ID, PAYPAL_ORDER_ID)
        );

        assertEquals(PaymentErrorCode.CONFIG_INCOMPLETE, exception.getTypedErrorCode());
    }

    @Test
    void capturePayment_orderNotFound_throwsPaymentException() {
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.capturePayment(ORDER_ID, PAYPAL_ORDER_ID)
        );

        assertEquals(PaymentErrorCode.ORDER_NOT_FOUND, exception.getTypedErrorCode());
    }

    @Test
    void capturePayment_orderNotInPendingPaymentStatus_throwsPaymentException() {
        Order order = Order.builder()
                .orderId(ORDER_ID)
                .status(com.example.aims.enums.OrderStatus.PENDING_PROCESSING)
                .build();
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.capturePayment(ORDER_ID, PAYPAL_ORDER_ID)
        );

        // Order has no invoice, so INVOICE_MISSING is thrown before ORDER_NOT_PENDING_PAYMENT
        assertEquals(PaymentErrorCode.INVOICE_MISSING, exception.getTypedErrorCode());
    }

    @Test
    void capturePayment_invoiceMissing_throwsPaymentException() {
        Order order = Order.builder()
                .orderId(ORDER_ID)
                .status(com.example.aims.enums.OrderStatus.PENDING_PAYMENT)
                .build();
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.capturePayment(ORDER_ID, PAYPAL_ORDER_ID)
        );

        assertEquals(PaymentErrorCode.INVOICE_MISSING, exception.getTypedErrorCode());
    }

    @Test
    void capturePayment_paymentTransactionAlreadyExists_returnsExistingTransaction() {
        Order order = payableOrderWithInvoice(new BigDecimal("250000"));
        order.setStatus(com.example.aims.enums.OrderStatus.PAYMENT_SUCCESS);
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionId(3003)
                .invoice(order.getInvoice())
                .method("PAYPAL")
                .transactionStatus("PAID")
                .transactionContent("Payment for Order #" + ORDER_ID)
                .externalOrderId(PAYPAL_ORDER_ID)
                .externalCaptureId(PAYPAL_CAPTURE_ID)
                .build();
        when(orderRepository.findDetailedByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.findByInvoice_InvoiceId(INVOICE_ID))
                .thenReturn(Optional.of(transaction));

        PayOrderResponse result = service.capturePayment(ORDER_ID, PAYPAL_ORDER_ID);

        assertEquals(ORDER_ID, result.getOrderId());
        assertEquals("PAYMENT_SUCCESS", result.getOrderStatus());
        assertEquals(3003, result.getTransactionId());
        assertEquals("PAYPAL", result.getPaymentMethod());
        verify(apiGateway, never()).getAccessToken();
        verify(apiGateway, never()).capturePayment(any(), any());
    }

    private PayThroughPaymentGatewayService buildService(PaypalConfig config) {
        CurrencyConverter currencyConverter = new CurrencyConverter(config);
        PayPalPaymentValidator validator = new PayPalPaymentValidator(config, paymentTransactionRepository);
        PayPalOrderRequestMapper requestMapper = new PayPalOrderRequestMapper(config, currencyConverter);
        PayPalTransactionFactory txFactory = new PayPalTransactionFactory();
        PayOrderResponseAssembler assembler = new PayOrderResponseAssembler();
        return new PayThroughPaymentGatewayService(
                orderRepository, invoiceRepository, paymentTransactionRepository,
                apiGateway, validator, requestMapper, txFactory, assembler, currencyConverter
        );
    }

    private Order payableOrderWithInvoice(BigDecimal totalAmount) {
        Invoice invoice = Invoice.builder()
                .invoiceId(INVOICE_ID)
                .totalAmount(totalAmount)
                .build();
        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
                .receiverName("Nguyen Van A")
                .phoneNumber("0901234567")
                .address("1 Dai Co Viet")
                .city("Ha Noi")
                .build();
        Order order = Order.builder()
                .orderId(ORDER_ID)
                .status(com.example.aims.enums.OrderStatus.PENDING_PAYMENT)
                .invoice(invoice)
                .deliveryInfo(deliveryInfo)
                .build();
        invoice.setOrder(order);
        deliveryInfo.setOrder(order);
        return order;
    }

    private PaypalTokenResponse tokenResponse() {
        return PaypalTokenResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();
    }
}
