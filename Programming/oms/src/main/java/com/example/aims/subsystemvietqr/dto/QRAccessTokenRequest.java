package com.example.aims.subsystemvietqr.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Base64;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRAccessTokenRequest {

    String username;
    String password;

    public String buildAuthorizationHeader() {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
