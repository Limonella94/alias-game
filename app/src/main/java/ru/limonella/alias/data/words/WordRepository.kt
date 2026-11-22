package ru.limonella.alias.data.words

import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.model.WordFilter

/**
 * Источник слов для игры. Экраны знают только этот интерфейс.
 *
 * Сейчас реализация читает JSON из assets ([AssetWordRepository]).
 * Позже появится реализация на Room с обновлением наборов из GitHub —
 * остальной код при этом менять не придётся.
 */
interface WordRepository {
    suspend fun getWords(filter: WordFilter): List<Word>
}
