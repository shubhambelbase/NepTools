package com.neptools.app.astrology.data

import android.content.Context
import com.neptools.app.astrology.ephemeris.EphemerisEngine
import com.neptools.app.astrology.vedic.VedicAstrologyEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

object AstroRepo {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _birth = MutableStateFlow<BirthData?>(null)
    val birth: StateFlow<BirthData?> = _birth

    private val _result = MutableStateFlow<AstrologyResult?>(null)
    val result: StateFlow<AstrologyResult?> = _result

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private lateinit var engine: VedicAstrologyEngine
    private var lastKey: Int = 0

    fun init(context: Context, ephemeris: EphemerisEngine) {
        if (::engine.isInitialized) return
        engine = VedicAstrologyEngine(ephemeris)
        val prefs = context.getSharedPreferences("astro_prefs", Context.MODE_PRIVATE)
        if (prefs.contains("bd_json")) {
            runCatching { parseBirth(prefs.getString("bd_json", null)!!) }.getOrNull()?.let {
                _birth.value = it
                recompute()
            }
        }
    }

    fun saveBirth(context: Context, birth: BirthData) {
        val err = birth.validationError()
        require(err == null) { err ?: "Invalid" }
        context.getSharedPreferences("astro_prefs", Context.MODE_PRIVATE)
            .edit().putString("bd_json", toJson(birth)).apply()
        _birth.value = birth
        recompute()
    }

    private fun recompute() {
        val b = _birth.value ?: return
        _busy.value = true
        scope.launch {
            val res = engine.fullResult(b)
            lastKey = b.hashCode()
            _result.value = res
            _busy.value = false
        }
    }

    fun transitsFor(now: java.time.LocalDateTime, tzOffsetHours: Double = 5.75): Pair<List<TransitInfo>, Map<Planet, Planet>>? {
        val chart = _result.value?.chart ?: return null
        if (!::engine.isInitialized) return null
        val list = engine.currentTransits(chart, now, tzOffsetHours)
        val conj = engine.natalConjunctions(list, chart)
        return Pair(list, conj)
    }

    fun toJson(b: BirthData): String =
        listOf(
            b.date.year, b.date.monthValue, b.date.dayOfMonth,
            b.time.hour, b.time.minute,
            b.latitude, b.longitude, b.tzOffsetHours,
            b.birthTimeUncertain,
            b.placeLabel.replace("|", "/")
        ).joinToString("|")

    fun parseBirth(s: String): BirthData {
        val p = s.split("|")
        return BirthData(
            date = LocalDate.of(p[0].toInt(), p[1].toInt(), p[2].toInt()),
            time = LocalTime.of(p[3].toInt(), p[4].toInt()),
            latitude = p[5].toDouble(),
            longitude = p[6].toDouble(),
            tzOffsetHours = p[7].toDouble(),
            placeLabel = p[9],
            birthTimeUncertain = p[8].toBoolean()
        )
    }
}
