package com.neptools.app.astrology.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.data.CategoryScore
import com.neptools.app.ui.components.EmptyState
import com.neptools.app.ui.components.Meter
import com.neptools.app.ui.components.SoftCard
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T

@Composable
fun AnalysisScreen(onBack: () -> Unit, onOpenDasha: () -> Unit) {
    val result by AstroRepo.result.collectAsState()
    var showAll by remember { mutableStateOf(false) }
    var expandedKey by remember { mutableStateOf<String?>(null) }
    var showTimeline by remember { mutableStateOf(false) }
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
                    if (isEn) "Today's Analysis" else "आजको विश्लेषण",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (isEn) "Daily energy, 15 life areas & guidance" else "दैनिक ऊर्जा, १५ जीवन-क्षेत्र र सुझाव",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val r = result
        if (r == null) {
            Spacer(Modifier.height(20.dp))
            EmptyState(PIcons.Sparkle, if (isEn) "No Chart Yet" else "कुण्डली छैन",
                if (isEn) "Enter birth details to begin analysis" else "जन्म विवरण भरेर सुरु गर्नुहोस्")
            return@Column
        }

        val sorted = r.scores.sortedByDescending { it.score }
        val best = sorted.firstOrNull()
        val worst = sorted.lastOrNull()

        // ---------- today highlights ----------
        if (r.todaySummary.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SoftCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Sparkle, null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp))
                        Spacer(Modifier.size(width = 7.dp, height = 0.dp))
                        Text(
                            if (isEn) "Today's Key Highlights"
                            else "आजको मुख्य झलक र सुझाव",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    r.todaySummary.forEach { line ->
                        Row(Modifier.padding(vertical = 3.dp)) {
                            Text("• ", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                            Text(friendly(line, isEn), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // ---------- current phase ----------
        Spacer(Modifier.height(14.dp))
        Text(
            if (isEn) "Your Current Life Chapter"
            else "तपाईंको चालु समय (ग्रह-अवधि)",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            if (isEn)
                "Classic Vimshottari period guiding your current focus — tap for full 120-year timeline"
            else "जीवनको वर्तमान अध्याय र मुख्य ऊर्जा — पूरा १२० वर्षे दशा तालिका हेर्न थिच्नुहोस्",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        val mahaP = r.currentMaha
        val antarP = r.currentAntar
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            PhaseCard(
                title = mahaP?.let { KundaliTexts.planetName(it.lord) } ?: "—",
                subtitle = if (isEn) "Mahadasha (Main Phase)" else "मूल महादशा",
                dates = mahaP?.let { "${it.start.year} – ${it.end.year}" } ?: "",
                onClick = onOpenDasha,
                modifier = Modifier.weight(1f)
            )
            PhaseCard(
                title = antarP?.let { KundaliTexts.planetName(it.lord) } ?: "—",
                subtitle = if (isEn) "Antardasha (Sub Phase)" else "अन्तर्दशा",
                dates = antarP?.let { "${it.start.monthValue}/${it.start.dayOfMonth} – ${it.end.monthValue}/${it.end.dayOfMonth}" } ?: "",
                onClick = onOpenDasha,
                modifier = Modifier.weight(1f)
            )
        }

        // ---------- best / care cards ----------
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            if (best != null) HighlightCard(if (isEn) "Strongest Area" else "सबैभन्दा राम्रो पक्ष", best, Modifier.weight(1f), isEn)
            if (worst != null && worst != best) HighlightCard(if (isEn) "Needs Attention" else "ध्यान दिनुपर्ने पक्ष", worst, Modifier.weight(1f), isEn)
        }
        Spacer(Modifier.height(8.dp))
        if (r.yogas.isNotEmpty()) {
            val y = r.yogas.first()
            Text(
                "✦ ${if (isEn) y.nameEn else y.name} ${if (isEn) "Yoga" else "योग"} — ${y.detail}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        // ---------- all areas ----------
        Spacer(Modifier.height(18.dp))
        Text(if (isEn) "Life Areas (15 Categories)" else "जीवनका विभिन्न पक्षहरू", style = MaterialTheme.typography.titleMedium)
        Text(
            if (isEn) "Tap any area to see supporting factors & daily tips"
            else "सकारात्मक पक्ष र व्यावहारिक सुझाव हेर्न कुनै पनि क्षेत्रमा थिच्नुहोस्",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        val visible = if (showAll) sorted else sorted.take(6)
        visible.forEach { s ->
            ScoreRow(s, expandedKey == s.key, isEn) {
                expandedKey = if (expandedKey == s.key) null else s.key
            }
            Spacer(Modifier.height(7.dp))
        }
        SoftCard(
            onClick = { showAll = !showAll },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            Text(
                if (showAll) (if (isEn) "Show Less ▴" else "कम हेर्नुहोस् ▴")
                else (if (isEn) "Show Remaining ${sorted.size - 6} Areas ▾" else "बाँकी ${sorted.size - 6} क्षेत्र हेर्नुहोस् ▾"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // ---------- timeline collapsed ----------
        Spacer(Modifier.height(16.dp))
        SoftCard(onClick = { showTimeline = !showTimeline }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (isEn) "Next 12 Months Overview" else "आगामी १२ महिनाको झलक", style = MaterialTheme.typography.titleSmall)
                    Text(if (isEn) "Monthly outlook based on transits & dasha" else "ग्रहचाल र दशामा आधारित मासिक अनुमान",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    if (showTimeline) PIcons.ChevronUp else PIcons.ChevronDown, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(visible = showTimeline) {
                Column(Modifier.padding(top = 10.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.height(8.dp))
                    r.timeline.forEach { m ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${monthNp(m.month.monthValue)} ${m.month.year}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(m.ratings.values.joinToString("  ") { it.substringAfter(" · ") },
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            if (isEn) "This analysis is astrological guidance — make final decisions using your own judgment and real circumstances."
            else "यो विश्लेषण ज्योतिषीय गणितमा आधारित व्यक्तिगत मार्गदर्शन हो — अन्तिम निर्णय आफ्नै विवेक र यथार्थ परिस्थितिअनुसार लिनुहोस्।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun PhaseCard(
    title: String,
    subtitle: String,
    dates: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (dates.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(dates, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(5.dp))
            Text(if (com.neptools.app.ui.theme.ThemePrefs.lang.value == "en") "View details ›" else "विवरण हेर्नुहोस् ›", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun HighlightCard(label: String, s: CategoryScore, modifier: Modifier = Modifier, isEn: Boolean = false) {
    SoftCard(modifier) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text("${categoryTitle(s.key, s.labelNp, isEn)} (${s.score}%)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Meter(s.score)
            Spacer(Modifier.height(4.dp))
            val text = (s.positives.firstOrNull() ?: s.challenges.firstOrNull())?.text ?: ""
            Text(
                friendly(text, isEn),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun categoryTitle(key: String, labelNp: String, isEn: Boolean): String = if (isEn) when (key) {
    "overall" -> "Overall Vitality"
    "luck" -> "Luck & Fortune"
    "career" -> "Career & Profession"
    "finance" -> "Wealth & Savings"
    "education" -> "Education & Skills"
    "love" -> "Love & Relationships"
    "marriage" -> "Marriage & Partnerships"
    "family" -> "Family Harmony"
    "health" -> "Health & Fitness"
    "travel" -> "Travel & Exploration"
    "foreign" -> "Foreign Opportunities"
    "property" -> "Assets & Property"
    "children" -> "Children & Creativity"
    "reputation" -> "Social Standing & Fame"
    "growth" -> "Personal Growth & Peace"
    else -> labelNp
} else labelNp

private fun categorySubtitle(key: String, isEn: Boolean): String = if (isEn) when (key) {
    "overall" -> "Daily energy, confidence & vitality"
    "luck" -> "New opportunities & circumstance"
    "career" -> "Workplace achievements & business"
    "finance" -> "Earnings, assets & financial flow"
    "education" -> "Knowledge acquisition & learning"
    "love" -> "Romance, empathy & affection"
    "marriage" -> "Marital happiness & trusted partnerships"
    "family" -> "Domestic peace & family bonding"
    "health" -> "Physical energy & well-being"
    "travel" -> "Movement, journeys & networking"
    "foreign" -> "Foreign travel & international prospects"
    "property" -> "Vehicles, real estate & comforts"
    "children" -> "Children's joy & creative works"
    "reputation" -> "Social respect, status & recognition"
    "growth" -> "Inner reflection & peace of mind"
    else -> "Life domain"
} else when (key) {
    "overall" -> "दैनिक दिनचर्या र आत्मविश्वास"
    "luck" -> "नयाँ अवसर र परिस्थिति"
    "career" -> "कार्यक्षेत्र र व्यवसाय"
    "finance" -> "धन, आम्दानी र बचत"
    "education" -> "अध्ययन र नयाँ सीप"
    "love" -> "प्रेम र समझदारी"
    "marriage" -> "दाम्पत्य सुख र साझेदारी"
    "family" -> "पारिवारिक सुख र मेलमिलाप"
    "health" -> "शारीरिक स्फूर्ति र ऊर्जा"
    "travel" -> "यात्रा र नयाँ सम्पर्क"
    "foreign" -> "विदेश अवसर र सोच"
    "property" -> "घरजग्गा र भौतिक सुविधा"
    "children" -> "सन्तान सुख र सिर्जना"
    "reputation" -> "समाजमा प्रतिष्ठा र सम्मान"
    "growth" -> "आत्म-विकास र मनको शान्ति"
    else -> "जीवनको पक्ष"
}

private fun scoreBadge(score: Int, isEn: Boolean): Pair<String, androidx.compose.ui.graphics.Color> = when {
    score >= 75 -> (if (isEn) "Excellent" else "धेरै राम्रो") to androidx.compose.ui.graphics.Color(0xFF2E7D32)
    score >= 60 -> (if (isEn) "Good" else "राम्रो") to androidx.compose.ui.graphics.Color(0xFF16697A)
    score >= 48 -> (if (isEn) "Moderate" else "सामान्य") to androidx.compose.ui.graphics.Color(0xFF7A7261)
    else -> (if (isEn) "Needs Care" else "सावधानी") to androidx.compose.ui.graphics.Color(0xFFC73E2E)
}

@Composable
private fun ScoreRow(s: CategoryScore, expanded: Boolean, isEn: Boolean, onToggle: () -> Unit) {
    val (badgeText, badgeColor) = scoreBadge(s.score, isEn)
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .clickable(onClick = onToggle)
            .padding(horizontal = 15.dp, vertical = 11.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(categoryTitle(s.key, s.labelNp, isEn), style = MaterialTheme.typography.titleSmall)
                Text(categorySubtitle(s.key, isEn), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .background(badgeColor.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(badgeText, style = MaterialTheme.typography.labelSmall, color = badgeColor)
                }
                Spacer(Modifier.size(width = 8.dp, height = 0.dp))
                Text("${s.score}%", style = MaterialTheme.typography.titleSmall, color = badgeColor)
            }
        }
        Spacer(Modifier.height(6.dp))
        Meter(s.score)
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 9.dp)) {
                if (s.positives.isNotEmpty()) {
                    Text(
                        if (isEn) "Positive Influences:" else "सकारात्मक पक्ष:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(2.dp))
                    s.positives.forEach {
                        Text("• ${friendly(it.text, isEn)}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp))
                    }
                }
                if (s.challenges.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isEn) "Helpful Tips & Precautions:" else "उपयोगी सुझाव:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    s.challenges.forEach {
                        Text("• ${friendly(it.text, isEn)}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp))
                    }
                }
            }
        }
    }
}

// Replace English planet tokens with localized names so users get clear readable text
private fun friendly(text: String, isEn: Boolean): String {
    var out = text
    if (!isEn) {
        val map = mapOf(
            "SUN" to "सूर्य", "MOON" to "चन्द्र", "MARS" to "मंगल",
            "MERCURY" to "बुध", "JUPITER" to "गुरु", "VENUS" to "शुक्र",
            "SATURN" to "शनि", "RAHU" to "राहु", "KETU" to "केतु"
        )
        map.forEach { (en, np) -> out = out.replace(en, np) }
    } else {
        val map = mapOf(
            "SUN" to "Sun", "MOON" to "Moon", "MARS" to "Mars",
            "MERCURY" to "Mercury", "JUPITER" to "Jupiter", "VENUS" to "Venus",
            "SATURN" to "Saturn", "RAHU" to "Rahu", "KETU" to "Ketu"
        )
        map.forEach { (raw, pretty) -> out = out.replace(raw, pretty) }
    }
    out = Regex("\\s*\\(\\d+/100\\)").replace(out, "")
    return out
}

private fun monthNp(m: Int): String =
    if (com.neptools.app.ui.theme.ThemePrefs.lang.value == "en")
        java.time.Month.of(m).name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    else listOf(
    "जनवरी", "फेब्रुअरी", "मार्च", "अप्रिल", "मे", "जुन",
    "जुलाई", "अगस्त", "सेप्टेम्बर", "अक्टोबर", "नोभेम्बर", "डिसेम्बर"
)[m - 1]