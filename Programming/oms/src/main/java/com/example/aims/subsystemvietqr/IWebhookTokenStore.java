package com.example.aims.subsystemvietqr;

/**
 * Abstraction over the store that holds the token issued to VietQR (via Get Token)
 * so the inbound transaction-sync webhook can be authenticated.
 *
 * Cohesion: Functional - issue and validate the callback token.
 * Coupling: Data coupling - only String/long cross the boundary.
 *
 * SOLID:
 * - DIP: callers (PayOrderService, VietQRCallbackController) depend on this
 *   abstraction rather than the concrete in-memory implementation, which also
 *   makes them easy to unit test.
 */
public interface IWebhookTokenStore {

    void issue(String token, long ttlSeconds);

    boolean isValid(String candidate);
}