package ru.limonella.alias.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import ru.limonella.alias.AliasApp
import ru.limonella.alias.core.sound.SoundPlayer
import ru.limonella.alias.data.history.WordHistoryData
import ru.limonella.alias.data.history.WordHistoryStore
import ru.limonella.alias.data.session.GameSessionStore
import ru.limonella.alias.data.session.SavedGame
import ru.limonella.alias.data.session.SavedGameStore
import ru.limonella.alias.data.settings.SetupSnapshot
import ru.limonella.alias.data.sound.SoundPoolPlayer
import ru.limonella.alias.data.storage.JsonPreference
import ru.limonella.alias.data.storage.aliasDataStore
import ru.limonella.alias.data.update.UpdateManager
import ru.limonella.alias.data.words.AssetWordRepository
import ru.limonella.alias.data.words.WordRepository

/**
 * Ручное внедрение зависимостей: все «сервисы» приложения создаются здесь.
 * Когда проект вырастет, можно заменить на Hilt — экраны почти не изменятся.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** Фоновые задачи, которые должны доживать до конца, даже если экран закрыли (сохранение). */
    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dataStore by lazy { appContext.aliasDataStore }

    val wordRepository: WordRepository by lazy { AssetWordRepository(appContext, json) }

    val gameSessionStore: GameSessionStore by lazy { GameSessionStore() }

    val soundPlayer: SoundPlayer by lazy { SoundPoolPlayer(appContext) }

    val updateManager: UpdateManager by lazy { UpdateManager(appContext, json) }

    val setupSettings: JsonPreference<SetupSnapshot> by lazy {
        JsonPreference(dataStore, "setup", SetupSnapshot.serializer(), json)
    }

    val savedGameStore: SavedGameStore by lazy {
        SavedGameStore(JsonPreference(dataStore, "saved_game", SavedGame.serializer(), json), appScope)
    }

    val wordHistory: WordHistoryStore by lazy {
        WordHistoryStore(JsonPreference(dataStore, "word_history", WordHistoryData.serializer(), json), appScope)
    }
}

/** Достать контейнер из любого экрана. */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as AliasApp).container
