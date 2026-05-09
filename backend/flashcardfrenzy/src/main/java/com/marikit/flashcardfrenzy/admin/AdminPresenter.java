package com.marikit.flashcardfrenzy.admin;

import com.marikit.flashcardfrenzy.auth.UserRepository;
import com.marikit.flashcardfrenzy.common.exception.ResourceNotFoundException;
import com.marikit.flashcardfrenzy.deck.DeckRepository;
import com.marikit.flashcardfrenzy.flashcard.FlashcardRepository;
import com.marikit.flashcardfrenzy.quiz.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MVP — Presenter
 * Handles admin-only operations: platform stats, user listing, user deletion.
 *
 * ROLE_ADMIN is enforced at the SecurityConfig level — no annotation needed here.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminPresenter {

    private final UserRepository       userRepository;
    private final DeckRepository       deckRepository;
    private final FlashcardRepository  flashcardRepository;
    private final QuizResultRepository quizResultRepository;

    // ── GET /admin/stats ──────────────────────────────────────────────────────

    public AdminDto.StatsResponse getStats() {
        return new AdminDto.StatsResponse(
                userRepository.count(),
                deckRepository.count(),
                flashcardRepository.count(),
                quizResultRepository.count()
        );
    }

    // ── GET /admin/users ──────────────────────────────────────────────────────

    public List<AdminDto.AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new AdminDto.AdminUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name(),
                        user.getCreatedAt() != null ? user.getCreatedAt().toString() : java.time.Instant.now().toString()
                ))
                .toList();
    }

    // ── DELETE /admin/users/{id} ──────────────────────────────────────────────

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
