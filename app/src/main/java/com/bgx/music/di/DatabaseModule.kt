package com.bgx.music.di

import android.content.Context
import com.bgx.music.data.local.BGXDatabase
import com.bgx.music.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BGXDatabase =
        BGXDatabase.getInstance(context)

    @Provides fun provideSongDao(db: BGXDatabase): SongDao = db.songDao()
    @Provides fun provideAlbumDao(db: BGXDatabase): AlbumDao = db.albumDao()
    @Provides fun provideArtistDao(db: BGXDatabase): ArtistDao = db.artistDao()
    @Provides fun providePlaylistDao(db: BGXDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideFavoriteDao(db: BGXDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideHistoryDao(db: BGXDatabase): HistoryDao = db.historyDao()
    @Provides fun provideArtworkDao(db: BGXDatabase): ArtworkDao = db.artworkDao()
}
