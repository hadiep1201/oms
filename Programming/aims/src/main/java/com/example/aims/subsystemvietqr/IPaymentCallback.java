package com.example.aims.subsystemvietqr;

/**
 * Inbound webhook port of the VietQR subsystem.
 * VietQRCallbackController depends on this interface; PayOrderService implements it.
 * The seam lets the webhook REST controller depend on an abstraction
 * (DIP / testability) instead of the concrete service.
 *
 * Cohesion: Functional - the single port for receiving VietQR webhook callbacks
 *           (transaction sync notification and token request).
 *
 * Coupling:
 * - Data coupling with VietQRCallbackController (caller) and PayOrderService
 *   (implementor): all parameters and return types are String (raw JSON).
 */
public interface IPaymentCallback {

    /**
     * Called when VietQR sends a payment webhook to AIMS.
     * @param payload raw JSON body from VietQR
     * @return JSON response to return to VietQR
     */
    String onTransactionReceived(String payload);

    /**
     * Called when VietQR requests an auth token before sending webhooks.
     * @return JSON TokenResponse
     */
    String provideToken();
}