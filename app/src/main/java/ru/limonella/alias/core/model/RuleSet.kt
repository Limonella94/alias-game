package ru.limonella.alias.core.model

import kotlinx.serialization.Serializable

/** Что происходит со словом, которое объясняли в момент окончания времени. */
@Serializable
enum class LastWordRule {
    /** Слово сгорает, раунд сразу заканчивается. */
    NONE,

    /** Отгадать может любая команда, очко — тому, кто первым угадал. */
    ANY_TEAM,

    /** Объясняющий продолжает без ограничения времени, отгадывает только своя команда. */
    OWN_TEAM,
}

/**
 * Правила игры. Разные режимы (взрослый, детский, будущие) — это разные RuleSet
 * поверх одного и того же движка.
 *
 * @param roundSeconds длительность раунда; null — без таймера (раунд завершают вручную).
 * @param skipPoints сколько очков даёт пропуск: -1 (классика) или 0 (без штрафа).
 * @param pointsToWin до скольких очков играем; null — без победителя.
 * @param lastWord правило последнего слова.
 */
@Serializable
data class RuleSet(
    val roundSeconds: Int?,
    val skipPoints: Int,
    val pointsToWin: Int?,
    val lastWord: LastWordRule,
) {
    companion object {
        val Classic = RuleSet(
            roundSeconds = 60,
            skipPoints = -1,
            pointsToWin = 50,
            lastWord = LastWordRule.ANY_TEAM,
        )
    }
}
