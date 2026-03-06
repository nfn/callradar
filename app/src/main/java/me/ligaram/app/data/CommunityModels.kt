package me.ligaram.app.data

import com.google.gson.annotations.SerializedName

// ── Home feed ─────────────────────────────────────────────────────────────────
data class HomeComment(
    @SerializedName("id")             val id: Int,
    @SerializedName("number")         val number: String,
    @SerializedName("number_rating")  val numberRating: String?,
    @SerializedName("name")           val name: String?,
    @SerializedName("comment")        val comment: String?,
    @SerializedName("entity")         val entity: String?,
    @SerializedName("rating")         val rating: Int?,
    @SerializedName("classification") val classification: String?,
    @SerializedName("likes")          val likes: Int,
    @SerializedName("created_at")     val createdAt: String
)

data class Pagination(
    @SerializedName("next_cursor") val nextCursor: Int?,
    @SerializedName("has_more")    val hasMore: Boolean,
    @SerializedName("limit")       val limit: Int
)

data class HomeResponse(
    @SerializedName("data")       val data: List<HomeComment>,
    @SerializedName("pagination") val pagination: Pagination
)

// ── Number analysis ───────────────────────────────────────────────────────────
data class NumberAnalysis(
    @SerializedName("category")        val category: String?,
    @SerializedName("subcategory")     val subcategory: String?,
    @SerializedName("risk_level")      val riskLevel: String?,
    @SerializedName("seo_summary")     val seoSummary: String?,
    @SerializedName("advice")          val advice: String?
)

// ── Comments for a number ─────────────────────────────────────────────────────
data class NumberComment(
    @SerializedName("id")             val id: Int,
    @SerializedName("number")         val number: String,
    @SerializedName("name")           val name: String?,
    @SerializedName("comment")        val comment: String?,
    @SerializedName("entity")         val entity: String?,
    @SerializedName("rating")         val rating: Int?,
    @SerializedName("classification") val classification: String?,
    @SerializedName("likes")          val likes: Int,
    @SerializedName("created_at")     val createdAt: String
)

data class CommentsResponse(
    @SerializedName("number")        val number: String,
    @SerializedName("number_rating") val numberRating: String?,
    @SerializedName("analysis")      val analysis: NumberAnalysis?,
    @SerializedName("data")          val data: List<NumberComment>,
    @SerializedName("pagination")    val pagination: Pagination
)

// ── Entities autocomplete ─────────────────────────────────────────────────────
data class EntityItem(
    @SerializedName("id")     val id: Int,
    @SerializedName("entity") val entity: String
)

// ── Post comment ──────────────────────────────────────────────────────────────
data class PostCommentRequest(
    val number: String,
    val name: String?,
    val comment: String,
    val entity: String?,
    val rating: Int,
    val classification: String
)

data class PostCommentResponse(
    @SerializedName("id")      val id: Int,
    @SerializedName("number")  val number: String,
    @SerializedName("message") val message: String
)

// ── Generic result ────────────────────────────────────────────────────────────
sealed class CommunityResult<out T> {
    data class Success<T>(val data: T) : CommunityResult<T>()
    data class Error(val message: String) : CommunityResult<Nothing>()
}
