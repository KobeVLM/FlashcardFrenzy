package com.android.flashcardfrenzy.deck

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.flashcardfrenzy.databinding.ItemDeckBinding

/**
 * RecyclerView adapter for displaying a list of decks.
 *
 * item_deck.xml expected IDs:
 *   - tv_deck_title    : TextView
 *   - tv_deck_category : TextView
 *   - tv_owner_name    : TextView
 */
class DeckAdapter(
    private var decks: List<DeckDtos.DeckResponse> = emptyList(),
    private val onItemClick: (DeckDtos.DeckResponse) -> Unit
) : RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    inner class DeckViewHolder(private val binding: ItemDeckBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(deck: DeckDtos.DeckResponse) {
            binding.tvDeckTitle.text    = deck.title
            binding.tvDeckCategory.text = deck.category ?: "Uncategorized"
            binding.tvOwnerName.text    = deck.ownerName
            binding.root.setOnClickListener { onItemClick(deck) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = ItemDeckBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) =
        holder.bind(decks[position])

    override fun getItemCount() = decks.size

    fun updateData(newDecks: List<DeckDtos.DeckResponse>) {
        decks = newDecks
        notifyDataSetChanged()
    }
}
