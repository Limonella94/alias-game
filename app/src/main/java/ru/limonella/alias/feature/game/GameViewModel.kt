package ru.limonella.alias.feature.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.limonella.alias.core.engine.GameEngine
import ru.limonella.alias.core.engine.GameState
import ru.limonella.alias.core.engine.Phase
import ru.limonella.alias.core.engine.WordDeck
import ru.limonella.alias.data.session.GameSessionStore
import ru.limonella.alias.data.words.WordRepository

/**
 * Связывает движок с экраном: держит таймер и превращает нажатия в действия движка.
 * Вся логика правил — в [GameEngine].
 */
class GameViewModel(
    private val sessionStore: GameSessionStore,
    private val wordRepository: WordRepository,
) : ViewModel() {

    data class UiState(
        val game: GameState? = null,
        /** Осталось секунд; null — раунд без таймера. */
        val secondsLeft: Int? = null,
        val paused: Boolean = false,
        val error: String? = null,
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var engine: GameEngine? = null
    private var timerJob: Job? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val config = sessionStore.pendingConfig
        if (config == null) {
            _ui.update { it.copy(error = "Игра не настроена. Вернитесь в меню и начните заново.") }
            return
        }
        val words = try {
            wordRepository.getWords(config.wordFilter)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _ui.update { it.copy(error = "Не удалось загрузить слова: ${e.message}") }
            return
        }
        if (words.isEmpty()) {
            _ui.update { it.copy(error = "Нет слов для выбранной сложности") }
            return
        }
        engine = GameEngine(config, WordDeck(words))
        publish()
    }

    fun startRound() {
        val e = engine ?: return
        e.startRound()
        _ui.update { it.copy(paused = false) }
        publish()
        startTimer(e.state.config.rules.roundSeconds)
    }

    fun markGuessed() {
        if (_ui.value.paused) return
        engine?.markGuessed()
        publish()
    }

    fun markSkipped() {
        if (_ui.value.paused) return
        engine?.markSkipped()
        publish()
    }

    fun togglePause() = _ui.update { it.copy(paused = !it.paused) }

    /** Ставим на паузу, когда приложение свернули посреди раунда. */
    fun pauseIfPlaying() {
        if (engine?.state?.phase == Phase.PLAYING) _ui.update { it.copy(paused = true) }
    }

    /** Досрочно завершить раунд (нужно для режима без таймера). */
    fun finishRound() {
        timerJob?.cancel()
        engine?.timeUp()
        publish()
    }

    fun resolveLastWord(teamId: Int?) {
        engine?.resolveLastWord(teamId)
        publish()
    }

    fun toggleResult(index: Int) {
        engine?.toggleResult(index)
        publish()
    }

    fun setLastWordTeam(teamId: Int?) {
        engine?.setLastWordTeam(teamId)
        publish()
    }

    fun confirmRound() {
        engine?.confirmRound()
        publish()
    }

    private fun startTimer(totalSeconds: Int?) {
        timerJob?.cancel()
        _ui.update { it.copy(secondsLeft = totalSeconds) }
        if (totalSeconds == null) return
        timerJob = viewModelScope.launch {
            var left = totalSeconds
            while (left > 0) {
                delay(1_000)
                if (!_ui.value.paused) {
                    left--
                    _ui.update { it.copy(secondsLeft = left) }
                }
            }
            engine?.timeUp()
            publish()
        }
    }

    private fun publish() {
        _ui.update { it.copy(game = engine?.state) }
    }
}
