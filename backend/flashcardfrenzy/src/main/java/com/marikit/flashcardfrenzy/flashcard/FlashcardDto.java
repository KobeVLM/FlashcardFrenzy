package com.marikit.flashcardfrenzy.flashcard;

import jakarta.validation.constraints.NotBlank;

/**
 * Flashcard slice DTOs.
 * SDD: tags format is comma-separated string (e.g., "math,algebra,equations")
 */
public class FlashcardDto {

    // ── Requests ─────────────────────────────────────────────────────────────

    public record FlashcardRequest(
            @NotBlank(message = "Question is required")
            String question,

            @NotBlank(message = "Answer is required")
            String answer,

            // Optional — comma-separated string e.g. "math,algebra"
            String tags
    ) {}

    // ── Responses ─────────────────────────────────────────────────────────────

    public record FlashcardResponse(
            Long id,
            Long deckId,
            String question,
            String answer,
            String tags,
            String createdAt
    ) {}
}
