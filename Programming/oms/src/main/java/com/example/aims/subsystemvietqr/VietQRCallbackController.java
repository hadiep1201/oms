package com.example.aims.subsystemvietqr;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * REST controller inside the VietQR subsystem.
 * Receives inbound calls FROM VietQR (webhook / callback pattern).
 *
 * Endpoints:
 *  POST /api/token_generate        - VietQR requests a token before sending callbacks
 *  POST /bank/api/transaction-sync - VietQR notifies a completed transaction
 *
 * Cohesion: Functional - dedicated to receiving inbound VietQR callbacks.
 *
 * Coupling:
 * - Data coupling with IPaymentCallback: passes String payload; receives String
 *   JSON response. No complex object crosses the boundary.
 * - Data coupling with IWebhookTokenStore: checks the Bearer token against the token
 *   previously issued via /api/token_generate. Depends on the abstraction, not the
 *   concrete store (DIP).
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VietQRCallbackController {

    static final String BEARER_PREFIX = "Bearer ";

    final IPaymentCallback paymentCallback;
    final IWebhookTokenStore tokenStore;

    @Value("${vietqr.callback-username}")
    String callbackUsername;

    @Value("${vietqr.callback-password}")
    String callbackPassword;

    /**
     * When false (default), the transaction-sync webhook is accepted without Bearer
     * validation - useful while verifying the response format on the sandbox.
     * Set vietqr.callback.validate-token=true to enforce token authentication.
     */
    @Value("${vietqr.callback.validate-token:false}")
    boolean validateToken;

    /**
     * VietQR GET TOKEN - path: /oms/api/token_generate
     */
    @PostMapping(value = "/api/token_generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleTokenRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!isValidBasicAuth(authHeader)) {
            return "{\"status\":\"FAILED\",\"message\":\"UNAUTHORIZED\"}";
        }
        return paymentCallback.provideToken();
    }

    /**
     * VietQR TRANSACTION SYNC - path: /aims/bank/api/transaction-sync
     */
    @PostMapping(value = "/bank/api/transaction-sync",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleTransactionSync(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String payload) {

        if (validateToken && !tokenStore.isValid(extractBearer(authHeader))) {
            return "{\"error\":true,\"errorReason\":\"INVALID_TOKEN\","
                 + "\"toastMessage\":\"Invalid or expired token\",\"object\":null}";
        }
        return paymentCallback.onTransactionReceived(payload);
    }

    private String extractBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }

    private boolean isValidBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }
        try {
            String decoded = new String(
                Base64.getDecoder().decode(authHeader.substring(6)),
                StandardCharsets.UTF_8
            );
            String[] parts = decoded.split(":", 2);
            return parts.length == 2
                && callbackUsername.equals(parts[0])
                && callbackPassword.equals(parts[1]);
        } catch (Exception e) {
            return false;
        }
    }
}