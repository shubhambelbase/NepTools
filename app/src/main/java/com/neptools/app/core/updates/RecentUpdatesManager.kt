package com.neptools.app.core.updates

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.FuelRepo
import com.neptools.app.core.data.KalimatiRepo
import com.neptools.app.core.data.RatesRepo
import com.neptools.app.core.data.WeatherRepo
import com.neptools.app.ui.navigation.Routes
import org.json.JSONArray
import org.json.JSONObject

object RecentUpdatesManager {
    private const val PREFS = "patro_prefs"
    private const val KEY_UPDATES = "recent_live_updates_json"
    private const val MAX_RECORDS = 30

    val updates = mutableStateListOf<RecentUpdateRecord>()
    private var isLoaded = false

    /**
     * Guards every read-modify-write of [updates].
     *
     * Repository refresh callbacks invoke [recordSuccessfulUpdate] from background executor
     * threads while the UI observes the same list, so the compound sequences in this object
     * must be serialized or a refresh can interleave with a UI-triggered mutation.
     */
    private val lock = Any()

    fun load(context: Context) {
        synchronized(lock) {
            if (isLoaded) return
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_UPDATES, "") ?: ""
            updates.clear()

            if (raw.isNotBlank()) {
                runCatching {
                    val arr = JSONArray(raw)
                    for (i in 0 until arr.length()) {
                        updates.add(RecentUpdateRecord.fromJson(arr.getJSONObject(i)))
                    }
                }
            }

            // Seed with genuine timestamps from existing repository disk caches if available
            seedFromExistingCachesIfMissing(context)

            sortUpdates()
            isLoaded = true
        }
    }

    private fun seedFromExistingCachesIfMissing(context: Context) {
        val existingIds = updates.map { it.serviceId }.toSet()

        // 1. Weather
        if (!existingIds.contains("weather")) {
            val weatherLive = WeatherRepo.loadLiveCached(context)
            if (weatherLive != null && weatherLive.fetchedAtMillis > 0L) {
                updates.add(
                    RecentUpdateRecord(
                        serviceId = "weather",
                        nameNp = "मौसम पूर्वानुमान",
                        nameEn = "Weather & Forecast",
                        route = Routes.WEATHER,
                        iconType = "sun",
                        timestampMillis = weatherLive.fetchedAtMillis,
                        statusNp = "${NepaliNames.toDevanagari(weatherLive.weather.tempC)}°C · ${weatherLive.weather.conditionNp}",
                        statusEn = "${weatherLive.weather.tempC}°C · ${weatherLive.weather.conditionEn}",
                        isUnread = false
                    )
                )
            }
        }

        // 2. Fuel Prices
        if (!existingIds.contains("fuel")) {
            val fuel = FuelRepo.loadCached(context)
            if (fuel.fetchedAtMillis > 0L) {
                updates.add(
                    RecentUpdateRecord(
                        serviceId = "fuel",
                        nameNp = "पेट्रोलियम भाउ",
                        nameEn = "Fuel Prices",
                        route = Routes.FUEL,
                        iconType = "fuel",
                        timestampMillis = fuel.fetchedAtMillis,
                        statusNp = "नेपाल आयल निगम",
                        statusEn = "NOC Retail Rates",
                        isUnread = false
                    )
                )
            }
        }

        // 3. Kalimati Market
        if (!existingIds.contains("kalimati")) {
            val kalimati = KalimatiRepo.loadCached(context)
            if (kalimati.fetchedAtMillis > 0L) {
                updates.add(
                    RecentUpdateRecord(
                        serviceId = "kalimati",
                        nameNp = "कालिमाटी बजार",
                        nameEn = "Kalimati Market",
                        route = Routes.KALIMATI,
                        iconType = "leaf",
                        timestampMillis = kalimati.fetchedAtMillis,
                        statusNp = "दैनिक थोक बजार",
                        statusEn = "Daily Wholesale",
                        isUnread = false
                    )
                )
            }
        }

        // 4. Forex Currency
        if (!existingIds.contains("rates")) {
            val rates = RatesRepo.loadCached(context)
            if (rates.fetchedAtMillis > 0L) {
                updates.add(
                    RecentUpdateRecord(
                        serviceId = "rates",
                        nameNp = "विदेशी मुद्रा",
                        nameEn = "Forex Currency",
                        route = Routes.CURRENCY,
                        iconType = "coin",
                        timestampMillis = rates.fetchedAtMillis,
                        statusNp = "नेपाल राष्ट्र बैंक",
                        statusEn = "Nepal Rastra Bank",
                        isUnread = false
                    )
                )
            }
        }
    }

    private val SERVICE_ORDER = mapOf(
        "rates" to 1,
        "fuel" to 2,
        "kalimati" to 3,
        "weather" to 4
    )

    fun recordSuccessfulUpdate(
        context: Context,
        serviceId: String,
        nameNp: String,
        nameEn: String,
        route: String,
        iconType: String,
        timestampMillis: Long,
        statusNp: String? = null,
        statusEn: String? = null
    ) {
        if (timestampMillis <= 0L) return
        synchronized(lock) {
            load(context)

            // Find existing record
            val existingIndex = updates.indexOfFirst { it.serviceId == serviceId }
            var isUnread = true
            if (existingIndex >= 0) {
                val existing = updates[existingIndex]
                if (existing.timestampMillis >= timestampMillis) {
                    // Not newer than existing verified timestamp, do not create duplicate
                    return
                }
                // If user already viewed this update, preserve the read status (do not show red dot again)
                // unless the new timestamp is genuinely from a different refresh cycle (> 1 hour later)
                // or the status text actually changed.
                if (!existing.isUnread) {
                    val timeDifference = timestampMillis - existing.timestampMillis
                    val statusChanged = (statusEn != null && statusEn != existing.statusEn) ||
                            (statusNp != null && statusNp != existing.statusNp)
                    if (!statusChanged && timeDifference < 60 * 60 * 1000L) {
                        isUnread = false
                    }
                }
                updates.removeAt(existingIndex)
            }

            val record = RecentUpdateRecord(
                serviceId = serviceId,
                nameNp = nameNp,
                nameEn = nameEn,
                route = route,
                iconType = iconType,
                timestampMillis = timestampMillis,
                statusNp = statusNp,
                statusEn = statusEn,
                isUnread = isUnread
            )

            updates.add(record)
            while (updates.size > MAX_RECORDS) {
                updates.removeAt(updates.lastIndex)
            }

            sortUpdates()
            persist(context)
        }
    }

    fun markSeen(context: Context, serviceIds: List<String>) {
        synchronized(lock) {
            load(context)
            var changed = false
            for (i in 0 until updates.size) {
                val item = updates[i]
                if (item.serviceId in serviceIds && item.isUnread) {
                    updates[i] = item.copy(isUnread = false)
                    changed = true
                }
            }
            if (changed) {
                persist(context)
            }
        }
    }

    fun markAllSeen(context: Context) {
        synchronized(lock) {
            load(context)
            var changed = false
            for (i in 0 until updates.size) {
                val item = updates[i]
                if (item.isUnread) {
                    updates[i] = item.copy(isUnread = false)
                    changed = true
                }
            }
            if (changed) {
                persist(context)
            }
        }
    }

    fun clearOldUpdates(context: Context) {
        synchronized(lock) {
            updates.clear()
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_UPDATES).apply()
        }
    }

    private fun sortUpdates() {
        // Locked, deterministic display order:
        // Service cards stay permanently anchored in their fixed slots (Forex -> Fuel -> Kalimati -> Weather)
        // so that tapping a card or refreshing never shuffles or moves the layout.
        val sorted = updates.sortedWith(
            compareBy<RecentUpdateRecord> { SERVICE_ORDER[it.serviceId] ?: 99 }
                .thenByDescending { it.timestampMillis }
        )
        if (updates != sorted) {
            updates.clear()
            updates.addAll(sorted)
        }
    }

    private fun persist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        updates.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_UPDATES, arr.toString()).apply()
    }
}
