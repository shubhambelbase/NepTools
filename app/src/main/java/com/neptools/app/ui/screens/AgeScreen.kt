package com.neptools.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    val engine = PatroRepo.d.engine
    val todayNp = remember { engine.today() }
    val todayAd = remember { engine.bsToAd(todayNp) }

    var year by remember { mutableIntStateOf(2058) }
    var month by remember { mutableIntStateOf(5) }
    var day by remember { mutableIntStateOf(10) }

    val maxD = runCatching { engine.monthLength(year, month) }.getOrDefault(31)
    val dSafe = day.coerceIn(1, maxD)

    val dob = NepaliDate(year, month, dSafe)
    val dobAd = remember(dob) { runCatching { engine.bsToAd(dob) }.getOrNull() }

    var y = 0; var m = 0; var days = 0
    if (dobAd != null && !dobAd.isAfter(todayAd)) {
        var cy = todayNp.year - dob.year
        var cm = todayNp.month - dob.month
        var cd = todayNp.day - dob.day
        if (cd < 0) {
            cm -= 1
            val pmYear = if (todayNp.month == 1) todayNp.year - 1 else todayNp.year
            val pmMonth = if (todayNp.month == 1) 12 else todayNp.month - 1
            cd += engine.monthLength(pmYear, pmMonth)
        }
        if (cm < 0) { cy -= 1; cm += 12 }
        y = cy; m = cm; days = cd
    }

    val totalDays = dobAd?.let { ChronoUnit.DAYS.between(it, todayAd).toInt() } ?: 0
    val totalWeeks = totalDays / 7
    val totalHours = totalDays * 24L

    // Next Birthday calculation
    val nextBdayYear = if (todayNp.month > month || (todayNp.month == month && todayNp.day > dSafe)) todayNp.year + 1 else todayNp.year
    val nextBday = NepaliDate(nextBdayYear, month, dSafe.coerceAtMost(engine.monthLength(nextBdayYear, month)))
    val nextBdayAd = remember(nextBday) { runCatching { engine.bsToAd(nextBday) }.getOrNull() }
    val daysUntilNextBday = nextBdayAd?.let { ChronoUnit.DAYS.between(todayAd, it).toInt() } ?: 0

    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }

    if (showYearPicker) {
        com.neptools.app.ui.components.YearPickerDialog(
            title = if (isEn) "Select Birth Year (B.S.)" else "जन्म वर्ष छनोट (बि.सं.)",
            selectedYear = year,
            yearRange = engine.supportedRange().first..todayNp.year,
            isEn = isEn,
            onSelect = { year = it },
            onDismiss = { showYearPicker = false }
        )
    }
    if (showMonthPicker) {
        com.neptools.app.ui.components.MonthPickerDialog(
            title = if (isEn) "Select Birth Month" else "जन्म महिना छनोट",
            selectedMonth = month,
            monthNames = if (isEn) NepaliNames.monthsEn else NepaliNames.monthsNp,
            isEn = isEn,
            onSelect = { month = it },
            onDismiss = { showMonthPicker = false }
        )
    }
    if (showDayPicker) {
        com.neptools.app.ui.components.DayPickerDialog(
            title = if (isEn) "Select Birth Day" else "जन्म गते छनोट",
            selectedDay = dSafe,
            maxDays = maxD,
            isEn = isEn,
            onSelect = { day = it },
            onDismiss = { showDayPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEn) "Age Calculator" else "उमेर क्यालकुलेटर",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PIcons.ChevronLeft, contentDescription = if (isEn) "Back" else "पछाडि")
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Birth Date Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEn) "Date of Birth (B.S.)" else "जन्म मिति छनोट (बि.सं.)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.neptools.app.ui.components.SelectableDateChip(
                            label = if (isEn) "Year" else "वर्ष",
                            value = npNum(year),
                            modifier = Modifier.weight(1.2f),
                            onClick = { showYearPicker = true }
                        )
                        com.neptools.app.ui.components.SelectableDateChip(
                            label = if (isEn) "Month" else "महिना",
                            value = if (isEn) NepaliNames.monthsEn[month - 1] else NepaliNames.monthsNp[month - 1],
                            modifier = Modifier.weight(1.3f),
                            onClick = { showMonthPicker = true }
                        )
                        com.neptools.app.ui.components.SelectableDateChip(
                            label = if (isEn) "Day" else "गते",
                            value = npNum(dSafe),
                            modifier = Modifier.weight(1.0f),
                            onClick = { showDayPicker = true }
                        )
                    }
                }
            }

            // Main Age Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEn) "Your Exact Age" else "तपाईंको उमेर",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isEn) "$y" else npNum(y),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isEn) " yrs " else " वर्ष ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = if (isEn) "$m" else npNum(m),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isEn) " mo " else " महिना ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = if (isEn) "$days" else npNum(days),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isEn) " days" else " दिन",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    if (dobAd != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${if (isEn) "Born on:" else "जन्म बार:"} ${NepaliNames.weekdaysNp[engine.weekdayIndexOf(dob)]} (${dobAd.year}-${dobAd.monthValue}-${dobAd.dayOfMonth} AD)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Metric Breakdown Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AgeMetricCard(
                    title = if (isEn) "Next Birthday" else "अर्को जन्मदिन",
                    value = "${if (isEn) daysUntilNextBday else npNum(daysUntilNextBday)} ${if (isEn) "Days" else "दिन"}",
                    icon = PIcons.Sparkle,
                    color = Color(0xFF9333EA),
                    modifier = Modifier.weight(1f)
                )

                AgeMetricCard(
                    title = if (isEn) "Total Lived" else "कुल बाँचेको",
                    value = "${if (isEn) totalDays else npNum(totalDays)} ${if (isEn) "Days" else "दिन"}",
                    icon = PIcons.Hourglass,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AgeMetricCard(
                    title = if (isEn) "Total Weeks" else "कुल हप्ता",
                    value = "${if (isEn) totalWeeks else npNum(totalWeeks)} ${if (isEn) "Wks" else "हप्ता"}",
                    icon = PIcons.Calendar,
                    color = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )

                AgeMetricCard(
                    title = if (isEn) "Total Hours" else "कुल घण्टा",
                    value = "${if (isEn) totalHours else npNum(totalHours.toInt())} ${if (isEn) "Hrs" else "घण्टा"}",
                    icon = PIcons.Timer,
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



@Composable
private fun AgeMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
