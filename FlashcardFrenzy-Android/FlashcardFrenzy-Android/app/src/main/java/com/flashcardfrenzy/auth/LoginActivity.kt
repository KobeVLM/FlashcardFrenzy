package com.flashcardfrenzy.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flashcardfrenzy.common.TokenManager
import com.flashcardfrenzy.databinding.ActivityLoginBinding
import com.flashcardfrenzy.deck.DeckListActivity
import com.flashcardfrenzy.network.RetrofitClient

/**
 * MVP — View
 * Handles login UI. Delegates all logic to AuthPresenter.
 *
 * Layout IDs expected in activity_login.xml:
 *   - et_email      : EditText
 *   - et_password   : EditText
 *   - btn_login     : Button
 *   - tv_register   : TextView  (tap to go to RegisterActivity)
 *   - progress_bar  : ProgressBar
 */
class LoginActivity : AppCompatActivity(), AuthContract.LoginView {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var presenter: AuthPresenter
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)

        // If already logged in, skip to deck list
        if (tokenManager.isLoggedIn()) {
            goToDeckList()
            return
        }

        setupPresenter()
        setupClickListeners()
    }

    private fun setupPresenter() {
        val apiService = RetrofitClient.getApiService(tokenManager)
        val repository = AuthRepository(apiService)
        presenter = AuthPresenter(
            loginView = this,
            repository = repository,
            tokenManager = tokenManager
        )
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            presenter.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // ── AuthContract.LoginView ────────────────────────────────────────────────

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }

    override fun onLoginSuccess(response: AuthDtos.AuthResponse) {
        goToDeckList()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun goToDeckList() {
        startActivity(Intent(this, DeckListActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
