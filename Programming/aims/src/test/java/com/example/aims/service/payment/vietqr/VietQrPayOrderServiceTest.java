package com.example.aims.service.payment.vietqr;

import com.example.aims.entity.Order;
import com.example.aims.entity.QRCode;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.OrderRepository;
import com.example.aims.subsystemvietqr.IVietQrQrCode;
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
 * Testing strategy for VietQrPayOrderService (UC007 - VietQR QR initiation).
 *
 * Unit under test:
 *   - payOrder(Integer) : loads the order and delegates QR generation to IVietQrQrCode
 *
 * Strategy: black-box with equivalence partitioning. IVietQrQrCode and OrderRepository
 * are mocked to isolate the service.
 *
 * Partitions:
 *   [valid] returns QRCode | [generateQRCode throws] propagates | [order missing] throws
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VietQrPayOrderServiceTest {

    @Mock
    private IVietQrQrCode vietQrQrCode;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private VietQrPayOrderService vietQrPayOrderService;

    private Order testOrder;
    private QRCode mockQRCode;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .orderId(1)
                .status(OrderStatus.PENDING_PAYMENT)
                .shippingFee(BigDecimal.valueOf(22000))
                .orderDetails(new HashSet<>())
                .build();

        mockQRCode = QRCode.builder()
                .qrCode("base64QRdata==")
                .qrLink("https://img.vietqr.io/image/sample.png")
                .bankCode("MB")
                .bankAccount("1234567890")
                .amount(122000L)
                .content("AIMS1")
                .transactionId("TXN001")
                .build();
    }

    // UT042
    @Test
    @DisplayName("UT042: payOrder - returns QRCode when generateQRCode succeeds")
    void payOrder_validOrder_returnsQRCode() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(vietQrQrCode.generateQRCode(testOrder)).thenReturn(mockQRCode);

        QRCode result = vietQrPayOrderService.payOrder(1);

        assertNotNull(result);
        assertEquals("base64QRdata==", result.getQrCode());
        assertEquals("MB", result.getBankCode());
        assertEquals(122000L, result.getAmount());
        verify(vietQrQrCode, times(1)).generateQRCode(testOrder);
    }

    // UT043
    @Test
    @DisplayName("UT043: payOrder - exception from generateQRCode is propagated to caller")
    void payOrder_generateQRCodeThrows_exceptionPropagated() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(vietQrQrCode.generateQRCode(testOrder))
                .thenThrow(new RuntimeException("VietQR API unavailable"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vietQrPayOrderService.payOrder(1));

        assertEquals("VietQR API unavailable", ex.getMessage());
        verify(vietQrQrCode, times(1)).generateQRCode(testOrder);
    }

    // UT059
    @Test
    @DisplayName("UT059: payOrder - throws ResourceNotFoundException when order is missing")
    void payOrder_orderMissing_throwsResourceNotFound() {
        when(orderRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vietQrPayOrderService.payOrder(404));
        verify(vietQrQrCode, never()).generateQRCode(any());
    }
}