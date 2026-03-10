package me.ligaram.app.service

// ⚠️ DEMO_MODE — import para modo de testes — REMOVER EM PRODUÇÃO (ou mudar DEMO_MODE = false)
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.ligaram.app.R
import me.ligaram.app.data.ApiClient
import me.ligaram.app.data.ApiResult
import me.ligaram.app.data.TestConfig
import me.ligaram.app.ui.screens.OverlayActivity

class CallMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null
    private var isForeground = false
    /** Só a resposta da chamada mais recente pode mostrar overlay (evita race com jobs que terminam tarde). */
    private var latestRequestGeneration = 0

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
                // Only called from MainActivity (app visible) - safe to promote to foreground
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        buildNotification(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                    )
                } else {
                    startForeground(NOTIFICATION_ID, buildNotification())
                }
                isForeground = true
                Log.d("CallMonitorService", "Promoted to foreground service")
            } catch (e: Exception) {
                Log.w("CallMonitorService", "Could not promote to foreground: ${e.message}")
            }
        }
    }

    private fun handleIncomingCall(number: String, contactName: String?) {
        currentJob?.cancel()
        latestRequestGeneration += 1
        val myGeneration = latestRequestGeneration
        currentJob = serviceScope.launch {
            Log.d("CallMonitorService", "Fetching info for: $number (contact: $contactName)")
            when (val result = ApiClient.fetchCallInfo(number)) {
                is ApiResult.Success -> {
                    if (myGeneration != latestRequestGeneration) {
                        Log.d("CallMonitorService", "Discarding stale result for $number (newer call in progress)")
                        return@launch
                    }
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
                    // ╔══════════════════════════════════════════════════════════════╗
                    // ║  ⚠️ DEMO_MODE — Em produção o overlay NÃO é mostrado        ║
                    // ║  quando o número não tem resultados na base de dados.        ║
                    // ║  Em DEMO_MODE lançamos sempre o overlay com dados vazios;    ║
                    // ║  o OverlayActivity.applyDemoIfEnabled() substitui-os pelos   ║
                    // ║  dados fictícios rotativos.                                  ║
                    // ║  REMOVER o bloco if() abaixo ao desativar DEMO_MODE          ║
                    // ╚══════════════════════════════════════════════════════════════╝
                    if (TestConfig.DEMO_MODE && TestConfig.isDemoOverlayEnabled(this@CallMonitorService)) {
                        Log.d("CallMonitorService", "DEMO_MODE: forcing overlay on NoResult for $number")
                        showOverlay(number = number, rating = "", risk = "", category = "", subcategory = "", contactName = contactName)
                    } else {
                        Log.d("CallMonitorService", "No result for $number - overlay suppressed")
                    }
                    // ⚠️ DEMO_MODE — fim do bloco
                }
                is ApiResult.Error -> {
                    // ╔══════════════════════════════════════════════════════════════╗
                    // ║  ⚠️ DEMO_MODE — Em produção o overlay NÃO é mostrado        ║
                    // ║  quando a API devolve erro.                                  ║
                    // ║  Em DEMO_MODE lançamos sempre o overlay com dados vazios;    ║
                    // ║  o OverlayActivity.applyDemoIfEnabled() substitui-os pelos   ║
                    // ║  dados fictícios rotativos.                                  ║
                    // ║  REMOVER o bloco if() abaixo ao desativar DEMO_MODE          ║
                    // ╚══════════════════════════════════════════════════════════════╝
                    if (TestConfig.DEMO_MODE && TestConfig.isDemoOverlayEnabled(this@CallMonitorService)) {
                        Log.d("CallMonitorService", "DEMO_MODE: forcing overlay on API error for $number — ${result.message}")
                        showOverlay(number = number, rating = "", risk = "", category = "", subcategory = "", contactName = contactName)
                    } else {
                        Log.e("CallMonitorService", "API error: ${result.message}")
                    }
                    // ⚠️ DEMO_MODE — fim do bloco
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
