package com.bgx.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey val artistId: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int
)
