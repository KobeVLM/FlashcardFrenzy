package com.flashcardfrenzy.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // Returns results newest-first for the quiz history endpoint
    List<QuizResult> findByUserIdOrderByCreatedAtDesc(Long userId);
}
