package com.neptools.app.astrology.vedic

import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.PlanetPosition

object DrishtiCalculator {

    fun aspects(from: Planet): List<Int> = when (from) {
        Planet.SUN, Planet.MOON -> listOf(7)
        Planet.MARS -> listOf(4, 7, 8)
        Planet.MERCURY, Planet.VENUS -> listOf(7)
        Planet.JUPITER -> listOf(5, 7, 9)
        Planet.SATURN -> listOf(3, 7, 10)
        Planet.RAHU, Planet.KETU -> listOf(5, 7, 9)
    }

    fun aspectsOn(
        target: Planet,
        positions: Map<Planet, PlanetPosition>
    ): List<Planet> {
        val targetHouse = positions.getValue(target).houseFromLagna
        return Planet.NINE.filter { source ->
            if (source == target) return@filter false
            val srcHouse = positions.getValue(source).houseFromLagna
            aspects(source).any { a -> ((srcHouse + a - 2) % 12) + 1 == targetHouse }
        }
    }
}

class StrengthCalculator {

    private val exaltation = mapOf(
        Planet.SUN to 0, Planet.MOON to 1, Planet.MARS to 9,
        Planet.MERCURY to 5, Planet.JUPITER to 3, Planet.VENUS to 11, Planet.SATURN to 6
    )
    private val debilitation = mapOf(
        Planet.SUN to 6, Planet.MOON to 7, Planet.MARS to 3,
        Planet.MERCURY to 11, Planet.JUPITER to 9, Planet.VENUS to 5, Planet.SATURN to 0
    )
    private val ownSigns = mapOf(
        Planet.SUN to setOf(4), Planet.MOON to setOf(3),
        Planet.MARS to setOf(0, 7), Planet.MERCURY to setOf(2, 5),
        Planet.JUPITER to setOf(8, 11), Planet.VENUS to setOf(1, 6),
        Planet.SATURN to setOf(9, 10)
    )
    private val friends = mapOf(
        Planet.SUN to setOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
        Planet.MOON to setOf(Planet.SUN, Planet.MERCURY),
        Planet.MARS to setOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
        Planet.MERCURY to setOf(Planet.SUN, Planet.VENUS),
        Planet.JUPITER to setOf(Planet.SUN, Planet.MOON, Planet.MARS),
        Planet.VENUS to setOf(Planet.MERCURY, Planet.SATURN),
        Planet.SATURN to setOf(Planet.MERCURY, Planet.VENUS)
    )
    private val signLords = mapOf(
        0 to Planet.MARS, 1 to Planet.VENUS, 2 to Planet.MERCURY, 3 to Planet.MOON,
        4 to Planet.SUN, 5 to Planet.MERCURY, 6 to Planet.VENUS, 7 to Planet.MARS,
        8 to Planet.JUPITER, 9 to Planet.SATURN, 10 to Planet.SATURN, 11 to Planet.JUPITER
    )
    private val digBalaHouse = mapOf(
        Planet.JUPITER to 1, Planet.MERCURY to 1,
        Planet.SUN to 10, Planet.MARS to 10,
        Planet.SATURN to 7, Planet.MOON to 4, Planet.VENUS to 4
    )
    private val combustionOrb = mapOf(
        Planet.MOON to 12.0, Planet.MARS to 17.0, Planet.MERCURY to 14.0,
        Planet.JUPITER to 11.0, Planet.VENUS to 10.0, Planet.SATURN to 15.0
    )

    data class StrengthResult(val score: Int, val notes: List<String>)

    fun evaluate(p: Planet, chart: com.neptools.app.astrology.data.NatalChart): StrengthResult {
        if (p in setOf(Planet.RAHU, Planet.KETU)) {
            return StrengthResult(50, listOf("छाया ग्रह — शुद्ध शक्ति लागू हुँदैन"))
        }
        val pos = chart.positions.getValue(p)
        val notes = mutableListOf<String>()
        var score = 50

        when (p) {
            Planet.SUN, Planet.MOON, Planet.MARS, Planet.MERCURY,
            Planet.JUPITER, Planet.VENUS, Planet.SATURN -> {
                if (pos.signIndex == exaltation[p]) { score += 18; notes.add("उच्च राशिमा") }
                if (pos.signIndex == debilitation[p]) { score -= 18; notes.add("नीच राशिमा") }
                if (pos.signIndex in ownSigns.getValue(p)) { score += 12; notes.add("स्वराशि") }
                val dispositor = signLords.getValue(pos.signIndex)
                if (dispositor != p && friends.getValue(p).contains(dispositor)) {
                    score += 5; notes.add("मित्र राशि")
                } else if (dispositor != p) {
                    score -= 4; notes.add("शत्रु/अलग राशि")
                }
            }
            else -> {}
        }

        digBalaHouse[p]?.let { best ->
            val h = pos.houseFromLagna
            if (h == best) { score += 8; notes.add("दिग्बल (भाव $best)") }
            else if ((h - best + 12) % 12 <= 1 || (best - h + 12) % 12 <= 1) score += 3
        }

        val benefics = setOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY)
        val malefics = setOf(Planet.SATURN, Planet.MARS, Planet.RAHU, Planet.KETU, Planet.SUN)
        DrishtiCalculator.aspectsOn(p, chart.positions).forEach { src ->
            if (src in benefics) { score += 5; notes.add("${planetNameNp(src)}को शुभ दृष्टि") }
            if (src in malefics && p != Planet.SUN) { score -= 4; notes.add("${planetNameNp(src)}को पाप दृष्टि") }
        }

        combustionOrb[p]?.let { orb ->
            val sunLon = chart.positions.getValue(Planet.SUN).siderealLon
            var diff = kotlin.math.abs(pos.siderealLon - sunLon)
            if (diff > 180) diff = 360 - diff
            if (diff < orb) { score -= 8; notes.add("अस्त (${"%.1f".format(diff)}° सूर्यबाट)") }
        }

        if (pos.retrograde && p !in setOf(Planet.SUN, Planet.MOON)) {
            score += 4; notes.add("वक्री (चेष्टा बल)")
        }

        return StrengthResult(score.coerceIn(5, 98), notes)
    }

    private fun planetNameNp(p: Planet): String = when (p) {
        Planet.SUN -> "सूर्य"
        Planet.MOON -> "चन्द्र"
        Planet.MARS -> "मंगल"
        Planet.MERCURY -> "बुध"
        Planet.JUPITER -> "बृहस्पति"
        Planet.VENUS -> "शुक्र"
        Planet.SATURN -> "शनि"
        Planet.RAHU -> "राहु"
        Planet.KETU -> "केतु"
    }
}
