package com.mardous.booming.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AiAlbumEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ai_album_id")
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "theme")
    val theme: String?,
    // Comma separated song ids
    @ColumnInfo(name = "track_ids")
    val trackIds: String,
    @ColumnInfo(name = "suggested_genres")
    val suggestedGenres: String?,
    @ColumnInfo(name = "cover_prompt")
    val coverPrompt: String?,
    @ColumnInfo(name = "cover_uri")
    val coverUri: String? = null,
    @ColumnInfo(name = "generated_at")
    val generatedAt: Long = System.currentTimeMillis()
)
