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
import com.neptools.app.astrology.data.TransitInfo
import com.neptools.app.astrology.vedic.Signs
import com.neptools.app.ui.components.EmptyState
import com.neptools.app.ui.components.PlanetStyle
import com.neptools.app.ui.components.SoftCard
import com.neptools.app.ui.icons.PIcons

@Composable
fun GocharScreen(onBack: () -> Unit) {
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
                    if (isEn) "Planetary Transits (Gochar)" else "गोचर (ग्रहचाल)",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (isEn) "Live planetary movement & transit influences" else "वर्तमान ग्रहहरूको गति र दैनिक प्रभाव",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            if (isEn)
                "Current live planet positions in the sky and how their energies support or challenge your natal Moon sign."
            else "अहिले अन्तरिक्षमा घुमिरहेका ग्रहहरूको चालले तपाईंको चन्द्र राशिमा पार्ने दैनिक प्रभाव।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        val r = result
        if (r == null) {
            EmptyState(PIcons.SunUp,
                if (isEn) "No Chart Data" else "डाटा छैन",
                if (isEn) "Fill birth details to calculate transits" else "जन्म विवरण भरेर कुण्डली बनाउनुहोस्")
            return@Column
        }

        val favorable = r.transits.filter { it.favorable }
        val caution = r.transits.filter { !it.favorable }

        SectionHeaderGo(if (isEn) "Favorable Transits" else "अनुकूल ग्रहचाल", MaterialTheme.colorScheme.secondary)
        favorable.forEach { TransitCard(it, isEn) }
        Spacer(Modifier.height(14.dp))
        SectionHeaderGo(if (isEn) "Needs Caution & Care" else "ध्यान दिनुपर्ने ग्रहचाल", MaterialTheme.colorScheme.primary)
        caution.forEach { TransitCard(it, isEn) }

        Spacer(Modifier.height(14.dp))
        Text(
            if (isEn)
                "Transits reflect cosmic atmospheric weather — use them mindfully for positive awareness in your daily life."
            else "गोचर भनेको वर्तमान ग्रहस्थिति हो — यसलाई दैनिक दिनचर्या र सजगताका लागि सकारात्मक रूपमा लिनुहोस्।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun SectionHeaderGo(text: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 7.dp)) {
        Box(Modifier.size(8.dp, 8.dp).background(color, CircleShape))
        Spacer(Modifier.size(width = 7.dp, height = 0.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
private fun TransitCard(t: TransitInfo, isEn: Boolean) {
    SoftCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(
        horizontal = 13.dp, vertical = 11.dp)) {
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
                Icon(
                    if (t.favorable) PIcons.CheckCircle else PIcons.Alert,
                    contentDescription = null,
                    tint = if (t.favorable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.size(width = 12.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isEn) PlanetStyle.nameEn(t.planet) else PlanetStyle.abbr(t.planet),
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (t.retrograde) {
                        Spacer(Modifier.size(width = 5.dp, height = 0.dp))
                        Text(if (isEn) "(R)" else "(वक्री)", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${if (isEn) Signs.en[t.signIndex] else Signs.np[t.signIndex]} ${"%.1f°".format(t.degreeInSign)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    t.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (isEn) "Influence in House ${t.natalHouseFromMoon} from Moon sign"
                    else "चन्द्र राशिबाट घर-${t.natalHouseFromMoon} मा प्रभाव",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(Modifier.height(7.dp))
}
