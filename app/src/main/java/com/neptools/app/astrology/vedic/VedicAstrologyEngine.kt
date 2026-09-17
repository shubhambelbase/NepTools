package com.neptools.app.astrology.vedic

import com.neptools.app.astrology.analysis.AnalysisBundleBuilder
import com.neptools.app.astrology.analysis.TimelineBuilder
import com.neptools.app.astrology.data.AstrologyResult
import com.neptools.app.astrology.data.BirthData
import com.neptools.app.astrology.data.DashaPeriod
import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.PlanetPosition
import com.neptools.app.astrology.data.HouseInfo
import com.neptools.app.astrology.data.TransitInfo
import java.time.LocalDate
import java.time.LocalDateTime

class VedicAstrologyEngine(private val engine: com.neptools.app.astrology.ephemeris.EphemerisEngine) {

    private val sidereal = SiderealCalculator(engine)
    private val lagna = LagnaCalculator(sidereal)
    private val houses = HouseCalculator()
    private val dasha = DashaCalculator()
    private val yogas = YogaDetector()
    private val strength = StrengthCalculator()
    private val transitCalc = TransitCalculator(engine)

    fun natalChart(birth: BirthData): NatalChart {
        val jdUt = engine.julianDay(birth.date, birth.time, birth.tzOffsetHours)
        val (ay, positions) = sidereal.all(birth)
        val (lagnaLon, lagnaSign) = lagna.compute(birth)
        val houseList = houses.houses(lagnaSign)

        val mcTrop = (engine as? com.neptools.app.astrology.ephemeris.HighPrecisionEphemeris)?.midheavenTropical(jdUt, birth.longitude)
            ?: (engine as? com.neptools.app.astrology.ephemeris.MeeusEphemeris)?.midheavenTropicalDeg(jdUt, birth.longitude)
            ?: SiderealCalculator.normalize(lagnaLon + 90.0 + ay)
        val mcSidereal = SiderealCalculator.normalize(mcTrop - ay)
        val cusps = houses.sripatiCusps(lagnaLon, mcSidereal)

        val posMap = positions.associate { sp ->
            val sign = (sp.lon / 30.0).toInt()
            val bhava = houses.bhavaOf(sp.lon, cusps)
            sp.planet to PlanetPosition(
                planet = sp.planet,
                tropicalLon = SiderealCalculator.normalize(sp.lon + ay),
                siderealLon = sp.lon,
                speedPerDay = sp.speed,
                retrograde = sp.speed < 0 && sp.planet != Planet.RAHU && sp.planet != Planet.KETU,
                signIndex = sign,
                degreeInSign = sp.lon % 30.0,
                nakshatraName = NakshatraCalc.names[NakshatraCalc.index(sp.lon)],
                nakshatraPada = NakshatraCalc.pada(sp.lon),
                houseFromLagna = HouseCalc.houseOf(sign, lagnaSign),
                houseFromMoon = -1,
                bhavaFromLagna = bhava
            )
        }
        val moonSign = posMap.getValue(Planet.MOON).signIndex
        val withMoonHouses = posMap.mapValues { (_, v) ->
            v.copy(houseFromMoon = HouseCalc.houseFromMoon(v.signIndex, moonSign))
        }

        return NatalChart(
            birth = birth,
            julianDayUt = jdUt,
            ayanamsa = ay,
            lagnaSiderealLon = lagnaLon,
            lagnaSign = lagnaSign,
            houses = houseList,
            positions = withMoonHouses,
            moonNakshatra = posMap.getValue(Planet.MOON).nakshatraName,
            engineTag = engine.tag,
            sripatiCusps = cusps
        )
    }

    fun currentTransits(chart: NatalChart, now: LocalDateTime, tzOffset: Double = 5.75): List<TransitInfo> =
        transitCalc.currentTransits(chart, now, tzOffset)

    fun natalConjunctions(transits: List<TransitInfo>, chart: NatalChart): Map<Planet, Planet> =
        transitCalc.natalConjunctions(transits, chart)

    fun fullResult(birth: BirthData, now: LocalDateTime = LocalDateTime.now()): AstrologyResult {
        val chart = natalChart(birth)
        val roots = dasha.mahaRoots(chart)
        val (maha, antar, praty) = dasha.current(LocalDate.now(), roots)
        val transits = transitCalc.currentTransits(chart, now, birth.tzOffsetHours)
        val conj = transitCalc.natalConjunctions(transits, chart)
        val yogaFindings = yogas.detect(chart).filter { it.present }
        val strengths: Map<Planet, StrengthCalculator.StrengthResult> =
            Planet.SEVEN.associateWith { strength.evaluate(it, chart) }

        val analysis = AnalysisBundleBuilder.build(
            chart, strengths, maha, antar, praty, transits, conj, yogaFindings
        )

        return AstrologyResult(
            chart = chart,
            dashaTreeRoots = roots,
            currentMaha = maha,
            currentAntar = antar,
            currentPratyantar = praty,
            transits = transits,
            yogas = yogaFindings,
            scores = analysis.scores,
            todaySummary = analysis.todayLines,
            timeline = TimelineBuilder.buildMonthly(chart, strengths, dasha, roots),
            computedAtMillis = System.currentTimeMillis()
        )
    }
}
