package ru.limonella.alias.core.model

/**
 * Тема (набор слов): «Базовый», «Гарри Поттер», «Ведьмак»…
 *
 * @param emoji значок, пока нет картинки-обложки.
 * @param colorArgb основной цвет обложки в формате 0xAARRGGBB; null — цвет темы приложения.
 * @param cover путь к картинке-обложке внутри assets (например, "covers/ru_witcher.webp"); null — без картинки.
 * @param wordCountByLevel сколько слов каждого уровня сложности.
 * @param audience для кого тема: [AUDIENCE_ADULT] — в обычной игре, [AUDIENCE_KIDS] — только в детском режиме.
 */
data class WordPack(
    val id: String,
    val title: String,
    val description: String = "",
    val emoji: String = "💬",
    val colorArgb: Long? = null,
    val cover: String? = null,
    val wordCountByLevel: Map<Int, Int> = emptyMap(),
    val audience: String = AUDIENCE_ADULT,
) {
    companion object {
        const val AUDIENCE_ADULT = "adult"
        const val AUDIENCE_KIDS = "kids"
    }

    val totalWords: Int get() = wordCountByLevel.values.sum()

    fun countFor(levels: Set<Int>): Int =
        wordCountByLevel.filterKeys { it in levels }.values.sum()
}
