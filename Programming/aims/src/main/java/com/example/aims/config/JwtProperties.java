package com.example.aims.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtProperties {

    String secret;
    long accessExpirationMs = 1_800_000L;
    int refreshExpirationDays = 7;
}
