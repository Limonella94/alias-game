package ru.limonella.alias.ui.components

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Картинка из папки assets (обложки тем, детские картинки).
 * Грузится в фоне; пока не загрузилась или если файла нет — значение null.
 */
@Composable
fun rememberAssetImage(path: String?): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, path) {
        value = if (path == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open(path).use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                }.getOrNull()
            }
        }
    }
}
