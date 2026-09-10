package com.neptools.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.vedic.AshtakootaGunaMilan
import com.neptools.app.astrology.vedic.NakshatraCalc
import com.neptools.app.ui.components.SoftCard
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GunaMilanScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    val savedAstroResult by AstroRepo.result.collectAsState()

    var boyNakIdx by remember { mutableIntStateOf(0) } // Ashwini
    var girlNakIdx by remember { mutableIntStateOf(3) } // Rohini

    var boyRashiIdx by remember(boyNakIdx) {
        mutableIntStateOf(AshtakootaGunaMilan.DEFAULT_RASHI_FOR_NAKSHATRA[boyNakIdx])
    }
    var girlRashiIdx by remember(girlNakIdx) {
        mutableIntStateOf(AshtakootaGunaMilan.DEFAULT_RASHI_FOR_NAKSHATRA[girlNakIdx])
    }

    var boyExp by remember { mutableStateOf(false) }
    var girlExp by remember { mutableStateOf(false) }

    val milan = remember(boyNakIdx, girlNakIdx, boyRashiIdx, girlRashiIdx) {
        AshtakootaGunaMilan.calculate(boyNakIdx, girlNakIdx, boyRashiIdx, girlRashiIdx)
    }

    // Auto-fill option from user's computed natal chart
    val userMoonPos = savedAstroResult?.chart?.positions?.get(Planet.MOON)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isEn) "Vedic Marriage Compatibility (36 Gun)" else "विवाह गुण मिलान (३६ गुण)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            if (isEn) "Authentic Ashta Koota & Dosha Analysis" else "प्रामाणिक अष्टकूट गणना तथा दोष विश्लेषण",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PIcons.ChevronLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick fill banner if user has saved natal chart
            if (userMoonPos != null) {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isEn) "My Saved Chart Detected" else "तपाईंको जन्म कुण्डली भेटियो",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${if (isEn) "Moon: " else "चन्द्र नक्षत्र: "}${userMoonPos.nakshatraName} (${if (isEn) AshtakootaGunaMilan.RASHIS_EN[userMoonPos.signIndex] else AshtakootaGunaMilan.RASHIS_NP[userMoonPos.signIndex]})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .clickable {
                                        boyNakIdx = NakshatraCalc.index(userMoonPos.siderealLon)
                                        boyRashiIdx = userMoonPos.signIndex
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    if (isEn) "Set as Groom" else "वरमा भर्नुहोस्",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                    .clickable {
                                        girlNakIdx = NakshatraCalc.index(userMoonPos.siderealLon)
                                        girlRashiIdx = userMoonPos.signIndex
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    if (isEn) "Set as Bride" else "वधुमा भर्नुहोस्",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Input Selection Card (Boy & Girl)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        if (isEn) "1. Select Birth Stars (Nakshatra)" else "१. जन्म नक्षत्र छान्नुहोस्",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Boy's Selector
                    ExposedDropdownMenuBox(expanded = boyExp, onExpandedChange = { boyExp = !boyExp }) {
                        OutlinedTextField(
                            value = "${if (isEn) AshtakootaGunaMilan.NAKSHATRAS_EN[boyNakIdx] else AshtakootaGunaMilan.NAKSHATRAS_NP[boyNakIdx]} · ${if (isEn) AshtakootaGunaMilan.RASHIS_EN[boyRashiIdx] else AshtakootaGunaMilan.RASHIS_NP[boyRashiIdx]}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isEn) "Groom's Nakshatra & Sign (वर)" else "वरको नक्षत्र र राशि") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boyExp) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = boyExp, onDismissRequest = { boyExp = false }) {
                            AshtakootaGunaMilan.NAKSHATRAS_NP.forEachIndexed { i, npName ->
                                val defaultR = AshtakootaGunaMilan.DEFAULT_RASHI_FOR_NAKSHATRA[i]
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isEn) "${AshtakootaGunaMilan.NAKSHATRAS_EN[i]} (${AshtakootaGunaMilan.RASHIS_EN[defaultR]})"
                                            else "$npName (${AshtakootaGunaMilan.RASHIS_NP[defaultR]})"
                                        )
                                    },
                                    onClick = {
                                        boyNakIdx = i
                                        boyRashiIdx = defaultR
                                        boyExp = false
                                    }
                                )
                            }
                        }
                    }

                    // Girl's Selector
                    ExposedDropdownMenuBox(expanded = girlExp, onExpandedChange = { girlExp = !girlExp }) {
                        OutlinedTextField(
                            value = "${if (isEn) AshtakootaGunaMilan.NAKSHATRAS_EN[girlNakIdx] else AshtakootaGunaMilan.NAKSHATRAS_NP[girlNakIdx]} · ${if (isEn) AshtakootaGunaMilan.RASHIS_EN[girlRashiIdx] else AshtakootaGunaMilan.RASHIS_NP[girlRashiIdx]}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isEn) "Bride's Nakshatra & Sign (वधु)" else "वधुको नक्षत्र र राशि") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = girlExp) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = girlExp, onDismissRequest = { girlExp = false }) {
                            AshtakootaGunaMilan.NAKSHATRAS_NP.forEachIndexed { i, npName ->
                                val defaultR = AshtakootaGunaMilan.DEFAULT_RASHI_FOR_NAKSHATRA[i]
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isEn) "${AshtakootaGunaMilan.NAKSHATRAS_EN[i]} (${AshtakootaGunaMilan.RASHIS_EN[defaultR]})"
                                            else "$npName (${AshtakootaGunaMilan.RASHIS_NP[defaultR]})"
                                        )
                                    },
                                    onClick = {
                                        girlNakIdx = i
                                        girlRashiIdx = defaultR
                                        girlExp = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        if (isEn) "Tip: Vedic marriage matching requires the Moon nakshatra and rashi of both partners."
                        else "सुझाव: गुण मिलान वर र वधु दुवैको चन्द्रमा रहेको नक्षत्र र राशिका आधारमा गरिन्छ।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overall Score & Verdict Hero Card
            val scoreColor = when (milan.verdictLevel) {
                AshtakootaGunaMilan.VerdictLevel.EXCELLENT -> Color(0xFF2E7D32)
                AshtakootaGunaMilan.VerdictLevel.GOOD -> MaterialTheme.colorScheme.primary
                AshtakootaGunaMilan.VerdictLevel.AVERAGE -> Color(0xFFE65100)
                AshtakootaGunaMilan.VerdictLevel.LOW -> MaterialTheme.colorScheme.error
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(scoreColor.copy(alpha = 0.5f)))
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (isEn) "TOTAL COMPATIBILITY SCORE" else "कुल गुण मिलान प्राप्ताङ्क",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                val displayScore = if (milan.totalPoints % 1.0 == 0.0) milan.totalPoints.toInt().toString() else "%.1f".format(milan.totalPoints)
                                Text(
                                    displayScore,
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = scoreColor
                                )
                                Text(
                                    " / 36",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                            }
                        }

                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(scoreColor.copy(alpha = 0.12f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${milan.percentage}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = scoreColor
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (milan.totalPoints / 36.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = scoreColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(scoreColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            if (isEn) milan.verdictEn else milan.verdictNp,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = scoreColor
                        )
                    }
                }
            }

            // Three Critical Doshas Overview
            Text(
                if (isEn) "2. Key Astrological Dosha Checks" else "२. मुख्य ज्योतिषीय दोष परीक्षण",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DoshaBadge(
                    title = if (isEn) "Nadi Dosha" else "नाडी दोष",
                    hasDosha = milan.hasNadiDosha,
                    isEn = isEn,
                    modifier = Modifier.weight(1f)
                )
                DoshaBadge(
                    title = if (isEn) "Bhakoot Dosha" else "भकूट दोष",
                    hasDosha = milan.hasBhakootDosha,
                    isEn = isEn,
                    modifier = Modifier.weight(1f)
                )
                DoshaBadge(
                    title = if (isEn) "Gana Dosha" else "गण दोष",
                    hasDosha = milan.hasGanaDosha,
                    isEn = isEn,
                    modifier = Modifier.weight(1f)
                )
            }

            // Life Domain Breakdown (Simple for normal people)
            Text(
                if (isEn) "3. Life Area Breakdown (Easy View)" else "३. वैवाहिक जीवनका ४ मुख्य पक्षहरू",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            DomainProgressCard(
                title = if (isEn) "Mental & Emotional Harmony" else "मानसिक तथा भावनात्मक तालमेल",
                subtitle = if (isEn) "Maitri + Gana (Temperament & Friendship)" else "ग्रह मैत्री + गण (विचार र स्वभावको सामञ्जस्य)",
                earned = milan.summaryByDomain.mentalHarmony,
                max = 11.0,
                isEn = isEn
            )

            DomainProgressCard(
                title = if (isEn) "Health, Genetics & Children" else "स्वास्थ्य, वंशाणु तथा सन्तान सुख",
                subtitle = if (isEn) "Nadi + Tara (Longevity & Biological Health)" else "नाडी + तारा (दीर्घायु, शारीरिक स्वास्थ्य र सन्तान)",
                earned = milan.summaryByDomain.healthAndProgeny,
                max = 11.0,
                isEn = isEn
            )

            DomainProgressCard(
                title = if (isEn) "Family Prosperity & Love" else "पारिवारिक समृद्धि तथा आर्थिक उन्नति",
                subtitle = if (isEn) "Bhakoot + Vashya + Varna (Growth & Mutual Care)" else "भकूट + वश्य + वर्ण (घरबार, प्रतिष्ठा र आर्थिक स्थायित्व)",
                earned = milan.summaryByDomain.familyAndProsperity,
                max = 10.0,
                isEn = isEn
            )

            DomainProgressCard(
                title = if (isEn) "Physical Compatibility & Warmth" else "शारीरिक आकर्षण तथा अन्तरङ्ग सामञ्जस्य",
                subtitle = if (isEn) "Yoni (Biological & Intimate Harmony)" else "योनि (शारीरिक तालमेल र वैवाहिक आकर्षण)",
                earned = milan.summaryByDomain.physicalCompatibility,
                max = 4.0,
                isEn = isEn
            )

            // 4. All 8 Gunas Detailed Accordion
            Text(
                if (isEn) "4. Detailed Ashta Koota Score (All 8 Gunas)" else "४. अष्टकूटको पूर्ण विवरण (८ वटै गुणहरू)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            milan.kootas.forEach { koota ->
                ExpandableKootaCard(koota = koota, isEn = isEn)
            }

            // Traditional Rules & Guidance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (isEn) "About 36 Guna Milan Tradition" else "३६ गुण मिलान परम्परा बारे जान्नुपर्ने कुरा",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isEn)
                            "• 18+ points is traditionally acceptable for marriage.\n• 28+ points is considered an exceptional and highly auspicious match.\n• Even with doshas, cancellations (Parihara) or strong Jupiter/Venus in Kundali often provide protection.\n• Use this analysis as guidance alongside real mutual understanding and family harmony."
                        else
                            "• परम्परा अनुसार १८ भन्दा बढी गुण आएमा विवाह योग्य मानिन्छ।\n• २८ भन्दा बढी गुण आउनु अति उत्तम र दुर्लभ शुभ योग हो।\n• यदि कुनै दोष देखिए तापनि राशि स्वामीको मित्रता वा कुण्डलीमा शुभ ग्रहको दृष्टिले धेरै दोषहरू स्वतः निष्प्रभावी (परिहार) हुन्छन्।\n• गुण मिलान मार्गदर्शन हो — आपसी विश्वास, समझदारी र संस्कार नै सफल वैवाहिक जीवनका मुख्य आधार हुन्।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DoshaBadge(title: String, hasDosha: Boolean, isEn: Boolean, modifier: Modifier = Modifier) {
    val bg = if (hasDosha) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val fg = if (hasDosha) Color(0xFFC62828) else Color(0xFF2E7D32)
    val text = if (hasDosha) (if (isEn) "Dosha Present" else "दोष उपस्थित") else (if (isEn) "No Dosha" else "दोष छैन (सुरक्षित)")

    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = fg)
            Spacer(Modifier.height(2.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = fg)
        }
    }
}

@Composable
private fun DomainProgressCard(title: String, subtitle: String, earned: Double, max: Double, isEn: Boolean) {
    val pct = (earned / max).toFloat().coerceIn(0f, 1f)
    val color = when {
        pct >= 0.75f -> Color(0xFF2E7D32)
        pct >= 0.50f -> MaterialTheme.colorScheme.primary
        else -> Color(0xFFE65100)
    }

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val displayEarned = if (earned % 1.0 == 0.0) earned.toInt().toString() else "%.1f".format(earned)
                val displayMax = max.toInt().toString()
                Text(
                    "$displayEarned / $displayMax",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun ExpandableKootaCard(koota: AshtakootaGunaMilan.KootaScore, isEn: Boolean) {
    var open by remember { mutableStateOf(false) }
    val isFull = koota.earnedPoints == koota.maxPoints

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { open = !open },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isEn) koota.nameEn else koota.nameNp,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (koota.hasDosha) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (isEn) koota.doshaNameEn ?: "Dosha" else koota.doshaNameNp ?: "दोष",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
                Text(
                    "${if (isEn) "Groom: " else "वर: "}${koota.boyValue}  |  ${if (isEn) "Bride: " else "वधु: "}${koota.girlValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val displayEarned = if (koota.earnedPoints % 1.0 == 0.0) koota.earnedPoints.toInt().toString() else "%.1f".format(koota.earnedPoints)
            val displayMax = koota.maxPoints.toInt().toString()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$displayEarned / $displayMax",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isFull) MaterialTheme.colorScheme.primary else if (koota.earnedPoints > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (open) PIcons.ChevronUp else PIcons.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(visible = open) {
            Column(Modifier.padding(top = 10.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isEn) koota.explanationEn else koota.explanationNp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
