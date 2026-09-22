package com.neptools.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.neptools.app.core.calendar.ChoghadiyaEngine
import com.neptools.app.core.calendar.ChoghadiyaNature
import com.neptools.app.core.calendar.ChoghadiyaSlot
import com.neptools.app.core.calendar.ChoghadiyaType
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.PanchangCalc
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private data class MuhuratPurpose(
    val np: String,
    val en: String,
    val avoidTithis: Set<String>,
    val avoidYogas: Set<String>
)

private data class MuhuratResult(
    val ad: LocalDate,
    val bsText: String,
    val panchang: com.neptools.app.core.calendar.Panchang,
    val score: Int,
    val reason: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuhuratFinderScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            ToolTopBar(
                title = if (isEn) "Muhurat & Choghadiya" else "शुभ साइत तथा चौघडिया",
                subtitle = if (isEn) "Vedic auspicious timings & daily Choghadiya" else "वैदिक शुभ साइत तथा दैनिक चौघडिया समय",
                onBack = onBack,
                actions = {
                    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                            .clickable(
                                interactionSource = interaction,
                                indication = androidx.compose.material3.ripple(bounded = true, radius = 18.dp),
                                onClick = { selectedTab = 2 }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PIcons.Info,
                            contentDescription = if (isEn) "Choghadiya Guide" else "चौघडिया निर्देशिका",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            if (isEn) "Live Choghadiya" else "आजको चौघडिया",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            if (isEn) "Muhurat Finder" else "साइत खोजकर्ता",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            if (isEn) "Vedic Guide" else "चौघडिया निर्देशिका",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> ChoghadiyaView(isEn = isEn, onOpenGuide = { selectedTab = 2 })
                1 -> MuhuratFinderView(isEn = isEn)
                2 -> ChoghadiyaGuideView(isEn = isEn)
            }
        }
    }
}

@Composable
private fun ChoghadiyaView(isEn: Boolean, onOpenGuide: () -> Unit) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val today = remember { LocalDate.now() }

    // Live update ticker every 15 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000L)
            currentTime = LocalTime.now()
        }
    }

    val schedule = remember(currentTime.minute) {
        ChoghadiyaEngine.computeSchedule(date = today, nowTime = currentTime)
    }

    var showDaySlots by remember { mutableStateOf(schedule.isCurrentlyDay) }
    val timeFmt = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Kathmandu (27.72° N, 85.32° E)" else "काठमाडौँ (२७.७२° उ, ८५.३२° पू)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (isEn) "Dynamic Solar Muhurat" else "सौर्य समय आधारित",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            // Active Choghadiya Hero Card
            val cur = schedule.currentSlot
            val badgeColor = when (cur?.type?.nature) {
                ChoghadiyaNature.EXCELLENT -> Color(0xFF10B981)
                ChoghadiyaNature.GOOD -> Color(0xFF0EA5E9)
                ChoghadiyaNature.NEUTRAL -> Color(0xFFF59E0B)
                ChoghadiyaNature.INAUSPICIOUS -> Color(0xFFEF4444)
                null -> MaterialTheme.colorScheme.primary
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        badgeColor.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "ACTIVE RIGHT NOW" else "अहिले सक्रिय चौघडिया",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(0.75.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isEn) (cur?.type?.nature?.en ?: "") else (cur?.type?.nature?.np ?: ""),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isEn) (cur?.type?.enName ?: "Unknown") else (cur?.type?.npName ?: "अज्ञात"),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (schedule.isCurrentlyDay) (if (isEn) "Day" else "दिन") else (if (isEn) "Night" else "रात्रि"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (cur != null) {
                            Text(
                                text = if (isEn) "Ruler: ${cur.type.ruler(isEn)}" else "स्वामी: ${cur.type.ruler(isEn)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (cur != null) {
                        val totalMinutes = runCatching {
                            if (cur.end.isAfter(cur.start)) {
                                java.time.temporal.ChronoUnit.MINUTES.between(cur.start, cur.end)
                            } else {
                                val toMidnight = java.time.temporal.ChronoUnit.MINUTES.between(cur.start, LocalTime.MAX) + 1
                                val fromMidnight = java.time.temporal.ChronoUnit.MINUTES.between(LocalTime.MIN, cur.end)
                                toMidnight + fromMidnight
                            }
                        }.getOrDefault(90L).coerceAtLeast(1L)

                        val remMin = cur.remainingMinutes
                        val elapsedMin = (totalMinutes - remMin).coerceIn(0L, totalMinutes)
                        val progress = (elapsedMin.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = badgeColor,
                            trackColor = badgeColor.copy(alpha = 0.15f)
                        )

                        val remStr = if (ThemePrefs.nepaliDigits.value && !isEn) {
                            NepaliNames.toDevanagari(remMin.toInt())
                        } else remMin.toString()
                        val pctElapsedStr = if (ThemePrefs.nepaliDigits.value && !isEn) {
                            NepaliNames.toDevanagari((progress * 100).toInt().toString())
                        } else "${(progress * 100).toInt()}"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cur.start.format(timeFmt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isEn) "Remaining: ${remStr}m (${(progress * 100).toInt()}% elapsed)"
                                       else "बाँकी: $remStr मिनेट ($pctElapsedStr% बित्यो)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeColor
                            )
                            Text(
                                text = cur.end.format(timeFmt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Guidance Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isEn) "Recommended Activities:" else "उपयुक्त कार्यहरू:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) cur.type.activityEn else cur.type.activityNp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (cur.type.avoidNp.isNotEmpty() && cur.type.nature == ChoghadiyaNature.INAUSPICIOUS) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (isEn) "Caution: ${cur.type.avoidEn}" else "सतर्कता: ${cur.type.avoidNp}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }

                        // Quick Guide Link
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenGuide() }
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "How to choose Muhurat? Read Vedic Guide" else "शुभ साइत कसरी छान्ने? वैदिक निर्देशिका हेर्नुहोस्",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                PIcons.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Sunrise / Sunset info footer
                    val dayDurationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(schedule.sunrise, schedule.sunset).coerceAtLeast(0)
                    val dayHours = dayDurationMinutes / 60
                    val dayMins = dayDurationMinutes % 60
                    val hoursStr = if (ThemePrefs.nepaliDigits.value && !isEn) NepaliNames.toDevanagari(dayHours.toInt()) else dayHours.toString()
                    val minsStr = if (ThemePrefs.nepaliDigits.value && !isEn) NepaliNames.toDevanagari(dayMins.toInt()) else dayMins.toString()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) "Sunrise: ${schedule.sunrise.format(timeFmt)}" else "सूर्योदय: ${schedule.sunrise.format(timeFmt)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = if (isEn) "Sunset: ${schedule.sunset.format(timeFmt)}" else "सूर्यास्त: ${schedule.sunset.format(timeFmt)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = if (isEn) "Day: ${dayHours}h ${dayMins}m" else "दिनमान: ${hoursStr}घ ${minsStr}मि",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        item {
            // Day / Night Switch Buttons (Segmented Pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (showDaySlots) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { showDaySlots = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEn) "Day Choghadiya (8)" else "दिनको चौघडिया (८)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (showDaySlots) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (showDaySlots) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (!showDaySlots) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { showDaySlots = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEn) "Night Choghadiya (8)" else "रातको चौघडिया (८)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (!showDaySlots) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (!showDaySlots) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val slots = if (showDaySlots) schedule.daySlots else schedule.nightSlots
        items(slots) { slot ->
            ChoghadiyaSlotRow(slot = slot, isEn = isEn, timeFmt = timeFmt)
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChoghadiyaSlotRow(
    slot: ChoghadiyaSlot,
    isEn: Boolean,
    timeFmt: DateTimeFormatter
) {
    val badgeColor = when (slot.type.nature) {
        ChoghadiyaNature.EXCELLENT -> Color(0xFF10B981)
        ChoghadiyaNature.GOOD -> Color(0xFF0EA5E9)
        ChoghadiyaNature.NEUTRAL -> Color(0xFFF59E0B)
        ChoghadiyaNature.INAUSPICIOUS -> Color(0xFFEF4444)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (slot.isCurrent) 1.5.dp else 0.75.dp,
                color = if (slot.isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEn) slot.type.enName else slot.type.npName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (slot.isCurrent) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isEn) "NOW" else "सक्रिय",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${slot.start.format(timeFmt)} - ${slot.end.format(timeFmt)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (isEn) slot.type.nature.en else slot.type.nature.np,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) slot.type.activityEn else slot.type.activityNp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Ruler: ${slot.type.ruler(isEn)}" else "स्वामी: ${slot.type.ruler(isEn)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuhuratFinderView(isEn: Boolean) {
    val engine = PatroRepo.d.engine
    val purposes = remember {
        listOf(
            MuhuratPurpose("विवाह", "Marriage", setOf("औंसी", "चतुर्दशी"), setOf("व्यतिपात", "वैधृति")),
            MuhuratPurpose("ब्रतबन्ध", "Bratabandha", setOf("औंसी", "पूर्णिमा"), setOf("व्यतिपात")),
            MuhuratPurpose("पास्नी", "Pasni", setOf("औंसी", "चतुर्थी"), setOf("व्यतिपात", "परिघ")),
            MuhuratPurpose("गृह प्रवेश", "Griha Pravesh", setOf("औंसी"), setOf("व्यतिपात", "वैधृति", "परिघ")),
            MuhuratPurpose("व्यापार सुरुवात", "Business Start", setOf("औंसी"), setOf("व्यतिपात"))
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(purposes[0]) }

    val results = remember(selected) {
        val today = LocalDate.now()
        val lastBsYear = engine.supportedRange().last
        val lastBsMonth = 12
        val lastBsDay = runCatching { engine.monthLength(lastBsYear, lastBsMonth) }.getOrDefault(30)
        val lastAd = runCatching {
            engine.bsToAd(com.neptools.app.core.calendar.NepaliDate(lastBsYear, lastBsMonth, lastBsDay))
        }.getOrDefault(today.plusYears(50))
        val maxDays = java.time.temporal.ChronoUnit.DAYS.between(today, lastAd).toInt().coerceAtLeast(365)
        val list = mutableListOf<MuhuratResult>()
        var d = today.plusDays(1)
        var tries = 0
        val target = 20
        while (list.size < target && tries < maxDays) {
            tries++
            val p = PanchangCalc.compute(d)
            val tBad = p.tithiName in selected.avoidTithis
            val yBad = p.yogaName in selected.avoidYogas
            val isSaturday = d.dayOfWeek.value == 6
            val badDay = when (selected.en) {
                "Marriage" -> tBad || yBad || isSaturday
                else -> tBad || yBad
            }
            if (!badDay) {
                val np = runCatching { engine.adToBs(d) }.getOrNull()
                val bsText = np?.let { "${it.year}/${it.month}/${it.day}" } ?: "-"
                val score = if (p.tithiName in listOf("पञ्चमी", "सप्तमी", "दशमी", "एकादशी", "त्रयोदशी")) 95 else 82
                val reason = if (isEn) "${p.tithiNameEn} / ${p.nakshatraNameEn} / ${p.yogaNameEn}" else "${p.tithiName} / ${p.nakshatraName} / ${p.yogaName}"
                list.add(MuhuratResult(d, bsText, p, score, reason))
            }
            d = d.plusDays(1)
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = if (isEn) selected.en else selected.np,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isEn) "Select Ceremony / Occasion" else "कार्य वा उद्देश्य छान्नुहोस्") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    purposes.forEach { purpose ->
                        DropdownMenuItem(
                            text = { Text(if (isEn) purpose.en else purpose.np) },
                            onClick = {
                                selected = purpose
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        items(results) { res ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        0.75.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${res.bsText} BS (${res.ad})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = res.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${res.score}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChoghadiyaGuideView(isEn: Boolean) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            // Overview card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) "What is Vedic Choghadiya?" else "वैदिक चौघडिया के हो?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isEn)
                            "Choghadiya is a classical Vedic muhurat system used to determine auspicious and inauspicious time windows throughout the day and night without requiring a complex horoscope calculation.\n\n• Each day (sunrise to sunset) is divided into 8 equal parts (Day Choghadiyas).\n• Each night (sunset to next sunrise) is divided into 8 equal parts (Night Choghadiyas).\n• 1 Choghadiya corresponds to approximately 3.75 Ghatis (roughly 90 minutes / 1.5 hours).\n• In total, there are 16 Choghadiyas across a 24-hour cycle."
                        else
                            "चौघडिया वैदिक ज्योतिषको अत्यन्तै सरल, लोकप्रिय र प्रभावकारी मुहूर्त प्रणाली हो। कुनै जटिल चिना वा कुण्डली बिना नै दिन तथा रातका शुभ र अशुभ समय निर्धारण गर्न यसको प्रयोग गरिन्छ।\n\n• सूर्योदयदेखि सूर्यास्तसम्मको दिनमानलाई ८ बराबर भागमा विभाजन गरिन्छ (दिन चौघडिया)।\n• सूर्यास्तदेखि भोलिपल्टको सूर्योदयसम्मको रात्रिमानलाई थप ८ भागमा विभाजन गरिन्छ (रात्रि चौघडिया)।\n• १ चौघडिया लगभग ३.७५ घटी (करिब ९० मिनेट वा १ घण्टा ३० मिनेट) को हुन्छ।\n• २४ घण्टाको अहोरात्र चक्रमा कुल १६ चौघडिया (८ दिनको र ८ रातको) हुने गर्छन्।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Practical Selection Guide Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) "How to Choose Auspicious Muhurat (4 Steps)" else "उत्तम साइत कसरी छनौट गर्ने? (४ चरण)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    val steps = if (isEn) listOf(
                        "1. Identify Task Purpose" to "Determine your task: commercial, wedding/spiritual, travel, or medical treatment.",
                        "2. Select Best Choghadiya" to "Prefer Amrit or Shubh for ceremonies; Labh for business & investments; Char for journeys and vehicle purchases.",
                        "3. Avoid Inauspicious Windows" to "Strictly avoid Rog, Kaal, or Udveg when starting critical commitments or signing documents.",
                        "4. Cross-Check with Rahu Kaal" to "Ensure your chosen auspicious slot does not coincide with Rahu Kaal. If it overlaps, initiate after Rahu Kaal passes."
                    ) else listOf(
                        "१. कार्यको प्रकृति पहिचान" to "आफ्नो कार्य कुन वर्गमा पर्छ (व्यापार, नयाँ लगानी, विवाह/धार्मिक, यात्रा, वा स्वास्थ्य उपचार) स्पष्ट हुनुहोस्।",
                        "२. उपयुक्त चौघडिया चयन" to "मांगलिक कार्यमा अमृत वा शुभ, आर्थिक कारोबारमा लाभ, र यात्रा वा सवारीमा चर चौघडिया प्राथमिकता दिनुहोस्।",
                        "३. अशुभ समय त्याग" to "नयाँ सम्झौता, लगानी वा शुभ कार्य थालनी गर्दा रोग, काल वा उद्वेग चौघडिया पूर्ण रूपमा त्याग्नुहोस्।",
                        "४. राहु कालको सतर्कता" to "छानिएको शुभ चौघडिया राहु कालसँग जुध्न नदिनुहोस्। यदि जुधेको भए राहु काल सकिएपछिको समय सदुपयोग गर्नुहोस्।"
                    )

                    steps.forEach { (stepTitle, stepDesc) ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stepTitle,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stepDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (isEn) "The 7 Choghadiya Types & Activities" else "७ प्रकारका चौघडिया र तिनका प्रभाव",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(ChoghadiyaType.entries) { type ->
            val natureColor = when (type.nature) {
                ChoghadiyaNature.EXCELLENT -> Color(0xFF10B981)
                ChoghadiyaNature.GOOD -> Color(0xFF0EA5E9)
                ChoghadiyaNature.NEUTRAL -> Color(0xFFF59E0B)
                ChoghadiyaNature.INAUSPICIOUS -> Color(0xFFEF4444)
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.75.dp, natureColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(natureColor)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isEn) type.enName else type.npName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(natureColor.copy(alpha = 0.12f))
                                .border(0.5.dp, natureColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isEn) type.nature.en else type.nature.np,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = natureColor
                            )
                        }
                    }

                    Text(
                        text = if (isEn) "Planetary Ruler: ${type.ruler(isEn)}" else "स्वामी ग्रह: ${type.ruler(isEn)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isEn) "Recommended For:" else "उपयुक्त कार्य (के गर्ने?):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) type.activityEn else type.activityNp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (type.avoidNp.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isEn) "Avoid / Caution:" else "सतर्कता (के नगर्ने?):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (type.nature == ChoghadiyaNature.INAUSPICIOUS) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) type.avoidEn else type.avoidNp,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (type.nature == ChoghadiyaNature.INAUSPICIOUS) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            // Rahu Kaal Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) "Rahu Kaal & Choghadiya Precedence" else "राहु काल र चौघडियाको शास्त्रीय नियम",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isEn)
                            "• Rahu Kaal is a 90-minute daily planetary window ruled by Rahu, considered inauspicious for initiating new undertakings.\n• Rule of Precedence: If an auspicious Choghadiya (such as Amrit or Shubh) coincides with Rahu Kaal, starting major auspicious work should be paused until Rahu Kaal concludes.\n• Ongoing, essential, or routine work can continue without impediment."
                        else
                            "• प्रत्येक दिन करिब ९० मिनेटको समय राहुको प्रभाव रहने 'राहु काल' हुन्छ। यो समय नयाँ काम शुभारम्भका लागि त्याज्य मानिन्छ।\n• शास्त्रीय मर्यादा: यदि कुनै समय शुभ वा अमृत चौघडिया परे तापनि त्यही समयमा राहु काल पनि चलिरहेको छ भने, नयाँ र महत्वपूर्ण कार्य राहु काल सकिएपछि मात्र थाल्नुपर्छ।\n• पहिले नै सुरु भइसकेका नित्य वा नियमित कार्यहरूमा भने कुनै बाधा पर्दैन।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Weekday Sequence Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) "Daywise Starting Choghadiya" else "वार अनुसार प्रारम्भ हुने पहिलो चौघडिया",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    val daysSeq = if (isEn) listOf(
                        Triple("Sunday", "Udveg (Sun)", "Inauspicious"),
                        Triple("Monday", "Amrit (Moon)", "Excellent"),
                        Triple("Tuesday", "Rog (Mars)", "Inauspicious"),
                        Triple("Wednesday", "Labh (Mercury)", "Auspicious"),
                        Triple("Thursday", "Shubh (Jupiter)", "Auspicious"),
                        Triple("Friday", "Char (Venus)", "Neutral"),
                        Triple("Saturday", "Kaal (Saturn)", "Inauspicious")
                    ) else listOf(
                        Triple("आइतबार", "उद्वेग (सूर्य)", "अशुभ"),
                        Triple("सोमबार", "अमृत (चन्द्र)", "सर्वोत्तम"),
                        Triple("मङ्गलबार", "रोग (मङ्गल)", "अशुभ"),
                        Triple("बुधबार", "लाभ (बुध)", "शुभ"),
                        Triple("बिहीबार", "शुभ (बृहस्पति)", "शुभ"),
                        Triple("शुक्रबार", "चर (शुक्र)", "सामान्य"),
                        Triple("शनिबार", "काल (शनि)", "अशुभ")
                    )
                    daysSeq.forEach { (day, chog, qual) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(day, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(chog, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(6.dp))
                                val isGood = if (isEn) qual in listOf("Excellent", "Auspicious") else qual in listOf("सर्वोत्तम", "शुभ")
                                val isNeutral = if (isEn) qual == "Neutral" else qual == "सामान्य"
                                Text(
                                    qual,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isGood) Color(0xFF10B981) else if (isNeutral) Color(0xFFF59E0B) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isEn) "Note: The 8th Choghadiya repeats the starting ruler of the day before handing over to night."
                               else "स्मरण रहोस्: आठौं चौघडिया फेरि पहिलो चौघडियाकै स्वामी अनुसार दोहोरिन्छ र त्यसपछि रात्रिकालीन चक्र प्रारम्भ हुन्छ।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}
