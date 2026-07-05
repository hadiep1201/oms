package com.example.aims.subsystemvietqr;

import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * In-memory implementation of IWebhookTokenStore. Holds the most recent token
 * issued to VietQR so the inbound transaction-sync webhook can be authenticated.
 *
 * Single-token and in-memory: sufficient for a single backend instance. A restart
 * clears it, after which VietQR must obtain a fresh token before the next sync.
 *
 * Cohesion: Functional - issue and validate the callback token.
 * Coupling: none beyond JDK types.
 */
@Component
public class VietQRTokenStore implements IWebhookTokenStore {

    private volatile String token;
    private volatile Instant expiresAt;

    @Override
    public synchronized void issue(String token, long ttlSeconds) {
        this.token = token;
        this.expiresAt = Instant.now().plusSeconds(ttlSeconds);
    }

    @Override
    public synchronized boolean isValid(String candidate) {
        if (candidate == null || token == null || expiresAt == null) {
            return false;
        }
        if (Instant.now().isAfter(expiresAt)) {
            return false;
        }
        return token.equals(candidate);
    }
}