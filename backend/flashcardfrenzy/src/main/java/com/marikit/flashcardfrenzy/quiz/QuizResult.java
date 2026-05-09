package com.marikit.flashcardfrenzy.quiz;

import com.marikit.flashcardfrenzy.auth.User;
import com.marikit.flashcardfrenzy.deck.Deck;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * MVP — Model
 * Represents the `quiz_results` table.
 *
 * SDD fix: column was originally named `date_taken` — renamed to `created_at`
 * for consistency with all other tables (users, decks, flashcards all use created_at).
 */
@Entity
@Table(name = "quiz_results")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    // SDD: score must be between 0 and 100
    @Column(nullable = false)
    private Integer score;

    // Duration in seconds
    @Column(name = "time_spent")
    private Integer timeSpent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
