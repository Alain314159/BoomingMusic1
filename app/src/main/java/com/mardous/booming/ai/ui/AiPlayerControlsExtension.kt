package com.mardous.booming.ai.ui

import com.mardous.booming.ai.ui.view.AiPlayerControlsView
import com.mardous.booming.data.model.Song
import com.mardous.booming.ui.component.base.AbsPlayerControlsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Extension function to setup AI controls in player fragment
 */
fun AbsPlayerControlsFragment.setupAiControls(
    aiControlsView: AiPlayerControlsView?,
    currentSong: Song?,
    currentLyrics: String,
    onTranslatedLyricsReceived: (String) -> Unit = {}
) {
    aiControlsView?.let {
        val viewModel: AiPlayerViewModel by viewModel()
        it.setup(
            viewModel,
            currentSong,
            currentLyrics,
            viewLifecycleOwner,
            onTranslatedLyricsReceived
        )
    }
}
