package com.neptools.app.core.backup

import android.content.Context
import com.neptools.app.core.notes.NotesStore
import com.neptools.app.core.reminder.UserEventManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream

data class BackupSummary(
    val habitCount: Int,
    val subscriptionCount: Int,
    val notesCount: Int,
    val eventCount: Int,
    val timestamp: Long
)

object BackupManager {

    private const val BACKUP_VERSION = 1
    private const val APP_IDENTIFIER = "NepTools"

    fun createBackupJson(context: Context): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("appName", APP_IDENTIFIER)
        root.put("timestamp", System.currentTimeMillis())

        // 1. Habits
        val habitPrefs = context.getSharedPreferences("patro_habits_prefs", Context.MODE_PRIVATE)
        val habitsObj = JSONObject()
        for ((k, v) in habitPrefs.all) {
            habitsObj.put(k, v)
        }
        root.put("habits", habitsObj)

        // 2. Subscriptions
        val subPrefs = context.getSharedPreferences("patro_subscriptions_prefs", Context.MODE_PRIVATE)
        val subsObj = JSONObject()
        for ((k, v) in subPrefs.all) {
            subsObj.put(k, v)
        }
        root.put("subscriptions", subsObj)

        // 3. Notes
        val notesFile = File(context.filesDir, "notes_store.json")
        val notesArr = if (notesFile.exists()) {
            try { JSONArray(notesFile.readText()) } catch (_: Exception) { JSONArray() }
        } else JSONArray()
        root.put("notes", notesArr)

        // 4. User Calendar Events
        val eventsFile = File(context.filesDir, "user_calendar_events.json")
        val eventsArr = if (eventsFile.exists()) {
            try { JSONArray(eventsFile.readText()) } catch (_: Exception) { JSONArray() }
        } else JSONArray()
        root.put("userEvents", eventsArr)

        return root.toString(2)
    }

    fun exportToStream(context: Context, outputStream: OutputStream): Boolean {
        return try {
            val json = createBackupJson(context)
            outputStream.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun restoreFromStream(context: Context, inputStream: InputStream): Result<BackupSummary> {
        return try {
            val raw = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(raw)

            val app = root.optString("appName", "")
            if (app != APP_IDENTIFIER && app != "Nepal Patro") {
                return Result.failure(IllegalArgumentException("Invalid backup file: Not a NepTools backup."))
            }

            var habitCount = 0
            var subCount = 0
            var notesCount = 0
            var eventCount = 0

            // Restore Habits
            if (root.has("habits")) {
                val habitsObj = root.getJSONObject("habits")
                val habitPrefs = context.getSharedPreferences("patro_habits_prefs", Context.MODE_PRIVATE)
                val editor = habitPrefs.edit()
                for (key in habitsObj.keys()) {
                    when (val v = habitsObj.get(key)) {
                        is Boolean -> editor.putBoolean(key, v)
                        is Int -> editor.putInt(key, v)
                        is Long -> editor.putLong(key, v)
                        is Float -> editor.putFloat(key, v)
                        is Double -> editor.putFloat(key, v.toFloat())
                        is String -> editor.putString(key, v)
                    }
                }
                editor.apply()
                val listRaw = habitsObj.optString("habits_list", "[]")
                habitCount = try { JSONArray(listRaw).length() } catch (_: Exception) { 0 }
            }

            // Restore Subscriptions
            if (root.has("subscriptions")) {
                val subsObj = root.getJSONObject("subscriptions")
                val subPrefs = context.getSharedPreferences("patro_subscriptions_prefs", Context.MODE_PRIVATE)
                val editor = subPrefs.edit()
                for (key in subsObj.keys()) {
                    when (val v = subsObj.get(key)) {
                        is Boolean -> editor.putBoolean(key, v)
                        is Int -> editor.putInt(key, v)
                        is Long -> editor.putLong(key, v)
                        is Float -> editor.putFloat(key, v)
                        is Double -> editor.putFloat(key, v.toFloat())
                        is String -> editor.putString(key, v)
                    }
                }
                editor.apply()
                val subListRaw = subsObj.optString("subscriptions_list", "[]")
                subCount = try { JSONArray(subListRaw).length() } catch (_: Exception) { 0 }
            }

            // Restore Notes
            if (root.has("notes")) {
                val notesArr = root.getJSONArray("notes")
                File(context.filesDir, "notes_store.json").writeText(notesArr.toString())
                notesCount = notesArr.length()
            }

            // Restore User Events
            if (root.has("userEvents")) {
                val eventsArr = root.getJSONArray("userEvents")
                File(context.filesDir, "user_calendar_events.json").writeText(eventsArr.toString())
                eventCount = eventsArr.length()
            }

            Result.success(
                BackupSummary(
                    habitCount = habitCount,
                    subscriptionCount = subCount,
                    notesCount = notesCount,
                    eventCount = eventCount,
                    timestamp = root.optLong("timestamp", System.currentTimeMillis())
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
