package com.android.flashcardfrenzy.auth

import com.google.gson.annotations.SerializedName

object AuthDtos {

    // ── Requests ─────────────────────────────────────────────────────────────

    data class RegisterRequest(
        @SerializedName("fullName") val fullName: String,
        @SerializedName("email")    val email: String,
        @SerializedName("password") val password: String
    )

    data class LoginRequest(
        @SerializedName("email")    val email: String,
        @SerializedName("password") val password: String
    )

    data class RefreshRequest(
        @SerializedName("refreshToken") val refreshToken: String
    )

    // ── Responses ─────────────────────────────────────────────────────────────

    data class AuthResponse(
        @SerializedName("accessToken")  val accessToken: String,
        @SerializedName("refreshToken") val refreshToken: String,
        @SerializedName("email")        val email: String,
        @SerializedName("fullName")     val fullName: String,
        @SerializedName("role")         val role: String
    )

    data class RefreshResponse(
        @SerializedName("accessToken") val accessToken: String
    )

    data class UserResponse(
        @SerializedName("id")        val id: Long,
        @SerializedName("email")     val email: String,
        @SerializedName("fullName")  val fullName: String,
        @SerializedName("role")      val role: String,
        @SerializedName("createdAt") val createdAt: String
    )
}
