package ru.limonella.alias.data.session

import ru.limonella.alias.core.model.GameConfig

/**
 * Передаёт настройки с экрана «Новая игра» на экран игры.
 * Живёт в памяти, пока запущено приложение.
 */
class GameSessionStore {
    var pendingConfig: GameConfig? = null
}
