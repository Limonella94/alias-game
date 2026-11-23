package ru.limonella.alias.data.history

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.limonella.alias.data.storage.JsonPreference

@Serializable
data class WordHistoryData(
    /** id слова → когда оно выпадало в последний раз (мс). */
    val lastShown: Map<String, Long> = emptyMap(),
)

/** Какие слова уже выпадали — чтобы в новых играх сначала шли новые слова. */
class WordHistoryStore(
    private val preference: JsonPreference<WordHistoryData>,
    private val scope: CoroutineScope,
) {
    /** Сколько разных слов уже выпадало. */
    val shownCount: Flow<Int> = preference.flow.map { it?.lastShown?.size ?: 0 }

    suspend fun lastShown(): Map<String, Long> = preference.get()?.lastShown.orEmpty()

    fun markShown(wordId: String) {
        val now = System.currentTimeMillis()
        scope.launch {
            preference.update { current ->
                val map = current?.lastShown.orEmpty()
                WordHistoryData(map + (wordId to now))
            }
        }
    }

    fun clear() {
        scope.launch { preference.set(null) }
    }
}
