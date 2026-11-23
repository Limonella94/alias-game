package ru.limonella.alias.core.model

import kotlinx.serialization.Serializable

/** Для кого игра: от этого зависит вид карточек и кнопок. */
@Serializable
enum class GameMode { ADULT, KIDS }

/** Всё, что выбрано на экране настроек перед началом игры. */
@Serializable
data class GameConfig(
    val teams: List<Team>,
    val rules: RuleSet,
    val wordFilter: WordFilter = WordFilter(),
    val soundEnabled: Boolean = true,
    val mode: GameMode = GameMode.ADULT,
) {
    init {
        require(teams.isNotEmpty()) { "Нужна хотя бы одна команда" }
        require(teams.map { it.id }.toSet().size == teams.size) { "id команд должны быть уникальны" }
    }
}
