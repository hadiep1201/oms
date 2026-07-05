package com.example.aims.service;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.entity.Order;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.payment.IPayOrderResult;
import com.example.aims.payment.IPayableAmountSource;
import com.example.aims.repository.InvoiceRepository;
import com.example.aims.repository.OrderRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Gateway-neutral pay-order query service: the read operations shared by every payment
 * method - read the pay-order result and read the payable amount.
 *
 * These operations carry no gateway-specific logic, so they live here (neutral) rather
 * than inside a VietQR-specific class. A new QR/redirect gateway reuses this service
 * directly instead of duplicating the reads.
 *
 * Implements two segregated neutral ports (ISP): IPayOrderResult and IPayableAmountSource.
 *
 * Cohesion: Functional - read/assemble an order's payment-facing views.
 *
 * Coupling:
 * - Data coupling with OrderRepository / InvoiceRepository.
 * - Data coupling with PayOrderResultAssembler (passes Order, receives the DTO).
 *
 * SOLID:
 * - SRP: only the neutral pay-order reads; no gateway protocol, no QR generation.
 * - DIP: depends on Spring Data interfaces; exposed via neutral ports.
 * - ISP: implements two focused interfaces rather than one wide one.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOrderQueryService implements IPayOrderResult, IPayableAmountSource {

    OrderRepository orderRepository;
    InvoiceRepository invoiceRepository;
    PayOrderResultAssembler payOrderResultAssembler;

    @Override
    @Transactional(readOnly = true)
    public PayOrderResponse getPayOrderResult(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return payOrderResultAssembler.toPayOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPayableAmount(Integer orderId) {
        BigDecimal total = invoiceRepository.findTotalAmountByOrderId(orderId);
        if (total == null) {
            throw new ResourceNotFoundException("No invoice total for order: " + orderId);
        }
        return total.longValue();
    }
}