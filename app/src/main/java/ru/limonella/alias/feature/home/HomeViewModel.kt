package ru.limonella.alias.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import ru.limonella.alias.data.session.GameSessionStore
import ru.limonella.alias.data.session.SavedGame
import ru.limonella.alias.data.session.SavedGameStore
import kotlinx.coroutines.launch
import ru.limonella.alias.data.update.UpdateInfo
import ru.limonella.alias.data.update.UpdateManager
import java.io.File

/** Состояние баннера обновления на главном экране. */
sealed interface UpdateUi {
    data object None : UpdateUi
    data class Available(val info: UpdateInfo) : UpdateUi
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateUi
    data class Ready(val info: UpdateInfo, val file: File) : UpdateUi
    data class Failed(val info: UpdateInfo, val message: String) : UpdateUi
}

class HomeViewModel(
    private val updates: UpdateManager,
    private val savedGames: SavedGameStore,
    private val sessionStore: GameSessionStore,
) : ViewModel() {

    /** Незаконченная игра, если есть. */
    val savedGame: StateFlow<SavedGame?> = savedGames.saved
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Подготовить продолжение игры; после этого открывается экран игры. */
    fun continueGame(game: SavedGame) {
        sessionStore.resume = game
    }

    fun discardSavedGame() = savedGames.clear()

    private val _update = MutableStateFlow<UpdateUi>(UpdateUi.None)
    val update: StateFlow<UpdateUi> = _update.asStateFlow()

    init {
        viewModelScope.launch {
            updates.checkForUpdate()?.let { _update.value = UpdateUi.Available(it) }
        }
    }

    /** Нажата кнопка в баннере. */
    fun onUpdateClick() {
        when (val s = _update.value) {
            is UpdateUi.Available -> download(s.info)
            is UpdateUi.Failed -> download(s.info)
            is UpdateUi.Ready -> install(s.file)
            is UpdateUi.Downloading, UpdateUi.None -> Unit
        }
    }

    private fun download(info: UpdateInfo) {
        viewModelScope.launch {
            _update.value = UpdateUi.Downloading(info, 0f)
            try {
                val file = updates.download(info) { p -> _update.value = UpdateUi.Downloading(info, p) }
                _update.value = UpdateUi.Ready(info, file)
                install(file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _update.value = UpdateUi.Failed(info, "Не удалось скачать: ${e.message}")
            }
        }
    }

    private fun install(file: File) {
        if (!updates.canInstall()) {
            // Первый раз Android попросит разрешить установку из этого приложения.
            // После разрешения нужно вернуться и нажать «Установить» ещё раз.
            updates.openInstallPermissionSettings()
            return
        }
        updates.install(file)
    }
}
