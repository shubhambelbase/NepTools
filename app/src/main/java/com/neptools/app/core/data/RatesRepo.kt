package com.neptools.app.core.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Immutable
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.concurrent.Executors

@Immutable
data class NrbRateDetail(
    val currency: String,
    val name: String,
    val unit: Int,
    val buy: Double,
    val sell: Double
)

@Immutable
data class RateSet(
    val rates: Map<String, Double>,
    val nrbDetails: Map<String, NrbRateDetail> = emptyMap(),
    val fetchedAtMillis: Long,
    val stale: Boolean,
    val effectiveDate: String = "",
    val source: String = "Nepal Rastra Bank (NRB)"
)

object RatesRepo {

    private const val FILE = "rates_cache.json"
    private const val NRB_ENDPOINT_BASE = "https://www.nrb.org.np/api/forex/v1/rates"
    private const val FALLBACK_ENDPOINT = "https://open.er-api.com/v6/latest/NPR"
    private const val MAX_AGE_MS = 12L * 60 * 60 * 1000 // 12 hours
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    val currencies = listOf(
        "NPR", "USD", "INR", "AED", "SAR", "QAR", "KWD", "OMR", "BHD",
        "MYR", "SGD", "HKD", "KRW", "JPY", "CNY", "THB", "AUD", "GBP",
        "EUR", "CAD", "CHF"
    )

    // Official Nepal Rastra Bank baseline exchange rates
    val defaultDetails: Map<String, NrbRateDetail> = mapOf(
        "INR" to NrbRateDetail("INR", "Indian Rupee", 100, 160.00, 160.15),
        "USD" to NrbRateDetail("USD", "U.S. Dollar", 1, 153.24, 153.84),
        "EUR" to NrbRateDetail("EUR", "European Euro", 1, 176.82, 177.52),
        "GBP" to NrbRateDetail("GBP", "UK Pound Sterling", 1, 206.38, 207.19),
        "CHF" to NrbRateDetail("CHF", "Swiss Franc", 1, 187.05, 187.78),
        "AUD" to NrbRateDetail("AUD", "Australian Dollar", 1, 101.45, 101.85),
        "CAD" to NrbRateDetail("CAD", "Canadian Dollar", 1, 111.75, 112.19),
        "SGD" to NrbRateDetail("SGD", "Singapore Dollar", 1, 117.82, 118.28),
        "JPY" to NrbRateDetail("JPY", "Japanese Yen", 10, 10.35, 10.39),
        "CNY" to NrbRateDetail("CNY", "Chinese Yuan", 1, 21.48, 21.56),
        "SAR" to NrbRateDetail("SAR", "Saudi Arabian Riyal", 1, 40.85, 41.01),
        "QAR" to NrbRateDetail("QAR", "Qatari Riyal", 1, 42.04, 42.20),
        "THB" to NrbRateDetail("THB", "Thai Baht", 1, 4.45, 4.47),
        "AED" to NrbRateDetail("AED", "UAE Dirham", 1, 41.72, 41.89),
        "MYR" to NrbRateDetail("MYR", "Malaysian Ringgit", 1, 35.12, 35.26),
        "KRW" to NrbRateDetail("KRW", "South Korean Won", 100, 11.25, 11.30),
        "KWD" to NrbRateDetail("KWD", "Kuwaiti Dinar", 1, 501.20, 503.16),
        "BHD" to NrbRateDetail("BHD", "Bahraini Dinar", 1, 406.50, 408.09),
        "OMR" to NrbRateDetail("OMR", "Omani Rial", 1, 398.05, 399.61),
        "HKD" to NrbRateDetail("HKD", "Hong Kong Dollar", 1, 19.64, 19.72)
    )

    val defaultRates: Map<String, Double> = defaultDetails.mapValues { (_, d) ->
        if (d.buy > 0.0) d.unit / d.buy else 1.0
    }.toMutableMap().apply {
        put("NPR", 1.0)
        put("INR", 0.625) // Fixed 1.60 INR Peg
    }

    const val INR_PEG_NPR_PER_INR = 1.60
    fun isStale(set: RateSet): Boolean = set.stale || set.fetchedAtMillis == 0L

    fun loadCached(context: Context): RateSet {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return RateSet(defaultRates, defaultDetails, 0L, true)
        return runCatching {
            val o = JSONObject(f.readText())
            val r = o.getJSONObject("rates")
            val map = HashMap<String, Double>()
            val keys = r.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = r.getDouble(k)
            }
            map["INR"] = 0.625
            map.putIfAbsent("NPR", 1.0)

            val detailsMap = HashMap<String, NrbRateDetail>()
            val detailsArr = o.optJSONArray("nrbDetails")
            if (detailsArr != null) {
                for (i in 0 until detailsArr.length()) {
                    val d = detailsArr.getJSONObject(i)
                    val code = d.getString("currency")
                    detailsMap[code] = NrbRateDetail(
                        currency = code,
                        name = d.optString("name", code),
                        unit = d.optInt("unit", 1),
                        buy = d.optDouble("buy", 0.0),
                        sell = d.optDouble("sell", 0.0)
                    )
                }
            }

            val fetchedAt = o.optLong("fetchedAt", 0L)
            val age = System.currentTimeMillis() - fetchedAt
            RateSet(
                rates = map,
                nrbDetails = if (detailsMap.isNotEmpty()) detailsMap else defaultDetails,
                fetchedAtMillis = fetchedAt,
                stale = age > MAX_AGE_MS || fetchedAt == 0L,
                effectiveDate = o.optString("effectiveDate", ""),
                source = o.optString("source", "Nepal Rastra Bank (NRB)")
            )
        }.getOrElse { RateSet(defaultRates, defaultDetails, 0L, true) }
    }

    fun refresh(context: Context, onResult: (RateSet) -> Unit) {
        executor.execute {
            val result = fetchAndCache(context) ?: loadCached(context)
            main.post { onResult(result) }
        }
    }

    private fun fetchAndCache(context: Context): RateSet? {
        // Try Official Nepal Rastra Bank (NRB) first
        val nrbResult = fetchFromNrb(context)
        if (nrbResult != null) return nrbResult

        // Fallback to Open ER-API
        return fetchFromErApi(context)
    }

    private fun fetchFromNrb(context: Context): RateSet? {
        var conn: HttpURLConnection? = null
        return try {
            val today = LocalDate.now()
            val fromDate = today.minusDays(7)
            val urlStr = "$NRB_ENDPOINT_BASE?page=1&per_page=1&from=$fromDate&to=$today"
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val dataObj = root.optJSONObject("data") ?: return null
            val payloadArr = dataObj.optJSONArray("payload") ?: return null
            if (payloadArr.length() == 0) return null

            val latest = payloadArr.getJSONObject(0)
            val effectiveDate = latest.optString("date", "")
            val ratesArr = latest.optJSONArray("rates") ?: return null

            val ratesMap = HashMap<String, Double>()
            val detailsMap = HashMap<String, NrbRateDetail>()

            for (i in 0 until ratesArr.length()) {
                val item = ratesArr.getJSONObject(i)
                val curObj = item.getJSONObject("currency")
                val iso3 = curObj.getString("iso3").trim()
                val unit = curObj.optInt("unit", 1)
                val name = curObj.optString("name", iso3)
                val buy = item.optString("buy", "0").replace(",", "").toDoubleOrNull() ?: 0.0
                val sell = item.optString("sell", "0").replace(",", "").toDoubleOrNull() ?: 0.0

                if (buy > 0.0) {
                    val nprPerForeign = buy / unit.toDouble()
                    ratesMap[iso3] = 1.0 / nprPerForeign
                    detailsMap[iso3] = NrbRateDetail(
                        currency = iso3,
                        name = name,
                        unit = unit,
                        buy = buy,
                        sell = sell
                    )
                }
            }

            ratesMap["NPR"] = 1.0
            ratesMap["INR"] = 0.625 // Always 1.60 fixed peg

            val now = System.currentTimeMillis()
            val detailsJsonArr = JSONArray()
            detailsMap.values.forEach { d ->
                detailsJsonArr.put(
                    JSONObject().apply {
                        put("currency", d.currency)
                        put("name", d.name)
                        put("unit", d.unit)
                        put("buy", d.buy)
                        put("sell", d.sell)
                    }
                )
            }

            val cacheJson = JSONObject().apply {
                put("rates", JSONObject(ratesMap))
                put("nrbDetails", detailsJsonArr)
                put("fetchedAt", now)
                put("effectiveDate", effectiveDate)
                put("source", "Nepal Rastra Bank (NRB)")
            }

            File(context.filesDir, FILE).writeText(cacheJson.toString())
            com.neptools.app.core.updates.RecentUpdatesManager.recordSuccessfulUpdate(
                context = context,
                serviceId = "rates",
                nameNp = "विदेशी मुद्रा",
                nameEn = "Forex Currency",
                route = com.neptools.app.ui.navigation.Routes.CURRENCY,
                iconType = "coin",
                timestampMillis = now,
                statusNp = "नेपाल राष्ट्र बैंक",
                statusEn = "Nepal Rastra Bank"
            )
            RateSet(
                rates = ratesMap,
                nrbDetails = detailsMap,
                fetchedAtMillis = now,
                stale = false,
                effectiveDate = effectiveDate,
                source = "Nepal Rastra Bank (NRB)"
            )
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun fetchFromErApi(context: Context): RateSet? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(FALLBACK_ENDPOINT).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
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
            map["INR"] = 0.625
            map.putIfAbsent("NPR", 1.0)
            val now = System.currentTimeMillis()
            val cacheJson = JSONObject().apply {
                put("rates", JSONObject(map))
                put("fetchedAt", now)
                put("source", "Exchange Rates API")
            }
            File(context.filesDir, FILE).writeText(cacheJson.toString())
            com.neptools.app.core.updates.RecentUpdatesManager.recordSuccessfulUpdate(
                context = context,
                serviceId = "rates",
                nameNp = "विदेशी मुद्रा",
                nameEn = "Forex Currency",
                route = com.neptools.app.ui.navigation.Routes.CURRENCY,
                iconType = "coin",
                timestampMillis = now,
                statusNp = "विदेशी विनिमय दर",
                statusEn = "Exchange Rates"
            )
            RateSet(
                rates = map,
                nrbDetails = defaultDetails,
                fetchedAtMillis = now,
                stale = false,
                source = "Exchange Rates API"
            )
        } catch (_: Exception) {
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
