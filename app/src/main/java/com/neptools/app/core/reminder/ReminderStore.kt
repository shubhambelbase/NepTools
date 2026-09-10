package com.neptools.app.core.reminder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class StoredReminder(
    val id: Int,
    val title: String,
    val triggerAtMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)

object ReminderStore {
    private const val FILE = "reminders.json"

    private fun file(context: Context) = File(context.filesDir, FILE)

    fun all(context: Context): List<StoredReminder> {
        val f = file(context)
        if (!f.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(f.readText())
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                StoredReminder(
                    id = o.getInt("id"),
                    title = o.getString("title"),
                    triggerAtMillis = o.getLong("triggerAtMillis"),
                    createdAt = o.optLong("createdAt", 0L)
                )
            }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, reminder: StoredReminder) {
        val list = all(context).toMutableList()
        list.removeAll { it.id == reminder.id }
        list.add(reminder)
        save(context, list)
    }

    fun remove(context: Context, id: Int) {
        val list = all(context).filterNot { it.id == id }
        save(context, list)
    }

    fun clearPast(context: Context) {
        val now = System.currentTimeMillis()
        val list = all(context).filter { it.triggerAtMillis > now }
        if (list.size != all(context).size) save(context, list)
    }

    private fun save(context: Context, list: List<StoredReminder>) {
        val arr = JSONArray()
        for (r in list) {
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("triggerAtMillis", r.triggerAtMillis)
                put("createdAt", r.createdAt)
            })
        }
        file(context).writeText(arr.toString())
    }

    fun nextId(title: String, triggerAtMillis: Long): Int {
        // Unique stable id from title+time, avoids hash collision of millis alone
        return (title.hashCode() * 31 + triggerAtMillis.hashCode())
    }
}
