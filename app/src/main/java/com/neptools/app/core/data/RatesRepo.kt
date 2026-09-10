package com.neptools.app.core.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class RateSet(
    val rates: Map<String, Double>,
    val fetchedAtMillis: Long,
    val stale: Boolean
)

object RatesRepo {

    private const val FILE = "rates_cache.json"
    private const val ENDPOINT = "https://open.er-api.com/v6/latest/NPR"
    private const val MAX_AGE_MS = 12L * 60 * 60 * 1000
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    val currencies = listOf(
        "NPR", "USD", "INR", "AED", "SAR", "QAR", "KWD", "OMR", "BHD",
        "MYR", "SGD", "HKD", "KRW", "JPY", "CNY", "THB", "AUD", "GBP",
        "EUR", "CAD", "CHF"
    )

    private val defaultRates: Map<String, Double> = mapOf(
        "NPR" to 1.0,
        "USD" to 0.00749, // ~133.5 NPR
        "INR" to 0.625,   // 1.60 NPR pegged
        "AED" to 0.0275,  // ~36.3 NPR
        "SAR" to 0.0281,  // ~35.6 NPR
        "QAR" to 0.0273,  // ~36.6 NPR
        "KWD" to 0.0023,  // ~435 NPR
        "OMR" to 0.00288, // ~347 NPR
        "BHD" to 0.00282, // ~354 NPR
        "MYR" to 0.0353,  // ~28.3 NPR
        "SGD" to 0.0101,  // ~99 NPR
        "HKD" to 0.0585,  // ~17.1 NPR
        "KRW" to 10.38,   // ~0.096 NPR
        "JPY" to 1.15,    // ~0.87 NPR
        "CNY" to 0.0545,  // ~18.3 NPR
        "THB" to 0.272,   // ~3.67 NPR
        "AUD" to 0.0115,  // ~87 NPR
        "GBP" to 0.0059,  // ~170 NPR
        "EUR" to 0.0069,  // ~145 NPR
        "CAD" to 0.0102,  // ~98 NPR
        "CHF" to 0.0066   // ~151 NPR
    )

    const val INR_PEG_NPR_PER_INR = 1.60
    fun isStale(set: RateSet): Boolean = set.stale || set.fetchedAtMillis == 0L

    fun loadCached(context: Context): RateSet? {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return RateSet(defaultRates, 0L, true)
        return runCatching {
            val o = JSONObject(f.readText())
            val r = o.getJSONObject("rates")
            val map = HashMap<String, Double>()
            val keys = r.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = r.getDouble(k)
            }
            // Enforce INR peg: NRB fixed rate; never use stale API for INR crossing
            map["INR"] = 0.625
            map.putIfAbsent("NPR", 1.0)
            val fetchedAt = o.optLong("fetchedAt", 0L)
            val age = System.currentTimeMillis() - fetchedAt
            RateSet(map, fetchedAt, age > MAX_AGE_MS || fetchedAt == 0L)
        }.getOrElse { RateSet(defaultRates, 0L, true) }
    }

    fun refresh(context: Context, onResult: (RateSet?) -> Unit) {
        executor.execute {
            val result = fetchAndCache(context)
            main.post { onResult(result) }
        }
    }

    private fun fetchAndCache(context: Context): RateSet? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val ratesJson = root.getJSONObject("rates")
            val map = HashMap<String, Double>()
            val keys = ratesJson.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = ratesJson.getDouble(k)
            }
            // Keep INR peg fixed regardless of API float
            map["INR"] = 0.625
            val now = System.currentTimeMillis()
            File(context.filesDir, FILE).writeText(
                JSONObject().put("fetchedAt", now).put("rates", JSONObject(map)).toString()
            )
            RateSet(map, now, false)
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    fun convert(amount: Double, from: String, to: String, rates: Map<String, Double>): Double? {
        val fromPerNpr = if (from == "NPR") 1.0 else rates[from] ?: return null
        val toPerNpr = if (to == "NPR") 1.0 else rates[to] ?: return null
        val nprAmount = amount / fromPerNpr
        return nprAmount * toPerNpr
    }
}
