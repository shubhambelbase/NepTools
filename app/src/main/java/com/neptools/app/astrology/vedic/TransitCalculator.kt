package com.neptools.app.astrology.vedic

import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.TransitInfo

class TransitCalculator(private val engine: com.neptools.app.astrology.ephemeris.EphemerisEngine) {

    private val favorableFromMoon = mapOf(
        Planet.SUN to setOf(3, 6, 10, 11),
        Planet.MOON to setOf(1, 3, 6, 7, 10, 11),
        Planet.MARS to setOf(3, 6, 11),
        Planet.MERCURY to setOf(2, 4, 6, 8, 10, 11),
        Planet.JUPITER to setOf(2, 5, 7, 9, 11),
        Planet.VENUS to setOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
        Planet.SATURN to setOf(3, 6, 11),
        Planet.RAHU to setOf(3, 6, 10, 11),
        Planet.KETU to setOf(3, 6, 11)
    )

    fun currentTransits(
        chart: NatalChart,
        now: java.time.LocalDateTime,
        tzOffsetHours: Double = 5.75
    ): List<TransitInfo> {
        val jd = engine.julianDay(now.toLocalDate(), now.toLocalTime(), tzOffsetHours)
        val ay = engine.ayanamsa(jd, com.neptools.app.astrology.ephemeris.AyanamsaType.LAHIRI)
        val tropical = engine.tropicalLongitudes(jd)
        val moonSign = chart.positions.getValue(Planet.MOON).signIndex

        return Planet.NINE.map { p ->
            val (trop, speed) = tropical.getValue(p)
            val sid = SiderealCalculator.normalize(trop - ay)
            val sign = (sid / 30.0).toInt()
            val house = HouseCalc.houseFromMoon(sign, moonSign)
            val fav = favorableFromMoon.getValue(p).contains(house)
            TransitInfo(
                planet = p,
                siderealLon = sid,
                signIndex = sign,
                degreeInSign = sid % 30.0,
                retrograde = speed < 0 && p !in setOf(Planet.SUN, Planet.MOON),
                natalHouseFromMoon = house,
                favorable = fav,
                note = transitNote(p, fav),
                noteEn = transitNoteEn(p, fav)
            )
        }
    }

    fun natalConjunctions(
        transits: List<TransitInfo>,
        chart: NatalChart,
        orbDeg: Double = 8.0
    ): Map<Planet, Planet> {
        val out = HashMap<Planet, Planet>()
        for (t in transits) {
            if (t.planet in setOf(Planet.RAHU, Planet.KETU)) continue
            var closest: Planet? = null
            var bestDiff = Double.MAX_VALUE
            for ((np, pos) in chart.positions) {
                if (np in setOf(Planet.RAHU, Planet.KETU)) continue
                var diff = kotlin.math.abs(t.siderealLon - pos.siderealLon)
                if (diff > 180) diff = 360 - diff
                if (diff < bestDiff) { bestDiff = diff; closest = np }
            }
            if (closest != null && bestDiff <= orbDeg) out[t.planet] = closest
        }
        return out
    }

    private fun transitNote(p: Planet, fav: Boolean): String = when (p) {
        Planet.JUPITER -> if (fav) "विस्तार/सुरक्षा अनुकूल" else "अवसर ढिलो — धैर्य"
        Planet.SATURN -> if (fav) "अनुशासनले फल" else "दबाब/जिम्मेवारी बढी — स्वास्थ्य ख्याल"
        Planet.MARS -> if (fav) "ऊर्जा/पहल राम्रो" else "जल्दबन्दी/विवाद जोखिम"
        Planet.SUN -> if (fav) "आत्मविश्वास/पहिचान बलियो" else "ऊर्जा फैलिने — प्राथमिकता तोक्ने"
        Planet.MOON -> if (fav) "मन शान्त/समर्थन" else "भावनात्मक उतारचढाव"
        Planet.MERCURY -> if (fav) "सञ्चार/लेनदेन अनुकूल" else "गलतफहमी/कागजात सावधानी"
        Planet.VENUS -> if (fav) "सम्बन्ध/आनन्द अनुकूल" else "खर्च/सम्बन्धमा संयम"
        Planet.RAHU -> if (fav) "महत्वाकांक्षा बढ्दो" else "भ्रम/छलोकता सावधानी"
        else -> if (fav) "समापन/छोड्ने अनुकूल" else "पुरानो झुकाव सावधानी"
    }

    private fun transitNoteEn(p: Planet, fav: Boolean): String = when (p) {
        Planet.JUPITER -> if (fav) "Expansion & divine protection favorable" else "Opportunities delayed — practice patience"
        Planet.SATURN -> if (fav) "Hard work and discipline yield rewards" else "Heavy duty and pressure — prioritize health"
        Planet.MARS -> if (fav) "High courage, energy and initiative" else "Risk of rash decisions and friction"
        Planet.SUN -> if (fav) "Strong self-confidence and recognition" else "Scattered energy — prioritize essentials"
        Planet.MOON -> if (fav) "Peaceful mind and emotional support" else "Emotional fluctuations — practice mindfulness"
        Planet.MERCURY -> if (fav) "Smooth communication and commerce" else "Double check documents and avoid misunderstandings"
        Planet.VENUS -> if (fav) "Harmony in relationships and creative joy" else "Exercise moderation in spending and desires"
        Planet.RAHU -> if (fav) "Rising ambition and unconventional gains" else "Beware of illusion and overconfidence"
        else -> if (fav) "Spiritual release and letting go favorable" else "Caution against past detachment patterns"
    }
}
