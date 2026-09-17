package com.neptools.app.astrology.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.ui.components.PulsingLoader
import com.neptools.app.ui.components.SoftCard
import com.neptools.app.ui.icons.PIcons

@Composable
fun AstrologyHomeScreen(
    onOpen: (String) -> Unit,
    onBack: () -> Unit
) {
    val birth by AstroRepo.birth.collectAsState()
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
                    if (isEn) "Vedic Astrology" else "ज्योतिष · कुण्डली",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (isEn) "Vedic chart, planetary periods & transits" else "वैदिक कुण्डली, विंशोत्तरी दशा र गोचर",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        SoftCard {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    if (isEn) "Birth Details" else "जन्म विवरण",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                if (birth == null) {
                    Text(
                        if (isEn) "Birth date, time and place are required to generate your personal birth chart."
                        else "कुण्डली बनाउन समय, मिति र ठेगाना चाहिन्छ।",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        if (isEn) "${birth!!.date.dayOfMonth} ${birth!!.date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${birth!!.date.year}, ${birth!!.time}"
                        else "${birth!!.date.year}/${"%02d".format(birth!!.date.monthValue)}/${"%02d".format(birth!!.date.dayOfMonth)}, ${birth!!.time}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Pin, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp))
                        Spacer(Modifier.size(width = 4.dp, height = 0.dp))
                        Text("${birth!!.placeLabel} · ${if (isEn) "UTC" else "युटिसी"}${birth!!.tzOffsetHours}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.shapes.small)
                        .clickable { onOpen("astrology/birth") }
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(PIcons.Gear, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp))
                    Spacer(Modifier.size(width = 8.dp, height = 0.dp))
                    Text(
                        if (birth == null) (if (isEn) "Enter Birth Details" else "जन्म विवरण भर्नुहोस्")
                        else (if (isEn) "Edit Birth Details" else "विवरण सच्याउनुहोस्"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (busy) {
            Spacer(Modifier.height(16.dp))
            PulsingLoader(if (isEn) "Calculating planetary positions…" else "ग्रहगणना हुँदैछ…")
        }

        Spacer(Modifier.height(18.dp))
        Text(
            if (isEn) "DAILY GUIDANCE" else "दैनिक मार्गदर्शन",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        NavRow(
            title = if (isEn) "Today's Analysis" else "आजको विश्लेषण",
            subtitle = if (isEn) "Daily energy, 15 life areas & actionable tips" else "दैनिक ऊर्जा, १५ जीवन-क्षेत्र र व्यावहारिक सुझाव",
            icon = PIcons.Sparkle
        ) { onOpen("astrology/analysis") }

        Spacer(Modifier.height(14.dp))
        Text(
            if (isEn) "YOUR CHART & PLANETARY PERIODS" else "कुण्डली र समयतालिका",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        NavRow(
            title = if (isEn) "Birth Chart • Janma Kundali (जन्म कुण्डली)" else "जन्म कुण्डली",
            subtitle = if (isEn) "Ascendant, Moon sign, Nakshatra & 9 planets" else "लग्न, चन्द्र राशि, नक्षत्र र ९ ग्रहको स्थिति",
            icon = PIcons.Calendar
        ) { onOpen("astrology/kundali") }
        Spacer(Modifier.height(8.dp))
        NavRow(
            title = if (isEn) "Vimshottari Dasha (विंशोत्तरी दशा)" else "विंशोत्तरी दशा",
            subtitle = if (isEn) "Current planetary period & 120-year cycle" else "चालु ग्रह-समय र १२० वर्षे जीवन चक्र",
            icon = PIcons.Hourglass
        ) { onOpen("astrology/dasha") }
        Spacer(Modifier.height(8.dp))
        NavRow(
            title = if (isEn) "Interactive Gochar Transit Wheel (गोचर चक्र)" else "सजीव गोचर ग्रह चक्र",
            subtitle = if (isEn) "360° dual-ring live wheel, active aspects & retrograde status" else "३६०° दोहोरो गोचर चक्र, सक्रिय युति, दृष्टि र वक्री ग्रह स्थिति",
            icon = PIcons.SunUp
        ) { onOpen("astrology/gochar") }
        Spacer(Modifier.height(8.dp))
        NavRow(
            title = if (isEn) "Marriage Compatibility • Guna Milan (गुण मिलान)" else "विवाह गुण मिलान (३६ गुण)",
            subtitle = if (isEn) "36-point Ashta Koota, Nadi/Bhakoot doshas & compatibility" else "३६ गुण मिलान, नाडी तथा भकूट दोष र वैवाहिक सामञ्जस्य",
            icon = PIcons.Sparkle
        ) { onOpen("guna_milan") }

        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun NavRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    SoftCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.size(width = 13.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(PIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
