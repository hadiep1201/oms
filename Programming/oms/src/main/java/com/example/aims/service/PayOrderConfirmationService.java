package com.example.aims.service;

import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.enums.OrderStatus;
import com.example.aims.payment.IPaymentConfirmation;
import com.example.aims.payment.PaymentConfirmationCommand;
import com.example.aims.payment.PaymentConfirmationResult;
import com.example.aims.repository.InvoiceRepository;
import com.example.aims.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Gateway-neutral application service that confirms an order's payment (UC007 and any
 * future gateway).
 *
 * It contains only business rules - resolve the order, verify the paid amount against
 * the invoice total, mark the order paid (idempotent) and record the PaymentTransaction.
 * It has NO dependency on any payment gateway's protocol (no JSON, no VietQR DTOs):
 * the gateway-specific parsing and acknowledgement live in the gateway's adapter, which
 * calls this service through the IPaymentConfirmation port. A new gateway reuses this
 * logic unchanged.
 *
 * Cohesion: Functional - one responsibility: apply a confirmed payment to an order.
 *
 * Coupling:
 * - Data coupling with OrderRepository / InvoiceRepository.
 * - Data coupling with the IPaymentConfirmation command/result (no gateway types).
 *
 * SOLID:
 * - SRP: payment-confirmation business rules only.
 * - DIP: implements the neutral IPaymentConfirmation abstraction; adapters depend on it.
 * - OCP: adding a gateway adds an adapter, not a change here.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOrderConfirmationService implements IPaymentConfirmation {

    OrderRepository orderRepository;
    InvoiceRepository invoiceRepository;

    /** Status tag stored on the PaymentTransaction once a payment is confirmed. */
    static final String TRANSACTION_STATUS_SUCCESS = "PAYMENT_SUCCESS";

    @Transactional
    @Override
    public PaymentConfirmationResult confirm(PaymentConfirmationCommand command) {
        Integer orderId = command.getOrderId();

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return PaymentConfirmationResult.orderNotFound("Order not found: " + orderId);
        }

        Invoice invoice = order.getInvoice();
        if (invoice == null || invoice.getTotalAmount() == null) {
            return PaymentConfirmationResult.invalidOrderState(
                    "Invoice or total amount not available for order " + orderId);
        }

        Long paidAmount = command.getPaidAmount();
        if (paidAmount == null
                || invoice.getTotalAmount().compareTo(BigDecimal.valueOf(paidAmount)) != 0) {
            return PaymentConfirmationResult.amountMismatch(
                    "Paid amount does not match order total for order " + orderId);
        }

        String refTransactionId = applyPaymentAndGetRef(order, invoice, command);
        return PaymentConfirmationResult.success(refTransactionId);
    }

    /**
     * Marks the order as paid if it is not already, creating the PaymentTransaction
     * when missing. Idempotent: a retried confirmation for an already-paid order reuses
     * the existing transaction. Returns the partner reference id for the response.
     */
    private String applyPaymentAndGetRef(Order order, Invoice invoice, PaymentConfirmationCommand command) {
        if (!OrderStatus.PAYMENT_SUCCESS.equals(order.getStatus())) {
            order.setStatus(OrderStatus.PAYMENT_SUCCESS);
            orderRepository.save(order);
        }

        PaymentTransaction transaction = invoice.getPaymentTransaction();
        if (transaction == null) {
            transaction = PaymentTransaction.builder()
                    .invoice(invoice)
                    .method(command.getMethod())
                    .transactionDatetime(new Timestamp(System.currentTimeMillis()))
                    .transactionStatus(TRANSACTION_STATUS_SUCCESS)
                    .transactionContent(command.getReference())
                    .build();
            invoice.setPaymentTransaction(transaction);
            invoiceRepository.save(invoice);
        }
        return refTransactionIdOf(transaction);
    }

    private String refTransactionIdOf(PaymentTransaction transaction) {
        if (transaction.getTransactionId() != null) {
            return "AIMS-TX-" + transaction.getTransactionId();
        }
        return "AIMS-TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}