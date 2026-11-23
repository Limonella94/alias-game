package ru.limonella.alias.data.words

import kotlinx.serialization.Serializable
import ru.limonella.alias.core.model.Word

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
    val audience: String = "adult",
    val words: List<WordDto>,
)

@Serializable
data class WordDto(
    val id: String,
    val text: String,
    val level: Int,
    val categories: List<String> = emptyList(),
    val ageMin: Int = 0,
    val image: String? = null,
) {
    fun toDomain() = Word(
        id = id,
        text = text,
        level = level,
        categories = categories,
        ageMin = ageMin,
        image = image,
    )
}
