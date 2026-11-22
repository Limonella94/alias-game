package ru.limonella.alias.core.model

/** Всё, что выбрано на экране настроек перед началом игры. */
data class GameConfig(
    val teams: List<Team>,
    val rules: RuleSet,
    val wordFilter: WordFilter = WordFilter(),
) {
    init {
        require(teams.isNotEmpty()) { "Нужна хотя бы одна команда" }
        require(teams.map { it.id }.toSet().size == teams.size) { "id команд должны быть уникальны" }
    }
}
