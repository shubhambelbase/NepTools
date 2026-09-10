package com.neptools.app.astrology.ephemeris

import com.neptools.app.astrology.data.Planet
import java.time.LocalDate
import java.time.LocalTime

class MeeusEphemeris(
    private val precise: HighPrecisionEphemeris = HighPrecisionEphemeris(useTrueNode = true)
) : EphemerisEngine by precise {

    override val tag = "VSOP87+NOVASs+ELP2000-v2"

    fun daysSinceJ2000(jd: Double) = jd - 2451545.0

    fun ayanamsaDegrees(jd: Double): Double = precise.ayanamsa(jd, AyanamsaType.LAHIRI)

    fun greenwichMeanSiderealTimeHours(jd: Double): Double = precise.greenwichSiderealTimeHours(jd)

    fun ascendantTropicalDeg(jd: Double, latitude: Double, longitude: Double): Double =
        precise.ascendantTropical(jd, latitude, longitude)

    fun midheavenTropicalDeg(jd: Double, longitude: Double): Double =
        precise.midheavenTropical(jd, longitude)

    fun meanNode(jd: Double): Double = precise.meanNode(jd)

    fun trueNode(jd: Double): Double = precise.trueNode(jd)

    companion object {
        const val J2000 = HighPrecisionEphemeris.J2000
        const val LAHIRI_J2000_DEG = HighPrecisionEphemeris.LAHIRI_J2000_DEG
        const val PRECESSION_ARCSEC_PER_YEAR = 50.2882

        fun normalizeDeg(deg: Double): Double = HighPrecisionEphemeris.normalizeDeg(deg)
    }
}
