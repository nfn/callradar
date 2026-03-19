package me.ligaram.app.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import me.ligaram.app.data.OverlayPreferences
import me.ligaram.app.service.CallMonitorService
import me.ligaram.app.ui.components.AppBackground
import me.ligaram.app.ui.components.PermissionDialog
import me.ligaram.app.ui.components.PermissionRow
import me.ligaram.app.ui.permissions.PermissionUiState
import me.ligaram.app.ui.permissions.rememberPermissionUiState
import me.ligaram.app.ui.permissions.requestNotificationPermissionOrOpenSettings
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentGreen
import me.ligaram.app.ui.theme.AccentOrange
import me.ligaram.app.ui.theme.TextSecondary
import me.ligaram.app.ui.theme.ligaramColors

// ─── Navigation Routes ────────────────────────────────────────────────────────
object Routes {
    const val HOME             = "home"
    const val ABOUT            = "about"
    const val SETTINGS         = "settings"
    const val COMMUNITY_HOME   = "community_home"
    const val COMMUNITY_NUMBER = "community_number"
    const val ADD_COMMENT      = "add_comment"
    const val OVERLAY_STYLE    = "overlay_style"
}

// ─── Main Nav Host ────────────────────────────────────────────────────────────

// Constantes de animação partilhadas por todas as rotas
private const val ANIM_DURATION = 280
private const val SLIDE_OFFSET  = 0.30f   // 30% da largura — elimina a faixa lateral

@Composable
fun AppNavigation(
    initialCommunityNumber: String? = null,
    onInitialCommunityNumberConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Navegar para CommunityNumberScreen se a app foi aberta pela notificação
    androidx.compose.runtime.LaunchedEffect(initialCommunityNumber) {
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

// ─── Main shell com Bottom Navigation ────────────────────────────────────────
@Composable
fun MainShell(rootNav: NavController, startTab: Int = 0) {
    val permissionUiState = rememberPermissionUiState()
    val currentEntry = rootNav.currentBackStackEntry ?: return
    val forceCommunityInitial =
        currentEntry.savedStateHandle.get<Boolean>("force_community_tab") == true

    // Usa a flag já no 1o frame para evitar ver o tab Home por baixo durante a pop transition.
    // Sem forceCommunityInitial como chave: evita o reset duplo quando a flag é lida (true)
    // e logo limpa (false), causando dois rebuilds de rememberSaveable com selectedTab = 0.
    val selectedTabState = rememberSaveable { mutableIntStateOf(startTab) }
    // Rastreia o tab anterior para que o back regresse ao tab de onde o utilizador veio.
    val previousTabState = rememberSaveable { mutableIntStateOf(startTab) }
    val selectedTab = selectedTabState.intValue

    val forceCommunityTabFlow = currentEntry
        .savedStateHandle
        .getStateFlow("force_community_tab", false)

    // LaunchedEffect(Unit) corre uma vez por entrada na composição e captura
    // forceCommunityInitial via closure, sem causar re-key no rememberSaveable.
    LaunchedEffect(Unit) {
        if (forceCommunityInitial) {
            selectedTabState.intValue = 1
            currentEntry.savedStateHandle.set("force_community_tab", false)
        }
    }

    LaunchedEffect(forceCommunityTabFlow) {
        forceCommunityTabFlow.collect { forceCommunity ->
            if (forceCommunity) {
                selectedTabState.intValue = 1
                currentEntry.savedStateHandle.set("force_community_tab", false)
            }
        }
    }

    // Back quando estamos no tab Comunidade ou Definições → volta ao tab anterior.
    androidx.activity.compose.BackHandler(enabled = selectedTab == 1 || selectedTab == 2) {
        selectedTabState.intValue = previousTabState.intValue
        previousTabState.intValue = 0
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                        onClick  = {
                            if (selectedTab != 0) {
                                previousTabState.intValue = selectedTab
                                selectedTabState.intValue = 0
                            }
                        },
                    icon     = { Icon(Icons.Default.Shield, null) },
                    label    = { Text("Proteção") },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = AccentBlue,
                        selectedTextColor   = AccentBlue,
                        indicatorColor      = AccentBlue.copy(alpha = 0.12f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                        onClick  = {
                            if (selectedTab != 1) {
                                previousTabState.intValue = selectedTab
                                selectedTabState.intValue = 1
                            }
                        },
                    icon     = { Icon(Icons.Default.Forum, null) },
                    label    = { Text("Comunidade") },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = AccentBlue,
                        selectedTextColor   = AccentBlue,
                        indicatorColor      = AccentBlue.copy(alpha = 0.12f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                        onClick  = {
                            if (selectedTab != 2) {
                                previousTabState.intValue = selectedTab
                                selectedTabState.intValue = 2
                            }
                        },
                    icon     = { Icon(Icons.Default.Settings, null) },
                    label    = { Text("Definições") },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = AccentBlue,
                        selectedTextColor   = AccentBlue,
                        indicatorColor      = AccentBlue.copy(alpha = 0.12f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier    = Modifier.padding(innerPadding),
            transitionSpec = {
                val toRight = targetState > initialState
                (fadeIn(tween(ANIM_DURATION, easing = EaseInOut)) +
                    slideInHorizontally(tween(ANIM_DURATION, easing = EaseOut)) {
                        if (toRight) (it * SLIDE_OFFSET).toInt() else -(it * SLIDE_OFFSET).toInt()
                    }) togetherWith
                (fadeOut(tween(ANIM_DURATION, easing = EaseInOut)) +
                    slideOutHorizontally(tween(ANIM_DURATION, easing = EaseIn)) {
                        if (toRight) -(it * SLIDE_OFFSET).toInt() else (it * SLIDE_OFFSET).toInt()
                    })
            },
            label = "tab_transition"
        ) { tab ->
            when (tab) {
                0 -> HomeScreen(permissionUiState)
                1 -> CommunityHomeScreen(rootNav)
                2 -> SettingsScreen(rootNav, permissionUiState)
            }
        }
    }
}



