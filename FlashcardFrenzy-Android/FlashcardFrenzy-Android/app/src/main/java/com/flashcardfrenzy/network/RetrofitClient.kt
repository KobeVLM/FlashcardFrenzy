package com.flashcardfrenzy.network

import com.flashcardfrenzy.BuildConfig
import com.flashcardfrenzy.common.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client.
 * Base URL is set in app/build.gradle as a BuildConfig field.
 *
 * To switch environments, just change BASE_URL in build.gradle.
 */
object RetrofitClient {

    private var retrofit: Retrofit? = null

    fun getInstance(tokenManager: TokenManager): Retrofit {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            }

            val httpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getApiService(tokenManager: TokenManager): ApiService =
        getInstance(tokenManager).create(ApiService::class.java)
}
