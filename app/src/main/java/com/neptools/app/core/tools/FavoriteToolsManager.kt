package com.neptools.app.core.tools

import android.content.Context
import androidx.compose.runtime.mutableStateListOf

object FavoriteToolsManager {
    private const val PREFS = "patro_prefs"
    private const val KEY_FAVORITES = "favorite_tools_routes"

    val favoriteRoutes = mutableStateListOf<String>()
    private var isLoaded = false

    fun load(context: Context) {
        if (isLoaded) return
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = p.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        favoriteRoutes.clear()
        favoriteRoutes.addAll(saved)
        isLoaded = true
    }

    fun toggleFavorite(context: Context, route: String) {
        if (favoriteRoutes.contains(route)) {
            favoriteRoutes.remove(route)
        } else {
            favoriteRoutes.add(route)
        }
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putStringSet(KEY_FAVORITES, favoriteRoutes.toSet()).apply()
    }

    fun isFavorite(route: String): Boolean = favoriteRoutes.contains(route)
}
