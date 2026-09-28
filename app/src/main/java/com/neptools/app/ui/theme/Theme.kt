package com.neptools.app.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

private val NewariInkScheme = lightColorScheme(
    primary = Vermilion,
    onPrimary = OnVermilion,
    primaryContainer = VermilionContainer,
    onPrimaryContainer = Vermilion,
    secondary = TealInk,
    onSecondary = Paper,
    secondaryContainer = Parchment,
    onSecondaryContainer = Ink,
    tertiary = Ink,
    onTertiary = RicePaper,
    background = RicePaper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Parchment,
    onSurfaceVariant = Faded,
    outline = Hairline,
    outlineVariant = Hairline
)

private val InkNightScheme = darkColorScheme(
    primary = Color_VermilionNight,
    onPrimary = Color(0xFF2A0C08),
    primaryContainer = Color(0x3DC73E2E),
    onPrimaryContainer = Color(0xFFF3B8AC),
    secondary = Color_TealNight,
    onSecondary = Color(0xFF06181D),
    secondaryContainer = Color(0xFF20343B),
    onSecondaryContainer = Color(0xFFBFD9E0),
    tertiary = Color_PaperNight,
    onTertiary = Ink,
    background = Color_NightBg,
    onBackground = Color_NightText,
    surface = Color_NightSurface,
    onSurface = Color_NightText,
    surfaceVariant = Color_NightChip,
    onSurfaceVariant = Color_NightMuted,
    outline = Color_NightHairline,
    outlineVariant = Color_NightHairline
)

/** How the app resolves light vs dark. */
enum class ThemeMode(val stored: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun from(stored: String?): ThemeMode =
            entries.firstOrNull { it.stored == stored } ?: System
    }
}

object ThemePrefs {
    val darkTheme: MutableState<Boolean> = mutableStateOf(false)
    val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.System)
    val nepaliDigits: MutableState<Boolean> = mutableStateOf(true)
    val lang: MutableState<String> = mutableStateOf("np")
    val dailyDateNotification: MutableState<Boolean> = mutableStateOf(true)
    val habitNotification: MutableState<Boolean> = mutableStateOf(true)
    val subNotification: MutableState<Boolean> = mutableStateOf(true)
    val weatherNotification: MutableState<Boolean> = mutableStateOf(true)
    val festivalNotification: MutableState<Boolean> = mutableStateOf(true)
    private const val PREFS = "patro_prefs"

    fun load(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedMode = p.getString("theme_mode", null)
        // Migrate the old boolean toggle. Existing users who had explicitly turned
        // dark mode on keep it; everyone else now follows the system.
        themeMode.value = if (storedMode != null) {
            ThemeMode.from(storedMode)
        } else if (p.getBoolean("dark", false)) {
            ThemeMode.Dark
        } else {
            ThemeMode.System
        }
        lang.value = p.getString("lang", null) ?: defaultLang()
        nepaliDigits.value = (lang.value == "np")
        dailyDateNotification.value = p.getBoolean("notif_daily_date", true)
        habitNotification.value = p.getBoolean("notif_habit", true)
        subNotification.value = p.getBoolean("notif_subs", true)
        weatherNotification.value = p.getBoolean("notif_weather", true)
        festivalNotification.value = p.getBoolean("notif_festivals", true)
    }

    /**
     * First run has no stored preference, so follow the device language. An
     * English phone should not open into a Nepali-only UI.
     */
    private fun defaultLang(): String {
        val language = runCatching { java.util.Locale.getDefault().language }.getOrNull()
        return if (language == "ne") "np" else "en"
    }

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        themeMode.value = mode
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", mode.stored)
            .putBoolean("dark", mode == ThemeMode.Dark)
            .apply()
    }

    fun saveDark(context: Context, v: Boolean) {
        saveThemeMode(context, if (v) ThemeMode.Dark else ThemeMode.Light)
    }


    fun saveNpDigits(context: Context, v: Boolean) {
        nepaliDigits.value = v
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("np_digits", v).apply()
        com.neptools.app.core.notification.NepaliDateNotificationManager.updateNotification(context)
    }

    fun saveLang(context: Context, v: String) {
        lang.value = v
        val isNepali = (v == "np")
        nepaliDigits.value = isNepali
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("lang", v)
            .putBoolean("np_digits", isNepali)
            .apply()
        com.neptools.app.core.notification.NepaliDateNotificationManager.updateNotification(context)
    }

    fun saveDailyDateNotification(context: Context, v: Boolean) {
        dailyDateNotification.value = v
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("notif_daily_date", v).apply()
        if (v) {
            com.neptools.app.core.notification.NepaliDateStickyService.start(context)
            com.neptools.app.core.notification.NepaliDateNotificationManager.updateNotification(context)
        } else {
            com.neptools.app.core.notification.NepaliDateStickyService.stop(context)
            com.neptools.app.core.notification.NepaliDateNotificationManager.cancelNotification(context)
        }
    }

    fun saveHabitNotification(context: Context, v: Boolean) {
        habitNotification.value = v
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("notif_habit", v).apply()
    }

    fun saveSubNotification(context: Context, v: Boolean) {
        subNotification.value = v
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("notif_sub", v).apply()
    }

    fun saveWeatherNotification(context: Context, v: Boolean) {
        weatherNotification.value = v
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("notif_weather", v).apply()
    }

    fun saveFestivalNotification(context: Context, v: Boolean) {
        festivalNotification.value = v
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("notif_festivals", v).apply()
    }
}

@Composable
fun NepToolsTheme(
    mode: ThemeMode = ThemePrefs.themeMode.value,
    content: @Composable () -> Unit
) {
    // ThemeMode.System follows the OS setting, so a user on a dark phone gets a
    // dark app without hunting for an in-app toggle. The resolved boolean is
    // mirrored into ThemePrefs.darkTheme for the few call sites that still read it.
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    SideEffect { ThemePrefs.darkTheme.value = dark }

    MaterialTheme(
        colorScheme = if (dark) InkNightScheme else NewariInkScheme,
        typography = PatroTypography,
        shapes = PatroShapes,
        content = content
    )
}

@Deprecated("Use NepToolsTheme instead", ReplaceWith("NepToolsTheme(mode, content)"))
@Composable
fun NepalPatroTheme(dark: Boolean = ThemePrefs.darkTheme.value, content: @Composable () -> Unit) {
    NepToolsTheme(mode = if (dark) ThemeMode.Dark else ThemeMode.Light, content = content)
}
