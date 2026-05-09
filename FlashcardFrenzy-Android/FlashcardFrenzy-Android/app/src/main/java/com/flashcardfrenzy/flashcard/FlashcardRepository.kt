package com.flashcardfrenzy.flashcard

import com.flashcardfrenzy.network.ApiService

class FlashcardRepository(private val apiService: ApiService) {
    suspend fun getCardsByDeck(deckId: Long) = apiService.getCardsByDeck(deckId)
    suspend fun getCardById(cardId: Long)     = apiService.getCardById(cardId)
    suspend fun createCard(deckId: Long, request: FlashcardDtos.FlashcardRequest) =
        apiService.createCard(deckId, request)
    suspend fun updateCard(cardId: Long, request: FlashcardDtos.FlashcardRequest) =
        apiService.updateCard(cardId, request)
    suspend fun deleteCard(cardId: Long)      = apiService.deleteCard(cardId)
}
