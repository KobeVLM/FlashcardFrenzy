package com.android.flashcardfrenzy.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.flashcardfrenzy.common.TokenManager
import com.android.flashcardfrenzy.databinding.ActivityRegisterBinding
import com.android.flashcardfrenzy.deck.DeckListActivity
import com.android.flashcardfrenzy.network.RetrofitClient

/**
 * MVP — View
 * Handles registration UI. Delegates all logic to AuthPresenter.
 *
 * Layout IDs expected in activity_register.xml:
 *   - et_full_name  : EditText
 *   - et_email      : EditText
 *   - et_password   : EditText
 *   - btn_register  : Button
 *   - tv_login      : TextView  (tap to go back to LoginActivity)
 *   - progress_bar  : ProgressBar
 */
class RegisterActivity : AppCompatActivity(), AuthContract.RegisterView {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var presenter: AuthPresenter
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        setupPresenter()
        setupClickListeners()
    }

    private fun setupPresenter() {
        val apiService = RetrofitClient.getApiService(tokenManager)
        val repository = AuthRepository(apiService)
        presenter = AuthPresenter(
            registerView = this,
            repository = repository,
            tokenManager = tokenManager
        )
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            presenter.register(fullName, email, password)
        }

        binding.tvLogin.setOnClickListener { finish() }
    }

    // ── AuthContract.RegisterView ─────────────────────────────────────────────

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
    }

    override fun onRegisterSuccess(response: AuthDtos.AuthResponse) {
        startActivity(Intent(this, DeckListActivity::class.java))
        finishAffinity()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
