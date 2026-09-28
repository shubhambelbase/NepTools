package com.neptools.app.ui.theme

import androidx.compose.ui.graphics.Color

val RicePaper = Color(0xFFF6F1E6)
val Paper = Color(0xFFFFFDF7)
val Parchment = Color(0xFFEFE9DA)
val Ink = Color(0xFF22201B)
val InkSoft = Color(0xFF4A463D)
val Vermilion = Color(0xFFC73E2E)
// Material 3 container roles must be opaque: they are the backdrop for
// onContainer text and for tonal surfaces. The previous 10%-alpha value
// composited against the paper background and dropped onPrimaryContainer to
// roughly 4.3:1, below the WCAG AA 4.5:1 floor.
val VermilionContainer = Color(0xFFF9E4DC)
val VermilionSoft = Color(0x1AC73E2E)
val TealInk = Color(0xFF16697A)
// Secondary text. The previous #8D8574 measured 3.6:1 on Paper and 3.25:1 on
// RicePaper, which fails WCAG AA for body text. This value clears 4.5:1 on
// RicePaper, Parchment and Paper, so muted text stays legible outdoors.
val Faded = Color(0xFF6B6455)
val Hairline = Color(0x293C3426)
val OnVermilion = Color(0xFFFDF3EC)
val OnInk = RicePaper

val Color_VermilionNight = Color(0xFFE85D4A)
val Color_TealNight = Color(0xFF38BDF8)
val Color_PaperNight = Color(0xFFEDE5D3)
val Color_NightBg = Color(0xFF0C0F14)
val Color_NightSurface = Color(0xFF141A22)
val Color_NightChip = Color(0xFF1E2530)
val Color_NightText = Color(0xFFF1F5F9)
val Color_NightMuted = Color(0xFF94A3B8)
val Color_NightHairline = Color(0x2E94A3B8)
