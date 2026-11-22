package ru.limonella.alias.core.engine

import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.RuleSet
import ru.limonella.alias.core.model.Team
import ru.limonella.alias.core.model.Word

/** Этапы игры. Экран игры просто показывает то, что соответствует текущему этапу. */
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

enum class Outcome { GUESSED, SKIPPED }

data class WordResult(
    val word: Word,
    val outcome: Outcome,
)

fun RuleSet.pointsFor(outcome: Outcome): Int = when (outcome) {
    Outcome.GUESSED -> 1
    Outcome.SKIPPED -> skipPoints
}

/** Неизменяемый снимок состояния игры. */
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
) {
    val currentTeam: Team get() = config.teams[currentTeamIndex]

    /** Сколько очков текущая команда получит за этот раунд. */
    val roundPoints: Int
        get() = roundResults.sumOf { config.rules.pointsFor(it.outcome) } +
            (if (lastWordTeamId == currentTeam.id) 1 else 0)
}
