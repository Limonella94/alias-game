package ru.limonella.alias.data.session

import ru.limonella.alias.core.model.GameConfig

/**
 * Передаёт данные на экран игры:
 * [pendingConfig] — настройки новой игры (с экрана «Новая игра», нужны и для «Реванша»),
 * [resume] — сохранённая игра, которую надо продолжить (с главного экрана).
 * Живёт в памяти, пока запущено приложение.
 */
class GameSessionStore {
    var pendingConfig: GameConfig? = null
    var resume: SavedGame? = null
}
