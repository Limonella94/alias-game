package ru.limonella.alias.feature.kids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.limonella.alias.core.engine.GameEngine
import ru.limonella.alias.core.engine.Outcome
import ru.limonella.alias.core.engine.WordDeck
import ru.limonella.alias.core.model.KidsPresets
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.sound.GameSound
import ru.limonella.alias.core.sound.SoundPlayer
import ru.limonella.alias.data.history.WordHistoryStore
import ru.limonella.alias.data.words.WordRepository

/**
 * Игра для малышей 3–5: без таймера, без команд и штрафов.
 * Используется тот же движок — один бесконечный раунд, пока взрослый не нажмёт «Закончить».
 */
class KidsLittleViewModel(
    private val wordRepository: WordRepository,
    private val soundPlayer: SoundPlayer,
    private val wordHistory: WordHistoryStore,
) : ViewModel() {

    data class UiState(
        val word: Word? = null,
        val guessed: Int = 0,
        /** Угаданные слова — показываем их на экране итогов. */
        val guessedWords: List<Word> = emptyList(),
        val finished: Boolean = false,
        val error: String? = null,
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var engine: GameEngine? = null

    init {
        start()
    }

    /** Начать (или начать заново). */
    fun start() {
        viewModelScope.launch {
            val config = KidsPresets.little()
            val words = try {
                wordRepository.getWords(config.wordFilter)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.update { it.copy(error = "Не удалось загрузить картинки: ${e.message}") }
                return@launch
            }
            if (words.isEmpty()) {
                _ui.update { it.copy(error = "Нет картинок для малышей") }
                return@launch
            }
            val history = runCatching { wordHistory.lastShown() }.getOrDefault(emptyMap())
            val e = GameEngine(config, WordDeck(words, lastShown = history))
            e.startRound()
            engine = e
            _ui.value = UiState()
            publish()
        }
    }

    fun guessed() {
        if (_ui.value.finished) return
        engine?.markGuessed()
        soundPlayer.play(GameSound.GUESSED)
        publish()
    }

    fun next() {
        if (_ui.value.finished) return
        engine?.markSkipped()
        publish()
    }

    fun finish() {
        _ui.update { it.copy(finished = true) }
    }

    private fun publish() {
        val state = engine?.state ?: return
        val guessedWords = state.roundResults.filter { it.outcome == Outcome.GUESSED }.map { it.word }
        _ui.update {
            it.copy(word = state.currentWord, guessed = guessedWords.size, guessedWords = guessedWords)
        }
        state.currentWord?.id?.let(wordHistory::markShown)
    }
}
