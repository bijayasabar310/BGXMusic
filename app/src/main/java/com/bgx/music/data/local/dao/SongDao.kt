package com.bgx.music.data.local.dao

import androidx.room.*
import com.bgx.music.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Upsert
    suspend fun upsertAll(songs: List<Song>)

    @Upsert
    suspend fun upsert(song: Song)

    @Query("DELETE FROM songs WHERE mediaStoreId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT mediaStoreId FROM songs")
    suspend fun getAllIds(): List<Long>

    @Query("UPDATE songs SET isMissing = 1 WHERE mediaStoreId IN (:ids)")
    suspend fun markMissing(ids: List<Long>)

    @Query("SELECT * FROM songs WHERE isMissing = 0 ORDER BY title COLLATE NOCASE ASC")
    fun observeAllByTitle(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isMissing = 0 ORDER BY artistName COLLATE NOCASE ASC, albumName COLLATE NOCASE ASC, trackNumber ASC")
    fun observeAllByArtist(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isMissing = 0 ORDER BY albumName COLLATE NOCASE ASC, trackNumber ASC")
    fun observeAllByAlbum(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isMissing = 0 ORDER BY dateAdded DESC")
    fun observeAllByDateAdded(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isMissing = 0 ORDER BY durationMs ASC")
    fun observeAllByDuration(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isMissing = 0 AND albumId = :albumId ORDER BY trackNumber ASC, title ASC")
    fun observeSongsForAlbum(albumId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isMissing = 0 AND artistId = :artistId ORDER BY albumName ASC, trackNumber ASC")
    fun observeSongsForArtist(artistId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isMissing = 0 AND folderPath = :folderPath ORDER BY title COLLATE NOCASE ASC")
    fun observeSongsInFolder(folderPath: String): Flow<List<Song>>

    @Query("SELECT DISTINCT folderPath FROM songs WHERE isMissing = 0")
    fun observeFolders(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM songs
        WHERE isMissing = 0 AND (
            title LIKE "%" || :query || "%" COLLATE NOCASE OR
            artistName LIKE "%" || :query || "%" COLLATE NOCASE OR
            albumName LIKE "%" || :query || "%" COLLATE NOCASE
        )
        ORDER BY title COLLATE NOCASE ASC
        LIMIT 100
        """
    )
    suspend fun search(query: String): List<Song>

    @Query("SELECT * FROM songs WHERE mediaStoreId IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<Song>

    @Query("SELECT * FROM songs WHERE mediaStoreId = :id LIMIT 1")
    suspend fun getById(id: Long): Song?

    @Query("SELECT COUNT(*) FROM songs WHERE isMissing = 0")
    fun observeTotalCount(): Flow<Int>
}
