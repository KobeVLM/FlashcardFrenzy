package com.android.flashcardfrenzy.auth

import com.android.flashcardfrenzy.common.ApiResponse
import com.android.flashcardfrenzy.network.ApiService
import retrofit2.Response

/**
 * MVP — Model
 * Handles all auth-related API calls. Called only by AuthPresenter.
 */
class AuthRepository(private val apiService: ApiService) {

    suspend fun login(request: AuthDtos.LoginRequest): Response<ApiResponse<AuthDtos.AuthResponse>> =
        apiService.login(request)

    suspend fun register(request: AuthDtos.RegisterRequest): Response<ApiResponse<AuthDtos.AuthResponse>> =
        apiService.register(request)

    suspend fun logout(): Response<ApiResponse<String>> =
        apiService.logout()

    suspend fun refresh(request: AuthDtos.RefreshRequest): Response<ApiResponse<AuthDtos.RefreshResponse>> =
        apiService.refresh(request)

    suspend fun getProfile(): Response<ApiResponse<AuthDtos.UserResponse>> =
        apiService.getProfile()
}
