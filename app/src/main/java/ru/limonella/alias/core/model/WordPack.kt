package ru.limonella.alias.core.model

/**
 * Тема (набор слов): «Базовый», «Гарри Поттер», «Ведьмак»…
 *
 * @param emoji значок, пока нет картинки-обложки.
 * @param colorArgb основной цвет обложки в формате 0xAARRGGBB; null — цвет темы приложения.
 * @param cover путь к картинке-обложке внутри assets (например, "covers/ru_witcher.webp"); null — без картинки.
 * @param wordCountByLevel сколько слов каждого уровня сложности.
 */
data class WordPack(
    val id: String,
    val title: String,
    val description: String = "",
    val emoji: String = "💬",
    val colorArgb: Long? = null,
    val cover: String? = null,
    val wordCountByLevel: Map<Int, Int> = emptyMap(),
) {
    val totalWords: Int get() = wordCountByLevel.values.sum()

    fun countFor(levels: Set<Int>): Int =
        wordCountByLevel.filterKeys { it in levels }.values.sum()
}
