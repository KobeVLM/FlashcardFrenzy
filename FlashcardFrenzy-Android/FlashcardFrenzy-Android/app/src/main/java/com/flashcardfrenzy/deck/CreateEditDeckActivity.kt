package com.flashcardfrenzy.deck

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flashcardfrenzy.common.TokenManager
import com.flashcardfrenzy.databinding.ActivityCreateEditDeckBinding
import com.flashcardfrenzy.network.RetrofitClient

/**
 * MVP — View
 * Used for both creating a new deck and editing an existing one.
 * Pass EXTRA_DECK_ID to enter edit mode (pre-fills fields).
 *
 * Layout IDs expected in activity_create_edit_deck.xml:
 *   - et_title       : EditText
 *   - et_category    : EditText
 *   - et_description : EditText
 *   - btn_save       : Button
 *   - progress_bar   : ProgressBar
 */
class CreateEditDeckActivity : AppCompatActivity(), DeckContract.CreateEditView {

    private lateinit var binding: ActivityCreateEditDeckBinding
    private lateinit var presenter: DeckPresenter
    private lateinit var tokenManager: TokenManager

    private var deckId: Long = -1L   // -1 = create mode; any other = edit mode
    private var existingDeck: DeckDtos.DeckResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEditDeckBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        deckId = intent.getLongExtra(EXTRA_DECK_ID, -1L)
        tokenManager = TokenManager.getInstance(this)

        setupPresenter()

        if (deckId != -1L) {
            supportActionBar?.title = "Edit Deck"
            // Load existing deck data to pre-fill fields
            loadExistingDeck()
        } else {
            supportActionBar?.title = "Create Deck"
        }

        binding.btnSave.setOnClickListener {
            val title       = binding.etTitle.text.toString().trim()
            val category    = binding.etCategory.text.toString().trim().ifBlank { null }
            val description = binding.etDescription.text.toString().trim().ifBlank { null }

            if (deckId != -1L) {
                presenter.updateDeck(deckId, title, category, description)
            } else {
                presenter.createDeck(title, category, description)
            }
        }
    }

    private fun setupPresenter() {
        val apiService = RetrofitClient.getApiService(tokenManager)
        presenter = DeckPresenter(
            createEditView = this,
            repository = DeckRepository(apiService)
        )
    }

    private fun loadExistingDeck() {
        // Load the deck via a detail presenter just for pre-filling
        val apiService = RetrofitClient.getApiService(tokenManager)
        val detailPresenter = DeckPresenter(
            detailView = object : DeckContract.DetailView {
                override fun showLoading() { binding.progressBar.visibility = View.VISIBLE }
                override fun hideLoading() { binding.progressBar.visibility = View.GONE }
                override fun onDeckLoaded(deck: DeckDtos.DeckResponse) {
                    binding.etTitle.setText(deck.title)
                    binding.etCategory.setText(deck.category ?: "")
                    binding.etDescription.setText(deck.description ?: "")
                }
                override fun onDeckDeleted() {}
                override fun onError(message: String) {
                    Toast.makeText(this@CreateEditDeckActivity, message, Toast.LENGTH_SHORT).show()
                }
            },
            repository = DeckRepository(apiService)
        )
        detailPresenter.loadDeckById(deckId)
    }

    // ── DeckContract.CreateEditView ───────────────────────────────────────────

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
    }

    override fun onSaveSuccess(deck: DeckDtos.DeckResponse) {
        Toast.makeText(this, "Deck saved!", Toast.LENGTH_SHORT).show()
        finish()
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
        const val EXTRA_DECK_ID = "extra_deck_id"
    }
}
