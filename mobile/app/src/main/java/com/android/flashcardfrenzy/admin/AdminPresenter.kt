package com.android.flashcardfrenzy.admin

import kotlinx.coroutines.*

/**
 * MVP — Presenter
 * Handles stats loading, user listing, and user deletion for the admin panel.
 */
class AdminPresenter(
    private val view: AdminContract.View,
    private val repository: AdminRepository
) : AdminContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun loadStats() {
        view.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.getStats() }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { view.onStatsLoaded(it) }
                } else {
                    view.onError(response.body()?.error?.message ?: "Failed to load stats")
                }
            } catch (e: Exception) {
                view.onError("Network error: ${e.message}")
            } finally {
                view.hideLoading()
            }
        }
    }

    override fun loadUsers() {
        view.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.getAllUsers() }
                if (response.isSuccessful && response.body()?.success == true) {
                    view.onUsersLoaded(response.body()?.data ?: emptyList())
                } else {
                    view.onError(response.body()?.error?.message ?: "Failed to load users")
                }
            } catch (e: Exception) {
                view.onError("Network error: ${e.message}")
            } finally {
                view.hideLoading()
            }
        }
    }

    override fun deleteUser(userId: Long) {
        view.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.deleteUser(userId) }
                if (response.isSuccessful && response.body()?.success == true) {
                    view.onUserDeleted(userId)
                } else {
                    view.onError(response.body()?.error?.message ?: "Failed to delete user")
                }
            } catch (e: Exception) {
                view.onError("Network error: ${e.message}")
            } finally {
                view.hideLoading()
            }
        }
    }

    override fun onDestroy() = scope.cancel()
}
