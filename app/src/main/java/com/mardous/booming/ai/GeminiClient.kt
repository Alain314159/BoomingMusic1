package com.mardous.booming.ai

import android.util.Log
import com.mardous.booming.ai.models.GeminiRequest
import com.mardous.booming.ai.models.GeminiResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit interface for Gemini API
 */
interface GeminiApi {
    @POST("v1/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: GeminiRequest
    ): GeminiResponse
}

/**
 * Client for interacting with Gemini API
 */
class GeminiClient(private val aiPreferences: AiPreferences) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val api = retrofit.create(GeminiApi::class.java)

    suspend fun generateContent(prompt: String): Result<String> {
        return try {
            val apiKey = aiPreferences.geminiApiKey
            if (apiKey.isNullOrEmpty()) {
                return Result.failure(Exception("Gemini API key not configured"))
            }

            val request = GeminiRequest(
                contents = listOf(
                    com.mardous.booming.ai.models.Content(
                        parts = listOf(
                            com.mardous.booming.ai.models.Part(text = prompt)
                        )
                    )
                )
            )

            val response = api.generateContent(apiKey, request = request)

            if (response.error != null) {
                return Result.failure(Exception("Gemini API Error: ${response.error.message}"))
            }

            val text = response.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: return Result.failure(Exception("Empty response from Gemini API"))

            // Increment request count
            aiPreferences.requestCount += 1

            Result.success(text)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error calling Gemini API", e)
            Result.failure(e)
        }
    }
}
