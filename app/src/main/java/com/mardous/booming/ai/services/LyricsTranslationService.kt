package com.mardous.booming.ai.services

import android.util.Log
import com.mardous.booming.ai.AiPreferences
import com.mardous.booming.ai.GeminiClient
import com.mardous.booming.data.model.Song

/**
 * Service for translating song lyrics and synchronizing with LRC timestamps
 */
class LyricsTranslationService(
    private val aiPreferences: AiPreferences,
    private val geminiClient: GeminiClient
) {

    /**
     * Translate lyrics to target language while preserving LRC timestamps
     * @param currentLyrics LRC format lyrics (with [MM:SS.xx] timestamps)
     * @param targetLanguage Target language (e.g., "Spanish", "French", "English")
     * @return Translated LRC or null if failed
     */
    suspend fun translateLyrics(
        currentLyrics: String,
        targetLanguage: String = "English"
    ): Result<String> {
        if (!aiPreferences.isConfigured()) {
            return Result.failure(Exception("AI not configured"))
        }

        val prompt = """
            Translate this LRC (synchronized lyrics) file to $targetLanguage.
            Keep ALL timestamps in the format [MM:SS.xx] unchanged.
            Only translate the text after the timestamps.
            Preserve the LRC structure exactly.
            
            LRC Content:
            $currentLyrics
            
            Return ONLY the translated LRC file, nothing else.
        """.trimIndent()

        return geminiClient.generateContent(prompt)
    }

    /**
     * Search for lyrics using Gemini and generate LRC synchronized format
     * @param song Song to search lyrics for
     * @return LRC format lyrics or null if failed
     */
    suspend fun searchAndGenerateLyrics(song: Song): Result<String> {
        if (!aiPreferences.isConfigured()) {
            return Result.failure(Exception("AI not configured"))
        }

        val prompt = """
            Generate synchronized LRC lyrics for:
            Title: ${song.title}
            Artist: ${song.artistName}
            Duration: ${song.duration}ms
            
            Requirements:
            - Use LRC format with [MM:SS.xx] timestamps
            - Timestamps should be accurate to the song duration
            - Include all verses, choruses, and sections
            - Be creative but realistic - this is for a real song
            - Format: [00:00.00]First line
            [00:05.50]Second line
            etc.
            
            Return ONLY the LRC content, nothing else.
        """.trimIndent()

        return geminiClient.generateContent(prompt)
    }

    /**
     * Extract timestamps from LRC format
     */
    private fun extractLrcTimestamps(lrc: String): List<Pair<Long, String>> {
        val timestampRegex = Regex("""\[(\d{2}):(\d{2})\.(\d{2})\](.*)""")
        return lrc.lines()
            .mapNotNull { line ->
                val match = timestampRegex.find(line)
                if (match != null) {
                    val minutes = match.groupValues[1].toInt()
                    val seconds = match.groupValues[2].toInt()
                    val centiseconds = match.groupValues[3].toInt()
                    val text = match.groupValues[4]
                    val totalMs = (minutes * 60 * 1000) + (seconds * 1000) + (centiseconds * 10)
                    Pair(totalMs.toLong(), text)
                } else {
                    null
                }
            }
    }

    /**
     * Validate LRC format
     */
    fun isValidLrc(lrc: String): Boolean {
        if (lrc.isBlank()) return false
        val hasTimestamps = lrc.contains(Regex("""\[\d{2}:\d{2}.\d{2}\]"""))
        val lines = lrc.lines().filter { it.isNotBlank() }
        return hasTimestamps && lines.size > 5 // Should have meaningful content
    }
}
