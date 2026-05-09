package com.flashcardfrenzy.deck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flashcardfrenzy.common.TokenManager
import com.flashcardfrenzy.databinding.ActivityDeckDetailBinding
import com.flashcardfrenzy.flashcard.FlashcardAdapter
import com.flashcardfrenzy.flashcard.FlashcardContract
import com.flashcardfrenzy.flashcard.FlashcardDtos
import com.flashcardfrenzy.flashcard.FlashcardPresenter
import com.flashcardfrenzy.flashcard.FlashcardRepository
import com.flashcardfrenzy.flashcard.CreateEditFlashcardActivity
import com.flashcardfrenzy.network.RetrofitClient
import com.flashcardfrenzy.quiz.QuizActivity

/**
 * MVP — View
 * Shows deck details and its flashcard list.
 * Allows starting a quiz, editing the deck, adding/deleting cards (owner only).
 *
 * Layout IDs expected in activity_deck_detail.xml:
 *   - tv_deck_title       : TextView
 *   - tv_deck_category    : TextView
 *   - tv_deck_description : TextView
 *   - tv_owner_name       : TextView
 *   - btn_start_quiz      : Button
 *   - btn_edit_deck       : Button   (gone if not owner)
 *   - btn_delete_deck     : Button   (gone if not owner)
 *   - fab_add_card        : FloatingActionButton (gone if not owner)
 *   - rv_flashcards       : RecyclerView
 *   - progress_bar        : ProgressBar
 */
class DeckDetailActivity : AppCompatActivity(),
    DeckContract.DetailView,
    FlashcardContract.ListView {

    private lateinit var binding: ActivityDeckDetailBinding
    private lateinit var deckPresenter: DeckPresenter
    private lateinit var flashcardPresenter: FlashcardPresenter
    private lateinit var tokenManager: TokenManager
    private lateinit var flashcardAdapter: FlashcardAdapter

    private var deckId: Long = -1
    private var currentDeck: DeckDtos.DeckResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeckDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        deckId = intent.getLongExtra(EXTRA_DECK_ID, -1)
        if (deckId == -1L) { finish(); return }

        tokenManager = TokenManager.getInstance(this)
        setupPresenters()
        setupRecyclerView()

        deckPresenter.loadDeckById(deckId)
        flashcardPresenter.loadCardsByDeck(deckId)
    }

    private fun setupPresenters() {
        val apiService = RetrofitClient.getApiService(tokenManager)
        deckPresenter = DeckPresenter(
            detailView = this,
            repository = DeckRepository(apiService)
        )
        flashcardPresenter = FlashcardPresenter(
            listView = this,
            repository = FlashcardRepository(apiService)
        )
    }

    private fun setupRecyclerView() {
        flashcardAdapter = FlashcardAdapter(
            onEditClick = { card ->
                val intent = Intent(this, CreateEditFlashcardActivity::class.java)
                intent.putExtra(CreateEditFlashcardActivity.EXTRA_CARD_ID, card.id)
                intent.putExtra(CreateEditFlashcardActivity.EXTRA_DECK_ID, deckId)
                startActivity(intent)
            },
            onDeleteClick = { card ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Card")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Delete") { _, _ ->
                        flashcardPresenter.deleteCard(card.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvFlashcards.layoutManager = LinearLayoutManager(this)
        binding.rvFlashcards.adapter = flashcardAdapter
    }

    private fun setupOwnerActions(deck: DeckDtos.DeckResponse) {
        val isOwner = tokenManager.isLoggedIn() && deck.ownerName == tokenManager.getFullName()

        binding.btnEditDeck.visibility  = if (isOwner) View.VISIBLE else View.GONE
        binding.btnDeleteDeck.visibility = if (isOwner) View.VISIBLE else View.GONE
        binding.fabAddCard.visibility   = if (isOwner) View.VISIBLE else View.GONE

        binding.btnEditDeck.setOnClickListener {
            val intent = Intent(this, CreateEditDeckActivity::class.java)
            intent.putExtra(CreateEditDeckActivity.EXTRA_DECK_ID, deckId)
            startActivity(intent)
        }

        binding.btnDeleteDeck.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Deck")
                .setMessage("This will delete the deck and all its flashcards. Continue?")
                .setPositiveButton("Delete") { _, _ -> deckPresenter.deleteDeck(deckId) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.fabAddCard.setOnClickListener {
            val intent = Intent(this, CreateEditFlashcardActivity::class.java)
            intent.putExtra(CreateEditFlashcardActivity.EXTRA_DECK_ID, deckId)
            startActivity(intent)
        }

        binding.btnStartQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra(QuizActivity.EXTRA_DECK_ID, deckId)
            intent.putExtra(QuizActivity.EXTRA_DECK_TITLE, deck.title)
            startActivity(intent)
        }
    }

    // ── DeckContract.DetailView ───────────────────────────────────────────────

    override fun showLoading() { binding.progressBar.visibility = View.VISIBLE }
    override fun hideLoading() { binding.progressBar.visibility = View.GONE }

    override fun onDeckLoaded(deck: DeckDtos.DeckResponse) {
        currentDeck = deck
        binding.tvDeckTitle.text       = deck.title
        binding.tvDeckCategory.text    = deck.category ?: "Uncategorized"
        binding.tvDeckDescription.text = deck.description ?: ""
        binding.tvOwnerName.text       = "By ${deck.ownerName}"
        supportActionBar?.title        = deck.title
        setupOwnerActions(deck)
    }

    override fun onDeckDeleted() { finish() }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // ── FlashcardContract.ListView ────────────────────────────────────────────

    override fun onCardsLoaded(cards: List<FlashcardDtos.FlashcardResponse>) {
        flashcardAdapter.updateData(cards)
    }

    override fun onCardDeleted() {
        flashcardPresenter.loadCardsByDeck(deckId)
    }

    override fun onResume() {
        super.onResume()
        flashcardPresenter.loadCardsByDeck(deckId)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    override fun onDestroy() {
        super.onDestroy()
        deckPresenter.onDestroy()
        flashcardPresenter.onDestroy()
    }

    companion object {
        const val EXTRA_DECK_ID = "extra_deck_id"
    }
}
