package com.neptools.app.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.neptools.app.core.util.getPlainText
import com.neptools.app.core.util.setPlainText
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.SmartDateParser
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.DayPickerDialog
import com.neptools.app.ui.components.MonthPickerDialog
import com.neptools.app.ui.components.ToolTopBar
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

/**
 * Variation 1: Heritage Lithograph Date Converter with Rich Micro-Animations.
 * Features:
 * - Fluid sliding magnetic pill for B.S. / A.D. segmented control.
 * - 180-degree elastic swap node with spring overshoot physics.
 * - Mechanical rolling odometer reels for year, month, and day blocks.
 * - Fresh lithograph stamp pop & glow on converted hero result.
 * - Tactile spring-depression physics on all interactive buttons and preset chips.
 * - Ambient breathing glow on the Smart Date Clipboard paste pill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
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

    // Swap rotation animation state
    var swapRotationDegrees by remember { mutableFloatStateOf(0f) }
    val animatedSwapRotation by animateFloatAsState(
        targetValue = swapRotationDegrees,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "swapRotation"
    )

    // Breathing glow animation for Smart Paste pill
    val infiniteTransition = rememberInfiniteTransition(label = "pasteBreathing")
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathAlpha"
    )

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
        swapRotationDegrees += 180f
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
            ToolTopBar(
                title = if (isEn) "Date Converter" else "मिति रूपान्तरण",
                subtitle = if (isEn) "Bi-directional B.S. & A.D. conversion" else "वि.सं. तथा ई.सं. दुईतर्फी मिति रूपान्तरण",
                onBack = onBack,
                actions = {
                    val todayInteractionSource = remember { MutableInteractionSource() }
                    val isTodayPressed by todayInteractionSource.collectIsPressedAsState()
                    val todayScale by animateFloatAsState(
                        targetValue = if (isTodayPressed) 0.90f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "todayPress"
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                scaleX = todayScale
                                scaleY = todayScale
                            }
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = todayInteractionSource,
                                indication = androidx.compose.material3.ripple(bounded = true, radius = 18.dp)
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                bsYear = todayNp.year
                                bsMonth = todayNp.month
                                bsDay = todayNp.day
                                adYear = todayAd.year
                                adMonth = todayAd.monthValue
                                adDay = todayAd.dayOfMonth
                                Toast.makeText(
                                    context,
                                    if (isEn) "Reset to Today" else "आजको मितिमा रिसेट गरियो",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = PIcons.Calendar,
                                contentDescription = if (isEn) "Today" else "आज",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Fluid Sliding Magnetic Pill Segmented Control
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
                    val tabWidth = maxWidth / 2
                    val pillOffset by animateDpAsState(
                        targetValue = if (bsToAdMode) 0.dp else tabWidth,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                        label = "pillOffset"
                    )

                    // Sliding Pill Surface
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .offset(x = pillOffset)
                            .width(tabWidth)
                            .matchParentSize()
                    ) {}

                    Row(modifier = Modifier.fillMaxWidth()) {
                        val bsMainColor by animateColorAsState(
                            targetValue = if (bsToAdMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "bsMainColor"
                        )
                        val bsSubColor by animateColorAsState(
                            targetValue = if (bsToAdMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            label = "bsSubColor"
                        )
                        val adMainColor by animateColorAsState(
                            targetValue = if (!bsToAdMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "adMainColor"
                        )
                        val adSubColor by animateColorAsState(
                            targetValue = if (!bsToAdMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            label = "adSubColor"
                        )

                        // Option 1: BS to AD
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (!bsToAdMode) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        syncAndSwap()
                                    }
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "B.S. ➔ A.D.",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = bsMainColor
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (isEn) "Nepali to English" else "नेपाली ➔ अंग्रेजी",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = bsSubColor
                                )
                            }
                        }

                        // Option 2: AD to BS
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (bsToAdMode) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        syncAndSwap()
                                    }
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "A.D. ➔ B.S.",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = adMainColor
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (isEn) "English to Nepali" else "अंग्रेजी ➔ नेपाली",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = adSubColor
                                )
                            }
                        }
                    }
                }
            }

            // 2. Source Voucher Slip Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Eyebrow Row: Source Tag + Paste Date Button with Breathing Glow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (bsToAdMode)
                                (if (isEn) "SOURCE: BIKRAM SAMBAT (B.S.)" else "स्रोत: विक्रम संवत् (वि.सं.)")
                            else
                                (if (isEn) "SOURCE: GREGORIAN (A.D.)" else "स्रोत: ईस्वी संवत् (ई.सं.)"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Smart Paste Link with Ambient Breathing Glow
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = breathAlpha),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = breathAlpha * 2f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        val clipText = clipboard.getPlainText()
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
                                                        Toast.makeText(
                                                            context,
                                                            if (isEn) "Detected: ${detected.description}" else "पत्ता लाग्यो: ${detected.description}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    is com.neptools.app.core.calendar.DetectedDate.Ad -> {
                                                        bsToAdMode = false
                                                        adYear = detected.date.year
                                                        adMonth = detected.date.monthValue
                                                        adDay = detected.date.dayOfMonth
                                                        Toast.makeText(
                                                            context,
                                                            if (isEn) "Detected: ${detected.description}" else "पत्ता लाग्यो: ${detected.description}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    if (isEn) "No valid date found in clipboard" else "क्लिपबोर्डमा कुनै मिति भेटिएन",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Clipboard is empty" else "क्लिपबोर्ड खाली छ",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PIcons.Sparkle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isEn) "Paste Date" else "पेस्ट गर्नुहोस्",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Quick Preset Chips (Row of 4 with Spring Depression)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LithoPresetChip(
                            text = if (isEn) "Today" else "आज",
                            modifier = Modifier.weight(1f),
                            onClick = {
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
                        )

                        LithoPresetChip(
                            text = if (isEn) "Yesterday" else "हिजो",
                            modifier = Modifier.weight(1f),
                            onClick = {
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
                        )

                        LithoPresetChip(
                            text = if (isEn) "Tomorrow" else "भोलि",
                            modifier = Modifier.weight(1f),
                            onClick = {
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
                        )

                        LithoPresetChip(
                            text = if (isEn) "1st of Month" else "१ गते",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (bsToAdMode) {
                                    bsDay = 1
                                } else {
                                    adDay = 1
                                }
                            }
                        )
                    }

                    // Date Selectors: 3 styled tactile columns with Mechanical Odometer Reel
                    if (bsToAdMode) {
                        val dSafe = if (bsDay > currentMaxBsDay) currentMaxBsDay else bsDay
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LithoPickerBox(
                                label = if (isEn) "Year" else "वर्ष",
                                value = npNum(bsYear),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showYearPicker = true }
                            )
                            LithoPickerBox(
                                label = if (isEn) "Month" else "महिना",
                                value = if (isEn) NepaliNames.monthsEn[bsMonth - 1] else NepaliNames.monthsNp[bsMonth - 1],
                                modifier = Modifier.weight(1.3f),
                                onClick = { showMonthPicker = true }
                            )
                            LithoPickerBox(
                                label = if (isEn) "Day" else "गते",
                                value = npNum(dSafe),
                                modifier = Modifier.weight(1.0f),
                                onClick = { showDayPicker = true }
                            )
                        }
                    } else {
                        val dSafe = adDay.coerceIn(1, currentMaxAdDay)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LithoPickerBox(
                                label = if (isEn) "Year" else "वर्ष",
                                value = adYear.toString(),
                                modifier = Modifier.weight(1.2f),
                                onClick = { showYearPicker = true }
                            )
                            LithoPickerBox(
                                label = if (isEn) "Month" else "महिना",
                                value = adMonthNamesEn[adMonth - 1],
                                modifier = Modifier.weight(1.3f),
                                onClick = { showMonthPicker = true }
                            )
                            LithoPickerBox(
                                label = if (isEn) "Day" else "तारीख",
                                value = dSafe.toString().padStart(2, '0'),
                                modifier = Modifier.weight(1.0f),
                                onClick = { showDayPicker = true }
                            )
                        }
                    }
                }
            }

            // 3. Central Embossed 180-Degree Flip Direction Node
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(3.dp, MaterialTheme.colorScheme.background),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            syncAndSwap()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PIcons.Swap,
                            contentDescription = if (isEn) "Swap Direction" else "दिशा परिवर्तन",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(90f + animatedSwapRotation)
                        )
                    }
                }
            }

            // 4. Converted Result Hero Card with Fresh Stamp Pop
            val resultTitle: String
            val resultMainDate: String
            val resultWeekday: String
            val targetAdDate: LocalDate?
            val dayOfYearText: String
            val extraMetaText: String
            val shareString: String

            if (bsToAdMode) {
                val npDate = NepaliDate(bsYear, bsMonth, bsDay.coerceAtMost(maxBsDay(engine, bsYear, bsMonth)))
                val ad = runCatching { engine.bsToAd(npDate) }.getOrNull()
                targetAdDate = ad
                resultTitle = if (isEn) "CONVERTED DATE (A.D.)" else "रूपान्तरित मिति (ई.सं.)"
                resultMainDate = ad?.let {
                    "${monthName(it)} ${it.dayOfMonth.toString().padStart(2, '0')}, ${it.year}"
                } ?: "— Out of Range —"
                resultWeekday = ad?.let {
                    if (isEn) weekdayName(it) else NepaliNames.weekdaysNp[engine.weekdayIndexOf(npDate)]
                } ?: ""
                dayOfYearText = ad?.let {
                    if (isEn) "Day ${it.dayOfYear} of Year" else "वर्षको ${npNum(it.dayOfYear)} औं दिन"
                } ?: ""
                extraMetaText = ad?.let {
                    if (isEn) "Week ${it.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}" else "हप्ता ${npNum(it.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))}"
                } ?: ""
                shareString = if (ad != null) {
                    "${npNum(npDate.year)}/${npNum(npDate.month)}/${npNum(npDate.day)} B.S. = ${ad.year}-${ad.monthValue}-${ad.dayOfMonth} A.D. (${resultWeekday})"
                } else ""
            } else {
                val maxAdD = runCatching { LocalDate.of(adYear, adMonth, 1).lengthOfMonth() }.getOrDefault(31)
                val ad = runCatching { LocalDate.of(adYear, adMonth, adDay.coerceIn(1, maxAdD)) }.getOrNull()
                targetAdDate = ad
                val np = ad?.let { runCatching { engine.adToBs(it) }.getOrNull() }
                resultTitle = if (isEn) "CONVERTED DATE (B.S.)" else "रूपान्तरित मिति (वि.सं.)"
                resultMainDate = np?.let {
                    "${npNum(it.year)} ${NepaliNames.monthsNp[it.month - 1]} ${npNum(it.day)} गते"
                } ?: "— Out of Range —"
                resultWeekday = if (ad != null && np != null) {
                    if (isEn) weekdayName(ad) else NepaliNames.weekdaysNp[engine.weekdayIndexOf(np)]
                } else ""
                dayOfYearText = np?.let {
                    if (isEn) "Year ${it.year} B.S." else "साल ${npNum(it.year)} वि.सं."
                } ?: ""
                extraMetaText = np?.let {
                    if (isEn) "Month ${NepaliNames.monthsEn[it.month - 1]}" else "महिना ${NepaliNames.monthsNp[it.month - 1]}"
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
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row: Accent dot + Eyebrow tag + Quick Copy Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = resultTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    clipboard.setPlainText(resultMainDate)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Copied to clipboard" else "क्लिपबोर्डमा प्रतिलिपि गरियो",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = PIcons.Copy,
                                contentDescription = if (isEn) "Copy" else "प्रतिलिपि",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Main Result Date Headline with Stamp Pop
                    AnimatedContent(
                        targetState = resultMainDate,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.93f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(220)))
                                .togetherWith(scaleOut(targetScale = 0.96f, animationSpec = tween(150)) + fadeOut(tween(150)))
                        },
                        label = "stampPopHeadline"
                    ) { dateHeadline ->
                        Text(
                            text = dateHeadline,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Weekday Subtitle
                    if (resultWeekday.isNotBlank()) {
                        Text(
                            text = resultWeekday,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Metadata Badges Row with animated transitions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (relativeDaysText.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            ) {
                                AnimatedContent(
                                    targetState = relativeDaysText,
                                    transitionSpec = {
                                        (fadeIn(tween(180)) + scaleIn(initialScale = 0.9f)).togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.9f))
                                    },
                                    label = "relBadgeAnim"
                                ) { relText ->
                                    Text(
                                        text = relText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (dayOfYearText.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = dayOfYearText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (extraMetaText.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = extraMetaText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 1-Tap Action Grid with Spring Physics
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val copyBtnInteractionSource = remember { MutableInteractionSource() }
                        val isCopyBtnPressed by copyBtnInteractionSource.collectIsPressedAsState()
                        val copyBtnScale by animateFloatAsState(
                            targetValue = if (isCopyBtnPressed) 0.94f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "copyBtnPress"
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    clipboard.setPlainText(resultMainDate)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Copied result date" else "मिति प्रतिलिपि गरियो",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = copyBtnScale
                                    scaleY = copyBtnScale
                                },
                            interactionSource = copyBtnInteractionSource,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(PIcons.Copy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Copy Date" else "मिति प्रतिलिपि",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        val shareBtnInteractionSource = remember { MutableInteractionSource() }
                        val isShareBtnPressed by shareBtnInteractionSource.collectIsPressedAsState()
                        val shareBtnScale by animateFloatAsState(
                            targetValue = if (isShareBtnPressed) 0.94f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "shareBtnPress"
                        )

                        OutlinedButton(
                            onClick = {
                                if (shareString.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareString)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent,
                                            if (isEn) "Share Date Breakdown" else "मिति साझा गर्नुहोस्"
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = shareBtnScale
                                    scaleY = shareBtnScale
                                },
                            interactionSource = shareBtnInteractionSource,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                        ) {
                            Icon(PIcons.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Share" else "साझा गर्नुहोस्",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LithoPresetChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "chipPress"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 7.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LithoPickerBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pickerPress"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> height } + fadeIn(tween(180)))
                        .togetherWith(slideOutVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> -height } + fadeOut(tween(150)))
                },
                label = "odometerReel"
            ) { targetVal ->
                Text(
                    text = targetVal,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
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
