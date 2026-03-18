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

// ─── Permission helpers ───────────────────────────────────────────────────────
fun hasPhonePermissions(context: android.content.Context): Boolean {
    val perms = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS
    )
    return perms.all {
        context.checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

fun hasOverlayPermission(context: android.content.Context): Boolean =
    Settings.canDrawOverlays(context)

fun allPermissionsGranted(context: android.content.Context): Boolean =
    hasPhonePermissions(context) && hasOverlayPermission(context)

private class PermissionUiState(
    private val context: android.content.Context
) {
    var phoneGranted by mutableStateOf(hasPhonePermissions(context))
        private set
    var overlayGranted by mutableStateOf(hasOverlayPermission(context))
        private set
    var suggestComment by mutableStateOf(OverlayPreferences.getSuggestComment(context))
        private set

    val allCoreGranted: Boolean
        get() = phoneGranted && overlayGranted

    fun refreshCorePermissions() {
        phoneGranted = hasPhonePermissions(context)
        overlayGranted = hasOverlayPermission(context)
    }

    fun updateSuggestComment(enabled: Boolean) {
        suggestComment = enabled
        OverlayPreferences.setSuggestComment(context, enabled)
    }
}

@Composable
private fun rememberPermissionUiState(): PermissionUiState {
    val context = LocalContext.current
    return remember(context) { PermissionUiState(context) }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun requestNotificationPermissionOrOpenSettings(
    context: android.content.Context,
    permissionState: PermissionState
) {
    if (permissionState.status.isGranted) return

    val askedBefore = OverlayPreferences.wasNotificationPermissionAsked(context)
    val shouldShowRationale = permissionState.status.shouldShowRationale
    if (!askedBefore || shouldShowRationale) {
        OverlayPreferences.markNotificationPermissionAsked(context)
        permissionState.launchPermissionRequest()
    } else {
        // Já foi pedido antes e não há rationale: provavelmente bloqueado em definições.
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }
}

// ─── Main Nav Host ────────────────────────────────────────────────────────────

// Constantes de animação partilhadas por todas as rotas
private const val ANIM_DURATION = 280
private const val SLIDE_OFFSET  = 0.30f   // 30% da largura — elimina a faixa lateral

@Composable
fun AppNavigation(
    initialAddComment: String? = null,
    onInitialAddCommentConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Navegar para AddCommentScreen se a app foi aberta pela notificação de sugestão
    androidx.compose.runtime.LaunchedEffect(initialAddComment) {
        if (!initialAddComment.isNullOrBlank()) {
            navController.navigate("${Routes.ADD_COMMENT}/$initialAddComment")
            onInitialAddCommentConsumed()
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
    val selectedTabState = rememberSaveable(startTab, forceCommunityInitial) {
        mutableIntStateOf(if (forceCommunityInitial) 1 else startTab)
    }
    val selectedTab = selectedTabState.intValue

    val forceCommunityTabFlow = currentEntry
        .savedStateHandle
        .getStateFlow("force_community_tab", false)

    LaunchedEffect(forceCommunityInitial) {
        if (forceCommunityInitial) {
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

    // Botão/gesto back quando estamos no tab Comunidade ou Definições → volta ao tab Proteção
    // Em qualquer tab → não sai da app (comportamento padrão do sistema)
    androidx.activity.compose.BackHandler(enabled = selectedTab == 1 || selectedTab == 2) {
        selectedTabState.intValue = 0
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
                    onClick  = { selectedTabState.intValue = 0 },
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
                    onClick  = { selectedTabState.intValue = 1 },
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
                    onClick  = { selectedTabState.intValue = 2 },
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

// ─── Shared background ────────────────────────────────────────────────────────
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(ligaramColors.bgPrimary, ligaramColors.bgSecondary)))
    ) { content() }
}

// ─── Home Screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HomeScreen(
    permissionUiState: PermissionUiState = rememberPermissionUiState()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val phoneGranted = permissionUiState.phoneGranted
    val overlayGranted = permissionUiState.overlayGranted
    val allGood = permissionUiState.allCoreGranted

    // Diálogo de activação de permissões (abre ao clicar no status card)
    val showPermDialog = remember { mutableStateOf(false) }

    // Atualiza estado ao regressar ao ecrã (ex.: após definições do sistema)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionUiState.refreshCorePermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(allGood) {
        if (allGood) {
            try {
                val svc = Intent(context, CallMonitorService::class.java).apply {
                    action = CallMonitorService.ACTION_START
                }
                context.startForegroundService(svc)
            } catch (_: Exception) {}
            // Notificações: geridas nas Definições ao tocar no toggle inativo
        }
    }

    if (showPermDialog.value) {
        PermissionDialog(
            phoneGranted   = phoneGranted,
            overlayGranted = overlayGranted,
            onDismiss      = { showPermDialog.value = false }
        )
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LigaramLogo(size = 72.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text("CallRadar", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("por ligaram.me", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Proteção contra chamadas indesejadas", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(36.dp))

            // ── Status card - clicável quando as permissões não estão completas ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = if (allGood) AccentGreen.copy(alpha = 0.1f) else AccentOrange.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, if (allGood) AccentGreen.copy(alpha = 0.4f) else AccentOrange.copy(alpha = 0.4f)),
                onClick = { showPermDialog.value = true }
            ) {
                Row(
                    modifier          = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (allGood) Icons.Default.CheckCircle else Icons.Default.Warning,
                        null,
                        tint     = if (allGood) AccentGreen else AccentOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (allGood) "Proteção ativa" else "Proteção inativa",
                            color      = if (allGood) AccentGreen else AccentOrange,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                        Text(
                            if (allGood)
                                "Chamadas recebidas serão identificadas automaticamente · Toca para rever as permissões"
                            else
                                "Toque aqui para ativar a identificação de chamadas",
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    if (!allGood) {
                        Icon(
                            Icons.Default.ChevronRight, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Como funciona", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))

            listOf(
                Triple(Icons.Default.PhoneInTalk, "Chamada recebida",        "A app deteta automaticamente quem está a ligar"),
                Triple(Icons.Default.Search,       "Consulta a base de dados","O número é verificado em tempo real no ligaram.me"),
                Triple(Icons.Default.Layers,       "Overlay apresentado",     "Se houver resultado, mostramos risco e categoria sobre o ecrã"),
                Triple(Icons.Default.Block,        "Proteja-se",              "Decida com informação se atende ou rejeita a chamada"),
                Triple(Icons.Default.Notifications,"Notificação de comentário",  "Após chamadas curtas de números desconhecidos, convidamos a partilhar a experiência")
            ).forEachIndexed { idx, (icon, title, desc) ->
                HowItWorksStep(icon = icon, title = title, description = desc)
                if (idx < 4) Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Diálogo inline de activação de permissões ────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog(
    phoneGranted: Boolean,
    overlayGranted: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val phonePermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )
    )

    // Permissão opcional de notificações (Android 13+)
    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    } else null
    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        notifPermission?.status?.isGranted == true
    } else {
        true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(20.dp),
        containerColor   = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Shield, null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                Text("Ativar Proteção", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ativa as permissões necessárias para que a app funcione corretamente:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)

                // ── Permissão 1: Chamadas ─────────────────────────────────────
                PermissionRow(
                    icon    = Icons.Default.Phone,
                    title   = "Acesso às chamadas",
                    desc    = "Detecta chamadas recebidas em tempo real",
                    granted = phoneGranted || phonePermissions.allPermissionsGranted,
                    onAction    = { phonePermissions.launchMultiplePermissionRequest() }
                )

                // ── Permissão 2: Overlay ──────────────────────────────────────
                PermissionRow(
                    icon    = Icons.Default.Layers,
                    title   = "Mostrar sobre outras apps",
                    desc    = "Exibe informação durante a chamada",
                    granted = overlayGranted,
                    onAction    = {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri()))
                    }
                )

                // ── Permissão 3: Notificações (opcional) ──────────────────────
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionRow(
                        icon    = Icons.Default.Notifications,
                        title   = "Notificações de comentário",
                        desc    = "Recebe notificação para comentar chamadas muito curtas de números desconhecidos.",
                        granted = hasNotificationPermission,
                        onAction    = {
                            notifPermission?.let {
                                requestNotificationPermissionOrOpenSettings(context, it)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ─── Linha de permissão no diálogo ────────────────────────────────────────────
@Composable
fun PermissionRow(
    icon: ImageVector,
    title: String,
    desc: String,
    granted: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (granted) AccentGreen.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(enabled = !granted, onClick = onAction)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (granted) AccentGreen.copy(alpha = 0.15f) else AccentBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint     = if (granted) AccentGreen else AccentBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc,  color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
        }
        if (granted) {
            Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun HowItWorksStep(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

// ─── Settings Screen ─────────────────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SettingsScreen(
    navController: NavController,
    permissionUiState: PermissionUiState = rememberPermissionUiState()
) {
    val context       = LocalContext.current
    val suggestComment = permissionUiState.suggestComment

    // Permissão de notificação — necessária no Android 13+ para notificações funcionarem
    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS) { granted ->
            if (granted) {
                permissionUiState.updateSuggestComment(true)
            }
            // Se recusou: não alterar a preferência (toggle continua ON mas inativo)
        }
    } else null

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        notifPermission?.status?.isGranted == true
    } else {
        true
    }
    val isToggleInactive = suggestComment && !hasNotificationPermission

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val perm = notifPermission ?: return
        requestNotificationPermissionOrOpenSettings(context, perm)
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 52.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Definições",
                    color      = MaterialTheme.colorScheme.onBackground,
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier   = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ── Secção Overlay ────────────────────────────────────────────
                Text(
                    "Overlay",
                    color      = AccentBlue,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )

                SettingsRow(
                    icon        = Icons.Default.Layers,
                    title       = "Estilo do overlay",
                    description = "Escolhe como o overlay é apresentado durante as chamadas",
                    onClick     = {
                        navController.navigate(Routes.OVERLAY_STYLE)
                    }
                )

                // Toggle — notificações de comentário após chamada rejeitada
                // Quando ON mas sem permissão: card clicável para pedir permissão; switch em ON mas inativo
                Card(
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier  = Modifier.fillMaxWidth(),
                    onClick   = { if (isToggleInactive) requestNotificationPermission() }
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentBlue.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications, null,
                                tint     = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Notificações de comentário",
                                color      = MaterialTheme.colorScheme.onBackground,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isToggleInactive)
                                    "As notificações estão desativadas no sistema. Toque para ativar."
                                else
                                    "Notificação após chamada curta, que pode ser sinal de spam",
                                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize   = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked         = suggestComment,
                            onCheckedChange = {
                                if (isToggleInactive) {
                                    requestNotificationPermission()
                                    return@Switch
                                }
                                if (it) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val perm = notifPermission ?: return@Switch
                                        when {
                                            perm.status.isGranted -> {
                                                permissionUiState.updateSuggestComment(true)
                                            }
                                            perm.status.shouldShowRationale ->
                                                perm.launchPermissionRequest()
                                            else -> requestNotificationPermission()
                                        }
                                    } else {
                                        permissionUiState.updateSuggestComment(true)
                                    }
                                } else {
                                    permissionUiState.updateSuggestComment(false)
                                }
                            },
                            enabled = !isToggleInactive,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = Color.White,
                                checkedTrackColor   = AccentBlue,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // ── Secção Aplicação ──────────────────────────────────────────
                Text(
                    "Aplicação",
                    color      = AccentBlue,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                )

                SettingsRow(
                    icon        = Icons.Default.Info,
                    title       = "Sobre a aplicação",
                    description = "Versão, privacidade e informações do CallRadar",
                    onClick     = {
                        navController.navigate(Routes.ABOUT)
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Linha de definição reutilizável ─────────────────────────────────────────
@Composable
fun SettingsRow(
    icon:        ImageVector,
    title:       String,
    description: String,
    onClick:     () -> Unit
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentBlue.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color      = MaterialTheme.colorScheme.onBackground,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize   = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Icon(
                Icons.Default.ChevronRight, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── About Screen ──────────────────────────────────────────────────────────────
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current

    fun openSite() {
        context.startActivity(Intent(Intent.ACTION_VIEW, "https://ligaram.me".toUri()))
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(AccentBlue.copy(alpha = 0.2f), Color.Transparent))
                    )
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick  = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        LigaramLogo(size = 56.dp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("CallRadar",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text("por ligaram.me",
                        color = AccentBlue,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    // PRÉ-PRODUÇÃO - [Melhoria] - versão lida dinamicamente do manifesto
                    val versionName = remember {
                        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0" }
                        catch (_: Exception) { "1.0.0" }
                    }
                    Text("v$versionName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    // Link para o site
                    OutlinedButton(
                        onClick = ::openSite,
                        shape   = RoundedCornerShape(10.dp),
                        border  = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                            tint     = AccentBlue,
                            modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ligaram.me", color = AccentBlue,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {

                AboutSection(
                    title = "O que é o CallRadar?",
                    content = "O CallRadar é uma aplicação de proteção contra chamadas indesejadas desenvolvida pelo ligaram.me. Quando recebe uma chamada, a app consulta automaticamente a base de dados e apresenta informação sobre o número - incluindo nível de risco, categoria e avaliação da comunidade."
                )

                Spacer(modifier = Modifier.height(16.dp))

                AboutSection(
                    title = "Como protege os utilizadores?",
                    content = "Através de uma sobreposição visual apresentada diretamente sobre o ecrã de chamada, o utilizador consegue ver antes de atender se o número está associado a fraude, spam, telemarketing ou outras categorias de risco."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Comunidade ligaram.me ─────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = AccentBlue.copy(alpha = 0.07f)),
                    border   = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.25f)),
                    onClick  = ::openSite
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, null,
                                tint     = AccentBlue,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Comunidade ligaram.me",
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "O CallRadar é também a aplicação oficial da comunidade de visitantes do ligaram.me. Pesquise números, consulte comentários de outros utilizadores, reporte chamadas suspeitas e ajude a proteger mais pessoas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp, lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Visitar ligaram.me",
                                color      = AccentBlue,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                                tint     = AccentBlue,
                                modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Features
                Text("Funcionalidades", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                listOf(
                    Icons.Default.Bolt       to "Identificação em tempo real",
                    Icons.Default.Shield     to "Base de dados comunitária",
                    Icons.Default.Forum      to "Comunidade de reporte",
                    Icons.Default.Visibility to "Overlay não intrusivo",
                    Icons.Default.Notifications to "Notificação após chamada curta",
                    Icons.Default.BatteryFull to "Baixo consumo de bateria",
                    Icons.Default.Lock       to "Sem armazenamento de dados pessoais",
                    Icons.Default.Update     to "Base de dados sempre atualizada"
                ).forEach { (icon, text) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Permissões ────────────────────────────────────────────────
                Text("Permissões utilizadas", color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    Triple(Icons.Default.Phone, "READ_PHONE_STATE",
                        "Permite detetar quando uma chamada é recebida, para que a identificação ocorra em tempo real sem necessidade de interação do utilizador."),
                    Triple(Icons.Default.History, "READ_CALL_LOG",
                        "Permite ler o número de telefone da chamada recebida. Este número é enviado à API do ligaram.me apenas para consulta de risco - nunca é armazenado localmente."),
                    Triple(Icons.Default.Contacts, "READ_CONTACTS",
                        "Utilizado para mostrar o nome do contacto no overlay, caso o número já exista na agenda. Nenhum dado de contacto é transmitido ou armazenado."),
                    Triple(Icons.Default.Notifications, "POST_NOTIFICATIONS",
                        "Permite apresentar notificações com sugestão de comentário após chamadas curtas de números sem dados na base de dados. Esta permissão é opcional e pode ser desativada nas Definições da app."),
                    Triple(Icons.Default.Layers, "SYSTEM_ALERT_WINDOW",
                        "Necessário para apresentar o overlay de identificação sobre a interface de chamada do sistema. Sem esta permissão, a informação de risco não pode ser exibida durante a chamada.")
                ).forEach { (icon, perm, desc) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(perm, color = AccentBlue, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Bateria ───────────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.07f)),
                    border   = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryFull, null,
                                tint     = AccentOrange,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Funcionamento em segundo plano",
                                color      = AccentOrange,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Para garantir que o CallRadar funciona corretamente em todas as chamadas, recomendamos duas configurações no Android:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp, lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Item 1
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AccentOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = AccentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Desativar otimização de bateria",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Aceda a Definições → Bateria → Otimização de bateria e exclua o CallRadar. Alguns fabricantes (Xiaomi, Samsung, Huawei) podem encerrar o serviço de deteção de chamadas se esta opção estiver ativa.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // Item 2
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AccentOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = AccentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Excluir da lista de apps não utilizadas",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Alguns sistemas Android desativam automaticamente apps que não são abertas com frequência. Aceda a Definições → Apps → CallRadar e desative a opção \"Pausar app se não usada\" ou equivalente.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PrivacyTip, null, tint = AccentGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privacidade", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "A aplicação envia apenas o número da chamada recebida à API do ligaram.me para consulta. Nenhum dado pessoal, histórico de chamadas ou informação de contacto é armazenado ou transmitido.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer com link
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /*
                    // Remoção do link no rodapé

                    TextButton(onClick = ::openSite) {
                        Text("ligaram.me",
                            color      = AccentBlue,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                     */
                    Text(
                        "© 2026 · ligaram.me · Todos os direitos reservados",
                        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize  = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AboutSection(title: String, content: String) {
    Column {
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 22.sp)
    }
}
