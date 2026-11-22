package ru.limonella.alias.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Grape,
    onPrimary = Color.White,
    primaryContainer = GrapeContainer,
    onPrimaryContainer = Ink,
    secondary = Coral,
    onSecondary = Color.White,
    tertiary = Sun,
    onTertiary = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
)

private val DarkColors = darkColorScheme(
    primary = GrapeLight,
    onPrimary = Ink,
    primaryContainer = GrapeContainerDark,
    onPrimaryContainer = Mist,
    secondary = Coral,
    onSecondary = Ink,
    tertiary = Sun,
    onTertiary = Ink,
    background = Night,
    onBackground = Mist,
    surface = NightSurface,
    onSurface = Mist,
)

@Composable
fun AliasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AliasTypography,
        content = content,
    )
}
