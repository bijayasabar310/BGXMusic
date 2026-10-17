package com.bgx.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey val albumId: Long,
    val name: String,
    val artistId: Long,
    val artistName: String,
    val year: Int?,
    val songCount: Int,
    val hasArtwork: Boolean
)
