package me.ligaram.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.ligaram.app.service.CallMonitorService
import me.ligaram.app.ui.components.AppBackground
import me.ligaram.app.ui.components.HowItWorksStep
import me.ligaram.app.ui.components.PermissionDialog
import me.ligaram.app.ui.permissions.PermissionUiState
import me.ligaram.app.ui.permissions.rememberPermissionUiState
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentGreen
import me.ligaram.app.ui.theme.AccentOrange

@Composable
fun HomeScreen(
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
            Text("CallRadar", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium)
            Text("por ligaram.me", color = AccentBlue, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Proteção contra chamadas indesejadas", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

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
                            color = if (allGood) AccentGreen else AccentOrange,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (allGood)
                                "Chamadas recebidas são identificadas automaticamente"
                            else
                                "Toque aqui para ativar a identificação de chamadas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
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

            Text("Como funciona", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))

            listOf(
                Triple(Icons.Default.PhoneInTalk,  "Chamada recebida",                "A app deteta automaticamente quem está a ligar"),
                Triple(Icons.Default.Search,        "Consulta a base de dados",        "O número é verificado em tempo real no ligaram.me"),
                Triple(Icons.Default.Layers,        "Aviso sobre chamadas apresentado","Se houver resultado, mostramos risco e categoria sobre o ecrã"),
                Triple(Icons.Default.Block,         "Proteja-se",                     "Decida com informação se atende ou rejeita a chamada"),
                Triple(Icons.Default.Notifications, "Notificação de comentário",      "Após chamadas curtas de números desconhecidos, convidamos a partilhar a experiência")
            ).forEachIndexed { idx, (icon, title, desc) ->
                HowItWorksStep(icon = icon, title = title, description = desc, accentColor = AccentBlue)
                if (idx < 4) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

