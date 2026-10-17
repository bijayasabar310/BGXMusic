package com.bgx.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val songId: Long,
    val addedAt: Long
)

@Entity(tableName = "recently_played")
data class RecentlyPlayed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val playedAt: Long
)

@Entity(tableName = "play_counts")
data class PlayCount(
    @PrimaryKey val songId: Long,
    val count: Int,
    val lastPlayedAt: Long
)

@Entity(tableName = "custom_artwork")
data class CustomArtwork(
    @PrimaryKey val songId: Long,
    val localImagePath: String,
    val appliesToAlbum: Boolean = false,
    val albumId: Long? = null
)
