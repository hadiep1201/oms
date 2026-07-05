package com.example.aims.subsystemvietqr;

import com.example.aims.subsystemvietqr.dto.QRAccessTokenResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Acquires a VietQR API access token (Basic Auth -> Bearer token).
 *
 * Extracted so both the QR-code adapter and the simulator adapter share one
 * token-acquisition path instead of duplicating it.
 *
 * Cohesion: Functional - one responsibility: obtain a VietQR access token.
 * Coupling: Data coupling with VietQRApiClient - exchanges only String values; the
 *           credentials and Basic Auth header stay inside VietQRApiClient.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VietQRAccessTokenProvider {

    final VietQRApiClient apiClient;

    public String acquire() {
        String tokenResponseString = apiClient.getAccessToken();

        QRAccessTokenResponse tokenResponse = new QRAccessTokenResponse();
        tokenResponse.parseResponseString(tokenResponseString);
        return tokenResponse.getAccessToken();
    }
}