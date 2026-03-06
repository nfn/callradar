package me.ligaram.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.ligaram.app.data.ApiClient
import me.ligaram.app.data.CommunityApi
import me.ligaram.app.service.CallMonitorService
import me.ligaram.app.ui.screens.AppNavigation
import me.ligaram.app.ui.screens.allPermissionsGranted
import me.ligaram.app.ui.theme.LigaramTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Inicializar clientes com context para os headers de diagnóstico
        ApiClient.init(this)
        CommunityApi.init(this)
        enableEdgeToEdge()
        setContent {
            LigaramTheme {
                AppNavigation()
            }
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
