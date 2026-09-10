package com.neptools.app.ui.components

import androidx.compose.ui.graphics.Color
import com.neptools.app.astrology.data.Planet
import com.neptools.app.ui.theme.ThemePrefs

object PlanetStyle {
    fun color(p: Planet): Color = when (p) {
        Planet.SUN -> Color(0xFFD97B1F)
        Planet.MOON -> Color(0xFF6E9EC4)
        Planet.MARS -> Color(0xFFC94434)
        Planet.MERCURY -> Color(0xFF3C9673)
        Planet.JUPITER -> Color(0xFFC99A2E)
        Planet.VENUS -> Color(0xFFBE5B92)
        Planet.SATURN -> Color(0xFF51618F)
        Planet.RAHU -> Color(0xFF7A7261)
        Planet.KETU -> Color(0xFF8C6F63)
    }

    fun abbr(p: Planet): String =
        if (ThemePrefs.lang.value == "en") nameEn(p) else nameNp(p)

    fun nameNp(p: Planet): String = when (p) {
        Planet.SUN -> "सूर्य"; Planet.MOON -> "चन्द्र"
        Planet.MARS -> "मंगल"; Planet.MERCURY -> "बुध"
        Planet.JUPITER -> "गुरु"; Planet.VENUS -> "शुक्र"
        Planet.SATURN -> "शनि"; Planet.RAHU -> "राहु"; Planet.KETU -> "केतु"
    }

    fun nameEn(p: Planet): String = when (p) {
        Planet.SUN -> "Sun"; Planet.MOON -> "Moon"
        Planet.MARS -> "Mars"; Planet.MERCURY -> "Mercury"
        Planet.JUPITER -> "Jupiter"; Planet.VENUS -> "Venus"
        Planet.SATURN -> "Saturn"; Planet.RAHU -> "Rahu"; Planet.KETU -> "Ketu"
    }
}
