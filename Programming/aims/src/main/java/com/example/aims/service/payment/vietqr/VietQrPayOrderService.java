package com.example.aims.service.payment.vietqr;

import com.example.aims.entity.Order;
import com.example.aims.entity.QRCode;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.OrderRepository;
import com.example.aims.subsystemvietqr.IVietQRPayment;
import com.example.aims.subsystemvietqr.IVietQrQrCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

/**
 * VietQR-specific application service for the Pay Order use case (UC007): the one
 * operation that is intrinsically VietQR - generate a payment QR for an order.
 *
 * Lives in its own vertical (service.payment.vietqr) so a new QR gateway adds a parallel
 * vertical (e.g. service.payment.vnpayqr) without touching this class, and the neutral
 * reads stay in PayOrderQueryService. It delegates the actual VietQR protocol to the
 * IVietQrQrCode port (implemented by VietQRQrCodeAdapter); it holds no protocol detail.
 *
 * Cohesion: Functional - initiate a VietQR payment for an order.
 *
 * Coupling:
 * - Data coupling with IVietQrQrCode (passes Order, receives QRCode).
 * - Data coupling with OrderRepository.
 *
 * SOLID:
 * - SRP: only VietQR QR initiation; neutral reads live in PayOrderQueryService.
 * - DIP: depends on the IVietQrQrCode abstraction, not the concrete adapter.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietQrPayOrderService implements IVietQRPayment {

    IVietQrQrCode vietQrQrCode;
    OrderRepository orderRepository;

    @Override
    public QRCode payOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return vietQrQrCode.generateQRCode(order);
    }
}