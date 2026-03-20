package me.ligaram.app.ui.screens

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import me.ligaram.app.ui.components.AppBackground
import me.ligaram.app.ui.components.SettingsRow
import me.ligaram.app.ui.navigation.Routes
import me.ligaram.app.ui.permissions.PermissionUiState
import me.ligaram.app.ui.permissions.rememberPermissionUiState
import me.ligaram.app.ui.permissions.requestNotificationPermissionOrOpenSettings
import me.ligaram.app.ui.theme.AccentBlue

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
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
                    .padding(start = 20.dp, end = 20.dp, top = 27.dp, bottom = 19.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Definições",
                    color      = MaterialTheme.colorScheme.onBackground,
                    fontSize   = 20.sp,
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

                // ── Secção Sobreposição ───────────────────────────────────────
                Text(
                    "Sobreposição",
                    color      = AccentBlue,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 4.dp)
                )

                SettingsRow(
                    icon        = Icons.Default.Layers,
                    title       = "Estilo do aviso sobre chamadas",
                    description = "Escolhe como o aviso sobre chamadas é apresentado durante as chamadas",
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

