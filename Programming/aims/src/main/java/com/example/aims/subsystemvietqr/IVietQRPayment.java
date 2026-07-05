package com.example.aims.subsystemvietqr;

import com.example.aims.entity.QRCode;

/**
 * Inbound port for the VietQR-specific payment initiation.
 * Implemented by VietQrPayOrderService (service.payment.vietqr); the single client is
 * VietQrPaymentProvider. Kept one-way (service -> subsystemvietqr) to avoid a cycle.
 *
 * Scope (ISP): exactly the one VietQR-specific operation its client uses - initiate a QR
 * payment. The pay-order result read is now the gateway-neutral IPayOrderResult port (in
 * package payment), used by a different client.
 *
 * Cohesion: Functional - initiate a VietQR payment.
 * Coupling: Data coupling - input is Integer orderId; output is a value object.
 */
public interface IVietQRPayment {

    QRCode payOrder(Integer orderId);
}