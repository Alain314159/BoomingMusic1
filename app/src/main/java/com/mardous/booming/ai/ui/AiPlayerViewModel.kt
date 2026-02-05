package com.mardous.booming.ai.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mardous.booming.ai.AiPreferences
import com.mardous.booming.ai.services.LyricsTranslationService
import com.mardous.booming.ai.services.MetadataEnricherService
import com.mardous.booming.data.model.Song
import kotlinx.coroutines.launch

/**
 * ViewModel for AI features in the player screen
 */
class AiPlayerViewModel(
    private val aiPreferences: AiPreferences,
    private val lyricsService: LyricsTranslationService,
    private val metadataService: MetadataEnricherService
) : ViewModel() {

    private val _translationState = MutableLiveData<TranslationState>(TranslationState.Idle)
    val translationState: LiveData<TranslationState> = _translationState

    private val _metadataState = MutableLiveData<MetadataState>(MetadataState.Idle)
    val metadataState: LiveData<MetadataState> = _metadataState

    private val _translatedLyrics = MutableLiveData<String>()
    val translatedLyrics: LiveData<String> = _translatedLyrics

    fun isAiEnabled(): Boolean = aiPreferences.isConfigured()

    fun translateLyrics(currentLyrics: String) {
        if (!isAiEnabled()) {
            _translationState.value = TranslationState.Error("AI not configured")
            return
        }

        viewModelScope.launch {
            _translationState.value = TranslationState.Loading
            val result = lyricsService.translateLyrics(
                currentLyrics = currentLyrics,
                targetLanguage = aiPreferences.preferredTranslateLang
            )

            result.onSuccess { translated ->
                _translatedLyrics.value = translated
                _translationState.value = TranslationState.Success
            }.onFailure { error ->
                _translationState.value = TranslationState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun searchLyrics(song: Song) {
        if (!isAiEnabled()) {
            _translationState.value = TranslationState.Error("AI not configured")
            return
        }

        viewModelScope.launch {
            _translationState.value = TranslationState.Loading
            val result = lyricsService.searchAndGenerateLyrics(song)

            result.onSuccess { lyrics ->
                if (lyricsService.isValidLrc(lyrics)) {
                    _translatedLyrics.value = lyrics
                    _translationState.value = TranslationState.Success
                } else {
                    _translationState.value = TranslationState.Error("Invalid lyrics format")
                }
            }.onFailure { error ->
                _translationState.value = TranslationState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun enrichMetadata(song: Song) {
        if (!isAiEnabled()) {
            _metadataState.value = MetadataState.Error("AI not configured")
            return
        }

        viewModelScope.launch {
            _metadataState.value = MetadataState.Loading
            val result = metadataService.enrichMetadata(song)

            result.onSuccess { metadata ->
                if (metadata.title != null || metadata.artist != null || metadata.album != null) {
                    _metadataState.value = MetadataState.Success(metadata)
                } else {
                    _metadataState.value = MetadataState.Error("No improvements found")
                }
            }.onFailure { error ->
                _metadataState.value = MetadataState.Error(error.message ?: "Unknown error")
            }
        }
    }

    sealed class TranslationState {
        object Idle : TranslationState()
        object Loading : TranslationState()
        object Success : TranslationState()
        data class Error(val message: String) : TranslationState()
    }

    sealed class MetadataState {
        object Idle : MetadataState()
        object Loading : MetadataState()
        data class Success(val metadata: MetadataEnricherService.MetadataCorrection) : MetadataState()
        data class Error(val message: String) : MetadataState()
    }
}
