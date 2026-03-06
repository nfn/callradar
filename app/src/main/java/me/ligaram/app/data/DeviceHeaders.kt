package me.ligaram.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager

/**
 * Constrói os headers de diagnóstico enviados em todos os pedidos à API.
 *
 * Campos:
 *   X-App-Version    — versionName do APK  (ex: "1.0")
 *   X-Android-SDK    — API level           (ex: "34")
 *   X-Android-Version— versão legível      (ex: "14")
 *   X-Device-Brand   — fabricante          (ex: "samsung")
 *   X-Device-Model   — modelo              (ex: "SM-S721B")
 *   X-Network-Type   — tipo de ligação     (ex: "wifi" | "lte" | "5g" | "4g" | "3g" | "2g" | "none" | "unknown")
 *   X-Device-Locale  — locale do sistema   (ex: "pt-PT")  ← útil para debug de i18n
 *   X-Screen-DPI     — densidade do ecrã   (ex: "420")    ← útil para debug de layout
 */
object DeviceHeaders {

    fun build(context: Context): Map<String, String> {
        val appVersion = runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: "unknown"
        }.getOrDefault("unknown")

        val userAgent = "LigaramApp/$appVersion (Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"

        return mapOf(
            "User-Agent"        to userAgent,
            "X-App-Version"     to appVersion,
            "X-Android-SDK"     to Build.VERSION.SDK_INT.toString(),
            "X-Android-Version" to Build.VERSION.RELEASE,
            "X-Device-Brand"    to Build.BRAND.lowercase(),
            "X-Device-Model"    to Build.MODEL,
            "X-Network-Type"    to getNetworkType(context),
            "X-Device-Locale"   to context.resources.configuration.locales[0].toLanguageTag(),
            "X-Screen-DPI"      to context.resources.displayMetrics.densityDpi.toString()
        )
    }

    private fun getNetworkType(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "unknown"
        val network = cm.activeNetwork ?: return "none"
        val caps    = cm.getNetworkCapabilities(network) ?: return "none"

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> getCellularType(context)
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)      -> "vpn"
            else -> "unknown"
        }
    }

    @Suppress("DEPRECATION")
    private fun getCellularType(context: Context): String {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: return "cellular"
            when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_NR                          -> "5g"
                TelephonyManager.NETWORK_TYPE_LTE                         -> "lte"
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_EVDO_B                      -> "3g"
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT                       -> "2g"
                else                                                       -> "cellular"
            }
        } catch (_: SecurityException) {
            // READ_PHONE_STATE pode não estar concedida — devolve valor genérico
            "cellular"
        }
    }
}
