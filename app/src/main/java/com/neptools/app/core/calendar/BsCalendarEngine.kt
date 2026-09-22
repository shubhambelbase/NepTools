package com.neptools.app.core.calendar

import java.time.DayOfWeek
import java.time.LocalDate

@androidx.compose.runtime.Immutable
data class CalendarCell(
    val nepaliDate: NepaliDate,
    val adDate: LocalDate,
    val dayOfMonth: Int,
    val weekdayIndex: Int,
    val isToday: Boolean,
    val isSaturday: Boolean,
    val festivals: List<Festival>,
    val hasUserEvent: Boolean = false,
    val tithiName: String = "",
    val tithiNameEn: String = "",
    val isHoliday: Boolean = isSaturday || festivals.any { it.isPublicHoliday }
)

class BsCalendarEngine(
    private val firstYear: Int,
    private val lastYear: Int,
    monthLengthsByYear: Map<Int, IntArray>,
    private val anchorAd: LocalDate,
    anchorWeekdayIndex: Int
) {
    private val lengths = monthLengthsByYear.toSortedMap()
    private val cumulativeDaysBeforeYear = HashMap<Int, Long>()

    init {
        require(firstYear in lengths.keys && lastYear in lengths.keys) {
            "Dataset must include boundary years"
        }
        var running = 0L
        for (y in firstYear..lastYear) {
            cumulativeDaysBeforeYear[y] = running
            running += yearLength(y)
        }
        check(anchorAd.dayOfWeek.let { WeekdayMapper.fromJava(it) } == anchorWeekdayIndex) {
            "Anchor weekday mismatch"
        }
    }

    fun supportedRange(): IntRange = firstYear..lastYear

    fun contains(date: NepaliDate): Boolean =
        date.year in supportedRange() && date.month in 1..12 &&
            date.day in 1..monthLength(date.year, date.month)

    fun monthLength(year: Int, month: Int): Int {
        require(year in supportedRange()) { "BS year $year outside $firstYear..$lastYear" }
        require(month in 1..12) { "BS month must be 1..12" }
        return lengths.getValue(year)[month - 1]
    }

    fun yearLength(year: Int): Int {
        require(year in supportedRange()) { "BS year $year outside range" }
        return lengths.getValue(year).sum()
    }

    fun daysInMonthGrid(year: Int, month: Int): List<Int> = (1..monthLength(year, month)).toList()

    fun bsToAd(date: NepaliDate): LocalDate {
        require(contains(date)) { "Invalid BS date: $date" }
        val offset = cumulativeDaysBeforeYear.getValue(date.year) +
            daysBeforeMonth(date.year, date.month) + (date.day - 1)
        return anchorAd.plusDays(offset)
    }

    fun adToBs(ad: LocalDate): NepaliDate {
        var diff = java.time.temporal.ChronoUnit.DAYS.between(anchorAd, ad)
        require(diff >= 0 && diff < totalSpanDays()) { "AD date $ad outside supported BS range" }
        var year = firstYear
        while (diff >= yearLength(year)) {
            diff -= yearLength(year)
            year++
        }
        var month = 1
        while (diff >= monthLength(year, month)) {
            diff -= monthLength(year, month)
            month++
        }
        return NepaliDate(year, month, diff.toInt() + 1)
    }

    fun weekdayIndexOf(date: NepaliDate): Int =
        WeekdayMapper.fromJava(bsToAd(date).dayOfWeek)

    fun today(todayAd: LocalDate = LocalDate.now()): NepaliDate = adToBs(todayAd)

    fun buildMonthGrid(
        year: Int,
        month: Int,
        todayNp: NepaliDate? = null,
        festivalsByDay: Map<Int, List<Festival>> = emptyMap(),
        userEventDays: Set<Int> = emptySet()
    ): List<List<CalendarCell?>> {
        val total = monthLength(year, month)
        val firstAd = bsToAd(NepaliDate(year, month, 1))
        val leadingBlanks = WeekdayMapper.fromJava(firstAd.dayOfWeek)
        val cells = ArrayList<CalendarCell?>(leadingBlanks + total + trailingPad(total, leadingBlanks))
        repeat(leadingBlanks) { cells.add(null) }
        for (d in 1..total) {
            val np = NepaliDate(year, month, d)
            val ad = bsToAd(np)
            val dayFestivals = festivalsByDay[d] ?: emptyList()
            val isSat = ad.dayOfWeek == DayOfWeek.SATURDAY
            val isHol = isSat || dayFestivals.any { it.isPublicHoliday }
            val panchang = PanchangCalc.compute(ad)
            cells.add(
                CalendarCell(
                    nepaliDate = np,
                    adDate = ad,
                    dayOfMonth = d,
                    weekdayIndex = WeekdayMapper.fromJava(ad.dayOfWeek),
                    isToday = todayNp != null && np == todayNp,
                    isSaturday = isSat,
                    festivals = dayFestivals,
                    hasUserEvent = userEventDays.contains(d),
                    tithiName = panchang.tithiName,
                    tithiNameEn = panchang.tithiNameEn,
                    isHoliday = isHol
                )
            )
        }
        while (cells.size % 7 != 0) cells.add(null)
        return cells.chunked(7)
    }

    private fun trailingPad(total: Int, leading: Int): Int {
        val used = leading + total
        return if (used % 7 == 0) 0 else 7 - (used % 7)
    }

    private fun daysBeforeMonth(year: Int, month: Int): Long {
        var sum = 0L
        for (m in 1 until month) sum += monthLength(year, m)
        return sum
    }

    private fun totalSpanDays(): Long {
        var sum = 0L
        for (y in supportedRange()) sum += yearLength(y)
        return sum
    }
}
