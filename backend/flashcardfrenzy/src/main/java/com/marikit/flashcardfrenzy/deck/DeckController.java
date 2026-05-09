package com.marikit.flashcardfrenzy.deck;

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
 * HTTP routing and response wrapping only. All logic in DeckPresenter.
 *
 * Routes:
 *   GET    /api/v1/decks           — public   (supports ?search=keyword)
 *   GET    /api/v1/decks/my        — auth required
 *   GET    /api/v1/decks/{id}      — public
 *   POST   /api/v1/decks           — auth required
 *   PUT    /api/v1/decks/{id}      — auth required (owner only)
 *   DELETE /api/v1/decks/{id}      — auth required (owner only)
 */
@RestController
@RequestMapping("/api/v1/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckPresenter deckPresenter;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeckDto.DeckResponse>>> getAllDecks(
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(ApiResponse.ok(deckPresenter.getAllDecks(search)));
    }

    // NOTE: /my must be declared BEFORE /{id} so Spring does not
    // treat "my" as a path variable value.
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DeckDto.DeckResponse>>> getMyDecks(
            Principal principal) {

        return ResponseEntity.ok(ApiResponse.ok(deckPresenter.getMyDecks(principal.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeckDto.DeckResponse>> getDeckById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok(deckPresenter.getDeckById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeckDto.DeckResponse>> createDeck(
            @Valid @RequestBody DeckDto.DeckRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(deckPresenter.createDeck(request, principal.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeckDto.DeckResponse>> updateDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckDto.DeckRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                ApiResponse.ok(deckPresenter.updateDeck(id, request, principal.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDeck(
            @PathVariable Long id,
            Principal principal) {

        deckPresenter.deleteDeck(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Deck deleted successfully"));
    }
}
