package com.mardous.booming.ai.services

import android.util.Log
import com.mardous.booming.ai.AiPreferences
import com.mardous.booming.ai.GeminiClient
import com.mardous.booming.data.model.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Service for enriching and correcting song metadata using Gemini
 */
class MetadataEnricherService(
    private val aiPreferences: AiPreferences,
    private val geminiClient: GeminiClient
) {

    @Serializable
    data class MetadataCorrection(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val genre: String? = null,
        val year: Int? = null
    )

    /**
     * Suggest metadata corrections for a song
     */
    suspend fun suggestCorrections(song: Song): Result<MetadataCorrection> {
        if (!aiPreferences.isConfigured()) {
            return Result.failure(Exception("AI not configured"))
        }

        val prompt = """
            Analyze this song metadata and suggest corrections/improvements:
            Current Title: ${song.title}
            Current Artist: ${song.artistName}
            Current Album: ${song.albumName}
            Duration: ${song.duration}ms
            
            If the metadata appears correct and standard, return it as-is.
            If there are obvious errors or inconsistencies, suggest corrections.
            Also identify the genre if not provided.
            
            Return a JSON object with ONLY these fields (use null for unchanged):
            {
              "title": "corrected title or null",
              "artist": "corrected artist or null",
              "album": "corrected album or null",
              "genre": "detected genre or null",
              "year": "year as number or null"
            }
        """.trimIndent()

        return try {
            val response = geminiClient.generateContent(prompt)
            
            response.onSuccess { content ->
                try {
                    // Extract JSON from response
                    val jsonStr = extractJson(content)
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    val correction = json.decodeFromString<MetadataCorrection>(jsonStr)
                    Result.success(correction)
                } catch (e: Exception) {
                    Log.e("MetadataEnricher", "Failed to parse metadata suggestions", e)
                    Result.failure(e)
                }
            }.getOrElse {
                Result.failure(it)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search for missing metadata (artist, album, year, genre)
     */
    suspend fun enrichMetadata(song: Song): Result<MetadataCorrection> {
        if (!aiPreferences.isConfigured()) {
            return Result.failure(Exception("AI not configured"))
        }

        val prompt = """
            Based on the song title and artist, provide complete metadata:
            Title: ${song.title}
            Artist: ${song.artistName}
            
            Provide accurate information about this song if you know it.
            If uncertain, provide reasonable guesses based on common songs.
            
            Return JSON format:
            {
              "album": "album name",
              "artist": "confirm or correct artist",
              "genre": "genre",
              "year": "release year as number"
            }
        """.trimIndent()

        return try {
            val response = geminiClient.generateContent(prompt)
            
            response.onSuccess { content ->
                try {
                    val jsonStr = extractJson(content)
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    val correction = json.decodeFromString<MetadataCorrection>(jsonStr)
                    Result.success(correction)
                } catch (e: Exception) {
                    Log.e("MetadataEnricher", "Failed to parse enriched metadata", e)
                    Result.failure(e)
                }
            }.getOrElse {
                Result.failure(it)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract JSON from Gemini response (which may contain markdown or extra text)
     */
    private fun extractJson(content: String): String {
        val jsonMatch = Regex("""\{[\s\S]*\}""").find(content)
        return jsonMatch?.value ?: content
    }
}
