package com.neptools.app.core.habit

enum class HabitType {
    BOOLEAN, // Yes / No checkbox
    NUMERIC, // Count (e.g. 8 glasses of water, 20 pages)
    TIMER    // Minutes (e.g. 30 min exercise, 15 min meditation)
}

@androidx.compose.runtime.Immutable
data class Habit(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val icon: String, // Icon key or symbol
    val colorHex: Long, // ARGB long color
    val type: HabitType,
    val targetValue: Float = 1f,
    val unitNp: String = "पटक",
    val unitEn: String = "times",
    val frequencyDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6), // 0=Sun..6=Sat
    val reminderTime: String? = null, // "HH:mm"
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@androidx.compose.runtime.Immutable
data class HabitLog(
    val habitId: String,
    val dateIso: String, // "YYYY-MM-DD" in AD
    val value: Float,
    val completed: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@androidx.compose.runtime.Immutable
data class HabitStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalActiveDays: Int,
    val completionRateMonth: Int, // 0-100%
    val totalHabitsCount: Int,
    val todayCompletedCount: Int,
    val todayTotalCount: Int
)

data class DayHeatmapCell(
    val dateIso: String,
    val dayOfMonth: Int,
    val bsYear: Int,
    val bsMonth: Int,
    val bsDay: Int,
    val weekdayIndex: Int, // 0=Sun..6=Sat
    val completionRatio: Float, // 0.0f to 1.0f
    val completedCount: Int,
    val totalCount: Int,
    val isToday: Boolean,
    val isFuture: Boolean
)
