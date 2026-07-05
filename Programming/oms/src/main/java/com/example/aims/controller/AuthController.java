package com.example.aims.controller;

import com.example.aims.dto.request.LoginRequest;
import com.example.aims.dto.request.RefreshTokenRequest;
import com.example.aims.dto.response.ApiResponse;
import com.example.aims.dto.response.LoginResponse;
import com.example.aims.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

/**
 * Coupling: data coupling with AuthService.
 * Cohesion: functional cohesion — one endpoint per auth use case.
 * SOLID: SRP compliant; delegates all auth logic to AuthService.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ApiResponse.<LoginResponse>builder()
                .result(result)
                .message("Login successful")
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        LoginResponse result = authService.refresh(request);
        return ApiResponse.<LoginResponse>builder()
                .result(result)
                .message("Token refreshed")
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request);
        return ApiResponse.<Void>builder()
                .message("Logged out")
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me() {
        LoginResponse result = authService.me();
        return ApiResponse.<LoginResponse>builder()
                .result(result)
                .build();
    }
}
