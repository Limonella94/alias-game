package ru.limonella.alias.data.words

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.limonella.alias.core.model.KidsPresets
import ru.limonella.alias.core.model.WordPack
import java.io.File

/**
 * Проверка всех наборов слов из assets: каждое изменение словарей
 * (и наше, и будущие из alias-words) должно проходить эти правила.
 */
class WordPacksTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Unit-тесты запускаются из папки модуля app.
    private val packs: List<WordPackDto> = File("src/main/assets/words")
        .listFiles { f -> f.name.endsWith(".json") }
        .orEmpty()
        .sortedBy { it.name }
        .map { file ->
            runCatching { json.decodeFromString(WordPackDto.serializer(), file.readText()) }
                .getOrElse { throw AssertionError("Не читается ${file.name}: ${it.message}", it) }
        }

    @Test
    fun packsExist() {
        assertTrue("Не найдено ни одного набора слов", packs.isNotEmpty())
    }

    @Test
    fun packIdsAreUnique() {
        val ids = packs.map { it.id }
        assertEquals("Повторяются id тем", ids.size, ids.toSet().size)
    }

    @Test
    fun wordIdsAreUniqueAcrossAllPacks() {
        val ids = packs.flatMap { p -> p.words.map { it.id } }
        val duplicates = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue("Повторяются id слов: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun wordsAreValid() {
        packs.forEach { pack ->
            assertTrue("Пустая тема ${pack.id}", pack.words.isNotEmpty())
            pack.words.forEach { w ->
                assertTrue("Пустое слово ${w.id}", w.text.isNotBlank())
                assertTrue("Уровень ${w.level} у ${w.id} вне 1..3", w.level in 1..3)
            }
        }
    }

    @Test
    fun coverFilesExist() {
        packs.mapNotNull { it.cover }.forEach { cover ->
            assertTrue("Нет файла обложки $cover", File("src/main/assets/$cover").isFile)
        }
    }

    @Test
    fun kidsPackHasPictureForEveryWord() {
        val kids = packs.single { it.id == KidsPresets.PACK_ID }
        assertEquals(WordPack.AUDIENCE_KIDS, kids.audience)
        val missing = kids.words.filter { it.emoji.isNullOrBlank() }.map { it.text }
        assertTrue("Нет картинки у слов: $missing", missing.isEmpty())
        assertTrue("Мало слов для малышей", kids.words.count { it.level == 1 } >= 30)
    }
}
