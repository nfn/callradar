package me.ligaram.app.ui.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import me.ligaram.app.data.OverlayPreferences

class PermissionUiState(
    private val context: android.content.Context
) {
    private val _phoneGranted = mutableStateOf(hasPhonePermissions(context))
    var phoneGranted: Boolean
        get() = _phoneGranted.value
        set(value) { _phoneGranted.value = value }

    private val _overlayGranted = mutableStateOf(hasOverlayPermission(context))
    var overlayGranted: Boolean
        get() = _overlayGranted.value
        set(value) { _overlayGranted.value = value }

    private val _suggestComment = mutableStateOf(OverlayPreferences.getSuggestComment(context))
    var suggestComment: Boolean
        get() = _suggestComment.value
        set(value) { _suggestComment.value = value }

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
fun rememberPermissionUiState(): PermissionUiState {
    val context = LocalContext.current
    return remember(context) { PermissionUiState(context) }
}
