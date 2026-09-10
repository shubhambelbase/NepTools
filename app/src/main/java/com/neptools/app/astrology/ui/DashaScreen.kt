package com.neptools.app.astrology.ui

import com.neptools.app.ui.strings.T

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.data.DashaPeriod
import com.neptools.app.ui.components.EmptyState
import com.neptools.app.ui.components.Meter
import com.neptools.app.ui.components.SoftCard
import java.time.LocalDate

@Composable
fun DashaScreen(onBack: () -> Unit) {
    val result by AstroRepo.result.collectAsState()
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
                    .background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(com.neptools.app.ui.icons.PIcons.ChevronLeft, if (isEn) "Back" else "फिर्ता", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(width = 14.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "Vimshottari Dasha" else "विंशोत्तरी दशा",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (isEn) "Planetary periods & 120-year timeline" else "चालु ग्रह-अवधि र १२० वर्षे जीवन चक्र",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        val r = result
        if (r == null) {
            EmptyState(com.neptools.app.ui.icons.PIcons.Hourglass,
                if (isEn) "No Chart Data" else "डाटा छैन",
                if (isEn) "Fill birth details to calculate dasha timeline" else "जन्म विवरण भरेर कुण्डली बनाउनुहोस्")
            return@Column
        }

        // current phase hero
        SoftCard {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    if (isEn) "Current Planetary Period"
                    else "वर्तमान चालु ग्रह-अवधि",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                val maha = r.currentMaha; val antar = r.currentAntar
                if (maha != null) {
                    Text(
                        "${KundaliTexts.planetName(maha.lord)} ${if (isEn) "Mahadasha" else "महादशा"}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "${KundaliTexts.role(maha.lord)} — ${if (isEn) "Core Life Direction" else "जीवनको मुख्य ऊर्जा"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (antar != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${KundaliTexts.planetName(antar.lord)} ${if (isEn) "Antardasha" else "अन्तर्दशा"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "${KundaliTexts.role(antar.lord)} — ${if (isEn) "Current Daily Focus" else "वर्तमान दैनिक सक्रियता"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Meter(percentElapsed(maha.start, maha.end))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isEn) "Mahadasha Period: ${maha.start.year} to ${maha.end.year}"
                        else "महादशा अवधि: ${maha.start.year} देखि ${maha.end.year} सम्म",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (antar != null) {
                        Spacer(Modifier.height(8.dp))
                        Meter(percentElapsed(antar.start, antar.end),
                            tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isEn) "Antardasha Period: ${antar.start} to ${antar.end}"
                            else "अन्तर्दशा अवधि: ${antar.start} देखि ${antar.end} सम्म",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            if (isEn) "120-Year Life Sequence"
            else "१२० वर्षे महादशा चक्र",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            if (isEn) "The sequence of main planetary chapters throughout your life"
            else "जीवनभर कुन उमेरमा कुन ग्रहको प्रभाव रहन्छ भन्ने पूर्ण समयतालिका",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        r.dashaTreeRoots.forEach { root ->
            val isCurrent = r.currentMaha == root
            SoftCard(
                onClick = { },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 13.dp, vertical = 10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${if (isEn) com.neptools.app.ui.components.PlanetStyle.nameEn(root.lord) else com.neptools.app.ui.components.PlanetStyle.abbr(root.lord)} ${if (isEn) "Mahadasha" else T("maha")}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (isCurrent) {
                                Spacer(Modifier.size(width = 7.dp, height = 0.dp))
                                Box(
                                    Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = .12f), MaterialTheme.shapes.extraSmall)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(if (isEn) "CURRENT" else T("tag_current"), style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Text(if (isEn) "${root.start} → ${root.end}" else "${root.start} देखि ${root.end} सम्म", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(110.dp))
    }
}

private fun percentElapsed(start: LocalDate, end: LocalDate): Int {
    val total = (end.toEpochDay() - start.toEpochDay()).coerceAtLeast(1)
    val done = (LocalDate.now().toEpochDay() - start.toEpochDay()).coerceIn(0, total)
    return (done * 100 / total).toInt()
}
