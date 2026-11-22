package ru.limonella.alias.core.model

/**
 * Одно слово для объяснения.
 *
 * @param level сложность: 1 — лёгкое, 2 — среднее, 3 — сложное (шкалу можно расширить до 5).
 * @param categories темы: "animals", "food" и т.д. Используются для фильтра.
 * @param ageMin минимальный возраст игрока, для которого слово подходит.
 * @param image ссылка/путь к картинке (для детского режима), null — без картинки.
 */
data class Word(
    val id: String,
    val text: String,
    val level: Int,
    val categories: List<String> = emptyList(),
    val ageMin: Int = 0,
    val image: String? = null,
)
