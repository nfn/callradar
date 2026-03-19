package me.ligaram.app.ui.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings

fun hasPhonePermissions(context: android.content.Context): Boolean {
    val perms = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS
    )
    return perms.all {
        context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }
}

fun hasOverlayPermission(context: android.content.Context): Boolean =
    Settings.canDrawOverlays(context)

fun allPermissionsGranted(context: android.content.Context): Boolean =
    hasPhonePermissions(context) && hasOverlayPermission(context)
