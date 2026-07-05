package com.example.aims.subsystemvietqr;

import com.example.aims.entity.Order;
import com.example.aims.entity.QRCode;

/**
 * Outbound port for VietQR QR-code generation.
 * VietQrPayOrderService depends on this interface - never on the concrete
 * VietQRQrCodeAdapter directly.
 *
 * Named for VietQR specifically (it is not a generic QR abstraction): a future QR gateway
 * declares its own port (e.g. IVnpayQrCode) implemented by its own adapter, so the two do
 * not collide on a single shared bean type.
 *
 * Cohesion: Functional - single port for VietQR QR code generation.
 *
 * Coupling:
 * - Stamp coupling with Order: the implementor reads order fields to build the
 *   QR request content.
 * - Data coupling with QRCode: return value whose fields are consumed by callers.
 *
 * SOLID:
 * - ISP: exposes exactly one operation that every implementor uses.
 * - DIP: lets VietQrPayOrderService (high level) avoid depending on the adapter (low level).
 */
public interface IVietQrQrCode {

    QRCode generateQRCode(Order order);
}