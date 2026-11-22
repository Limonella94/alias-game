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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onBack: () -> Unit,
    onStartGame: () -> Unit,
) {
    val container = appContainer()
    val viewModel: SetupViewModel = viewModel { SetupViewModel(container.gameSessionStore) }
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

            item { SectionTitle("Правила") }
            item {
                SwitchRow(
                    text = "Штраф за пропуск (−1 очко)",
                    checked = state.skipPenalty,
                    onCheckedChange = viewModel::setSkipPenalty,
                )
            }
            item {
                SwitchRow(
                    text = "Последнее слово может отгадать любая команда",
                    checked = state.lastWordForAll,
                    onCheckedChange = viewModel::setLastWordForAll,
                )
            }
        }
    }
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
