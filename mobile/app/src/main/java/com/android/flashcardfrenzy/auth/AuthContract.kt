package com.android.flashcardfrenzy.auth

/**
 * MVP Contract for the Auth feature.
 * Defines what the View can display and what the Presenter can do.
 */
interface AuthContract {

    // ── Login View ────────────────────────────────────────────────────────────

    interface LoginView {
        fun showLoading()
        fun hideLoading()
        fun onLoginSuccess(response: AuthDtos.AuthResponse)
        fun onError(message: String)
    }

    // ── Register View ─────────────────────────────────────────────────────────

    interface RegisterView {
        fun showLoading()
        fun hideLoading()
        fun onRegisterSuccess(response: AuthDtos.AuthResponse)
        fun onError(message: String)
    }

    // ── Presenter ─────────────────────────────────────────────────────────────

    interface Presenter {
        fun login(email: String, password: String)
        fun register(fullName: String, email: String, password: String)
        fun logout()
        fun refreshToken(refreshToken: String)
        fun onDestroy()
    }
}
