package me.ligaram.app.data

import com.google.gson.annotations.SerializedName

data class CallInfo(
    @SerializedName("number") val number: String,
    @SerializedName("rating") val rating: String,
    @SerializedName("risk") val risk: String,
    @SerializedName("category") val category: String,
    @SerializedName("subcategory") val subcategory: String
)

sealed class ApiResult {
    data class Success(val callInfo: CallInfo) : ApiResult()
    data object NoResult : ApiResult()
    data class Error(val message: String) : ApiResult()
}
