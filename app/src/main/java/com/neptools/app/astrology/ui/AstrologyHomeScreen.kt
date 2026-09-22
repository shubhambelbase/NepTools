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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.vedic.Signs
import com.neptools.app.ui.components.PulsingLoader
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun AstrologyHomeScreen(
    onOpen: (String) -> Unit,
    onBack: () -> Unit
) {
    val birth by AstroRepo.birth.collectAsState()
    val result by AstroRepo.result.collectAsState()
    val busy by AstroRepo.busy.collectAsState()
    val isEn = ThemePrefs.lang.value == "en"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolTopBar(
            title = if (isEn) "Vedic Astrology" else "ज्योतिष तथा कुण्डली",
            subtitle = if (isEn) "Birth chart, planetary periods, gochar & compatibility" else "वैदिक कुण्डली, विंशोत्तरी दशा, गोचर र गुण मिलान",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // BIRTH PROFILE & ASTROLOGICAL PASSPORT CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Vedic Birth Profile" else "जन्म कुण्डली विवरण",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (birth != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (birth != null) (if (isEn) "Profile Set" else "विवरण सुरक्षित")
                                    else (if (isEn) "Not Set" else "विवरण छैन"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (birth != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (birth == null) {
                            Text(
                                text = if (isEn) "Birth date, time, and location are required to calculate your personalized planetary chart, dashas, and daily transits."
                                else "कुण्डली, दशा र गोचर ग्रहगणना गर्न आफ्नो जन्म मिति, समय र स्थान प्रविष्ट गर्नुहोस्।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        } else {
                            val b = birth!!
                            Text(
                                text = if (isEn) "${b.date.dayOfMonth} ${b.date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${b.date.year}, ${b.time}"
                                else "${b.date.year}/${"%02d".format(b.date.monthValue)}/${"%02d".format(b.date.dayOfMonth)}, ${b.time}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = PIcons.Pin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${b.placeLabel} · ${if (isEn) "UTC" else "युटिसी"}${b.tzOffsetHours}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Quick Vedic Identity Pill Row
                            val chart = result?.chart
                            if (chart != null) {
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val lagnaText = if (isEn) Signs.en.getOrElse(chart.lagnaSign) { "Aries" } else Signs.np.getOrElse(chart.lagnaSign) { "मेष" }
                                    AstroTagPill(
                                        label = if (isEn) "Lagna" else "लग्न",
                                        value = lagnaText,
                                        modifier = Modifier.weight(1f)
                                    )

                                    val moonSignIdx = chart.positions[Planet.MOON]?.signIndex ?: 0
                                    val rashiText = if (isEn) Signs.en.getOrElse(moonSignIdx) { "Aries" } else Signs.np.getOrElse(moonSignIdx) { "मेष" }
                                    AstroTagPill(
                                        label = if (isEn) "Rashi" else "राशि",
                                        value = rashiText,
                                        modifier = Modifier.weight(1f)
                                    )

                                    val nakshatra = chart.moonNakshatra.split(" ").firstOrNull() ?: chart.moonNakshatra
                                    AstroTagPill(
                                        label = if (isEn) "Nakshatra" else "नक्षत्र",
                                        value = nakshatra,
                                        modifier = Modifier.weight(1f)
                                    )

                                    val currentDashaLord = result?.currentMaha?.lord
                                    if (currentDashaLord != null) {
                                        val dashaName = KundaliTexts.planetName(currentDashaLord)
                                        AstroTagPill(
                                            label = if (isEn) "Dasha" else "दशा",
                                            value = dashaName,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Edit / Enter button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpen("astrology/birth") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = PIcons.Gear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (birth == null) (if (isEn) "Enter Birth Details" else "जन्म विवरण भर्नुहोस्")
                                    else (if (isEn) "Edit Birth Details" else "जन्म विवरण सच्याउनुहोस्"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            if (busy) {
                item {
                    PulsingLoader(if (isEn) "Calculating planetary positions…" else "ग्रहगणना हुँदैछ…")
                }
            }

            // SECTION 1: CHART & PLANETARY PERIODS
            item {
                SectionHeader(if (isEn) "CHART & PLANETARY PERIODS" else "कुण्डली तथा ग्रह-समय")
            }
            item {
                AstroNavCard(
                    title = if (isEn) "Birth Chart (Kundali)" else "जन्म कुण्डली (लग्न तथा नवमांश)",
                    subtitle = if (isEn) "Ascendant, Moon sign, Nakshatra & 9 planetary houses" else "लग्न, चन्द्र राशि, जन्म नक्षत्र तथा १२ भावमा ग्रह स्थिति",
                    icon = PIcons.Calendar,
                    onClick = { onOpen("astrology/kundali") }
                )
            }
            item {
                AstroNavCard(
                    title = if (isEn) "Vimshottari Dasha" else "विंशोत्तरी महादशा तथा अन्तरदशा",
                    subtitle = if (isEn) "Current planetary period & complete 120-year cycle" else "चालु ग्रह समय, प्रभाव र १२० वर्षे जीवन दशा चक्र",
                    icon = PIcons.Hourglass,
                    onClick = { onOpen("astrology/dasha") }
                )
            }
            item {
                AstroNavCard(
                    title = if (isEn) "Gochar Planetary Wheel" else "सजीव गोचर ग्रह चक्र",
                    subtitle = if (isEn) "360° dual-ring live wheel, active aspects & retrograde status" else "३६०° सजीव गोचर चक्र, सक्रिय युति, दृष्टि र वक्री ग्रह",
                    icon = PIcons.SunUp,
                    onClick = { onOpen("astrology/gochar") }
                )
            }

            // SECTION 2: DAILY GUIDANCE & TIMING
            item {
                SectionHeader(if (isEn) "DAILY GUIDANCE & TIMING" else "दैनिक मार्गदर्शन तथा शुभ समय")
            }
            item {
                AstroNavCard(
                    title = if (isEn) "Today's Astrology Analysis" else "आजको ग्रह विश्लेषण",
                    subtitle = if (isEn) "Daily energy, 15 life areas & actionable tips" else "दैनिक ऊर्जा, १५ जीवन-क्षेत्र र व्यावहारिक सुझाव",
                    icon = PIcons.Sparkle,
                    onClick = { onOpen("astrology/analysis") }
                )
            }
            item {
                AstroNavCard(
                    title = if (isEn) "Daily Rashifal (Horoscope)" else "दैनिक तथा मासिक राशिफल",
                    subtitle = if (isEn) "12 zodiac signs daily predictions, lucky numbers & colors" else "१२ राशिको दैनिक भविष्यवाणी, शुभ रंग र अंक",
                    icon = PIcons.Sun,
                    onClick = { onOpen("rashifal") }
                )
            }
            item {
                AstroNavCard(
                    title = if (isEn) "Shubha Muhurat Finder" else "शुभ मुहूर्त खोजक",
                    subtitle = if (isEn) "Marriage, bratabandha, pasni, and griha pravesh dates" else "विवाह, व्रतबन्ध, पास्नी र गृहप्रवेशका शुभ दिनहरू",
                    icon = PIcons.Timer,
                    onClick = { onOpen("muhurat") }
                )
            }

            // SECTION 3: COMPATIBILITY
            item {
                SectionHeader(if (isEn) "COMPATIBILITY" else "वैवाहिक सामञ्जस्य")
            }
            item {
                AstroNavCard(
                    title = if (isEn) "Marriage Compatibility (36 Guna)" else "विवाह गुण मिलान (३६ गुण)",
                    subtitle = if (isEn) "36-point Ashta Koota, Nadi/Bhakoot doshas & compatibility" else "३६ गुण मिलान, नाडी तथा भकूट दोष र वैवाहिक सामञ्जस्य",
                    icon = PIcons.Users,
                    onClick = { onOpen("guna_milan") }
                )
            }

            item {
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun AstroTagPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AstroNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = PIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
