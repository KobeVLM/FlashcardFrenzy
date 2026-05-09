package com.flashcardfrenzy.quiz;

import com.flashcardfrenzy.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * MVP — View (Controller)
 * HTTP routing and response wrapping only. All logic in QuizPresenter.
 *
 * Routes:
 *   POST /api/v1/quizzes/results   — auth required
 *   GET  /api/v1/quizzes/history   — auth required
 */
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizPresenter quizPresenter;

    @PostMapping("/results")
    public ResponseEntity<ApiResponse<QuizDto.QuizResultResponse>> submitResult(
            @Valid @RequestBody QuizDto.QuizResultRequest request,
            Principal principal) {

        QuizDto.QuizResultResponse response =
                quizPresenter.submitResult(request, principal.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<QuizDto.QuizResultResponse>>> getHistory(
            Principal principal) {

        return ResponseEntity.ok(ApiResponse.ok(quizPresenter.getHistory(principal.getName())));
    }
}
