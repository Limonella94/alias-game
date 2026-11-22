package ru.limonella.alias.feature.game

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.limonella.alias.core.engine.GameState
import kotlin.math.roundToInt

@Composable
internal fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

/** Таблица счёта. [highlightTeamId] — чью строку подсветить. */
@Composable
internal fun ScoreBoard(
    game: GameState,
    highlightTeamId: Int?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        game.config.rules.pointsToWin?.let {
            Text(
                "Играем до $it очков",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        game.config.teams.forEach { team ->
            val highlighted = team.id == highlightTeamId
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(team.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(
                        "${game.scores[team.id] ?: 0}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * Карточка со словом. Свайп вверх — угадали, вниз — пропуск.
 * [wordKey] сбрасывает положение карточки при смене слова.
 */
@Composable
internal fun SwipeableWordCard(
    text: String,
    wordKey: String,
    onGuessed: () -> Unit,
    onSkipped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnGuessed by rememberUpdatedState(onGuessed)
    val currentOnSkipped by rememberUpdatedState(onSkipped)
    var offsetY by remember(wordKey) { mutableFloatStateOf(0f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .pointerInput(wordKey) {
                val threshold = 100.dp.toPx()
                detectVerticalDragGestures(
                    onDragEnd = {
                        val y = offsetY
                        offsetY = 0f
                        when {
                            y < -threshold -> currentOnGuessed()
                            y > threshold -> currentOnSkipped()
                        }
                    },
                    onDragCancel = { offsetY = 0f },
                ) { change, dragAmount ->
                    change.consume()
                    offsetY += dragAmount
                }
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            WordText(text)
        }
    }
}

/** Крупный текст слова; длинные слова — чуть мельче, чтобы не переносились. */
@Composable
internal fun WordText(text: String) {
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

internal fun formatSeconds(seconds: Int): String =
    "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"

internal fun formatPoints(points: Int): String = if (points > 0) "+$points" else "$points"
