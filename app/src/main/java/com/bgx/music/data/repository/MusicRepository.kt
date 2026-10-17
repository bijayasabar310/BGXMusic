package com.bgx.music.data.repository

import com.bgx.music.data.local.dao.*
import com.bgx.music.data.local.entity.*
import com.bgx.music.data.mediastore.MediaStoreScanner
import com.bgx.music.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val scanner: MediaStoreScanner,
    private val preferences: UserPreferences
) {

    suspend fun rescanLibrary() {
        val excluded = preferences.excludedFolders.first()
        val result = scanner.scan(excluded)

        songDao.upsertAll(result.songs)
        albumDao.upsertAll(result.albums)
        artistDao.upsertAll(result.artists)

        val scannedIds = result.songs.map { it.mediaStoreId }.toSet()
        val existingIds = songDao.getAllIds()
        val missingIds = existingIds.filter { it !in scannedIds }
        if (missingIds.isNotEmpty()) songDao.markMissing(missingIds)

        albumDao.pruneMissing(result.albums.map { it.albumId })
        artistDao.pruneMissing(result.artists.map { it.artistId })
    }

    fun observeSongs(): Flow<List<Song>> = songDao.observeAllByTitle()
    fun observeAlbums(): Flow<List<Album>> = albumDao.observeAll()
    fun observeArtists(): Flow<List<Artist>> = artistDao.observeAll()
    fun observeAlbum(albumId: Long): Flow<Album?> = albumDao.observeById(albumId)
    fun observeArtist(artistId: Long): Flow<Artist?> = artistDao.observeById(artistId)
    fun observeSongsForAlbum(albumId: Long): Flow<List<Song>> = songDao.observeSongsForAlbum(albumId)
    fun observeSongsForArtist(artistId: Long): Flow<List<Song>> = songDao.observeSongsForArtist(artistId)
    fun observeAlbumsForArtist(artistId: Long): Flow<List<Album>> = albumDao.observeByArtist(artistId)
    fun observeSongsInFolder(folderPath: String): Flow<List<Song>> = songDao.observeSongsInFolder(folderPath)
    fun observeLibrarySize(): Flow<Int> = songDao.observeTotalCount()

    suspend fun search(query: String): List<Song> = songDao.search(query)
    suspend fun getSongsByIds(ids: List<Long>): List<Song> = songDao.getByIds(ids)

    fun observeFavorites(): Flow<List<Song>> = favoriteDao.observeFavoriteSongs()
    fun observeIsFavorite(songId: Long): Flow<Boolean> = favoriteDao.observeIsFavorite(songId)

    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        if (isFavorite) favoriteDao.add(Favorite(songId, System.currentTimeMillis()))
        else favoriteDao.remove(songId)
    }

    fun observeRecentlyPlayed(limit: Int = 30): Flow<List<Song>> = historyDao.observeRecentlyPlayed(limit)
    fun observeMostPlayed(limit: Int = 30): Flow<List<Song>> = historyDao.observeMostPlayed(limit)

    suspend fun recordPlaybackStart(songId: Long) {
        historyDao.logPlayed(RecentlyPlayed(songId = songId, playedAt = System.currentTimeMillis()))
    }

    suspend fun incrementPlayCount(songId: Long) {
        val existing = historyDao.getPlayCount(songId)
        historyDao.upsertPlayCount(
            PlayCount(
                songId = songId,
                count = (existing?.count ?: 0) + 1,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }
}
