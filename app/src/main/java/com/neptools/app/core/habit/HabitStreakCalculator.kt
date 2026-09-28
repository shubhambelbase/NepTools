package com.neptools.app.core.habit

import java.time.LocalDate

/**
 * Pure streak arithmetic for the habit tracker, split out of
 * [HabitRepository] so it can be unit tested without Android.
 *
 * Semantics:
 *  - A day with no habits scheduled is neutral: it is skipped and never breaks
 *    a streak, so scheduled rest days do not destroy a run.
 *  - A scheduled day counts as active when at least one scheduled habit was
 *    completed.
 *  - Today is only counted once the user has actually logged it. Until then the
 *    streak is still alive but pending, so a streak shown in the morning always
 *    reflects yesterday's work.
 */
object HabitStreakCalculator {

    data class StreakResult(
        val currentStreak: Int,
        val longestStreak: Int,
        val totalActiveDays: Int
    )

    /**
     * @param scheduledCountFor number of habits scheduled on a given weekday
     *        (0 = rest day, i.e. neutral).
     * @param isActiveOn whether at least one scheduled habit was completed on a
     *        given day.
     */
    fun compute(
        today: LocalDate,
        scheduledCountFor: (Int) -> Int,
        isActiveOn: (LocalDate) -> Boolean,
        lookbackDays: Int = 365
    ): StreakResult {
        val weekday = today.dayOfWeek.value % 7
        val todayIsActive = scheduledCountFor(weekday) > 0 && isActiveOn(today)

        var currentStreak = if (todayIsActive) 1 else 0
        var longestStreak = 0
        var tempStreak = 0
        var totalActiveDays = if (todayIsActive) 1 else 0
        var streakBroken = false

        var cursor = today.minusDays(1)
        repeat(lookbackDays) {
            val dayWeekday = cursor.dayOfWeek.value % 7
            when {
                scheduledCountFor(dayWeekday) == 0 -> Unit // neutral rest day
                isActiveOn(cursor) -> {
                    totalActiveDays++
                    tempStreak++
                    if (!streakBroken) {
                        currentStreak++
                    }
                }
                else -> {
                    streakBroken = true
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak
                    }
                    tempStreak = 0
                }
            }
            cursor = cursor.minusDays(1)
        }

        if (tempStreak > longestStreak) {
            longestStreak = tempStreak
        }
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        return StreakResult(currentStreak, longestStreak, totalActiveDays)
    }
}
