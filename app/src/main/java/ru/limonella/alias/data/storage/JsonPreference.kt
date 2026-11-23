package ru.limonella.alias.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Единственный файл настроек приложения (DataStore). */
val Context.aliasDataStore: DataStore<Preferences> by preferencesDataStore(name = "alias")

/**
 * Одно значение, хранящееся в DataStore как JSON.
 * Если сохранённые данные не читаются (поменялся формат) — считаем, что значения нет.
 */
class JsonPreference<T : Any>(
    private val dataStore: DataStore<Preferences>,
    name: String,
    private val serializer: KSerializer<T>,
    private val json: Json,
) {
    private val key = stringPreferencesKey(name)

    val flow: Flow<T?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[key]?.let(::decode) }

    suspend fun get(): T? = flow.first()

    suspend fun set(value: T?) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(key) else prefs[key] = json.encodeToString(serializer, value)
        }
    }

    /** Атомарно прочитать-изменить-записать. */
    suspend fun update(transform: (T?) -> T?) {
        dataStore.edit { prefs ->
            val updated = transform(prefs[key]?.let(::decode))
            if (updated == null) prefs.remove(key) else prefs[key] = json.encodeToString(serializer, updated)
        }
    }

    private fun decode(text: String): T? =
        runCatching { json.decodeFromString(serializer, text) }.getOrNull()
}
