package com.neptools.app

import com.neptools.app.astrology.data.BirthData
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.ephemeris.MeeusEphemeris
import com.neptools.app.astrology.vedic.DashaCalculator
import com.neptools.app.astrology.vedic.NakshatraCalc
import com.neptools.app.astrology.vedic.VedicAstrologyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class AstrologyValidationTest {

    private val eng = MeeusEphemeris()

    private fun jd(y: Int, mo: Int, d: Int, h: Double) =
        eng.julianDay(LocalDate.of(y, mo, d), LocalTime.of(h.toInt(), ((h % 1) * 60).toInt()), 0.0)

    @Test
    fun `julian day J2000 anchor`() {
        assertEquals(2451545.0, jd(2000, 1, 1, 12.0), 1e-9)
    }

    @Test
    fun `sun longitude matches Meeus example 25a within tolerance`() {
        val jd = jd(1992, 10, 13, 0.0)
        val sunLon = eng.tropicalLongitudes(jd).getValue(Planet.SUN).first
        assertTrue("got $sunLon", kotlin.math.abs(sunLon - 199.90988) < 0.03)
    }

    @Test
    fun `moon longitude matches Meeus example 47a within truncated-series tolerance`() {
        val jd = jd(1992, 4, 12, 0.0)
        val moonLon = eng.tropicalLongitudes(jd).getValue(Planet.MOON).first
        val diff = kotlin.math.abs(moonLon - 133.162655)
        assertTrue("moon diff $diff", diff < 0.25)
    }

    @Test
    fun `lahiri ayanamsa near 2026 in expected window`() {
        val ay = eng.ayanamsa(jd(2026, 8, 23, 0.0), com.neptools.app.astrology.ephemeris.AyanamsaType.LAHIRI)
        assertTrue("ay=$ay", ay in 24.19..24.27)
    }

    @Test
    fun `rahu mean node matches Meeus 47p7 anchor`() {
        val rahu = eng.meanNode(jd(2000, 1, 1, 12.0))
        assertTrue("rahu=$rahu", kotlin.math.abs(rahu - 125.0445) < 0.02)
    }

    @Test
    fun `mercury shows retrograde and direct motion within 2026`() {
        var sawRetro = false
        var sawDirect = false
        var d = LocalDate.of(2026, 1, 1)
        while (d.isBefore(LocalDate.of(2026, 12, 31))) {
            val speed = eng.tropicalLongitudes(jd(d.year, d.monthValue, d.dayOfMonth, 12.0))
                .getValue(Planet.MERCURY).second
            if (speed < 0) sawRetro = true else sawDirect = true
            if (sawRetro && sawDirect) break
            d = d.plusDays(3)
        }
        assertTrue("retro=$sawRetro direct=$sawDirect", sawRetro && sawDirect)
    }

    @Test
    fun `ketu opposite rahu`() {
        val map = eng.tropicalLongitudes(jd(2026, 8, 20, 12.0))
        val rahu = map.getValue(Planet.RAHU).first
        val ketu = map.getValue(Planet.KETU).first
        var d = (ketu - rahu + 360) % 360
        assertEquals(180.0, d, 1e-6)
    }

    @Test
    fun `vimshottari starts with nakshatra lords sequence`() {
        assertEquals(Planet.KETU, NakshatraCalc.lord(0))
        assertEquals(Planet.VENUS, NakshatraCalc.lord(1))
        assertEquals(Planet.SUN, NakshatraCalc.lord(2))
        assertEquals(Planet.MOON, NakshatraCalc.lord(3))
    }

    @Test
    fun `full pipeline deterministic and dasha current found`() {
        val birth = BirthData(
            date = LocalDate.of(1997, 8, 15),
            time = LocalTime.of(9, 15),
            latitude = 27.7172, longitude = 85.3240,
            tzOffsetHours = 5.75, placeLabel = "KTM"
        )
        val engine = VedicAstrologyEngine(MeeusEphemeris())
        val a = engine.fullResult(birth)
        val b = engine.fullResult(birth)
        assertEquals(a.chart.positions.getValue(Planet.MOON).siderealLon,
            b.chart.positions.getValue(Planet.MOON).siderealLon, 1e-9)
        assertTrue(a.currentMaha != null)
        assertTrue(a.currentAntar != null)
        assertTrue(a.scores.size == 15)
    }

    @Test
    fun `panchang nakshatra is sidereal and valid`() {
        val date = LocalDate.of(2024, 4, 14)
        val panchang = com.neptools.app.core.calendar.PanchangCalc.compute(date)
        assertTrue(panchang.nakshatraName.isNotBlank())
        assertTrue(panchang.yogaName.isNotBlank())
        assertTrue(panchang.tithiEndsPercent in 0..99)
    }

    @Test
    fun `sripati bhava chalit cusps are 12 and contain all planets`() {
        val birth = BirthData(
            date = LocalDate.of(2000, 1, 1),
            time = LocalTime.of(6, 0),
            latitude = 27.7172, longitude = 85.3240,
            tzOffsetHours = 5.75, placeLabel = "KTM"
        )
        val engine = VedicAstrologyEngine(MeeusEphemeris())
        val res = engine.natalChart(birth)
        assertEquals(12, res.sripatiCusps.size)
        Planet.NINE.forEach { p ->
            val pos = res.positions.getValue(p)
            assertTrue("Bhava for $p was ${pos.bhavaFromLagna}", pos.bhavaFromLagna in 1..12)
        }
    }

    @Test
    fun `moon position high precision within tight tolerance`() {
        val jd = jd(1992, 4, 12, 0.0)
        val moonLon = eng.tropicalLongitudes(jd).getValue(Planet.MOON).first
        val diff = kotlin.math.abs(moonLon - 133.162655)
        assertTrue("moon diff $diff", diff < 0.05)
    }

    @Test
    fun `ashtakoota guna milan max points 36 and correct nadi dosha`() {
        // Ashwini (Adi Nadi) and Bharani (Madhya Nadi) -> different Nadi -> 8 pts
        val res1 = com.neptools.app.astrology.vedic.AshtakootaGunaMilan.calculate(0, 1)
        val nadi1 = res1.kootas.first { it.key == "nadi" }
        assertEquals(8.0, nadi1.earnedPoints, 0.01)
        assertTrue(!res1.hasNadiDosha)

        // Ashwini (Adi Nadi, idx 0) and Ardra (Adi Nadi, idx 5) -> same Nadi -> Nadi Dosha -> 0 pts
        val res2 = com.neptools.app.astrology.vedic.AshtakootaGunaMilan.calculate(0, 5)
        val nadi2 = res2.kootas.first { it.key == "nadi" }
        assertEquals(0.0, nadi2.earnedPoints, 0.01)
        assertTrue(res2.hasNadiDosha)

        // Total points must be within 0..36
        assertTrue(res1.totalPoints in 0.0..36.0)
        assertTrue(res2.totalPoints in 0.0..36.0)
        assertEquals(8, res1.kootas.size)
    }
}

