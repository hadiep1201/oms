package com.example.aims.service;

import com.example.aims.config.JwtProperties;
import com.example.aims.dto.request.RefreshTokenRequest;
import com.example.aims.entity.RefreshToken;
import com.example.aims.entity.User;
import com.example.aims.exception.AimsException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.repository.RefreshTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * SRP: manages refresh token lifecycle only.
 * Coupling: data coupling with RefreshTokenRepository and JwtProperties.
 * Cohesion: functional cohesion.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenService {

    RefreshTokenRepository refreshTokenRepository;
    JwtProperties jwtProperties;

    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public String createForUser(User user) {
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(Instant.now().plus(jwtProperties.getRefreshExpirationDays(), ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    @Transactional
    public RefreshToken requireValid(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository
                .findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new AimsException(
                        ErrorCode.UNAUTHORIZED.getCode(),
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new AimsException(
                    ErrorCode.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired");
        }

        return stored;
    }

    @Transactional
    public void revoke(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }
}
