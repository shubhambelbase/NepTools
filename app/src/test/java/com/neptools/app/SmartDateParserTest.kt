package com.neptools.app

import com.neptools.app.core.calendar.DetectedDate
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.SmartDateParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SmartDateParserTest {

    @Test
    fun testDetectBsSlashFormat() {
        val res = SmartDateParser.detectDate("Citizenship issued on 2081/05/15 in Kathmandu")
        assertNotNull(res)
        assertTrue(res is DetectedDate.Bs)
        val bs = res as DetectedDate.Bs
        assertEquals(NepaliDate(2081, 5, 15), bs.date)
    }

    @Test
    fun testDetectBsDevanagariDigits() {
        val res = SmartDateParser.detectDate("मिति: २०८२-०८-२२ गते")
        assertNotNull(res)
        assertTrue(res is DetectedDate.Bs)
        val bs = res as DetectedDate.Bs
        assertEquals(NepaliDate(2082, 8, 22), bs.date)
    }

    @Test
    fun testDetectBsMonthName() {
        val res = SmartDateParser.detectDate("The exam will occur on 15 Falgun 2081")
        assertNotNull(res)
        assertTrue(res is DetectedDate.Bs)
        val bs = res as DetectedDate.Bs
        assertEquals(NepaliDate(2081, 11, 15), bs.date)
    }

    @Test
    fun testDetectAdStandardFormat() {
        val res = SmartDateParser.detectDate("Passport expiry: 2028-11-20")
        assertNotNull(res)
        assertTrue(res is DetectedDate.Ad)
        val ad = res as DetectedDate.Ad
        assertEquals(LocalDate.of(2028, 11, 20), ad.date)
    }

    @Test
    fun testDetectBlankInput() {
        assertNull(SmartDateParser.detectDate(""))
        assertNull(SmartDateParser.detectDate("   "))
        assertNull(SmartDateParser.detectDate("Random text with no numbers"))
    }
}
