package com.bgx.music.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.bgx.music.data.preferences.AppTheme

private fun lightScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.15f),
    onPrimaryContainer = accent,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = ErrorRed
)

private fun darkScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.25f),
    onPrimaryContainer = BGXAccentLight,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed
)

private fun amoledScheme(accent: Color) = darkScheme(accent).copy(
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant
)

@Composable
fun BGXMusicTheme(
    appTheme: AppTheme,
    accentColor: Color = BGXAccent,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        appTheme == AppTheme.SYSTEM_DYNAMIC && useDynamicColor && supportsDynamic ->
            if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        appTheme == AppTheme.SYSTEM_DYNAMIC ->
            if (systemDark) darkScheme(accentColor) else lightScheme(accentColor)

        appTheme == AppTheme.LIGHT -> lightScheme(accentColor)
        appTheme == AppTheme.DARK -> darkScheme(accentColor)
        appTheme == AppTheme.AMOLED -> amoledScheme(accentColor)
        else -> darkScheme(accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BGXTypography,
        content = content
    )
}
