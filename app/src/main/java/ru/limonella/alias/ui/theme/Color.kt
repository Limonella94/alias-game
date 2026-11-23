package ru.limonella.alias.ui.theme

import androidx.compose.ui.graphics.Color

// Стиль «мрачное фэнтези»: графит, лунное серебро, туман и тёплый янтарь фонаря.
// Меняем вид всего приложения здесь.

/** Фон: графит. */
val Graphite = Color(0xFF121417)
val GraphiteRaised = Color(0xFF1C1F24)
val GraphiteHigh = Color(0xFF2A2E35)

/** Текст: лунное серебро и туманный серый. */
val MoonSilver = Color(0xFFD9DEE5)
val FogGray = Color(0xFF9AA3AE)

/** Акцент: янтарь фонаря. */
val Amber = Color(0xFFF2A541)
val AmberLight = Color(0xFFF7C77A)
val AmberDeep = Color(0xFFC97E1E)
val AmberInk = Color(0xFF1B1305)

/** «Матовое стекло» карточек поверх фона. */
val Glass = Color(0xFF1C1F24).copy(alpha = 0.78f)
val GlassStrong = Color(0xFF16181C).copy(alpha = 0.90f)
val GlassBorder = Color.White.copy(alpha = 0.12f)

/** Цвета игровых действий. */
object GameColors {
    val Guessed = Color(0xFF4CAF7A)
    val Skipped = Color(0xFFE05A47)
}
