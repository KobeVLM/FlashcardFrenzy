package com.marikit.flashcardfrenzy.deck;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    // Used by GET /decks/my — returns only the authenticated user's decks
    List<Deck> findByUserId(Long userId);

    // Used by GET /decks?search=keyword — case-insensitive partial match on title or category
    @Query("SELECT d FROM Deck d WHERE " +
           "LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Deck> searchByKeyword(@Param("keyword") String keyword);
}
