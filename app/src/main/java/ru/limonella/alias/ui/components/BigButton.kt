package ru.limonella.alias.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.limonella.alias.ui.theme.Amber
import ru.limonella.alias.ui.theme.AmberInk
import ru.limonella.alias.ui.theme.Glass
import ru.limonella.alias.ui.theme.GlassBorder
import ru.limonella.alias.ui.theme.MoonSilver

private val ButtonShape = RoundedCornerShape(20.dp)

/**
 * Главная крупная кнопка: «светящаяся» заливка с мягким свечением своего цвета.
 * По умолчанию — янтарь фонаря. Используется на всех экранах — вид меняем здесь.
 */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Amber,
    contentColor: Color = AmberInk,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .shadow(
                elevation = if (enabled) 14.dp else 0.dp,
                shape = ButtonShape,
                ambientColor = containerColor,
                spotColor = containerColor,
            ),
        shape = ButtonShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = contentColor,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(lerp(containerColor, Color.White, 0.18f), containerColor, lerp(containerColor, Color.Black, 0.15f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/** Второстепенная кнопка из «стекла» с серебристой рамкой. */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = ButtonShape,
        border = BorderStroke(1.dp, GlassBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Glass,
            contentColor = MoonSilver,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
