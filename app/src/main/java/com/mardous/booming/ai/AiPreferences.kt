package com.mardous.booming.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages AI preferences securely using EncryptedSharedPreferences.
 * Stores sensitive data like API keys in encrypted form.
 */
class AiPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "ai_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_AI_ENABLED = "ai_enabled"
        private const val KEY_AUTO_TRANSLATE_LYRICS = "auto_translate_lyrics"
        private const val KEY_AUTO_SEARCH_LYRICS = "auto_search_lyrics"
        private const val KEY_AUTO_FIX_METADATA = "auto_fix_metadata"
        private const val KEY_PREFERRED_TRANSLATE_LANG = "preferred_translate_lang"
        private const val KEY_DAILY_ALBUM_ENABLED = "daily_album_enabled"
        private const val KEY_REQUEST_COUNT = "request_count"
    }

    var geminiApiKey: String?
        get() = sharedPreferences.getString(KEY_GEMINI_API_KEY, null)
        set(value) {
            if (value.isNullOrEmpty()) {
                sharedPreferences.edit().remove(KEY_GEMINI_API_KEY).apply()
            } else {
                sharedPreferences.edit().putString(KEY_GEMINI_API_KEY, value).apply()
            }
        }

    var isAiEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AI_ENABLED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_AI_ENABLED, value).apply()

    var autoTranslateLyrics: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_TRANSLATE_LYRICS, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_AUTO_TRANSLATE_LYRICS, value).apply()

    var autoSearchLyrics: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_SEARCH_LYRICS, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_AUTO_SEARCH_LYRICS, value).apply()

    var autoFixMetadata: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_FIX_METADATA, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_AUTO_FIX_METADATA, value).apply()

    var preferredTranslateLang: String
        get() = sharedPreferences.getString(KEY_PREFERRED_TRANSLATE_LANG, "en") ?: "en"
        set(value) = sharedPreferences.edit().putString(KEY_PREFERRED_TRANSLATE_LANG, value).apply()

    var dailyAlbumEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DAILY_ALBUM_ENABLED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_DAILY_ALBUM_ENABLED, value).apply()

    var requestCount: Int
        get() = sharedPreferences.getInt(KEY_REQUEST_COUNT, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_REQUEST_COUNT, value).apply()

    fun isConfigured(): Boolean = !geminiApiKey.isNullOrEmpty()

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
