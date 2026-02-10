package com.mardous.booming.data.model

import com.mardous.booming.data.local.room.AiAlbumEntity

data class AiAlbum(
    val id: Long,
    val title: String,
    val description: String?,
    val theme: String?,
    val trackIds: List<Long>,
    val suggestedGenres: List<String>,
    val coverPrompt: String?,
    val coverUri: String?,
    val generatedAt: Long
)

object AiAlbumMapper {
    fun fromEntity(entity: AiAlbumEntity): AiAlbum {
        val ids = if (entity.trackIds.isBlank()) emptyList() else entity.trackIds.split(",").mapNotNull { it.toLongOrNull() }
        val genres = entity.suggestedGenres?.split(",")?.map { it.trim() } ?: emptyList()
        return AiAlbum(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            theme = entity.theme,
            trackIds = ids,
            suggestedGenres = genres,
            coverPrompt = entity.coverPrompt,
            coverUri = entity.coverUri,
            generatedAt = entity.generatedAt
        )
    }
}
