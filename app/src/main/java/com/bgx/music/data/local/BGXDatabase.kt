package com.bgx.music.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bgx.music.data.local.dao.*
import com.bgx.music.data.local.entity.*

@Database(
    entities = [
        Song::class,
        Album::class,
        Artist::class,
        Playlist::class,
        PlaylistSongCrossRef::class,
        Favorite::class,
        RecentlyPlayed::class,
        PlayCount::class,
        CustomArtwork::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BGXDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun artworkDao(): ArtworkDao

    companion object {
        private const val DB_NAME = "bgx_music.db"

        @Volatile private var instance: BGXDatabase? = null

        fun getInstance(context: Context): BGXDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BGXDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
