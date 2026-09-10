package com.neptools.app.core.habit

import android.content.Context
import android.content.SharedPreferences
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.data.PatroRepo
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class HabitRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("patro_habits_prefs", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        val migrated = prefs.getBoolean("presets_cleaned_v2", false)
        if (!migrated) {
            // Remove legacy hardcoded prewritten presets
            val raw = prefs.getString(KEY_HABITS, null)
            if (raw != null) {
                try {
                    val arr = JSONArray(raw)
                    val remaining = JSONArray()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val id = obj.optString("id", "")
                        if (!id.startsWith("preset_")) {
                            remaining.put(obj)
                        }
                    }
                    prefs.edit().putString(KEY_HABITS, remaining.toString()).putBoolean("presets_cleaned_v2", true).apply()
                } catch (_: Exception) {
                    prefs.edit().putBoolean("presets_cleaned_v2", true).apply()
                }
            } else {
                prefs.edit().putString(KEY_HABITS, "[]").putBoolean("presets_cleaned_v2", true).apply()
            }
        }
    }

    companion object {
        private const val KEY_HABITS = "habits_list"
        private const val KEY_LOGS = "habits_logs"

        @Volatile
        private var instance: HabitRepository? = null

        fun get(context: Context): HabitRepository {
            return instance ?: synchronized(this) {
                instance ?: HabitRepository(context.applicationContext).also { instance = it }
            }
        }

        val PRESET_HABITS = listOf(
            Habit(
                id = "preset_walk",
                nameNp = "बिहानी हिँडाइ / व्यायाम",
                nameEn = "Morning Walk & Exercise",
                icon = "WALK",
                colorHex = 0xFFC73E2E, // Vermillion
                type = HabitType.TIMER,
                targetValue = 30f,
                unitNp = "मिनेट",
                unitEn = "mins"
            ),
            Habit(
                id = "preset_water",
                nameNp = "दैनिक पर्याप्त पानी",
                nameEn = "Daily 3L Water",
                icon = "WATER",
                colorHex = 0xFF16697A, // Teal Ink
                type = HabitType.NUMERIC,
                targetValue = 8f,
                unitNp = "गिलास",
                unitEn = "glasses"
            ),
            Habit(
                id = "preset_reading",
                nameNp = "पुस्तक अध्ययन",
                nameEn = "Book Reading",
                icon = "READ",
                colorHex = 0xFF4F46E5, // Indigo
                type = HabitType.NUMERIC,
                targetValue = 15f,
                unitNp = "पेज",
                unitEn = "pages"
            ),
            Habit(
                id = "preset_meditation",
                nameNp = "ध्यान तथा प्राणायाम",
                nameEn = "Meditation & Breathwork",
                icon = "ZEN",
                colorHex = 0xFFD97706, // Amber
                type = HabitType.TIMER,
                targetValue = 10f,
                unitNp = "मिनेट",
                unitEn = "mins"
            ),
            Habit(
                id = "preset_diet",
                nameNp = "स्वस्थ खानपान / फलफूल",
                nameEn = "Clean Eating & Fruits",
                icon = "DIET",
                colorHex = 0xFF16A34A, // Emerald Green
                type = HabitType.BOOLEAN,
                targetValue = 1f,
                unitNp = "पटक",
                unitEn = "times"
            ),
            Habit(
                id = "preset_screen",
                nameNp = "सुत्नुअघि स्क्रिन नहेर्ने",
                nameEn = "No Screen Before Bed",
                icon = "FOCUS",
                colorHex = 0xFF9333EA, // Purple
                type = HabitType.BOOLEAN,
                targetValue = 1f,
                unitNp = "पटक",
                unitEn = "times"
            )
        )
    }

    @Synchronized
    fun getHabits(includeArchived: Boolean = false): List<Habit> {
        val raw = prefs.getString(KEY_HABITS, null) ?: return emptyList()
        val list = mutableListOf<Habit>()
        try {
            val jsonArr = JSONArray(raw)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val typeStr = obj.optString("type", HabitType.BOOLEAN.name)
                val type = try {
                    HabitType.valueOf(typeStr)
                } catch (e: Exception) {
                    HabitType.BOOLEAN
                }

                val freqArr = obj.optJSONArray("frequencyDays")
                val freq = mutableListOf<Int>()
                if (freqArr != null) {
                    for (j in 0 until freqArr.length()) {
                        freq.add(freqArr.getInt(j))
                    }
                } else {
                    freq.addAll(listOf(0, 1, 2, 3, 4, 5, 6))
                }

                val habit = Habit(
                    id = obj.getString("id"),
                    nameNp = obj.getString("nameNp"),
                    nameEn = obj.getString("nameEn"),
                    icon = obj.optString("icon", "⭐"),
                    colorHex = obj.optLong("colorHex", 0xFFC73E2E),
                    type = type,
                    targetValue = obj.optDouble("targetValue", 1.0).toFloat(),
                    unitNp = obj.optString("unitNp", "पटक"),
                    unitEn = obj.optString("unitEn", "times"),
                    frequencyDays = freq,
                    reminderTime = if (obj.has("reminderTime") && !obj.isNull("reminderTime")) obj.getString("reminderTime") else null,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    isArchived = obj.optBoolean("isArchived", false)
                )
                if (includeArchived || !habit.isArchived) {
                    list.add(habit)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    @Synchronized
    fun saveHabit(habit: Habit) {
        val habits = getHabits(includeArchived = true).toMutableList()
        val idx = habits.indexOfFirst { it.id == habit.id }
        if (idx >= 0) {
            habits[idx] = habit
        } else {
            habits.add(habit)
        }
        saveHabitsList(habits)
    }

    @Synchronized
    fun deleteHabit(id: String) {
        val habits = getHabits(includeArchived = true).filter { it.id != id }
        saveHabitsList(habits)

        // Also clean logs
        try {
            val rawLogs = prefs.getString(KEY_LOGS, null)
            if (rawLogs != null) {
                val json = JSONObject(rawLogs)
                json.remove(id)
                prefs.edit().putString(KEY_LOGS, json.toString()).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveHabitsList(habits: List<Habit>) {
        val jsonArr = JSONArray()
        habits.forEach { habit ->
            val obj = JSONObject().apply {
                put("id", habit.id)
                put("nameNp", habit.nameNp)
                put("nameEn", habit.nameEn)
                put("icon", habit.icon)
                put("colorHex", habit.colorHex)
                put("type", habit.type.name)
                put("targetValue", habit.targetValue.toDouble())
                put("unitNp", habit.unitNp)
                put("unitEn", habit.unitEn)
                put("frequencyDays", JSONArray(habit.frequencyDays))
                put("reminderTime", habit.reminderTime ?: JSONObject.NULL)
                put("createdAt", habit.createdAt)
                put("isArchived", habit.isArchived)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_HABITS, jsonArr.toString()).apply()
    }

    @Synchronized
    fun getLog(habitId: String, dateIso: String): HabitLog? {
        try {
            val raw = prefs.getString(KEY_LOGS, null) ?: return null
            val root = JSONObject(raw)
            if (!root.has(habitId)) return null
            val habitLogs = root.getJSONObject(habitId)
            if (!habitLogs.has(dateIso)) return null
            val obj = habitLogs.getJSONObject(dateIso)
            return HabitLog(
                habitId = habitId,
                dateIso = dateIso,
                value = obj.optDouble("value", 0.0).toFloat(),
                completed = obj.optBoolean("completed", false),
                updatedAt = obj.optLong("updatedAt", 0L)
            )
        } catch (e: Exception) {
            return null
        }
    }

    @Synchronized
    fun getLogsForDate(dateIso: String): Map<String, HabitLog> {
        val result = mutableMapOf<String, HabitLog>()
        try {
            val raw = prefs.getString(KEY_LOGS, null) ?: return emptyMap()
            val root = JSONObject(raw)
            val keys = root.keys()
            while (keys.hasNext()) {
                val habitId = keys.next()
                val habitLogs = root.getJSONObject(habitId)
                if (habitLogs.has(dateIso)) {
                    val obj = habitLogs.getJSONObject(dateIso)
                    result[habitId] = HabitLog(
                        habitId = habitId,
                        dateIso = dateIso,
                        value = obj.optDouble("value", 0.0).toFloat(),
                        completed = obj.optBoolean("completed", false),
                        updatedAt = obj.optLong("updatedAt", 0L)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @Synchronized
    fun getLogsForHabit(habitId: String): Map<String, HabitLog> {
        val result = mutableMapOf<String, HabitLog>()
        try {
            val raw = prefs.getString(KEY_LOGS, null) ?: return emptyMap()
            val root = JSONObject(raw)
            if (!root.has(habitId)) return emptyMap()
            val habitLogs = root.getJSONObject(habitId)
            val dates = habitLogs.keys()
            while (dates.hasNext()) {
                val dateIso = dates.next()
                val obj = habitLogs.getJSONObject(dateIso)
                result[dateIso] = HabitLog(
                    habitId = habitId,
                    dateIso = dateIso,
                    value = obj.optDouble("value", 0.0).toFloat(),
                    completed = obj.optBoolean("completed", false),
                    updatedAt = obj.optLong("updatedAt", 0L)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @Synchronized
    fun setHabitLog(habitId: String, dateIso: String, value: Float, completed: Boolean) {
        try {
            val raw = prefs.getString(KEY_LOGS, null)
            val root = if (raw != null) JSONObject(raw) else JSONObject()
            val habitLogs = if (root.has(habitId)) root.getJSONObject(habitId) else JSONObject()

            if (value <= 0f && !completed) {
                habitLogs.remove(dateIso)
            } else {
                val obj = JSONObject().apply {
                    put("value", value.toDouble())
                    put("completed", completed)
                    put("updatedAt", System.currentTimeMillis())
                }
                habitLogs.put(dateIso, obj)
            }
            root.put(habitId, habitLogs)
            prefs.edit().putString(KEY_LOGS, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun toggleHabitToday(habitId: String, dateIso: String = LocalDate.now().toString()): Boolean {
        val habits = getHabits()
        val habit = habits.firstOrNull { it.id == habitId } ?: return false
        val existing = getLog(habitId, dateIso)
        val nowDone = !(existing?.completed ?: false)
        val newValue = if (nowDone) habit.targetValue else 0f
        setHabitLog(habitId, dateIso, newValue, nowDone)
        return nowDone
    }

    @Synchronized
    fun updateHabitValue(habitId: String, dateIso: String, newValue: Float) {
        val habits = getHabits()
        val habit = habits.firstOrNull { it.id == habitId } ?: return
        val completed = newValue >= habit.targetValue
        setHabitLog(habitId, dateIso, newValue, completed)
    }

    /**
     * Builds the full annual heatmap cells.
     */
    fun buildHeatmap(
        isBs: Boolean,
        targetYear: Int,
        habitFilterId: String? = null
    ): List<DayHeatmapCell> {
        val engine = PatroRepo.d.engine
        val todayAd = LocalDate.now()
        val todayIso = todayAd.toString()
        val habits = getHabits().filter { habitFilterId == null || it.id == habitFilterId }
        val cells = mutableListOf<DayHeatmapCell>()

        if (habits.isEmpty()) return emptyList()

        if (isBs) {
            val year = targetYear.coerceIn(engine.supportedRange())
            for (m in 1..12) {
                val mLen = engine.monthLength(year, m)
                for (d in 1..mLen) {
                    val nepDate = NepaliDate(year, m, d)
                    val adDate = engine.bsToAd(nepDate)
                    val iso = adDate.toString()
                    val weekday = engine.weekdayIndexOf(nepDate)

                    val logs = getLogsForDate(iso)
                    var completedCount = 0
                    var eligibleCount = 0

                    habits.forEach { habit ->
                        if (habit.frequencyDays.contains(weekday)) {
                            eligibleCount++
                            val log = logs[habit.id]
                            if (log?.completed == true) {
                                completedCount++
                            }
                        }
                    }

                    val ratio = if (eligibleCount > 0) completedCount.toFloat() / eligibleCount else 0f
                    val isToday = (iso == todayIso)
                    val isFuture = adDate.isAfter(todayAd)

                    cells.add(
                        DayHeatmapCell(
                            dateIso = iso,
                            dayOfMonth = d,
                            bsYear = year,
                            bsMonth = m,
                            bsDay = d,
                            weekdayIndex = weekday,
                            completionRatio = if (isFuture) 0f else ratio,
                            completedCount = completedCount,
                            totalCount = eligibleCount,
                            isToday = isToday,
                            isFuture = isFuture
                        )
                    )
                }
            }
        } else {
            // AD Year
            var curDate = LocalDate.of(targetYear, 1, 1)
            val endDate = LocalDate.of(targetYear, 12, 31)

            while (!curDate.isAfter(endDate)) {
                val iso = curDate.toString()
                val nepDate = try {
                    engine.adToBs(curDate)
                } catch (e: Exception) {
                    NepaliDate(2083, 1, 1)
                }
                val weekday = (curDate.dayOfWeek.value % 7) // 0=Sun..6=Sat

                val logs = getLogsForDate(iso)
                var completedCount = 0
                var eligibleCount = 0

                habits.forEach { habit ->
                    if (habit.frequencyDays.contains(weekday)) {
                        eligibleCount++
                        val log = logs[habit.id]
                        if (log?.completed == true) {
                            completedCount++
                        }
                    }
                }

                val ratio = if (eligibleCount > 0) completedCount.toFloat() / eligibleCount else 0f
                val isToday = (iso == todayIso)
                val isFuture = curDate.isAfter(todayAd)

                cells.add(
                    DayHeatmapCell(
                        dateIso = iso,
                        dayOfMonth = curDate.dayOfMonth,
                        bsYear = nepDate.year,
                        bsMonth = nepDate.month,
                        bsDay = nepDate.day,
                        weekdayIndex = weekday,
                        completionRatio = if (isFuture) 0f else ratio,
                        completedCount = completedCount,
                        totalCount = eligibleCount,
                        isToday = isToday,
                        isFuture = isFuture
                    )
                )
                curDate = curDate.plusDays(1)
            }
        }
        return cells
    }

    /**
     * Compute current streak, best streak, and completion statistics.
     */
    fun computeStats(habitFilterId: String? = null): HabitStats {
        val today = LocalDate.now()
        val habits = getHabits().filter { habitFilterId == null || it.id == habitFilterId }
        if (habits.isEmpty()) {
            return HabitStats(0, 0, 0, 0, 0, 0, 0)
        }

        val todayIso = today.toString()
        val todayLogs = getLogsForDate(todayIso)
        val todayWeekday = (today.dayOfWeek.value % 7)
        var todayEligible = 0
        var todayDone = 0

        habits.forEach { h ->
            if (h.frequencyDays.contains(todayWeekday)) {
                todayEligible++
                if (todayLogs[h.id]?.completed == true) {
                    todayDone++
                }
            }
        }

        // Calculate streaks by walking backwards
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var totalActiveDays = 0

        // Check if today counts or start from yesterday
        val isTodayActive = if (todayEligible > 0) (todayDone > 0) else false
        if (isTodayActive) {
            currentStreak = 1
        }

        var checkDate = if (isTodayActive) today.minusDays(1) else today.minusDays(1)
        var firstStreakBroken = !isTodayActive

        // Look back up to 365 days
        for (i in 1..365) {
            val dateIso = checkDate.toString()
            val weekday = (checkDate.dayOfWeek.value % 7)
            val logs = getLogsForDate(dateIso)

            var elCount = 0
            var dnCount = 0
            habits.forEach { h ->
                if (h.frequencyDays.contains(weekday)) {
                    elCount++
                    if (logs[h.id]?.completed == true) {
                        dnCount++
                    }
                }
            }

            val isActive = if (elCount > 0) (dnCount > 0) else false
            if (isActive) {
                totalActiveDays++
                tempStreak++
                if (!firstStreakBroken) {
                    currentStreak++
                }
            } else {
                firstStreakBroken = true
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
                tempStreak = 0
            }

            checkDate = checkDate.minusDays(1)
        }
        if (tempStreak > longestStreak) {
            longestStreak = tempStreak
        }
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        // Month completion rate (last 30 days)
        var monthTotal = 0
        var monthDone = 0
        var mDate = today
        for (i in 0 until 30) {
            val dateIso = mDate.toString()
            val weekday = (mDate.dayOfWeek.value % 7)
            val logs = getLogsForDate(dateIso)

            habits.forEach { h ->
                if (h.frequencyDays.contains(weekday)) {
                    monthTotal++
                    if (logs[h.id]?.completed == true) {
                        monthDone++
                    }
                }
            }
            mDate = mDate.minusDays(1)
        }

        val monthRate = if (monthTotal > 0) ((monthDone.toFloat() / monthTotal) * 100).toInt() else 0

        return HabitStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalActiveDays = totalActiveDays + (if (isTodayActive) 1 else 0),
            completionRateMonth = monthRate,
            totalHabitsCount = habits.size,
            todayCompletedCount = todayDone,
            todayTotalCount = todayEligible
        )
    }

    fun createNewHabit(
        nameNp: String,
        nameEn: String,
        icon: String,
        colorHex: Long,
        type: HabitType,
        targetValue: Float,
        unitNp: String,
        unitEn: String,
        frequencyDays: List<Int>
    ): Habit {
        val habit = Habit(
            id = "habit_${UUID.randomUUID()}",
            nameNp = nameNp.ifBlank { nameEn },
            nameEn = nameEn.ifBlank { nameNp },
            icon = icon.ifBlank { "🎯" },
            colorHex = colorHex,
            type = type,
            targetValue = targetValue.coerceAtLeast(1f),
            unitNp = unitNp,
            unitEn = unitEn,
            frequencyDays = if (frequencyDays.isEmpty()) listOf(0, 1, 2, 3, 4, 5, 6) else frequencyDays
        )
        saveHabit(habit)
        return habit
    }
}
