package com.neptools.app.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.converter.LandConverter
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate

private val adMonthNamesEn = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isEn = ThemePrefs.lang.value == "en"

    var landTab by remember { mutableStateOf(false) }

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

    var copiedToast by remember { mutableStateOf(false) }

    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }

    val currentMaxBsDay = runCatching { engine.monthLength(bsYear, bsMonth) }.getOrDefault(31)
    val currentMaxAdDay = runCatching { LocalDate.of(adYear, adMonth, 1).lengthOfMonth() }.getOrDefault(31)

    if (showYearPicker) {
        if (bsToAdMode) {
            com.neptools.app.ui.components.YearPickerDialog(
                title = if (isEn) "Select Year (B.S.)" else "वर्ष छनोट गर्नुहोस् (बि.सं.)",
                selectedYear = bsYear,
                yearRange = engine.supportedRange().first..engine.supportedRange().last,
                isEn = isEn,
                onSelect = { bsYear = it },
                onDismiss = { showYearPicker = false }
            )
        } else {
            com.neptools.app.ui.components.YearPickerDialog(
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
            com.neptools.app.ui.components.MonthPickerDialog(
                title = if (isEn) "Select Month (B.S.)" else "महिना छनोट गर्नुहोस् (बि.सं.)",
                selectedMonth = bsMonth,
                monthNames = if (isEn) NepaliNames.monthsEn else NepaliNames.monthsNp,
                isEn = isEn,
                onSelect = { bsMonth = it },
                onDismiss = { showMonthPicker = false }
            )
        } else {
            com.neptools.app.ui.components.MonthPickerDialog(
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
            com.neptools.app.ui.components.DayPickerDialog(
                title = if (isEn) "Select Day (B.S.)" else "गते छनोट गर्नुहोस् (बि.सं.)",
                selectedDay = bsDay.coerceIn(1, currentMaxBsDay),
                maxDays = currentMaxBsDay,
                isEn = isEn,
                onSelect = { bsDay = it },
                onDismiss = { showDayPicker = false }
            )
        } else {
            com.neptools.app.ui.components.DayPickerDialog(
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
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                bsYear = todayNp.year
                                bsMonth = todayNp.month
                                bsDay = todayNp.day
                                adYear = todayAd.year
                                adMonth = todayAd.monthValue
                                adDay = todayAd.dayOfMonth
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isEn) "Today" else "आज",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
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
            // Outer mode: Date vs Land
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                        .background(if (!landTab) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { landTab = false }.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isEn) "Date" else "मिति", style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (!landTab) FontWeight.Bold else FontWeight.Medium), color = if (!landTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                        .background(if (landTab) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { landTab = true }.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isEn) "Land (Ropani)" else "जग्गा (रोपनी)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (landTab) FontWeight.Bold else FontWeight.Medium), color = if (landTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (landTab) {
                LandConverterSection(isEn, clipboardManager)
            } else {
            // Mode Indicator / Switcher Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (bsToAdMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { if (!bsToAdMode) syncAndSwap() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEn) "B.S. ➔ A.D." else "बि.सं. ➔ ई.सं.",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (bsToAdMode) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (bsToAdMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (!bsToAdMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { if (bsToAdMode) syncAndSwap() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEn) "A.D. ➔ B.S." else "ई.सं. ➔ बि.सं.",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (!bsToAdMode) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (!bsToAdMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Smart Date Auto-Detect Action
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        .clickable {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                val detected = com.neptools.app.core.calendar.SmartDateParser.detectDate(clipText)
                                if (detected != null) {
                                    when (detected) {
                                        is com.neptools.app.core.calendar.DetectedDate.Bs -> {
                                            bsToAdMode = true
                                            bsYear = detected.date.year
                                            bsMonth = detected.date.month
                                            val maxD = runCatching { engine.monthLength(bsYear, bsMonth) }.getOrDefault(30)
                                            bsDay = detected.date.day.coerceIn(1, maxD)
                                            android.widget.Toast.makeText(context, if (isEn) "Detected: ${detected.description}" else "पत्ता लाग्यो: ${detected.description}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        is com.neptools.app.core.calendar.DetectedDate.Ad -> {
                                            bsToAdMode = false
                                            adYear = detected.date.year
                                            adMonth = detected.date.monthValue
                                            adDay = detected.date.dayOfMonth
                                            android.widget.Toast.makeText(context, if (isEn) "Detected: ${detected.description}" else "पत्ता लाग्यो: ${detected.description}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, if (isEn) "No valid date found in clipboard text" else "क्लिपबोर्डमा कुनै मिति भेटिएन", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                android.widget.Toast.makeText(context, if (isEn) "Clipboard is empty" else "क्लिपबोर्ड खाली छ", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            PIcons.Sparkle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Paste & Detect Date" else "क्लिपबोर्डबाट मिति पहिचान",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Input Selection Card
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
                        text = if (bsToAdMode)
                            (if (isEn) "Select Nepali Date (B.S.)" else "नेपाली मिति छनोट गर्नुहोस् (बि.सं.)")
                        else
                            (if (isEn) "Select English Date (A.D.)" else "अंग्रेजी मिति छनोट गर्नुहोस् (ई.सं.)"),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(14.dp))

                    if (bsToAdMode) {
                        val dSafe = if (bsDay > currentMaxBsDay) currentMaxBsDay else bsDay
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.neptools.app.ui.components.SelectableDateChip(
                                label = if (isEn) "Year" else "वर्ष",
                                value = npNum(bsYear),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showYearPicker = true }
                            )
                            com.neptools.app.ui.components.SelectableDateChip(
                                label = if (isEn) "Month" else "महिना",
                                value = if (isEn) NepaliNames.monthsEn[bsMonth - 1] else NepaliNames.monthsNp[bsMonth - 1],
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
                    } else {
                        val dSafe = adDay.coerceIn(1, currentMaxAdDay)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.neptools.app.ui.components.SelectableDateChip(
                                label = if (isEn) "Year" else "वर्ष",
                                value = adYear.toString(),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showYearPicker = true }
                            )
                            com.neptools.app.ui.components.SelectableDateChip(
                                label = if (isEn) "Month" else "महिना",
                                value = adMonthNamesEn[adMonth - 1].take(3),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showMonthPicker = true }
                            )
                            com.neptools.app.ui.components.SelectableDateChip(
                                label = if (isEn) "Day" else "तारीख",
                                value = dSafe.toString(),
                                modifier = Modifier.weight(1.0f),
                                onClick = { showDayPicker = true }
                            )
                        }
                    }
                }
            }

            // Quick Swap Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { syncAndSwap() }),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PIcons.Swap,
                            contentDescription = if (isEn) "Swap" else "साट्नुहोस्",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Switch Direction" else "दिशा परिवर्तन गर्नुहोस्",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Converted Result Card
            val resultTitle: String
            val resultDateString: String
            val secondaryDetail: String

            if (bsToAdMode) {
                val npDate = NepaliDate(bsYear, bsMonth, bsDay.coerceAtMost(maxBsDay(engine, bsYear, bsMonth)))
                val ad = runCatching { engine.bsToAd(npDate) }.getOrNull()
                resultTitle = if (isEn) "Converted Gregorian Date (A.D.)" else "रूपान्तरित अंग्रेजी मिति (ई.सं.)"
                resultDateString = ad?.let {
                    "${weekdayName(it)}, ${monthName(it)} ${it.dayOfMonth}, ${it.year}"
                } ?: "— Out of Range —"
                secondaryDetail = ad?.let {
                    "Day of year: ${it.dayOfYear} · Week ${it.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
                } ?: ""
            } else {
                val maxAdD = runCatching { LocalDate.of(adYear, adMonth, 1).lengthOfMonth() }.getOrDefault(31)
                val ad = runCatching { LocalDate.of(adYear, adMonth, adDay.coerceIn(1, maxAdD)) }.getOrNull()
                val np = ad?.let { runCatching { engine.adToBs(it) }.getOrNull() }
                resultTitle = if (isEn) "Converted Nepali Date (B.S.)" else "रूपान्तरित नेपाली मिति (बि.सं.)"
                resultDateString = np?.let {
                    "${NepaliNames.weekdaysNp[engine.weekdayIndexOf(it)]}, ${NepaliNames.monthsNp[it.month - 1]} ${npNum(it.day)}, ${npNum(it.year)}"
                } ?: "— Out of Range —"
                secondaryDetail = np?.let {
                    "साल ${npNum(it.year)}, महिना ${NepaliNames.monthsNp[it.month - 1]}"
                } ?: ""
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = resultTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(resultDateString))
                                copiedToast = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = PIcons.Copy,
                                contentDescription = if (isEn) "Copy" else "कपी गर्नुहोस्",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = resultDateString,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (secondaryDetail.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = secondaryDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            } // else landTab
        }
    }
}

@Composable
private fun LandConverterSection(isEn: Boolean, clipboardManager: androidx.compose.ui.platform.ClipboardManager) {
    var ropaniStr by remember { mutableStateOf("1") }
    var aanaStr by remember { mutableStateOf("0") }
    var paisaStr by remember { mutableStateOf("0") }
    var damStr by remember { mutableStateOf("0") }
    var amountStr by remember { mutableStateOf("1") }
    var fromUnit by remember { mutableStateOf(LandConverter.Unit.ROPANI) }
    var toUnit by remember { mutableStateOf(LandConverter.Unit.SQM) }
    var showFrom by remember { mutableStateOf(false) }
    var showTo by remember { mutableStateOf(false) }

    val ropani = ropaniStr.toIntOrNull() ?: 0
    val aana = aanaStr.toIntOrNull() ?: 0
    val paisa = paisaStr.toIntOrNull() ?: 0
    val dam = damStr.toDoubleOrNull() ?: 0.0
    val sqmFromBreakdown = LandConverter.breakdownToSqm(ropani.coerceIn(0,999), aana.coerceIn(0,15), paisa.coerceIn(0,3), dam.coerceIn(0.0,3.99))
    val breakdownSqm = LandConverter.toRopaniBreakdown(sqmFromBreakdown)
    val bighaStr = LandConverter.formatBighaBreakdown(sqmFromBreakdown)

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val converted = LandConverter.convert(amount, fromUnit, toUnit)

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (isEn) "Ropani - Aana - Paisa - Dam" else "रोपनी - आना - पैसा - दाम", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) { Text(if (isEn) LandConverter.Unit.ROPANI.labelEn else LandConverter.Unit.ROPANI.labelNp, style = MaterialTheme.typography.labelSmall); androidx.compose.material3.OutlinedTextField(value = ropaniStr, onValueChange = { ropaniStr = it.filter { c -> c.isDigit() }.take(4) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) }
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) { Text(if (isEn) "${LandConverter.Unit.AANA.labelEn} (0-15)" else "${LandConverter.Unit.AANA.labelNp} (०-१५)", style = MaterialTheme.typography.labelSmall); androidx.compose.material3.OutlinedTextField(value = aanaStr, onValueChange = { aanaStr = it.filter { c -> c.isDigit() }.take(2) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) }
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) { Text(if (isEn) "${LandConverter.Unit.PAISA.labelEn} (0-3)" else "${LandConverter.Unit.PAISA.labelNp} (०-३)", style = MaterialTheme.typography.labelSmall); androidx.compose.material3.OutlinedTextField(value = paisaStr, onValueChange = { paisaStr = it.filter { c -> c.isDigit() }.take(1) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) }
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) { Text(if (isEn) LandConverter.Unit.DAM.labelEn else LandConverter.Unit.DAM.labelNp, style = MaterialTheme.typography.labelSmall); androidx.compose.material3.OutlinedTextField(value = damStr, onValueChange = { damStr = it.filter { c -> c.isDigit() || c=='.' }.take(5) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) }
            }
            Text(if (isEn) "= ${String.format("%.2f", sqmFromBreakdown)} sqm  •  $bighaStr (B-K-D)" else "= ${String.format("%.2f", sqmFromBreakdown)} वर्ग मिटर  •  $bighaStr (बि-क-ध)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (isEn) "Breakdown: ${breakdownSqm.ropani}-${breakdownSqm.aana}-${breakdownSqm.paisa}-${String.format("%.2f", breakdownSqm.dam)} (R-A-P-D)" else "विवरण: ${npNum(breakdownSqm.ropani)}-${npNum(breakdownSqm.aana)}-${npNum(breakdownSqm.paisa)}-${com.neptools.app.core.calendar.NepaliNames.toDevanagari(String.format("%.2f", breakdownSqm.dam))} (रो-आ-प-दा)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            androidx.compose.material3.OutlinedButton(onClick = { clipboardManager.setText(AnnotatedString("${String.format("%.2f", sqmFromBreakdown)} sqm")) }, modifier = Modifier.fillMaxWidth()) { Text(if (isEn) "Copy sqm" else "कपी गर्नुहोस्") }
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (isEn) "Any Unit Converter" else "एकाइ रूपान्तरण", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            androidx.compose.material3.OutlinedTextField(value = amountStr, onValueChange = { amountStr = it.filter { c -> c.isDigit() || c=='.' } }, label = { Text(if (isEn) "Amount" else "परिमाण") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showFrom = true }.padding(12.dp), contentAlignment = Alignment.Center) { Text(if (isEn) fromUnit.labelEn else fromUnit.labelNp, fontWeight = FontWeight.Bold) }
                Icon(PIcons.Swap, contentDescription = if (isEn) "Swap" else "साट्नुहोस्", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp).clickable { val t = fromUnit; fromUnit = toUnit; toUnit = t })
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showTo = true }.padding(12.dp), contentAlignment = Alignment.Center) { Text(if (isEn) toUnit.labelEn else toUnit.labelNp, fontWeight = FontWeight.Bold) }
            }
            if (showFrom) { LandUnitPicker(LandConverter.Unit.values().toList(), { fromUnit = it; showFrom = false }, { showFrom = false }) }
            if (showTo) { LandUnitPicker(LandConverter.Unit.values().toList(), { toUnit = it; showTo = false }, { showTo = false }) }
            Text("${String.format("%.4f", converted)} ${if (isEn) toUnit.labelEn else toUnit.labelNp}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text("1 ${if (isEn) fromUnit.labelEn else fromUnit.labelNp} = ${String.format("%.6f", LandConverter.convert(1.0, fromUnit, toUnit))} ${if (isEn) toUnit.labelEn else toUnit.labelNp}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LandUnitPicker(units: List<LandConverter.Unit>, onSelect: (LandConverter.Unit)->Unit, onDismiss: ()->Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(8.dp)) {
        units.forEach { u -> Row(Modifier.fillMaxWidth().clickable { onSelect(u) }.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (isEn) u.labelEn else u.labelNp); Text(if (isEn) u.labelEn else u.labelNp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        androidx.compose.material3.TextButton(onClick = onDismiss) { Text(T("close")) }
    }
}

private fun maxBsDay(engine: com.neptools.app.core.calendar.BsCalendarEngine, y: Int, m: Int): Int =
    runCatching { engine.monthLength(y, m) }.getOrDefault(31)

private fun weekdayName(d: LocalDate): String =
    d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

private fun monthName(d: LocalDate): String =
    d.month.name.lowercase().replaceFirstChar { it.uppercase() }


