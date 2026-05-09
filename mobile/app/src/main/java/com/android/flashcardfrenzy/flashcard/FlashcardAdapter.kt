package com.android.flashcardfrenzy.flashcard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.flashcardfrenzy.databinding.ItemFlashcardBinding

/**
 * RecyclerView adapter for flashcards shown inside DeckDetailActivity.
 *
 * item_flashcard.xml expected IDs:
 *   - tv_question   : TextView
 *   - tv_answer     : TextView
 *   - tv_tags       : TextView
 *   - btn_edit      : ImageButton  (only shown if owner — handled by DeckDetailActivity)
 *   - btn_delete    : ImageButton
 */
class FlashcardAdapter(
    private var cards: List<FlashcardDtos.FlashcardResponse> = emptyList(),
    private val onEditClick: (FlashcardDtos.FlashcardResponse) -> Unit,
    private val onDeleteClick: (FlashcardDtos.FlashcardResponse) -> Unit
) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(private val binding: ItemFlashcardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: FlashcardDtos.FlashcardResponse) {
            binding.tvQuestion.text = card.question
            binding.tvAnswer.text   = card.answer
            binding.tvTags.text     = card.tags ?: ""
            binding.btnEdit.setOnClickListener   { onEditClick(card) }
            binding.btnDelete.setOnClickListener { onDeleteClick(card) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val binding = ItemFlashcardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlashcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) =
        holder.bind(cards[position])

    override fun getItemCount() = cards.size

    fun updateData(newCards: List<FlashcardDtos.FlashcardResponse>) {
        cards = newCards
        notifyDataSetChanged()
    }
}
