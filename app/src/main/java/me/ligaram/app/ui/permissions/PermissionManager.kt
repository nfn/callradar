package me.ligaram.app.ui.permissions

import android.content.Intent
import android.provider.Settings
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import me.ligaram.app.data.OverlayPreferences

@OptIn(ExperimentalPermissionsApi::class)
fun requestNotificationPermissionOrOpenSettings(
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
