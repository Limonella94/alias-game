package ru.limonella.alias.core.model

/**
 * Готовые настройки детского режима — дети ничего не настраивают сами.
 * Слова берутся из темы [PACK_ID]: уровень 1 — для малышей, уровни 1–2 — для деток.
 */
object KidsPresets {
    const val PACK_ID = "ru_kids"

    /** Малыши 3–5: объясняет взрослый, без таймера, без штрафов, все вместе. */
    fun little(): GameConfig = GameConfig(
        teams = listOf(Team(0, "Мы")),
        rules = RuleSet(
            roundSeconds = null,
            skipPoints = 0,
            pointsToWin = null,
            lastWord = LastWordRule.NONE,
        ),
        wordFilter = WordFilter(levels = setOf(1), packIds = setOf(PACK_ID)),
        mode = GameMode.KIDS,
    )

    /** Детки 5–6: объясняют сами, мягкий таймер, без штрафов; последнее слово можно договорить. */
    fun big(twoTeams: Boolean): GameConfig = GameConfig(
        teams = if (twoTeams) {
            listOf(Team(0, "Зайчики 🐰"), Team(1, "Мишки 🐻"))
        } else {
            listOf(Team(0, "Мы"))
        },
        rules = RuleSet(
            roundSeconds = 90,
            skipPoints = 0,
            pointsToWin = if (twoTeams) 15 else 20,
            lastWord = LastWordRule.OWN_TEAM,
        ),
        wordFilter = WordFilter(levels = setOf(1, 2), packIds = setOf(PACK_ID)),
        mode = GameMode.KIDS,
    )
}
