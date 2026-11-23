package ru.limonella.alias.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Приложение всегда тёмное — так задумано стилем (фоны-картинки ночные).
private val AliasColors = darkColorScheme(
    primary = Amber,
    onPrimary = AmberInk,
    primaryContainer = Color(0xFF3A2C12),
    onPrimaryContainer = AmberLight,
    secondary = GraphiteHigh,
    onSecondary = MoonSilver,
    secondaryContainer = GraphiteHigh,
    onSecondaryContainer = MoonSilver,
    tertiary = Color(0xFF8FB8D8),
    onTertiary = Graphite,
    background = Graphite,
    onBackground = MoonSilver,
    surface = GraphiteRaised,
    onSurface = MoonSilver,
    surfaceVariant = GraphiteHigh,
    onSurfaceVariant = FogGray,
    surfaceContainer = GraphiteRaised,
    surfaceContainerHigh = GraphiteHigh,
    surfaceContainerHighest = GraphiteHigh,
    outline = Color(0xFF4A505A),
    outlineVariant = Color(0xFF353A42),
    error = GameColors.Skipped,
    onError = Color.White,
)

@Composable
fun AliasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AliasColors,
        typography = AliasTypography,
        content = content,
    )
}
