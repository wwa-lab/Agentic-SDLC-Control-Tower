package com.sdlctower.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Consistent API response envelope for all endpoints.
 * {@code data} is present on success; {@code error} is present on failure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(T data, String error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> fail(String error) {
        return new ApiResponse<>(null, error);
    }
}
