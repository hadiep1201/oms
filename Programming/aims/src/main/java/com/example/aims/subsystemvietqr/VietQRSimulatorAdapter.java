package com.example.aims.subsystemvietqr;

import com.example.aims.subsystemvietqr.dto.TestCallbackRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter realizing IVietQRSimulator: triggers VietQR's sandbox Test Callback so
 * VietQR, in turn, calls this system's transaction-sync endpoint (automating the
 * manual Thunder Client / Postman step).
 *
 * Cohesion: Functional - one responsibility: replay a transaction via VietQR sandbox.
 *
 * Coupling:
 * - Data coupling with VietQRApiClient and VietQRAccessTokenProvider (String values).
 *
 * SOLID:
 * - SRP: simulation only; QR generation lives in VietQRQrCodeAdapter.
 * - DIP: callers depend on IVietQRSimulator; token acquisition delegated to the
 *   VietQRAccessTokenProvider.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VietQRSimulatorAdapter implements IVietQRSimulator {

    final VietQRApiClient apiClient;
    final VietQRAccessTokenProvider tokenProvider;

    @Value("${vietqr.bank-code}")
    String bankCode;

    @Value("${vietqr.bank-account}")
    String bankAccount;

    @Override
    public String triggerTestCallback(String content, long amount) {
        String accessToken = tokenProvider.acquire();

        TestCallbackRequest request = new TestCallbackRequest(bankAccount, bankCode, content, amount);
        return apiClient.sendTestCallback(request.buildRequestString(), accessToken);
    }
}