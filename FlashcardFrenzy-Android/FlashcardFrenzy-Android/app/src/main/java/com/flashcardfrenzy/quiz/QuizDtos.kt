package com.flashcardfrenzy.quiz

import com.google.gson.annotations.SerializedName

object QuizDtos {

    data class QuizResultRequest(
        @SerializedName("deckId")    val deckId: Long,
        @SerializedName("score")     val score: Int,
        @SerializedName("timeSpent") val timeSpent: Int
    )

    data class QuizResultResponse(
        @SerializedName("id")        val id: Long,
        @SerializedName("deckId")    val deckId: Long,
        @SerializedName("deckTitle") val deckTitle: String,
        @SerializedName("score")     val score: Int,
        @SerializedName("timeSpent") val timeSpent: Int,
        @SerializedName("createdAt") val createdAt: String
    )
}
