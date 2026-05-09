package com.flashcardfrenzy.flashcard;

import com.flashcardfrenzy.deck.Deck;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * MVP — Model
 * Represents the `flashcards` table.
 * SDD: tags is stored as a comma-separated string (e.g., "math,algebra,equations")
 */
@Entity
@Table(name = "flashcards")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    // Comma-separated string — e.g. "math,algebra"
    // Max 500 chars to match DB column constraint
    @Column(length = 500)
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
