package com.neptools.app.core.updates

import androidx.compose.runtime.Immutable
import org.json.JSONObject

@Immutable
data class RecentUpdateRecord(
    val serviceId: String,
    val nameNp: String,
    val nameEn: String,
    val route: String,
    val iconType: String,
    val timestampMillis: Long,
    val statusNp: String? = null,
    val statusEn: String? = null,
    val isUnread: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("serviceId", serviceId)
        put("nameNp", nameNp)
        put("nameEn", nameEn)
        put("route", route)
        put("iconType", iconType)
        put("timestampMillis", timestampMillis)
        statusNp?.let { put("statusNp", it) }
        statusEn?.let { put("statusEn", it) }
        put("isUnread", isUnread)
    }

    companion object {
        fun fromJson(json: JSONObject): RecentUpdateRecord = RecentUpdateRecord(
            serviceId = json.getString("serviceId"),
            nameNp = json.getString("nameNp"),
            nameEn = json.getString("nameEn"),
            route = json.getString("route"),
            iconType = json.getString("iconType"),
            timestampMillis = json.getLong("timestampMillis"),
            statusNp = json.optString("statusNp").takeIf { it.isNotEmpty() },
            statusEn = json.optString("statusEn").takeIf { it.isNotEmpty() },
            isUnread = json.optBoolean("isUnread", false)
        )
    }
}
