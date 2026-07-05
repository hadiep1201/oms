package com.example.aims.subsystemvietqr;

import com.example.aims.subsystemvietqr.dto.QRAccessTokenRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Low-level HTTP client for the VietQR API.
 * All outbound HTTP calls to VietQR go through this class.
 *
 * The client owns its credentials and builds its own Basic Auth header internally, so
 * the username/password never leave this class (no credential getters are exposed).
 *
 * Cohesion: Functional - solely responsible for HTTP communication with VietQR.
 * Coupling: Data coupling with its callers (VietQRAccessTokenProvider,
 *           VietQRQrCodeAdapter, VietQRSimulatorAdapter) - exchanges only String values.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VietQRApiClient {

    @Value("${vietqr.get-token-url}")
    String getTokenUrl;

    @Value("${vietqr.generate-qr-url}")
    String generateQrUrl;

    @Value("${vietqr.test-callback-url:https://dev.vietqr.org/vqr/bank/api/test/transaction-callback}")
    String testCallbackUrl;

    @Value("${vietqr.username}")
    String username;

    @Value("${vietqr.password}")
    String password;

    final RestTemplate restTemplate;

    public String getAccessToken() {
        String authHeader = new QRAccessTokenRequest(username, password).buildAuthorizationHeader();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getTokenUrl, entity, String.class);
        return response.getBody();
    }

    public String generateQRCode(String requestBody, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(generateQrUrl, entity, String.class);
        return response.getBody();
    }

    public String sendTestCallback(String requestBody, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(testCallbackUrl, entity, String.class);
        return response.getBody();
    }
}