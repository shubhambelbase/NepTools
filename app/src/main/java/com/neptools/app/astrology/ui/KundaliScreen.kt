package com.neptools.app.astrology.ui

import com.neptools.app.ui.strings.T

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.PlanetPosition
import com.neptools.app.astrology.vedic.NakshatraCalc
import com.neptools.app.astrology.vedic.Signs
import com.neptools.app.ui.components.EmptyState
import com.neptools.app.ui.components.Meter
import com.neptools.app.ui.components.PulsingLoader
import com.neptools.app.ui.components.SoftCard
import com.neptools.app.ui.icons.PIcons

@Composable
fun KundaliScreen(onBack: () -> Unit) {
    val birth by AstroRepo.birth.collectAsState()
    val result by AstroRepo.result.collectAsState()
    val busy by AstroRepo.busy.collectAsState()
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.ChevronLeft, if (isEn) "Back" else "फिर्ता", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(width = 14.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "Birth Chart (Kundali)" else "जन्म कुण्डली",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (isEn) "Vedic birth chart & planetary positions" else "वैदिक जन्म कुण्डली र ९ ग्रहको स्थिति",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        when {
            birth == null -> EmptyState(
                PIcons.Calendar, T("empty_need_birth"), T("empty_need_birth_s"))
            busy && result == null -> PulsingLoader(T("computing"))
            else -> {
                val r = result!!
                val chart = r.chart

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    HighlightCard(
                        label = if (isEn) "Ascendant (Lagna)" else "लग्न",
                        value = KundaliTexts.signName(chart.lagnaSign),
                        meaning = KundaliTexts.signNature(chart.lagnaSign),
                        modifier = Modifier.weight(1f)
                    )
                    val moon = chart.positions.getValue(Planet.MOON)
                    HighlightCard(
                        label = if (isEn) "Moon Sign (Rashi)" else "जन्म राशि",
                        value = KundaliTexts.signName(moon.signIndex),
                        meaning = KundaliTexts.signNature(moon.signIndex),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(9.dp))
                SoftCard {
                    Column {
                        Text(
                            if (isEn) "Birth Star (Nakshatra): ${KundaliTexts.nakshatraOf(chart.positions.getValue(Planet.MOON))}"
                            else "जन्म नक्षत्र: ${chart.moonNakshatra}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(KundaliTexts.nakshatraNote(chart.moonNakshatra),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))
                var showGuide by remember { mutableStateOf(false) }
                SoftCard(onClick = { showGuide = !showGuide }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Info, null, tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(width = 8.dp, height = 0.dp))
                        Text(
                            if (isEn) "How to understand your Kundali?"
                            else "यो कुण्डली कसरी बुझ्ने? (सजिलो मार्गदर्शन)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(if (showGuide) PIcons.ChevronUp else PIcons.ChevronDown, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    }
                    AnimatedVisibility(visible = showGuide) {
                        Column(Modifier.padding(top = 10.dp)) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )
                            Spacer(Modifier.height(8.dp))
                            if (isEn) {
                                GuideLine("Ascendant (Lagna)", "Rising sign at birth — your core identity and mindset")
                                GuideLine("Moon Sign (Rashi)", "Inner mind, emotions and mental tendencies")
                                GuideLine("Houses (1–12)", "Life areas: Wealth (2), Family (4), Creativity (5), Marriage (7), Career (10)…")
                                GuideLine("Planets (Grahas)", "Cosmic guides directing specific strengths and energies")
                                GuideLine("Retrograde (R)", "Intense, deep and internalized planetary influence")
                            } else {
                                GuideLine("लग्न (Ascendant)", "जन्मक्षणमा पूर्वीय क्षितिजको राशि — तपाईंको व्यक्तित्व र दृष्टिकोण")
                                GuideLine("चन्द्र राशि (Moon Sign)", "मन, भावना र विचार गर्ने शैली")
                                GuideLine("घरहरू (१–१२)", "जीवनका १२ पक्ष: धन (२), परिवार (४), प्रेम/शिक्षा (५), विवाह (७), करियर (१०)…")
                                GuideLine("ग्रहहरू (Planets)", "हरेक ग्रहले जीवनको कुनै खास क्षेत्र र क्षमतालाई मार्गदर्शन गर्छ")
                                GuideLine("वक्री (Retrograde)", "ग्रहको गहिरो, अन्तर्मुखी र बलियो प्रभाव")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                var chartMode by remember { mutableStateOf(0) }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (chartMode == 0) (if (isEn) "Lagna Chart (Rashi D1)" else "लग्न कुण्डली (राशी)")
                        else (if (isEn) "Bhava Chalit (Sripati Cusps)" else "भाव चलित कुण्डली (श्रीपति)"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .padding(2.dp)
                    ) {
                        val rashiSelected = chartMode == 0
                        Box(
                            Modifier
                                .background(
                                    if (rashiSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                    CircleShape
                                )
                                .clickable { chartMode = 0 }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (isEn) "Rashi" else "राशी",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (rashiSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            Modifier
                                .background(
                                    if (!rashiSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                    CircleShape
                                )
                                .clickable { chartMode = 1 }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (isEn) "Bhava" else "भाव चलित",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!rashiSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                NorthIndianChart(chart, isBhavaChalit = chartMode == 1)

                Spacer(Modifier.height(16.dp))
                Text(T("planets_ask"), style = MaterialTheme.typography.titleMedium)
                Text(T("planets_sub"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                Planet.SEVEN.forEach { p ->
                    ExpandablePlanet(p, chart)
                    Spacer(Modifier.height(7.dp))
                }
                listOf(Planet.RAHU, Planet.KETU).forEach { p ->
                    ShadowPlanetRow(p, chart)
                    Spacer(Modifier.height(7.dp))
                }

                if (birth?.birthTimeUncertain == true) {
                    Spacer(Modifier.height(4.dp))
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(PIcons.Alert, null, tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(width = 8.dp, height = 0.dp))
                            Text(T("uncertain_warn"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(110.dp))
            }
        }
    }
}

@Composable
private fun HighlightCard(label: String, value: String, meaning: String, modifier: Modifier = Modifier) {
    SoftCard(modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(13.dp)) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(meaning, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GuideLine(term: String, meaning: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("• ", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary)
        Text("$term — $meaning", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExpandablePlanet(p: Planet, chart: NatalChart) {
    val pos = chart.positions.getValue(p)
    var open by remember { mutableStateOf(false) }
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
    SoftCard(onClick = { open = !open }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .background(
                        com.neptools.app.ui.components.PlanetStyle.color(p).copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(KundaliTexts.chartShortCode(p),
                    style = MaterialTheme.typography.labelLarge,
                    color = com.neptools.app.ui.components.PlanetStyle.color(p))
            }
            Spacer(Modifier.size(width = 12.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isEn) com.neptools.app.ui.components.PlanetStyle.nameEn(p) else com.neptools.app.ui.components.PlanetStyle.abbr(p), style = MaterialTheme.typography.titleSmall)
                    if (pos.retrograde) {
                        Spacer(Modifier.size(width = 5.dp, height = 0.dp))
                        Text(if (isEn) "(R)" else "(वक्री)", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    if (isEn) "${Signs.en[pos.signIndex]} · House ${pos.houseFromLagna} (Bhava ${pos.bhavaFromLagna}) · ${KundaliTexts.nakshatraOf(pos)}"
                    else "${Signs.np[pos.signIndex]} · घर ${pos.houseFromLagna} (भाव ${pos.bhavaFromLagna}) · ${KundaliTexts.nakshatraOf(pos)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                if (open) PIcons.ChevronUp else PIcons.ChevronDown,
                null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(visible = open) {
            Column(Modifier.padding(top = 10.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    KundaliTexts.planetLine(p, pos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isEn) "Star: ${KundaliTexts.nakshatraOf(pos)}, Pada ${NakshatraCalc.pada(pos.siderealLon)} · ${"%.2f".format(pos.degreeInSign)}°"
                    else "नक्षत्र: ${pos.nakshatraName}, पदा ${NakshatraCalc.pada(pos.siderealLon)} · ${"%.2f".format(pos.degreeInSign)}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ShadowPlanetRow(p: Planet, chart: NatalChart) {
    val pos = chart.positions.getValue(p)
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
    SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(
        horizontal = 13.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .background(
                        com.neptools.app.ui.components.PlanetStyle.color(p).copy(alpha = 0.13f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(KundaliTexts.chartShortCode(p),
                    style = MaterialTheme.typography.labelSmall,
                    color = com.neptools.app.ui.components.PlanetStyle.color(p))
            }
            Spacer(Modifier.size(width = 11.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "${com.neptools.app.ui.components.PlanetStyle.nameEn(p)} — ${KundaliTexts.role(p)}"
                    else "${KundaliTexts.planetName(p)} — ${KundaliTexts.role(p)}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    if (isEn) "${Signs.en[pos.signIndex]} · House ${pos.houseFromLagna} (Bhava ${pos.bhavaFromLagna}) (${KundaliTexts.houseTheme(pos.houseFromLagna)})"
                    else "${Signs.np[pos.signIndex]} · घर ${pos.houseFromLagna} (भाव ${pos.bhavaFromLagna})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NorthIndianChart(chart: NatalChart, isBhavaChalit: Boolean = false) {
    val lineColor = MaterialTheme.colorScheme.onBackground
    val inkSoft = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(10.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val stroke = 2.dp.toPx()
            fun ln(a: Offset, b: Offset) =
                drawLine(lineColor, a, b, strokeWidth = stroke, cap = StrokeCap.Round)
            // Outer square
            ln(Offset(0f, 0f), Offset(w, 0f)); ln(Offset(w, 0f), Offset(w, h))
            ln(Offset(w, h), Offset(0f, h)); ln(Offset(0f, h), Offset(0f, 0f))
            // Cross diagonals
            ln(Offset(0f, 0f), Offset(w, h)); ln(Offset(w, 0f), Offset(0f, h))
            // Inner diamond
            ln(Offset(w / 2, 0f), Offset(0f, h / 2))
            ln(Offset(w / 2, 0f), Offset(w, h / 2))
            ln(Offset(0f, h / 2), Offset(w / 2, h))
            ln(Offset(w, h / 2), Offset(w / 2, h))
        }
        val lagnaSign = chart.lagnaSign
        for (house in 1..12) {
            val sign = (lagnaSign + house - 1) % 12
            val occupants = if (isBhavaChalit) {
                Planet.NINE.filter { chart.positions.getValue(it).bhavaFromLagna == house }
            } else {
                Planet.NINE.filter { chart.positions.getValue(it).signIndex == sign }
            }
            val label = buildString {
                if (isBhavaChalit) {
                    append(if (isEn) "H$house" else "भाव $house")
                } else {
                    append(if (isEn) Signs.en[sign].take(3) else Signs.np[sign])
                }
                if (house == 1) append(if (isEn) " · Asc" else "·लग्न")
                if (occupants.isNotEmpty()) {
                    append("\n")
                    append(occupants.joinToString(" ") { KundaliTexts.chartShortCode(it) })
                }
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (house == 1) accent else inkSoft,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = MaterialTheme.typography.labelSmall.lineHeight,
                modifier = Modifier.align(alignmentFor(house)).padding(4.dp)
            )
        }
    }
}

private fun alignmentFor(house: Int): Alignment = when (house) {
    1 -> BiasAlignment(0.0f, -0.50f)
    2 -> BiasAlignment(-0.50f, -0.75f)
    3 -> BiasAlignment(-0.75f, -0.50f)
    4 -> BiasAlignment(-0.50f, 0.0f)
    5 -> BiasAlignment(-0.75f, 0.50f)
    6 -> BiasAlignment(-0.50f, 0.75f)
    7 -> BiasAlignment(0.0f, 0.50f)
    8 -> BiasAlignment(0.50f, 0.75f)
    9 -> BiasAlignment(0.75f, 0.50f)
    10 -> BiasAlignment(0.50f, 0.0f)
    11 -> BiasAlignment(0.75f, -0.50f)
    12 -> BiasAlignment(0.50f, -0.75f)
    else -> Alignment.Center
}
