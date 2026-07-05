package com.example.aims.security;

import com.example.aims.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {

    JwtProperties jwtProperties;

    public String generateAccessToken(UserPrincipal principal) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(principal.getUserId()))
                .claim("email", principal.getEmail())
                .claim("userName", principal.getUserName())
                .claim("roles", principal.getRoleNames())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getAccessExpirationMs()))
                .signWith(signingKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer extractUserId(String token) {
        return Integer.valueOf(parseClaims(token).getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private SecretKey signingKey() {
        String secret = jwtProperties.getSecret();
        byte[] keyBytes;
        if (secret.length() >= 64 && secret.matches("^[A-Za-z0-9+/=]+$")) {
            try {
                keyBytes = Decoders.BASE64.decode(secret);
            } catch (Exception ex) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
