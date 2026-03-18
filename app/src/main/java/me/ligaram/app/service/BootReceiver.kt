package me.ligaram.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// This receiver is NOT registered in AndroidManifest.xml.
//
// Android 15+ restricts starting foreground services of type 'phoneCall' (and other
// restricted types) from BOOT_COMPLETED broadcast receivers. The previous implementation
// sent ACTION_START which triggered promoteToForegroundSafe() → startForeground(phoneCall),
// violating this restriction.
//
// The service is now started on demand:
//   - By PhoneStateReceiver when a phone call arrives (ACTION_INCOMING_CALL via startService)
//   - By MainActivity when the user opens the app (ACTION_START → promoted to foreground)
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // No-op: receiver is not registered in the manifest.
    }
}
