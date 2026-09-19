package com.neptools.app.core.tools

import android.content.Context
import androidx.compose.runtime.mutableStateListOf

object RecentToolsManager {
    private const val PREFS = "patro_prefs"
    private const val KEY_RECENTS = "recent_tools_routes"
    private const val MAX_RECENTS = 5

    val recentRoutes = mutableStateListOf<String>()
    private var isLoaded = false

    fun load(context: Context) {
        if (isLoaded) return
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = p.getString(KEY_RECENTS, "") ?: ""
        recentRoutes.clear()
        if (raw.isNotBlank()) {
            val list = raw.split(",").filter { it.isNotBlank() }
            recentRoutes.addAll(list.take(MAX_RECENTS))
        }
        isLoaded = true
    }

    fun recordToolUsed(context: Context, route: String) {
        if (route.isBlank()) return
        recentRoutes.remove(route)
        recentRoutes.add(0, route)
        while (recentRoutes.size > MAX_RECENTS) {
            recentRoutes.removeAt(recentRoutes.lastIndex)
        }
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putString(KEY_RECENTS, recentRoutes.joinToString(",")).apply()
    }

    fun clearRecents(context: Context) {
        recentRoutes.clear()
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().remove(KEY_RECENTS).apply()
    }
}
