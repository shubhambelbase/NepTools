package com.neptools.app.core.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.data.PatroRepo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.ZoneId
import java.util.UUID

@Immutable
data class UserCalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val yearBs: Int,
    val monthBs: Int,
    val dayBs: Int,
    val title: String,
    val note: String = "",
    val hasReminder: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

object UserEventManager {

    private const val FILE = "user_calendar_events.json"

    @Synchronized
    fun getAllEvents(context: Context): List<UserCalendarEvent> {
        val file = File(context.filesDir, FILE)
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            val list = mutableListOf<UserCalendarEvent>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    UserCalendarEvent(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        yearBs = o.getInt("y"),
                        monthBs = o.getInt("m"),
                        dayBs = o.getInt("d"),
                        title = o.getString("title"),
                        note = o.optString("note", ""),
                        hasReminder = o.optBoolean("hasReminder", false),
                        reminderHour = o.optInt("reminderHour", 8),
                        reminderMinute = o.optInt("reminderMinute", 0),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun getEventsForDate(context: Context, yearBs: Int, monthBs: Int, dayBs: Int): List<UserCalendarEvent> {
        return getAllEvents(context).filter { it.yearBs == yearBs && it.monthBs == monthBs && it.dayBs == dayBs }
    }

    @Synchronized
    fun getEventDaysForMonth(context: Context, yearBs: Int, monthBs: Int): Set<Int> {
        return getAllEvents(context)
            .filter { it.yearBs == yearBs && it.monthBs == monthBs }
            .map { it.dayBs }
            .toSet()
    }

    @Synchronized
    fun saveEvent(context: Context, event: UserCalendarEvent) {
        val all = getAllEvents(context).toMutableList()
        val existingIndex = all.indexOfFirst { it.id == event.id }
        if (existingIndex >= 0) {
            all[existingIndex] = event
        } else {
            all.add(0, event)
        }
        persist(context, all)

        if (event.hasReminder) {
            scheduleNotification(context, event)
        } else {
            cancelNotification(context, event.id)
        }
    }

    @Synchronized
    fun deleteEvent(context: Context, eventId: String) {
        val all = getAllEvents(context).filter { it.id != eventId }
        persist(context, all)
        cancelNotification(context, eventId)
    }

    private fun persist(context: Context, list: List<UserCalendarEvent>) {
        try {
            val arr = JSONArray()
            list.forEach { e ->
                arr.put(
                    JSONObject().apply {
                        put("id", e.id)
                        put("y", e.yearBs)
                        put("m", e.monthBs)
                        put("d", e.dayBs)
                        put("title", e.title)
                        put("note", e.note)
                        put("hasReminder", e.hasReminder)
                        put("reminderHour", e.reminderHour)
                        put("reminderMinute", e.reminderMinute)
                        put("createdAt", e.createdAt)
                    }
                )
            }
            File(context.filesDir, FILE).writeText(arr.toString())
        } catch (_: Exception) {
        }
    }

    private fun scheduleNotification(context: Context, event: UserCalendarEvent) {
        try {
            val engine = PatroRepo.d.engine
            val adDate = engine.bsToAd(NepaliDate(event.yearBs, event.monthBs, event.dayBs))
            val zonedDateTime = adDate.atTime(event.reminderHour, event.reminderMinute)
                .atZone(ZoneId.systemDefault())
            val triggerMillis = zonedDateTime.toInstant().toEpochMilli()

            if (triggerMillis <= System.currentTimeMillis()) return

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("event_id", event.id)
                putExtra("event_title", event.title)
                putExtra("event_note", event.note)
            }
            val reqCode = (event.id.hashCode() and 0x7FFFFFFF)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (_: Exception) {
        }
    }

    private fun cancelNotification(context: Context, eventId: String) {
        try {
            val intent = Intent(context, ReminderReceiver::class.java)
            val reqCode = (eventId.hashCode() and 0x7FFFFFFF)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(pendingIntent)
        } catch (_: Exception) {
        }
    }
}
