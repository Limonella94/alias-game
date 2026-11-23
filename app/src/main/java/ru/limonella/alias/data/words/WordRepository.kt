package ru.limonella.alias.data.words

import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.model.WordFilter
import ru.limonella.alias.core.model.WordPack

/**
 * Источник слов для игры. Экраны знают только этот интерфейс.
 *
 * Сейчас реализация читает JSON из assets ([AssetWordRepository]).
 * Позже появится реализация на Room с обновлением наборов из GitHub —
 * остальной код при этом менять не придётся.
 */
interface WordRepository {
    /** Все доступные темы, в порядке показа. */
    suspend fun getPacks(): List<WordPack>

    suspend fun getWords(filter: WordFilter): List<Word>
}
