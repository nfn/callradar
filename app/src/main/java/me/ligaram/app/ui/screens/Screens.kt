package me.ligaram.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import me.ligaram.app.service.CallMonitorService
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentGreen
import me.ligaram.app.ui.theme.AccentOrange
import me.ligaram.app.ui.theme.TextSecondary
import me.ligaram.app.ui.theme.ligaramColors

// ─── Navigation Routes ────────────────────────────────────────────────────────
object Routes {
    const val PERM_PHONE       = "perm_phone"
    const val PERM_OVERLAY     = "perm_overlay"
    const val HOME             = "home"
    const val ABOUT            = "about"
    const val COMMUNITY_HOME   = "community_home"
    const val COMMUNITY_NUMBER = "community_number"
    const val ADD_COMMENT      = "add_comment"
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

// ─── Main Nav Host ────────────────────────────────────────────────────────────
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // A app arranca sempre no HOME - as permissões são opcionais e activadas
    // a partir do status card da tab Proteção.
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.PERM_PHONE,
            enterTransition = { fadeIn() + slideInHorizontally() },
            exitTransition  = { fadeOut() + slideOutHorizontally { -it } }
        ) { PermPhoneScreen(navController) }

        composable(Routes.PERM_OVERLAY,
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition  = { fadeOut() + slideOutHorizontally { -it } }
        ) { PermOverlayScreen(navController) }

        // HOME e COMMUNITY_HOME são o mesmo shell - apenas diferem no tab inicial
        composable(Routes.HOME,
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition  = { fadeOut() + slideOutHorizontally { -it } }
        ) { MainShell(navController, startTab = 0) }

        composable(Routes.COMMUNITY_HOME,
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition  = { fadeOut() + slideOutHorizontally { -it } }
        ) { MainShell(navController, startTab = 1) }

        composable(Routes.ABOUT,
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition  = { fadeOut() + slideOutHorizontally { -it } }
        ) { AboutScreen(navController) }

        // Página de detalhe de número
        composable("${Routes.COMMUNITY_NUMBER}/{number}",
            enterTransition    = { fadeIn() + slideInHorizontally { it } },
            exitTransition     = { fadeOut() + slideOutHorizontally { -it } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
            popExitTransition  = { fadeOut() + slideOutHorizontally { it } }
        ) { back ->
            val number = back.arguments?.getString("number") ?: ""
            CommunityNumberScreen(navController, number)
        }

        // Screen para adicionar comentário — slide + fade bem suave
        // Screen para adicionar comentário — mesmos tempos e suavidade que CommunityNumberScreen
        composable("${Routes.ADD_COMMENT}/{number}",
            enterTransition    = { fadeIn() + slideInHorizontally { it } },
            exitTransition     = { fadeOut() + slideOutHorizontally { -it } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
            popExitTransition  = { fadeOut() + slideOutHorizontally { it } }
        ) { back ->
            val number = back.arguments?.getString("number") ?: ""
            AddCommentScreen(navController, number)
        }
    }
}

// ─── Main shell com Bottom Navigation ────────────────────────────────────────
@Composable
fun MainShell(rootNav: NavController, startTab: Int = 0) {
    var selectedTab by remember { mutableStateOf(startTab) }

    // Botão/gesto back quando estamos no tab Comunidade → volta ao tab Proteção
    // Em qualquer tab → não sai da app (comportamento padrão do sistema)
    androidx.activity.compose.BackHandler(enabled = selectedTab == 1) {
        selectedTab = 0
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
                    onClick  = { selectedTab = 0 },
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
                    onClick  = { selectedTab = 1 },
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
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(rootNav)
                1 -> CommunityHomeScreen(rootNav)
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

// ─── Step indicator ────────────────────────────────────────────────────────────
@Composable
fun StepDots(total: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { idx ->
            val active = idx == current
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 24.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (active) AccentBlue else MaterialTheme.colorScheme.outline)
                    
            )
        }
    }
}

// ─── Permission Screen Template ───────────────────────────────────────────────
@Composable
fun PermissionScreenLayout(
    step: Int,
    total: Int,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    detailPoints: List<String>,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    granted: Boolean,
    onNext: () -> Unit
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepDots(total, step)
            Spacer(modifier = Modifier.height(40.dp))

            // Ícone de estado
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (granted) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (granted) AccentGreen else iconTint,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(28.dp))

            // Detail points
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                detailPoints.forEach { point ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Circle, null, tint = AccentBlue,
                            modifier = Modifier.size(8.dp).padding(top = 6.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(point, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (granted) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    )
                ) {
                    Text(buttonLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ─── Permission 1: Phone State ────────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermPhoneScreen(navController: NavController) {
    val phonePermission = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )
    )

    PermissionScreenLayout(
        step = 0, total = 2,
        icon = Icons.Default.Phone,
        iconTint = AccentBlue,
        iconBg = AccentBlue.copy(alpha = 0.15f),
        title = "Acesso às Chamadas",
        description = "Para identificar quem está a ligar, a aplicação precisa de acesso ao estado do telefone.",
        detailPoints = listOf(
            "Detetamos chamadas recebidas em tempo real",
            "O número é consultado na base de dados do ligaram.me",
            "O nome do contacto é mostrado no overlay (se existir)",
            "Nunca armazenamos chamadas ou contactos pessoais",
            "O acesso é utilizado exclusivamente para identificação"
        ),
        buttonLabel = "Conceder Permissão",
        onButtonClick = { phonePermission.launchMultiplePermissionRequest() },
        granted = phonePermission.allPermissionsGranted,
        onNext = { navController.navigate(Routes.PERM_OVERLAY) }
    )
}

// ─── Permission 2: Overlay ─────────────────────────────────────────────────────
@Composable
fun PermOverlayScreen(navController: NavController) {
    val context = LocalContext.current
    val canDraw = remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Poll for overlay permission (user returns from settings)
    LaunchedEffect(Unit) {
        while (!canDraw.value) {
            kotlinx.coroutines.delay(500)
            canDraw.value = Settings.canDrawOverlays(context)
        }
    }

    PermissionScreenLayout(
        step = 1, total = 2,
        icon = Icons.Default.Layers,
        iconTint = AccentOrange,
        iconBg = AccentOrange.copy(alpha = 0.15f),
        title = "Mostrar sobre outras apps",
        description = "Para apresentar informação sobre chamadas suspeitas, a app precisa de permissão para sobrepor conteúdo.",
        detailPoints = listOf(
            "O overlay aparece automaticamente ao receber uma chamada",
            "Apresenta risco, categoria e avaliação do número",
            "Desaparece quando a chamada termina",
            "Não interfere com nenhuma outra aplicação"
        ),
        buttonLabel = "Ativar Sobreposição",
        onButtonClick = {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri()))
        },
        granted = canDraw.value,
        onNext = {
            // App is in foreground here - startForegroundService is safe
            context.startForegroundService(Intent(context, CallMonitorService::class.java).apply {
                action = CallMonitorService.ACTION_START
            })
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.PERM_PHONE) { inclusive = true }
            }
        }
    )
}

// ─── Home Screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var phoneGranted   by remember { mutableStateOf(hasPhonePermissions(context)) }
    var overlayGranted by remember { mutableStateOf(hasOverlayPermission(context)) }
    val allGood = phoneGranted && overlayGranted

    // Diálogo de activação de permissões (abre ao clicar no status card)
    var showPermDialog by remember { mutableStateOf(false) }

    // Poll permissões enquanto o ecrã estiver visível
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            phoneGranted   = hasPhonePermissions(context)
            overlayGranted = hasOverlayPermission(context)
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
        }
    }

    if (showPermDialog) {
        PermissionDialog(
            phoneGranted   = phoneGranted,
            overlayGranted = overlayGranted,
            onDismiss      = { showPermDialog = false }
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
                onClick = { if (!allGood) showPermDialog = true }
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
                            if (allGood) "Chamadas recebidas serão identificadas automaticamente"
                            else "Toque aqui para ativar a identificação de chamadas",
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
                Triple(Icons.Default.PhoneInTalk, "Chamada recebida",       "A app deteta automaticamente quem está a ligar"),
                Triple(Icons.Default.Search,       "Consulta a base de dados","O número é verificado em tempo real no ligaram.me"),
                Triple(Icons.Default.Layers,       "Overlay apresentado",    "Se houver resultado, mostramos risco e categoria sobre o ecrã"),
                Triple(Icons.Default.Block,        "Proteja-se",             "Decida com informação se atende ou rejeita a chamada")
            ).forEachIndexed { idx, (icon, title, desc) ->
                HowItWorksStep(_step = idx + 1, icon = icon, title = title, description = desc)
                if (idx < 3) Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick  = { navController.navigate(Routes.ABOUT) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sobre a aplicação", color = TextSecondary)
            }
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

    // Ao obter todas as permissões, fechar o diálogo automaticamente
    LaunchedEffect(phonePermissions.allPermissionsGranted, overlayGranted) {
        if (phonePermissions.allPermissionsGranted && overlayGranted) {
            kotlinx.coroutines.delay(800)
            onDismiss()
        }
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
                Text("Para identificar chamadas em tempo real, a app precisa de duas permissões:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)

                // ── Permissão 1: Chamadas ─────────────────────────────────────
                PermissionRow(
                    icon    = Icons.Default.Phone,
                    title   = "Acesso às chamadas",
                    desc    = "Detecta chamadas recebidas em tempo real",
                    granted = phoneGranted || phonePermissions.allPermissionsGranted,
                    buttonLabel = "Conceder",
                    onAction    = { phonePermissions.launchMultiplePermissionRequest() }
                )

                // ── Permissão 2: Overlay ──────────────────────────────────────
                PermissionRow(
                    icon    = Icons.Default.Layers,
                    title   = "Mostrar sobre outras apps",
                    desc    = "Exibe informação durante a chamada",
                    granted = overlayGranted,
                    buttonLabel = "Ativar",
                    onAction    = {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri()))
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Agora não", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    buttonLabel: String,
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
                if (granted) Icons.Default.CheckCircle else icon,
                null,
                tint     = if (granted) AccentGreen else AccentBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc,  color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp)
        }
        if (!granted) {
            Button(
                onClick = onAction,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(buttonLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text("✓ Ativo", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun HowItWorksStep(_step: Int, icon: ImageVector, title: String, description: String) {
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
                        onClick  = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                            }
                        },
                        modifier = Modifier.align(Alignment.Start)
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
                    Text("v1.0.0",
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
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 18.sp)
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
                    TextButton(onClick = ::openSite) {
                        Text("ligaram.me",
                            color      = AccentBlue,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "© 2026 CallRadar · ligaram.me · Todos os direitos reservados",
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
