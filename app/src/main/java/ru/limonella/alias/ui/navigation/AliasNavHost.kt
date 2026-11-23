package ru.limonella.alias.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import ru.limonella.alias.feature.game.GameScreen
import ru.limonella.alias.feature.home.HomeScreen
import ru.limonella.alias.feature.kids.KidsScreen
import ru.limonella.alias.feature.setup.SetupScreen
import ru.limonella.alias.feature.themes.ThemesScreen

// Маршруты (экраны) приложения. Новый экран = новый объект здесь + composable<> ниже.
@Serializable data object HomeRoute
@Serializable data object SetupRoute
@Serializable data object GameRoute
@Serializable data object KidsRoute
@Serializable data object ThemesRoute

@Composable
fun AliasNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onPlay = { navController.navigate(SetupRoute) },
                onKids = { navController.navigate(KidsRoute) },
            )
        }
        composable<SetupRoute> {
            SetupScreen(
                onBack = { navController.popBackStack() },
                onStartGame = { navController.navigate(GameRoute) },
                onOpenThemes = { navController.navigate(ThemesRoute) },
            )
        }
        composable<ThemesRoute> { entry ->
            // Экран тем делит ViewModel с экраном настроек.
            val setupEntry = remember(entry) { navController.getBackStackEntry<SetupRoute>() }
            ThemesScreen(
                viewModelStoreOwner = setupEntry,
                onBack = { navController.popBackStack() },
            )
        }
        composable<GameRoute> {
            GameScreen(
                onExitToMenu = { navController.popBackStack(route = HomeRoute, inclusive = false) },
                onNewGame = { navController.popBackStack(route = SetupRoute, inclusive = false) },
            )
        }
        composable<KidsRoute> {
            KidsScreen(onBack = { navController.popBackStack() })
        }
    }
}
