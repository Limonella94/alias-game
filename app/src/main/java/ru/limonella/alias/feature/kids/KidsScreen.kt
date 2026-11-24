package ru.limonella.alias.feature.kids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.limonella.alias.core.model.KidsPresets
import ru.limonella.alias.di.appContainer
import ru.limonella.alias.ui.components.BigButton
import androidx.compose.material3.TopAppBarDefaults
import ru.limonella.alias.R
import ru.limonella.alias.ui.components.ScreenBackground
import ru.limonella.alias.ui.components.GlassCard
import ru.limonella.alias.ui.components.GlassButton
import ru.limonella.alias.ui.theme.GlassStrong
import ru.limonella.alias.ui.theme.MoonSilver
import ru.limonella.alias.ui.theme.FogGray
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

/** Вход в детский режим: выбор возраста. Для деток 5–6 — ещё «все вместе» или «две команды». */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsScreen(
    onBack: () -> Unit,
    onLittle: () -> Unit,
    onStartBigGame: () -> Unit,
) {
    val container = appContainer()
    var bigExpanded by rememberSaveable { mutableStateOf(false) }

    fun startBig(twoTeams: Boolean) {
        container.gameSessionStore.resume = null
        container.gameSessionStore.pendingConfig = KidsPresets.big(twoTeams)
        onStartBigGame()
    }

    ScreenBackground(image = R.drawable.bg_kids) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { Text("Детский режим") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Небо с луной сверху остаётся открытым — заголовок ниже.
                Spacer(Modifier.height(120.dp))
                Text("Кто играет?", style = MaterialTheme.typography.headlineMedium, color = MoonSilver)

                AgeCard(
                    emoji = "🐣",
                    title = "Малыши 3–5",
                    description = "Взрослый объясняет картинку звуками и жестами, малыши угадывают. Без таймера.",
                    color = Color(0xFFFFC145),
                    onClick = onLittle,
                )

                AgeCard(
                    emoji = "🧒",
                    title = "Детки 5–6",
                    description = "Дети объясняют сами по картинке. Таймер 90 секунд, без штрафов.",
                    color = Color(0xFF7DD3FC),
                    onClick = { bigExpanded = !bigExpanded },
                )
                if (bigExpanded) {
                    Text("Как играем?", style = MaterialTheme.typography.titleLarge)
                    BigButton(text = "Все вместе", onClick = { startBig(twoTeams = false) })
                    GlassButton(
                        text = "Две команды: 🐰 и 🐻",
                        onClick = { startBig(twoTeams = true) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AgeCard(
    emoji: String,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = GlassStrong,
        borderColor = color.copy(alpha = 0.7f),
        borderWidth = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 56.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = MoonSilver)
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MoonSilver.copy(alpha = 0.85f))
            }
        }
    }
}
