package com.marikit.flashcardfrenzy.deck;

import com.marikit.flashcardfrenzy.auth.User;
import com.marikit.flashcardfrenzy.auth.UserRepository;
import com.marikit.flashcardfrenzy.common.exception.ResourceNotFoundException;
import com.marikit.flashcardfrenzy.flashcard.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * MVP — Presenter
 * Handles all deck business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DeckPresenter {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final FlashcardRepository flashcardRepository;

    // ── GET /decks  (all, with optional search) ───────────────────────────────

    @Transactional(readOnly = true)
    public List<DeckDto.DeckResponse> getAllDecks(String keyword) {
        List<Deck> decks = (keyword != null && !keyword.isBlank())
                ? deckRepository.searchByKeyword(keyword)
                : deckRepository.findAll();
        return decks.stream().map(this::toResponse).toList();
    }

    // ── GET /decks/my  (authenticated user's own decks) ───────────────────────

    @Transactional(readOnly = true)
    public List<DeckDto.DeckResponse> getMyDecks(String email) {
        User user = getUser(email);
        return deckRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── GET /decks/{id} ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DeckDto.DeckResponse getDeckById(Long id) {
        return toResponse(getDeck(id));
    }

    // ── POST /decks ───────────────────────────────────────────────────────────

    public DeckDto.DeckResponse createDeck(DeckDto.DeckRequest request, String email) {
        User user = getUser(email);

        Deck deck = Deck.builder()
                .title(request.title())
                .category(request.category())
                .description(request.description())
                .user(user)
                .build();

        return toResponse(deckRepository.save(deck));
    }

    // ── PUT /decks/{id} ───────────────────────────────────────────────────────

    public DeckDto.DeckResponse updateDeck(Long id, DeckDto.DeckRequest request, String email) {
        Deck deck = getDeck(id);
        verifyOwner(deck, email);

        deck.setTitle(request.title());
        deck.setCategory(request.category());
        deck.setDescription(request.description());

        return toResponse(deckRepository.save(deck));
    }

    // ── DELETE /decks/{id} ────────────────────────────────────────────────────

    /**
     * Deletes the deck and all its flashcards (cascade).
     * SDD: flashcards.deck_id has ON DELETE CASCADE at DB level,
     * but we also delete explicitly here for clarity and safety.
     */
    @Transactional
    public void deleteDeck(Long id, String email) {
        Deck deck = getDeck(id);
        verifyOwner(deck, email);

        flashcardRepository.deleteByDeckId(id);
        deckRepository.delete(deck);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Deck getDeck(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id: " + id));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void verifyOwner(Deck deck, String email) {
        if (!deck.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You do not own this deck");
        }
    }

    private DeckDto.DeckResponse toResponse(Deck deck) {
        return new DeckDto.DeckResponse(
                deck.getId(),
                deck.getTitle(),
                deck.getCategory(),
                deck.getDescription(),
                deck.getUser().getId(),
                deck.getUser().getFullName(),
                deck.getCreatedAt() != null ? deck.getCreatedAt().toString() : Instant.now().toString()
        );
    }
}
