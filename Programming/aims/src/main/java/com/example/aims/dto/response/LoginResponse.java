package com.example.aims.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {

    String accessToken;
    String refreshToken;
    String tokenType;
    long expiresIn;
    Integer userId;
    String userName;
    String email;
    List<String> roles;
}
