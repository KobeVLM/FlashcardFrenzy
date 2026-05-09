package com.flashcardfrenzy.deck

import com.google.gson.annotations.SerializedName

object DeckDtos {

    data class DeckRequest(
        @SerializedName("title")       val title: String,
        @SerializedName("category")    val category: String?,
        @SerializedName("description") val description: String?
    )

    data class DeckResponse(
        @SerializedName("id")          val id: Long,
        @SerializedName("title")       val title: String,
        @SerializedName("category")    val category: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("userId")      val userId: Long,
        @SerializedName("ownerName")   val ownerName: String,
        @SerializedName("createdAt")   val createdAt: String
    )
}
