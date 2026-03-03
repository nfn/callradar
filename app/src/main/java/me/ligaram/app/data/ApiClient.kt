package me.ligaram.app.data

import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // Base URL — swap between ngrok (dev) and production
    private const val BASE_URL = "https://ligaram.me/api/v1/mobile/overlay"

    fun fetchCallInfo(number: String): ApiResult {
        return try {
            val cleanNumber = number.replace(Regex("[^0-9+]"), "")
            val url = "$BASE_URL/$cleanNumber"

            Log.d("ApiClient", "→ GET $url")

            val request = Request.Builder()
                .url(url)
                // Bypass ngrok browser-warning interstitial when testing with ngrok
                .addHeader("ngrok-skip-browser-warning", "true")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            Log.d("ApiClient", "← ${response.code} for $cleanNumber")

            if (response.code == 200) {
                val body = response.body?.string()
                Log.d("ApiClient", "Body: $body")
                if (!body.isNullOrBlank()) {
                    val callInfo = gson.fromJson(body, CallInfo::class.java)
                    ApiResult.Success(callInfo)
                } else {
                    ApiResult.NoResult
                }
            } else {
                Log.d("ApiClient", "Non-200 (${response.code}) — overlay suppressed")
                ApiResult.NoResult
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "Request failed: ${e.javaClass.simpleName}: ${e.message}")
            ApiResult.Error(e.message ?: "Erro desconhecido")
        }
    }
}
