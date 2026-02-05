package com.mardous.booming.ai.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.mardous.booming.R
import com.mardous.booming.ai.ui.AiPlayerViewModel
import com.mardous.booming.data.model.Song

/**
 * Custom view for AI player controls (Translate, Search Lyrics, Enrich Metadata)
 */
class AiPlayerControlsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var translateButton: MaterialButton? = null
    private var searchLyricsButton: MaterialButton? = null
    private var loadingState: Boolean = false

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_ai_player_controls, this, true)
        
        translateButton = findViewById(R.id.btn_translate_lyrics)
        searchLyricsButton = findViewById(R.id.btn_search_lyrics)
    }

    fun setup(
        viewModel: AiPlayerViewModel,
        currentSong: Song?,
        currentLyrics: String,
        lifecycleOwner: LifecycleOwner,
        onTranslatedLyricsReceived: (String) -> Unit = {}
    ) {
        if (currentSong == null || !viewModel.isAiEnabled()) {
            visibility = GONE
            return
        }

        visibility = VISIBLE

        // Translate button
        translateButton?.setOnClickListener {
            if (currentLyrics.isNotEmpty()) {
                viewModel.translateLyrics(currentLyrics)
            } else {
                showError("No lyrics available")
            }
        }

        // Search lyrics button
        searchLyricsButton?.setOnClickListener {
            viewModel.searchLyrics(currentSong)
        }

        // Observe translation state
        viewModel.translationState.observe(lifecycleOwner, Observer { state ->
            when (state) {
                is AiPlayerViewModel.TranslationState.Loading -> {
                    setButtonsEnabled(false)
                    loadingState = true
                }
                is AiPlayerViewModel.TranslationState.Success -> {
                    setButtonsEnabled(true)
                    loadingState = false
                    showSuccess("Lyrics processed successfully")
                }
                is AiPlayerViewModel.TranslationState.Error -> {
                    setButtonsEnabled(true)
                    loadingState = false
                    showError(state.message)
                }
                is AiPlayerViewModel.TranslationState.Idle -> {
                    setButtonsEnabled(true)
                    loadingState = false
                }
            }
        })

        // Observe metadata state
        viewModel.metadataState.observe(lifecycleOwner, Observer { state ->
            when (state) {
                is AiPlayerViewModel.MetadataState.Loading -> {
                    setButtonsEnabled(false)
                }
                is AiPlayerViewModel.MetadataState.Success -> {
                    setButtonsEnabled(true)
                    showSuccess("Metadata enriched successfully")
                }
                is AiPlayerViewModel.MetadataState.Error -> {
                    setButtonsEnabled(true)
                    showError(state.message)
                }
                is AiPlayerViewModel.MetadataState.Idle -> {
                    setButtonsEnabled(true)
                }
            }
        })

        // Observe translated lyrics
        viewModel.translatedLyrics.observe(lifecycleOwner, Observer { lyrics ->
            if (lyrics.isNotEmpty()) {
                onTranslatedLyricsReceived(lyrics)
            }
        })
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        translateButton?.isEnabled = enabled
        searchLyricsButton?.isEnabled = enabled
    }

    private fun showSuccess(message: String) {
        val parent = parent as? android.view.ViewGroup ?: return
        Snackbar.make(parent, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        val parent = parent as? android.view.ViewGroup ?: return
        Snackbar.make(parent, "Error: $message", Snackbar.LENGTH_LONG).show()
    }
}
