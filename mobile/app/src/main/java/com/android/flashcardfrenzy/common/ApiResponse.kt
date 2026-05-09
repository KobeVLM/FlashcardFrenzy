package com.android.flashcardfrenzy.common

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the backend SDD response envelope exactly:
 * { success, data, error, timestamp }
 *
 * Used as the generic wrapper for every Retrofit call:
 *   Response<ApiResponse<DeckResponse>>
 */
data class ApiResponse<T>(
    @SerializedName("success")   val success: Boolean,
    @SerializedName("data")      val data: T?,
    @SerializedName("error")     val error: ApiError?,
    @SerializedName("timestamp") val timestamp: String?
)

data class ApiError(
    @SerializedName("code")    val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: Any?
)
