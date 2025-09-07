package com.metalbook.assignment.dto;

import java.time.LocalDateTime;

/**
 * API Success Response wrapper
 */
public record ApiResponseCustom<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public ApiResponseCustom(T data) {
        this(true, "Success", data, LocalDateTime.now());
    }

    public ApiResponseCustom(String message, T data) {
        this(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponseCustom<T> success(T data) {
        return new ApiResponseCustom<>(data);
    }

    public static <T> ApiResponseCustom<T> success(String message, T data) {
        return new ApiResponseCustom<>(message, data);
    }
}
