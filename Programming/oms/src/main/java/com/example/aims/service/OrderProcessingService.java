package com.example.aims.service;

import com.example.aims.dto.response.ManagerOrderResponse;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.OrderDetail;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.entity.Product;
import com.example.aims.entity.Refund;
import com.example.aims.entity.User;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.PaymentException;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.OrderRepository;
import com.example.aims.repository.ProductRepository;
import com.example.aims.repository.RefundRepository;
import com.example.aims.repository.UserRepository;
import com.example.aims.service.notification.OrderProcessingNotificationService;
import com.example.aims.service.refund.IRefundStrategy;
import com.example.aims.service.refund.RefundContext;
import com.example.aims.service.refund.RefundStrategyResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderProcessingService {

    OrderRepository orderRepository;
    ProductRepository productRepository;
    RefundRepository refundRepository;
    UserRepository userRepository;
    RefundStrategyResolver refundStrategyResolver;
    OrderResponseMapper responseMapper;
    OrderProcessingNotificationService orderProcessingNotificationService;

    @Transactional
    public ManagerOrderResponse approveOrder(Integer orderId, Integer managerUserId) {
        requireManager(managerUserId);
        Order order = loadDetailedOrder(orderId);
        ensurePendingProcessing(order);

        List<Product> productsToSave = new ArrayList<>();
        for (OrderDetail detail : safeDetails(order)) {
            Product product = detail.getProduct();
            int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            int requested = detail.getQuantity() == null ? 0 : detail.getQuantity();
            if (stock < requested) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot approve order because product " + product.getId() + " has insufficient stock"
                );
            }
            product.setStockQuantity(stock - requested);
            productsToSave.add(product);
        }

        productRepository.saveAll(productsToSave);
        order.setStatus(OrderStatus.APPROVED);
        orderRepository.save(order);
        orderProcessingNotificationService.sendOrderApproved(order);
        return responseMapper.toResponse(order, null);
    }

    @Transactional
    public ManagerOrderResponse rejectOrder(Integer orderId, Integer managerUserId, String reason) {
        User manager = requireManager(managerUserId);
        Order order = loadDetailedOrder(orderId);
        ensurePendingProcessing(order);
        Invoice invoice = requireInvoice(order);
        PaymentTransaction transaction = requireTransaction(invoice);

        if (refundRepository.existsByPaymentTransaction_TransactionId(transaction.getTransactionId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already has a refund record");
        }

        IRefundStrategy strategy = refundStrategyResolver.resolve(transaction.getMethod());
        Refund refund = strategy.processRefund(RefundContext.builder()
                .transaction(transaction)
                .invoice(invoice)
                .manager(manager)
                .reason(reason)
                .build());

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
        orderProcessingNotificationService.sendOrderRejected(order, refund);
        return responseMapper.toResponse(order, refund);
    }

    private User requireManager(Integer managerUserId) {
        if (managerUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager user id is required");
        }
        return userRepository.findById(managerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + managerUserId));
    }

    private Order loadDetailedOrder(Integer orderId) {
        return orderRepository.findDetailedByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void ensurePendingProcessing(Order order) {
        if (!OrderStatus.PENDING_PROCESSING.equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only PENDING_PROCESSING orders can be processed"
            );
        }
    }

    private Invoice requireInvoice(Order order) {
        if (order.getInvoice() == null) {
            throw new ResourceNotFoundException("Invoice not found for order: " + order.getOrderId());
        }
        return order.getInvoice();
    }

    private PaymentTransaction requireTransaction(Invoice invoice) {
        if (invoice.getPaymentTransaction() == null) {
            throw new PaymentException("PAYMENT_NOT_FOUND",
                    "Payment transaction not found for invoice: " + invoice.getInvoiceId());
        }
        return invoice.getPaymentTransaction();
    }

    private List<OrderDetail> safeDetails(Order order) {
        if (order.getOrderDetails() == null) {
            return List.of();
        }
        return order.getOrderDetails().stream().toList();
    }
}
