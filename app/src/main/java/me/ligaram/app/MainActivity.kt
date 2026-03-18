package me.ligaram.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.ligaram.app.data.ApiClient
import me.ligaram.app.data.CommunityApi
import me.ligaram.app.service.CallMonitorService
import me.ligaram.app.ui.screens.AppNavigation
import me.ligaram.app.ui.screens.allPermissionsGranted
import me.ligaram.app.ui.theme.LigaramTheme

class MainActivity : ComponentActivity() {

    // Número recebido pela notificação — observado pelo Compose para navegar para CommunityNumber.
    private val pendingCommunityNumber = mutableStateOf<String?>(null)

    private fun extractCommunityNumber(intent: Intent?): String? {
        if (intent == null) return null
        return intent.getStringExtra(CallMonitorService.EXTRA_OPEN_COMMUNITY_NUMBER)
            ?: intent.getStringExtra("open_add_comment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        CommunityApi.init(this)
        // Verificar se foi aberta pela notificação para abrir detalhe de número
        pendingCommunityNumber.value = extractCommunityNumber(intent)
        enableEdgeToEdge()
        setContent {
            LigaramTheme {
                AppNavigation(
                    initialCommunityNumber = pendingCommunityNumber.value,
                    onInitialCommunityNumberConsumed = { pendingCommunityNumber.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // App já estava aberta — a notificação envia onNewIntent
        extractCommunityNumber(intent)?.let { number ->
            // Força transição de estado mesmo se o número for igual ao anterior.
            pendingCommunityNumber.value = null
            pendingCommunityNumber.value = number
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted(this)) {
            try {
                val svc = Intent(this, CallMonitorService::class.java).apply {
                    action = CallMonitorService.ACTION_START
                }
                startForegroundService(svc)
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "startForegroundService failed: ${e.message}")
            }
        }
    }
}
