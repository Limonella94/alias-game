package ru.limonella.alias.core.engine

import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.LastWordRule
import ru.limonella.alias.core.model.Team

/**
 * Игровой движок на чистом Kotlin — без Android, без таймеров, без UI.
 * Поэтому его легко тестировать и переиспользовать для любых режимов.
 *
 * Таймер живёт снаружи (в GameViewModel) и по окончании времени вызывает [timeUp].
 * Каждое действие заменяет [state] новым снимком.
 * Действия, не подходящие текущему этапу, молча игнорируются.
 */
class GameEngine private constructor(
    initialState: GameState,
    private val deck: WordDeck,
) {
    /** Новая игра. */
    constructor(config: GameConfig, deck: WordDeck) : this(
        GameState(config = config, scores = config.teams.associate { it.id to 0 }),
        deck,
    )

    var state: GameState = initialState
        private set

    companion object {
        /** Продолжить сохранённую игру с того же места. */
        fun restore(state: GameState, deck: WordDeck): GameEngine = GameEngine(state, deck)
    }

    fun startRound() {
        val s = state
        if (s.phase != Phase.READY) return
        state = s.copy(
            phase = Phase.PLAYING,
            currentWord = deck.next(),
            roundResults = emptyList(),
            lastWord = null,
            lastWordTeamId = null,
        )
    }

    fun markGuessed() = record(Outcome.GUESSED)

    fun markSkipped() = record(Outcome.SKIPPED)

    private fun record(outcome: Outcome) {
        val s = state
        if (s.phase != Phase.PLAYING) return
        val word = s.currentWord ?: return
        state = s.copy(
            roundResults = s.roundResults + WordResult(word, outcome),
            currentWord = deck.next(),
        )
    }

    /** Время вышло (или раунд завершён вручную). */
    fun timeUp() {
        val s = state
        if (s.phase != Phase.PLAYING) return
        state = if (s.config.rules.lastWord != LastWordRule.NONE && s.currentWord != null) {
            s.copy(phase = Phase.LAST_WORD)
        } else {
            s.copy(phase = Phase.REVIEW, currentWord = null)
        }
    }

    /**
     * Кто отгадал последнее слово: id команды или null — никто.
     * В режиме [LastWordRule.OWN_TEAM] засчитать можно только своей команде.
     */
    fun resolveLastWord(teamId: Int?) {
        val s = state
        if (s.phase != Phase.LAST_WORD || !isAllowedLastWordTeam(s, teamId)) return
        state = s.copy(
            phase = Phase.REVIEW,
            lastWord = s.currentWord,
            lastWordTeamId = teamId,
            currentWord = null,
        )
    }

    /** На экране итогов: поменять «угадано» ↔ «пропуск». */
    fun toggleResult(index: Int) {
        val s = state
        if (s.phase != Phase.REVIEW || index !in s.roundResults.indices) return
        val updated = s.roundResults.toMutableList()
        val result = updated[index]
        updated[index] = result.copy(
            outcome = if (result.outcome == Outcome.GUESSED) Outcome.SKIPPED else Outcome.GUESSED,
        )
        state = s.copy(roundResults = updated)
    }

    /** На экране итогов: исправить, кто отгадал последнее слово. */
    fun setLastWordTeam(teamId: Int?) {
        val s = state
        if (s.phase != Phase.REVIEW || s.lastWord == null || !isAllowedLastWordTeam(s, teamId)) return
        state = s.copy(lastWordTeamId = teamId)
    }

    private fun isAllowedLastWordTeam(s: GameState, teamId: Int?): Boolean = when {
        teamId == null -> true
        s.config.rules.lastWord == LastWordRule.OWN_TEAM -> teamId == s.currentTeam.id
        else -> s.config.teams.any { it.id == teamId }
    }

    /** Подтвердить итоги раунда: начислить очки и передать ход. */
    fun confirmRound() {
        val s = state
        if (s.phase != Phase.REVIEW) return

        val rules = s.config.rules
        val teams = s.config.teams
        val scores = s.scores.toMutableMap()

        val currentTeam = s.currentTeam
        val currentId = currentTeam.id
        scores[currentId] = (scores[currentId] ?: 0) + s.roundResults.sumOf { rules.pointsFor(it.outcome) }
        s.lastWordTeamId?.let { id -> scores[id] = (scores[id] ?: 0) + 1 }

        val record = RoundRecord(
            roundNumber = s.roundNumber,
            teamId = currentId,
            explainer = s.currentExplainer,
            guessed = s.roundResults.count { it.outcome == Outcome.GUESSED },
            skipped = s.roundResults.count { it.outcome == Outcome.SKIPPED },
            points = s.roundPoints,
            lastWordTeamId = s.lastWordTeamId,
        )
        // В следующий раз в этой команде объясняет следующий игрок.
        val explainers = s.explainerIndexByTeam.toMutableMap()
        if (currentTeam.players.isNotEmpty()) {
            explainers[currentId] = ((explainers[currentId] ?: 0) + 1) % currentTeam.players.size
        }

        // Победителя определяем только когда все команды сходили одинаковое число раз — так честно.
        val cycleComplete = s.currentTeamIndex == teams.lastIndex
        val winners = findWinners(teams, scores, rules.pointsToWin, cycleComplete)

        state = if (winners.size == 1) {
            s.copy(
                scores = scores,
                phase = Phase.FINISHED,
                winners = winners,
                history = s.history + record,
                explainerIndexByTeam = explainers,
            )
        } else {
            // При ничьей среди лидеров играем ещё круг.
            s.copy(
                scores = scores,
                phase = Phase.READY,
                currentTeamIndex = (s.currentTeamIndex + 1) % teams.size,
                roundNumber = if (cycleComplete) s.roundNumber + 1 else s.roundNumber,
                roundResults = emptyList(),
                lastWord = null,
                lastWordTeamId = null,
                history = s.history + record,
                explainerIndexByTeam = explainers,
            )
        }
    }

    private fun findWinners(
        teams: List<Team>,
        scores: Map<Int, Int>,
        target: Int?,
        cycleComplete: Boolean,
    ): List<Team> {
        if (target == null || !cycleComplete) return emptyList()
        val best = scores.values.maxOrNull() ?: return emptyList()
        if (best < target) return emptyList()
        return teams.filter { scores[it.id] == best }
    }
}
