package com.android.flashcardfrenzy.quiz

import com.android.flashcardfrenzy.flashcard.FlashcardDtos
import com.android.flashcardfrenzy.flashcard.FlashcardRepository
import kotlinx.coroutines.*

/**
 * MVP — Presenter
 * Manages the entire quiz session client-side (SDD: no server-side session).
 *
 * Flow:
 *  1. startQuiz(deckId) — fetches cards via FlashcardRepository, shuffles them
 *  2. revealAnswer()    — tells View to show the answer for the current card
 *  3. markCorrect()     — increments score, advances to next card
 *  4. markWrong()       — advances without incrementing score
 *  5. When all cards done → onQuizFinished(), then submitResult() to backend
 */
class QuizPresenter(
    private val sessionView: QuizContract.SessionView? = null,
    private val historyView: QuizContract.HistoryView? = null,
    private val quizRepository: QuizRepository,
    private val flashcardRepository: FlashcardRepository
) : QuizContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // ── Session state ─────────────────────────────────────────────────────────

    private var cards: List<FlashcardDtos.FlashcardResponse> = emptyList()
    private var currentIndex = 0
    private var correctCount = 0
    private var startTimeMs  = 0L

    // ── Start quiz ────────────────────────────────────────────────────────────

    override fun startQuiz(deckId: Long) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    flashcardRepository.getCardsByDeck(deckId)
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    cards = (response.body()?.data ?: emptyList()).shuffled()
                    if (cards.isEmpty()) {
                        sessionView?.onError("This deck has no flashcards yet.")
                        return@launch
                    }
                    currentIndex = 0
                    correctCount = 0
                    startTimeMs  = System.currentTimeMillis()
                    showCurrentCard()
                } else {
                    sessionView?.onError(response.body()?.error?.message ?: "Failed to load cards")
                }
            } catch (e: Exception) {
                sessionView?.onError("Network error: ${e.message}")
            }
        }
    }

    // ── Card navigation ───────────────────────────────────────────────────────

    override fun revealAnswer() {
        if (currentIndex < cards.size) {
            sessionView?.showAnswer(cards[currentIndex].answer)
        }
    }

    override fun markCorrect() {
        correctCount++
        advance()
    }

    override fun markWrong() {
        advance()
    }

    private fun advance() {
        currentIndex++
        if (currentIndex < cards.size) {
            showCurrentCard()
        } else {
            val timeSpentSeconds = ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()
            sessionView?.onQuizFinished(correctCount, cards.size, timeSpentSeconds)
        }
    }

    private fun showCurrentCard() {
        sessionView?.showQuestion(
            question = cards[currentIndex].question,
            current  = currentIndex + 1,
            total    = cards.size
        )
    }

    // ── Submit result to backend ──────────────────────────────────────────────

    override fun submitResult(deckId: Long, score: Int, timeSpent: Int) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    quizRepository.submitResult(
                        QuizDtos.QuizResultRequest(deckId, score, timeSpent)
                    )
                }
                // No UI update needed — result saved silently in the background
            } catch (_: Exception) {
                // Non-critical: don't show error if submit fails
            }
        }
    }

    // ── Load history ──────────────────────────────────────────────────────────

    override fun loadHistory() {
        historyView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { quizRepository.getHistory() }
                if (response.isSuccessful && response.body()?.success == true) {
                    historyView?.onHistoryLoaded(response.body()?.data ?: emptyList())
                } else {
                    historyView?.onError(response.body()?.error?.message ?: "Failed to load history")
                }
            } catch (e: Exception) {
                historyView?.onError("Network error: ${e.message}")
            } finally {
                historyView?.hideLoading()
            }
        }
    }

    override fun onDestroy() = scope.cancel()
}
