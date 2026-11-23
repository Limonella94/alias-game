package ru.limonella.alias.core.engine

import kotlinx.serialization.Serializable
import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.RuleSet
import ru.limonella.alias.core.model.Team
import ru.limonella.alias.core.model.Word

/** Этапы игры. Экран игры просто показывает то, что соответствует текущему этапу. */
@Serializable
enum class Phase {
    /** Ждём, пока команда нажмёт «Старт». */
    READY,

    /** Идёт раунд, тикает таймер. */
    PLAYING,

    /** Время вышло, последнее слово может отгадать любая команда. */
    LAST_WORD,

    /** Проверка итогов раунда: можно исправить отметки. */
    REVIEW,

    /** Есть победитель. */
    FINISHED,
}

@Serializable
enum class Outcome { GUESSED, SKIPPED }

@Serializable
data class WordResult(
    val word: Word,
    val outcome: Outcome,
)

fun RuleSet.pointsFor(outcome: Outcome): Int = when (outcome) {
    Outcome.GUESSED -> 1
    Outcome.SKIPPED -> skipPoints
}

/** Итог одного сыгранного раунда — для статистики. */
@Serializable
data class RoundRecord(
    val roundNumber: Int,
    val teamId: Int,
    val explainer: String?,
    val guessed: Int,
    val skipped: Int,
    val points: Int,
    val lastWordTeamId: Int?,
)

/**
 * Неизменяемый снимок состояния игры. Сериализуется целиком —
 * так прерванную игру можно сохранить и продолжить.
 */
@Serializable
data class GameState(
    val config: GameConfig,
    val scores: Map<Int, Int>,
    val currentTeamIndex: Int = 0,
    val roundNumber: Int = 1,
    val phase: Phase = Phase.READY,
    val currentWord: Word? = null,
    val roundResults: List<WordResult> = emptyList(),
    val lastWord: Word? = null,
    val lastWordTeamId: Int? = null,
    val winners: List<Team> = emptyList(),
    /** Чья очередь объяснять в каждой команде: id команды → индекс игрока. */
    val explainerIndexByTeam: Map<Int, Int> = emptyMap(),
    /** Сыгранные раунды по порядку. */
    val history: List<RoundRecord> = emptyList(),
) {
    val currentTeam: Team get() = config.teams[currentTeamIndex]

    /** Кто объясняет в этом раунде; null — игроки в команде не указаны. */
    val currentExplainer: String? get() = explainerOf(currentTeam)

    fun explainerOf(team: Team): String? {
        if (team.players.isEmpty()) return null
        val index = explainerIndexByTeam[team.id] ?: 0
        return team.players[index % team.players.size]
    }

    /** Сколько очков текущая команда получит за этот раунд. */
    val roundPoints: Int
        get() = roundResults.sumOf { config.rules.pointsFor(it.outcome) } +
            (if (lastWordTeamId == currentTeam.id) 1 else 0)
}
