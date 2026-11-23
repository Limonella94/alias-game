package ru.limonella.alias.data.settings

import kotlinx.serialization.Serializable
import ru.limonella.alias.core.model.LastWordRule

/** Команда на экране настроек: название и игроки (как ввёл пользователь). */
@Serializable
data class TeamSetup(
    val name: String,
    val players: List<String> = emptyList(),
)

/** Последние настройки игры — восстанавливаются при следующем открытии экрана «Новая игра». */
@Serializable
data class SetupSnapshot(
    val teams: List<TeamSetup>,
    val roundSeconds: Int,
    val pointsToWin: Int,
    val skipPenalty: Boolean,
    val lastWord: LastWordRule,
    val levels: Set<Int>,
    val selectedPackIds: Set<String>,
    val soundEnabled: Boolean,
)
