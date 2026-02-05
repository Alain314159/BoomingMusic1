package com.mardous.booming.ai.services

import android.util.Log
import com.mardous.booming.ai.AiPreferences
import com.mardous.booming.ai.GeminiClient
import com.mardous.booming.data.model.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Service for generating AI-curated daily albums based on listening history
 */
class AiAlbumGeneratorService(
    private val aiPreferences: AiPreferences,
    private val geminiClient: GeminiClient
) {

    @Serializable
    data class GeneratedAlbum(
        val title: String,
        val description: String,
        val theme: String,
        val selectedTrackIndices: List<Int>,
        val suggestedGenres: List<String> = emptyList(),
        val coverPrompt: String = ""
    )

    /**
     * Generate an AI-curated album from recent listening history
     * @param recentSongs Last 100 songs listened to with play counts
     * @param targetTrackCount How many tracks for the album
     * @return Generated album metadata
     */
    suspend fun generateDaily(
        recentSongs: List<Song>,
        playCounts: Map<Long, Int> = emptyMap(),
        targetTrackCount: Int = 10
    ): Result<GeneratedAlbum> {
        if (!aiPreferences.isConfigured()) {
            return Result.failure(Exception("AI not configured"))
        }

        if (recentSongs.isEmpty()) {
            return Result.failure(Exception("No recent songs to curate from"))
        }

        // Build song summary for the prompt (without reproducing content)
        val songSummary = recentSongs.take(100).mapIndexed { index, song ->
            val playCount = playCounts[song.id] ?: 1
            "[$index] ${song.title} by ${song.artistName} (Album: ${song.albumName}, Plays: $playCount, Duration: ${song.duration}ms)"
        }.joinToString("\n")

        val prompt = """
            Create a daily AI-curated album from this listening history.
            
            Recent songs listened to (with play counts):
            $songSummary
            
            Task:
            1. Analyze the listening patterns and music taste
            2. Select $targetTrackCount tracks that form a cohesive album
            3. Create a theme/narrative for the album
            4. Propose an album title and 2-3 sentence description
            5. Determine the primary genres
            6. Return results in JSON format
            
            Format the response as JSON with these exact fields:
            {
              "title": "Album title",
              "description": "2-3 sentences about the album",
              "theme": "Single theme/concept",
              "selectedTrackIndices": [0, 5, 12, 23, ...],
              "suggestedGenres": ["genre1", "genre2", "genre3"],
              "coverPrompt": "Text description for cover art (artistic style, mood, colors)"
            }
            
            NOTE: selectedTrackIndices are the array indices from the song list above.
            Ensure the indices are valid and the count equals targetTrackCount.
        """.trimIndent()

        return try {
            val response = geminiClient.generateContent(prompt)
            
            response.onSuccess { content ->
                try {
                    val jsonStr = extractJson(content)
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    val album = json.decodeFromString<GeneratedAlbum>(jsonStr)
                    
                    // Validate indices
                    val validIndices = album.selectedTrackIndices.all { it in recentSongs.indices }
                    if (!validIndices) {
                        return@onSuccess Result.failure(Exception("Invalid track indices in generated album"))
                    }
                    
                    Result.success(album)
                } catch (e: Exception) {
                    Log.e("AlbumGenerator", "Failed to parse generated album", e)
                    Result.failure(e)
                }
            }.getOrElse {
                Result.failure(it)
            }
        } catch (e: Exception) {
            Log.e("AlbumGenerator", "Error generating album", e)
            Result.failure(e)
        }
    }

    /**
     * Generate a themed playlist based on mood/genre
     */
    suspend fun generateThemed(
        recentSongs: List<Song>,
        theme: String,
        targetTrackCount: Int = 15
    ): Result<GeneratedAlbum> {
        if (!aiPreferences.isConfigured()) {
            return Result.failure(Exception("AI not configured"))
        }

        val songsSummary = recentSongs.take(100).mapIndexed { index, song ->
            val genres = "${song.albumName}" // Using album as proxy for style
            "[$index] ${song.title} - ${song.artistName} ($genres)"
        }.joinToString("\n")

        val prompt = """
            Create a themed playlist from this music library with theme: "$theme"
            
            Available songs:
            $songsSummary
            
            Select $targetTrackCount songs that best match the theme "$theme".
            Create a cohesive experience that flows well.
            
            Return JSON:
            {
              "title": "Playlist title incorporating theme",
              "description": "Why these songs match the theme",
              "theme": "$theme",
              "selectedTrackIndices": [indices],
              "suggestedGenres": ["genres"],
              "coverPrompt": "Visual description for playlist cover"
            }
        """.trimIndent()

        return try {
            val response = geminiClient.generateContent(prompt)
            response.onSuccess { content ->
                try {
                    val jsonStr = extractJson(content)
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    val playlist = json.decodeFromString<GeneratedAlbum>(jsonStr)
                    Result.success(playlist)
                } catch (e: Exception) {
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
     * Extract JSON from response (handles markdown formatting)
     */
    private fun extractJson(content: String): String {
        val jsonMatch = Regex("""\{[\s\S]*\}""").find(content)
        return jsonMatch?.value ?: content
    }
}
