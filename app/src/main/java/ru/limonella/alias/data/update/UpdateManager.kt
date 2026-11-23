package ru.limonella.alias.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.limonella.alias.BuildConfig
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Описание последней сборки — файл version.json, который CI кладёт рядом с APK. */
@Serializable
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val notes: String = "",
)

/**
 * Проверка и установка обновлений из релиза `latest` на GitHub.
 * Работает, только если репозиторий публичный; иначе проверка тихо ничего не находит.
 */
class UpdateManager(
    private val context: Context,
    private val json: Json,
) {

    /** Новая версия, если она есть; null — обновлений нет или проверить не удалось. */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val text = open(VERSION_URL).let { conn ->
                try {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } finally {
                    conn.disconnect()
                }
            }
            json.decodeFromString(UpdateInfo.serializer(), text)
        }.getOrNull()?.takeIf { it.versionCode > BuildConfig.VERSION_CODE }
    }

    /** Скачивает APK во внутреннюю папку приложения. [onProgress] — от 0 до 1. */
    suspend fun download(info: UpdateInfo, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val file = File(dir, "alias.apk")
        val conn = open(info.apkUrl)
        try {
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        done += read
                        if (total > 0) onProgress(done.toFloat() / total)
                    }
                }
            }
        } finally {
            conn.disconnect()
        }
        file
    }

    /** Разрешил ли пользователь этому приложению устанавливать APK. */
    fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    /** Открыть системную настройку «Разрешить установку из этого источника». */
    fun openInstallPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Открыть системный установщик. Подтверждение установки Android всегда спрашивает сам. */
    fun install(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun open(url: String): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 30_000
        conn.instanceFollowRedirects = true
        val code = conn.responseCode
        if (code != HttpURLConnection.HTTP_OK) {
            conn.disconnect()
            throw IOException("HTTP $code")
        }
        return conn
    }

    companion object {
        const val VERSION_URL =
            "https://github.com/Limonella94/alias-game/releases/download/latest/version.json"
    }
}
