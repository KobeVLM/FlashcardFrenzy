package com.marikit.flashcardfrenzy.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Auth slice DTOs — all request/response shapes for the auth feature.
 */
public class AuthDto {

    // ── Requests ─────────────────────────────────────────────────────────────

    public record RegisterRequest(
            @NotBlank(message = "Full name is required")
            String fullName,

            @Email(message = "Must be a valid email")
            @NotBlank(message = "Email is required")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password
    ) {}

    public record LoginRequest(
            @Email(message = "Must be a valid email")
            @NotBlank(message = "Email is required")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    /**
     * Used by POST /auth/refresh.
     * Client sends the refresh token to get a new access token.
     */
    public record RefreshRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) {}

    // ── Responses ─────────────────────────────────────────────────────────────

    /**
     * Returned on login and register.
     * Both access and refresh tokens are provided.
     */
    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String email,
            String fullName,
            String role
    ) {}

    /**
     * Returned on POST /auth/refresh.
     * Only a new access token is issued — refresh token stays the same.
     */
    public record RefreshResponse(
            String accessToken
    ) {}

    /**
     * Returned on GET /auth/me.
     */
    public record UserResponse(
            Long id,
            String email,
            String fullName,
            String role,
            String createdAt
    ) {}
}
