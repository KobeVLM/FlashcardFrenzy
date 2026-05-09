package com.android.flashcardfrenzy.network

import com.android.flashcardfrenzy.common.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that automatically adds the Authorization header
 * to every outgoing request if the user is logged in.
 *
 * This means no Presenter or Repository needs to manually pass the token.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenManager.getBearerToken()

        // Only attach if we have a token AND the request doesn't already have one
        val request = if (token != null && original.header("Authorization") == null) {
            original.newBuilder()
                .header("Authorization", token)
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
