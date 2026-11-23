package ru.limonella.alias.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.limonella.alias.BuildConfig
import ru.limonella.alias.di.appContainer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onPlay: () -> Unit,
    onKids: () -> Unit,
) {
    val container = appContainer()
    val viewModel: HomeViewModel = viewModel { HomeViewModel(container.updateManager) }
    val update by viewModel.update.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UpdateBanner(update = update, onClick = viewModel::onUpdateClick)
        Spacer(Modifier.weight(1f))
        Text(
            text = "Alias",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Объясни слово, не называя его",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(48.dp))
        ModeCard(
            title = "Играть",
            subtitle = "Команды, таймер, очки",
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onPlay,
        )
        Spacer(Modifier.height(16.dp))
        ModeCard(
            title = "Детский режим",
            subtitle = "Для малышей от 3 лет · скоро",
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            onClick = onKids,
        )
        Spacer(Modifier.weight(1f))
        Text(
            "Версия ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Баннер «Доступно обновление». Пока обновлений нет — ничего не показывает. */
@Composable
private fun UpdateBanner(update: UpdateUi, onClick: () -> Unit) {
    val info = when (update) {
        UpdateUi.None -> return
        is UpdateUi.Available -> update.info
        is UpdateUi.Downloading -> update.info
        is UpdateUi.Ready -> update.info
        is UpdateUi.Failed -> update.info
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Доступна новая версия ${info.versionName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (info.notes.isNotBlank()) {
                Text(info.notes, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Spacer(Modifier.height(8.dp))
            when (update) {
                is UpdateUi.Downloading -> {
                    LinearProgressIndicator(
                        progress = { update.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is UpdateUi.Failed -> {
                    Text(update.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onClick) { Text("Повторить") }
                }
                is UpdateUi.Ready -> Button(onClick = onClick) { Text("Установить") }
                else -> Button(onClick = onClick) { Text("Обновить") }
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
