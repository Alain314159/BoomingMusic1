package com.mardous.booming.data.local.repository

import com.mardous.booming.data.local.room.AiAlbumEntity

interface AiAlbumRepository {
    suspend fun saveAlbum(aiAlbumEntity: AiAlbumEntity): Long
    suspend fun recentAiAlbums(limit: Int = 10): List<AiAlbumEntity>
    suspend fun deleteOlderThan(beforeTs: Long)
}

class RealAiAlbumRepository(
    private val aiAlbumDao: com.mardous.booming.data.local.room.AiAlbumDao
) : AiAlbumRepository {
    override suspend fun saveAlbum(aiAlbumEntity: AiAlbumEntity): Long = aiAlbumDao.insert(aiAlbumEntity)

    override suspend fun recentAiAlbums(limit: Int): List<AiAlbumEntity> = aiAlbumDao.recentAiAlbums(limit)

    override suspend fun deleteOlderThan(beforeTs: Long) = aiAlbumDao.deleteOlderThan(beforeTs)
}
