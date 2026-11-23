package ru.limonella.alias.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.LastWordRule
import ru.limonella.alias.core.model.RuleSet
import ru.limonella.alias.core.model.Team
import ru.limonella.alias.core.model.WordFilter
import ru.limonella.alias.core.model.WordPack
import ru.limonella.alias.data.session.GameSessionStore
import ru.limonella.alias.data.words.WordRepository

data class SetupUiState(
    val teamNames: List<String> = DEFAULT_TEAM_NAMES.take(2),
    val roundSeconds: Int = 60,
    val pointsToWin: Int = 50,
    val skipPenalty: Boolean = true,
    val lastWord: LastWordRule = LastWordRule.ANY_TEAM,
    val levels: Set<Int> = setOf(1, 2),
    val packs: List<WordPack> = emptyList(),
    val selectedPackIds: Set<String> = emptySet(),
    val soundEnabled: Boolean = true,
) {
    val canAddTeam: Boolean get() = teamNames.size < MAX_TEAMS
    val canRemoveTeam: Boolean get() = teamNames.size > MIN_TEAMS

    /** Сколько слов попадёт в игру с текущим выбором тем и сложности. */
    val wordsInGame: Int
        get() = packs.filter { it.id in selectedPackIds }.sumOf { it.countFor(levels) }

    val canStart: Boolean
        get() = teamNames.size >= MIN_TEAMS && teamNames.all { it.isNotBlank() } && wordsInGame > 0

    companion object {
        const val MIN_TEAMS = 2
        const val MAX_TEAMS = 6
        const val DEFAULT_PACK_ID = "ru_base"
        val DEFAULT_TEAM_NAMES = listOf("Котики", "Ёжики", "Совы", "Лисы", "Панды", "Еноты")
        val ROUND_SECONDS_OPTIONS = listOf(30, 60, 90, 120)
        val POINTS_OPTIONS = listOf(20, 30, 50, 75)
        val LEVEL_OPTIONS = listOf(1, 2, 3)
        val LAST_WORD_OPTIONS = listOf(LastWordRule.NONE, LastWordRule.ANY_TEAM, LastWordRule.OWN_TEAM)
    }
}

class SetupViewModel(
    private val sessionStore: GameSessionStore,
    private val wordRepository: WordRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val packs = runCatching { wordRepository.getPacks() }.getOrDefault(emptyList())
            _state.update { s ->
                val defaultSelection = packs.firstOrNull { it.id == SetupUiState.DEFAULT_PACK_ID }
                    ?: packs.firstOrNull()
                s.copy(
                    packs = packs,
                    selectedPackIds = s.selectedPackIds.ifEmpty { setOfNotNull(defaultSelection?.id) },
                )
            }
        }
    }

    fun renameTeam(index: Int, name: String) = _state.update { s ->
        s.copy(teamNames = s.teamNames.mapIndexed { i, old -> if (i == index) name else old })
    }

    fun addTeam() = _state.update { s ->
        if (!s.canAddTeam) return@update s
        val name = SetupUiState.DEFAULT_TEAM_NAMES.firstOrNull { it !in s.teamNames }
            ?: "Команда ${s.teamNames.size + 1}"
        s.copy(teamNames = s.teamNames + name)
    }

    fun removeTeam(index: Int) = _state.update { s ->
        if (!s.canRemoveTeam) s else s.copy(teamNames = s.teamNames.filterIndexed { i, _ -> i != index })
    }

    fun setRoundSeconds(value: Int) = _state.update { it.copy(roundSeconds = value) }

    fun setPointsToWin(value: Int) = _state.update { it.copy(pointsToWin = value) }

    fun setSkipPenalty(value: Boolean) = _state.update { it.copy(skipPenalty = value) }

    fun setLastWord(value: LastWordRule) = _state.update { it.copy(lastWord = value) }

    fun setSoundEnabled(value: Boolean) = _state.update { it.copy(soundEnabled = value) }

    fun toggleLevel(level: Int) = _state.update { s ->
        s.copy(levels = if (level in s.levels) s.levels - level else s.levels + level)
    }

    fun togglePack(packId: String) = _state.update { s ->
        s.copy(
            selectedPackIds = if (packId in s.selectedPackIds) {
                s.selectedPackIds - packId
            } else {
                s.selectedPackIds + packId
            },
        )
    }

    /** Сохраняет настройки для экрана игры. Возвращает false, если настройки неполные. */
    fun saveConfig(): Boolean {
        val s = _state.value
        if (!s.canStart) return false
        sessionStore.pendingConfig = GameConfig(
            teams = s.teamNames.mapIndexed { i, name -> Team(id = i, name = name.trim()) },
            rules = RuleSet(
                roundSeconds = s.roundSeconds,
                skipPoints = if (s.skipPenalty) -1 else 0,
                pointsToWin = s.pointsToWin,
                lastWord = s.lastWord,
            ),
            wordFilter = WordFilter(levels = s.levels, packIds = s.selectedPackIds),
            soundEnabled = s.soundEnabled,
        )
        return true
    }
}
