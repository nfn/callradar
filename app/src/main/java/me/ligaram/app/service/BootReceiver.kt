package me.ligaram.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed - starting CallMonitorService")
            val serviceIntent = Intent(context, CallMonitorService::class.java).apply {
                action = CallMonitorService.ACTION_START
            }
            // Use startService, NOT startForegroundService - we're in background after boot
            try {
                context.startService(serviceIntent)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed: ${e.message}")
            }
        }
    }
}
