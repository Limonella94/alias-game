package ru.limonella.alias.feature.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.limonella.alias.core.engine.Phase
import ru.limonella.alias.di.appContainer
import ru.limonella.alias.R
import ru.limonella.alias.ui.components.ScreenBackground
import ru.limonella.alias.core.model.GameMode

@Composable
fun GameScreen(
    onExitToMenu: () -> Unit,
    onNewGame: () -> Unit,
    onRematch: () -> Unit,
) {
    val container = appContainer()
    val viewModel: GameViewModel = viewModel {
        GameViewModel(
            sessionStore = container.gameSessionStore,
            wordRepository = container.wordRepository,
            soundPlayer = container.soundPlayer,
            savedGames = container.savedGameStore,
            wordHistory = container.wordHistory,
        )
    }
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    // Лёгкая вибрация, чтобы объясняющий чувствовал, что нажатие/свайп засчитан.
    val vibrate = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

    KeepScreenOn()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.pauseIfPlaying() }
    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Выйти в меню?") },
            text = { Text("Игра сохранится — её можно будет продолжить с главного экрана.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onExitToMenu()
                }) { Text("В меню") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Остаться") }
            },
        )
    }

    val background = if (ui.game?.config?.mode == GameMode.KIDS) R.drawable.bg_kids else R.drawable.bg_game
    ScreenBackground(image = background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(20.dp),
        ) {
            val error = ui.error
            val game = ui.game
            when {
                error != null -> MessageContent(message = error, buttonText = "В меню", onClick = onExitToMenu)

                game == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                else -> when (game.phase) {
                    Phase.READY -> ReadyContent(game = game, onStart = viewModel::startRound)

                    Phase.PLAYING -> RoundContent(
                        game = game,
                        secondsLeft = ui.secondsLeft,
                        paused = ui.paused,
                        onGuessed = {
                            vibrate()
                            viewModel.markGuessed()
                        },
                        onSkipped = {
                            vibrate()
                            viewModel.markSkipped()
                        },
                        onTogglePause = viewModel::togglePause,
                        onFinishRound = viewModel::finishRound,
                    )

                    Phase.LAST_WORD -> LastWordContent(
                        game = game,
                        onResolve = { teamId ->
                            vibrate()
                            viewModel.resolveLastWord(teamId)
                        },
                    )

                    Phase.REVIEW -> ReviewContent(
                        game = game,
                        onToggle = viewModel::toggleResult,
                        onLastWordTeam = viewModel::setLastWordTeam,
                        onConfirm = viewModel::confirmRound,
                    )

                    Phase.FINISHED -> FinishedContent(
                        game = game,
                        onRematch = onRematch,
                        onNewGame = onNewGame,
                        onMenu = onExitToMenu,
                    )
                }
            }
        }
    }
}

/** Не даём экрану гаснуть, пока открыта игра. */
@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
