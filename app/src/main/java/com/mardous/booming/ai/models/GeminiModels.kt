package com.mardous.booming.ai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    @SerialName("generationConfig")
    val generationConfig: GenerationConfig = GenerationConfig()
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    @SerialName("maxOutputTokens")
    val maxOutputTokens: Int = 2048,
    val temperature: Float = 0.7f,
    @SerialName("topP")
    val topP: Float = 0.95f,
    @SerialName("topK")
    val topK: Int = 40
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate> = emptyList(),
    @SerialName("usageMetadata")
    val usageMetadata: UsageMetadata? = null,
    val error: ErrorInfo? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    @SerialName("finishReason")
    val finishReason: String = "",
    @SerialName("safetyRatings")
    val safetyRatings: List<SafetyRating> = emptyList()
)

@Serializable
data class UsageMetadata(
    @SerialName("promptTokenCount")
    val promptTokenCount: Int = 0,
    @SerialName("candidatesTokenCount")
    val candidatesTokenCount: Int = 0,
    @SerialName("totalTokenCount")
    val totalTokenCount: Int = 0
)

@Serializable
data class SafetyRating(
    val category: String = "",
    val probability: String = ""
)

@Serializable
data class ErrorInfo(
    val code: Int = 0,
    val message: String = "",
    val status: String = ""
)
