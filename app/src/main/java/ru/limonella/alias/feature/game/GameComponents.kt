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
import ru.limonella.alias.ui.components.WordPicture
import ru.limonella.alias.ui.components.WordText
import kotlin.math.roundToInt
import ru.limonella.alias.ui.components.GlassCard
import ru.limonella.alias.ui.theme.GlassStrong
import ru.limonella.alias.ui.theme.GlassBorder
import androidx.compose.foundation.BorderStroke
import ru.limonella.alias.ui.theme.Parchment
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.abs
import ru.limonella.alias.ui.theme.GameColors

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
            GlassCard(
                shape = RoundedCornerShape(16.dp),
                highlighted = highlighted,
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
 * Пока тянешь: карточка наклоняется и подсвечивается зелёным (вверх) или красным (вниз),
 * появляется подсказка «Угадано» / «Пропуск»; на пороге — лёгкая вибрация.
 * Отпустил до порога — карточка мягко возвращается на место.
 * [wordKey] сбрасывает положение карточки при смене слова.
 */
@Composable
internal fun SwipeableWordCard(
    text: String,
    wordKey: String,
    onGuessed: () -> Unit,
    onSkipped: () -> Unit,
    modifier: Modifier = Modifier,
    /** Если задан — карточка с картинкой (детский режим). */
    emoji: String? = null,
    /** Нарисованная картинка (путь в assets) — показывается вместо эмодзи. */
    image: String? = null,
) {
    val picture = emoji != null || image != null
    val currentOnGuessed by rememberUpdatedState(onGuessed)
    val currentOnSkipped by rememberUpdatedState(onSkipped)
    val haptic = LocalHapticFeedback.current
    val thresholdPx = with(LocalDensity.current) { SWIPE_THRESHOLD.toPx() }

    var offsetY by remember(wordKey) { mutableFloatStateOf(0f) }
    var dragging by remember(wordKey) { mutableStateOf(false) }
    // Во время перетаскивания карточка идёт за пальцем, после — пружинит обратно.
    val shownOffset by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = if (dragging) snap() else spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cardOffset",
    )
    val progress = (shownOffset / thresholdPx).coerceIn(-1f, 1f)
    val strength = abs(progress)
    val feedbackColor = if (progress < 0f) GameColors.Guessed else GameColors.Skipped

    val pastThreshold = abs(offsetY) >= thresholdPx
    LaunchedEffect(pastThreshold) {
        if (pastThreshold) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (picture) 300.dp else 240.dp)
            // Жест ловим на месте карточки, до сдвига и наклона — так перетаскивание ровное.
            .pointerInput(wordKey) {
                detectVerticalDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = {
                        val y = offsetY
                        dragging = false
                        offsetY = 0f
                        when {
                            y <= -thresholdPx -> currentOnGuessed()
                            y >= thresholdPx -> currentOnSkipped()
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        offsetY = 0f
                    },
                ) { change, dragAmount ->
                    change.consume()
                    offsetY += dragAmount
                }
            }
            .offset { IntOffset(0, shownOffset.roundToInt()) }
            .graphicsLayer {
                rotationZ = progress * 3f
                val scale = 1f - 0.04f * strength
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = if (picture) Parchment else GlassStrong),
        border = when {
            strength > 0.02f -> BorderStroke(3.dp, feedbackColor.copy(alpha = 0.4f + 0.6f * strength))
            picture -> null
            else -> BorderStroke(1.dp, GlassBorder)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (picture) {
                    WordPicture(emoji = emoji, text = text, image = image, onLight = true)
                } else {
                    WordText(text)
                }
            }
            // Цветная подсветка поверх карточки — тем сильнее, чем дальше утащили.
            if (strength > 0.02f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                if (progress < 0f) {
                                    listOf(feedbackColor.copy(alpha = 0.55f * strength), Color.Transparent)
                                } else {
                                    listOf(Color.Transparent, feedbackColor.copy(alpha = 0.55f * strength))
                                },
                            ),
                        ),
                )
            }
            if (strength > 0.15f) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = feedbackColor.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(if (progress < 0f) Alignment.TopCenter else Alignment.BottomCenter)
                        .padding(14.dp)
                        .graphicsLayer { alpha = ((strength - 0.15f) / 0.5f).coerceIn(0f, 1f) },
                ) {
                    Text(
                        if (progress < 0f) "✓  Угадано" else "✕  Пропуск",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

private val SWIPE_THRESHOLD = 100.dp

internal fun formatSeconds(seconds: Int): String =
    "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"

internal fun formatPoints(points: Int): String = if (points > 0) "+$points" else "$points"
