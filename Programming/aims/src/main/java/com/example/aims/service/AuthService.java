package com.example.aims.service;

import com.example.aims.config.JwtProperties;
import com.example.aims.dto.request.LoginRequest;
import com.example.aims.dto.request.RefreshTokenRequest;
import com.example.aims.dto.response.LoginResponse;
import com.example.aims.entity.User;
import com.example.aims.exception.AimsException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.security.CustomUserDetailsService;
import com.example.aims.security.JwtService;
import com.example.aims.security.SecurityUtils;
import com.example.aims.security.UserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coupling:
 * - Data coupling with CustomUserDetailsService, JwtService, RefreshTokenService, PasswordEncoder
 * Cohesion: functional cohesion — authentication use cases (login, refresh, logout, me).
 *
 * SOLID Review:
 * - SRP: compliant — refresh token persistence delegated to RefreshTokenService; JWT creation to JwtService.
 * - OCP: compliant — new auth flows can be added without changing token/JWT collaborators.
 * - LSP/ISP: not applicable.
 * - DIP: compliant — depends on injected security and token abstractions.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    CustomUserDetailsService userDetailsService;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    JwtProperties jwtProperties;
    RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userDetailsService.findActiveUser(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new AimsException(
                    ErrorCode.INVALID_CREDENTIALS.getCode(),
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        UserPrincipal principal = userDetailsService.loadByUserId(user.getUserId());
        refreshTokenService.revokeAllForUser(user);
        String refreshTokenValue = refreshTokenService.createForUser(user);

        return buildLoginResponse(principal, refreshTokenValue);
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        var stored = refreshTokenService.requireValid(request.getRefreshToken());
        UserPrincipal principal = userDetailsService.loadByUserId(stored.getUser().getUserId());
        return buildLoginResponse(principal, stored.getToken());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request);
    }

    public LoginResponse me() {
        UserPrincipal principal = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AimsException(
                        ErrorCode.UNAUTHORIZED.getCode(),
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.UNAUTHORIZED.getMessage()));
        return LoginResponse.builder()
                .userId(principal.getUserId())
                .userName(principal.getUserName())
                .email(principal.getEmail())
                .roles(principal.getRoleNames())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessExpirationMs() / 1000)
                .build();
    }

    private LoginResponse buildLoginResponse(UserPrincipal principal, String refreshTokenValue) {
        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(principal))
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessExpirationMs() / 1000)
                .userId(principal.getUserId())
                .userName(principal.getUserName())
                .email(principal.getEmail())
                .roles(principal.getRoleNames())
                .build();
    }
}
