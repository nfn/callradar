package me.ligaram.app.ui.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.ligaram.app.data.OverlayPreferences
import me.ligaram.app.ui.screens.AboutScreen
import me.ligaram.app.ui.screens.AddCommentScreen
import me.ligaram.app.ui.screens.CommunityNumberScreen
import me.ligaram.app.ui.screens.OverlayStyleScreen
import me.ligaram.app.ui.screens.SettingsScreen
import me.ligaram.app.ui.screens.WalkthroughScreen

@Composable
fun AppNavigation(
    initialCommunityNumber: String? = null,
    onInitialCommunityNumberConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val shouldShowWalkthrough =
        !OverlayPreferences.wasWalkthroughSeen(context) && initialCommunityNumber.isNullOrBlank()

    // Navegar para CommunityNumberScreen se a app foi aberta pela notificação
    LaunchedEffect(initialCommunityNumber) {
        if (!initialCommunityNumber.isNullOrBlank()) {
            navController.navigate("${Routes.COMMUNITY_NUMBER}/$initialCommunityNumber")
            onInitialCommunityNumberConsumed()
        }
    }

    // Arranca no walkthrough apenas na primeira instalação. Se abriu por
    // notificação, mantém o fluxo direto para não bloquear a navegação de detalhe.
    NavHost(
        navController    = navController,
        startDestination = if (shouldShowWalkthrough) Routes.WALKTHROUGH else Routes.HOME,
        modifier         = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable(Routes.WALKTHROUGH,
            enterTransition = {
                fadeIn(tween(ANIM_DURATION, easing = EaseInOut))
            },
            exitTransition = {
                fadeOut(tween(ANIM_DURATION, easing = EaseInOut))
            }
        ) {
            WalkthroughScreen(
                onFinish = {
                    OverlayPreferences.markWalkthroughSeen(context)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WALKTHROUGH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

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
