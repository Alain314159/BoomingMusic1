package com.mardous.booming.ai.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mardous.booming.ai.ui.AiPlayerViewModel
import com.mardous.booming.data.model.Song

/**
 * Composable row with AI buttons for the player screen
 */
@Composable
fun AiPlayerControls(
    viewModel: AiPlayerViewModel,
    currentSong: Song?,
    currentLyrics: String,
    onTranslatedLyricsReceived: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!viewModel.isAiEnabled() || currentSong == null) {
        return
    }

    val translationState by viewModel.translationState.observeAsState()
    val metadataState by viewModel.metadataState.observeAsState()
    val translatedLyrics by viewModel.translatedLyrics.observeAsState()

    // Call listener when lyrics are translated
    translatedLyrics?.let {
        onTranslatedLyricsReceived(it)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Translate Lyrics Button
        AiIconButton(
            icon = Icons.Outlined.Edit,
            contentDescription = "Translate lyrics",
            isLoading = translationState is AiPlayerViewModel.TranslationState.Loading,
            onClick = {
                if (currentLyrics.isNotEmpty()) {
                    viewModel.translateLyrics(currentLyrics)
                }
            }
        )

        // Search/Generate Lyrics Button
        AiIconButton(
            icon = Icons.Outlined.Search,
            contentDescription = "Search missing lyrics",
            isLoading = translationState is AiPlayerViewModel.TranslationState.Loading,
            onClick = {
                viewModel.searchLyrics(currentSong)
            }
        )

        // Note: Could add more buttons for metadata enrichment if needed
    }
}

@Composable
fun AiIconButton(
    icon: ImageVector,
    contentDescription: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(4.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
