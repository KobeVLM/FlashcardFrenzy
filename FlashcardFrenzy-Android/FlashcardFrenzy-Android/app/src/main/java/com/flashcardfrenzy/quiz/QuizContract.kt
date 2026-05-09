package com.flashcardfrenzy.quiz

interface QuizContract {

    interface SessionView {
        fun showQuestion(question: String, current: Int, total: Int)
        fun showAnswer(answer: String)
        fun onQuizFinished(score: Int, total: Int, timeSpentSeconds: Int)
        fun onError(message: String)
    }

    interface HistoryView {
        fun showLoading()
        fun hideLoading()
        fun onHistoryLoaded(results: List<QuizDtos.QuizResultResponse>)
        fun onError(message: String)
    }

    interface Presenter {
        fun startQuiz(deckId: Long)
        fun revealAnswer()
        fun markCorrect()
        fun markWrong()
        fun submitResult(deckId: Long, score: Int, timeSpent: Int)
        fun loadHistory()
        fun onDestroy()
    }
}
