package com.example.aims.service;

import com.example.aims.dto.request.DeliveryInfoRequest;
import com.example.aims.dto.request.OrderItemRequest;
import com.example.aims.dto.request.PlaceOrderRequest;
import com.example.aims.dto.request.ShippingFeeRequest;
import com.example.aims.dto.response.*;
import com.example.aims.entity.*;
import com.example.aims.enums.InvoiceStatus;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.PaymentException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.exception.PlaceOrderException;
import com.example.aims.exception.ProductNotAvailableException;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.DeliveryInfoRepository;
import com.example.aims.repository.InvoiceRepository;
import com.example.aims.repository.OrderDetailRepository;
import com.example.aims.repository.OrderRepository;
import com.example.aims.repository.PaymentTransactionRepository;
import com.example.aims.repository.ProductRepository;
import com.example.aims.service.notification.OrderConfirmationNotificationService;
import com.example.aims.service.shipping.ShippingFeeCalculationContext;
import com.example.aims.service.shipping.ShippingFeeStrategyResolver;
import com.example.aims.service.shipping.ShippingFeeStrategyType;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
/*
 * Coupling/Cohesion:
 * - Coupling: Stamp coupling.
 * - Cohesion: Procedural cohesion with Sequential cohesion.
 * Reason why:
 * - It receives request objects but each step only uses part of their data.
 * - The PlaceOrder logic is implemented as a sequence of steps where each step feeds the next.
 * Improvement:
 * - Pass only required data to internal methods and split major steps into smaller services.
 */
/*
 * SOLID Review:
 * - SRP violation: this service handles stock validation, order creation, shipping calculation,
 *   invoice creation, payment finalization, expired-order cleanup, and customer notification.
 *   Impact: future changes in shipping, payment, or notification can force edits in this class.
 * - OCP violation: shipping policy, payment status checks, and notification behavior are hard-coded.
 *   Impact: adding volumetric-weight shipping, new payment states, SMS, Zalo, or push notification
 *   requires modifying existing code instead of adding new implementations.
 * - DIP violation: the use-case service depends directly on concrete repositories and inline
 *   notification logic instead of abstractions such as ShippingFeeCalculator or NotificationSender.
 *   Impact: tests and future extensions need more setup and are tightly coupled to infrastructure.
 * - LSP/ISP: no clear violation is identified here because this class does not define an
 *   inheritance hierarchy or depend on a large interface with unused methods.
 * Improvement direction by principle:
 * - SRP: split stock validation, shipping calculation, payment finalization, notification,
 *   and expired-order cleanup into dedicated services.
 * - OCP: introduce ShippingFeeCalculator, PaymentStatusPolicy, and NotificationSender
 *   abstractions so new rules can be added without modifying this class.
 * - DIP: depend on abstractions such as ShippingFeeCalculator, OrderEventPublisher, and
 *   NotificationSender rather than concrete policy or notification logic.
 */
public class PlaceOrderService {

    // Constants
    static BigDecimal VAT_RATE = new BigDecimal("0.10"); // 10% VAT
    static long PENDING_PAYMENT_EXPIRATION_MINUTES = 30;
    static long EXPIRED_ORDER_RETENTION_DAYS = 30;
    
    @NonFinal
    @Value("${shipping.fee.strategy-type:ACTUAL_WEIGHT}")
    private ShippingFeeStrategyType shippingType;

    // Repositories
    OrderRepository orderRepository;
    OrderDetailRepository orderDetailRepository;
    InvoiceRepository invoiceRepository;
    ProductRepository productRepository;
    DeliveryInfoRepository deliveryInfoRepository;
    PaymentTransactionRepository paymentTransactionRepository;
    ShippingFeeStrategyResolver shippingFeeStrategyResolver;
    OrderConfirmationNotificationService orderConfirmationNotificationService;

    // ============================
    // 1. VALIDATE STOCK
    // ============================
    public StockValidationResponse validateOrderItems(List<OrderItemRequest> items) {
        List<UnavailableItemDetail> unavailableItems = new ArrayList<>();

        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElse(null);

            if (product == null) {
                unavailableItems.add(UnavailableItemDetail.builder()
                        .productId(item.getProductId())
                        .productTitle("Product not found")
                        .requestedQuantity(item.getQuantity())
                        .availableQuantity(0)
                        .build());
            } else if (product.getStockQuantity() < item.getQuantity()) {
                unavailableItems.add(UnavailableItemDetail.builder()
                        .productId(product.getId())
                        .productTitle(product.getTitle())
                        .requestedQuantity(item.getQuantity())
                        .availableQuantity(product.getStockQuantity())
                        .build());
            }
        }

        return StockValidationResponse.builder()
                .valid(unavailableItems.isEmpty())
                .unavailableItems(unavailableItems)
                .build();
    }

    // ============================
    // 2. CALCULATE SHIPPING FEE
    // ============================
    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request) {
        List<OrderItemRequest> items = request.getItems();
        String city = request.getCity();

        BigDecimal totalActualWeight = BigDecimal.ZERO;
        BigDecimal totalVolumetricWeight = BigDecimal.ZERO;
        BigDecimal subTotal = BigDecimal.ZERO;

        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotAvailableException(
                            "Product not found: " + item.getProductId()
                    ));

            BigDecimal qty = new BigDecimal(item.getQuantity());
            if (product instanceof PhysicalProduct pp) {
                totalActualWeight = totalActualWeight.add(safeMultiply(pp.getWeight(), qty));
                totalVolumetricWeight = totalVolumetricWeight.add(calculateVolumetricWeight(pp, qty));
            }
            subTotal = subTotal.add(product.getCurrentPrice().multiply(qty));
        }

        ShippingFeeCalculationContext context = ShippingFeeCalculationContext.builder()
                .city(city)
                .subTotal(subTotal)
                .actualWeight(totalActualWeight)
                .volumetricWeight(totalVolumetricWeight)
                .build();

        return shippingFeeStrategyResolver
                .resolve(shippingType)
                .calculate(context);
    }


    // ============================
    // 3. PLACE ORDER — Tạo Order + Invoice, trả về cho FE hiển thị
    //    (Chưa thanh toán, chưa trừ stock)
    // ============================
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        List<OrderItemRequest> items = request.getItems();
        DeliveryInfoRequest deliveryInfoReq = request.getDeliveryInfo();

        // --- Bước 1: Validate stock lần nữa ---
        StockValidationResponse validation = validateOrderItems(items);
        if (!validation.isValid()) {
            throw new ProductNotAvailableException("One or more products are not available in the requested quantity");
        }

        // --- Bước 2: Tạo Order ---
        Order order = Order.builder()
                .status(OrderStatus.PENDING_PAYMENT) // Chờ thanh toán, chưa phải PENDING_PROCESSING
                .createdDate(Timestamp.from(Instant.now()))
                .build();
        order = orderRepository.save(order); // save để có orderId

        // --- Bước 3: Tạo OrderDetails ---
        Set<OrderDetail> orderDetails = new HashSet<>();
        List<OrderItemDetail> orderItemDetails = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotAvailableException(
                            "Product not found: " + item.getProductId()
                    ));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getCurrentPrice())
                    .build();
            orderDetails.add(detail);

            BigDecimal itemTotal = product.getCurrentPrice()
                    .multiply(new BigDecimal(item.getQuantity()));
            subTotal = subTotal.add(itemTotal);

            // Build item detail cho response
            orderItemDetails.add(OrderItemDetail.builder()
                    .productId(product.getId())
                    .productTitle(product.getTitle())
                    .imageUrl(product.getImageUrl())
                    .quantity(item.getQuantity())
                    .price(product.getCurrentPrice())
                    .itemTotal(itemTotal)
                    .build());
        }
        order.setOrderDetails(orderDetails);

        // --- Bước 4: Tạo DeliveryInfo ---
        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
                .order(order)
                .receiverName(deliveryInfoReq.getReceiverName())
                .email(deliveryInfoReq.getEmail())
                .phoneNumber(deliveryInfoReq.getPhoneNumber())
                .address(deliveryInfoReq.getAddress())
                .city(deliveryInfoReq.getCity())
                .build();
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        order.setDeliveryInfo(deliveryInfo);

        // --- Bước 5: Tính phí ship + Tạo Invoice ---
        ShippingFeeRequest shippingReq = ShippingFeeRequest.builder()
                .items(items)
                .city(deliveryInfoReq.getCity())
                .build();
        ShippingFeeResponse shippingResult = calculateShippingFee(shippingReq);
        BigDecimal shippingFee = shippingResult.getShippingFee();
        boolean freeShippingApplied = shippingResult.isFreeShippingApplied();

        if (shippingFee == null) {
            throw new PlaceOrderException(400, HttpStatus.BAD_REQUEST, "Shipping fee could not be calculated");
        }
        order.setShippingFee(shippingFee);

        // Tạo Invoice: subTotal (chưa VAT), vatAmount (10%), shippingFee, totalAmount
        BigDecimal vatAmount = subTotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subTotal.add(vatAmount).add(shippingFee);

        Invoice invoice = Invoice.builder()
                .order(order)
                .subTotal(subTotal)
                .shippingFee(shippingFee)
                .vatAmount(vatAmount)
                .totalAmount(totalAmount)
                .status(InvoiceStatus.DRAFT)
                .build();
        order.setInvoice(invoice);

        // Save order (cascade sẽ save cả OrderDetails, DeliveryInfo, Invoice)
        order = orderRepository.save(order);

        // --- Trả Invoice về FE để hiển thị, CHƯA thanh toán ---
        return PlaceOrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(deliveryInfo.getReceiverName())
                .phoneNumber(deliveryInfo.getPhoneNumber())
                .shippingAddress(deliveryInfo.getAddress())
                .city(deliveryInfo.getCity())
                .orderStatus(order.getStatus().name())
                .subTotal(subTotal)
                .vatAmount(vatAmount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .freeShippingApplied(freeShippingApplied)
                .orderItems(orderItemDetails)
                .build();
    }

    @Transactional
    public PlaceOrderResponse recalculateDraftOrder(Integer orderId, PlaceOrderRequest request) {
        Order order = orderRepository.findDetailedByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ORDER_NOT_FOUND.getCode(),
                        "Order not found: " + orderId
                ));

        if (OrderStatus.EXPIRED.equals(order.getStatus())) {
            throw new PlaceOrderException(
                    ErrorCode.ORDER_EXPIRED.getCode(),
                    HttpStatus.CONFLICT,
                    "Order has expired and can no longer be recalculated"
            );
        }

        if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new PlaceOrderException(
                    ErrorCode.ORDER_ALREADY_PAID.getCode(),
                    HttpStatus.CONFLICT,
                    "Only PENDING_PAYMENT orders can be recalculated"
            );
        }

        Invoice invoice = order.getInvoice();
        if (invoice == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.INVOICE_NOT_FOUND.getCode(),
                    "Invoice not found for order: " + orderId
            );
        }
        if (!InvoiceStatus.DRAFT.equals(invoice.getStatus())) {
            int errorCode = InvoiceStatus.VOID.equals(invoice.getStatus())
                    ? ErrorCode.INVOICE_VOID.getCode()
                    : ErrorCode.INVOICE_FINALIZED.getCode();
            throw new PlaceOrderException(
                    errorCode,
                    HttpStatus.CONFLICT,
                    "Only DRAFT invoice can be recalculated"
            );
        }
        if (invoice.getPaymentTransaction() != null) {
            throw new PlaceOrderException(
                    ErrorCode.ORDER_ALREADY_PAID.getCode(),
                    HttpStatus.CONFLICT,
                    "Cannot recalculate invoice that already has payment transaction"
            );
        }

        List<OrderItemRequest> items = request.getItems();
        DeliveryInfoRequest deliveryInfoReq = request.getDeliveryInfo();

        StockValidationResponse validation = validateOrderItems(items);
        if (!validation.isValid()) {
            throw new ProductNotAvailableException("One or more products are not available in the requested quantity");
        }

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            List<OrderDetail> existingDetails = new ArrayList<>(order.getOrderDetails());
            order.getOrderDetails().clear();
            orderDetailRepository.deleteAll(existingDetails);
            orderDetailRepository.flush();
        }
        Set<OrderDetail> orderDetails = new HashSet<>();
        List<OrderItemDetail> orderItemDetails = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotAvailableException(
                            "Product not found: " + item.getProductId()
                    ));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getCurrentPrice())
                    .build();
            orderDetails.add(detail);

            BigDecimal itemTotal = product.getCurrentPrice()
                    .multiply(new BigDecimal(item.getQuantity()));
            subTotal = subTotal.add(itemTotal);

            orderItemDetails.add(OrderItemDetail.builder()
                    .productId(product.getId())
                    .productTitle(product.getTitle())
                    .imageUrl(product.getImageUrl())
                    .quantity(item.getQuantity())
                    .price(product.getCurrentPrice())
                    .itemTotal(itemTotal)
                    .build());
        }
        order.setOrderDetails(orderDetails);

        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        if (deliveryInfo == null) {
            deliveryInfo = DeliveryInfo.builder().order(order).build();
        }
        deliveryInfo.setReceiverName(deliveryInfoReq.getReceiverName());
        deliveryInfo.setEmail(deliveryInfoReq.getEmail());
        deliveryInfo.setPhoneNumber(deliveryInfoReq.getPhoneNumber());
        deliveryInfo.setAddress(deliveryInfoReq.getAddress());
        deliveryInfo.setCity(deliveryInfoReq.getCity());
        deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        order.setDeliveryInfo(deliveryInfo);

        ShippingFeeRequest shippingReq = ShippingFeeRequest.builder()
                .items(items)
                .city(deliveryInfoReq.getCity())
                .build();
        ShippingFeeResponse shippingResult = calculateShippingFee(shippingReq);
        BigDecimal shippingFee = shippingResult.getShippingFee();
        boolean freeShippingApplied = shippingResult.isFreeShippingApplied();

        if (shippingFee == null) {
            throw new PlaceOrderException(400, HttpStatus.BAD_REQUEST, "Shipping fee could not be calculated");
        }

        order.setShippingFee(shippingFee);
        BigDecimal vatAmount = subTotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subTotal.add(vatAmount).add(shippingFee);

        invoice.setSubTotal(subTotal);
        invoice.setShippingFee(shippingFee);
        invoice.setVatAmount(vatAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(InvoiceStatus.DRAFT);
        order.setInvoice(invoice);

        order = orderRepository.save(order);

        return PlaceOrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(deliveryInfo.getReceiverName())
                .phoneNumber(deliveryInfo.getPhoneNumber())
                .shippingAddress(deliveryInfo.getAddress())
                .city(deliveryInfo.getCity())
                .orderStatus(order.getStatus().name())
                .subTotal(subTotal)
                .vatAmount(vatAmount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .freeShippingApplied(freeShippingApplied)
                .orderItems(orderItemDetails)
                .build();
    }

    @Scheduled(fixedDelayString = "60000")
    @Transactional
    public void expirePendingPaymentOrders() {
        Timestamp cutoff = Timestamp.from(
                Instant.now().minusSeconds(PENDING_PAYMENT_EXPIRATION_MINUTES * 60)
        );

        List<Order> expiredOrders = orderRepository.findByStatusCreatedBeforeWithInvoice(
                OrderStatus.PENDING_PAYMENT,
                cutoff
        );

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {
            Invoice invoice = order.getInvoice();
            if (invoice != null && invoice.getPaymentTransaction() != null) {
                log.warn(
                        "Skip expiring order #{} because invoice #{} already has payment transaction #{}",
                        order.getOrderId(),
                        invoice.getInvoiceId(),
                        invoice.getPaymentTransaction().getTransactionId()
                );
                continue;
            }

            if (invoice != null) {
                invoice.setStatus(InvoiceStatus.VOID);
                invoiceRepository.save(invoice);
            }

            order.setStatus(OrderStatus.EXPIRED);
        }

        orderRepository.saveAll(expiredOrders);
        log.info("Expired {} pending payment order(s)", expiredOrders.size());
    }

    @Scheduled(fixedDelayString = "86400000")
    @Transactional
    public void deleteExpiredOrdersPastRetention() {
        Timestamp cutoff = Timestamp.from(
                Instant.now().minusSeconds(EXPIRED_ORDER_RETENTION_DAYS * 24 * 60 * 60)
        );

        List<Order> expiredOrders = orderRepository.findDetailedByStatusCreatedBefore(
                OrderStatus.EXPIRED,
                cutoff
        );

        if (expiredOrders.isEmpty()) {
            return;
        }

        List<Order> safeToDelete = expiredOrders.stream()
                .filter(order -> {
                    Invoice invoice = order.getInvoice();
                    boolean hasPaymentTransaction = invoice != null && invoice.getPaymentTransaction() != null;
                    if (hasPaymentTransaction) {
                        log.warn(
                                "Skip deleting expired order #{} because invoice #{} still has payment transaction #{}",
                                order.getOrderId(),
                                invoice.getInvoiceId(),
                                invoice.getPaymentTransaction().getTransactionId()
                        );
                    }
                    return !hasPaymentTransaction;
                })
                .toList();

        if (safeToDelete.isEmpty()) {
            return;
        }

        orderRepository.deleteAll(safeToDelete);
        log.info("Deleted {} expired order(s) past {} day retention", safeToDelete.size(), EXPIRED_ORDER_RETENTION_DAYS);
    }

    @Transactional
    public PayOrderResponse finalizePaidOrder(Integer orderId) {
        Order order = orderRepository.findDetailedByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Invoice invoice = order.getInvoice();
        if (invoice == null) {
            throw new ResourceNotFoundException("Invoice not found for order: " + orderId);
        }
        if (InvoiceStatus.VOID.equals(invoice.getStatus())) {
            throw new PaymentException(
                    "INVOICE_NOT_PAYABLE",
                    "Invoice is no longer payable for order: " + orderId
            );
        }
        

        PaymentTransaction transaction = paymentTransactionRepository.findByInvoice_InvoiceId(invoice.getInvoiceId())
                .orElseThrow(() -> new PaymentException(
                        "PAYMENT_NOT_FOUND",
                        "Payment transaction not found for order: " + orderId
                ));
        // invoice.setPaymentTransaction(transaction);
        invoice.setStatus(InvoiceStatus.FINALIZED);
        invoiceRepository.save(invoice);

        // Hỗ trợ cả status "PAID" và "PAYMENT_SUCCESS" của transaction
        String transactionStatus = transaction.getTransactionStatus();
        if (!"PAID".equalsIgnoreCase(transactionStatus) && !"PAYMENT_SUCCESS".equalsIgnoreCase(transactionStatus)) {
            throw new PaymentException(
                    "PAYMENT_NOT_COMPLETED",
                    "Payment is not completed for order: " + orderId + " (Status: " + transactionStatus + ")"
            );
        }

        // Chỉ xử lý khi order đã xác nhận thanh toán thành công và chưa được đưa sang quy trình xử lý
        if (OrderStatus.PENDING_PROCESSING.equals(order.getStatus())) {
            log.info("[FINALIZE PAID ORDER] Order #{} - Already processed (Status: PENDING_PROCESSING)", orderId);
        } else if (OrderStatus.PAYMENT_SUCCESS.equals(order.getStatus())) {
            order.setStatus(OrderStatus.PENDING_PROCESSING);
            orderRepository.save(order);
            orderConfirmationNotificationService.sendOrderConfirmation(order, invoice, transaction);
            log.info("[FINALIZE PAID ORDER] Order #{} - Status changed to PENDING_PROCESSING", orderId);
        } else {
            throw new PaymentException(
                    "ORDER_NOT_READY_FOR_FINALIZATION",
                    "Order is not in a valid state for finalize payment: " + order.getStatus()
            );
        }

        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        return PayOrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(deliveryInfo != null ? deliveryInfo.getReceiverName() : null)
                .email(deliveryInfo != null ? deliveryInfo.getEmail() : null)
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

    private BigDecimal calculateVolumetricWeight(PhysicalProduct product, BigDecimal quantity) {
        if (product.getLength() == null || product.getWidth() == null || product.getHeight() == null) {
            return BigDecimal.ZERO;
        }

        return product.getLength()
                .multiply(product.getWidth())
                .multiply(product.getHeight())
                .divide(new BigDecimal("6000"), 4, RoundingMode.HALF_UP)
                .multiply(quantity);
    }

    private BigDecimal safeMultiply(BigDecimal value, BigDecimal multiplier) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.multiply(multiplier);
    }

}
