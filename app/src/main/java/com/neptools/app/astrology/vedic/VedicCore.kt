package com.neptools.app.astrology.vedic

import com.neptools.app.astrology.data.BirthData
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.ephemeris.AyanamsaType
import com.neptools.app.astrology.ephemeris.EphemerisEngine
import kotlin.math.floor

object Signs {
    val np = listOf(
        "मेष", "वृष", "मिथुन", "कर्कट", "सिंह", "कन्या",
        "तुला", "वृश्चिक", "धनु", "मकर", "कुम्भ", "मीन"
    )
    val en = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )
}

object NakshatraCalc {
    val names = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु",
        "पुष्य", "अश्लेषा", "मघा", "पूर्व फाल्गुनी", "उत्तर फाल्गुनी", "हस्त",
        "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा", "मूल",
        "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा",
        "पूर्व भाद्रपद", "उत्तर भाद्रपद", "रेवती"
    )
    val namesEn = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu",
        "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
        "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula",
        "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )
    private val lords = listOf(
        Planet.KETU, Planet.VENUS, Planet.SUN, Planet.MOON, Planet.MARS,
        Planet.RAHU, Planet.JUPITER, Planet.SATURN, Planet.MERCURY
    )

    fun spanDeg() = 360.0 / 27.0

    fun index(lonSidereal: Double): Int =
        floor(normalizedLon(lonSidereal) / spanDeg()).toInt().coerceIn(0, 26)

    fun pada(lonSidereal: Double): Int {
        val withinSpan = normalizedLon(lonSidereal) % spanDeg()
        return (floor(withinSpan / (spanDeg() / 4.0)).toInt() + 1).coerceIn(1, 4)
    }

    fun lord(nakshatraIndex: Int): Planet = lords[nakshatraIndex % 9]

    fun normalizedLon(lon: Double): Double {
        var r = lon % 360.0
        if (r < 0) r += 360.0
        return r
    }
}

class SiderealCalculator(private val engine: EphemerisEngine) {

    data class SiderealPosition(val planet: Planet, val lon: Double, val speed: Double)

    fun all(birth: BirthData): Pair<Double, List<SiderealPosition>> {
        val jdUt = engine.julianDay(birth.date, birth.time, birth.tzOffsetHours)
        val ay = engine.ayanamsa(jdUt, AyanamsaType.LAHIRI)
        val tropical = engine.tropicalLongitudes(jdUt)
        val positions = Planet.NINE.map { p ->
            val (lon, speed) = tropical.getValue(p)
            SiderealPosition(p, normalize(SiderealCalculatorHelper.sub(lon, ay)), speed)
        }
        return Pair(ay, positions)
    }

    fun ascendant(birth: BirthData): Double {
        val jdUt = engine.julianDay(birth.date, birth.time, birth.tzOffsetHours)
        val ay = engine.ayanamsa(jdUt, AyanamsaType.LAHIRI)
        return normalize(engine.ascendantTropical(jdUt, birth.latitude, birth.longitude) - ay)
    }

    companion object {
        fun normalize(v: Double): Double {
            var r = v % 360.0
            if (r < 0) r += 360.0
            return r
        }
    }
}

private object SiderealCalculatorHelper {
    fun sub(a: Double, b: Double): Double = a - b
}

class LagnaCalculator(private val sidereal: SiderealCalculator) {
    fun compute(birth: BirthData): Pair<Double, Int> {
        val lon = sidereal.ascendant(birth)
        return Pair(lon, floor(lon / 30.0).toInt())
    }
}

class HouseCalculator {
    fun houses(lagnaSign: Int): List<com.neptools.app.astrology.data.HouseInfo> {
        val lords = mapOf(
            0 to Planet.MARS, 1 to Planet.VENUS, 2 to Planet.MERCURY, 3 to Planet.MOON,
            4 to Planet.SUN, 5 to Planet.MERCURY, 6 to Planet.VENUS, 7 to Planet.MARS,
            8 to Planet.JUPITER, 9 to Planet.SATURN, 10 to Planet.SATURN, 11 to Planet.JUPITER
        )
        return (1..12).map { h ->
            val sign = (lagnaSign + h - 1) % 12
            com.neptools.app.astrology.data.HouseInfo(h, sign, lords.getValue(sign))
        }
    }

    fun houseOf(planetSign: Int, lagnaSign: Int): Int =
        ((planetSign - lagnaSign + 12) % 12) + 1

    /**
     * Authentic Sripati Bhava Chalit house cusps (Bhava Madhyas) and boundaries (Sandhis)
     */
    fun sripatiCusps(lagnaLon: Double, mcLon: Double): List<Double> {
        val cusps = DoubleArray(12)
        cusps[0] = lagnaLon
        cusps[9] = mcLon
        cusps[6] = (lagnaLon + 180.0) % 360.0
        cusps[3] = (mcLon + 180.0) % 360.0

        val arc1 = (lagnaLon - mcLon + 360.0) % 360.0
        val int1 = arc1 / 3.0
        cusps[10] = (mcLon + int1) % 360.0
        cusps[11] = (mcLon + 2.0 * int1) % 360.0

        val ic = cusps[3]
        val arc2 = (ic - lagnaLon + 360.0) % 360.0
        val int2 = arc2 / 3.0
        cusps[1] = (lagnaLon + int2) % 360.0
        cusps[2] = (lagnaLon + 2.0 * int2) % 360.0

        cusps[4] = (cusps[10] + 180.0) % 360.0
        cusps[5] = (cusps[11] + 180.0) % 360.0
        cusps[7] = (cusps[1] + 180.0) % 360.0
        cusps[8] = (cusps[2] + 180.0) % 360.0

        return cusps.toList()
    }

    fun bhavaOf(planetLon: Double, cusps: List<Double>): Int {
        if (cusps.size < 12) return ((floor(planetLon / 30.0).toInt()) % 12) + 1
        val sandhis = DoubleArray(12)
        for (i in 0 until 12) {
            val prevCusp = if (i == 0) cusps[11] else cusps[i - 1]
            val currCusp = cusps[i]
            val diff = (currCusp - prevCusp + 360.0) % 360.0
            sandhis[i] = (prevCusp + diff / 2.0) % 360.0
        }
        for (i in 0 until 12) {
            val start = sandhis[i]
            val end = sandhis[(i + 1) % 12]
            if (isAngleBetween(planetLon, start, end)) {
                return i + 1
            }
        }
        return 1
    }

    private fun isAngleBetween(target: Double, start: Double, end: Double): Boolean {
        val sweep = (end - start + 360.0) % 360.0
        val dist = (target - start + 360.0) % 360.0
        return dist < sweep
    }
}
