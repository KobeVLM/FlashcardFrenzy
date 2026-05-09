package com.flashcardfrenzy.flashcard

import com.google.gson.annotations.SerializedName

object FlashcardDtos {

    data class FlashcardRequest(
        @SerializedName("question") val question: String,
        @SerializedName("answer")   val answer: String,
        @SerializedName("tags")     val tags: String?
    )

    data class FlashcardResponse(
        @SerializedName("id")        val id: Long,
        @SerializedName("deckId")    val deckId: Long,
        @SerializedName("question")  val question: String,
        @SerializedName("answer")    val answer: String,
        @SerializedName("tags")      val tags: String?,
        @SerializedName("createdAt") val createdAt: String
    )
}
