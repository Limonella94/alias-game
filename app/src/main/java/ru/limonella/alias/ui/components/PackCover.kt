package ru.limonella.alias.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.limonella.alias.core.model.WordPack

/**
 * Обложка темы.
 * Если у темы задана картинка ([WordPack.cover] — путь в assets) — показываем её.
 * Иначе — градиент цвета темы со значком-эмодзи.
 * На этапе дизайна достаточно положить картинки в assets/covers и прописать "cover" в JSON набора.
 */
@Composable
fun PackCover(pack: WordPack, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, pack.cover) {
        val path = pack.cover
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

    val base = pack.colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(base, lerp(base, Color.Black, 0.35f))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = image
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = pack.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(pack.emoji, fontSize = 40.sp)
        }
    }
}
