package me.ligaram.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.google.accompanist.permissions.*
import me.ligaram.app.service.CallMonitorService
import me.ligaram.app.ui.theme.*

// ─── Navigation Routes ────────────────────────────────────────────────────────
object Routes {
    const val PERM_PHONE = "perm_phone"
    const val PERM_OVERLAY = "perm_overlay"
    const val HOME = "home"
    const val ABOUT = "about"
}

// ─── Main Nav Host ────────────────────────────────────────────────────────────
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.PERM_PHONE) {
        composable(Routes.PERM_PHONE,
            enterTransition = { fadeIn() + slideInHorizontally() },
            exitTransition = { fadeOut() + slideOutHorizontally { -it } }
        ) { PermPhoneScreen(navController) }

        composable(Routes.PERM_OVERLAY,
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it } }
        ) { PermOverlayScreen(navController) }

        composable(Routes.HOME,
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it } }
        ) { HomeScreen(navController) }

        composable(Routes.ABOUT,
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it } }
        ) { AboutScreen(navController) }
    }
}

// ─── Shared background ────────────────────────────────────────────────────────
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDeep, Color(0xFF0A1628))
                )
            )
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
                    .background(if (active) AccentBlue else NavyLight)
                    .animateContentSize()
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

            // Animated icon
            val scale by animateFloatAsState(
                targetValue = if (granted) 1.1f else 1f,
                animationSpec = spring(dampingRatio = 0.4f), label = "scale"
            )
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

            Text(title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, color = TextSecondary, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(28.dp))

            // Detail points
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                detailPoints.forEach { point ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Circle, null, tint = AccentBlue,
                            modifier = Modifier.size(8.dp).padding(top = 6.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(point, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
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
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
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
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}"))
            context.startActivity(intent)
        },
        granted = canDraw.value,
        onNext = {
            // App is in foreground here — startForegroundService is safe
            val serviceIntent = Intent(context, CallMonitorService::class.java).apply {
                action = CallMonitorService.ACTION_START
            }
            context.startForegroundService(serviceIntent)
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.PERM_PHONE) { inclusive = true }
            }
        }
    )
}

// ─── Home Screen ───────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val phoneGranted = remember {
        context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    val overlayGranted = remember { Settings.canDrawOverlays(context) }
    val allGood = phoneGranted && overlayGranted

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Brand
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhoneEnabled, null, tint = AccentBlue, modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("ligaram.me", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Proteção contra chamadas indesejadas", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(36.dp))

            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (allGood) AccentGreen.copy(alpha = 0.1f) else AccentOrange.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, if (allGood) AccentGreen.copy(alpha = 0.4f) else AccentOrange.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (allGood) Icons.Default.CheckCircle else Icons.Default.Warning,
                        null,
                        tint = if (allGood) AccentGreen else AccentOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            if (allGood) "Proteção ativa" else "Configuração incompleta",
                            color = if (allGood) AccentGreen else AccentOrange,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                        Text(
                            if (allGood) "Chamadas recebidas serão identificadas automaticamente"
                            else "Algumas permissões estão em falta",
                            color = TextSecondary, fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How it works section
            Text("Como funciona", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))

            listOf(
                Triple(Icons.Default.PhoneInTalk, "Chamada recebida", "A app deteta automaticamente quem está a ligar"),
                Triple(Icons.Default.Search, "Consulta a base de dados", "O número é verificado em tempo real no ligaram.me"),
                Triple(Icons.Default.Layers, "Overlay apresentado", "Se houver resultado, mostramos risco e categoria sobre o ecrã"),
                Triple(Icons.Default.Block, "Proteja-se", "Decida com informação se atende ou rejeita a chamada")
            ).forEachIndexed { idx, (icon, title, desc) ->
                HowItWorksStep(step = idx + 1, icon = icon, title = title, description = desc)
                if (idx < 3) Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // About button
            OutlinedButton(
                onClick = { navController.navigate(Routes.ABOUT) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NavyLight)
            ) {
                Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sobre a aplicação", color = TextSecondary)
            }
        }
    }
}

@Composable
fun HowItWorksStep(step: Int, icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
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
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

// ─── About Screen ──────────────────────────────────────────────────────────────
@Composable
fun AboutScreen(navController: NavController) {
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
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(AccentBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneEnabled, null, tint = AccentBlue, modifier = Modifier.size(38.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("ligaram.me", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text("v1.0.0", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {

                AboutSection(
                    title = "O que é?",
                    content = "O ligaram.me é uma aplicação de proteção contra chamadas indesejadas. Quando recebe uma chamada, a app consulta automaticamente a nossa base de dados e apresenta informação sobre o número — incluindo nível de risco, categoria e avaliação da comunidade."
                )

                Spacer(modifier = Modifier.height(16.dp))

                AboutSection(
                    title = "Como protege os utilizadores?",
                    content = "Através de uma sobreposição visual apresentada diretamente sobre o ecrã de chamada, o utilizador consegue ver antes de atender se o número está associado a fraude, spam, telemarketing ou outras categorias de risco."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Features
                Text("Funcionalidades", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                listOf(
                    Icons.Default.Bolt to "Identificação em tempo real",
                    Icons.Default.Shield to "Base de dados comunitária",
                    Icons.Default.Visibility to "Overlay não intrusivo",
                    Icons.Default.BatteryFull to "Baixo consumo de bateria",
                    Icons.Default.Lock to "Sem armazenamento de dados pessoais",
                    Icons.Default.Update to "Base de dados sempre atualizada"
                ).forEach { (icon, text) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text, color = TextSecondary, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Privacy
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
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
                            color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer
                Text(
                    "© 2024 ligaram.me · Todos os direitos reservados",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AboutSection(title: String, content: String) {
    Column {
        Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
    }
}
