package me.ligaram.app.ui.components

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import me.ligaram.app.ui.theme.AccentGreen
import me.ligaram.app.ui.theme.AccentOrange
import me.ligaram.app.ui.permissions.requestNotificationPermissionOrOpenSettings

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
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
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
                Icon(Icons.Default.Shield, null,
                    tint     = if (phoneGranted && overlayGranted) AccentGreen else AccentOrange,
                    modifier = Modifier.size(24.dp))
                Text("Permissões para Proteção", color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Estas são as permissões necessárias para que a aplicação funcione corretamente.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)

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
                        title   = "Notificações de chamadas",
                        desc    = "Notifica chamadas curtas e suspeitas",
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
