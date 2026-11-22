package ru.limonella.alias.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.json.Json
import ru.limonella.alias.AliasApp
import ru.limonella.alias.data.session.GameSessionStore
import ru.limonella.alias.data.words.AssetWordRepository
import ru.limonella.alias.data.words.WordRepository

/**
 * Ручное внедрение зависимостей: все «сервисы» приложения создаются здесь.
 * Когда проект вырастет, можно заменить на Hilt — экраны почти не изменятся.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val json: Json = Json { ignoreUnknownKeys = true }

    val wordRepository: WordRepository by lazy { AssetWordRepository(appContext, json) }

    val gameSessionStore: GameSessionStore by lazy { GameSessionStore() }
}

/** Достать контейнер из любого экрана. */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as AliasApp).container
