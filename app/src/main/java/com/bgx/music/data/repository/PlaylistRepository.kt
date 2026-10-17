package com.bgx.music.data.repository

import com.bgx.music.data.local.dao.PlaylistDao
import com.bgx.music.data.local.entity.Playlist
import com.bgx.music.data.local.entity.PlaylistSongCrossRef
import com.bgx.music.data.local.entity.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {

    fun observePlaylists(): Flow<List<Playlist>> = playlistDao.observeAll()
    fun observePlaylist(id: Long): Flow<Playlist?> = playlistDao.observeById(id)
    fun observeSongsInPlaylist(id: Long): Flow<List<Song>> = playlistDao.observeSongsInPlaylist(id)

    suspend fun createPlaylist(name: String): Long {
        val now = System.currentTimeMillis()
        return playlistDao.insert(Playlist(name = name, createdAt = now, updatedAt = now))
    }

    suspend fun renamePlaylist(playlist: Playlist, newName: String) {
        playlistDao.update(playlist.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(id: Long) = playlistDao.delete(id)

    suspend fun addSongs(playlistId: Long, songIds: List<Long>) {
        var nextPosition = playlistDao.getNextPosition(playlistId)
        val now = System.currentTimeMillis()
        songIds.forEach { songId ->
            playlistDao.addSongToPlaylist(
                PlaylistSongCrossRef(playlistId, songId, nextPosition, now)
            )
            nextPosition++
        }
        touch(playlistId)
    }

    suspend fun removeSong(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        touch(playlistId)
    }

    suspend fun reorder(playlistId: Long, orderedSongIds: List<Long>) {
        val crossRefs = orderedSongIds.mapIndexed { index, songId ->
            PlaylistSongCrossRef(playlistId, songId, index, System.currentTimeMillis())
        }
        playlistDao.updatePositions(crossRefs)
        touch(playlistId)
    }

    suspend fun getCoverSongIds(playlistId: Long): List<Long> = playlistDao.getCoverSongIds(playlistId)

    private suspend fun touch(playlistId: Long) {
        playlistDao.touchUpdatedAt(playlistId, System.currentTimeMillis())
    }
}
