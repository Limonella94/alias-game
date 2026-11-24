package ru.limonella.alias.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.limonella.alias.ui.theme.InkBrown

/**
 * Карточка-картинка для детского режима и подпись под ней.
 * Если у слова есть нарисованная картинка ([image] — путь в assets) — показываем её, иначе — крупный эмодзи.
 * [onLight] — карточка светлая (детский режим): картинка без рамки, подпись тёмная.
 * На тёмной карточке картинка лежит на белой «наклейке».
 */
@Composable
fun WordPicture(
    emoji: String?,
    text: String,
    modifier: Modifier = Modifier,
    image: String? = null,
    emojiSize: TextUnit = 96.sp,
    imageSize: Dp = 170.dp,
    showCaption: Boolean = true,
    onLight: Boolean = false,
) {
    val bitmap by rememberAssetImage(image)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        val picture = bitmap
        when {
            picture != null && onLight -> Image(
                bitmap = picture,
                contentDescription = text,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(imageSize),
            )

            picture != null -> Box(
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = picture,
                    contentDescription = text,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(imageSize)
                        .padding(6.dp),
                )
            }

            // Пока картинка грузится — держим место, чтобы карточка не прыгала.
            image != null -> Box(Modifier.size(imageSize))

            else -> Text(emoji ?: "❓", fontSize = emojiSize, textAlign = TextAlign.Center)
        }
        if (showCaption) {
            Text(
                text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (onLight) InkBrown else Color.Unspecified,
            )
        }
    }
}
