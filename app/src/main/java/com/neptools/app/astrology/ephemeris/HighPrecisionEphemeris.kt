package com.neptools.app.astrology.ephemeris

import com.neptools.app.astrology.data.Planet
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.datan2
import io.github.cosinekitty.astronomy.degreesToRadians
import io.github.cosinekitty.astronomy.earthTilt
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.siderealTime
import io.github.cosinekitty.astronomy.sunPosition
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

class HighPrecisionEphemeris(
    val useTrueNode: Boolean = true
) : EphemerisEngine {

    override val tag = "VSOP87+NOVAS+ELP2000-v2"

    override fun julianDay(date: LocalDate, time: LocalTime, tzOffsetHours: Double): Double {
        var y = date.year
        var m = date.monthValue
        val dayFrac = (time.hour + time.minute / 60.0 + time.second / 3600.0 - tzOffsetHours) / 24.0
        if (m <= 2) { y -= 1; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716))+floor(30.6001 * (m + 1)) +
            date.dayOfMonth + b - 1524.5 + dayFrac
    }

    private fun toAstronomyTime(jdUt: Double): Time {
        return Time(jdUt - 2451545.0)
    }

    override fun ayanamsa(jdUt: Double, type: AyanamsaType): Double {
        require(type == AyanamsaType.LAHIRI) { "Only LAHIRI implemented" }
        val t = (jdUt - 2451545.0) / 36525.0
        val precArcSec = 5028.796195 * t + 1.1054348 * t * t + 0.00007964 * t * t * t
        return normalizeDeg(LAHIRI_J2000_DEG + precArcSec / 3600.0)
    }

    fun greenwichSiderealTimeHours(jdUt: Double): Double {
        val t = toAstronomyTime(jdUt)
        return siderealTime(t)
    }

    override fun ascendantTropical(jdUt: Double, latitude: Double, longitude: Double): Double {
        val t = toAstronomyTime(jdUt)
        val gstHours = siderealTime(t)
        val lstDeg = normalizeDeg(gstHours * 15.0 + longitude)
        val epsRad = earthTilt(t).tobl.degreesToRadians()
        val ramcRad = lstDeg.degreesToRadians()
        val phiRad = latitude.degreesToRadians()
        val y = cos(ramcRad)
        val x = -(sin(ramcRad) * cos(epsRad) + tan(phiRad) * sin(epsRad))
        return normalizeDeg(datan2(y, x))
    }

    fun midheavenTropical(jdUt: Double, longitude: Double): Double {
        val t = toAstronomyTime(jdUt)
        val gstHours = siderealTime(t)
        val lstDeg = normalizeDeg(gstHours * 15.0 + longitude)
        val epsRad = earthTilt(t).tobl.degreesToRadians()
        val ramcRad = lstDeg.degreesToRadians()
        val y = sin(ramcRad)
        val x = cos(ramcRad) * cos(epsRad)
        return normalizeDeg(datan2(y, x))
    }

    fun trueNode(jdUt: Double): Double {
        val t = (jdUt - 2451545.0) / 36525.0
        val mean = 125.04452 - 1934.136261 * t + 0.0020708 * t * t + 2.2e-6 * t * t * t
        val d = Math.toRadians(normalizeDeg(297.8501921 + 445267.1114034 * t))
        val m = Math.toRadians(normalizeDeg(357.5291092 + 35999.0502909 * t))
        val mp = Math.toRadians(normalizeDeg(134.9633964 + 477198.8675055 * t))
        val f = Math.toRadians(normalizeDeg(93.2720950 + 483202.0175233 * t))
        val corr = -1.4979 * sin(2.0 * (d - f)) -
            0.1500 * sin(m) -
            0.1226 * sin(2.0 * d) +
            0.1176 * sin(2.0 * mp) -
            0.0801 * sin(2.0 * (mp - f))
        return normalizeDeg(mean + corr)
    }

    fun meanNode(jdUt: Double): Double {
        val t = (jdUt - 2451545.0) / 36525.0
        return normalizeDeg(125.04452 - 1934.136261 * t + 0.0020708 * t * t + 2.2e-6 * t * t * t)
    }

    override fun tropicalLongitudes(jdUt: Double): Map<Planet, Pair<Double, Double>> {
        val time = toAstronomyTime(jdUt)
        val map = HashMap<Planet, Pair<Double, Double>>()

        val sunLon = sunPosition(time).elon
        val sunSpeed = diffDeg(
            sunPosition(time.addDays(0.02)).elon,
            sunPosition(time.addDays(-0.02)).elon
        ) / 0.04
        map[Planet.SUN] = sunLon to sunSpeed

        val moonLon = eclipticGeoMoon(time).lon
        val moonSpeed = diffDeg(
            eclipticGeoMoon(time.addDays(0.01)).lon,
            eclipticGeoMoon(time.addDays(-0.01)).lon
        ) / 0.02
        map[Planet.MOON] = moonLon to moonSpeed

        val planetBodies = listOf(
            Planet.MERCURY to Body.Mercury,
            Planet.VENUS to Body.Venus,
            Planet.MARS to Body.Mars,
            Planet.JUPITER to Body.Jupiter,
            Planet.SATURN to Body.Saturn
        )
        for ((planet, body) in planetBodies) {
            val lon = planetLon(body, time)
            val speed = diffDeg(
                planetLon(body, time.addDays(0.02)),
                planetLon(body, time.addDays(-0.02))
            ) / 0.04
            map[planet] = lon to speed
        }

        val nodeLon = if (useTrueNode) trueNode(jdUt) else meanNode(jdUt)
        val nodeSpeed = diffDeg(
            if (useTrueNode) trueNode(jdUt + 0.02) else meanNode(jdUt + 0.02),
            if (useTrueNode) trueNode(jdUt - 0.02) else meanNode(jdUt - 0.02)
        ) / 0.04
        map[Planet.RAHU] = nodeLon to nodeSpeed
        map[Planet.KETU] = normalizeDeg(nodeLon + 180.0) to nodeSpeed

        return map
    }

    private fun planetLon(body: Body, time: Time): Double {
        val vec = geoVector(body, time, Aberration.Corrected)
        return equatorialToEcliptic(vec).elon
    }

    companion object {
        const val J2000 = 2451545.0
        const val LAHIRI_J2000_DEG = 23.8570922

        fun normalizeDeg(deg: Double): Double {
            var r = deg % 360.0
            if (r < 0) r += 360.0
            return r
        }

        fun diffDeg(after: Double, before: Double): Double {
            var diff = after - before
            while (diff > 180.0) diff -= 360.0
            while (diff < -180.0) diff += 360.0
            return diff
        }

        fun meanNodeStatic(jdUt: Double): Double {
            val t = (jdUt - 2451545.0) / 36525.0
            return normalizeDeg(125.04452 - 1934.136261 * t + 0.0020708 * t * t + 2.2e-6 * t * t * t)
        }
    }
}
