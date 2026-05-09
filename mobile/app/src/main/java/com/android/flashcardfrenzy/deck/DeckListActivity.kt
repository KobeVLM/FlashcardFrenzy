package com.android.flashcardfrenzy.deck

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.flashcardfrenzy.R
import com.android.flashcardfrenzy.admin.AdminActivity
import com.android.flashcardfrenzy.auth.LoginActivity
import com.android.flashcardfrenzy.common.TokenManager
import com.android.flashcardfrenzy.databinding.ActivityDeckListBinding
import com.android.flashcardfrenzy.network.RetrofitClient
import com.android.flashcardfrenzy.quiz.QuizHistoryActivity

/**
 * MVP — View
 * Main screen: shows all decks. Supports search.
 * FAB to create a deck (only visible when logged in).
 *
 * Layout IDs expected in activity_deck_list.xml:
 *   - rv_decks      : RecyclerView
 *   - fab_add_deck  : FloatingActionButton
 *   - progress_bar  : ProgressBar
 *   - tv_empty      : TextView  (shown when list is empty)
 */
class DeckListActivity : AppCompatActivity(), DeckContract.ListView {

    private lateinit var binding: ActivityDeckListBinding
    private lateinit var presenter: DeckPresenter
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: DeckAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeckListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        setupRecyclerView()
        setupPresenter()
        setupFab()

        presenter.loadAllDecks()
    }

    private fun setupRecyclerView() {
        adapter = DeckAdapter { deck ->
            val intent = Intent(this, DeckDetailActivity::class.java)
            intent.putExtra(DeckDetailActivity.EXTRA_DECK_ID, deck.id)
            startActivity(intent)
        }
        binding.rvDecks.layoutManager = LinearLayoutManager(this)
        binding.rvDecks.adapter = adapter
    }

    private fun setupPresenter() {
        val apiService = RetrofitClient.getApiService(tokenManager)
        presenter = DeckPresenter(
            listView = this,
            repository = DeckRepository(apiService)
        )
    }

    private fun setupFab() {
        if (tokenManager.isLoggedIn()) {
            binding.fabAddDeck.visibility = View.VISIBLE
            binding.fabAddDeck.setOnClickListener {
                startActivity(Intent(this, CreateEditDeckActivity::class.java))
            }
        } else {
            binding.fabAddDeck.visibility = View.GONE
        }
    }

    // ── Options menu: search, my decks, quiz history, admin, logout ───────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_deck_list, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter.loadAllDecks(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) presenter.loadAllDecks()
                return true
            }
        })

        menu.findItem(R.id.action_my_decks)?.isVisible   = tokenManager.isLoggedIn()
        menu.findItem(R.id.action_history)?.isVisible     = tokenManager.isLoggedIn()
        menu.findItem(R.id.action_admin)?.isVisible       = tokenManager.isAdmin()
        menu.findItem(R.id.action_logout)?.isVisible      = tokenManager.isLoggedIn()
        menu.findItem(R.id.action_login)?.isVisible       = !tokenManager.isLoggedIn()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_my_decks -> {
                val intent = Intent(this, DeckListActivity::class.java)
                intent.putExtra(EXTRA_MY_DECKS, true)
                startActivity(intent)
                true
            }
            R.id.action_history -> {
                startActivity(Intent(this, QuizHistoryActivity::class.java))
                true
            }
            R.id.action_admin -> {
                startActivity(Intent(this, AdminActivity::class.java))
                true
            }
            R.id.action_logout -> {
                tokenManager.clearAll()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            R.id.action_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        val isMyDecks = intent.getBooleanExtra(EXTRA_MY_DECKS, false)
        if (isMyDecks) presenter.loadMyDecks() else presenter.loadAllDecks()
    }

    // ── DeckContract.ListView ─────────────────────────────────────────────────

    override fun showLoading() { binding.progressBar.visibility = View.VISIBLE }
    override fun hideLoading() { binding.progressBar.visibility = View.GONE }

    override fun onDecksLoaded(decks: List<DeckDtos.DeckResponse>) {
        adapter.updateData(decks)
        binding.tvEmpty.visibility = if (decks.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    companion object {
        const val EXTRA_MY_DECKS = "extra_my_decks"
    }
}
