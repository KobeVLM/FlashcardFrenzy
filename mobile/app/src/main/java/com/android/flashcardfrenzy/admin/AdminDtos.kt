package com.android.flashcardfrenzy.admin

import com.google.gson.annotations.SerializedName

object AdminDtos {

    data class StatsResponse(
        @SerializedName("totalUsers")       val totalUsers: Long,
        @SerializedName("totalDecks")       val totalDecks: Long,
        @SerializedName("totalFlashcards")  val totalFlashcards: Long,
        @SerializedName("totalQuizResults") val totalQuizResults: Long
    )

    data class AdminUserResponse(
        @SerializedName("id")        val id: Long,
        @SerializedName("email")     val email: String,
        @SerializedName("fullName")  val fullName: String,
        @SerializedName("role")      val role: String,
        @SerializedName("createdAt") val createdAt: String
    )
}
