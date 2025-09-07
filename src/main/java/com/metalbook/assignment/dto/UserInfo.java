package com.metalbook.assignment.dto;

import java.time.LocalDateTime;

/**
 * User Information DTO
 */
public record UserInfo(
        Long id,
        String username,
        String email,
        String role,
        LocalDateTime createdAt
) {}
