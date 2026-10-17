package com.bgx.music.data.local.dao

import androidx.room.*
import com.bgx.music.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Upsert
    suspend fun add(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun remove(songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun observeIsFavorite(songId: Long): Flow<Boolean>

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN favorites ON songs.mediaStoreId = favorites.songId
        WHERE songs.isMissing = 0
        ORDER BY favorites.addedAt DESC
        """
    )
    fun observeFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeCount(): Flow<Int>
}

@Dao
interface HistoryDao {
    @Insert
    suspend fun logPlayed(entry: RecentlyPlayed)

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN (
            SELECT songId, MAX(playedAt) AS lastPlayed
            FROM recently_played GROUP BY songId
        ) latest ON songs.mediaStoreId = latest.songId
        WHERE songs.isMissing = 0
        ORDER BY latest.lastPlayed DESC
        LIMIT :limit
        """
    )
    fun observeRecentlyPlayed(limit: Int = 30): Flow<List<Song>>

    @Query("DELETE FROM recently_played WHERE playedAt < :olderThan")
    suspend fun pruneOlderThan(olderThan: Long)

    @Upsert
    suspend fun upsertPlayCount(playCount: PlayCount)

    @Query("SELECT * FROM play_counts WHERE songId = :songId LIMIT 1")
    suspend fun getPlayCount(songId: Long): PlayCount?

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN play_counts ON songs.mediaStoreId = play_counts.songId
        WHERE songs.isMissing = 0
        ORDER BY play_counts.count DESC
        LIMIT :limit
        """
    )
    fun observeMostPlayed(limit: Int = 30): Flow<List<Song>>
}

@Dao
interface ArtworkDao {
    @Upsert
    suspend fun setCustomArtwork(artwork: CustomArtwork)

    @Query("DELETE FROM custom_artwork WHERE songId = :songId")
    suspend fun removeCustomArtwork(songId: Long)

    @Query("SELECT * FROM custom_artwork WHERE songId = :songId LIMIT 1")
    suspend fun getCustomArtwork(songId: Long): CustomArtwork?

    @Query("SELECT * FROM custom_artwork WHERE albumId = :albumId AND appliesToAlbum = 1 LIMIT 1")
    suspend fun getCustomArtworkForAlbum(albumId: Long): CustomArtwork?
}
