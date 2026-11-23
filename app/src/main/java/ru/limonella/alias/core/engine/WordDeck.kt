package ru.limonella.alias.core.engine

import ru.limonella.alias.core.model.Word
import kotlin.random.Random

/**
 * Колода слов. Сначала выдаёт слова, которые ещё ни разу не выпадали (в случайном порядке),
 * затем — давно выпадавшие (сначала самые старые). Когда колода закончилась — перемешивает всё заново.
 *
 * @param lastShown история: id слова → когда оно выпадало в последний раз (мс).
 */
class WordDeck(
    words: List<Word>,
    private val random: Random = Random.Default,
    lastShown: Map<String, Long> = emptyMap(),
) {
    // Одно и то же слово может быть в нескольких темах — в колоде оставляем один экземпляр.
    private val all: List<Word> = words.distinctBy { it.text.trim().lowercase() }
    private val queue = ArrayDeque<Word>()

    init {
        require(all.isNotEmpty()) { "Нет слов для игры" }
        val (seen, unseen) = all.partition { it.id in lastShown }
        queue.addAll(unseen.shuffled(random))
        queue.addAll(seen.sortedBy { lastShown[it.id] ?: 0L })
    }

    fun next(): Word {
        if (queue.isEmpty()) queue.addAll(all.shuffled(random))
        return queue.removeFirst()
    }
}
