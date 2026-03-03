package me.ligaram.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log

class PhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        /** Número enviado na sessão RINGING atual; usado para detectar 2.º broadcast com número diferente (ex.: OEM envia RINGING sem extraNumber, depois RINGING com extraNumber). */
        private var lastRingingNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val extraNumber = intent.getStringExtra("incoming_number")

        Log.d("PhoneStateReceiver", "state=$state extraNumber=$extraNumber lastState=$lastState")

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                val number = when {
                    !extraNumber.isNullOrBlank() -> extraNumber
                    else -> getLastIncomingNumberFromCallLog(context)
                }

                Log.d("PhoneStateReceiver", "Ringing - number: $number")

                val alreadyRinging = lastState == TelephonyManager.EXTRA_STATE_RINGING
                if (alreadyRinging) {
                    // Segundo (ou posterior) broadcast RINGING: se o número mudou, é nova chamada (ex.: primeiro veio sem extraNumber e usámos CallLog antigo).
                    if (!number.isNullOrBlank() && number != lastRingingNumber) {
                        lastRingingNumber = number
                        val contactName = lookupContactName(context, number)
                        Log.d("PhoneStateReceiver", "Contact name: $contactName (updated ringing number)")
                        sendToService(context, CallMonitorService.ACTION_INCOMING_CALL, number, contactName)
                    }
                    return
                }
                lastState = state
                lastRingingNumber = number

                if (!number.isNullOrBlank()) {
                    val contactName = lookupContactName(context, number)
                    Log.d("PhoneStateReceiver", "Contact name: $contactName")
                    sendToService(context, CallMonitorService.ACTION_INCOMING_CALL, number, contactName)
                } else {
                    Log.w("PhoneStateReceiver", "Could not resolve number")
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (lastState == TelephonyManager.EXTRA_STATE_IDLE) return
                lastState = state
                lastRingingNumber = null
                sendToService(context, CallMonitorService.ACTION_CALL_ENDED)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (lastState == TelephonyManager.EXTRA_STATE_OFFHOOK) return
                lastState = state
                lastRingingNumber = null
                sendToService(context, CallMonitorService.ACTION_CALL_ENDED)
            }
        }
    }

    private fun lookupContactName(context: Context, number: String): String? {
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )
            cursor?.use {
                if (it.moveToFirst())
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                else null
            }
        } catch (e: Exception) {
            Log.w("PhoneStateReceiver", "Contact lookup failed: ${e.message}")
            null
        }
    }

    private fun getLastIncomingNumberFromCallLog(context: Context): String? {
        // Brief pause: some OEMs write to CallLog slightly after the RINGING broadcast
        Thread.sleep(300)
        return try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE),
                null, null,
                CallLog.Calls.DATE + " DESC"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    Log.d("PhoneStateReceiver", "CallLog fallback: " + number)
                    number
                } else null
            }
        } catch (e: Exception) {
            Log.e("PhoneStateReceiver", "CallLog query failed: " + e.message)
            null
        }
    }

    private fun sendToService(
        context: Context,
        action: String,
        number: String? = null,
        contactName: String? = null
    ) {
        val serviceIntent = Intent(context, CallMonitorService::class.java).apply {
            this.action = action
            if (number != null) putExtra(CallMonitorService.EXTRA_NUMBER, number)
            if (contactName != null) putExtra(CallMonitorService.EXTRA_CONTACT_NAME, contactName)
        }
        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            Log.e("PhoneStateReceiver", "Failed to start service: ${e.message}")
        }
    }
}
