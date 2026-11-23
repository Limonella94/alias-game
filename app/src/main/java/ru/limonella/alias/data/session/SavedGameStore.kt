package ru.limonella.alias.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.limonella.alias.core.engine.GameState
import ru.limonella.alias.data.storage.JsonPreference

/** Прерванная игра: состояние движка и сколько секунд оставалось в раунде. */
@Serializable
data class SavedGame(
    val state: GameState,
    val secondsLeft: Int?,
    val savedAt: Long,
)

/**
 * Хранит одну незаконченную игру.
 * Запись идёт в фоне через одну очередь: последний вызов save/clear всегда побеждает,
 * даже если экран игры уже закрыли.
 */
class SavedGameStore(
    private val preference: JsonPreference<SavedGame>,
    scope: CoroutineScope,
) {
    private sealed interface Op {
        data class Save(val game: SavedGame) : Op
        data object Clear : Op
    }

    private val ops = MutableStateFlow<Op?>(null)

    val saved: Flow<SavedGame?> = preference.flow

    init {
        scope.launch {
            ops.filterNotNull().collect { op ->
                when (op) {
                    is Op.Save -> preference.set(op.game)
                    Op.Clear -> preference.set(null)
                }
            }
        }
    }

    fun save(game: SavedGame) {
        ops.value = Op.Save(game)
    }

    fun clear() {
        ops.value = Op.Clear
    }
}
