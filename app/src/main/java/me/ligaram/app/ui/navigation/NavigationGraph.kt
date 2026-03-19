package me.ligaram.app.ui.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.ligaram.app.ui.screens.AboutScreen
import me.ligaram.app.ui.screens.AddCommentScreen
import me.ligaram.app.ui.screens.CommunityHomeScreen
import me.ligaram.app.ui.screens.CommunityNumberScreen
import me.ligaram.app.ui.screens.OverlayStyleScreen
import me.ligaram.app.ui.screens.SettingsScreen

@Composable
fun AppNavigation(
    initialCommunityNumber: String? = null,
    onInitialCommunityNumberConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Navegar para CommunityNumberScreen se a app foi aberta pela notificação
    LaunchedEffect(initialCommunityNumber) {
        if (!initialCommunityNumber.isNullOrBlank()) {
            navController.navigate("${Routes.COMMUNITY_NUMBER}/$initialCommunityNumber")
            onInitialCommunityNumberConsumed()
        }
    }

    // A app arranca sempre no HOME - as permissões são opcionais e activadas
    // a partir do status card da tab Proteção.
    NavHost(
        navController    = navController,
        startDestination = Routes.HOME,
        modifier         = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        // HOME e COMMUNITY_HOME são o mesmo shell - apenas diferem no tab inicial
        composable(Routes.HOME,
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { (it * SLIDE_OFFSET).toInt() }
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { -(it * SLIDE_OFFSET).toInt() }
            }
        ) { MainShell(navController, startTab = 0) }

        composable(Routes.COMMUNITY_HOME,
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { (it * SLIDE_OFFSET).toInt() }
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { -(it * SLIDE_OFFSET).toInt() }
            }
        ) { MainShell(navController, startTab = 1) }

        composable(Routes.ABOUT,
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { (it * SLIDE_OFFSET).toInt() }
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { -(it * SLIDE_OFFSET).toInt() }
            }
        ) { AboutScreen(navController) }

        composable(Routes.SETTINGS,
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { (it * SLIDE_OFFSET).toInt() }
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { -(it * SLIDE_OFFSET).toInt() }
            }
        ) { SettingsScreen(navController) }

        composable(Routes.OVERLAY_STYLE,
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { (it * SLIDE_OFFSET).toInt() }
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { -(it * SLIDE_OFFSET).toInt() }
            }
        ) { OverlayStyleScreen(navController) }

        // Página de detalhe de número
        composable("${Routes.COMMUNITY_NUMBER}/{number}",
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { (it * SLIDE_OFFSET).toInt() }
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { -(it * SLIDE_OFFSET).toInt() }
            },
            popEnterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { -(it * SLIDE_OFFSET).toInt() }
            },
            popExitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { (it * SLIDE_OFFSET).toInt() }
            }
        ) { back ->
            val number = back.arguments?.getString("number") ?: ""
            CommunityNumberScreen(navController, number)
        }

        // Screen para adicionar comentário
        composable("${Routes.ADD_COMMENT}/{number}",
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { (it * SLIDE_OFFSET).toInt() }
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { -(it * SLIDE_OFFSET).toInt() }
            },
            popEnterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) { -(it * SLIDE_OFFSET).toInt() }
            },
            popExitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) { (it * SLIDE_OFFSET).toInt() }
            }
        ) { back ->
            val number = back.arguments?.getString("number") ?: ""
            AddCommentScreen(navController, number)
        }
    }
}
