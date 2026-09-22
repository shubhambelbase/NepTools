package com.neptools.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.BsCalendarEngine
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.PanchangCalc
import com.neptools.app.core.calendar.SacredTithiResolver
import com.neptools.app.core.calendar.SolarCalc
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.core.util.PatroGraphicGenerator
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private data class TithiEvent(
    val ad: LocalDate,
    val bsYear: Int,
    val bsMonth: Int,
    val bsDay: Int,
    val tithiNp: String,
    val tithiEn: String,
    val pakshaNp: String,
    val pakshaEn: String,
    val canonicalNameNp: String,
    val canonicalNameEn: String,
    val significanceNp: String,
    val significanceEn: String,
    val isMajor: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkadashiListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"
    val engine = PatroRepo.d.engine
    var filter by remember { mutableStateOf("Ekadashi") } // Ekadashi, Aunsi, Purnima, All
    var selectedYear by remember {
        mutableStateOf(runCatching { engine.today().year }.getOrDefault(engine.supportedRange().first))
    }
    var showRules by remember { mutableStateOf(false) }

    val events = remember(selectedYear) { generateEventsForYear(engine, selectedYear) }
    val filtered = remember(filter, events) {
        when (filter) {
            "Ekadashi" -> events.filter { it.tithiNp == "एकादशी" }
            "Aunsi" -> events.filter { it.tithiNp == "औंसी" }
            "Purnima" -> events.filter { it.tithiNp == "पूर्णिमा" }
            else -> events
        }
    }

    val today = remember { LocalDate.now() }
    val nextUpcoming = remember(filtered, today) {
        filtered.firstOrNull { !it.ad.isBefore(today) }
    }

    Scaffold(
        topBar = {
            ToolTopBar(
                title = if (isEn) "Ekadashi & Sacred Tithis" else "एकादशी तथा पवित्र तिथि",
                subtitle = if (isEn) "Vrata dates, significance & fasting timings" else "व्रत, पारणा समय एवं पवित्र चन्द्र तिथि",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Year Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEn) "CALENDAR YEAR" else "कलेन्डर वर्ष",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isEn) "BS $selectedYear" else "वि.सं. ${NepaliNames.toDevanagari(selectedYear)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val minYear = engine.supportedRange().first
                    val maxYear = engine.supportedRange().last
                    IconButton(
                        onClick = { if (selectedYear > minYear) selectedYear-- },
                        enabled = selectedYear > minYear
                    ) {
                        Icon(PIcons.ChevronLeft, contentDescription = "Previous Year")
                    }
                    IconButton(
                        onClick = { if (selectedYear < maxYear) selectedYear++ },
                        enabled = selectedYear < maxYear
                    ) {
                        Icon(PIcons.ChevronRight, contentDescription = "Next Year")
                    }
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == "Ekadashi",
                    onClick = { filter = "Ekadashi" },
                    label = { Text(if (isEn) "Ekadashi" else "एकादशी") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                FilterChip(
                    selected = filter == "Aunsi",
                    onClick = { filter = "Aunsi" },
                    label = { Text(if (isEn) "Aunsi" else "औंसी") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                FilterChip(
                    selected = filter == "Purnima",
                    onClick = { filter = "Purnima" },
                    label = { Text(if (isEn) "Purnima" else "पूर्णिमा") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                FilterChip(
                    selected = filter == "All",
                    onClick = { filter = "All" },
                    label = { Text(if (isEn) "All" else "सबै") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upcoming Hero Card
                if (nextUpcoming != null) {
                    item(key = "hero_upcoming") {
                        UpcomingHeroCard(
                            event = nextUpcoming,
                            today = today,
                            isEn = isEn,
                            onShare = {
                                val npDate = NepaliDate(nextUpcoming.bsYear, nextUpcoming.bsMonth, nextUpcoming.bsDay)
                                val panchang = PanchangCalc.compute(nextUpcoming.ad)
                                val solar = SolarCalc.compute(nextUpcoming.ad, 27.7172, 85.3240)
                                val fList = PatroRepo.d.festivalsFor(nextUpcoming.bsYear, nextUpcoming.bsMonth)[nextUpcoming.bsDay].orEmpty()
                                val file = PatroGraphicGenerator.createDailyPatroCard(
                                    context = context,
                                    date = npDate,
                                    adDate = nextUpcoming.ad,
                                    panchang = panchang,
                                    solar = solar,
                                    festivals = fList,
                                    isEn = isEn
                                )
                                val title = if (isEn) "${nextUpcoming.canonicalNameEn} - Sacred Observance" else "${nextUpcoming.canonicalNameNp} - पवित्र व्रत"
                                PatroGraphicGenerator.shareCardImage(context, file, title)
                            }
                        )
                    }
                }

                // Fasting & Festival Notification Reminder Card
                item(key = "festival_reminder_switch") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = if (isEn) "Fasting & Parana Reminders" else "व्रत तथा पारणा पूर्व सूचना",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn)
                                        "Evening prior reminder for fasting preparation + Dwadashi Parana timing alert"
                                    else
                                        "अघिल्लो साँझ व्रतको तयारी सूचना तथा द्वादशी बिहान पारणा समय अलर्ट",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = ThemePrefs.festivalNotification.value,
                                onCheckedChange = { ThemePrefs.saveFestivalNotification(context, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }

                // Vrata Rules Accordion Card
                item(key = "vrata_rules") {
                    VrataRulesCard(
                        isEn = isEn,
                        expanded = showRules,
                        onToggle = { showRules = !showRules }
                    )
                }

                // List Header
                item(key = "list_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "SACRED TITHI DATES" else "पवित्र तिथि तालिका",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isEn) "${filtered.size} events" else "${NepaliNames.toDevanagari(filtered.size)} वटा",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Event Cards
                items(filtered, key = { "${it.bsYear}_${it.bsMonth}_${it.bsDay}_${it.tithiEn}" }) { event ->
                    TithiCard(
                        event = event,
                        isToday = event.ad == today,
                        isEn = isEn,
                        onShare = {
                            val npDate = NepaliDate(event.bsYear, event.bsMonth, event.bsDay)
                            val panchang = PanchangCalc.compute(event.ad)
                            val solar = SolarCalc.compute(event.ad, 27.7172, 85.3240)
                            val fList = PatroRepo.d.festivalsFor(event.bsYear, event.bsMonth)[event.bsDay].orEmpty()
                            val file = PatroGraphicGenerator.createDailyPatroCard(
                                context = context,
                                date = npDate,
                                adDate = event.ad,
                                panchang = panchang,
                                solar = solar,
                                festivals = fList,
                                isEn = isEn
                            )
                            val title = if (isEn) "${event.canonicalNameEn} - Sacred Tithi" else "${event.canonicalNameNp} - पवित्र तिथि"
                            PatroGraphicGenerator.shareCardImage(context, file, title)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingHeroCard(
    event: TithiEvent,
    today: LocalDate,
    isEn: Boolean,
    onShare: () -> Unit
) {
    val daysAway = ChronoUnit.DAYS.between(today, event.ad)
    val badgeText = when {
        daysAway == 0L -> if (isEn) "TODAY" else "आज परेको"
        daysAway == 1L -> if (isEn) "TOMORROW" else "भोलि"
        else -> if (isEn) "In $daysAway days" else "${NepaliNames.toDevanagari(daysAway.toInt())} दिन बाँकी"
    }

    val monthName = if (isEn) NepaliNames.monthsEn.getOrElse(event.bsMonth - 1) { "" }
    else NepaliNames.monthsNp.getOrElse(event.bsMonth - 1) { "" }
    val dayDev = if (isEn) event.bsDay.toString() else NepaliNames.toDevanagari(event.bsDay)
    val title = if (isEn) event.canonicalNameEn else event.canonicalNameNp
    val subtitle = if (isEn) event.significanceEn else event.significanceNp
    val paksha = if (isEn) event.pakshaEn else event.pakshaNp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category label + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Text(
                        text = if (isEn) "NEXT SACRED OBSERVANCE" else "आगामी पवित्र व्रत",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.75.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Main Name
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // Info Badges Row: BS Date, AD Date, Paksha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text(
                            text = if (isEn) "NEPALI DATE" else "नेपाली मिति",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$monthName $dayDev",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text(
                            text = if (isEn) "LUNAR PHASE" else "पक्ष / तिथि",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = paksha,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Parana Timing Guidance Strip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        PIcons.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEn)
                            "Parana: Following morning (Dwadashi) after sunrise. Break fast with water & satvik prasad."
                        else
                            "पारणा: भोलिपल्ट (द्वादशी) सूर्योदय पश्चात् जल तथा सात्विक प्रसाद ग्रहण गरी व्रत खोल्नुहोला।",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Share Card Action Button
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Icon(PIcons.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Share Observance Card (WhatsApp / Viber)" else "पवित्र व्रत कार्ड सेयर गर्नुहोस् (व्हाट्सएप / भाइबर)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun VrataRulesCard(
    isEn: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        PIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isEn) "Fasting & Parana Guidelines" else "एकादशी व्रत विधि तथा नियम",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    if (expanded) PIcons.ChevronUp else PIcons.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GuidelineRow(
                        title = if (isEn) "Satvik Diet (Phalahar)" else "फलाहार तथा सात्विक भोजन",
                        desc = if (isEn)
                            "Fresh fruits, milk, curd, nuts, dry fruits, potato, buckwheat and sabudana may be consumed."
                        else
                            "फलफूल, दूध, दही, बदाम, काजु, साबुदाना, कुट्टुको पिठो तथा आलु सेवन गर्न सकिन्छ।"
                    )
                    GuidelineRow(
                        title = if (isEn) "Strict Abstinence" else "अन्न तथा दाल त्याग",
                        desc = if (isEn)
                            "Strictly abstain from rice, lentils, wheat, barley, beans, garlic, onion, and non-satvik foods."
                        else
                            "भात (चामल), दाल, गहुँ, कोदो, भटमास, लसुन, प्याज तथा तामसिक भोजन पूर्ण रूपमा त्याग गर्नुपर्दछ।"
                    )
                    GuidelineRow(
                        title = if (isEn) "Parana Schedule" else "द्वादशी पारणा नियम",
                        desc = if (isEn)
                            "Break the fast on the morning of Dwadashi after local sunrise and puja, before Dwadashi tithi expires."
                        else
                            "द्वादशी तिथिको सूर्योदय पछि पूजा-आराधना सम्पन्न गरी शुभ समयभित्र पारणा गरी व्रत सम्पन्न गरिन्छ।"
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidelineRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(top = 6.dp)
                .size(4.dp)
        ) {}
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TithiCard(
    event: TithiEvent,
    isToday: Boolean,
    isEn: Boolean,
    onShare: () -> Unit
) {
    val monthName = if (isEn) NepaliNames.monthsEn.getOrElse(event.bsMonth - 1) { "" }
    else NepaliNames.monthsNp.getOrElse(event.bsMonth - 1) { "" }
    val dayDev = if (isEn) event.bsDay.toString() else NepaliNames.toDevanagari(event.bsDay)
    val title = if (isEn) event.canonicalNameEn else event.canonicalNameNp
    val subtitle = if (isEn) event.significanceEn else event.significanceNp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(
                if (isToday)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Date Badge Block
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (event.isMajor)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .width(54.dp)
                    .padding(end = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthName.take(4),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dayDev,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (event.pakshaNp.contains("शुक्ल")) (if (isEn) "Shukla" else "शुक्ल")
                        else (if (isEn) "Krishna" else "कृष्ण"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Middle: Canonical Name, Significance & Gregorian Date
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isToday) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = if (isEn) "Today" else "आज",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "AD ${event.ad.format(DateTimeFormatter.ISO_DATE)}  •  ${event.ad.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // Right Share Icon Button
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    PIcons.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun generateEventsForYear(engine: BsCalendarEngine, bsYear: Int): List<TithiEvent> {
    val out = mutableListOf<TithiEvent>()
    for (m in 1..12) {
        val len = runCatching { engine.monthLength(bsYear, m) }.getOrDefault(0)
        for (d in 1..len) {
            val np = NepaliDate(bsYear, m, d)
            val ad = runCatching { engine.bsToAd(np) }.getOrNull() ?: continue
            val p = PanchangCalc.compute(ad)
            val t = p.tithiName
            if (t == "एकादशी" || t == "औंसी" || t == "पूर्णिमा") {
                val isShukla = p.paksha == "शुक्ल पक्ष"
                val info = when (t) {
                    "एकादशी" -> SacredTithiResolver.resolveEkadashi(p.lunarMasaIndex, isShukla)
                    "औंसी" -> SacredTithiResolver.resolveAunsi(p.lunarMasaIndex)
                    "पूर्णिमा" -> SacredTithiResolver.resolvePurnima(p.lunarMasaIndex)
                    else -> SacredTithiResolver.resolveEkadashi(p.lunarMasaIndex, isShukla)
                }
                out.add(
                    TithiEvent(
                        ad = ad,
                        bsYear = bsYear,
                        bsMonth = m,
                        bsDay = d,
                        tithiNp = t,
                        tithiEn = p.tithiNameEn,
                        pakshaNp = p.paksha,
                        pakshaEn = p.pakshaEn,
                        canonicalNameNp = info.canonicalNameNp,
                        canonicalNameEn = info.canonicalNameEn,
                        significanceNp = info.significanceNp,
                        significanceEn = info.significanceEn,
                        isMajor = info.isMajor
                    )
                )
            }
        }
    }
    return out
}
