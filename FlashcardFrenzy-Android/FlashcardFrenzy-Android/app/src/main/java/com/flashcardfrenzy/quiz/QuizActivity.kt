package com.flashcardfrenzy.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flashcardfrenzy.common.TokenManager
import com.flashcardfrenzy.databinding.ActivityQuizBinding
import com.flashcardfrenzy.flashcard.FlashcardRepository
import com.flashcardfrenzy.network.RetrofitClient

/**
 * MVP — View
 * Runs the quiz session entirely client-side.
 * Cards are fetched, shuffled, and presented one by one.
 *
 * Layout IDs expected in activity_quiz.xml:
 *   - tv_progress      : TextView   e.g. "Card 3 / 10"
 *   - tv_question      : TextView
 *   - tv_answer        : TextView   (GONE until reveal)
 *   - btn_reveal       : Button     "Show Answer"
 *   - btn_correct      : Button     "Got it ✓"   (GONE until reveal)
 *   - btn_wrong        : Button     "Missed ✗"   (GONE until reveal)
 */
class QuizActivity : AppCompatActivity(), QuizContract.SessionView {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var presenter: QuizPresenter
    private lateinit var tokenManager: TokenManager

    private var deckId: Long    = -1L
    private var deckTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckId    = intent.getLongExtra(EXTRA_DECK_ID, -1L)
        deckTitle = intent.getStringExtra(EXTRA_DECK_TITLE) ?: "Quiz"
        if (deckId == -1L) { finish(); return }

        supportActionBar?.title = deckTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tokenManager = TokenManager.getInstance(this)

        val apiService = RetrofitClient.getApiService(tokenManager)
        presenter = QuizPresenter(
            sessionView          = this,
            quizRepository       = QuizRepository(apiService),
            flashcardRepository  = FlashcardRepository(apiService)
        )

        setupClickListeners()
        presenter.startQuiz(deckId)
    }

    private fun setupClickListeners() {
        binding.btnReveal.setOnClickListener {
            presenter.revealAnswer()
        }
        binding.btnCorrect.setOnClickListener {
            resetAnswerVisibility()
            presenter.markCorrect()
        }
        binding.btnWrong.setOnClickListener {
            resetAnswerVisibility()
            presenter.markWrong()
        }
    }

    private fun resetAnswerVisibility() {
        binding.tvAnswer.visibility  = View.GONE
        binding.btnCorrect.visibility = View.GONE
        binding.btnWrong.visibility   = View.GONE
        binding.btnReveal.visibility  = View.VISIBLE
    }

    // ── QuizContract.SessionView ──────────────────────────────────────────────

    override fun showQuestion(question: String, current: Int, total: Int) {
        binding.tvProgress.text = "Card $current / $total"
        binding.tvQuestion.text = question
        resetAnswerVisibility()
    }

    override fun showAnswer(answer: String) {
        binding.tvAnswer.text     = answer
        binding.tvAnswer.visibility  = View.VISIBLE
        binding.btnCorrect.visibility = View.VISIBLE
        binding.btnWrong.visibility   = View.VISIBLE
        binding.btnReveal.visibility  = View.GONE
    }

    override fun onQuizFinished(score: Int, total: Int, timeSpentSeconds: Int) {
        // Calculate percentage score (0–100)
        val percentage = if (total > 0) (score * 100) / total else 0

        // Submit result silently in background
        presenter.submitResult(deckId, percentage, timeSpentSeconds)

        // Show results dialog
        AlertDialog.Builder(this)
            .setTitle("Quiz Complete!")
            .setMessage(
                "Score: $score / $total ($percentage%)\n" +
                "Time: ${timeSpentSeconds}s"
            )
            .setPositiveButton("View History") { _, _ ->
                startActivity(Intent(this, QuizHistoryActivity::class.java))
                finish()
            }
            .setNegativeButton("Done") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    companion object {
        const val EXTRA_DECK_ID    = "extra_deck_id"
        const val EXTRA_DECK_TITLE = "extra_deck_title"
    }
}
