package com.neptools.app.core.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.neptools.app.core.calendar.NepaliNames
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.concurrent.Executors

import androidx.compose.runtime.Immutable

@Immutable
data class VegetablePrice(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val unitNp: String,
    val unitEn: String,
    val minPrice: Double,
    val maxPrice: Double,
    val avgPrice: Double,
    val category: String // "vegetables", "leafy", "fruits", "spices"
)

@Immutable
data class KalimatiData(
    val dateNp: String,
    val dateEn: String,
    val marketNameNp: String,
    val marketNameEn: String,
    val items: List<VegetablePrice>,
    val fetchedAtMillis: Long,
    val isStale: Boolean
)

object KalimatiRepo {
    private const val FILE = "kalimati_cache.json"
    private const val LIVE_ENDPOINT_EN = "https://kalimatimarket.gov.np/api/daily-prices/en"
    private const val MAX_AGE_MS = 6L * 60 * 60 * 1000 // 6 hours
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    const val DEFAULT_MARKET_NP = "कालिमाटी फलफूल तथा तरकारी बजार विकास समिति"
    const val DEFAULT_MARKET_EN = "Kalimati Fruits & Vegetable Market Board"

    private fun nepNumStr(n: Any): String =
        n.toString().map { if (it in '0'..'9') ('०' + (it - '0')) else it }.joinToString("")

    private val NEP_NAME_MAP = mapOf(
        "tomato big(nepali)" to "गोलभेंडा ठूलो (नेपाली)",
        "tomato big(indian)" to "गोलभेंडा ठूलो (भारतीय)",
        "tomato small(local)" to "गोलभेंडा सानो (लोकल)",
        "tomato small(tunnel)" to "गोलभेंडा सानो (टनेल)",
        "tomato small(indian)" to "गोलभेंडा सानो (भारतीय)",
        "potato red" to "आलु रातो",
        "potato red(indian)" to "आलु रातो (भारतीय)",
        "potato red(mude)" to "आलु रातो (मुडे)",
        "potato white" to "आलु सेतो",
        "potato white(indian)" to "आलु सेतो (भारतीय)",
        "onion dry" to "प्याज सुकेको",
        "onion dry (indian)" to "प्याज सुकेको (भारतीय)",
        "onion dry (chinese)" to "प्याज सुकेको (चिनियाँ)",
        "onion green" to "प्याज हरियो",
        "carrot(local)" to "गाँजर (लोकल)",
        "carrot(terai)" to "गाँजर (तराई)",
        "cabbage(local)" to "बन्दा (लोकल)",
        "cabbage(terai)" to "बन्दा (तराई)",
        "cauliflower local" to "काउली (स्थानीय)",
        "cauliflower terai" to "काउली (तराई)",
        "radish white(local)" to "मूला सेतो (लोकल)",
        "radish red" to "मूला रातो",
        "brinjal long" to "भन्टा लाम्चो",
        "brinjal round" to "भन्टा डल्लो",
        "bitter gourd" to "तितो करेला",
        "bottle gourd" to "लौका",
        "pointed gourd(local)" to "परवर (लोकल)",
        "snake gourd" to "चिचिण्डो",
        "smooth gourd" to "घिरौला",
        "sponge gourd" to "पाटे घिरौला",
        "pumpkin" to "फर्सी पाकेको",
        "squash" to "स्कूस",
        "lady finger" to "भिण्डी",
        "french beans(local)" to "सिमी (लोकल)",
        "french beans(rajma)" to "राजमा सिमी",
        "green peas" to "मटरकोसा",
        "cucumber(local)" to "काँक्रो (स्थानीय)",
        "cucumber(hybrid)" to "काँक्रो (हाइब्रिड)",
        "capsicum" to "भेडे खुर्सानी",
        "mushroom(button)" to "च्याउ (डल्ले)",
        "mushroom(oyster)" to "च्याउ (कन्य)",
        "spinach leaf" to "पालुङ्गो साग",
        "mustard green" to "तोरीको साग",
        "broad leaf mustard" to "रायोको साग",
        "coriander green" to "धनियाँ हरियो",
        "fenugreek leaf" to "मेथीको साग",
        "cress leaf" to "चमसूरको साग",
        "ginger" to "अदुवा",
        "garlic dry chinese" to "लसुन सुकेको (चिनियाँ)",
        "garlic dry nepali" to "लसुन सुकेको (नेपाली)",
        "garlic green" to "लसुन हरियो",
        "chilli green" to "खुर्सानी हरियो",
        "chilli green(long)" to "खुर्सानी हरियो (लाम्चो)",
        "chilli green(bullet)" to "खुर्सानी हरियो (बुलेट)",
        "chilli green(akbare)" to "अकबरे खुर्सानी",
        "chilli dry" to "खुर्सानी सुकेको",
        "lemon" to "कागती",
        "apple(fuji)" to "स्याउ (फुजी)",
        "apple(jholey)" to "स्याउ (झोले)",
        "banana" to "केरा (दर्जन)",
        "pomegranate" to "अनार",
        "watermelon(green)" to "तरबुजा (हरियो)",
        "orange(nepali)" to "सुन्तला (नेपाली)",
        "orange(indian)" to "सुन्तला (भारतीय)",
        "sweet orange" to "मौसम",
        "papaya(nepali)" to "मेवा (नेपाली)",
        "papaya(indian)" to "मेवा (भारतीय)",
        "mango(maldah)" to "आँप (मालदह)",
        "mango(daspahari)" to "आँप (दशहरी)",
        "grapes(green)" to "अङ्गुर (हरियो)",
        "grapes(black)" to "अङ्गुर (कालो)",
        "kiwi" to "किवी",
        "avocado" to "एभोकाडो",
        "dragon fruit(nepali)" to "ड्रागन फ्रुट (नेपाली)"
    )

    val defaultItems = listOf(
        VegetablePrice("tomato_big", "गोलभेंडा ठूलो (नेपाली)", "Tomato Big (Local)", "के.जी.", "KG", 80.0, 90.0, 85.0, "vegetables"),
        VegetablePrice("tomato_small", "गोलभेंडा सानो (लोकल)", "Tomato Small (Local)", "के.जी.", "KG", 35.0, 45.0, 41.0, "vegetables"),
        VegetablePrice("potato_red", "आलु रातो", "Potato Red", "के.जी.", "KG", 55.0, 62.0, 58.0, "vegetables"),
        VegetablePrice("potato_white", "आलु सेतो", "Potato White", "के.जी.", "KG", 45.0, 50.0, 48.0, "vegetables"),
        VegetablePrice("onion_dry", "प्याज सुकेको", "Onion Dry", "के.जी.", "KG", 85.0, 95.0, 90.0, "vegetables"),
        VegetablePrice("cauli_local", "काउली स्थानीय", "Cauliflower Local", "के.जी.", "KG", 70.0, 85.0, 78.0, "vegetables"),
        VegetablePrice("cabbage_local", "बन्दा स्थानीय", "Cabbage Local", "के.जी.", "KG", 30.0, 40.0, 35.0, "vegetables"),
        VegetablePrice("carrot_local", "गाँजर स्थानीय", "Carrot Local", "के.जी.", "KG", 80.0, 95.0, 88.0, "vegetables"),
        VegetablePrice("radish_white", "मूला सेतो (लोकल)", "Radish White", "के.जी.", "KG", 30.0, 40.0, 35.0, "vegetables"),
        VegetablePrice("brinjal_long", "भन्टा लाम्चो", "Brinjal Long", "के.जी.", "KG", 40.0, 50.0, 45.0, "vegetables"),
        VegetablePrice("lady_finger", "भिण्डी", "Lady Finger (Okra)", "के.जी.", "KG", 50.0, 60.0, 55.0, "vegetables"),
        VegetablePrice("bitter_gourd", "तितो करेला", "Bitter Gourd", "के.जी.", "KG", 60.0, 75.0, 68.0, "vegetables"),
        VegetablePrice("bottle_gourd", "लौका", "Bottle Gourd", "के.जी.", "KG", 40.0, 50.0, 45.0, "vegetables"),
        VegetablePrice("green_peas", "मटरकोसा", "Green Peas", "के.जी.", "KG", 140.0, 160.0, 150.0, "vegetables"),
        VegetablePrice("french_beans", "सिमी (लोकल)", "French Beans Local", "के.जी.", "KG", 70.0, 85.0, 78.0, "vegetables"),
        VegetablePrice("cucumber_local", "काँक्रो स्थानीय", "Cucumber Local", "के.जी.", "KG", 50.0, 65.0, 58.0, "vegetables"),
        VegetablePrice("capsicum", "भेडे खुर्सानी", "Capsicum (Bell Pepper)", "के.जी.", "KG", 80.0, 100.0, 90.0, "vegetables"),
        VegetablePrice("spinach", "पालुङ्गो साग", "Spinach (Palungo)", "के.जी.", "KG", 90.0, 110.0, 100.0, "leafy"),
        VegetablePrice("coriander", "धनियाँ हरियो", "Green Coriander", "के.जी.", "KG", 300.0, 500.0, 400.0, "leafy"),
        VegetablePrice("apple_fuji", "स्याउ (फुजी)", "Apple (Fuji)", "के.जी.", "KG", 240.0, 280.0, 260.0, "fruits"),
        VegetablePrice("banana", "केरा (दर्जन)", "Banana (Dozen)", "दर्जन", "Dozen", 120.0, 140.0, 130.0, "fruits"),
        VegetablePrice("pomegranate", "अनार", "Pomegranate", "के.जी.", "KG", 280.0, 320.0, 300.0, "fruits"),
        VegetablePrice("ginger", "अदुवा", "Ginger", "के.जी.", "KG", 200.0, 300.0, 223.0, "spices"),
        VegetablePrice("garlic_dry", "लसुन सुकेको (नेपाली)", "Garlic Dry (Local)", "के.जी.", "KG", 180.0, 200.0, 188.0, "spices"),
        VegetablePrice("green_chilli", "खुर्सानी हरियो", "Green Chilli", "के.जी.", "KG", 50.0, 60.0, 56.0, "spices"),
        VegetablePrice("lemon", "कागती", "Lemon", "के.जी.", "KG", 140.0, 170.0, 155.0, "spices")
    )

    private fun getTodayDateStrings(): Pair<String, String> {
        val now = LocalDate.now()
        val bsDate = runCatching { PatroRepo.d.engine.adToBs(now) }.getOrNull()
        return if (bsDate != null) {
            val dNp = "${NepaliNames.monthsNp[bsDate.month - 1]} ${nepNumStr(bsDate.day)}, ${nepNumStr(bsDate.year)}"
            val dEn = "${bsDate.day} ${NepaliNames.monthsEn[bsDate.month - 1]} ${bsDate.year}"
            Pair(dNp, dEn)
        } else {
            Pair("२०८३ भदौ ०९", "25 August 2026")
        }
    }

    val defaultData: KalimatiData get() {
        val (dNp, dEn) = getTodayDateStrings()
        return KalimatiData(
            dateNp = dNp,
            dateEn = dEn,
            marketNameNp = DEFAULT_MARKET_NP,
            marketNameEn = DEFAULT_MARKET_EN,
            items = defaultItems,
            fetchedAtMillis = 0L,
            isStale = true
        )
    }

    fun loadCached(context: Context): KalimatiData {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return defaultData
        return runCatching {
            val o = JSONObject(f.readText())
            val age = System.currentTimeMillis() - o.optLong("fetchedAt", 0L)
            val (curNp, curEn) = getTodayDateStrings()
            val dateNp = o.optString("dateNp", curNp)
            val dateEn = o.optString("dateEn", curEn)
            val marketNameNp = o.optString("marketNameNp", DEFAULT_MARKET_NP)
            val marketNameEn = o.optString("marketNameEn", DEFAULT_MARKET_EN)
            val itemsArr = o.optJSONArray("items")

            val itemsList = if (itemsArr != null && itemsArr.length() > 0) {
                val list = mutableListOf<VegetablePrice>()
                for (i in 0 until itemsArr.length()) {
                    val r = itemsArr.getJSONObject(i)
                    list.add(
                        VegetablePrice(
                            id = r.getString("id"),
                            nameNp = r.getString("nameNp"),
                            nameEn = r.getString("nameEn"),
                            unitNp = r.optString("unitNp", "के.जी."),
                            unitEn = r.optString("unitEn", "KG"),
                            minPrice = r.getDouble("minPrice"),
                            maxPrice = r.getDouble("maxPrice"),
                            avgPrice = r.getDouble("avgPrice"),
                            category = r.optString("category", "vegetables")
                        )
                    )
                }
                list
            } else defaultItems

            KalimatiData(
                dateNp = dateNp,
                dateEn = dateEn,
                marketNameNp = marketNameNp,
                marketNameEn = marketNameEn,
                items = itemsList,
                fetchedAtMillis = o.optLong("fetchedAt", 0L),
                isStale = age > MAX_AGE_MS
            )
        }.getOrElse { defaultData }
    }

    fun refresh(context: Context, onResult: (KalimatiData) -> Unit) {
        executor.execute {
            val result = fetchAndCache(context) ?: loadCached(context)
            main.post { onResult(result) }
        }
    }

    private fun fetchAndCache(context: Context): KalimatiData? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(LIVE_ENDPOINT_EN).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val pricesArr = root.optJSONArray("prices") ?: return null
            if (pricesArr.length() == 0) return null

            val (dNp, dEn) = getTodayDateStrings()
            val parsedList = mutableListOf<VegetablePrice>()
            val jsonItemsArr = JSONArray()

            for (i in 0 until pricesArr.length()) {
                val item = pricesArr.getJSONObject(i)
                val rawName = item.optString("commodityname", "").trim()
                if (rawName.isEmpty()) continue

                val rawUnit = item.optString("commodityunit", "KG").trim()
                val minP = item.optString("minprice", "0").replace(",", "").toDoubleOrNull() ?: 0.0
                val maxP = item.optString("maxprice", "0").replace(",", "").toDoubleOrNull() ?: 0.0
                val avgP = item.optString("avgprice", "0").replace(",", "").toDoubleOrNull() ?: ((minP + maxP) / 2.0)

                val id = rawName.lowercase().replace(Regex("[^a-z0-9]"), "_").trim('_')
                val nameNp = NEP_NAME_MAP[rawName.lowercase()] ?: rawName
                val unitNp = if (rawUnit.equals("kg", ignoreCase = true)) "के.जी."
                else if (rawUnit.contains("dozen", true) || rawUnit.contains("दर्जन", true)) "दर्जन"
                else rawUnit

                val category = when {
                    rawName.contains("apple", true) || rawName.contains("banana", true) ||
                            rawName.contains("pomegranate", true) || rawName.contains("mango", true) ||
                            rawName.contains("orange", true) || rawName.contains("papaya", true) ||
                            rawName.contains("watermelon", true) || rawName.contains("grapes", true) ||
                            rawName.contains("kiwi", true) || rawName.contains("avocado", true) ||
                            rawName.contains("dragon", true) || rawName.contains("pear", true) -> "fruits"

                    rawName.contains("chilli", true) || rawName.contains("ginger", true) ||
                            rawName.contains("garlic", true) || rawName.contains("lemon", true) ||
                            rawName.contains("turmeric", true) -> "spices"

                    rawName.contains("spinach", true) || rawName.contains("leaf", true) ||
                            rawName.contains("coriander", true) || rawName.contains("methi", true) ||
                            rawName.contains("cress", true) || rawName.contains("mustard green", true) -> "leafy"

                    else -> "vegetables"
                }

                val veg = VegetablePrice(
                    id = id,
                    nameNp = nameNp,
                    nameEn = rawName,
                    unitNp = unitNp,
                    unitEn = rawUnit,
                    minPrice = minP,
                    maxPrice = maxP,
                    avgPrice = avgP,
                    category = category
                )
                parsedList.add(veg)

                val jObj = JSONObject().apply {
                    put("id", veg.id)
                    put("nameNp", veg.nameNp)
                    put("nameEn", veg.nameEn)
                    put("unitNp", veg.unitNp)
                    put("unitEn", veg.unitEn)
                    put("minPrice", veg.minPrice)
                    put("maxPrice", veg.maxPrice)
                    put("avgPrice", veg.avgPrice)
                    put("category", veg.category)
                }
                jsonItemsArr.put(jObj)
            }

            if (parsedList.isEmpty()) return null

            val now = System.currentTimeMillis()
            val cacheObj = JSONObject().apply {
                put("fetchedAt", now)
                put("dateNp", dNp)
                put("dateEn", dEn)
                put("marketNameNp", DEFAULT_MARKET_NP)
                put("marketNameEn", DEFAULT_MARKET_EN)
                put("items", jsonItemsArr)
            }

            File(context.filesDir, FILE).writeText(cacheObj.toString())
            loadCached(context)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            conn?.disconnect()
        }
    }
}
