package com.neptools.app.astrology.data

import java.time.LocalDate
import java.time.LocalTime

data class BirthData(
    val date: LocalDate,
    val time: LocalTime,
    val latitude: Double,
    val longitude: Double,
    val tzOffsetHours: Double,
    val placeLabel: String,
    val birthTimeUncertain: Boolean = false
) {
    fun validationError(): String? {
        if (latitude < -90 || latitude > 90) return "Latitude must be -90..90"
        if (longitude < -180 || longitude > 180) return "Longitude must be -180..180"
        if (tzOffsetHours < -12 || tzOffsetHours > 14) return "Timezone offset invalid"
        if (date.year !in 1800..2050) return "Year must be 1800..2050 for ephemeris validity"
        if (!birthTimeUncertain && time == LocalTime.MIDNIGHT && date == LocalDate.MIN) return "Invalid time"
        return null
    }
}

enum class Planet {
    SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN, RAHU, KETU;

    companion object {
        val SEVEN = listOf(SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN)
        val NINE = entries.toList()
    }
}

data class PlanetPosition(
    val planet: Planet,
    val tropicalLon: Double,
    val siderealLon: Double,
    val speedPerDay: Double,
    val retrograde: Boolean,
    val signIndex: Int,
    val degreeInSign: Double,
    val nakshatraName: String,
    val nakshatraPada: Int,
    val houseFromLagna: Int,
    val houseFromMoon: Int,
    val bhavaFromLagna: Int = houseFromLagna
)

data class HouseInfo(val index: Int, val rashiIndex: Int, val lord: Planet)

data class NatalChart(
    val birth: BirthData,
    val julianDayUt: Double,
    val ayanamsa: Double,
    val lagnaSiderealLon: Double,
    val lagnaSign: Int,
    val houses: List<HouseInfo>,
    val positions: Map<Planet, PlanetPosition>,
    val moonNakshatra: String,
    val engineTag: String,
    val sripatiCusps: List<Double> = emptyList()
) {
    fun planetInHouse(p: Planet): Int = positions.getValue(p).houseFromLagna
    fun planetInBhava(p: Planet): Int = positions.getValue(p).bhavaFromLagna
}

data class DashaPeriod(
    val lord: Planet,
    val start: LocalDate,
    val end: LocalDate,
    val level: Int,
    val children: List<DashaPeriod> = emptyList()
)

data class TransitInfo(
    val planet: Planet,
    val siderealLon: Double,
    val signIndex: Int,
    val degreeInSign: Double,
    val retrograde: Boolean,
    val natalHouseFromMoon: Int,
    val favorable: Boolean,
    val note: String,
    val noteEn: String = ""
)

data class YogaFinding(val name: String, val nameEn: String, val present: Boolean, val detail: String)

data class ScoreFactor(val text: String, val positive: Boolean, val weight: Int)

data class CategoryScore(
    val key: String,
    val labelNp: String,
    val score: Int,
    val positives: List<ScoreFactor>,
    val challenges: List<ScoreFactor>
)

data class MonthRating(val month: LocalDate, val ratings: Map<String, String>)

data class AstrologyResult(
    val chart: NatalChart,
    val dashaTreeRoots: List<DashaPeriod>,
    val currentMaha: DashaPeriod?,
    val currentAntar: DashaPeriod?,
    val currentPratyantar: DashaPeriod?,
    val transits: List<TransitInfo>,
    val yogas: List<YogaFinding>,
    val scores: List<CategoryScore>,
    val todaySummary: List<String>,
    val timeline: List<MonthRating>,
    val computedAtMillis: Long
)
