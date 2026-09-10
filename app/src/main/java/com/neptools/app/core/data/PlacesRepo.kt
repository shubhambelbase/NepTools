package com.neptools.app.core.data

import android.content.Context
import org.json.JSONObject

data class Place(
    val en: String,
    val np: String,
    val lat: Double,
    val lon: Double,
    val district: String,
    val province: String,
    val type: String
)

object PlacesRepo {

    @Volatile
    private var places: List<Place>? = null
    @Volatile
    private var appCtx: Context? = null
    const val NEPAL_TZ = 5.75

    fun init(context: Context) {
        appCtx = context.applicationContext
        Thread {
            ensure()
        }.start()
    }

    private fun ensure() {
        if (places != null) return
        synchronized(this) {
            if (places != null) return
            val raw = (appCtx ?: return).assets.open("nepal_places.json").bufferedReader().use { it.readText() }
            val root = JSONObject(raw)
            val arr = root.getJSONArray("places")
            val list = ArrayList<Place>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Place(
                        en = o.getString("en"),
                        np = o.optString("np", ""),
                        lat = o.getDouble("lat"),
                        lon = o.getDouble("lon"),
                        district = o.optString("district", ""),
                        province = o.optString("province", ""),
                        type = o.optString("type", "")
                    )
                )
            }
            places = list.sortedBy { it.en.lowercase() }
        }
    }

    fun all(): List<Place> {
        if (places == null) ensure()
        return places ?: emptyList()
    }

    fun search(query: String, limit: Int = 40): List<Place> {
        if (places == null) ensure()
        val all = places ?: return emptyList()
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            val metros = listOf(
                "Kathmandu", "Pokhara", "Biratnagar", "Janakpur", "Lalitpur",
                "Bharatpur", "Birgunj", "Dharan", "Butwal", "Nepalgunj"
            )
            return all.filter { p -> p.type == "local" && metros.any { p.en.contains(it, ignoreCase = true) } }
                .take(limit)
        }
        val startsEn = ArrayList<Place>()
        val containsEn = ArrayList<Place>()
        val containsNp = ArrayList<Place>()
        val containsDistrict = ArrayList<Place>()
        for (p in all) {
            val enLower = p.en.lowercase()
            if (enLower.startsWith(q)) startsEn.add(p)
            else if (enLower.contains(q)) containsEn.add(p)
            else if (p.np.contains(query.trim())) containsNp.add(p)
            else if (p.district.lowercase().contains(q)) containsDistrict.add(p)
        }
        return (startsEn + containsEn + containsNp + containsDistrict).distinctBy { it.en to it.district }.take(limit)
    }
}
