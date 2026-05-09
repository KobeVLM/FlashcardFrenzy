package com.flashcardfrenzy.network

import com.flashcardfrenzy.auth.AuthDtos
import com.flashcardfrenzy.common.ApiResponse
import com.flashcardfrenzy.deck.DeckDtos
import com.flashcardfrenzy.flashcard.FlashcardDtos
import com.flashcardfrenzy.quiz.QuizDtos
import com.flashcardfrenzy.admin.AdminDtos
import retrofit2.Response
import retrofit2.http.*

/**
 * All backend endpoints from the SDD, mapped to Retrofit suspend functions.
 * Authorization header is added automatically by AuthInterceptor.
 */
interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(
        @Body request: AuthDtos.RegisterRequest
    ): Response<ApiResponse<AuthDtos.AuthResponse>>

    @POST("auth/login")
    suspend fun login(
        @Body request: AuthDtos.LoginRequest
    ): Response<ApiResponse<AuthDtos.AuthResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<String>>

    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: AuthDtos.RefreshRequest
    ): Response<ApiResponse<AuthDtos.RefreshResponse>>

    @GET("auth/me")
    suspend fun getProfile(): Response<ApiResponse<AuthDtos.UserResponse>>

    // ── Decks ─────────────────────────────────────────────────────────────────

    @GET("decks")
    suspend fun getAllDecks(
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<DeckDtos.DeckResponse>>>

    @GET("decks/my")
    suspend fun getMyDecks(): Response<ApiResponse<List<DeckDtos.DeckResponse>>>

    @GET("decks/{id}")
    suspend fun getDeckById(
        @Path("id") id: Long
    ): Response<ApiResponse<DeckDtos.DeckResponse>>

    @POST("decks")
    suspend fun createDeck(
        @Body request: DeckDtos.DeckRequest
    ): Response<ApiResponse<DeckDtos.DeckResponse>>

    @PUT("decks/{id}")
    suspend fun updateDeck(
        @Path("id") id: Long,
        @Body request: DeckDtos.DeckRequest
    ): Response<ApiResponse<DeckDtos.DeckResponse>>

    @DELETE("decks/{id}")
    suspend fun deleteDeck(
        @Path("id") id: Long
    ): Response<ApiResponse<String>>

    // ── Flashcards ────────────────────────────────────────────────────────────

    @GET("decks/{id}/cards")
    suspend fun getCardsByDeck(
        @Path("id") deckId: Long
    ): Response<ApiResponse<List<FlashcardDtos.FlashcardResponse>>>

    @GET("cards/{cardId}")
    suspend fun getCardById(
        @Path("cardId") cardId: Long
    ): Response<ApiResponse<FlashcardDtos.FlashcardResponse>>

    @POST("decks/{id}/cards")
    suspend fun createCard(
        @Path("id") deckId: Long,
        @Body request: FlashcardDtos.FlashcardRequest
    ): Response<ApiResponse<FlashcardDtos.FlashcardResponse>>

    @PUT("cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: Long,
        @Body request: FlashcardDtos.FlashcardRequest
    ): Response<ApiResponse<FlashcardDtos.FlashcardResponse>>

    @DELETE("cards/{cardId}")
    suspend fun deleteCard(
        @Path("cardId") cardId: Long
    ): Response<ApiResponse<String>>

    // ── Quiz ──────────────────────────────────────────────────────────────────

    @POST("quizzes/results")
    suspend fun submitResult(
        @Body request: QuizDtos.QuizResultRequest
    ): Response<ApiResponse<QuizDtos.QuizResultResponse>>

    @GET("quizzes/history")
    suspend fun getQuizHistory(): Response<ApiResponse<List<QuizDtos.QuizResultResponse>>>

    // ── Admin ─────────────────────────────────────────────────────────────────

    @GET("admin/stats")
    suspend fun getAdminStats(): Response<ApiResponse<AdminDtos.StatsResponse>>

    @GET("admin/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<AdminDtos.AdminUserResponse>>>

    @DELETE("admin/users/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Long
    ): Response<ApiResponse<String>>
}
