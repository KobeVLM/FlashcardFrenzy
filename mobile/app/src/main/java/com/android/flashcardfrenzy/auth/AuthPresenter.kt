package com.android.flashcardfrenzy.auth

import com.android.flashcardfrenzy.common.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MVP — Presenter
 * Coordinates between AuthRepository (Model) and the Login/Register Views.
 * Runs API calls on IO dispatcher, updates View on Main.
 *
 * Call onDestroy() in Activity.onDestroy() to cancel coroutines and avoid leaks.
 */
class AuthPresenter(
    private val loginView: AuthContract.LoginView? = null,
    private val registerView: AuthContract.RegisterView? = null,
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : AuthContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // ── Login ─────────────────────────────────────────────────────────────────

    override fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return

        loginView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.login(AuthDtos.LoginRequest(email, password))
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                    tokenManager.saveUserInfo(data.email, data.fullName, data.role)
                    loginView?.onLoginSuccess(data)
                } else {
                    val message = response.body()?.error?.message ?: "Login failed"
                    loginView?.onError(message)
                }
            } catch (e: Exception) {
                loginView?.onError("Network error: ${e.message}")
            } finally {
                loginView?.hideLoading()
            }
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    override fun register(fullName: String, email: String, password: String) {
        if (!validateRegisterInput(fullName, email, password)) return

        registerView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.register(AuthDtos.RegisterRequest(fullName, email, password))
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                    tokenManager.saveUserInfo(data.email, data.fullName, data.role)
                    registerView?.onRegisterSuccess(data)
                } else {
                    val message = response.body()?.error?.message ?: "Registration failed"
                    registerView?.onError(message)
                }
            } catch (e: Exception) {
                registerView?.onError("Network error: ${e.message}")
            } finally {
                registerView?.hideLoading()
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    override fun logout() {
        scope.launch {
            try {
                withContext(Dispatchers.IO) { repository.logout() }
            } catch (_: Exception) { }
            finally {
                tokenManager.clearAll()
            }
        }
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    override fun refreshToken(refreshToken: String) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.refresh(AuthDtos.RefreshRequest(refreshToken))
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { tokenManager.updateAccessToken(it.accessToken) }
                } else {
                    // Refresh failed — force logout
                    tokenManager.clearAll()
                }
            } catch (e: Exception) {
                tokenManager.clearAll()
            }
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isBlank()) { loginView?.onError("Email is required"); return false }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginView?.onError("Enter a valid email"); return false
        }
        if (password.isBlank()) { loginView?.onError("Password is required"); return false }
        return true
    }

    private fun validateRegisterInput(fullName: String, email: String, password: String): Boolean {
        if (fullName.isBlank()) { registerView?.onError("Full name is required"); return false }
        if (email.isBlank()) { registerView?.onError("Email is required"); return false }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerView?.onError("Enter a valid email"); return false
        }
        if (password.length < 8) {
            registerView?.onError("Password must be at least 8 characters"); return false
        }
        return true
    }

    override fun onDestroy() = scope.cancel()
}
