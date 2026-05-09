package com.flashcardfrenzy.quiz

import com.flashcardfrenzy.network.ApiService

/**
 * MVP — Model
 * Handles quiz-related API calls. Called only by QuizPresenter.
 */
class QuizRepository(private val apiService: ApiService) {

    suspend fun submitResult(request: QuizDtos.QuizResultRequest) =
        apiService.submitResult(request)

    suspend fun getHistory() =
        apiService.getQuizHistory()
}
