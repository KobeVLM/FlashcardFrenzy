package com.flashcardfrenzy.flashcard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByDeckId(Long deckId);

    // Called by DeckPresenter.deleteDeck() to cascade-delete cards before deleting the deck
    void deleteByDeckId(Long deckId);
}
