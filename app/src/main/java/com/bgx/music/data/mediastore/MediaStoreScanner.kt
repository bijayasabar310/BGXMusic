package com.bgx.music.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.bgx.music.data.local.entity.Album
import com.bgx.music.data.local.entity.Artist
import com.bgx.music.data.local.entity.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ScanResult(
    val songs: List<Song>,
    val albums: List<Album>,
    val artists: List<Artist>
)

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM_ARTIST,
        MediaStore.Audio.Media.GENRE,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.DATE_MODIFIED,
        MediaStore.Audio.Media.MIME_TYPE
    )

    private val selection = buildString {
        append("${MediaStore.Audio.Media.IS_MUSIC} != 0")
        append(" AND ${MediaStore.Audio.Media.DURATION} >= 15000")
    }

    suspend fun scan(excludedFolderPaths: Set<String>): ScanResult = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val albumMap = LinkedHashMap<Long, AlbumAccumulator>()
        val artistMap = LinkedHashMap<Long, ArtistAccumulator>()

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val albumArtistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
            val genreCol = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val filePath = cursor.getStringOrNull(dataCol) ?: continue
                val folderPath = File(filePath).parent ?: continue

                if (excludedFolderPaths.any { folderPath.startsWith(it) }) continue

                val id = cursor.getLong(idCol)
                val artistId = cursor.getLong(artistIdCol)
                val albumId = cursor.getLong(albumIdCol)
                val artistName = cursor.getStringOrNull(artistCol)?.takeIf { it != "<unknown>" }
                    ?: "Unknown Artist"
                val albumName = cursor.getStringOrNull(albumCol) ?: "Unknown Album"
                val year = cursor.getIntOrNull(yearCol)?.takeIf { it > 0 }

                val contentUri = ContentUris.withAppendedId(collection, id)

                songs += Song(
                    mediaStoreId = id,
                    title = cursor.getStringOrNull(titleCol) ?: File(filePath).nameWithoutExtension,
                    artistId = artistId,
                    artistName = artistName,
                    albumId = albumId,
                    albumName = albumName,
                    albumArtist = albumArtistCol.takeIf { it >= 0 }?.let { cursor.getStringOrNull(it) },
                    genre = genreCol.takeIf { it >= 0 }?.let { cursor.getStringOrNull(it) },
                    year = year,
                    trackNumber = cursor.getIntOrNull(trackCol)?.let { it % 1000 },
                    durationMs = cursor.getLong(durationCol),
                    filePath = filePath,
                    contentUriString = contentUri.toString(),
                    sizeBytes = cursor.getLong(sizeCol),
                    dateAdded = cursor.getLong(dateAddedCol) * 1000L,
                    dateModified = cursor.getLong(dateModifiedCol) * 1000L,
                    mimeType = cursor.getStringOrNull(mimeCol),
                    folderPath = folderPath
                )

                albumMap.getOrPut(albumId) { AlbumAccumulator(albumId, albumName, artistId, artistName, year) }
                    .songCount++
                artistMap.getOrPut(artistId) { ArtistAccumulator(artistId, artistName) }
                    .also { it.songCount++; it.albumIds += albumId }
            }
        }

        ScanResult(
            songs = songs,
            albums = albumMap.values.map {
                Album(
                    albumId = it.albumId,
                    name = it.name,
                    artistId = it.artistId,
                    artistName = it.artistName,
                    year = it.year,
                    songCount = it.songCount,
                    hasArtwork = true
                )
            },
            artists = artistMap.values.map {
                Artist(
                    artistId = it.artistId,
                    name = it.name,
                    songCount = it.songCount,
                    albumCount = it.albumIds.size
                )
            }
        )
    }

    private data class AlbumAccumulator(
        val albumId: Long, val name: String, val artistId: Long,
        val artistName: String, val year: Int?, var songCount: Int = 0
    )

    private data class ArtistAccumulator(
        val artistId: Long, val name: String,
        var songCount: Int = 0, val albumIds: MutableSet<Long> = mutableSetOf()
    )

    private fun Cursor.getStringOrNull(index: Int): String? =
        if (index < 0 || isNull(index)) null else getString(index)

    private fun Cursor.getIntOrNull(index: Int): Int? =
        if (index < 0 || isNull(index)) null else getInt(index)
}
