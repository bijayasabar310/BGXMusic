package com.bgx.music.data.local.dao

import androidx.room.*
import com.bgx.music.data.local.entity.Playlist
import com.bgx.music.data.local.entity.PlaylistSongCrossRef
import com.bgx.music.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert
    suspend fun insert(playlist: Playlist): Long

    @Update
    suspend fun update(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE playlistId = :id AND isSystem = 0")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM playlists ORDER BY isSystem DESC, updatedAt DESC")
    fun observeAll(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE playlistId = :id LIMIT 1")
    fun observeById(id: Long): Flow<Playlist?>

    @Query("SELECT * FROM playlists WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): Playlist?

    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE playlistId = :id")
    suspend fun touchUpdatedAt(id: Long, timestamp: Long)

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN playlist_song_cross_ref ON songs.mediaStoreId = playlist_song_cross_ref.songId
        WHERE playlist_song_cross_ref.playlistId = :playlistId AND songs.isMissing = 0
        ORDER BY playlist_song_cross_ref.position ASC
        """
    )
    fun observeSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getSongCount(playlistId: Long): Int

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getNextPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Update
    suspend fun updatePositions(crossRefs: List<PlaylistSongCrossRef>)

    @Query("SELECT * FROM playlist_song_cross_ref WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getCrossRefsForPlaylist(playlistId: Long): List<PlaylistSongCrossRef>

    @Query(
        """
        SELECT songId FROM playlist_song_cross_ref
        WHERE playlistId = :playlistId
        ORDER BY position ASC LIMIT 4
        """
    )
    suspend fun getCoverSongIds(playlistId: Long): List<Long>
}
