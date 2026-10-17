package com.bgx.music.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artistId"]),
        Index(value = ["albumId"]),
        Index(value = ["dateAdded"])
    ]
)
data class Song(
    @PrimaryKey val mediaStoreId: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumName: String,
    val albumArtist: String?,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?,
    val durationMs: Long,
    val filePath: String,
    val contentUriString: String,
    val sizeBytes: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val mimeType: String?,
    val folderPath: String,
    val isMissing: Boolean = false
)
