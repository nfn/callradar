package me.ligaram.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.ligaram.app.service.CallMonitorService
import me.ligaram.app.ui.screens.AppNavigation
import me.ligaram.app.ui.screens.HomeScreen
import me.ligaram.app.ui.screens.Routes
import me.ligaram.app.ui.theme.LigaramTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            LigaramTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val phoneGranted = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val overlayGranted = Settings.canDrawOverlays(this)
        if (phoneGranted && overlayGranted) {
            // Called from foreground — service can safely promote itself
            val serviceIntent = Intent(this, CallMonitorService::class.java).apply {
                action = CallMonitorService.ACTION_START
            }
            startForegroundService(serviceIntent)
        }
    }
}
