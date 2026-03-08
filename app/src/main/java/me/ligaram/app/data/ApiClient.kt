package me.ligaram.app.data

import android.content.Context
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

    private const val BASE_URL  = "https://api.ligaram.me/api/v1/overlay"
    private const val API_TOKEN = "lUDf9WGHuW7OMbeNvQmZ8vIAwJLvTUo5HiwcCY9cPn7"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun fetchCallInfo(number: String): ApiResult {
        return try {
            val cleanNumber = number.replace(Regex("[^0-9+]"), "")
            val url = "$BASE_URL/$cleanNumber"

            Log.d("ApiClient", "→ GET $url")

            val builder = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $API_TOKEN")
                .addHeader("Accept", "application/json")

            appContext?.let { ctx ->
                DeviceHeaders.build(ctx).forEach { (k, v) -> builder.addHeader(k, v) }
            }

            val response = client.newCall(builder.get().build()).execute()
            Log.d("ApiClient", "← ${response.code} for $cleanNumber")

            if (response.code == 200) {
                val body = response.body.string()
                Log.d("ApiClient", "Body: $body")
                if (body.isNotBlank()) {
                    val callInfo = gson.fromJson(body, CallInfo::class.java)
                    ApiResult.Success(callInfo)
                } else {
                    ApiResult.NoResult
                }
            } else {
                Log.d("ApiClient", "Non-200 (${response.code}) - overlay suppressed")
                ApiResult.NoResult
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "Request failed: ${e.javaClass.simpleName}: ${e.message}")
            ApiResult.Error(e.message ?: "Erro desconhecido")
        }
    }
}
