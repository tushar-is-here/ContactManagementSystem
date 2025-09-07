package com.metalbook.assignment.dto;

import java.time.LocalDateTime;

/**
 * Contact Response DTO for returning contact information
 */
public record ContactResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
