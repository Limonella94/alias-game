package ru.limonella.alias.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.LastWordRule
import ru.limonella.alias.core.model.RuleSet
import ru.limonella.alias.core.model.Team
import ru.limonella.alias.core.model.WordFilter
import ru.limonella.alias.core.model.WordPack
import ru.limonella.alias.data.history.WordHistoryStore
import ru.limonella.alias.data.session.GameSessionStore
import ru.limonella.alias.data.settings.SetupSnapshot
import ru.limonella.alias.data.settings.TeamSetup
import ru.limonella.alias.data.storage.JsonPreference
import ru.limonella.alias.data.words.WordRepository

data class SetupUiState(
    val teams: List<TeamSetup> = DEFAULT_TEAM_NAMES.take(2).map { TeamSetup(it) },
    val roundSeconds: Int = 60,
    val pointsToWin: Int = 50,
    val skipPenalty: Boolean = true,
    val lastWord: LastWordRule = LastWordRule.ANY_TEAM,
    val levels: Set<Int> = setOf(1, 2),
    val packs: List<WordPack> = emptyList(),
    val selectedPackIds: Set<String> = emptySet(),
    val soundEnabled: Boolean = true,
    /** Сколько разных слов уже выпадало в прошлых играх. */
    val shownWordsCount: Int = 0,
) {
    val canAddTeam: Boolean get() = teams.size < MAX_TEAMS
    val canRemoveTeam: Boolean get() = teams.size > MIN_TEAMS

    /** Сколько слов попадёт в игру с текущим выбором тем и сложности. */
    val wordsInGame: Int
        get() = packs.filter { it.id in selectedPackIds }.sumOf { it.countFor(levels) }

    val canStart: Boolean
        get() = teams.size >= MIN_TEAMS && teams.all { it.name.isNotBlank() } && wordsInGame > 0

    fun toSnapshot() = SetupSnapshot(
        teams = teams,
        roundSeconds = roundSeconds,
        pointsToWin = pointsToWin,
        skipPenalty = skipPenalty,
        lastWord = lastWord,
        levels = levels,
        selectedPackIds = selectedPackIds,
        soundEnabled = soundEnabled,
    )

    fun applySnapshot(s: SetupSnapshot) = copy(
        teams = s.teams.takeIf { it.size >= MIN_TEAMS } ?: teams,
        roundSeconds = s.roundSeconds,
        pointsToWin = s.pointsToWin,
        skipPenalty = s.skipPenalty,
        lastWord = s.lastWord,
        levels = s.levels,
        selectedPackIds = s.selectedPackIds,
        soundEnabled = s.soundEnabled,
    )

    companion object {
        const val MIN_TEAMS = 2
        const val MAX_TEAMS = 6
        const val MAX_PLAYERS = 10
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
    private val settings: JsonPreference<SetupSnapshot>,
    private val wordHistory: WordHistoryStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val packs = runCatching { wordRepository.getPacks() }.getOrDefault(emptyList())
                .filter { it.audience == WordPack.AUDIENCE_ADULT }
            val saved = runCatching { settings.get() }.getOrNull()
            _state.update { s ->
                val restored = if (saved != null) s.applySnapshot(saved) else s
                // Оставляем только темы, которые реально есть (тему могли удалить).
                val knownIds = packs.map { it.id }.toSet()
                val selected = restored.selectedPackIds.filter { it in knownIds }.toSet()
                val defaultSelection = packs.firstOrNull { it.id == SetupUiState.DEFAULT_PACK_ID } ?: packs.firstOrNull()
                restored.copy(
                    packs = packs,
                    selectedPackIds = selected.ifEmpty { setOfNotNull(defaultSelection?.id) },
                )
            }
            // После загрузки — автосохранение любых изменений (с небольшой задержкой, чтобы не писать на каждую букву).
            _state.drop(1).collectLatest { s ->
                delay(400)
                runCatching { settings.set(s.toSnapshot()) }
            }
        }
        viewModelScope.launch {
            wordHistory.shownCount.collect { count -> _state.update { it.copy(shownWordsCount = count) } }
        }
    }

    // --- Команды и игроки ---

    private fun updateTeam(index: Int, transform: (TeamSetup) -> TeamSetup) = _state.update { s ->
        s.copy(teams = s.teams.mapIndexed { i, team -> if (i == index) transform(team) else team })
    }

    fun renameTeam(index: Int, name: String) = updateTeam(index) { it.copy(name = name) }

    fun addTeam() = _state.update { s ->
        if (!s.canAddTeam) return@update s
        val used = s.teams.map { it.name }
        val name = SetupUiState.DEFAULT_TEAM_NAMES.firstOrNull { it !in used } ?: "Команда ${s.teams.size + 1}"
        s.copy(teams = s.teams + TeamSetup(name))
    }

    fun removeTeam(index: Int) = _state.update { s ->
        if (!s.canRemoveTeam) s else s.copy(teams = s.teams.filterIndexed { i, _ -> i != index })
    }

    fun addPlayer(teamIndex: Int) = updateTeam(teamIndex) { team ->
        if (team.players.size >= SetupUiState.MAX_PLAYERS) team else team.copy(players = team.players + "")
    }

    fun renamePlayer(teamIndex: Int, playerIndex: Int, name: String) = updateTeam(teamIndex) { team ->
        team.copy(players = team.players.mapIndexed { i, old -> if (i == playerIndex) name else old })
    }

    fun removePlayer(teamIndex: Int, playerIndex: Int) = updateTeam(teamIndex) { team ->
        team.copy(players = team.players.filterIndexed { i, _ -> i != playerIndex })
    }

    // --- Правила ---

    fun setRoundSeconds(value: Int) = _state.update { it.copy(roundSeconds = value) }

    fun setPointsToWin(value: Int) = _state.update { it.copy(pointsToWin = value) }

    fun setSkipPenalty(value: Boolean) = _state.update { it.copy(skipPenalty = value) }

    fun setLastWord(value: LastWordRule) = _state.update { it.copy(lastWord = value) }

    fun setSoundEnabled(value: Boolean) = _state.update { it.copy(soundEnabled = value) }

    fun toggleLevel(level: Int) = _state.update { s ->
        s.copy(levels = if (level in s.levels) s.levels - level else s.levels + level)
    }

    // --- Темы ---

    fun togglePack(packId: String) = _state.update { s ->
        s.copy(
            selectedPackIds = if (packId in s.selectedPackIds) {
                s.selectedPackIds - packId
            } else {
                s.selectedPackIds + packId
            },
        )
    }

    fun selectAllPacks() = _state.update { s -> s.copy(selectedPackIds = s.packs.map { it.id }.toSet()) }

    fun clearPacks() = _state.update { it.copy(selectedPackIds = emptySet()) }

    // --- История слов ---

    fun resetWordHistory() = wordHistory.clear()

    /** Сохраняет настройки для экрана игры. Возвращает false, если настройки неполные. */
    fun saveConfig(): Boolean {
        val s = _state.value
        if (!s.canStart) return false
        sessionStore.resume = null
        sessionStore.pendingConfig = GameConfig(
            teams = s.teams.mapIndexed { i, team ->
                Team(
                    id = i,
                    name = team.name.trim(),
                    players = team.players.map { it.trim() }.filter { it.isNotEmpty() },
                )
            },
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
