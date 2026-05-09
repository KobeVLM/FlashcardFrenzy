package com.android.flashcardfrenzy.admin

import com.android.flashcardfrenzy.network.ApiService

class AdminRepository(private val apiService: ApiService) {
    suspend fun getStats()               = apiService.getAdminStats()
    suspend fun getAllUsers()             = apiService.getAllUsers()
    suspend fun deleteUser(userId: Long) = apiService.deleteUser(userId)
}
