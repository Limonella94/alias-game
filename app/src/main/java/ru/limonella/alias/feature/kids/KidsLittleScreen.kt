package ru.limonella.alias.feature.kids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.limonella.alias.di.appContainer
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.ui.components.WordPicture
import ru.limonella.alias.ui.theme.GameColors
import ru.limonella.alias.R
import ru.limonella.alias.ui.components.ScreenBackground
import ru.limonella.alias.ui.theme.GlassStrong
import ru.limonella.alias.ui.theme.GlassBorder
import androidx.compose.foundation.BorderStroke
import ru.limonella.alias.ui.theme.Parchment
import ru.limonella.alias.ui.components.GlassCard

/**
 * «Малыши 3–5»: взрослый держит телефон, видит картинку и подпись,
 * объясняет звуками и жестами. Огромные кнопки, без таймера, общий счёт.
 */
@Composable
fun KidsLittleScreen(onExit: () -> Unit) {
    val container = appContainer()
    val viewModel: KidsLittleViewModel = viewModel {
        KidsLittleViewModel(container.wordRepository, container.soundPlayer, container.wordHistory)
    }
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Экран не гаснет, пока играем.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    ScreenBackground(image = R.drawable.bg_kids) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(20.dp),
        ) {
            val error = ui.error
            val word = ui.word
            when {
                error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(error, textAlign = TextAlign.Center)
                    TextButton(onClick = onExit) { Text("Назад") }
                }

                ui.finished -> LittleResults(
                    guessed = ui.guessed,
                    words = ui.guessedWords,
                    onAgain = viewModel::start,
                    onExit = onExit,
                )

                word == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                else -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "⭐ ${ui.guessed}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        GlassCard(color = GlassStrong, shape = RoundedCornerShape(50)) {
                            TextButton(onClick = viewModel::finish) { Text("Закончить") }
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(40.dp),
                        colors = CardDefaults.cardColors(containerColor = Parchment),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            WordPicture(
                                emoji = word.emoji,
                                image = word.image,
                                imageSize = 260.dp,
                                onLight = true,
                                text = word.text,
                                emojiSize = 160.sp,
                            )
                        }
                    }
                    Text(
                        "Покажи звуками и жестами — не называя слово!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.guessed()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GameColors.Guessed, contentColor = Color.White),
                    ) {
                        Text("Угадали! 🎉", fontSize = 32.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = viewModel::next,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text("Дальше ➡️", fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LittleResults(
    guessed: Int,
    words: List<Word>,
    onAgain: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("🎉", fontSize = 96.sp)
        Text(
            if (guessed > 0) "Мы угадали $guessed!" else "Попробуем ещё?",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 64.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(words) { w ->
                WordPicture(
                    emoji = w.emoji,
                    text = w.text,
                    image = w.image,
                    emojiSize = 44.sp,
                    imageSize = 64.dp,
                    showCaption = false,
                )
            }
        }
        Button(
            onClick = onAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(28.dp),
        ) { Text("Ещё раз!", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) { Text("В меню") }
    }
}
