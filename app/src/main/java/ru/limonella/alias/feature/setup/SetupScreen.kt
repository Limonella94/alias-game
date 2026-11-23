package ru.limonella.alias.feature.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.limonella.alias.di.appContainer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import ru.limonella.alias.core.model.LastWordRule
import ru.limonella.alias.core.model.WordPack
import ru.limonella.alias.ui.components.PackCover

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onBack: () -> Unit,
    onStartGame: () -> Unit,
    onOpenThemes: () -> Unit,
) {
    val viewModel = setupViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая игра") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = { if (viewModel.saveConfig()) onStartGame() },
                enabled = state.canStart,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .height(60.dp),
            ) {
                Text("Начать игру", style = MaterialTheme.typography.titleLarge)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SectionTitle("Команды") }
            itemsIndexed(state.teamNames) { index, name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.renameTeam(index, it) },
                    label = { Text("Команда ${index + 1}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (state.canRemoveTeam) {
                            IconButton(onClick = { viewModel.removeTeam(index) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Удалить команду")
                            }
                        }
                    },
                )
            }
            if (state.canAddTeam) {
                item {
                    OutlinedButton(onClick = viewModel::addTeam) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить команду")
                    }
                }
            }

            item { SectionTitle("Темы") }
            item {
                ThemesSummary(
                    selected = state.packs.filter { it.id in state.selectedPackIds },
                    onClick = onOpenThemes,
                )
            }

            item { SectionTitle("Время раунда") }
            item {
                ChipRow(
                    options = SetupUiState.ROUND_SECONDS_OPTIONS,
                    isSelected = { it == state.roundSeconds },
                    label = { "$it сек" },
                    onClick = viewModel::setRoundSeconds,
                )
            }

            item { SectionTitle("Очки до победы") }
            item {
                ChipRow(
                    options = SetupUiState.POINTS_OPTIONS,
                    isSelected = { it == state.pointsToWin },
                    label = { "$it" },
                    onClick = viewModel::setPointsToWin,
                )
            }

            item { SectionTitle("Сложность слов") }
            item {
                ChipRow(
                    options = SetupUiState.LEVEL_OPTIONS,
                    isSelected = { it in state.levels },
                    label = ::levelName,
                    onClick = viewModel::toggleLevel,
                )
            }
            item {
                Text(
                    text = if (state.wordsInGame > 0) {
                        "Слов в игре: ${state.wordsInGame}"
                    } else {
                        "Выберите хотя бы одну тему и сложность"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.wordsInGame > 0) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }

            item { SectionTitle("Правила") }
            item {
                SwitchRow(
                    text = "Штраф за пропуск (−1 очко)",
                    checked = state.skipPenalty,
                    onCheckedChange = viewModel::setSkipPenalty,
                )
            }
            item { SectionTitle("Последнее слово") }
            item {
                ChipRow(
                    options = SetupUiState.LAST_WORD_OPTIONS,
                    isSelected = { it == state.lastWord },
                    label = ::lastWordName,
                    onClick = viewModel::setLastWord,
                )
            }
            item {
                Text(
                    lastWordDescription(state.lastWord),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item { SectionTitle("Прочее") }
            item {
                SwitchRow(
                    text = "Звуки",
                    checked = state.soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled,
                )
            }
        }
    }
}

/** Один SetupViewModel на экран настроек и экран тем: [owner] — запись экрана настроек. */
@Composable
fun setupViewModel(
    owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
): SetupViewModel {
    val container = appContainer()
    return viewModel(viewModelStoreOwner = owner) {
        SetupViewModel(container.gameSessionStore, container.wordRepository)
    }
}

/** Карточка «Темы»: что выбрано, по нажатию — экран с плитками. */
@Composable
private fun ThemesSummary(selected: List<WordPack>, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Обложки внахлёст: каждая следующая сдвинута вправо.
            Box {
                selected.take(3).forEachIndexed { index, pack ->
                    PackCover(
                        pack = pack,
                        modifier = Modifier
                            .padding(start = (index * 28).dp)
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (selected.isEmpty()) "Темы не выбраны" else selected.joinToString { it.title },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Нажмите, чтобы выбрать темы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

private fun lastWordName(rule: LastWordRule): String = when (rule) {
    LastWordRule.NONE -> "Сгорает"
    LastWordRule.ANY_TEAM -> "Любая команда"
    LastWordRule.OWN_TEAM -> "Своя команда"
}

private fun lastWordDescription(rule: LastWordRule): String = when (rule) {
    LastWordRule.NONE -> "Время вышло — слово не засчитывается, раунд сразу заканчивается."
    LastWordRule.ANY_TEAM -> "После сигнала слово может отгадать любая команда, очко — тому, кто первым угадал."
    LastWordRule.OWN_TEAM -> "После сигнала объясняющий договаривает слово без ограничения времени, отгадывает только своя команда."
}

private fun levelName(level: Int): String = when (level) {
    1 -> "Лёгкие"
    2 -> "Средние"
    3 -> "Сложные"
    else -> "Уровень $level"
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun <T> ChipRow(
    options: List<T>,
    isSelected: (T) -> Boolean,
    label: (T) -> String,
    onClick: (T) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = isSelected(option),
                onClick = { onClick(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

@Composable
private fun SwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
