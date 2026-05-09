package com.flashcardfrenzy.quiz;

import com.flashcardfrenzy.auth.User;
import com.flashcardfrenzy.auth.UserRepository;
import com.flashcardfrenzy.common.exception.ResourceNotFoundException;
import com.flashcardfrenzy.deck.Deck;
import com.flashcardfrenzy.deck.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MVP — Presenter
 * Handles quiz result submission and history retrieval.
 *
 * Note: There is no quiz session/start endpoint. Sessions are managed
 * client-side. The frontend fetches cards via GET /decks/{id}/cards,
 * runs the quiz locally, then calls POST /quizzes/results to save the score.
 */
@Service
@RequiredArgsConstructor
public class QuizPresenter {

    private final QuizResultRepository quizResultRepository;
    private final UserRepository       userRepository;
    private final DeckRepository       deckRepository;

    // ── POST /quizzes/results ─────────────────────────────────────────────────

    public QuizDto.QuizResultResponse submitResult(
            QuizDto.QuizResultRequest request,
            String email) {

        User user = getUser(email);
        Deck deck = deckRepository.findById(request.deckId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Deck not found with id: " + request.deckId()));

        QuizResult result = QuizResult.builder()
                .user(user)
                .deck(deck)
                .score(request.score())
                .timeSpent(request.timeSpent())
                .build();

        return toResponse(quizResultRepository.save(result));
    }

    // ── GET /quizzes/history ──────────────────────────────────────────────────

    public List<QuizDto.QuizResultResponse> getHistory(String email) {
        User user = getUser(email);
        return quizResultRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private QuizDto.QuizResultResponse toResponse(QuizResult result) {
        return new QuizDto.QuizResultResponse(
                result.getId(),
                result.getDeck().getId(),
                result.getDeck().getTitle(),
                result.getScore(),
                result.getTimeSpent(),
                result.getCreatedAt().toString()
        );
    }
}
