package com.example.aims.subsystemvietqr;

/**
 * Outbound port for simulating a VietQR payment on the sandbox.
 *
 * Implemented by VietQRSimulatorAdapter (which talks to the VietQR API) and called by
 * VietQrPaymentProvider. It triggers VietQR's Test Callback so that VietQR, in turn,
 * calls this system's transaction-sync endpoint - i.e. it automates the manual
 * Thunder Client / Postman step used during sandbox testing.
 *
 * Cohesion: Functional - a single operation: ask VietQR to replay a transaction.
 * Coupling: Data coupling - inputs are two scalars; output is the raw verdict String.
 *
 * SOLID:
 * - ISP: one focused method, separate from QR generation (IPaymentQRCode), so no
 *   client is forced to depend on operations it does not use.
 * - DIP: lets the caller depend on this abstraction rather than the concrete
 *   VietQRSimulatorAdapter.
 */
public interface IVietQRSimulator {

    /**
     * Triggers VietQR's sandbox Test Callback for the given transfer content and amount.
     * VietQR responds by calling this system's transaction-sync endpoint.
     *
     * @param content the transfer content shown on the QR (e.g. "AIMS1")
     * @param amount  the transfer amount in VND (whole number)
     * @return VietQR's raw verdict JSON (kept as String for diagnostics, e.g. E222)
     */
    String triggerTestCallback(String content, long amount);
}