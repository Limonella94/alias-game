package ru.limonella.alias.core.model

/**
 * Какие слова брать в игру.
 *
 * @param categories пустое множество = все категории.
 * @param maxAgeMin если задано — только слова, подходящие этому возрасту.
 */
data class WordFilter(
    val levels: Set<Int> = setOf(1, 2, 3),
    val categories: Set<String> = emptySet(),
    val maxAgeMin: Int? = null,
) {
    fun matches(word: Word): Boolean =
        word.level in levels &&
            (categories.isEmpty() || word.categories.any { it in categories }) &&
            (maxAgeMin == null || word.ageMin <= maxAgeMin)
}
