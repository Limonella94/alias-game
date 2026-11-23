package ru.limonella.alias.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Шрифты. Заголовки — с засечками (фэнтези-вид), остальное — без засечек.
 * Сейчас системные; когда появятся Playfair Display и Manrope (.ttf в res/font) —
 * заменить [TitleFont] и [BodyFont] на FontFamily(Font(R.font....)), остальное не трогать.
 */
val TitleFont: FontFamily = FontFamily.Serif
val BodyFont: FontFamily = FontFamily.SansSerif

private val base = Typography()

val AliasTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = TitleFont, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = TitleFont, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = TitleFont, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = TitleFont, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = TitleFont, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = TitleFont, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = BodyFont),
    bodyLarge = base.bodyLarge.copy(fontFamily = BodyFont),
    bodyMedium = base.bodyMedium.copy(fontFamily = BodyFont),
    bodySmall = base.bodySmall.copy(fontFamily = BodyFont),
    labelLarge = base.labelLarge.copy(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = BodyFont),
    labelSmall = base.labelSmall.copy(fontFamily = BodyFont),
)
