package com.mardous.booming.ai.ui

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.mardous.booming.ai.ui.view.AiPlayerControlsView
import com.mardous.booming.data.model.Song
import com.mardous.booming.ui.component.base.AbsPlayerControlsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Integration helper for AI controls in player fragments
 *
 * Usage in your player fragment's onViewCreated():
 *
 * ```kotlin
 * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *     super.onViewCreated(view, savedInstanceState)
 *     // ... existing setup code ...
 *
 *     // Add AI controls
 *     addAiControls(binding.root)
 * }
 * ```
 */
object AiPlayerControlsHelper {
    
    /**
     * Add AI player controls to the fragment's root view
     * The view will be added as the first child of the root view group
     */
    fun AbsPlayerControlsFragment.addAiPlayerControls(
        rootView: ViewGroup,
        currentSong: Song? = null,
        currentLyrics: String = "",
        onTranslatedLyricsReceived: (String) -> Unit = {}
    ) {
        // Check if AI is enabled
        val viewModel: AiPlayerViewModel by viewModel()
        if (!viewModel.isAiEnabled()) {
            return
        }

        // Create and setup AI controls view
        val aiControlsView = AiPlayerControlsView(rootView.context)
        
        // Add to the beginning of the layout (for better visibility)
        rootView.addView(aiControlsView, 0)
        
        // Setup the view with ViewModel
        aiControlsView.setup(
            viewModel,
            currentSong,
            currentLyrics,
            viewLifecycleOwner,
            onTranslatedLyricsReceived
        )
    }

    /**
     * Alternative method: Add AI controls at specific parent container
     * Useful when you have a dedicated container in your layout
     */
    fun AbsPlayerControlsFragment.addAiPlayerControlsToContainer(
        containerView: ViewGroup,
        currentSong: Song? = null,
        currentLyrics: String = "",
        onTranslatedLyricsReceived: (String) -> Unit = {}
    ) {
        val viewModel: AiPlayerViewModel by viewModel()
        if (!viewModel.isAiEnabled()) {
            return
        }

        val aiControlsView = AiPlayerControlsView(containerView.context)
        containerView.addView(aiControlsView)
        
        aiControlsView.setup(
            viewModel,
            currentSong,
            currentLyrics,
            viewLifecycleOwner,
            onTranslatedLyricsReceived
        )
    }
}
