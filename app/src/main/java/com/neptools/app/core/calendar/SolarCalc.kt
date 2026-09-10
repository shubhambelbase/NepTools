package com.neptools.app.core.calendar

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

@androidx.compose.runtime.Immutable
data class SolarDay(
    val sunrise: LocalTime?,
    val sunset: LocalTime?,
    val solarNoon: LocalTime,
    val dayLengthMinutes: Int,
    val rahuStart: LocalTime?,
    val rahuEnd: LocalTime?
)

object SolarCalc {

    const val NEPAL_TZ_OFFSET = 5.75

    fun compute(date: LocalDate, lat: Double, lon: Double): SolarDay {
        val n = date.dayOfYear.toDouble()
        val gamma = 2.0 * Math.PI / 365.0 * (n - 1.0 + 0.5)

        val eqTimeMin = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma)
            )

        val declRad = 0.006918 -
            0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
            0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
            0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)

        val latRad = Math.toRadians(lat)
        val noonUtcMin = 720.0 - 4.0 * lon - eqTimeMin
        val tzShift = NEPAL_TZ_OFFSET * 60.0
        val noonLocal = minutesToTime(noonUtcMin + tzShift)

        val cosH = Math.cos(Math.toRadians(90.833)) /
            (Math.cos(latRad) * Math.cos(declRad)) - Math.tan(latRad) * Math.tan(declRad)

        if (cosH < -1.0 || cosH > 1.0) {
            return SolarDay(null, null, noonLocal, if (cosH < -1.0) 1440 else 0, null, null)
        }

        val haDeg = Math.toDegrees(acos(cosH))
        val haMinutes = 4.0 * haDeg
        val sunriseLocal = minutesToTime(noonUtcMin - haMinutes + tzShift)
        val sunsetLocal = minutesToTime(noonUtcMin + haMinutes + tzShift)
        val dayLen = (2.0 * haMinutes).toInt()

        val rahu = rahuWindow(sunriseLocal, sunsetLocal, WeekdayMapper.fromJava(date.dayOfWeek))
        return SolarDay(sunriseLocal, sunsetLocal, noonLocal, dayLen, rahu.first, rahu.second)
    }

    private fun rahuWindow(sunrise: LocalTime?, sunset: LocalTime?, weekday: Int): Pair<LocalTime?, LocalTime?> {
        if (sunrise == null || sunset == null) return null to null
        val partOf8 = intArrayOf(8, 2, 7, 5, 6, 4, 3)[weekday]
        val riseMin = sunrise.hour * 60 + sunrise.minute
        val setMin = sunset.hour * 60 + sunset.minute
        val span = (setMin - riseMin) / 8.0
        return minutesToTime(riseMin + (partOf8 - 1) * span) to
            minutesToTime(riseMin + partOf8 * span)
    }

    fun minutesToTime(totalMinutes: Double): LocalTime {
        var m = floor(totalMinutes).toInt()
        m = ((m % 1440) + 1440) % 1440
        return LocalTime.of(m / 60, m % 60)
    }

    fun formatNp(t: LocalTime?, npDigits: Boolean): String = formatTime(t, false, npDigits)

    fun formatTime(t: LocalTime?, isEn: Boolean, npDigits: Boolean): String {
        t ?: return "—"
        val h24 = t.hour
        var h12 = h24 % 12
        if (h12 == 0) h12 = 12
        val mm = t.minute.toString().padStart(2, '0')
        val core = "$h12:$mm"
        if (isEn) {
            val ampm = if (h24 < 12) "AM" else "PM"
            return "$core $ampm"
        }
        val part = if (h24 < 12) "बिहान" else if (h24 < 17) "दिउँसो" else "साँझ"
        return if (npDigits) "$part ${NepaliNames.toDevanagari(core)}" else "$part $core"
    }
}
