package com.example.aims.service;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

/*
 * Testing strategy for PayOrderQueryService (gateway-neutral pay-order reads).
 *
 * Units under test:
 *   - getPayableAmount(Integer) : reads the invoice total via a scalar projection
 *   - getPayOrderResult(Integer): loads the order and delegates DTO assembly
 *
 * Strategy: black-box with equivalence partitioning. OrderRepository, InvoiceRepository
 * and PayOrderResultAssembler are mocked to isolate the service.
 *
 * Partitions:
 *   getPayableAmount: [present] returns invoice total | [missing] ResourceNotFoundException
 *   getPayOrderResult:[order present] returns assembled DTO | [missing] ResourceNotFoundException
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayOrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PayOrderResultAssembler payOrderResultAssembler;

    @InjectMocks
    private PayOrderQueryService payOrderQueryService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .orderId(1)
                .status(OrderStatus.PAYMENT_SUCCESS)
                .shippingFee(BigDecimal.valueOf(22000))
                .orderDetails(new HashSet<>())
                .build();

        Invoice invoice = Invoice.builder()
                .invoiceId(1)
                .totalAmount(BigDecimal.valueOf(122000))
                .build();
        testOrder.setInvoice(invoice);
    }

    // UT052
    @Test
    @DisplayName("UT052: getPayableAmount - returns the invoice total for the order")
    void getPayableAmount_invoicePresent_returnsTotal() {
        when(invoiceRepository.findTotalAmountByOrderId(1)).thenReturn(BigDecimal.valueOf(122000));

        long amount = payOrderQueryService.getPayableAmount(1);

        assertEquals(122000L, amount);
        verify(invoiceRepository, times(1)).findTotalAmountByOrderId(1);
    }

    // UT053
    @Test
    @DisplayName("UT053: getPayableAmount - throws ResourceNotFoundException when no invoice total exists")
    void getPayableAmount_noInvoiceTotal_throwsResourceNotFound() {
        when(invoiceRepository.findTotalAmountByOrderId(404)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> payOrderQueryService.getPayableAmount(404));
    }

    // UT057
    @Test
    @DisplayName("UT057: getPayOrderResult - loads order and returns the assembled DTO")
    void getPayOrderResult_orderPresent_returnsAssembledDto() {
        PayOrderResponse assembled = PayOrderResponse.builder()
                .orderId(1)
                .orderStatus(OrderStatus.PAYMENT_SUCCESS.name())
                .totalAmount(BigDecimal.valueOf(122000))
                .build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(payOrderResultAssembler.toPayOrderResponse(testOrder)).thenReturn(assembled);

        PayOrderResponse result = payOrderQueryService.getPayOrderResult(1);

        assertSame(assembled, result);
        verify(payOrderResultAssembler, times(1)).toPayOrderResponse(testOrder);
    }

    // UT058
    @Test
    @DisplayName("UT058: getPayOrderResult - throws ResourceNotFoundException when order is missing")
    void getPayOrderResult_orderMissing_throwsResourceNotFound() {
        when(orderRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> payOrderQueryService.getPayOrderResult(404));
        verify(payOrderResultAssembler, never()).toPayOrderResponse(any());
    }
}