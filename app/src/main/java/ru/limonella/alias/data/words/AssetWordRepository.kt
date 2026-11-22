package ru.limonella.alias.data.words

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.model.WordFilter

/** Читает все наборы из папки assets/words/ один раз и держит их в памяти. */
class AssetWordRepository(
    private val context: Context,
    private val json: Json,
) : WordRepository {

    private val mutex = Mutex()
    private var cache: List<Word>? = null

    override suspend fun getWords(filter: WordFilter): List<Word> =
        allWords().filter(filter::matches)

    private suspend fun allWords(): List<Word> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) { loadAll() }.also { cache = it }
    }

    private fun loadAll(): List<Word> {
        val files = context.assets.list(DIR).orEmpty().filter { it.endsWith(".json") }
        return files.flatMap { name ->
            val text = context.assets.open("$DIR/$name").bufferedReader().use { it.readText() }
            json.decodeFromString(WordPackDto.serializer(), text).words.map { it.toDomain() }
        }
    }

    private companion object {
        const val DIR = "words"
    }
}
