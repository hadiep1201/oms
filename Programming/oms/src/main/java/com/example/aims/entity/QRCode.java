package com.example.aims.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Value object representing the QR code returned by VietQR API.
 * Not a JPA entity — not persisted to DB.
 *
 * Cohesion: Functional - all fields represent the single domain concept of a
 *           VietQR payment QR code (image data, bank info, amount, transaction ID).
 *
 * Coupling:
 * - None - QRCode is a value object (not a JPA entity, not persisted).
 *   It is constructed by VietQRQrCodeAdapter and carried to the client through the
 *   payment initiation flow (VietQrPaymentProvider -> PaymentController) via the HTTP
 *   response; no shared mutable state with any other class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRCode {

    String qrCode; 
    String qrLink; 
    String bankCode;
    String bankName;
    String bankAccount;
    String userBankName;
    long amount;
    String content;
    String transactionId;
}