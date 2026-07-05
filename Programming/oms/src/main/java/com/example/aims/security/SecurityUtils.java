package com.example.aims.security;

import com.example.aims.exception.AimsException;
import com.example.aims.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Integer requireCurrentUserId() {
        return getCurrentUser()
                .map(UserPrincipal::getUserId)
                .orElseThrow(() -> new AimsException(
                        ErrorCode.UNAUTHORIZED.getCode(),
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.UNAUTHORIZED.getMessage()));
    }
}
