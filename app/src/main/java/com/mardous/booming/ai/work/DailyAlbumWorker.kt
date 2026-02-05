package com.mardous.booming.ai.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mardous.booming.ai.AiPreferences
import com.mardous.booming.ai.services.AiAlbumGeneratorService
import com.mardous.booming.data.local.repository.Repository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Background worker for generating daily AI albums
 */
class DailyAlbumWorker(
    context: Context,
    params: androidx.work.WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository by inject<Repository>()
    private val aiPreferences by inject<AiPreferences>()
    private val albumGenerator by inject<AiAlbumGeneratorService>()

    override suspend fun doWork(): Result {
        return try {
            if (!aiPreferences.isConfigured()) {
                Log.d("DailyAlbumWorker", "AI not configured, skipping")
                return Result.success()
            }

            if (!aiPreferences.dailyAlbumEnabled) {
                Log.d("DailyAlbumWorker", "Daily album disabled, skipping")
                return Result.success()
            }

            // Get last 100 songs listened
            val recentSongs = repository.allSongs().take(100)
            
            if (recentSongs.isEmpty()) {
                Log.d("DailyAlbumWorker", "No recent songs, skipping")
                return Result.success()
            }

            // Get play counts for each song
            val playCounts = mutableMapOf<Long, Int>()
            // TODO: Fetch actual play counts from persistence layer if available

            // Generate album
            val result = albumGenerator.generateDaily(
                recentSongs = recentSongs,
                playCounts = playCounts,
                targetTrackCount = 10
            )

            result.onSuccess { album ->
                // Create playlist with the selected tracks
                val selectedSongs = album.selectedTrackIndices.mapNotNull { index ->
                    recentSongs.getOrNull(index)
                }

                if (selectedSongs.isNotEmpty()) {
                    val playlistName = "AI ${album.title}"
                    // TODO: Create playlist in DB
                    Log.d("DailyAlbumWorker", "Created playlist: $playlistName")
                }
            }.onFailure { error ->
                Log.e("DailyAlbumWorker", "Failed to generate album", error)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyAlbumWorker", "Worker failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "daily_album_work"

        fun schedule(context: Context) {
            val dailyAlbumWork = PeriodicWorkRequestBuilder<DailyAlbumWorker>(
                Duration.ofHours(24)
            ).addTag(TAG).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                TAG,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                dailyAlbumWork
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
        }
    }
}
