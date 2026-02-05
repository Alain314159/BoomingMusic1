package com.mardous.booming.ui.adapters.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mardous.booming.R
import com.mardous.booming.data.local.room.PlaylistEntity
import com.mardous.booming.data.mapper.toSongEntity
import com.mardous.booming.data.model.AiAlbum
import com.mardous.booming.data.local.repository.AiAlbumRepository
import com.mardous.booming.data.local.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AiAlbumAdapter(
    private val activity: FragmentActivity,
    var dataSet: List<AiAlbum>
) : RecyclerView.Adapter<AiAlbumAdapter.ViewHolder>(), KoinComponent {

    private val repository: com.mardous.booming.data.local.repository.Repository by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ai_album, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        holder.title.text = item.title
        holder.description.text = item.description ?: ""
        holder.itemView.setOnClickListener {
            // Create playlist and add songs
            holder.itemView.isEnabled = false
            Snackbar.make(holder.itemView, activity.getString(R.string.creating_playlist), Snackbar.LENGTH_SHORT).show()
            activity.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val playlistName = "AI: ${item.title}"
                        val playlistEntity = PlaylistEntity(playlistName = playlistName)
                        val playlistId = repository.createPlaylist(playlistEntity)

                        val songs = item.trackIds.mapNotNull { id ->
                            try {
                                repository.songById(id)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (songs.isNotEmpty()) {
                            val songEntities = songs.map { it.toSongEntity(playlistId) }
                            repository.insertSongsInPlaylist(songEntities)
                        }
                    } catch (e: Exception) {
                        // ignore here, show message below
                    }
                }

                holder.itemView.isEnabled = true
                Snackbar.make(holder.itemView, activity.getString(R.string.playlist_created), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.ai_album_title)
        val description: TextView = itemView.findViewById(R.id.ai_album_description)
    }
}
