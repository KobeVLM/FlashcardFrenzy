package com.marikit.flashcardfrenzy.quiz;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Quiz slice DTOs.
 */
public class QuizDto {

    // ── Requests ─────────────────────────────────────────────────────────────

    public record QuizResultRequest(
            @NotNull(message = "Deck ID is required")
            Long deckId,

            @NotNull(message = "Score is required")
            @Min(value = 0,   message = "Score cannot be negative")
            @Max(value = 100, message = "Score cannot exceed 100")
            Integer score,

            @Min(value = 0, message = "Time spent cannot be negative")
            Integer timeSpent
    ) {}

    // ── Responses ─────────────────────────────────────────────────────────────

    public record QuizResultResponse(
            Long    id,
            Long    deckId,
            String  deckTitle,
            Integer score,
            Integer timeSpent,
            String  createdAt    // was "dateTaken" — fixed for consistency
    ) {}
}
