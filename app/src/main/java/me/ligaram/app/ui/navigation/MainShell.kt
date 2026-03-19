package me.ligaram.app.ui.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.ligaram.app.ui.permissions.rememberPermissionUiState
import me.ligaram.app.ui.screens.CommunityHomeScreen
import me.ligaram.app.ui.screens.HomeScreen
import me.ligaram.app.ui.screens.SettingsScreen
import me.ligaram.app.ui.theme.AccentBlue

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
    BackHandler(enabled = selectedTab == 1 || selectedTab == 2) {
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
