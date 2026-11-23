package ru.limonella.alias.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/** Крупный текст слова; длинные слова — чуть мельче, чтобы не переносились. */
@Composable
fun WordText(text: String) {
    Text(
        text = text,
        style = if (text.length > 10) {
            MaterialTheme.typography.headlineLarge
        } else {
            MaterialTheme.typography.displaySmall
        },
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}
