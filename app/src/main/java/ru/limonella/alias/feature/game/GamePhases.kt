package ru.limonella.alias.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.limonella.alias.core.engine.GameState
import ru.limonella.alias.core.engine.Outcome
import ru.limonella.alias.core.engine.WordResult
import ru.limonella.alias.core.engine.pointsFor
import ru.limonella.alias.core.model.LastWordRule
import ru.limonella.alias.core.model.RuleSet
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.ui.components.BigButton
import ru.limonella.alias.ui.components.WordText
import ru.limonella.alias.ui.theme.GameColors

// Экраны для каждого этапа игры (см. Phase).

@Composable
internal fun ReadyContent(game: GameState, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Раунд ${game.roundNumber}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Text("Объясняет команда", style = MaterialTheme.typography.titleLarge)
        Text(
            game.currentTeam.name,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        ScoreBoard(game, highlightTeamId = game.currentTeam.id, modifier = Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        BigButton("Старт!", onStart)
    }
}

@Composable
internal fun RoundContent(
    game: GameState,
    secondsLeft: Int?,
    paused: Boolean,
    onGuessed: () -> Unit,
    onSkipped: () -> Unit,
    onTogglePause: () -> Unit,
    onFinishRound: () -> Unit,
) {
    val guessed = game.roundResults.count { it.outcome == Outcome.GUESSED }
    val skipped = game.roundResults.size - guessed

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                game.currentTeam.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (secondsLeft == null) {
                TextButton(onClick = onFinishRound) { Text("Закончить") }
            }
            TextButton(onClick = onTogglePause) { Text(if (paused) "Продолжить" else "Пауза") }
        }
        Text(
            text = secondsLeft?.let(::formatSeconds) ?: "∞",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = if (secondsLeft != null && secondsLeft <= 10) {
                GameColors.Skipped
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )
        Text("✓ $guessed     ✗ $skipped", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val word = game.currentWord
            if (paused || word == null) {
                // На паузе слово прячем, чтобы никто не подсмотрел.
                Text(
                    "Пауза",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                SwipeableWordCard(
                    text = word.text,
                    wordKey = word.id + "#" + game.roundResults.size,
                    onGuessed = onGuessed,
                    onSkipped = onSkipped,
                )
            }
        }

        Text(
            "Свайп вверх — угадали, вниз — пропуск",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigButton(
                text = "Пропуск",
                onClick = onSkipped,
                modifier = Modifier.weight(1f),
                containerColor = GameColors.Skipped,
                contentColor = Color.White,
                enabled = !paused,
            )
            BigButton(
                text = "Угадано",
                onClick = onGuessed,
                modifier = Modifier.weight(1f),
                containerColor = GameColors.Guessed,
                contentColor = Color.White,
                enabled = !paused,
            )
        }
    }
}

@Composable
internal fun LastWordContent(game: GameState, onResolve: (Int?) -> Unit) {
    if (game.config.rules.lastWord == LastWordRule.OWN_TEAM) {
        OwnTeamLastWord(game, onResolve)
    } else {
        AnyTeamLastWord(game, onResolve)
    }
}

/** Объясняющий договаривает слово без ограничения времени, отгадывает только своя команда. */
@Composable
private fun OwnTeamLastWord(game: GameState, onResolve: (Int?) -> Unit) {
    val word = game.currentWord
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Время вышло!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = GameColors.Skipped,
        )
        Text(
            "Последнее слово — объясняй до конца, время не ограничено",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            "Отгадывает только команда «${game.currentTeam.name}»",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (word != null) {
                SwipeableWordCard(
                    text = word.text,
                    wordKey = "last#" + word.id,
                    onGuessed = { onResolve(game.currentTeam.id) },
                    onSkipped = { onResolve(null) },
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigButton(
                text = "Не угадали",
                onClick = { onResolve(null) },
                modifier = Modifier.weight(1f),
                containerColor = GameColors.Skipped,
                contentColor = Color.White,
            )
            BigButton(
                text = "Угадали",
                onClick = { onResolve(game.currentTeam.id) },
                modifier = Modifier.weight(1f),
                containerColor = GameColors.Guessed,
                contentColor = Color.White,
            )
        }
    }
}

/** Последнее слово может отгадать любая команда. */
@Composable
private fun AnyTeamLastWord(game: GameState, onResolve: (Int?) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Время вышло!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = GameColors.Skipped,
        )
        Text(
            "Последнее слово может отгадать любая команда",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                WordText(game.currentWord?.text.orEmpty())
            }
        }
        Text("Кто отгадал?", style = MaterialTheme.typography.titleLarge)
        game.config.teams.forEach { team ->
            BigButton(team.name, onClick = { onResolve(team.id) })
        }
        OutlinedButton(
            onClick = { onResolve(null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) { Text("Никто") }
    }
}

@Composable
internal fun ReviewContent(
    game: GameState,
    onToggle: (Int) -> Unit,
    onLastWordTeam: (Int?) -> Unit,
    onConfirm: () -> Unit,
) {
    val rules = game.config.rules
    Column(Modifier.fillMaxSize()) {
        Text("Итоги раунда", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "${game.currentTeam.name}: ${formatPoints(game.roundPoints)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "Нажмите на слово, чтобы исправить отметку",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(game.roundResults) { index, result ->
                ResultRow(result = result, rules = rules, onClick = { onToggle(index) })
            }
            val lastWord = game.lastWord
            if (lastWord != null) {
                item { LastWordPicker(word = lastWord, game = game, onSelect = onLastWordTeam) }
            }
            if (game.roundResults.isEmpty() && lastWord == null) {
                item { Text("В этом раунде слов не было", style = MaterialTheme.typography.bodyLarge) }
            }
        }
        Spacer(Modifier.height(12.dp))
        BigButton("Дальше", onConfirm)
    }
}

@Composable
private fun ResultRow(result: WordResult, rules: RuleSet, onClick: () -> Unit) {
    val guessed = result.outcome == Outcome.GUESSED
    val color = if (guessed) GameColors.Guessed else GameColors.Skipped
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (guessed) "✓" else "✗",
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(16.dp))
            Text(result.word.text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                formatPoints(rules.pointsFor(result.outcome)),
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
    }
}

@Composable
private fun LastWordPicker(word: Word, game: GameState, onSelect: (Int?) -> Unit) {
    val teams = if (game.config.rules.lastWord == LastWordRule.OWN_TEAM) {
        listOf(game.currentTeam)
    } else {
        game.config.teams
    }
    val options: List<Pair<Int?, String>> =
        teams.map { it.id to it.name } + listOf(null to "Никто")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Последнее слово: ${word.text}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Очко получает:", style = MaterialTheme.typography.bodyMedium)
        options.forEach { (teamId, name) ->
            FilterChip(
                selected = game.lastWordTeamId == teamId,
                onClick = { onSelect(teamId) },
                label = { Text(name) },
            )
        }
    }
}

@Composable
internal fun FinishedContent(
    game: GameState,
    onRematch: () -> Unit,
    onNewGame: () -> Unit,
    onMenu: () -> Unit,
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Text("🏆", style = MaterialTheme.typography.displayLarge)
        Text("Победа!", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
        Text(
            game.winners.joinToString { it.name },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        ScoreBoard(game, highlightTeamId = game.winners.firstOrNull()?.id, modifier = Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        BigButton("Реванш", onRematch)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onNewGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) { Text("Новая игра (другие настройки)") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onMenu,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) { Text("В меню") }
    }
}

@Composable
internal fun MessageContent(message: String, buttonText: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        BigButton(buttonText, onClick)
    }
}
