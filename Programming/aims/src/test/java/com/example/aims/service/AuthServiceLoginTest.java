package com.example.aims.service;

import com.example.aims.config.JwtProperties;
import com.example.aims.dto.request.LoginRequest;
import com.example.aims.dto.response.LoginResponse;
import com.example.aims.entity.User;
import com.example.aims.entity.UserStatus;
import com.example.aims.exception.AimsException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.security.CustomUserDetailsService;
import com.example.aims.security.JwtService;
import com.example.aims.security.UserPrincipal;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    private static final String RAW_PASSWORD = "P@ssw0rd123";
    private static final String HASHED_PASSWORD = "hashed-password";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    CustomUserDetailsService userDetailsService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @Mock
    JwtProperties jwtProperties;

    @Mock
    RefreshTokenService refreshTokenService;

    AuthService authService;

    User activeUser;
    UserPrincipal principal;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userDetailsService,
                passwordEncoder,
                jwtService,
                jwtProperties,
                refreshTokenService
        );

        activeUser = User.builder()
                .userId(1)
                .userName("pmanager01")
                .email("pmanager01@aims.com")
                .hashedPassword(HASHED_PASSWORD)
                .status(UserStatus.ACTIVE)
                .build();

        principal = new UserPrincipal(activeUser, List.of("PRODUCT_MANAGER"));
    }

    private void stubSuccessfulLogin(String loginId) {
        when(userDetailsService.findActiveUser(loginId)).thenReturn(activeUser);
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(userDetailsService.loadByUserId(1)).thenReturn(principal);
        when(refreshTokenService.createForUser(activeUser)).thenReturn(REFRESH_TOKEN);
        when(jwtService.generateAccessToken(principal)).thenReturn(ACCESS_TOKEN);
        when(jwtProperties.getAccessExpirationMs()).thenReturn(1_800_000L);
    }

    // UT065
    @Test
    @DisplayName("UT065: Login with valid username and password")
    void login_validUsernameAndPassword_returnsLoginResponse() {
        stubSuccessfulLogin("pmanager01");

        LoginResponse response = authService.login(
                LoginRequest.builder()
                        .username("pmanager01")
                        .password(RAW_PASSWORD)
                        .build()
        );

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getAccessToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1800L, response.getExpiresIn());
        assertEquals(1, response.getUserId());
        assertEquals("pmanager01", response.getUserName());
        assertEquals("pmanager01@aims.com", response.getEmail());
        assertEquals(List.of("PRODUCT_MANAGER"), response.getRoles());

        verify(refreshTokenService).revokeAllForUser(activeUser);
        verify(refreshTokenService).createForUser(activeUser);
        verify(jwtService).generateAccessToken(principal);
    }

    // UT066
    @Test
    @DisplayName("UT066: Login with valid email and password")
    void login_validEmailAndPassword_returnsLoginResponse() {
        stubSuccessfulLogin("pmanager01@aims.com");

        LoginResponse response = authService.login(
                LoginRequest.builder()
                        .username("pmanager01@aims.com")
                        .password(RAW_PASSWORD)
                        .build()
        );

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getAccessToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());

        verify(userDetailsService).findActiveUser("pmanager01@aims.com");
    }

    // UT067
    @Test
    @DisplayName("UT067: Login with incorrect password")
    void login_incorrectPassword_throwsInvalidCredentials() {
        when(userDetailsService.findActiveUser("pmanager01")).thenReturn(activeUser);
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        AimsException exception = assertThrows(
                AimsException.class,
                () -> authService.login(LoginRequest.builder()
                        .username("pmanager01")
                        .password(RAW_PASSWORD)
                        .build())
        );

        assertEquals(ErrorCode.INVALID_CREDENTIALS.getCode(), exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(ErrorCode.INVALID_CREDENTIALS.getMessage(), exception.getMessage());

        verify(refreshTokenService, never()).revokeAllForUser(any());
        verify(refreshTokenService, never()).createForUser(any());
        verify(jwtService, never()).generateAccessToken(any());
    }

    // UT068
    @Test
    @DisplayName("UT068: Login when username/email not found")
    void login_userNotFound_throwsInvalidCredentials() {
        when(userDetailsService.findActiveUser("unknown@aims.com"))
                .thenThrow(new AimsException(
                        ErrorCode.INVALID_CREDENTIALS.getCode(),
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.INVALID_CREDENTIALS.getMessage()));

        AimsException exception = assertThrows(
                AimsException.class,
                () -> authService.login(LoginRequest.builder()
                        .username("unknown@aims.com")
                        .password(RAW_PASSWORD)
                        .build())
        );

        assertEquals(ErrorCode.INVALID_CREDENTIALS.getCode(), exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(passwordEncoder, never()).matches(any(), any());
        verify(refreshTokenService, never()).createForUser(any());
    }

    // UT069
    @ParameterizedTest
    @CsvSource({
            "'', password",
            "username, ''"
    })
    @DisplayName("UT069: Login with blank username or password")
    void login_blankUsernameOrPassword_failsValidation(String username, String password) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            LoginRequest request = LoginRequest.builder()
                    .username(username)
                    .password(password)
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
        }
    }
}
