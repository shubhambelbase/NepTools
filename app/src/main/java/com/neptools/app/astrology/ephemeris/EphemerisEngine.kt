package com.neptools.app.astrology.ephemeris

import com.neptools.app.astrology.data.Planet
import java.time.LocalDate
import java.time.LocalTime

enum class AyanamsaType { LAHIRI }

interface EphemerisEngine {
    val tag: String

    fun julianDay(date: LocalDate, time: LocalTime, tzOffsetHours: Double): Double

    fun ayanamsa(jdUt: Double, type: AyanamsaType): Double

    fun tropicalLongitudes(jdUt: Double): Map<Planet, Pair<Double, Double>>

    fun ascendantTropical(jdUt: Double, latitude: Double, longitude: Double): Double
}

object AstroAssumptions {
    val NOTES = listOf(
        "Ephemeris: Meeus low-precision Sun (±0.01 deg), ELP-2000/82 truncated Moon (top terms, ±0.02 deg typical), Keplerian JPL approximate elements 1800-2050 for Mercury-Saturn (arc-minute class).",
        "Rahu = mean lunar ascending node (Meeus 47.7); true-node periodic corrections <0.05 deg ignored. Ketu = Rahu + 180.",
        "Ayanamsa: Lahiri anchored at 23deg51'11\" at J2000.0 with uniform general precession 50.2564 arcsec/yr; deviation from official polynomial <0.01 deg for 1900-2050.",
        "Houses: whole-sign (Rashi) system from Lagna sign; Bhava-Chalit not applied.",
        "Drishti: full Graha aspects only (Sun/Moon 7; Mars 4,7,8; Jupiter 5,7,9; Saturn 3,7,10; Rahu/Ketu 5,7,9).",
        "Strength: simplified Shadbala proxy - dignity, dig bala, benefic/malefic aspects, combustion orb, retrograde chesta marker.",
        "Vimshottari year = 365.25 days; spans Ketu 7, Venus 20, Sun 6, Moon 10, Mars 7, Rahu 18, Jupiter 16, Saturn 19, Mercury 17.",
        "Transit favorability: classic Chandra-Lagna Gochar tables; natal conjunction orb 8 deg sidereal.",
        "Scores are deterministic application-defined analytics derived from listed factors - not scientifically validated predictions."
    )
}
