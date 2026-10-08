package com.orbin.school.auth.dto;

import java.util.List;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,       // seconds
    Long userId,
    Long schoolId,
    String email,
    String fullName,
    List<String> roles
) {
    public static AuthResponse of(String accessToken, String refreshToken,
                                   long expiresInMs, Long userId, Long schoolId,
                                   String email, String fullName, List<String> roles) {
        return new AuthResponse(
            accessToken, refreshToken, "Bearer",
            expiresInMs / 1000,
            userId, schoolId, email, fullName, roles
        );
    }
}
