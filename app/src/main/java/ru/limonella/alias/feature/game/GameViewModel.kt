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
import ru.limonella.alias.core.sound.GameSound
import ru.limonella.alias.core.sound.SoundPlayer
import ru.limonella.alias.data.history.WordHistoryStore
import ru.limonella.alias.data.session.GameSessionStore
import ru.limonella.alias.data.session.SavedGame
import ru.limonella.alias.data.session.SavedGameStore
import ru.limonella.alias.data.words.WordRepository

/**
 * Связывает движок с экраном: держит таймер, звуки, автосохранение и историю слов.
 * Вся логика правил — в [GameEngine].
 */
class GameViewModel(
    private val sessionStore: GameSessionStore,
    private val wordRepository: WordRepository,
    private val soundPlayer: SoundPlayer,
    private val savedGames: SavedGameStore,
    private val wordHistory: WordHistoryStore,
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
    private var soundEnabled = true
    private var timerJob: Job? = null
    private var lastMarkedWordId: String? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        // Либо продолжаем сохранённую игру, либо начинаем новую по настройкам.
        val resume = sessionStore.resume
        sessionStore.resume = null
        val config = resume?.state?.config ?: sessionStore.pendingConfig
        if (config == null) {
            _ui.update { it.copy(error = "Игра не настроена. Вернитесь в меню и начните заново.") }
            return
        }
        // Чтобы «Реванш» после продолжения игры работал с теми же настройками.
        sessionStore.pendingConfig = config

        val words = try {
            wordRepository.getWords(config.wordFilter)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _ui.update { it.copy(error = "Не удалось загрузить слова: ${e.message}") }
            return
        }
        if (words.isEmpty()) {
            if (resume != null) savedGames.clear()
            _ui.update { it.copy(error = "Нет слов для выбранных тем и сложности") }
            return
        }
        val history = runCatching { wordHistory.lastShown() }.getOrDefault(emptyMap())
        val deck = WordDeck(words, lastShown = history)
        soundEnabled = config.soundEnabled

        if (resume != null) {
            val restored = GameEngine.restore(resume.state, deck)
            engine = restored
            lastMarkedWordId = resume.state.currentWord?.id
            if (restored.state.phase == Phase.PLAYING) {
                // Раунд прервали посреди — продолжаем с паузы, с тем же временем.
                _ui.update { it.copy(paused = true) }
                publish()
                startTimer(resume.secondsLeft)
                return
            }
        } else {
            engine = GameEngine(config, deck)
        }
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
        if (_ui.value.paused || !isPhase(Phase.PLAYING)) return
        engine?.markGuessed()
        sound(GameSound.GUESSED)
        publish()
    }

    fun markSkipped() {
        if (_ui.value.paused || !isPhase(Phase.PLAYING)) return
        engine?.markSkipped()
        sound(GameSound.SKIPPED)
        publish()
    }

    fun togglePause() {
        _ui.update { it.copy(paused = !it.paused) }
        persist()
    }

    /** Ставим на паузу, когда приложение свернули посреди раунда. */
    fun pauseIfPlaying() {
        if (isPhase(Phase.PLAYING)) _ui.update { it.copy(paused = true) }
        persist()
    }

    /** Досрочно завершить раунд (нужно для режима без таймера). */
    fun finishRound() {
        timerJob?.cancel()
        engine?.timeUp()
        publish()
    }

    fun resolveLastWord(teamId: Int?) {
        if (!isPhase(Phase.LAST_WORD)) return
        engine?.resolveLastWord(teamId)
        if (!isPhase(Phase.LAST_WORD)) {
            sound(if (teamId != null) GameSound.GUESSED else GameSound.SKIPPED)
        }
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
                    if (left in 1..3) sound(GameSound.TICK)
                    if (left % 5 == 0) persist()
                }
            }
            sound(GameSound.TIME_UP)
            engine?.timeUp()
            publish()
        }
    }

    private fun isPhase(phase: Phase): Boolean = engine?.state?.phase == phase

    private fun sound(sound: GameSound) {
        if (soundEnabled) soundPlayer.play(sound)
    }

    private fun publish() {
        _ui.update { it.copy(game = engine?.state) }
        markCurrentWordShown()
        persist()
    }

    /** Запоминаем каждое показанное слово, чтобы в следующих играх сначала шли новые. */
    private fun markCurrentWordShown() {
        val id = engine?.state?.currentWord?.id ?: return
        if (id == lastMarkedWordId) return
        lastMarkedWordId = id
        wordHistory.markShown(id)
    }

    /** Автосохранение: незаконченная игра переживёт закрытие приложения. Законченная — удаляется. */
    private fun persist() {
        val state = engine?.state ?: return
        if (state.phase == Phase.FINISHED) {
            savedGames.clear()
        } else {
            savedGames.save(
                SavedGame(
                    state = state,
                    secondsLeft = _ui.value.secondsLeft,
                    savedAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}
