package ru.limonella.alias.core.engine

import ru.limonella.alias.core.model.Word
import kotlin.random.Random

/**
 * Колода слов: выдаёт слова в случайном порядке без повторов.
 * Когда слова закончились — перемешивает заново.
 */
class WordDeck(
    words: List<Word>,
    private val random: Random = Random.Default,
) {
    // Одно и то же слово может быть в нескольких темах — в колоде оставляем один экземпляр.
    private val all: List<Word> = words.distinctBy { it.text.trim().lowercase() }
    private val queue = ArrayDeque<Word>()

    init {
        require(all.isNotEmpty()) { "Нет слов для игры" }
    }

    fun next(): Word {
        if (queue.isEmpty()) queue.addAll(all.shuffled(random))
        return queue.removeFirst()
    }
}
