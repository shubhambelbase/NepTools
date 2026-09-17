package com.neptools.app.astrology.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.TransitInfo
import com.neptools.app.astrology.vedic.Signs
import com.neptools.app.ui.components.EmptyState
import com.neptools.app.ui.components.PlanetStyle
import com.neptools.app.ui.components.SoftCard
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun GocharScreen(onBack: () -> Unit) {
    val result by AstroRepo.result.collectAsState()
    val isEn = ThemePrefs.lang.value == "en"

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPlanet by remember { mutableStateOf<Planet?>(Planet.JUPITER) }

    // Interactive Time-travel Controller
    var transitDateTime by remember { mutableStateOf(LocalDateTime.now()) }

    val activeTransitsAndConj = remember(transitDateTime, result) {
        val r = result ?: return@remember null
        val custom = AstroRepo.transitsFor(transitDateTime, r.chart.birth.tzOffsetHours)
        if (custom != null) custom else Pair(r.transits, emptyMap())
    }

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
                Icon(
                    PIcons.ChevronLeft,
                    if (isEn) "Back" else "फिर्ता",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "Planetary Transits • Gochar (गोचर)" else "सजीव गोचर ग्रह चक्र",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    if (isEn) "Interactive 360° Dual-Ring Transit Wheel" else "अन्तरक्रियात्मक ३६०° जन्म र गोचर चक्र",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        val r = result
        if (r == null) {
            EmptyState(
                PIcons.SunUp,
                if (isEn) "No Chart Data (विवरण छैन)" else "डाटा छैन",
                if (isEn) "Fill birth details to calculate transits" else "जन्म विवरण भरेर कुण्डली बनाउनुहोस्"
            )
            return@Column
        }

        // Tab Selection: Wheel View vs List View
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        if (isEn) "Interactive Wheel (गोचर चक्र)" else "सजीव गोचर चक्र",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        if (isEn) "Transit List (तालिका)" else "तालिका विवरण",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        val currentTransits = activeTransitsAndConj?.first ?: r.transits
        val currentConjunctions = activeTransitsAndConj?.second ?: emptyMap()

        if (selectedTab == 0) {
            // Interactive 360-Degree Wheel View
            // 1. Time Scrubbing Bar
            TimeControllerBar(
                dateTime = transitDateTime,
                onChangeDateTime = { transitDateTime = it },
                onResetNow = { transitDateTime = LocalDateTime.now() },
                isEn = isEn
            )

            Spacer(Modifier.height(12.dp))

            // 2. Dual-Ring Canvas Wheel
            TransitWheelView(
                chart = r.chart,
                transits = currentTransits,
                selectedPlanet = selectedPlanet,
                onSelectPlanet = { selectedPlanet = it },
                isEn = isEn
            )

            Spacer(Modifier.height(14.dp))

            // 3. Planet Quick Selector
            TransitPlanetSelectorRow(
                selectedPlanet = selectedPlanet,
                onSelect = { selectedPlanet = it },
                isEn = isEn
            )

            Spacer(Modifier.height(14.dp))

            // 4. Inspection Card for Selected Planet
            val selP = selectedPlanet ?: Planet.JUPITER
            val selTransit = currentTransits.firstOrNull { it.planet == selP } ?: currentTransits[0]
            val selNatal = r.chart.positions[selP] ?: r.chart.positions.values.first()
            val conjP = currentConjunctions[selP]

            TransitInspectionCard(
                planet = selP,
                transit = selTransit,
                natalPos = selNatal,
                conjunctionPlanet = conjP,
                isEn = isEn
            )
        } else {
            // Classic Categorized List View
            val favorable = currentTransits.filter { it.favorable }
            val caution = currentTransits.filter { !it.favorable }

            SectionHeaderGo(if (isEn) "Favorable Transits (शुभ गोचर)" else "अनुकूल ग्रहचाल", MaterialTheme.colorScheme.secondary)
            favorable.forEach { TransitCard(it, isEn) }
            Spacer(Modifier.height(14.dp))
            SectionHeaderGo(if (isEn) "Needs Caution & Care (सावधानी)" else "ध्यान दिनुपर्ने ग्रहचाल", MaterialTheme.colorScheme.primary)
            caution.forEach { TransitCard(it, isEn) }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            if (isEn)
                "Transits reflect cosmic planetary weather — use them mindfully for positive awareness in your daily life."
            else "गोचर भनेको वर्तमान ग्रहस्थिति हो — यसलाई दैनिक दिनचर्या र सजगताका लागि सकारात्मक रूपमा लिनुहोस्।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun TimeControllerBar(
    dateTime: LocalDateTime,
    onChangeDateTime: (LocalDateTime) -> Unit,
    onResetNow: () -> Unit,
    isEn: Boolean
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy MMM dd, hh:mm a") }
    val formatted = remember(dateTime) { dateTime.format(formatter) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isEn) "Ephemeris Time View" else "ग्रहचाल समय नियन्त्रक",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatted,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .clickable(onClick = onResetNow)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isEn) "Now" else "अहिले",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Quick Stepper Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TimeStepButton(text = if (isEn) "-1M" else "-१ महिना", modifier = Modifier.weight(1f)) { onChangeDateTime(dateTime.minusMonths(1)) }
            TimeStepButton(text = if (isEn) "-1W" else "-१ हप्ता", modifier = Modifier.weight(1f)) { onChangeDateTime(dateTime.minusWeeks(1)) }
            TimeStepButton(text = if (isEn) "-1D" else "-१ दिन", modifier = Modifier.weight(1f)) { onChangeDateTime(dateTime.minusDays(1)) }
            TimeStepButton(text = if (isEn) "+1D" else "+१ दिन", modifier = Modifier.weight(1f)) { onChangeDateTime(dateTime.plusDays(1)) }
            TimeStepButton(text = if (isEn) "+1W" else "+१ हप्ता", modifier = Modifier.weight(1f)) { onChangeDateTime(dateTime.plusWeeks(1)) }
            TimeStepButton(text = if (isEn) "+1M" else "+१ महिना", modifier = Modifier.weight(1f)) { onChangeDateTime(dateTime.plusMonths(1)) }
        }
    }
}

@Composable
private fun TimeStepButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SectionHeaderGo(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 7.dp)
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
private fun TransitCard(t: TransitInfo, isEn: Boolean) {
    SoftCard(
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .background(
                        (if (t.favorable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                            .copy(alpha = 0.13f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) t.planet.name.take(2) else PlanetStyle.nameNp(t.planet).take(1),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (t.favorable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isEn) "${PlanetStyle.nameEn(t.planet)} (${PlanetStyle.nameNp(t.planet)})" else PlanetStyle.nameNp(t.planet),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "in ${if (isEn) "${Signs.en[t.signIndex]} (${Signs.np[t.signIndex]})" else Signs.np[t.signIndex]} ${"%.1f".format(t.degreeInSign)}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (t.retrograde) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isEn) "(Vakri Rx)" else "(वक्री)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFDC2626)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    if (isEn && t.noteEn.isNotBlank()) "${t.noteEn} (${t.note})" else t.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(Modifier.height(6.dp))
}
