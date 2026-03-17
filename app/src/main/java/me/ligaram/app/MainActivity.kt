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

    // Número recebido pela notificação — observado pelo Compose para navegar para AddComment.
    private val pendingAddComment = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        CommunityApi.init(this)
        // Verificar se foi aberta pela notificação de sugestão de comentário
        pendingAddComment.value = intent?.getStringExtra("open_add_comment")
        enableEdgeToEdge()
        setContent {
            LigaramTheme {
                AppNavigation(
                    initialAddComment = pendingAddComment.value,
                    onInitialAddCommentConsumed = { pendingAddComment.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // App já estava aberta — a notificação envia onNewIntent
        intent.getStringExtra("open_add_comment")?.let { number ->
            // Força transição de estado mesmo se o número for igual ao anterior.
            pendingAddComment.value = null
            pendingAddComment.value = number
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
