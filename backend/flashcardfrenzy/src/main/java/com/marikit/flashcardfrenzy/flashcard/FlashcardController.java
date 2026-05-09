package com.marikit.flashcardfrenzy.flashcard;

import com.marikit.flashcardfrenzy.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * MVP — View (Controller)
 * HTTP routing and response wrapping only. All logic in FlashcardPresenter.
 *
 * Routes:
 *   GET    /api/v1/decks/{id}/cards   — public
 *   GET    /api/v1/cards/{cardId}     — public  (NEW)
 *   POST   /api/v1/decks/{id}/cards   — auth required (deck owner only)
 *   PUT    /api/v1/cards/{cardId}     — auth required (card owner only)
 *   DELETE /api/v1/cards/{cardId}     — auth required (card owner only)
 */
@RestController
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardPresenter flashcardPresenter;

    // ── GET /decks/{id}/cards  (public) ───────────────────────────────────────

    @GetMapping("/api/v1/decks/{id}/cards")
    public ResponseEntity<ApiResponse<List<FlashcardDto.FlashcardResponse>>> getCardsByDeck(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok(flashcardPresenter.getCardsByDeck(id)));
    }

    // ── GET /cards/{cardId}  (public) ─────────────────────────────────────────

    @GetMapping("/api/v1/cards/{cardId}")
    public ResponseEntity<ApiResponse<FlashcardDto.FlashcardResponse>> getCardById(
            @PathVariable Long cardId) {

        return ResponseEntity.ok(ApiResponse.ok(flashcardPresenter.getCardById(cardId)));
    }

    // ── POST /decks/{id}/cards ────────────────────────────────────────────────

    @PostMapping("/api/v1/decks/{id}/cards")
    public ResponseEntity<ApiResponse<FlashcardDto.FlashcardResponse>> createCard(
            @PathVariable Long id,
            @Valid @RequestBody FlashcardDto.FlashcardRequest request,
            Principal principal) {

        FlashcardDto.FlashcardResponse response =
                flashcardPresenter.createCard(id, request, principal.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    // ── PUT /cards/{cardId} ───────────────────────────────────────────────────

    @PutMapping("/api/v1/cards/{cardId}")
    public ResponseEntity<ApiResponse<FlashcardDto.FlashcardResponse>> updateCard(
            @PathVariable Long cardId,
            @Valid @RequestBody FlashcardDto.FlashcardRequest request,
            Principal principal) {

        FlashcardDto.FlashcardResponse response =
                flashcardPresenter.updateCard(cardId, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ── DELETE /cards/{cardId} ────────────────────────────────────────────────

    @DeleteMapping("/api/v1/cards/{cardId}")
    public ResponseEntity<ApiResponse<String>> deleteCard(
            @PathVariable Long cardId,
            Principal principal) {

        flashcardPresenter.deleteCard(cardId, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Flashcard deleted successfully"));
    }
}
