package com.bgx.music.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bgx.music.data.local.entity.Album
import com.bgx.music.data.local.entity.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Upsert
    suspend fun upsertAll(albums: List<Album>)

    @Query("DELETE FROM albums WHERE albumId NOT IN (:keepIds)")
    suspend fun pruneMissing(keepIds: List<Long>)

    @Query("SELECT * FROM albums ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE albumId = :albumId LIMIT 1")
    fun observeById(albumId: Long): Flow<Album?>

    @Query("SELECT * FROM albums WHERE artistId = :artistId ORDER BY year DESC")
    fun observeByArtist(artistId: Long): Flow<List<Album>>
}

@Dao
interface ArtistDao {
    @Upsert
    suspend fun upsertAll(artists: List<Artist>)

    @Query("DELETE FROM artists WHERE artistId NOT IN (:keepIds)")
    suspend fun pruneMissing(keepIds: List<Long>)

    @Query("SELECT * FROM artists ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Artist>>

    @Query("SELECT * FROM artists WHERE artistId = :artistId LIMIT 1")
    fun observeById(artistId: Long): Flow<Artist?>
}
