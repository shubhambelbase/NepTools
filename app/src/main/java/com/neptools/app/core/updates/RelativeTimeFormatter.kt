package com.neptools.app.core.updates

import com.neptools.app.core.calendar.NepaliNames
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RelativeTimeFormatter {

    fun format(
        timestampMillis: Long,
        isEn: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        if (timestampMillis <= 0L) {
            return if (isEn) "No update recorded" else "कुनै अद्यावधिक छैन"
        }

        val diffMs = maxOf(0L, nowMillis - timestampMillis)
        val minutes = diffMs / (60L * 1000L)
        val hours = diffMs / (60L * 60L * 1000L)
        val days = diffMs / (24L * 60L * 60L * 1000L)

        return if (isEn) {
            when {
                minutes < 1L -> "Updated just now"
                minutes < 60L -> if (minutes == 1L) "Updated 1 minute ago" else "Updated $minutes minutes ago"
                hours < 24L -> if (hours == 1L) "Updated 1 hour ago" else "Updated $hours hours ago"
                days == 1L -> "Updated yesterday"
                else -> "Updated $days days ago"
            }
        } else {
            when {
                minutes < 1L -> "भर्खरै अद्यावधिक"
                minutes < 60L -> "${NepaliNames.toDevanagari(minutes)} मिनेट अघि अद्यावधिक"
                hours < 24L -> "${NepaliNames.toDevanagari(hours)} घण्टा अघि अद्यावधिक"
                days == 1L -> "हिजो अद्यावधिक"
                else -> "${NepaliNames.toDevanagari(days)} दिन अघि अद्यावधिक"
            }
        }
    }

    fun formatExact(timestampMillis: Long, isEn: Boolean): String {
        if (timestampMillis <= 0L) return "—"
        val date = Date(timestampMillis)
        return if (isEn) {
            val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US)
            sdf.format(date)
        } else {
            val sdfTime = SimpleDateFormat("h:mm", Locale.US)
            val sdfAmPm = SimpleDateFormat("a", Locale.US)
            val timeStr = sdfTime.format(date)
            val amPm = if (sdfAmPm.format(date).equals("PM", ignoreCase = true)) "अपराह्न" else "पूर्वाह्न"
            val devTime = NepaliNames.toDevanagari(timeStr)
            "$devTime $amPm"
        }
    }
}
