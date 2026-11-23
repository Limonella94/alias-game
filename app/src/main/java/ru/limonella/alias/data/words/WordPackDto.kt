package ru.limonella.alias.data.words

import kotlinx.serialization.Serializable
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.model.WordPack

/**
 * Формат файла с набором слов (JSON-файлы в папке assets/words, позже — из репозитория alias-words).
 * Новые поля добавляем со значением по умолчанию, чтобы старые файлы продолжали читаться.
 */
@Serializable
data class WordPackDto(
    val id: String,
    val version: Int,
    val language: String,
    val title: String,
    val description: String = "",
    val audience: String = "adult",
    /** Значок темы, пока нет обложки. */
    val emoji: String = "💬",
    /** Цвет обложки, "#RRGGBB". */
    val color: String? = null,
    /** Картинка-обложка: путь внутри assets, например "covers/ru_witcher.webp". */
    val cover: String? = null,
    /** Порядок в списке тем: меньше — выше. */
    val order: Int = 100,
    val words: List<WordDto>,
) {
    fun toPack() = WordPack(
        id = id,
        title = title,
        description = description,
        emoji = emoji,
        colorArgb = parseColor(color),
        cover = cover,
        wordCountByLevel = words.groupingBy { it.level }.eachCount(),
        audience = audience,
    )

    private fun parseColor(hex: String?): Long? {
        val digits = hex?.removePrefix("#") ?: return null
        if (digits.length != 6) return null
        return digits.toLongOrNull(16)?.let { 0xFF000000L or it }
    }
}

@Serializable
data class WordDto(
    val id: String,
    val text: String,
    val level: Int,
    val categories: List<String> = emptyList(),
    val ageMin: Int = 0,
    val image: String? = null,
    /** Картинка-эмодзи для детского режима. */
    val emoji: String? = null,
) {
    fun toDomain(packId: String) = Word(
        id = id,
        text = text,
        level = level,
        categories = categories,
        ageMin = ageMin,
        image = image,
        packId = packId,
        emoji = emoji,
    )
}
