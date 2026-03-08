package me.ligaram.app.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object CommunityApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private const val BASE  = "https://api.ligaram.me/api/v1"
    private const val TOKEN = "lUDf9WGHuW7OMbeNvQmZ8vIAwJLvTUo5HiwcCY9cPn7"

    // Context guardado na primeira chamada a init() - chamada na MainActivity
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun deviceHeaders(): Map<String, String> =
        appContext?.let { DeviceHeaders.build(it) } ?: emptyMap()

    private fun get(url: String): okhttp3.Response {
        val builder = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $TOKEN")
            .addHeader("Accept", "application/json")
        deviceHeaders().forEach { (k, v) -> builder.addHeader(k, v) }
        return client.newCall(builder.get().build()).execute()
    }

    private fun post(url: String, body: Any): okhttp3.Response {
        val json = gson.toJson(body)
        val builder = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $TOKEN")
            .addHeader("Accept", "application/json")
            .post(json.toRequestBody(JSON))
        deviceHeaders().forEach { (k, v) -> builder.addHeader(k, v) }
        return client.newCall(builder.build()).execute()
    }

    // ── GET /home?limit=20&cursor=X ───────────────────────────────────────────
    fun fetchHome(limit: Int = 20, cursor: Int? = null): CommunityResult<HomeResponse> {
        return try {
            val url = buildString {
                append("$BASE/home?limit=$limit")
                if (cursor != null) append("&cursor=$cursor")
            }
            Log.d("CommunityApi", "GET $url")
            val resp = get(url)
            val body = resp.body.string()
            if (resp.isSuccessful) {
                val parsed = gson.fromJson(body, HomeResponse::class.java)
                CommunityResult.Success(parsed)
            } else {
                CommunityResult.Error("Erro ${resp.code}")
            }
        } catch (e: Exception) {
            Log.e("CommunityApi", "fetchHome: ${e.message}")
            CommunityResult.Error(e.message ?: "Erro de rede")
        }
    }

    // ── GET /comments/:number?limit=20&cursor=X ───────────────────────────────
    fun fetchComments(number: String, limit: Int = 20, cursor: Int? = null): CommunityResult<CommentsResponse> {
        return try {
            val url = buildString {
                append("$BASE/comments/$number?limit=$limit")
                if (cursor != null) append("&cursor=$cursor")
            }
            Log.d("CommunityApi", "GET $url")
            val resp = get(url)
            val body = resp.body.string()
            if (resp.isSuccessful) {
                val parsed = gson.fromJson(body, CommentsResponse::class.java)
                CommunityResult.Success(parsed)
            } else {
                CommunityResult.Error("Erro ${resp.code}")
            }
        } catch (e: Exception) {
            Log.e("CommunityApi", "fetchComments: ${e.message}")
            CommunityResult.Error(e.message ?: "Erro de rede")
        }
    }

    // ── POST /comments/:number/store ──────────────────────────────────────────
    fun postComment(req: PostCommentRequest): CommunityResult<PostCommentResponse> {
        return try {
            val url = "$BASE/comments/${req.number}/store"
            Log.d("CommunityApi", "POST $url")
            val body = mapOf(
                "name"           to req.name,
                "comment"        to req.comment,
                "entity"         to req.entity,
                "rating"         to req.rating,
                "classification" to req.classification
            )
            val resp = post(url, body)
            val respBody = resp.body.string()
            if (resp.code == 201) {
                val parsed = gson.fromJson(respBody, PostCommentResponse::class.java)
                CommunityResult.Success(parsed)
            } else {
                // Tenta extrair mensagem de erro da API
                try {
                    val err = gson.fromJson(respBody, Map::class.java)
                    CommunityResult.Error(err["message"]?.toString() ?: "Erro ${resp.code}")
                } catch (_: Exception) {
                    CommunityResult.Error("Erro ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("CommunityApi", "postComment: ${e.message}")
            CommunityResult.Error(e.message ?: "Erro de rede")
        }
    }

    // ── POST /comments/:comment_id/like ──────────────────────────────────────
    // Toggle: se o IP já tem like → remove; caso contrário → adiciona.
    // Sem body - o IP é lido no servidor via request.ip
    fun toggleLike(commentId: Int): CommunityResult<LikeResponse> {
        return try {
            val url = "$BASE/comments/$commentId/like"
            Log.d("CommunityApi", "POST $url")
            val resp = post(url, emptyMap<String, Any>())
            val body = resp.body.string()
            if (resp.isSuccessful) {
                val parsed = gson.fromJson(body, LikeResponse::class.java)
                CommunityResult.Success(parsed)
            } else {
                CommunityResult.Error("Erro ${resp.code}")
            }
        } catch (e: Exception) {
            Log.e("CommunityApi", "toggleLike: ${e.message}")
            CommunityResult.Error(e.message ?: "Erro de rede")
        }
    }

    // ── GET /entities?q=X ────────────────────────────────────────────────────
    // Endpoint: GET /api/v1/entities?q={string}  → { data: [{id, entity}] }
    // Usado no autocomplete do formulário - não é obrigatório, o utilizador pode escrever livremente.
    fun searchEntities(query: String): CommunityResult<List<EntityItem>> {
        return try {
            val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "$BASE/entities?q=$encoded"
            Log.d("CommunityApi", "GET $url")
            val resp = get(url)
            val body = resp.body.string()
            if (resp.isSuccessful) {
                // A API devolve { data: [...] }
                val wrapper = gson.fromJson(body, EntityListWrapper::class.java)
                CommunityResult.Success(wrapper?.data ?: emptyList())
            } else {
                CommunityResult.Success(emptyList())
            }
        } catch (e: Exception) {
            Log.e("CommunityApi", "searchEntities: ${e.message}")
            CommunityResult.Success(emptyList())
        }
    }

    private data class EntityListWrapper(val data: List<EntityItem>?)
}
