package ru.limonella.alias.core.model

/**
 * Правила игры. Разные режимы (взрослый, детский, будущие) — это разные RuleSet
 * поверх одного и того же движка.
 *
 * @param roundSeconds длительность раунда; null — без таймера (раунд завершают вручную).
 * @param skipPoints сколько очков даёт пропуск: -1 (классика) или 0 (без штрафа).
 * @param pointsToWin до скольких очков играем; null — без победителя.
 * @param lastWordForAll после окончания времени текущее слово может отгадать любая команда.
 */
data class RuleSet(
    val roundSeconds: Int?,
    val skipPoints: Int,
    val pointsToWin: Int?,
    val lastWordForAll: Boolean,
) {
    companion object {
        val Classic = RuleSet(
            roundSeconds = 60,
            skipPoints = -1,
            pointsToWin = 50,
            lastWordForAll = true,
        )
    }
}
