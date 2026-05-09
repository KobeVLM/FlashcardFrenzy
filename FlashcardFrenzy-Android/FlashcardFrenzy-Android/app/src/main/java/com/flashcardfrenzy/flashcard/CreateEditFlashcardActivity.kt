package com.flashcardfrenzy.flashcard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flashcardfrenzy.common.TokenManager
import com.flashcardfrenzy.databinding.ActivityCreateEditFlashcardBinding
import com.flashcardfrenzy.network.RetrofitClient

/**
 * MVP — View
 * Create or edit a flashcard.
 * Pass EXTRA_CARD_ID to enter edit mode. Always pass EXTRA_DECK_ID.
 *
 * Layout IDs expected in activity_create_edit_flashcard.xml:
 *   - et_question  : EditText
 *   - et_answer    : EditText
 *   - et_tags      : EditText  (comma-separated e.g. "math,algebra")
 *   - btn_save     : Button
 *   - progress_bar : ProgressBar
 */
class CreateEditFlashcardActivity : AppCompatActivity(), FlashcardContract.CreateEditView {

    private lateinit var binding: ActivityCreateEditFlashcardBinding
    private lateinit var presenter: FlashcardPresenter
    private lateinit var tokenManager: TokenManager

    private var cardId: Long = -1L
    private var deckId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEditFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cardId = intent.getLongExtra(EXTRA_CARD_ID, -1L)
        deckId = intent.getLongExtra(EXTRA_DECK_ID, -1L)

        if (deckId == -1L) { finish(); return }

        tokenManager = TokenManager.getInstance(this)
        setupPresenter()

        if (cardId != -1L) {
            supportActionBar?.title = "Edit Card"
            presenter.getCardById(cardId)   // pre-fill fields via onSaveSuccess reuse
        } else {
            supportActionBar?.title = "Add Card"
        }

        binding.btnSave.setOnClickListener {
            val question = binding.etQuestion.text.toString().trim()
            val answer   = binding.etAnswer.text.toString().trim()
            val tags     = binding.etTags.text.toString().trim().ifBlank { null }

            if (cardId != -1L) {
                presenter.updateCard(cardId, question, answer, tags)
            } else {
                presenter.createCard(deckId, question, answer, tags)
            }
        }
    }

    private fun setupPresenter() {
        val apiService = RetrofitClient.getApiService(tokenManager)
        presenter = FlashcardPresenter(
            createEditView = this,
            repository = FlashcardRepository(apiService)
        )
    }

    // ── FlashcardContract.CreateEditView ──────────────────────────────────────

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
    }

    override fun onSaveSuccess(card: FlashcardDtos.FlashcardResponse) {
        // Called both for pre-filling (edit mode) and after save
        if (binding.etQuestion.text.isNullOrBlank()) {
            // Pre-fill mode
            binding.etQuestion.setText(card.question)
            binding.etAnswer.setText(card.answer)
            binding.etTags.setText(card.tags ?: "")
        } else {
            Toast.makeText(this, "Card saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    companion object {
        const val EXTRA_CARD_ID = "extra_card_id"
        const val EXTRA_DECK_ID = "extra_deck_id"
    }
}
