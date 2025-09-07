package com.metalbook.assignment.dto;

import java.time.LocalDateTime;

/**
 * API Error Response DTO
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now());
    }
}
