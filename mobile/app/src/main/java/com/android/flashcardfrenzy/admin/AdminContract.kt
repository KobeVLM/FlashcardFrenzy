package com.android.flashcardfrenzy.admin

interface AdminContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun onStatsLoaded(stats: AdminDtos.StatsResponse)
        fun onUsersLoaded(users: List<AdminDtos.AdminUserResponse>)
        fun onUserDeleted(userId: Long)
        fun onError(message: String)
    }

    interface Presenter {
        fun loadStats()
        fun loadUsers()
        fun deleteUser(userId: Long)
        fun onDestroy()
    }
}
