package ru.limonella.alias.feature.themes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.limonella.alias.core.model.WordPack
import ru.limonella.alias.feature.setup.setupViewModel
import ru.limonella.alias.ui.components.PackCover

/**
 * Выбор тем плитками. Использует тот же SetupViewModel, что и экран настроек
 * ([viewModelStoreOwner] — запись экрана настроек в навигации), поэтому выбор сразу виден там.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onBack: () -> Unit,
) {
    val viewModel = setupViewModel(viewModelStoreOwner)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val allSelected = state.packs.isNotEmpty() && state.packs.all { it.id in state.selectedPackIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Темы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = { if (allSelected) viewModel.clearPacks() else viewModel.selectAllPacks() }) {
                        Text(if (allSelected) "Снять все" else "Выбрать все")
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .height(60.dp),
            ) {
                Text(
                    "Готово · ${state.selectedPackIds.size} тем, ${state.wordsInGame} слов",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.packs, key = { it.id }) { pack ->
                ThemeTile(
                    pack = pack,
                    selected = pack.id in state.selectedPackIds,
                    onClick = { viewModel.togglePack(pack.id) },
                )
            }
        }
    }
}

/** Плитка темы: обложка на всю плитку, название поверх. Невыбранные — затемнены. */
@Composable
private fun ThemeTile(pack: WordPack, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Box(Modifier.fillMaxSize()) {
            PackCover(pack = pack, modifier = Modifier.fillMaxSize())
            if (!selected) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))),
                    )
                    .padding(12.dp),
            ) {
                Text(
                    pack.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${pack.totalWords} слов",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                )
            }
        }
    }
}
