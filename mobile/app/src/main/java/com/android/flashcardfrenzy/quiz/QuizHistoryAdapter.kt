package com.android.flashcardfrenzy.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.flashcardfrenzy.databinding.ItemQuizHistoryBinding

/**
 * RecyclerView adapter for quiz history results.
 *
 * item_quiz_history.xml expected IDs:
 *   - tv_deck_title  : TextView
 *   - tv_score       : TextView   e.g. "85%"
 *   - tv_time_spent  : TextView   e.g. "42s"
 *   - tv_date        : TextView
 */
class QuizHistoryAdapter(
    private var results: List<QuizDtos.QuizResultResponse> = emptyList()
) : RecyclerView.Adapter<QuizHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemQuizHistoryBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: QuizDtos.QuizResultResponse) {
            binding.tvDeckTitle.text  = result.deckTitle
            binding.tvScore.text      = "${result.score}%"
            binding.tvTimeSpent.text  = "${result.timeSpent}s"
            // Show only date portion of ISO timestamp
            binding.tvDate.text       = result.createdAt.take(10)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemQuizHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) =
        holder.bind(results[position])

    override fun getItemCount() = results.size

    fun updateData(newResults: List<QuizDtos.QuizResultResponse>) {
        results = newResults
        notifyDataSetChanged()
    }
}
