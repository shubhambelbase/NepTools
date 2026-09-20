package com.neptools.app.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.SmartDateParser
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.DayPickerDialog
import com.neptools.app.ui.components.MonthPickerDialog
import com.neptools.app.ui.components.SelectableDateChip
import com.neptools.app.ui.components.YearPickerDialog
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields

private val adMonthNamesEn = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val isEn = ThemePrefs.lang.value == "en"

    val engine = PatroRepo.d.engine
    val todayNp = remember { engine.today() }
    val todayAd = remember { engine.bsToAd(todayNp) }

    var bsToAdMode by remember { mutableStateOf(true) }
    var bsYear by remember { mutableIntStateOf(todayNp.year) }
    var bsMonth by remember { mutableIntStateOf(todayNp.month) }
    var bsDay by remember { mutableIntStateOf(todayNp.day) }
    var adYear by remember { mutableIntStateOf(todayAd.year) }
    var adMonth by remember { mutableIntStateOf(todayAd.monthValue) }
    var adDay by remember { mutableIntStateOf(todayAd.dayOfMonth) }

    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }

    val currentMaxBsDay = runCatching { engine.monthLength(bsYear, bsMonth) }.getOrDefault(31)
    val currentMaxAdDay = runCatching { LocalDate.of(adYear, adMonth, 1).lengthOfMonth() }.getOrDefault(31)

    // Picker Dialogs
    if (showYearPicker) {
        if (bsToAdMode) {
            YearPickerDialog(
                title = if (isEn) "Select Year (B.S.)" else "वर्ष छनोट गर्नुहोस् (बि.सं.)",
                selectedYear = bsYear,
                yearRange = engine.supportedRange().first..engine.supportedRange().last,
                isEn = isEn,
                onSelect = { bsYear = it },
                onDismiss = { showYearPicker = false }
            )
        } else {
            YearPickerDialog(
                title = if (isEn) "Select Year (A.D.)" else "वर्ष छनोट गर्नुहोस् (ई.सं.)",
                selectedYear = adYear,
                yearRange = 1918..2043,
                isEn = isEn,
                onSelect = { adYear = it },
                onDismiss = { showYearPicker = false }
            )
        }
    }

    if (showMonthPicker) {
        if (bsToAdMode) {
            MonthPickerDialog(
                title = if (isEn) "Select Month (B.S.)" else "महिना छनोट गर्नुहोस् (बि.सं.)",
                selectedMonth = bsMonth,
                monthNames = if (isEn) NepaliNames.monthsEn else NepaliNames.monthsNp,
                isEn = isEn,
                onSelect = { bsMonth = it },
                onDismiss = { showMonthPicker = false }
            )
        } else {
            MonthPickerDialog(
                title = if (isEn) "Select Month (A.D.)" else "महिना छनोट गर्नुहोस् (ई.सं.)",
                selectedMonth = adMonth,
                monthNames = adMonthNamesEn,
                isEn = isEn,
                onSelect = { adMonth = it },
                onDismiss = { showMonthPicker = false }
            )
        }
    }

    if (showDayPicker) {
        if (bsToAdMode) {
            DayPickerDialog(
                title = if (isEn) "Select Day (B.S.)" else "गते छनोट गर्नुहोस् (बि.सं.)",
                selectedDay = bsDay.coerceIn(1, currentMaxBsDay),
                maxDays = currentMaxBsDay,
                isEn = isEn,
                onSelect = { bsDay = it },
                onDismiss = { showDayPicker = false }
            )
        } else {
            DayPickerDialog(
                title = if (isEn) "Select Day (A.D.)" else "तारीख छनोट गर्नुहोस् (ई.सं.)",
                selectedDay = adDay.coerceIn(1, currentMaxAdDay),
                maxDays = currentMaxAdDay,
                isEn = isEn,
                onSelect = { adDay = it },
                onDismiss = { showDayPicker = false }
            )
        }
    }

    fun syncAndSwap() {
        if (bsToAdMode) {
            val np = NepaliDate(bsYear, bsMonth, bsDay.coerceAtMost(maxBsDay(engine, bsYear, bsMonth)))
            val ad = runCatching { engine.bsToAd(np) }.getOrNull()
            if (ad != null) {
                adYear = ad.year
                adMonth = ad.monthValue
                adDay = ad.dayOfMonth
            }
        } else {
            val maxAdDay = runCatching { LocalDate.of(adYear, adMonth, 1).lengthOfMonth() }.getOrDefault(30)
            val ad = runCatching { LocalDate.of(adYear, adMonth, adDay.coerceIn(1, maxAdDay)) }.getOrNull()
            val np = ad?.let { runCatching { engine.adToBs(it) }.getOrNull() }
            if (np != null) {
                bsYear = np.year
                bsMonth = np.month
                bsDay = np.day
            }
        }
        bsToAdMode = !bsToAdMode
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEn) "Date Converter" else "मिति रूपान्तरण",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PIcons.ChevronLeft, contentDescription = if (isEn) "Back" else "पछाडि")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                bsYear = todayNp.year
                                bsMonth = todayNp.month
                                bsDay = todayNp.day
                                adYear = todayAd.year
                                adMonth = todayAd.monthValue
                                adDay = todayAd.dayOfMonth
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = PIcons.Calendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Today" else "आज",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sleek Mode Switcher Tabs
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (bsToAdMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (bsToAdMode) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (!bsToAdMode) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    syncAndSwap()
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "B.S. ➔ A.D.",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (bsToAdMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isEn) "Nepali to English" else "नेपाली ➔ अंग्रेजी",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (bsToAdMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!bsToAdMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (!bsToAdMode) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (bsToAdMode) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    syncAndSwap()
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "A.D. ➔ B.S.",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (!bsToAdMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isEn) "English to Nepali" else "अंग्रेजी ➔ नेपाली",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!bsToAdMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // 2. Source Date Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row: Subtitle + Smart Detect Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isEn) "SOURCE DATE" else "स्रोत मिति",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (bsToAdMode)
                                    (if (isEn) "Bikram Sambat (B.S.)" else "विक्रम संवत् (बि.सं.)")
                                else
                                    (if (isEn) "Gregorian (A.D.)" else "ईस्वी संवत् (ई.सं.)"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Smart Paste & Detect Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.clickable {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    val detected = SmartDateParser.detectDate(clipText)
                                    if (detected != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        when (detected) {
                                            is com.neptools.app.core.calendar.DetectedDate.Bs -> {
                                                bsToAdMode = true
                                                bsYear = detected.date.year
                                                bsMonth = detected.date.month
                                                val maxD = runCatching { engine.monthLength(bsYear, bsMonth) }.getOrDefault(30)
                                                bsDay = detected.date.day.coerceIn(1, maxD)
                                                Toast.makeText(context, if (isEn) "Detected: ${detected.description}" else "पत्ता लाग्यो: ${detected.description}", Toast.LENGTH_SHORT).show()
                                            }
                                            is com.neptools.app.core.calendar.DetectedDate.Ad -> {
                                                bsToAdMode = false
                                                adYear = detected.date.year
                                                adMonth = detected.date.monthValue
                                                adDay = detected.date.dayOfMonth
                                                Toast.makeText(context, if (isEn) "Detected: ${detected.description}" else "पत्ता लाग्यो: ${detected.description}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, if (isEn) "No valid date found in clipboard" else "क्लिपबोर्डमा कुनै मिति भेटिएन", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, if (isEn) "Clipboard is empty" else "क्लिपबोर्ड खाली छ", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PIcons.Sparkle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = if (isEn) "Paste Date" else "क्लिपबोर्ड पेस्ट",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Quick Date Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Today
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (bsToAdMode) {
                                        bsYear = todayNp.year
                                        bsMonth = todayNp.month
                                        bsDay = todayNp.day
                                    } else {
                                        adYear = todayAd.year
                                        adMonth = todayAd.monthValue
                                        adDay = todayAd.dayOfMonth
                                    }
                                }
                        ) {
                            Text(
                                text = if (isEn) "Today" else "आज",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        // Yesterday
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val yestAd = todayAd.minusDays(1)
                                    if (bsToAdMode) {
                                        val yestBs = engine.adToBs(yestAd)
                                        bsYear = yestBs.year
                                        bsMonth = yestBs.month
                                        bsDay = yestBs.day
                                    } else {
                                        adYear = yestAd.year
                                        adMonth = yestAd.monthValue
                                        adDay = yestAd.dayOfMonth
                                    }
                                }
                        ) {
                            Text(
                                text = if (isEn) "Yesterday" else "हिजो",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        // Tomorrow
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val tomAd = todayAd.plusDays(1)
                                    if (bsToAdMode) {
                                        val tomBs = engine.adToBs(tomAd)
                                        bsYear = tomBs.year
                                        bsMonth = tomBs.month
                                        bsDay = tomBs.day
                                    } else {
                                        adYear = tomAd.year
                                        adMonth = tomAd.monthValue
                                        adDay = tomAd.dayOfMonth
                                    }
                                }
                        ) {
                            Text(
                                text = if (isEn) "Tomorrow" else "भोलि",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        // 1st of Month
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (bsToAdMode) {
                                        bsDay = 1
                                    } else {
                                        adDay = 1
                                    }
                                }
                        ) {
                            Text(
                                text = if (isEn) "1st" else "१ गते",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Touch-First Pickers Row (Year, Month, Day)
                    if (bsToAdMode) {
                        val dSafe = if (bsDay > currentMaxBsDay) currentMaxBsDay else bsDay
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SelectableDateChip(
                                label = if (isEn) "Year" else "वर्ष",
                                value = npNum(bsYear),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showYearPicker = true }
                            )
                            SelectableDateChip(
                                label = if (isEn) "Month" else "महिना",
                                value = if (isEn) NepaliNames.monthsEn[bsMonth - 1] else NepaliNames.monthsNp[bsMonth - 1],
                                modifier = Modifier.weight(1.3f),
                                onClick = { showMonthPicker = true }
                            )
                            SelectableDateChip(
                                label = if (isEn) "Day" else "गते",
                                value = npNum(dSafe),
                                modifier = Modifier.weight(1.0f),
                                onClick = { showDayPicker = true }
                            )
                        }
                    } else {
                        val dSafe = adDay.coerceIn(1, currentMaxAdDay)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SelectableDateChip(
                                label = if (isEn) "Year" else "वर्ष",
                                value = adYear.toString(),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showYearPicker = true }
                            )
                            SelectableDateChip(
                                label = if (isEn) "Month" else "महिना",
                                value = adMonthNamesEn[adMonth - 1].take(3),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showMonthPicker = true }
                            )
                            SelectableDateChip(
                                label = if (isEn) "Day" else "तारीख",
                                value = dSafe.toString(),
                                modifier = Modifier.weight(1.0f),
                                onClick = { showDayPicker = true }
                            )
                        }
                    }
                }
            }

            // 3. Central Swap Direction Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            syncAndSwap()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PIcons.Swap,
                            contentDescription = if (isEn) "Switch Direction" else "दिशा परिवर्तन",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 4. Converted Result Card
            val resultTitle: String
            val resultMainDate: String
            val resultWeekday: String
            val targetAdDate: LocalDate?
            val secondaryDetail: String
            val shareString: String

            if (bsToAdMode) {
                val npDate = NepaliDate(bsYear, bsMonth, bsDay.coerceAtMost(maxBsDay(engine, bsYear, bsMonth)))
                val ad = runCatching { engine.bsToAd(npDate) }.getOrNull()
                targetAdDate = ad
                resultTitle = if (isEn) "CONVERTED DATE (A.D.)" else "रूपान्तरित अंग्रेजी मिति (ई.सं.)"
                resultMainDate = ad?.let {
                    "${monthName(it)} ${it.dayOfMonth}, ${it.year}"
                } ?: "— Out of Range —"
                resultWeekday = ad?.let {
                    if (isEn) weekdayName(it) else NepaliNames.weekdaysNp[engine.weekdayIndexOf(npDate)]
                } ?: ""
                secondaryDetail = ad?.let {
                    if (isEn) "Day ${it.dayOfYear} of year · Week ${it.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
                    else "वर्षको दिन ${npNum(it.dayOfYear)} · हप्ता ${npNum(it.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))}"
                } ?: ""
                shareString = if (ad != null) {
                    "${npNum(npDate.year)}/${npNum(npDate.month)}/${npNum(npDate.day)} B.S. = ${ad.year}-${ad.monthValue}-${ad.dayOfMonth} A.D. (${resultWeekday})"
                } else ""
            } else {
                val maxAdD = runCatching { LocalDate.of(adYear, adMonth, 1).lengthOfMonth() }.getOrDefault(31)
                val ad = runCatching { LocalDate.of(adYear, adMonth, adDay.coerceIn(1, maxAdD)) }.getOrNull()
                targetAdDate = ad
                val np = ad?.let { runCatching { engine.adToBs(it) }.getOrNull() }
                resultTitle = if (isEn) "CONVERTED DATE (B.S.)" else "रूपान्तरित नेपाली मिति (बि.सं.)"
                resultMainDate = np?.let {
                    "${npNum(it.year)} ${NepaliNames.monthsNp[it.month - 1]} ${npNum(it.day)} गते"
                } ?: "— Out of Range —"
                resultWeekday = if (ad != null && np != null) {
                    if (isEn) weekdayName(ad) else NepaliNames.weekdaysNp[engine.weekdayIndexOf(np)]
                } else ""
                secondaryDetail = np?.let {
                    if (isEn) "Year ${it.year} B.S., Month ${NepaliNames.monthsEn[it.month - 1]}"
                    else "साल ${npNum(it.year)} बि.सं., महिना ${NepaliNames.monthsNp[it.month - 1]}"
                } ?: ""
                shareString = if (ad != null && np != null) {
                    "${ad.year}-${ad.monthValue}-${ad.dayOfMonth} A.D. = ${npNum(np.year)}/${npNum(np.month)}/${npNum(np.day)} B.S. (${resultWeekday})"
                } else ""
            }

            val relativeDaysText = if (targetAdDate != null) {
                formatRelativeDays(targetAdDate, todayAd, isEn)
            } else ""

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row: Status badge + Quick action icon buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PIcons.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = resultTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(resultMainDate))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, if (isEn) "Date copied to clipboard" else "मिति क्लिपबोर्डमा कपी गरियो", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PIcons.Copy,
                                    contentDescription = if (isEn) "Copy" else "कपी गर्नुहोस्",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (shareString.isNotBlank()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareString)
                                        }
                                        context.startActivity(Intent.createChooser(intent, if (isEn) "Share Converted Date" else "मिति सेयर गर्नुहोस्"))
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PIcons.Share,
                                    contentDescription = if (isEn) "Share" else "सेयर गर्नुहोस्",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Main Result Date
                    Text(
                        text = resultMainDate,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Weekday Subtitle
                    if (resultWeekday.isNotBlank()) {
                        Text(
                            text = resultWeekday,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Contextual Pill Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (relativeDaysText.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = relativeDaysText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        if (secondaryDetail.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = secondaryDetail,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // 1-Tap Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(resultMainDate))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, if (isEn) "Date copied" else "मिति कपी गरियो", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(PIcons.Copy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEn) "Copy Date" else "मिति कपी गर्नुहोस्")
                        }

                        OutlinedButton(
                            onClick = {
                                if (shareString.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareString)
                                    }
                                    context.startActivity(Intent.createChooser(intent, if (isEn) "Share Converted Date" else "मिति सेयर गर्नुहोस्"))
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(PIcons.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEn) "Share" else "सेयर गर्नुहोस्")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun formatRelativeDays(targetAd: LocalDate, todayAd: LocalDate, isEn: Boolean): String {
    val diff = ChronoUnit.DAYS.between(todayAd, targetAd)
    return when {
        diff == 0L -> if (isEn) "Today" else "आज"
        diff == 1L -> if (isEn) "Tomorrow" else "भोलि"
        diff == -1L -> if (isEn) "Yesterday" else "हिजो"
        diff > 1L -> if (isEn) "In $diff days" else "${npNum(diff.toInt())} दिन पछि"
        else -> if (isEn) "${-diff} days ago" else "${npNum((-diff).toInt())} दिन पहिले"
    }
}

private fun maxBsDay(engine: com.neptools.app.core.calendar.BsCalendarEngine, y: Int, m: Int): Int =
    runCatching { engine.monthLength(y, m) }.getOrDefault(31)

private fun weekdayName(d: LocalDate): String =
    d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

private fun monthName(d: LocalDate): String =
    d.month.name.lowercase().replaceFirstChar { it.uppercase() }
