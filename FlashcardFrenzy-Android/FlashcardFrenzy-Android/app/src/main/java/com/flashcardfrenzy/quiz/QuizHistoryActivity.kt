package com.flashcardfrenzy.quiz

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flashcardfrenzy.common.TokenManager
import com.flashcardfrenzy.databinding.ActivityQuizHistoryBinding
import com.flashcardfrenzy.flashcard.FlashcardRepository
import com.flashcardfrenzy.network.RetrofitClient

/**
 * MVP — View
 * Shows the authenticated user's quiz history, newest first.
 *
 * Layout IDs expected in activity_quiz_history.xml:
 *   - rv_history    : RecyclerView
 *   - progress_bar  : ProgressBar
 *   - tv_empty      : TextView  (shown when no history)
 */
class QuizHistoryActivity : AppCompatActivity(), QuizContract.HistoryView {

    private lateinit var binding: ActivityQuizHistoryBinding
    private lateinit var presenter: QuizPresenter
    private lateinit var adapter: QuizHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Quiz History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tokenManager = TokenManager.getInstance(this)
        val apiService   = RetrofitClient.getApiService(tokenManager)

        adapter = QuizHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        presenter = QuizPresenter(
            historyView         = this,
            quizRepository      = QuizRepository(apiService),
            flashcardRepository = FlashcardRepository(apiService)
        )

        presenter.loadHistory()
    }

    // ── QuizContract.HistoryView ──────────────────────────────────────────────

    override fun showLoading() { binding.progressBar.visibility = View.VISIBLE }
    override fun hideLoading() { binding.progressBar.visibility = View.GONE }

    override fun onHistoryLoaded(results: List<QuizDtos.QuizResultResponse>) {
        adapter.updateData(results)
        binding.tvEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
