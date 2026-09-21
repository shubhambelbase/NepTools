package com.neptools.app

import com.neptools.app.core.calendar.BsCalendarEngine
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class BsCalendarEngineTest {

    private fun realEngine(): BsCalendarEngine {
        val file = File("src/main/assets/bs_calendar.json")
        assertTrue("Dataset missing at ${file.absolutePath}", file.exists())
        val root = JSONObject(file.readText())
        val anchor = root.getJSONObject("anchor")
        val yearsJson = root.getJSONObject("years")
        val map = HashMap<Int, IntArray>()
        val keys = yearsJson.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val arr = yearsJson.getJSONArray(k)
            map[k.toInt()] = IntArray(arr.length()) { arr.getInt(it) }
        }
        return BsCalendarEngine(
            firstYear = root.getInt("firstYear"),
            lastYear = root.getInt("lastYear"),
            monthLengthsByYear = map,
            anchorAd = LocalDate.parse(anchor.getString("adDate")),
            anchorWeekdayIndex = anchor.getInt("weekdayIndex")
        )
    }

    @Test
    fun `dataset loads with 125 years`() {
        val engine = realEngine()
        assertEquals(1975, engine.supportedRange().first)
        assertEquals(2099, engine.supportedRange().last)
        for (y in engine.supportedRange()) {
            assertTrue(engine.yearLength(y) in 355..370)
        }
    }

    @Test
    fun `anchor maps to 1918-04-13`() {
        assertEquals(LocalDate.of(1918, 4, 13), realEngine().bsToAd(NepaliDate(1975, 1, 1)))
    }

    @Test
    fun `new year 2077 is 2020-04-13`() {
        assertEquals(LocalDate.of(2020, 4, 13), realEngine().bsToAd(NepaliDate(2077, 1, 1)))
    }

    @Test
    fun `new year 2081 is 2024-04-13`() {
        assertEquals(LocalDate.of(2024, 4, 13), realEngine().bsToAd(NepaliDate(2081, 1, 1)))
    }

    @Test
    fun `august 23 2026 is bhadra 7 2083 sunday`() {
        val engine = realEngine()
        val np = engine.adToBs(LocalDate.of(2026, 8, 23))
        assertEquals(NepaliDate(2083, 5, 7), np)
        assertEquals(0, engine.weekdayIndexOf(np))
    }

    @Test
    fun `round trip across full range`() {
        val engine = realEngine()
        for (year in intArrayOf(1975, 1985, 1999, 2000, 2043, 2077, 2083, 2099)) {
            var ad = engine.bsToAd(NepaliDate(year, 1, 1))
            val last = NepaliDate(year, 12, engine.monthLength(year, 12))
            val endAd = engine.bsToAd(last)
            var cursor = ad
            while (!cursor.isAfter(endAd)) {
                val back = engine.adToBs(cursor)
                assertEquals(cursor, engine.bsToAd(back))
                cursor = cursor.plusDays(1)
            }
            assertTrue(!ad.isAfter(endAd))
        }
    }

    @Test
    fun `devanagari digits convert both ways`() {
        assertEquals("२०८३", NepaliNames.toDevanagari(2083))
        assertEquals("2083", NepaliNames.toAscii("२०८३"))
        assertEquals("७", NepaliNames.toDevanagari(7))
    }

    @Test
    fun `month grid pads correctly`() {
        val engine = realEngine()
        val grid = engine.buildMonthGrid(2083, 5)
        assertTrue(grid.isNotEmpty())
        grid.forEach { row -> assertEquals(7, row.size) }
        val flat = grid.flatten().filterNotNull()
        assertEquals(engine.monthLength(2083, 5), flat.size)
        assertEquals(NepaliDate(2083, 5, 1), flat.first().nepaliDate)
    }

    @Test
    fun `panchang lunar masa calculation is accurate across normal and adhik maas years`() {
        val engine = realEngine()

        // 2081 Normal Year: Dashain Vijaya Dashami (Ashoj 26 / 2024-10-12)
        val p2081Dashain = com.neptools.app.core.calendar.PanchangCalc.compute(engine.bsToAd(NepaliDate(2081, 6, 26)))
        assertEquals(5, p2081Dashain.lunarMasaIndex) // 5 = Ashwin

        // 2081 Normal Year: Tihar Bhai Tika (Kartik 18 / 2024-11-03)
        val p2081Tihar = com.neptools.app.core.calendar.PanchangCalc.compute(engine.bsToAd(NepaliDate(2081, 7, 18)))
        assertEquals(6, p2081Tihar.lunarMasaIndex) // 6 = Kartik

        // 2080 Adhik Maas Year: Vijaya Dashami shifted to Kartik 7 (2023-10-24)
        val p2080Dashain = com.neptools.app.core.calendar.PanchangCalc.compute(engine.bsToAd(NepaliDate(2080, 7, 7)))
        assertEquals(5, p2080Dashain.lunarMasaIndex) // Ashvina masa even in Kartik solar month

        // 2083 Adhik Maas Year: Vijaya Dashami shifted to Kartik 4 (2026-10-20)
        val p2083Dashain = com.neptools.app.core.calendar.PanchangCalc.compute(engine.bsToAd(NepaliDate(2083, 7, 4)))
        assertEquals(5, p2083Dashain.lunarMasaIndex) // Ashvina masa
    }

    @Test
    fun `dynamic festival engine places festivals in correct lunar masa`() {
        val engine = realEngine()

        // 2083 Ashoj 5 should NOT have Vijaya Dashami
        val d2083Ashoj5 = NepaliDate(2083, 6, 5)
        val ad2083Ashoj5 = engine.bsToAd(d2083Ashoj5)
        val p2083Ashoj5 = com.neptools.app.core.calendar.PanchangCalc.compute(ad2083Ashoj5)
        val f2083Ashoj5 = com.neptools.app.core.calendar.DynamicFestivalEngine.computeForDay(
            2083, 6, 5, ad2083Ashoj5, p2083Ashoj5
        )
        assertTrue(f2083Ashoj5.none { it.nameNp.contains("विजया दशमी") })

        // 2083 Kartik 4 should have Vijaya Dashami
        val d2083Kartik4 = NepaliDate(2083, 7, 4)
        val ad2083Kartik4 = engine.bsToAd(d2083Kartik4)
        val p2083Kartik4 = com.neptools.app.core.calendar.PanchangCalc.compute(ad2083Kartik4)
        val f2083Kartik4 = com.neptools.app.core.calendar.DynamicFestivalEngine.computeForDay(
            2083, 7, 4, ad2083Kartik4, p2083Kartik4
        )
        assertTrue(f2083Kartik4.any { it.nameNp.contains("विजया दशमी") })
    }

    @Test
    fun `static festival dataset loads and covers verified years 2075 through 2085`() {
        val file = File("src/main/assets/festivals_sample.json")
        assertTrue("Festivals dataset missing at ${file.absolutePath}", file.exists())
        val root = JSONObject(file.readText())

        for (year in 2075..2085) {
            for (m in 1..12) {
                val key = "$year-$m"
                assertTrue("Month $key missing from festivals_sample.json", root.has(key))
            }
        }
    }
}
