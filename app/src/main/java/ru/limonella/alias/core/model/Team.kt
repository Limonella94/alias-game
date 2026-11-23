package ru.limonella.alias.core.model

import kotlinx.serialization.Serializable

/**
 * Команда. [players] — имена игроков; пустой список = игроков не указали
 * (тогда объясняющего не показываем).
 */
@Serializable
data class Team(
    val id: Int,
    val name: String,
    val players: List<String> = emptyList(),
)
