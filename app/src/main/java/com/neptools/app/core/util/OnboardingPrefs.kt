package com.neptools.app.core.util

import android.content.Context

object OnboardingPrefs {
    private const val PREFS = "onboarding_prefs"
    private const val KEY_COMPLETED = "completed_v1"

    fun isCompleted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_COMPLETED, false)
    }

    fun setCompleted(context: Context, completed: Boolean = true) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_COMPLETED, completed).apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_COMPLETED).apply()
    }
}
