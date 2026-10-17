package com.bgx.music.playback

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.bgx.music.data.local.entity.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackController @Inject constructor(
    private val connection: PlaybackConnection,
    private val queueManager: QueueManager
) {

    fun playSongs(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        connection.connect {
            queueManager.setQueue(songs, startIndex, shuffle)
            pushQueueToPlayer(startIndex)
        }
    }

    fun playSingle(song: Song) = playSongs(listOf(song), 0)

    fun shuffleAll(songs: List<Song>) = playSongs(songs, startIndex = 0, shuffle = true)

    private fun pushQueueToPlayer(startIndex: Int) {
        val state = queueManager.state.value
        val items = state.songs.map { it.toMediaItem() }
        val controller = connection.controller ?: return
        controller.setMediaItems(items, startIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0)), C.TIME_UNSET)
        controller.prepare()
        controller.play()
    }

    fun addToQueueNext(song: Song) {
        queueManager.addToQueueNext(song)
        val index = queueManager.state.value.songs.indexOf(song)
        connection.controller?.addMediaItem(index.coerceAtLeast(0), song.toMediaItem())
    }

    fun addToQueueEnd(song: Song) {
        queueManager.addToQueueEnd(song)
        connection.controller?.addMediaItem(song.toMediaItem())
    }

    fun removeFromQueue(index: Int) {
        queueManager.removeAt(index)
        connection.controller?.removeMediaItem(index)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        queueManager.reorder(fromIndex, toIndex)
        connection.controller?.moveMediaItem(fromIndex, toIndex)
    }

    fun playFromQueue(index: Int) {
        queueManager.moveToIndex(index)
        connection.controller?.seekTo(index, 0)
        connection.controller?.play()
    }

    fun toggleShuffle() {
        val newState = !queueManager.state.value.shuffleEnabled
        queueManager.setShuffle(newState)
        connection.controller?.shuffleModeEnabled = newState
    }

    private fun Song.toMediaItem(): MediaItem {
        val artworkUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            .setArtworkUri(artworkUri)
            .setIsPlayable(true)
            .build()
        return MediaItem.Builder()
            .setMediaId(mediaStoreId.toString())
            .setUri(contentUriString)
            .setMediaMetadata(metadata)
            .build()
    }
}
