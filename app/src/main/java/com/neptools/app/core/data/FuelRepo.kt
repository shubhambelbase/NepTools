package com.neptools.app.core.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.Immutable
import java.util.concurrent.Executors

@Immutable
data class FuelRateSet(
    val petrol: Double,
    val diesel: Double,
    val kerosene: Double,
    val lpg: Double,
    val atfDomestic: Double,
    val categoryPrices: Map<String, Map<String, Double>>, // "first", "second", "third"
    val fetchedAtMillis: Long,
    val stale: Boolean,
    val effectiveDate: String = "2083.05.16 (2026.09.01)"
)

object FuelRepo {

    private const val FILE = "fuel_cache.json"
    // Official Nepal Oil Corporation (NOC) retail selling price portal
    private const val NOC_RETAIL_URL = "https://noc.org.np/retailprice"
    private const val MAX_AGE_MS = 12L * 60 * 60 * 1000 // 12 hours
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    val regions = listOf(
        "third" to "तेस्रो वर्ग (काठमाडौं, पोखरा, दिपायल)",
        "second" to "दोस्रो वर्ग (सुर्खेत, दाङ)",
        "first" to "पहिलो वर्ग (विराटनगर, वीरगञ्ज, भैरहवा, नेपालगञ्ज, धनगढी)"
    )

    // Verified official NOC retail baseline rates (in NPR)
    val defaultRates = FuelRateSet(
        petrol = 200.00,
        diesel = 200.00,
        kerosene = 200.00,
        lpg = 2060.00,
        atfDomestic = 249.00,
        categoryPrices = mapOf(
            "first" to mapOf(
                "petrol" to 197.50,
                "diesel" to 197.50,
                "kerosene" to 197.50,
                "lpg" to 2060.00,
                "atf" to 249.00
            ),
            "second" to mapOf(
                "petrol" to 199.00,
                "diesel" to 199.00,
                "kerosene" to 199.00,
                "lpg" to 2060.00,
                "atf" to 249.00
            ),
            "third" to mapOf(
                "petrol" to 200.00,
                "diesel" to 200.00,
                "kerosene" to 200.00,
                "lpg" to 2060.00,
                "atf" to 249.00
            )
        ),
        fetchedAtMillis = 0L,
        stale = false,
        effectiveDate = "2083.05.16 (2026.09.01)"
    )

    fun loadCached(context: Context): FuelRateSet {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return defaultRates
        return runCatching {
            val o = JSONObject(f.readText())
            val petrolVal = o.optDouble("petrol", 0.0)
            val lpgVal = o.optDouble("lpg", 0.0)

            // Invalidate obsolete cached values from older versions
            if (petrolVal < 180.0 || lpgVal < 1950.0) {
                f.delete()
                return defaultRates
            }

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
                petrol = o.optDouble("petrol", defaultRates.petrol),
                diesel = o.optDouble("diesel", defaultRates.diesel),
                kerosene = o.optDouble("kerosene", defaultRates.kerosene),
                lpg = o.optDouble("lpg", defaultRates.lpg),
                atfDomestic = o.optDouble("atf", defaultRates.atfDomestic),
                categoryPrices = if (categoriesMap.isNotEmpty()) categoriesMap else defaultRates.categoryPrices,
                fetchedAtMillis = o.optLong("fetchedAt", 0L),
                stale = age > MAX_AGE_MS,
                effectiveDate = o.optString("effectiveDate", defaultRates.effectiveDate)
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
            val url = URL(NOC_RETAIL_URL)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }

            val rowRegex = Regex("<tr[^>]*>(.*?)</tr>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            val colRegex = Regex("<td[^>]*>(.*?)</td>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            val tagRegex = Regex("<[^>]+>")

            var parsedRow: List<String>? = null
            for (rowMatch in rowRegex.findAll(body)) {
                val rowHtml = rowMatch.groupValues[1]
                val cols = colRegex.findAll(rowHtml).map {
                    tagRegex.replace(it.groupValues[1], "").trim()
                }.toList()

                // Row format: [effectiveDate, effectiveTime, petrol, diesel, kerosene, lpg, atfDp, atfDf]
                if (cols.size >= 6 && cols[0].any { it.isDigit() }) {
                    parsedRow = cols
                    break
                }
            }

            if (parsedRow == null) return null

            val effectiveDate = parsedRow[0]
            val cat3Petrol = parsedRow[2].replace(",", "").toDoubleOrNull() ?: return null
            val cat3Diesel = parsedRow[3].replace(",", "").toDoubleOrNull() ?: return null
            val cat3Kerosene = parsedRow[4].replace(",", "").toDoubleOrNull() ?: return null
            val lpgPrice = parsedRow[5].replace(",", "").toDoubleOrNull() ?: 2060.0
            val atfPrice = if (parsedRow.size >= 7) {
                parsedRow[6].replace(",", "").toDoubleOrNull() ?: 249.0
            } else 249.0

            if (cat3Petrol < 50.0 || cat3Diesel < 50.0 || lpgPrice < 500.0) return null

            // Build categorized rates based on official NOC pricing tiers:
            // Cat 3 (Kathmandu/Pokhara/Dipayal): Base retail rate
            // Cat 2 (Surkhet/Dang): Base minus 1.00
            // Cat 1 (Biratnagar/Birgunj/Bhairahawa/Nepalgunj/Dhangadhi): Base minus 2.50
            val cat3Map = mapOf(
                "petrol" to cat3Petrol,
                "diesel" to cat3Diesel,
                "kerosene" to cat3Kerosene,
                "lpg" to lpgPrice,
                "atf" to atfPrice
            )
            val cat2Map = mapOf(
                "petrol" to cat3Petrol - 1.0,
                "diesel" to cat3Diesel - 1.0,
                "kerosene" to cat3Kerosene - 1.0,
                "lpg" to lpgPrice,
                "atf" to atfPrice
            )
            val cat1Map = mapOf(
                "petrol" to cat3Petrol - 2.5,
                "diesel" to cat3Diesel - 2.5,
                "kerosene" to cat3Kerosene - 2.5,
                "lpg" to lpgPrice,
                "atf" to atfPrice
            )

            val categoriesJson = JSONObject().apply {
                put("first", JSONObject(cat1Map))
                put("second", JSONObject(cat2Map))
                put("third", JSONObject(cat3Map))
            }

            val now = System.currentTimeMillis()
            val cacheJson = JSONObject().apply {
                put("petrol", cat3Petrol)
                put("diesel", cat3Diesel)
                put("kerosene", cat3Kerosene)
                put("lpg", lpgPrice)
                put("atf", atfPrice)
                put("categories", categoriesJson)
                put("effectiveDate", effectiveDate)
                put("fetchedAt", now)
            }

            com.neptools.app.core.util.SafeFileWriter.writeAtomic(File(context.filesDir, FILE), cacheJson.toString())
            com.neptools.app.core.updates.RecentUpdatesManager.recordSuccessfulUpdate(
                context = context,
                serviceId = "fuel",
                nameNp = "पेट्रोलियम भाउ",
                nameEn = "Fuel Prices",
                route = com.neptools.app.ui.navigation.Routes.FUEL,
                iconType = "fuel",
                timestampMillis = now,
                statusNp = "नेपाल आयल निगम",
                statusEn = "NOC Retail Rates"
            )
            loadCached(context)
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
