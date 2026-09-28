package com.neptools.app

import com.neptools.app.core.habit.HabitStreakCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression tests for habit streak arithmetic.
 *
 * The original in-repository implementation reset a "firstStreakBroken" flag that
 * was never set back to false, and walked backwards from the same date whether or
 * not today had been logged. The visible effect was that a streak read 0 every
 * morning before the user ticked anything, and read 0 forever on any scheduled
 * rest day.
 */
class HabitStreakTest {

    private val today: LocalDate = LocalDate.of(2026, 3, 15)

    private val everyDay: (Int) -> Int = { 1 }

    private fun streakOver(activeDays: Set<LocalDate>, scheduled: (Int) -> Int = everyDay) =
        HabitStreakCalculator.compute(
            today = today,
            scheduledCountFor = scheduled,
            isActiveOn = { it in activeDays }
        )

    /** Ten consecutive completed days ending yesterday, today not yet ticked. */
    private fun tenDayRunEndingYesterday(): Set<LocalDate> =
        (1..10).map { today.minusDays(it.toLong()) }.toSet()

    @Test
    fun `streak survives a day that has not been ticked yet`() {
        val result = streakOver(tenDayRunEndingYesterday())
        assertEquals(10, result.currentStreak)
    }

    @Test
    fun `streak counts today once the user logs it`() {
        val result = streakOver(tenDayRunEndingYesterday() + today)
        assertEquals(11, result.currentStreak)
    }

    @Test
    fun `a missed scheduled day breaks the streak`() {
        // Run of 10 days, but two days ago was missed.
        val active = tenDayRunEndingYesterday() - today.minusDays(2)
        val result = streakOver(active)
        // Yesterday still counts; the eight uninterrupted days before the miss
        // remain the best historical run.
        assertEquals(1, result.currentStreak)
        assertEquals(8, result.longestStreak)
    }

    @Test
    fun `a scheduled rest day is neutral and does not break the streak`() {
        // Habit scheduled every day except Sunday.
        val monToSat: (Int) -> Int = { weekday -> if (weekday == 0) 0 else 1 }
        val window = (1..30).map { today.minusDays(it.toLong()) }
        val active = window.toSet()
        val result = streakOver(active, monToSat)

        val scheduledDays = window.count { it.dayOfWeek.value % 7 != 0 }
        assertEquals(4, window.count { it.dayOfWeek.value % 7 == 0 })
        assertEquals(scheduledDays, result.currentStreak)
        assertEquals(scheduledDays, result.longestStreak)
    }

    @Test
    fun `a brand new user has a zero streak`() {
        val result = streakOver(emptySet())
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
        assertEquals(0, result.totalActiveDays)
    }

    @Test
    fun `total active days counts today only when it is logged`() {
        val active = tenDayRunEndingYesterday()
        assertEquals(10, streakOver(active).totalActiveDays)
        assertEquals(11, streakOver(active + today).totalActiveDays)
    }
}
