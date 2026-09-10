package com.neptools.app.astrology.vedic

import com.neptools.app.astrology.data.DashaPeriod
import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import java.time.LocalDate

class DashaCalculator {

    private val spans = mapOf(
        Planet.KETU to 7, Planet.VENUS to 20, Planet.SUN to 6, Planet.MOON to 10,
        Planet.MARS to 7, Planet.RAHU to 18, Planet.JUPITER to 16,
        Planet.SATURN to 19, Planet.MERCURY to 17
    )

    private fun orderFrom(lord: Planet): List<Planet> {
        val full = listOf(
            Planet.KETU, Planet.VENUS, Planet.SUN, Planet.MOON, Planet.MARS,
            Planet.RAHU, Planet.JUPITER, Planet.SATURN, Planet.MERCURY
        )
        val idx = full.indexOf(lord)
        return full.subList(idx, full.size) + full.subList(0, idx)
    }

    fun mahaRoots(chart: NatalChart): List<DashaPeriod> {
        val moonLon = chart.positions.getValue(Planet.MOON).siderealLon
        val nakIdx = NakshatraCalc.index(moonLon)
        val startLord = NakshatraCalc.lord(nakIdx)
        val span = NakshatraCalc.spanDeg()
        val elapsedFraction = ((moonLon % span) / span)
        val firstSpanYears = spans.getValue(startLord).toDouble()
        val birth = chart.birth.date
        val firstEnd = birth.plusDays(Math.round(firstSpanYears * (1 - elapsedFraction) * 365.25).toLong())

        var lord = startLord
        var cursor = firstEnd
        val roots = ArrayList<DashaPeriod>()
        roots.add(
            DashaPeriod(
                lord, birth.minusDays(Math.round(elapsedFraction * firstSpanYears * 365.25).toLong()),
                firstEnd, 1
            )
        )
        for (i in 1 until 9) {
            lord = orderFrom(startLord)[i]
            val end = cursor.plusDays(Math.round(spans.getValue(lord) * 365.25).toLong())
            roots.add(DashaPeriod(lord, cursor, end, 1))
            cursor = end
        }
        return roots
    }

    fun antardashas(root: DashaPeriod): List<DashaPeriod> {
        val totalDays = root.end.toEpochDay() - root.start.toEpochDay()
        val seq = orderFrom(root.lord)
        var cursor = root.start
        val out = ArrayList<DashaPeriod>()
        for (sub in seq) {
            val days = Math.round(totalDays * (spans.getValue(sub).toDouble() / 120.0))
            val end = if (out.size == seq.size - 1) root.end else cursor.plusDays(days)
            out.add(DashaPeriod(sub, cursor, end, 2))
            cursor = end
        }
        return out
    }

    fun pratyantardashas(antar: DashaPeriod): List<DashaPeriod> {
        val totalDays = antar.end.toEpochDay() - antar.start.toEpochDay()
        val seq = orderFrom(antar.lord)
        var cursor = antar.start
        val out = ArrayList<DashaPeriod>()
        for (sub in seq) {
            val days = Math.round(totalDays * (spans.getValue(sub).toDouble() / 120.0))
            val end = if (out.size == seq.size - 1) antar.end else cursor.plusDays(days)
            out.add(DashaPeriod(sub, cursor, end, 3))
            cursor = end
        }
        return out
    }

    fun current(date: LocalDate, roots: List<DashaPeriod>): Triple<DashaPeriod?, DashaPeriod?, DashaPeriod?> {
        val maha = roots.lastOrNull { !date.isBefore(it.start) && date.isBefore(it.end) }
        val antar = maha?.let { antardashas(it).lastOrNull { a -> !date.isBefore(a.start) && date.isBefore(a.end) } }
        val praty = antar?.let { pratyantardashas(it).lastOrNull { p -> !date.isBefore(p.start) && date.isBefore(p.end) } }
        return Triple(maha, antar, praty)
    }
}
