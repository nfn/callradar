package me.ligaram.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log

class PhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        // Android 10+ may not deliver this in background without READ_CALL_LOG
        val extraNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.d("PhoneStateReceiver", "state=$state extraNumber=$extraNumber lastState=$lastState")

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                if (lastState == TelephonyManager.EXTRA_STATE_RINGING) return
                lastState = state

                val number = when {
                    // Best case: number came directly in the broadcast
                    !extraNumber.isNullOrBlank() -> extraNumber
                    // Fallback: query CallLog for the most recent incoming call
                    else -> getLastIncomingNumberFromCallLog(context)
                }

                Log.d("PhoneStateReceiver", "Ringing — resolved number: $number")

                if (!number.isNullOrBlank()) {
                    sendToService(context, CallMonitorService.ACTION_INCOMING_CALL, number)
                } else {
                    Log.w("PhoneStateReceiver", "Could not resolve number — READ_CALL_LOG may be missing")
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (lastState == TelephonyManager.EXTRA_STATE_IDLE) return
                lastState = state
                sendToService(context, CallMonitorService.ACTION_CALL_ENDED)
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (lastState == TelephonyManager.EXTRA_STATE_OFFHOOK) return
                lastState = state
                sendToService(context, CallMonitorService.ACTION_CALL_ENDED)
            }
        }
    }

    /**
     * Query CallLog for the most recent INCOMING or MISSED call.
     * This works on Android 10+ when EXTRA_INCOMING_NUMBER is not delivered,
     * provided READ_CALL_LOG permission is granted.
     */
    private fun getLastIncomingNumberFromCallLog(context: Context): String? {
        return try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
                "${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ?",
                arrayOf(
                    CallLog.Calls.INCOMING_TYPE.toString(),
                    CallLog.Calls.MISSED_TYPE.toString()
                ),
                "${CallLog.Calls.DATE} DESC"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    Log.d("PhoneStateReceiver", "CallLog fallback number: $number")
                    number
                } else null
            }
        } catch (e: SecurityException) {
            Log.e("PhoneStateReceiver", "READ_CALL_LOG permission denied: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("PhoneStateReceiver", "CallLog query failed: ${e.message}")
            null
        }
    }

    private fun sendToService(context: Context, action: String, number: String? = null) {
        val serviceIntent = Intent(context, CallMonitorService::class.java).apply {
            this.action = action
            if (number != null) putExtra(CallMonitorService.EXTRA_NUMBER, number)
        }
        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            Log.e("PhoneStateReceiver", "Failed to start service: ${e.message}")
        }
    }
}
