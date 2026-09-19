package com.neptools.app

import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.updates.RecentUpdateRecord
import com.neptools.app.core.updates.RelativeTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentUpdatesTest {

    @Test
    fun `relative time formatting - English - various intervals`() {
        val now = 1_700_000_000_000L

        // Just now (< 1 min)
        assertEquals("Updated just now", RelativeTimeFormatter.format(now - 30_000L, isEn = true, nowMillis = now))

        // 1 minute ago
        assertEquals("Updated 1 minute ago", RelativeTimeFormatter.format(now - 60_000L, isEn = true, nowMillis = now))

        // 15 minutes ago
        assertEquals("Updated 15 minutes ago", RelativeTimeFormatter.format(now - 15 * 60_000L, isEn = true, nowMillis = now))

        // 1 hour ago
        assertEquals("Updated 1 hour ago", RelativeTimeFormatter.format(now - 3600_000L, isEn = true, nowMillis = now))

        // 4 hours ago
        assertEquals("Updated 4 hours ago", RelativeTimeFormatter.format(now - 4 * 3600_000L, isEn = true, nowMillis = now))

        // Yesterday (1 day ago)
        assertEquals("Updated yesterday", RelativeTimeFormatter.format(now - 24 * 3600_000L, isEn = true, nowMillis = now))

        // 3 days ago
        assertEquals("Updated 3 days ago", RelativeTimeFormatter.format(now - 3 * 24 * 3600_000L, isEn = true, nowMillis = now))

        // Zero / negative timestamp
        assertEquals("No update recorded", RelativeTimeFormatter.format(0L, isEn = true, nowMillis = now))
    }

    @Test
    fun `relative time formatting - Nepali - various intervals with Devanagari numerals`() {
        val now = 1_700_000_000_000L

        // Just now (< 1 min)
        assertEquals("भर्खरै अद्यावधिक", RelativeTimeFormatter.format(now - 30_000L, isEn = false, nowMillis = now))

        // 1 minute ago
        assertEquals("१ मिनेट अघि अद्यावधिक", RelativeTimeFormatter.format(now - 60_000L, isEn = false, nowMillis = now))

        // 25 minutes ago
        assertEquals("२५ मिनेट अघि अद्यावधिक", RelativeTimeFormatter.format(now - 25 * 60_000L, isEn = false, nowMillis = now))

        // 1 hour ago
        assertEquals("१ घण्टा अघि अद्यावधिक", RelativeTimeFormatter.format(now - 3600_000L, isEn = false, nowMillis = now))

        // 6 hours ago
        assertEquals("६ घण्टा अघि अद्यावधिक", RelativeTimeFormatter.format(now - 6 * 3600_000L, isEn = false, nowMillis = now))

        // Yesterday (1 day ago)
        assertEquals("हिजो अद्यावधिक", RelativeTimeFormatter.format(now - 24 * 3600_000L, isEn = false, nowMillis = now))

        // 5 days ago
        assertEquals("५ दिन अघि अद्यावधिक", RelativeTimeFormatter.format(now - 5 * 24 * 3600_000L, isEn = false, nowMillis = now))

        // Zero / negative timestamp
        assertEquals("कुनै अद्यावधिक छैन", RelativeTimeFormatter.format(0L, isEn = false, nowMillis = now))
    }

    @Test
    fun `relative time formatting contains no emojis in either language`() {
        val now = 1_700_000_000_000L
        val intervals = listOf(
            0L,
            now - 10_000L,
            now - 120_000L,
            now - 7200_000L,
            now - 86400_000L,
            now - 5 * 86400_000L
        )

        val emojiRegex = Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")

        for (ts in intervals) {
            val en = RelativeTimeFormatter.format(ts, isEn = true, nowMillis = now)
            val np = RelativeTimeFormatter.format(ts, isEn = false, nowMillis = now)

            assertFalse("English relative string '$en' must not contain emojis", emojiRegex.containsMatchIn(en))
            assertFalse("Nepali relative string '$np' must not contain emojis", emojiRegex.containsMatchIn(np))
        }
    }

    @Test
    fun `recent update record json serialization and deserialization roundtrip`() {
        val original = RecentUpdateRecord(
            serviceId = "fuel",
            nameNp = "पेट्रोलियम भाउ",
            nameEn = "Fuel Prices",
            route = "fuel",
            iconType = "fuel",
            timestampMillis = 1_700_000_123_456L,
            statusNp = "नेपाल आयल निगम",
            statusEn = "NOC Retail Rates",
            isUnread = true
        )

        val json = original.toJson()
        val restored = RecentUpdateRecord.fromJson(json)

        assertEquals(original.serviceId, restored.serviceId)
        assertEquals(original.nameNp, restored.nameNp)
        assertEquals(original.nameEn, restored.nameEn)
        assertEquals(original.route, restored.route)
        assertEquals(original.iconType, restored.iconType)
        assertEquals(original.timestampMillis, restored.timestampMillis)
        assertEquals(original.statusNp, restored.statusNp)
        assertEquals(original.statusEn, restored.statusEn)
        assertEquals(original.isUnread, restored.isUnread)
    }

    @Test
    fun `exact time formatter Nepali output uses Devanagari numerals and no emojis`() {
        val ts = 1_700_000_000_000L
        val exactNp = RelativeTimeFormatter.formatExact(ts, isEn = false)
        val exactEn = RelativeTimeFormatter.formatExact(ts, isEn = true)

        val emojiRegex = Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")
        assertFalse("Nepali exact string '$exactNp' must not contain emojis", emojiRegex.containsMatchIn(exactNp))
        assertFalse("English exact string '$exactEn' must not contain emojis", emojiRegex.containsMatchIn(exactEn))
        assertTrue("Nepali exact string must have time indicator", exactNp.contains("पूर्वाह्न") || exactNp.contains("अपराह्न"))
    }
}
