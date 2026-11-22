package ru.limonella.alias.core.engine

import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.Team

/**
 * Игровой движок на чистом Kotlin — без Android, без таймеров, без UI.
 * Поэтому его легко тестировать и переиспользовать для любых режимов.
 *
 * Таймер живёт снаружи (в GameViewModel) и по окончании времени вызывает [timeUp].
 * Каждое действие заменяет [state] новым снимком.
 * Действия, не подходящие текущему этапу, молча игнорируются.
 */
class GameEngine(
    config: GameConfig,
    private val deck: WordDeck,
) {
    var state: GameState = GameState(
        config = config,
        scores = config.teams.associate { it.id to 0 },
    )
        private set

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
        state = if (s.config.rules.lastWordForAll && s.currentWord != null) {
            s.copy(phase = Phase.LAST_WORD)
        } else {
            s.copy(phase = Phase.REVIEW, currentWord = null)
        }
    }

    /** Кто отгадал последнее слово: id команды или null — никто. */
    fun resolveLastWord(teamId: Int?) {
        val s = state
        if (s.phase != Phase.LAST_WORD) return
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
        if (s.phase != Phase.REVIEW || s.lastWord == null) return
        state = s.copy(lastWordTeamId = teamId)
    }

    /** Подтвердить итоги раунда: начислить очки и передать ход. */
    fun confirmRound() {
        val s = state
        if (s.phase != Phase.REVIEW) return

        val rules = s.config.rules
        val teams = s.config.teams
        val scores = s.scores.toMutableMap()

        val currentId = s.currentTeam.id
        scores[currentId] = (scores[currentId] ?: 0) + s.roundResults.sumOf { rules.pointsFor(it.outcome) }
        s.lastWordTeamId?.let { id -> scores[id] = (scores[id] ?: 0) + 1 }

        // Победителя определяем только когда все команды сходили одинаковое число раз — так честно.
        val cycleComplete = s.currentTeamIndex == teams.lastIndex
        val winners = findWinners(teams, scores, rules.pointsToWin, cycleComplete)

        state = if (winners.size == 1) {
            s.copy(scores = scores, phase = Phase.FINISHED, winners = winners)
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
