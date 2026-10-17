package com.bgx.music.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "bgx_settings")

enum class AppTheme { LIGHT, DARK, AMOLED, SYSTEM_DYNAMIC }
enum class SongSortOrder { TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION }
enum class LibraryViewStyle { LIST, GRID }
enum class RepeatMode { OFF, ONE, ALL }

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val ACCENT_COLOR_ARGB = intPreferencesKey("accent_color_argb")
        val USE_DYNAMIC_ACCENT = booleanPreferencesKey("use_dynamic_accent")
        val SONG_SORT_ORDER = stringPreferencesKey("song_sort_order")
        val LIBRARY_VIEW_STYLE = stringPreferencesKey("library_view_style")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val CROSSFADE_MS = intPreferencesKey("crossfade_ms")
        val RESUME_ON_LAUNCH = booleanPreferencesKey("resume_on_launch")
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val LAST_SONG_ID = longPreferencesKey("last_song_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val LAST_QUEUE_IDS = stringPreferencesKey("last_queue_ids")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val EQ_PRESET = stringPreferencesKey("eq_preset")
        val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
        val BALANCE = floatPreferencesKey("balance")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
    }

    val theme: Flow<AppTheme> = context.dataStore.data.map {
        runCatching { AppTheme.valueOf(it[Keys.THEME] ?: AppTheme.SYSTEM_DYNAMIC.name) }
            .getOrDefault(AppTheme.SYSTEM_DYNAMIC)
    }
    suspend fun setTheme(theme: AppTheme) = edit { it[Keys.THEME] = theme.name }

    val useDynamicAccent: Flow<Boolean> = context.dataStore.data.map { it[Keys.USE_DYNAMIC_ACCENT] ?: true }
    suspend fun setUseDynamicAccent(enabled: Boolean) = edit { it[Keys.USE_DYNAMIC_ACCENT] = enabled }

    val accentColorArgb: Flow<Int?> = context.dataStore.data.map { it[Keys.ACCENT_COLOR_ARGB] }
    suspend fun setAccentColorArgb(argb: Int) = edit { it[Keys.ACCENT_COLOR_ARGB] = argb }

    val songSortOrder: Flow<SongSortOrder> = context.dataStore.data.map {
        runCatching { SongSortOrder.valueOf(it[Keys.SONG_SORT_ORDER] ?: SongSortOrder.TITLE.name) }
            .getOrDefault(SongSortOrder.TITLE)
    }
    suspend fun setSongSortOrder(order: SongSortOrder) = edit { it[Keys.SONG_SORT_ORDER] = order.name }

    val libraryViewStyle: Flow<LibraryViewStyle> = context.dataStore.data.map {
        runCatching { LibraryViewStyle.valueOf(it[Keys.LIBRARY_VIEW_STYLE] ?: LibraryViewStyle.GRID.name) }
            .getOrDefault(LibraryViewStyle.GRID)
    }
    suspend fun setLibraryViewStyle(style: LibraryViewStyle) = edit { it[Keys.LIBRARY_VIEW_STYLE] = style.name }

    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.map { it[Keys.GAPLESS_PLAYBACK] ?: true }
    suspend fun setGaplessPlayback(enabled: Boolean) = edit { it[Keys.GAPLESS_PLAYBACK] = enabled }

    val crossfadeMs: Flow<Int> = context.dataStore.data.map { it[Keys.CROSSFADE_MS] ?: 0 }
    suspend fun setCrossfadeMs(ms: Int) = edit { it[Keys.CROSSFADE_MS] = ms.coerceIn(0, 12_000) }

    val resumeOnLaunch: Flow<Boolean> = context.dataStore.data.map { it[Keys.RESUME_ON_LAUNCH] ?: true }
    suspend fun setResumeOnLaunch(enabled: Boolean) = edit { it[Keys.RESUME_ON_LAUNCH] = enabled }

    val repeatMode: Flow<RepeatMode> = context.dataStore.data.map {
        runCatching { RepeatMode.valueOf(it[Keys.REPEAT_MODE] ?: RepeatMode.OFF.name) }
            .getOrDefault(RepeatMode.OFF)
    }
    suspend fun setRepeatMode(mode: RepeatMode) = edit { it[Keys.REPEAT_MODE] = mode.name }

    val shuffleEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHUFFLE_ENABLED] ?: false }
    suspend fun setShuffleEnabled(enabled: Boolean) = edit { it[Keys.SHUFFLE_ENABLED] = enabled }

    suspend fun savePlaybackState(songId: Long, positionMs: Long, queueIds: List<Long>) = edit {
        it[Keys.LAST_SONG_ID] = songId
        it[Keys.LAST_POSITION_MS] = positionMs
        it[Keys.LAST_QUEUE_IDS] = queueIds.joinToString(",")
    }
    data class SavedPlaybackState(val songId: Long?, val positionMs: Long, val queueIds: List<Long>)
    val savedPlaybackState: Flow<SavedPlaybackState> = context.dataStore.data.map { prefs ->
        SavedPlaybackState(
            songId = prefs[Keys.LAST_SONG_ID],
            positionMs = prefs[Keys.LAST_POSITION_MS] ?: 0L,
            queueIds = prefs[Keys.LAST_QUEUE_IDS]?.takeIf { it.isNotBlank() }
                ?.split(",")?.mapNotNull { id -> id.toLongOrNull() } ?: emptyList()
        )
    }

    val excludedFolders: Flow<Set<String>> = context.dataStore.data.map { it[Keys.EXCLUDED_FOLDERS] ?: emptySet() }
    suspend fun setExcludedFolders(paths: Set<String>) = edit { it[Keys.EXCLUDED_FOLDERS] = paths }

    val eqPreset: Flow<String> = context.dataStore.data.map { it[Keys.EQ_PRESET] ?: "Normal" }
    suspend fun setEqPreset(preset: String) = edit { it[Keys.EQ_PRESET] = preset }

    val bassBoostStrength: Flow<Int> = context.dataStore.data.map { it[Keys.BASS_BOOST_STRENGTH] ?: 0 }
    suspend fun setBassBoostStrength(strength: Int) = edit { it[Keys.BASS_BOOST_STRENGTH] = strength.coerceIn(0, 1000) }

    val virtualizerStrength: Flow<Int> = context.dataStore.data.map { it[Keys.VIRTUALIZER_STRENGTH] ?: 0 }
    suspend fun setVirtualizerStrength(strength: Int) = edit { it[Keys.VIRTUALIZER_STRENGTH] = strength.coerceIn(0, 1000) }

    val balance: Flow<Float> = context.dataStore.data.map { it[Keys.BALANCE] ?: 0f }
    suspend fun setBalance(value: Float) = edit { it[Keys.BALANCE] = value.coerceIn(-1f, 1f) }

    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ANIMATIONS_ENABLED] ?: true }
    suspend fun setAnimationsEnabled(enabled: Boolean) = edit { it[Keys.ANIMATIONS_ENABLED] = enabled }

    private suspend fun edit(transform: (MutablePreferences) -> Unit) {
        context.dataStore.edit(transform)
    }
}
