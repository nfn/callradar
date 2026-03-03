package me.ligaram.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import me.ligaram.app.R
import me.ligaram.app.data.ApiClient
import me.ligaram.app.data.ApiResult
import me.ligaram.app.ui.screens.OverlayActivity
import kotlinx.coroutines.*

class CallMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null
    private var isForeground = false

    companion object {
        const val ACTION_START = "me.ligaram.app.START"
        const val ACTION_INCOMING_CALL = "me.ligaram.app.INCOMING_CALL"
        const val ACTION_CALL_ENDED = "me.ligaram.app.CALL_ENDED"
        const val EXTRA_NUMBER = "extra_number"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "ligaram_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                // Only called from MainActivity (app visible) — safe to promote to foreground
                promoteToForegroundSafe()
            }
            ACTION_INCOMING_CALL -> {
                val number = intent.getStringExtra(EXTRA_NUMBER) ?: return START_STICKY
                val contactName = intent.getStringExtra(EXTRA_CONTACT_NAME)
                handleIncomingCall(number, contactName)
            }
            ACTION_CALL_ENDED -> {
                dismissOverlay()
            }
            // Boot/background starts: don't call startForeground, just stay alive
        }
        return START_STICKY
    }

    /**
     * Only call this when the app is in the foreground (from MainActivity).
     * startForeground() from background throws ForegroundServiceStartNotAllowedException on API 31+.
     */
    private fun promoteToForegroundSafe() {
        if (!isForeground) {
            try {
                startForeground(NOTIFICATION_ID, buildNotification())
                isForeground = true
                Log.d("CallMonitorService", "Promoted to foreground service")
            } catch (e: Exception) {
                Log.w("CallMonitorService", "Could not promote to foreground: ${e.message}")
            }
        }
    }

    private fun handleIncomingCall(number: String, contactName: String?) {
        currentJob?.cancel()
        currentJob = serviceScope.launch {
            Log.d("CallMonitorService", "Fetching info for: $number (contact: $contactName)")
            when (val result = ApiClient.fetchCallInfo(number)) {
                is ApiResult.Success -> {
                    Log.d("CallMonitorService", "Result: ${result.callInfo}")
                    showOverlay(
                        number = result.callInfo.number,
                        rating = result.callInfo.rating,
                        risk = result.callInfo.risk,
                        category = result.callInfo.category,
                        subcategory = result.callInfo.subcategory,
                        contactName = contactName
                    )
                }
                is ApiResult.NoResult -> {
                    Log.d("CallMonitorService", "No result for $number — overlay suppressed")
                }
                is ApiResult.Error -> {
                    Log.e("CallMonitorService", "API error: ${result.message}")
                }
            }
        }
    }

    private fun showOverlay(
        number: String,
        rating: String,
        risk: String,
        category: String,
        subcategory: String,
        contactName: String?
    ) {
        val overlayIntent = Intent(this, OverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra("number", number)
            putExtra("rating", rating)
            putExtra("risk", risk)
            putExtra("category", category)
            putExtra("subcategory", subcategory)
            if (contactName != null) putExtra("contactName", contactName)
        }
        startActivity(overlayIntent)
    }

    private fun dismissOverlay() {
        currentJob?.cancel()
        val dismissIntent = Intent(OverlayActivity.ACTION_DISMISS).apply {
            `package` = packageName
        }
        sendBroadcast(dismissIntent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
