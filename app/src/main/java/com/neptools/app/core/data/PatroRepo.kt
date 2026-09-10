package com.neptools.app.core.data

import android.content.Context
import com.neptools.app.core.calendar.BsCalendarEngine
import com.neptools.app.core.calendar.Festival
import org.json.JSONObject
import java.time.LocalDate

import com.neptools.app.core.calendar.DynamicFestivalEngine
import java.util.concurrent.ConcurrentHashMap

data class PatroDataset(
    val engine: BsCalendarEngine,
    val festivals: Map<String, Map<Int, List<Festival>>>
) {
    private val dynamicCache = ConcurrentHashMap<String, Map<Int, List<Festival>>>()

    fun festivalsFor(bsYear: Int, bsMonth: Int): Map<Int, List<Festival>> {
        val key = "$bsYear-$bsMonth"
        val staticMap = festivals[key]
        if (staticMap != null && staticMap.isNotEmpty()) {
            return staticMap
        }

        return dynamicCache.getOrPut(key) {
            DynamicFestivalEngine.computeForMonth(engine, bsYear, bsMonth)
        }
    }
}

object PatroRepo {

    @Volatile
    private var cached: PatroDataset? = null

    @Volatile
    private var appCtx: Context? = null

    fun init(context: Context) {
        appCtx = context.applicationContext
    }

    val d: PatroDataset
        get() {
            cached?.let { return it }
            return synchronized(this) {
                cached ?: load(appCtx ?: throw IllegalStateException("PatroRepo.init required")).also { cached = it }
            }
        }

    fun festivalsFor(bsYear: Int, bsMonth: Int): Map<Int, List<Festival>> =
        d.festivalsFor(bsYear, bsMonth)

    private fun load(context: Context): PatroDataset {
        val json = context.assets.open("bs_calendar.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val anchor = root.getJSONObject("anchor")
        val yearsJson = root.getJSONObject("years")
        val map = HashMap<Int, IntArray>()
        val keys = yearsJson.keys()
        while (keys.hasNext()) {
            val yearStr = keys.next()
            val arr = yearsJson.getJSONArray(yearStr)
            map[yearStr.toInt()] = IntArray(arr.length()) { arr.getInt(it) }
        }
        val engine = BsCalendarEngine(
            firstYear = root.getInt("firstYear"),
            lastYear = root.getInt("lastYear"),
            monthLengthsByYear = map,
            anchorAd = LocalDate.parse(anchor.getString("adDate")),
            anchorWeekdayIndex = anchor.getInt("weekdayIndex")
        )
        val rawFest = loadFestivals(context)
        val validated = FestivalValidator.validate(rawFest, engine)
        if (validated.warnings.isNotEmpty()) {
            android.util.Log.w("PatroRepo", "Festival validation warnings: ${validated.warnings.take(5)} dropped=${validated.droppedOutOfRange} dedup=${validated.deduplicated}")
        }
        return PatroDataset(engine, validated.valid)
    }

    private fun loadFestivals(context: Context): Map<String, Map<Int, List<Festival>>> {
        val raw = try {
            context.assets.open("festivals_sample.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            return emptyMap()
        }
        val root = JSONObject(raw)
        val temp = HashMap<String, Map<Int, List<Festival>>>()
        val monthKeys = root.keys()
        while (monthKeys.hasNext()) {
            val key = monthKeys.next()
            val arr = root.optJSONArray(key) ?: continue
            val byDay = HashMap<Int, MutableList<Festival>>()
            for (i in 0 until arr.length()) {
                val f = arr.getJSONObject(i)
                byDay.getOrPut(f.getInt("day")) { mutableListOf() }.add(
                    Festival(
                        day = f.getInt("day"),
                        nameNp = f.getString("nameNp"),
                        nameEn = f.optString("nameEn", ""),
                        isPublicHoliday = f.optBoolean("publicHoliday", false)
                    )
                )
            }
            temp[key] = byDay
        }
        // Validate against engine if available; engine must be built before validation
        return temp
    }

    fun validatedFestivals(raw: Map<String, Map<Int, List<Festival>>>, engine: BsCalendarEngine): Map<String, Map<Int, List<Festival>>> {
        val result = FestivalValidator.validate(raw, engine)
        if (result.warnings.isNotEmpty()) {
            android.util.Log.w("PatroRepo", "Festival validation: dropped=${result.droppedOutOfRange} deduped=${result.deduplicated} warnings=${result.warnings.take(5)}")
        }
        return result.valid
    }
}
