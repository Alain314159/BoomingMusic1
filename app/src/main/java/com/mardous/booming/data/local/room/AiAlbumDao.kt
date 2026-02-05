package com.mardous.booming.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AiAlbumDao {
    @Insert
    suspend fun insert(aiAlbumEntity: AiAlbumEntity): Long

    @Query("SELECT * FROM AiAlbumEntity ORDER BY generated_at DESC LIMIT :limit")
    suspend fun recentAiAlbums(limit: Int = 10): List<AiAlbumEntity>

    @Query("DELETE FROM AiAlbumEntity WHERE generated_at < :beforeTs")
    suspend fun deleteOlderThan(beforeTs: Long)
}
