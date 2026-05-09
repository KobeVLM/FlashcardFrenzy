package com.marikit.flashcardfrenzy.deck;

import jakarta.validation.constraints.NotBlank;

/**
 * Deck slice DTOs.
 */
public class DeckDto {

    // ── Requests ─────────────────────────────────────────────────────────────

    public record DeckRequest(
            @NotBlank(message = "Title is required")
            String title,

            String category,
            String description
    ) {}

    // ── Responses ─────────────────────────────────────────────────────────────

    public record DeckResponse(
            Long id,
            String title,
            String category,
            String description,
            Long userId,
            String ownerName,
            String createdAt
    ) {}
}
