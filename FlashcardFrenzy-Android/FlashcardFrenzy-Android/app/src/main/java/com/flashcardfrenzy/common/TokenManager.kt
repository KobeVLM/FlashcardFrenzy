package com.flashcardfrenzy.common

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores and retrieves JWT tokens and user info using SharedPreferences.
 * Use as a singleton via TokenManager.getInstance(context).
 *
 * Stores:
 *  - Access token  (24h)
 *  - Refresh token (7d)
 *  - User email, full name, role
 */
class TokenManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Token access ──────────────────────────────────────────────────────────

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getBearerToken(): String? = getAccessToken()?.let { "Bearer $it" }

    fun updateAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }

    // ── User info ─────────────────────────────────────────────────────────────

    fun saveUserInfo(email: String, fullName: String, role: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getEmail(): String?    = prefs.getString(KEY_EMAIL, null)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    fun getRole(): String?     = prefs.getString(KEY_ROLE, null)

    fun isAdmin(): Boolean = getRole() == "ADMIN"

    // ── Session state ─────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    companion object {
        private const val PREFS_NAME       = "flashcard_frenzy_prefs"
        private const val KEY_ACCESS_TOKEN  = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EMAIL         = "email"
        private const val KEY_FULL_NAME     = "full_name"
        private const val KEY_ROLE          = "role"

        @Volatile private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager =
            instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
    }
}
