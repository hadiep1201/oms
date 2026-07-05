package com.example.aims.service;

import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.enums.OrderStatus;
import com.example.aims.payment.PaymentConfirmationCommand;
import com.example.aims.payment.PaymentConfirmationResult;
import com.example.aims.repository.InvoiceRepository;
import com.example.aims.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Testing strategy for PayOrderConfirmationService (gateway-neutral payment confirmation).
 *
 * Unit under test:
 *   - confirm(PaymentConfirmationCommand) : verify amount, mark order paid, record the
 *                                           PaymentTransaction; returns a neutral outcome
 *
 * Strategy: black-box with equivalence partitioning. OrderRepository and InvoiceRepository
 * are mocked to isolate the service. No gateway/protocol types are involved here.
 *
 * Partitions:
 *   [match + correct amount] -> SUCCESS, order paid and saved, refTransactionId present
 *   [amount mismatch]        -> AMOUNT_MISMATCH, order untouched
 *   [order not found]        -> ORDER_NOT_FOUND
 *   [no invoice / total]     -> INVALID_ORDER_STATE
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayOrderConfirmationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PayOrderConfirmationService confirmationService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .orderId(1)
                .status(OrderStatus.PENDING_PAYMENT)
                .shippingFee(BigDecimal.valueOf(22000))
                .orderDetails(new HashSet<>())
                .build();

        Invoice invoice = Invoice.builder()
                .invoiceId(1)
                .totalAmount(BigDecimal.valueOf(122000))
                .build();
        testOrder.setInvoice(invoice);
    }

    private PaymentConfirmationCommand command(Integer orderId, Long paidAmount) {
        return PaymentConfirmationCommand.builder()
                .orderId(orderId)
                .paidAmount(paidAmount)
                .reference("AIMS" + orderId)
                .method("VIETQR")
                .build();
    }

    // UT046
    @Test
    @DisplayName("UT046: confirm - matching order with correct amount becomes PAYMENT_SUCCESS and is saved")
    void confirm_matchingOrderCorrectAmount_updatesStatusAndSaves() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        PaymentConfirmationResult result = confirmationService.confirm(command(1, 122000L));

        assertEquals(PaymentConfirmationResult.Outcome.SUCCESS, result.getOutcome());
        assertEquals(OrderStatus.PAYMENT_SUCCESS, testOrder.getStatus());
        assertNotNull(result.getRefTransactionId());
        assertTrue(result.getRefTransactionId().startsWith("AIMS-TX-"));
        verify(orderRepository, times(1)).save(testOrder);
    }

    // UT050
    @Test
    @DisplayName("UT050: confirm - amount mismatch returns AMOUNT_MISMATCH and leaves order unpaid")
    void confirm_amountMismatch_returnsMismatchAndDoesNotPay() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        PaymentConfirmationResult result = confirmationService.confirm(command(1, 999L));

        assertEquals(PaymentConfirmationResult.Outcome.AMOUNT_MISMATCH, result.getOutcome());
        assertEquals(OrderStatus.PENDING_PAYMENT, testOrder.getStatus());
        verify(orderRepository, never()).save(any());
    }

    // UT051
    @Test
    @DisplayName("UT051: confirm - unknown order returns ORDER_NOT_FOUND")
    void confirm_orderNotFound_returnsOrderNotFound() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        PaymentConfirmationResult result = confirmationService.confirm(command(99, 122000L));

        assertEquals(PaymentConfirmationResult.Outcome.ORDER_NOT_FOUND, result.getOutcome());
        verify(orderRepository, never()).save(any());
    }

    // UT054
    @Test
    @DisplayName("UT054: confirm - missing invoice total returns INVALID_ORDER_STATE")
    void confirm_noInvoiceTotal_returnsInvalidOrderState() {
        Order orderNoInvoice = Order.builder()
                .orderId(2)
                .status(OrderStatus.PENDING_PAYMENT)
                .orderDetails(new HashSet<>())
                .build();
        when(orderRepository.findById(2)).thenReturn(Optional.of(orderNoInvoice));

        PaymentConfirmationResult result = confirmationService.confirm(command(2, 122000L));

        assertEquals(PaymentConfirmationResult.Outcome.INVALID_ORDER_STATE, result.getOutcome());
        verify(orderRepository, never()).save(any());
    }
}