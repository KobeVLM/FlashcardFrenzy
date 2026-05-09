package com.android.flashcardfrenzy.deck

import com.android.flashcardfrenzy.common.ApiResponse
import com.android.flashcardfrenzy.network.ApiService
import retrofit2.Response

/**
 * MVP — Model
 * Handles all deck-related API calls. Called only by DeckPresenter.
 */
class DeckRepository(private val apiService: ApiService) {

    suspend fun getAllDecks(search: String?) = apiService.getAllDecks(search)
    suspend fun getMyDecks()                 = apiService.getMyDecks()
    suspend fun getDeckById(id: Long)        = apiService.getDeckById(id)

    suspend fun createDeck(request: DeckDtos.DeckRequest) =
        apiService.createDeck(request)

    suspend fun updateDeck(id: Long, request: DeckDtos.DeckRequest) =
        apiService.updateDeck(id, request)

    suspend fun deleteDeck(id: Long) = apiService.deleteDeck(id)
}
