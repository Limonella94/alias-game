package ru.limonella.alias.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.limonella.alias.BuildConfig
import ru.limonella.alias.R
import ru.limonella.alias.data.session.SavedGame
import ru.limonella.alias.di.appContainer
import ru.limonella.alias.ui.components.GlassCard
import ru.limonella.alias.ui.components.ScreenBackground
import ru.limonella.alias.ui.theme.Amber
import ru.limonella.alias.ui.theme.AmberInk
import ru.limonella.alias.ui.theme.FogGray
import ru.limonella.alias.ui.theme.MoonSilver

@Composable
fun HomeScreen(
    onPlay: () -> Unit,
    onKids: () -> Unit,
    onContinueGame: () -> Unit,
) {
    val container = appContainer()
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(container.updateManager, container.savedGameStore, container.gameSessionStore)
    }
    val update by viewModel.update.collectAsStateWithLifecycle()
    val savedGame by viewModel.savedGame.collectAsStateWithLifecycle()

    ScreenBackground(image = R.drawable.bg_home, scrim = 0.35f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UpdateBanner(update = update, onClick = viewModel::onUpdateClick)
            // Верх экрана — картинка с замком; заголовок ниже неё, на тумане.
            Spacer(Modifier.height(200.dp))
            Text(
                text = "Alias",
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = Shadow(color = Amber.copy(alpha = 0.55f), blurRadius = 28f),
                ),
                color = MoonSilver,
            )
            Text(
                text = "Объясни слово, не называя его",
                style = MaterialTheme.typography.titleMedium,
                color = FogGray,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            val saved = savedGame
            if (saved != null) {
                ContinueCard(
                    saved = saved,
                    onContinue = {
                        viewModel.continueGame(saved)
                        onContinueGame()
                    },
                    onDiscard = viewModel::discardSavedGame,
                )
                Spacer(Modifier.height(16.dp))
            }

            ModeCard(
                image = R.drawable.card_play,
                title = "Играть",
                subtitle = "Команды, таймер, очки",
                onClick = onPlay,
            )
            Spacer(Modifier.height(16.dp))
            ModeCard(
                image = R.drawable.card_kids,
                title = "Детский режим",
                subtitle = "Малыши 3–5 и детки 5–6",
                onClick = onKids,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Версия ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = FogGray.copy(alpha = 0.7f),
            )
        }
    }
}

/** Карточка режима: иллюстрация во всю карточку, текст слева на тёмной части картинки. */
@Composable
private fun ModeCard(
    @DrawableRes image: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.65f),
                            0.55f to Color.Black.copy(alpha = 0.15f),
                            1f to Color.Transparent,
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 24.dp),
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = MoonSilver)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = FogGray)
            }
        }
    }
}

/** Карточка незаконченной игры: счёт и кнопки «Продолжить» / «Удалить». */
@Composable
private fun ContinueCard(saved: SavedGame, onContinue: () -> Unit, onDiscard: () -> Unit) {
    val state = saved.state
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        highlighted = true,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "Незаконченная игра · раунд ${state.roundNumber}",
                style = MaterialTheme.typography.titleMedium,
                color = MoonSilver,
            )
            Text(
                state.config.teams.joinToString("  ·  ") { "${it.name} ${state.scores[it.id] ?: 0}" },
                style = MaterialTheme.typography.bodyMedium,
                color = FogGray,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = AmberInk),
                ) { Text("Продолжить", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onDiscard) { Text("Удалить", color = FogGray) }
            }
        }
    }
}

/** Баннер «Доступно обновление». Пока обновлений нет — ничего не показывает. */
@Composable
private fun UpdateBanner(update: UpdateUi, onClick: () -> Unit) {
    val info = when (update) {
        UpdateUi.None -> return
        is UpdateUi.Available -> update.info
        is UpdateUi.Downloading -> update.info
        is UpdateUi.Ready -> update.info
        is UpdateUi.Failed -> update.info
    }
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        highlighted = true,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Доступна новая версия ${info.versionName}",
                style = MaterialTheme.typography.titleMedium,
                color = MoonSilver,
            )
            if (info.notes.isNotBlank()) {
                Text(info.notes, style = MaterialTheme.typography.bodySmall, color = FogGray, maxLines = 2)
            }
            Spacer(Modifier.height(8.dp))
            when (update) {
                is UpdateUi.Downloading -> {
                    LinearProgressIndicator(
                        progress = { update.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is UpdateUi.Failed -> {
                    Text(update.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    UpdateButton("Повторить", onClick)
                }
                is UpdateUi.Ready -> UpdateButton("Установить", onClick)
                else -> UpdateButton("Обновить", onClick)
            }
        }
    }
}

@Composable
private fun UpdateButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = AmberInk),
    ) { Text(text, fontWeight = FontWeight.Bold) }
}
