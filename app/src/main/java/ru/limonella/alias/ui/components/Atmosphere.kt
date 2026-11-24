package ru.limonella.alias.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import ru.limonella.alias.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.limonella.alias.ui.theme.Amber
import ru.limonella.alias.ui.theme.Glass
import ru.limonella.alias.ui.theme.GlassBorder
import ru.limonella.alias.ui.theme.Graphite

/**
 * Фон экрана: картинка во весь экран + затемнение, чтобы текст и кнопки читались.
 * [scrim] — насколько сильно затемнять (0 — не затемнять, 1 — почти чёрный низ).
 */
@Composable
fun ScreenBackground(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    scrim: Float = 0.5f,
    grain: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val grainImage = ImageBitmap.imageResource(R.drawable.grain)
    val grainBrush = remember(grainImage) {
        ShaderBrush(ImageShader(grainImage, TileMode.Repeated, TileMode.Repeated))
    }
    Box(
        modifier
            .fillMaxSize()
            .background(Graphite),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Graphite.copy(alpha = scrim * 0.35f),
                        0.45f to Graphite.copy(alpha = scrim * 0.6f),
                        1f to Graphite.copy(alpha = (scrim + 0.25f).coerceAtMost(0.95f)),
                    ),
                ),
        )
        // Лёгкое «плёночное зерно»: прячет мягкость картинок и добавляет фактуры.
        if (grain) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(grainBrush),
            )
        }
        content()
    }
}

/**
 * Карточка из «матового стекла»: полупрозрачный графит и тонкая светлая рамка.
 * [highlighted] — янтарная рамка (выбранное, текущая команда).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = Glass,
    highlighted: Boolean = false,
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val border = if (highlighted) {
        BorderStroke(2.dp, Amber.copy(alpha = 0.85f))
    } else {
        BorderStroke(borderWidth, borderColor ?: GlassBorder)
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = color,
            border = border,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            border = border,
            content = content,
        )
    }
}
