package com.marikit.flashcardfrenzy.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Standard API response envelope for ALL endpoints.
 * SDD contract: { success, data, error, timestamp }
 *
 * Rules:
 *  - data  must be null when success = false
 *  - error must be null when success = true
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ApiError error;
    private String timestamp;

    // ── Success ──────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── Error ────────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> fail(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ApiError.builder()
                        .code(code)
                        .message(message)
                        .details(null)
                        .build())
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> fail(String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ApiError.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .timestamp(Instant.now().toString())
                .build();
    }

    // ── Nested error object ──────────────────────────────────────────────────

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiError {
        private String code;
        private String message;
        private Object details;
    }
}
