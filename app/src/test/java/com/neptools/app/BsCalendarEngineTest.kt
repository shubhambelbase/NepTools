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
}
