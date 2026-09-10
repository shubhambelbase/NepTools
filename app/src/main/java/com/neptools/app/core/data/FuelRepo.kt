package com.neptools.app.core.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class FuelRateSet(
    val petrol: Double,
    val diesel: Double,
    val kerosene: Double,
    val lpg: Double,
    val atfDomestic: Double,
    val categoryPrices: Map<String, Map<String, Double>>, // "first", "second", "third"
    val fetchedAtMillis: Long,
    val stale: Boolean
)

object FuelRepo {

    private const val FILE = "fuel_cache.json"
    // Public NOC / Nepal Patro sync endpoint (or raw github/json fallback)
    private const val ENDPOINT = "https://raw.githubusercontent.com/nepalpatro/data/main/fuel_rates.json"
    private const val MAX_AGE_MS = 24L * 60 * 60 * 1000 // 24 hours
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    val regions = listOf(
        "third" to "तेस्रो वर्ग (काठमाडौं, पोखरा, दिपायल)",
        "second" to "दोस्रो वर्ग (सुर्खेत, दाङ)",
        "first" to "पहिलो वर्ग (विराटनगर, वीरगञ्ज, भैरहवा, नेपालगञ्ज, धनगढी)"
    )

    // Official NOC baseline categorized prices (in NPR)
    val defaultRates = FuelRateSet(
        petrol = 167.50,
        diesel = 155.50,
        kerosene = 155.50,
        lpg = 1895.00,
        atfDomestic = 137.00,
        categoryPrices = mapOf(
            "first" to mapOf(
                "petrol" to 165.00,
                "diesel" to 153.00,
                "kerosene" to 153.00,
                "lpg" to 1895.00,
                "atf" to 137.00
            ),
            "second" to mapOf(
                "petrol" to 166.50,
                "diesel" to 154.50,
                "kerosene" to 154.50,
                "lpg" to 1895.00,
                "atf" to 137.00
            ),
            "third" to mapOf(
                "petrol" to 167.50,
                "diesel" to 155.50,
                "kerosene" to 155.50,
                "lpg" to 1895.00,
                "atf" to 137.00
            )
        ),
        fetchedAtMillis = 0L,
        stale = true
    )

    fun loadCached(context: Context): FuelRateSet {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return defaultRates
        return runCatching {
            val o = JSONObject(f.readText())
            val age = System.currentTimeMillis() - o.getLong("fetchedAt")
            val catObj = o.optJSONObject("categories")
            val categoriesMap = HashMap<String, Map<String, Double>>()

            if (catObj != null) {
                val keys = catObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val sub = catObj.getJSONObject(k)
                    val subMap = HashMap<String, Double>()
                    val subKeys = sub.keys()
                    while (subKeys.hasNext()) {
                        val sk = subKeys.next()
                        subMap[sk] = sub.getDouble(sk)
                    }
                    categoriesMap[k] = subMap
                }
            }

            FuelRateSet(
                petrol = o.optDouble("petrol", 167.50),
                diesel = o.optDouble("diesel", 155.50),
                kerosene = o.optDouble("kerosene", 155.50),
                lpg = o.optDouble("lpg", 1895.00),
                atfDomestic = o.optDouble("atf", 137.00),
                categoryPrices = if (categoriesMap.isNotEmpty()) categoriesMap else defaultRates.categoryPrices,
                fetchedAtMillis = o.getLong("fetchedAt"),
                stale = age > MAX_AGE_MS
            )
        }.getOrElse { defaultRates }
    }

    fun refresh(context: Context, onResult: (FuelRateSet) -> Unit) {
        executor.execute {
            val result = fetchAndCache(context) ?: loadCached(context)
            main.post { onResult(result) }
        }
    }

    private fun fetchAndCache(context: Context): FuelRateSet? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val o = JSONObject(body)
            val now = System.currentTimeMillis()

            File(context.filesDir, FILE).writeText(
                JSONObject(body).put("fetchedAt", now).toString()
            )
            loadCached(context)
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
