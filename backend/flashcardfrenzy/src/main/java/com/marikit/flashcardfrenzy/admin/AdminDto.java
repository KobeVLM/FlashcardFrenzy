package com.marikit.flashcardfrenzy.admin;

/**
 * Admin slice DTOs.
 */
public class AdminDto {

    public record StatsResponse(
            long totalUsers,
            long totalDecks,
            long totalFlashcards,
            long totalQuizResults
    ) {}

    public record AdminUserResponse(
            Long   id,
            String email,
            String fullName,
            String role,
            String createdAt
    ) {}
}
