package com.neptools.app.core.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class ChoghadiyaType(
    val npName: String,
    val enName: String,
    val nature: ChoghadiyaNature,
    val rulerNp: String,
    val rulerEn: String,
    val activityNp: String,
    val activityEn: String,
    val avoidNp: String = "",
    val avoidEn: String = ""
) {
    AMRIT(
        "अमृत", "Amrit", ChoghadiyaNature.EXCELLENT, "चन्द्रमा", "Moon",
        "सबै प्रकारका नयाँ काम, यात्रा, पूजा, विवाह तथा शुभ कार्य",
        "Best for all auspicious work, travel, ceremonies and new beginnings",
        "कुनै बन्देज छैन — सर्वोत्तम समय",
        "No restrictions — peak auspicious time"
    ),
    SHUBH(
        "शुभ", "Shubh", ChoghadiyaNature.GOOD, "बृहस्पति", "Jupiter",
        "विवाह, धार्मिक अनुष्ठान, शिक्षा, मन्दिर दर्शन र सामाजिक कार्य",
        "Ideal for wedding rituals, ceremonies, education and religious activities",
        "विवाद वा तामसिक कार्य नगर्नुहोला",
        "Avoid aggressive disputes or harsh negotiations"
    ),
    LABH(
        "लाभ", "Labh", ChoghadiyaNature.GOOD, "बुध", "Mercury",
        "व्यापार विस्तार, नयाँ लगानी, पसल खोल्न र आर्थिक कारोबार",
        "Best for business, commerce, investments and financial growth",
        "अनावश्यक ऋण वा हतारमा सम्झौता नगर्नुहोला",
        "Avoid rash debt commitments or rushed agreements"
    ),
    CHAR(
        "चर", "Char", ChoghadiyaNature.NEUTRAL, "शुक्र", "Venus",
        "यात्रा, सवारी साधन खरिद, गतिशीलता र चलायमान कार्य",
        "Suitable for travel, vehicle purchase and mobile activities",
        "स्थिर रहने काम (जग हाल्ने, स्थायी बचत) नगर्नुहोला",
        "Avoid permanent investments or laying foundations"
    ),
    ROG(
        "रोग", "Rog", ChoghadiyaNature.INAUSPICIOUS, "मङ्गल", "Mars",
        "रोगोपचार, औषधि सेवन, शल्यक्रिया वा बाधा निवारण",
        "Suitable for medical treatments, surgeries and health cures",
        "कुनै पनि नयाँ कार्य, मांगलिक कार्य वा शुभ यात्रा नगर्नुहोला",
        "Strictly avoid starting new projects or auspicious ceremonies"
    ),
    KAAL(
        "काल", "Kaal", ChoghadiyaNature.INAUSPICIOUS, "शनि", "Saturn",
        "मेशिनरी काम, कडा श्रम, फलाम सम्बन्धी कार्य",
        "Suitable for machinery, manual labor and heavy work",
        "कुनै पनि शुभ काम नगर्नुहोला, हानी र अवरोधको भय हुन्छ",
        "Avoid auspicious events, financial trades or journeys"
    ),
    UDVEG(
        "उद्वेग", "Udveg", ChoghadiyaNature.INAUSPICIOUS, "सूर्य", "Sun",
        "धार्मिक जप, ध्यान, शान्ति पाठ वा मौन बस्न उपयुक्त",
        "Best for quiet meditation, spiritual chanting or prayer",
        "सरकारी काम, नयाँ सम्झौता, वादविवाद वा ठूलो निर्णय नलिनुहोला",
        "Avoid government paperwork, negotiations or risky decisions"
    );

    val ruler: String get() = rulerNp
    fun ruler(isEn: Boolean): String = if (isEn) rulerEn else rulerNp
}

enum class ChoghadiyaNature(val np: String, val en: String) {
    EXCELLENT("सर्वोत्तम", "Excellent"),
    GOOD("शुभ फलदायी", "Auspicious"),
    NEUTRAL("सामान्य", "Neutral"),
    INAUSPICIOUS("अशुभ", "Inauspicious")
}

data class ChoghadiyaSlot(
    val type: ChoghadiyaType,
    val isDay: Boolean,
    val start: LocalTime,
    val end: LocalTime,
    val isCurrent: Boolean,
    val remainingMinutes: Long
)

data class DayChoghadiyaSchedule(
    val date: LocalDate,
    val daySlots: List<ChoghadiyaSlot>,
    val nightSlots: List<ChoghadiyaSlot>,
    val currentSlot: ChoghadiyaSlot?,
    val isCurrentlyDay: Boolean,
    val sunrise: LocalTime,
    val sunset: LocalTime,
    val nextSunrise: LocalTime
)

object ChoghadiyaEngine {

    // Default Kathmandu coordinates if GPS unavailable
    const val KATHMANDU_LAT = 27.7172
    const val KATHMANDU_LON = 85.3240

    private val DAY_SEQUENCE = mapOf(
        DayOfWeek.SUNDAY to listOf(
            ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR, ChoghadiyaType.LABH,
            ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH,
            ChoghadiyaType.ROG, ChoghadiyaType.UDVEG
        ),
        DayOfWeek.MONDAY to listOf(
            ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH,
            ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR,
            ChoghadiyaType.LABH, ChoghadiyaType.AMRIT
        ),
        DayOfWeek.TUESDAY to listOf(
            ChoghadiyaType.ROG, ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR,
            ChoghadiyaType.LABH, ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL,
            ChoghadiyaType.SHUBH, ChoghadiyaType.ROG
        ),
        DayOfWeek.WEDNESDAY to listOf(
            ChoghadiyaType.LABH, ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL,
            ChoghadiyaType.SHUBH, ChoghadiyaType.ROG, ChoghadiyaType.UDVEG,
            ChoghadiyaType.CHAR, ChoghadiyaType.LABH
        ),
        DayOfWeek.THURSDAY to listOf(
            ChoghadiyaType.SHUBH, ChoghadiyaType.ROG, ChoghadiyaType.UDVEG,
            ChoghadiyaType.CHAR, ChoghadiyaType.LABH, ChoghadiyaType.AMRIT,
            ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH
        ),
        DayOfWeek.FRIDAY to listOf(
            ChoghadiyaType.CHAR, ChoghadiyaType.LABH, ChoghadiyaType.AMRIT,
            ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH, ChoghadiyaType.ROG,
            ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR
        ),
        DayOfWeek.SATURDAY to listOf(
            ChoghadiyaType.KAAL, ChoghadiyaType.SHUBH, ChoghadiyaType.ROG,
            ChoghadiyaType.UDVEG, ChoghadiyaType.CHAR, ChoghadiyaType.LABH,
            ChoghadiyaType.AMRIT, ChoghadiyaType.KAAL
        )
    )

    private val NIGHT_SEQUENCE = mapOf(
        DayOfWeek.SUNDAY to listOf(
            ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR,
            ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH,
            ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH
        ),
        DayOfWeek.MONDAY to listOf(
            ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL,
            ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH,
            ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR
        ),
        DayOfWeek.TUESDAY to listOf(
            ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG,
            ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR,
            ChoghadiyaType.ROG, ChoghadiyaType.KAAL
        ),
        DayOfWeek.WEDNESDAY to listOf(
            ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT,
            ChoghadiyaType.CHAR, ChoghadiyaType.ROG, ChoghadiyaType.KAAL,
            ChoghadiyaType.LABH, ChoghadiyaType.UDVEG
        ),
        DayOfWeek.THURSDAY to listOf(
            ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG,
            ChoghadiyaType.KAAL, ChoghadiyaType.LABH, ChoghadiyaType.UDVEG,
            ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT
        ),
        DayOfWeek.FRIDAY to listOf(
            ChoghadiyaType.ROG, ChoghadiyaType.KAAL, ChoghadiyaType.LABH,
            ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH, ChoghadiyaType.AMRIT,
            ChoghadiyaType.CHAR, ChoghadiyaType.ROG
        ),
        DayOfWeek.SATURDAY to listOf(
            ChoghadiyaType.LABH, ChoghadiyaType.UDVEG, ChoghadiyaType.SHUBH,
            ChoghadiyaType.AMRIT, ChoghadiyaType.CHAR, ChoghadiyaType.ROG,
            ChoghadiyaType.KAAL, ChoghadiyaType.LABH
        )
    )

    fun computeSchedule(
        date: LocalDate = LocalDate.now(),
        nowTime: LocalTime = LocalTime.now(),
        lat: Double = KATHMANDU_LAT,
        lon: Double = KATHMANDU_LON
    ): DayChoghadiyaSchedule {
        val solarToday = SolarCalc.compute(date, lat, lon)
        val solarTomorrow = SolarCalc.compute(date.plusDays(1), lat, lon)

        val sunrise = solarToday.sunrise ?: LocalTime.of(6, 0)
        val sunset = solarToday.sunset ?: LocalTime.of(18, 0)
        val nextSunrise = solarTomorrow.sunrise ?: LocalTime.of(6, 0)

        val daySeq = DAY_SEQUENCE[date.dayOfWeek] ?: DAY_SEQUENCE[DayOfWeek.SUNDAY]!!
        val nightSeq = NIGHT_SEQUENCE[date.dayOfWeek] ?: NIGHT_SEQUENCE[DayOfWeek.SUNDAY]!!

        val daySecs = ChronoUnit.SECONDS.between(sunrise, sunset)
        val daySlotSecs = daySecs / 8.0

        val isCurrentlyDay = !nowTime.isBefore(sunrise) && nowTime.isBefore(sunset)

        val daySlots = daySeq.mapIndexed { index, type ->
            val slotStart = sunrise.plusSeconds((index * daySlotSecs).toLong())
            val slotEnd = sunrise.plusSeconds(((index + 1) * daySlotSecs).toLong())
            val isCurrent = isCurrentlyDay && !nowTime.isBefore(slotStart) && nowTime.isBefore(slotEnd)
            val remMin = if (isCurrent) {
                ChronoUnit.MINUTES.between(nowTime, slotEnd).coerceAtLeast(0)
            } else 0L

            ChoghadiyaSlot(
                type = type,
                isDay = true,
                start = slotStart,
                end = slotEnd,
                isCurrent = isCurrent,
                remainingMinutes = remMin
            )
        }

        val nightSecs = (24 * 3600 - sunset.toSecondOfDay()) + nextSunrise.toSecondOfDay()
        val nightSlotSecs = nightSecs / 8.0

        val nightSlots = nightSeq.mapIndexed { index, type ->
            val startSec = (sunset.toSecondOfDay() + (index * nightSlotSecs).toLong()) % (24 * 3600)
            val endSec = (sunset.toSecondOfDay() + ((index + 1) * nightSlotSecs).toLong()) % (24 * 3600)

            val slotStart = LocalTime.ofSecondOfDay(startSec)
            val slotEnd = LocalTime.ofSecondOfDay(endSec)

            val isCurrent = if (!isCurrentlyDay) {
                if (startSec < endSec) {
                    !nowTime.isBefore(slotStart) && nowTime.isBefore(slotEnd)
                } else {
                    // Spanning midnight
                    !nowTime.isBefore(slotStart) || nowTime.isBefore(slotEnd)
                }
            } else false

            val remMin = if (isCurrent) {
                if (nowTime.isBefore(slotEnd)) {
                    ChronoUnit.MINUTES.between(nowTime, slotEnd).coerceAtLeast(0)
                } else {
                    // Midnight wrap
                    val minsToMid = ChronoUnit.MINUTES.between(nowTime, LocalTime.MAX)
                    val minsFromMid = ChronoUnit.MINUTES.between(LocalTime.MIN, slotEnd)
                    (minsToMid + minsFromMid).coerceAtLeast(0)
                }
            } else 0L

            ChoghadiyaSlot(
                type = type,
                isDay = false,
                start = slotStart,
                end = slotEnd,
                isCurrent = isCurrent,
                remainingMinutes = remMin
            )
        }

        val currentSlot = if (isCurrentlyDay) {
            daySlots.firstOrNull { it.isCurrent }
        } else {
            nightSlots.firstOrNull { it.isCurrent }
        }

        return DayChoghadiyaSchedule(
            date = date,
            daySlots = daySlots,
            nightSlots = nightSlots,
            currentSlot = currentSlot,
            isCurrentlyDay = isCurrentlyDay,
            sunrise = sunrise,
            sunset = sunset,
            nextSunrise = nextSunrise
        )
    }
}
