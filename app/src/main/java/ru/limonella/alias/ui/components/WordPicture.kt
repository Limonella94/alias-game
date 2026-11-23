package ru.limonella.alias.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Карточка-картинка для детского режима: крупный эмодзи и подпись под ним.
 * На этапе визуала сюда добавим показ нарисованной картинки (поле Word.image) вместо эмодзи.
 */
@Composable
fun WordPicture(
    emoji: String,
    text: String,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 96.sp,
    showCaption: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text(emoji, fontSize = emojiSize, textAlign = TextAlign.Center)
        if (showCaption) {
            Text(
                text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
