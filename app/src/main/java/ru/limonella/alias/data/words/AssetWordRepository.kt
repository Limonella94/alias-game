package ru.limonella.alias.data.words

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.model.WordFilter
import ru.limonella.alias.core.model.WordPack

/** Читает все наборы из папки assets/words один раз и держит их в памяти. */
class AssetWordRepository(
    private val context: Context,
    private val json: Json,
) : WordRepository {

    private class Loaded(val packs: List<WordPack>, val words: List<Word>)

    private val mutex = Mutex()
    private var cache: Loaded? = null

    override suspend fun getPacks(): List<WordPack> = load().packs

    override suspend fun getWords(filter: WordFilter): List<Word> =
        load().words.filter(filter::matches)

    private suspend fun load(): Loaded = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) { readAll() }.also { cache = it }
    }

    private fun readAll(): Loaded {
        val files = context.assets.list(DIR).orEmpty().filter { it.endsWith(".json") }
        val dtos = files
            .map { name ->
                val text = context.assets.open("$DIR/$name").bufferedReader().use { it.readText() }
                json.decodeFromString(WordPackDto.serializer(), text)
            }
            .sortedWith(compareBy<WordPackDto> { it.order }.thenBy { it.title })
        return Loaded(
            packs = dtos.map { it.toPack() },
            words = dtos.flatMap { dto -> dto.words.map { it.toDomain(dto.id) } },
        )
    }

    private companion object {
        const val DIR = "words"
    }
}
