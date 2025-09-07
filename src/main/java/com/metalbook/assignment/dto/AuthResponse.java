package com.metalbook.assignment.dto;

/**
 * Authentication Response DTO
 */
public record AuthResponse(
        String token,
        String tokenType,
        Long expiresIn,
        UserInfo user
) {
    public AuthResponse(String token, Long expiresIn, UserInfo user) {
        this(token, "Bearer", expiresIn, user);
    }
}
