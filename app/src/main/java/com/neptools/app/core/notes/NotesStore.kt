package com.neptools.app.core.notes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Note(val text: String, val createdAt: Long, val kind: String)

object NotesStore {

    private const val FILE = "notes_store.json"

    fun load(context: Context): List<Note> {
        val f = java.io.File(context.filesDir, FILE)
        if (!f.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(f.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Note(o.getString("text"), o.getLong("at"), o.getString("kind"))
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, note: Note) {
        val all = load(context).toMutableList()
        all.add(0, note)
        val arr = JSONArray()
        all.take(200).forEach { n ->
            arr.put(JSONObject().put("text", n.text).put("at", n.createdAt).put("kind", n.kind))
        }
        java.io.File(context.filesDir, FILE).writeText(arr.toString())
    }

    fun delete(context: Context, createdAt: Long) {
        val all = load(context).filter { it.createdAt != createdAt }
        val arr = JSONArray()
        all.forEach { n ->
            arr.put(JSONObject().put("text", n.text).put("at", n.createdAt).put("kind", n.kind))
        }
        java.io.File(context.filesDir, FILE).writeText(arr.toString())
    }
}
