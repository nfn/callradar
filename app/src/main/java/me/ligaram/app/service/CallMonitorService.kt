package me.ligaram.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import me.ligaram.app.MainActivity
import me.ligaram.app.R
import me.ligaram.app.data.ApiClient
import me.ligaram.app.data.ApiResult
import me.ligaram.app.data.OverlayPreferences
import me.ligaram.app.ui.screens.OverlayActivity

class CallMonitorService : Service() {

    private data class ActiveCallState(
        val number: String = "",
        val overlayShownAt: Long = 0L,
        val overlayHadResult: Boolean = false
    )

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null
    private var isForeground = false
    private val callStateLock = Any()
    /** Só a resposta da chamada mais recente pode mostrar overlay (evita race com jobs que terminam tarde). */
    private var latestRequestGeneration = 0
    /** Estado da chamada activa — lido/escrito sempre sob lock. */
    private var activeCallState = ActiveCallState()

    companion object {
        const val ACTION_START         = "me.ligaram.app.START"
        const val ACTION_INCOMING_CALL = "me.ligaram.app.INCOMING_CALL"
        const val ACTION_CALL_ENDED    = "me.ligaram.app.CALL_ENDED"
        const val EXTRA_NUMBER         = "extra_number"
        const val EXTRA_CONTACT_NAME   = "extra_contact_name"
        const val NOTIFICATION_ID      = 1001
        const val NOTIFICATION_SUGGEST_ID = 1002
        const val CHANNEL_ID           = "ligaram_channel"
        const val CHANNEL_SUGGEST_ID   = "ligaram_suggest"
        /** Chamadas com duração inferior a este valor (ms) sugerem comentário. */
        private const val SUGGEST_THRESHOLD_MS = 8_000L
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createSuggestChannel()
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
                val endedCallState = synchronized(callStateLock) {
                    latestRequestGeneration += 1
                    activeCallState.also { activeCallState = ActiveCallState() }
                }
                val duration = if (endedCallState.overlayShownAt > 0L) {
                    System.currentTimeMillis() - endedCallState.overlayShownAt
                } else {
                    Long.MAX_VALUE
                }
                dismissOverlay()
                // Notifica apenas se: chamada curta + número activo + API não devolveu resultado
                // Se houve resultado (overlay com dados) o utilizador já tinha informação suficiente
                if (
                    duration < SUGGEST_THRESHOLD_MS &&
                    endedCallState.number.isNotBlank() &&
                    !endedCallState.overlayHadResult
                ) {
                    maybeSuggestComment(endedCallState.number)
                }
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
        val myGeneration = synchronized(callStateLock) {
            latestRequestGeneration += 1
            activeCallState = ActiveCallState(
                number = number,
                // Número nos contactos → nunca mostrar notificação de sugestão
                overlayHadResult = contactName != null,
                overlayShownAt = System.currentTimeMillis()
            )
            latestRequestGeneration
        }
        currentJob = serviceScope.launch {
            Log.d("CallMonitorService", "Fetching info for: $number (contact: $contactName)")
            when (val result = ApiClient.fetchCallInfo(number)) {
                is ApiResult.Success -> {
                    val shouldShowResult = synchronized(callStateLock) {
                        if (myGeneration != latestRequestGeneration || activeCallState.number != number) {
                            false
                        } else {
                            activeCallState = activeCallState.copy(overlayHadResult = true)
                            true
                        }
                    }
                    if (!shouldShowResult) {
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
                    // overlayHadResult mantém-se false — notificação será lançada se chamada curta
                    Log.d("CallMonitorService", "No result for $number - overlay suppressed")
                }
                is ApiResult.Error -> {
                    // Erro de rede — não notificar (não é culpa do número)
                    synchronized(callStateLock) {
                        if (myGeneration == latestRequestGeneration && activeCallState.number == number) {
                            activeCallState = activeCallState.copy(overlayHadResult = true)
                        }
                    }
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
        // overlayShownAt e activeNumber já definidos em handleIncomingCall
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

    private fun createSuggestChannel() {
        val channel = NotificationChannel(
            CHANNEL_SUGGEST_ID,
            "Sugestões de comentário",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Sugestão para comentar chamadas rejeitadas rapidamente"
            setShowBadge(true)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun maybeSuggestComment(number: String) {
        if (!OverlayPreferences.getSuggestComment(this)) return

        // Abre a MainActivity com o número para navegar para AddCommentScreen
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_add_comment", number)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, number.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_SUGGEST_ID)
            .setContentTitle("Chamada de $number")
            .setContentText("Queres partilhar a tua experiência com a comunidade?")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_SUGGEST_ID, notification)
        Log.d("CallMonitorService", "Suggest comment notification sent for $number")
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
