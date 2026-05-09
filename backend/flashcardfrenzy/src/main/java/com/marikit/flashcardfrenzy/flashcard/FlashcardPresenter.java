package com.marikit.flashcardfrenzy.flashcard;

import com.marikit.flashcardfrenzy.common.exception.ResourceNotFoundException;
import com.marikit.flashcardfrenzy.deck.Deck;
import com.marikit.flashcardfrenzy.deck.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * MVP — Presenter
 * Handles all flashcard business logic.
 *
 * Read operations (getCardsByDeck, getCardById) are public — no auth check.
 * Write operations (create, update, delete) verify deck/card ownership.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FlashcardPresenter {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;

    // ── GET /decks/{id}/cards  (public) ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FlashcardDto.FlashcardResponse> getCardsByDeck(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new ResourceNotFoundException("Deck not found with id: " + deckId);
        }
        return flashcardRepository.findByDeckId(deckId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── GET /cards/{cardId}  (public) ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public FlashcardDto.FlashcardResponse getCardById(Long cardId) {
        return toResponse(getCard(cardId));
    }

    // ── POST /decks/{id}/cards ────────────────────────────────────────────────

    public FlashcardDto.FlashcardResponse createCard(
            Long deckId,
            FlashcardDto.FlashcardRequest request,
            String email) {

        Deck deck = getDeckAndVerifyOwner(deckId, email);

        Flashcard card = Flashcard.builder()
                .deck(deck)
                .question(request.question())
                .answer(request.answer())
                .tags(request.tags())
                .build();

        return toResponse(flashcardRepository.save(card));
    }

    // ── PUT /cards/{cardId} ───────────────────────────────────────────────────

    public FlashcardDto.FlashcardResponse updateCard(
            Long cardId,
            FlashcardDto.FlashcardRequest request,
            String email) {

        Flashcard card = getCard(cardId);
        verifyCardOwner(card, email);

        card.setQuestion(request.question());
        card.setAnswer(request.answer());
        card.setTags(request.tags());

        return toResponse(flashcardRepository.save(card));
    }

    // ── DELETE /cards/{cardId} ────────────────────────────────────────────────

    public void deleteCard(Long cardId, String email) {
        Flashcard card = getCard(cardId);
        verifyCardOwner(card, email);
        flashcardRepository.delete(card);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Flashcard getCard(Long cardId) {
        return flashcardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found with id: " + cardId));
    }

    private Deck getDeckAndVerifyOwner(Long deckId, String email) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id: " + deckId));
        if (!deck.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You do not own this deck");
        }
        return deck;
    }

    private void verifyCardOwner(Flashcard card, String email) {
        if (!card.getDeck().getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You do not own this flashcard");
        }
    }

    private FlashcardDto.FlashcardResponse toResponse(Flashcard card) {
        return new FlashcardDto.FlashcardResponse(
                card.getId(),
                card.getDeck().getId(),
                card.getQuestion(),
                card.getAnswer(),
                card.getTags(),
                card.getCreatedAt() != null ? card.getCreatedAt().toString() : Instant.now().toString()
        );
    }
}
