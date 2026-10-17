package com.bgx.music.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.repository.MusicRepository
import com.bgx.music.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class FolderSummary(val path: String, val name: String, val songCount: Int)

@HiltViewModel
class FoldersViewModel @Inject constructor(
    musicRepository: MusicRepository
) : ViewModel() {

    val folders: StateFlow<List<FolderSummary>> = musicRepository.observeSongs()
        .map { songs ->
            songs.groupBy { it.folderPath }
                .map { (path, songsInFolder) ->
                    FolderSummary(path, path.substringAfterLast("/"), songsInFolder.size)
                }
                .sortedBy { it.name.lowercase() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    musicRepository: MusicRepository,
    val playbackController: PlaybackController
) : ViewModel() {

    private val folderPath: String = java.net.URLDecoder.decode(
        checkNotNull(savedStateHandle["path"]), "UTF-8"
    )
    val folderName: String = folderPath.substringAfterLast("/")

    val songs: StateFlow<List<Song>> = musicRepository.observeSongsInFolder(folderPath)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playFolder() = playbackController.playSongs(songs.value, 0)
    fun shuffleFolder() = playbackController.shuffleAll(songs.value)
    fun playSong(song: Song) {
        val list = songs.value
        playbackController.playSongs(list, list.indexOf(song).coerceAtLeast(0))
    }
}
