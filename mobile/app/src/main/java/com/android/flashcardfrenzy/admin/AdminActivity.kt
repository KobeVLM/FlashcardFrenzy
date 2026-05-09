package com.android.flashcardfrenzy.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.flashcardfrenzy.common.TokenManager
import com.android.flashcardfrenzy.databinding.ActivityAdminBinding
import com.android.flashcardfrenzy.network.RetrofitClient

/**
 * MVP — View
 * Admin panel: shows platform stats and full user list with delete capability.
 * Only accessible to users with ROLE_ADMIN (enforced by backend).
 *
 * Layout IDs expected in activity_admin.xml:
 *   - tv_total_users    : TextView
 *   - tv_total_decks    : TextView
 *   - tv_total_cards    : TextView
 *   - tv_total_quizzes  : TextView
 *   - rv_users          : RecyclerView
 *   - progress_bar      : ProgressBar
 */
class AdminActivity : AppCompatActivity(), AdminContract.View {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var presenter: AdminPresenter
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Admin Panel"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tokenManager = TokenManager.getInstance(this)
        val apiService   = RetrofitClient.getApiService(tokenManager)

        adapter = AdminUserAdapter(
            onDeleteClick = { user ->
                AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Delete ${user.fullName} (${user.email})? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> presenter.deleteUser(user.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        presenter = AdminPresenter(
            view       = this,
            repository = AdminRepository(apiService)
        )

        presenter.loadStats()
        presenter.loadUsers()
    }

    // ── AdminContract.View ────────────────────────────────────────────────────

    override fun showLoading() { binding.progressBar.visibility = View.VISIBLE }
    override fun hideLoading() { binding.progressBar.visibility = View.GONE }

    override fun onStatsLoaded(stats: AdminDtos.StatsResponse) {
        binding.tvTotalUsers.text   = "Users: ${stats.totalUsers}"
        binding.tvTotalDecks.text   = "Decks: ${stats.totalDecks}"
        binding.tvTotalCards.text   = "Cards: ${stats.totalFlashcards}"
        binding.tvTotalQuizzes.text = "Quizzes: ${stats.totalQuizResults}"
    }

    override fun onUsersLoaded(users: List<AdminDtos.AdminUserResponse>) {
        adapter.updateData(users)
    }

    override fun onUserDeleted(userId: Long) {
        adapter.removeUser(userId)
        Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show()
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
