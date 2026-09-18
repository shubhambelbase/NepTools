package com.neptools.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.ripple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.neptools.app.ui.navigation.AppNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.PanchangCalc
import com.neptools.app.core.calendar.SolarCalc
import com.neptools.app.core.calendar.SolarDay
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.IconTile
import com.neptools.app.ui.components.LatticeBand
import com.neptools.app.ui.components.SectionTitle
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.navigation.Routes
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    onOpenCalendar: (year: Int, month: Int) -> Unit,
    onOpenTool: (route: String) -> Unit
) {
    val dataset = PatroRepo.d
    val engine = dataset.engine
    val today = remember { engine.today() }
    val todayAd = remember { engine.bsToAd(today) }
    val weekdayIdx = remember { engine.weekdayIndexOf(today) }
    val currentWeather by com.neptools.app.core.util.WeatherLocationManager.currentWeather.collectAsState()
    val birthLoc = com.neptools.app.astrology.data.AstroRepo.birth.value
    val lat = birthLoc?.latitude ?: currentWeather.lat
    val lon = birthLoc?.longitude ?: currentWeather.lon
    val solar = remember(todayAd, lat, lon) { SolarCalc.compute(todayAd, lat, lon) }
    val panchang = remember(todayAd) { PanchangCalc.compute(todayAd) }
    val festivalToday = remember(today) { dataset.festivalsFor(today.year, today.month)[today.day]?.firstOrNull() }
    val upcomingFest = remember(today) { nextFestival(dataset.festivalsFor(today.year, today.month), today.day) }

    val isEn = ThemePrefs.lang.value == "en"
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val updateManager = remember { com.neptools.app.core.updater.GitHubUpdateManager.get(ctx) }
    var availableUpdate by remember { mutableStateOf<com.neptools.app.core.updater.GitHubReleaseInfo?>(null) }
    var showUpdatePopup by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var downloadedApkFile by remember { mutableStateOf<java.io.File?>(null) }
    var updateError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Auto-refresh market rates & daily services whenever user lands on HomeScreen
    LaunchedEffect(Unit) {
        com.neptools.app.core.data.FuelRepo.refresh(ctx) {}
        com.neptools.app.core.data.KalimatiRepo.refresh(ctx) {}
        com.neptools.app.core.data.RatesRepo.refresh(ctx) {}

        // Check for updates in background after initial render
        delay(1200)
        try {
            val res = updateManager.checkForUpdates()
            if (res.isSuccess) {
                val info = res.getOrNull()
                if (info != null && info.isNewerVersion) {
                    availableUpdate = info
                    downloadedApkFile = updateManager.getCachedDownloadedApk(info)
                    showUpdatePopup = true
                }
            }
        } catch (_: Exception) {}
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---- hero ----
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        greeting(LocalTime.now().hour),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                PIcons.Menu,
                                contentDescription = if (isEn) "Menu" else "मेनु",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isEn) "Check for Updates" else "एप अपडेट जाँच्नुहोस्",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        PIcons.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    AppNavigator.navigateTo(Routes.UPDATER)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isEn) "Settings" else "सेटिङ",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        PIcons.Gear,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    AppNavigator.navigateTo(Routes.SETTINGS)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isEn) "About NepTools" else "हाम्रो बारे",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        PIcons.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    AppNavigator.navigateTo(Routes.ABOUT)
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Date Block
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            npNum(today.day),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                (if (isEn) NepaliNames.monthsEn[today.month - 1] else NepaliNames.monthsNp[today.month - 1]) + " " + npNum(today.year),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "${todayAd.dayOfMonth} ${todayAd.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${todayAd.year}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    if (isEn) NepaliNames.weekdaysEn[weekdayIdx] else NepaliNames.weekdaysNp[weekdayIdx],
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Right: Compact Weather Widget
                    val weather by com.neptools.app.core.util.WeatherLocationManager.currentWeather.collectAsState()
                    val weatherInteraction = remember { MutableInteractionSource() }
                    val weatherPressed by weatherInteraction.collectIsPressedAsState()
                    val weatherScale by animateFloatAsState(
                        targetValue = if (weatherPressed) 0.93f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "weatherScale"
                    )
                    Box(
                        Modifier
                            .graphicsLayer {
                                scaleX = weatherScale
                                scaleY = weatherScale
                            }
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        androidx.compose.ui.graphics.Color(0xFFE0F2FE),
                                        androidx.compose.ui.graphics.Color(0xFFBAE6FD)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                androidx.compose.ui.graphics.Color(0xFF7DD3FC),
                                androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = weatherInteraction,
                                indication = ripple(
                                    bounded = true,
                                    color = androidx.compose.ui.graphics.Color(0xFF0284C7).copy(alpha = 0.2f)
                                ),
                                onClick = { onOpenTool(Routes.WEATHER) }
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    PIcons.Sun,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${if (isEn) weather.tempC else npNum(weather.tempC)}°C",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = androidx.compose.ui.graphics.Color(0xFF0369A1)
                                )
                            }
                            Spacer(Modifier.height(1.dp))
                            Text(
                                text = if (isEn) weather.nameEn else weather.nameNp,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                color = androidx.compose.ui.graphics.Color(0xFF0284C7)
                            )
                            Text(
                                text = if (isEn) weather.conditionEn else weather.conditionNp,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = androidx.compose.ui.graphics.Color(0xFF0369A1).copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                val tName = if (isEn) panchang.tithiNameEn.ifBlank { panchang.tithiName } else panchang.tithiName
                val pName = if (isEn) panchang.pakshaEn.ifBlank { pakshaNp(panchang.paksha) } else pakshaNp(panchang.paksha)
                Text(
                    "${pName} · ${tName} ${T("tithi_word")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---- festival banner ----
        if (festivalToday != null || upcomingFest != null) {
            val f = festivalToday ?: upcomingFest!!
            val fName = if (isEn) f.nameEn.ifBlank { f.nameNp } else f.nameNp
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                    .clickable { onOpenCalendar(today.year, today.month) }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(PIcons.Calendar, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(width = 10.dp, height = 0.dp))
                Text(
                    if (festivalToday != null) "${T("festival_today")} $fName"
                    else "${npNum(f.day)} ${T("festival_on")} $fName",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Text("›", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }

        // ---- week strip ----
        WeekStrip(
            weekDates = buildWeek(engine, today, todayAd, weekdayIdx),
            activeIndex = weekdayIdx,
            onOpenCalendar = onOpenCalendar
        )

        // ---- panchang cards ----
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            PanchangCard(PIcons.SunUp, T("sunrise"), timeShort(solar.sunrise), Modifier.weight(1f))
            PanchangCard(PIcons.SunDown, T("sunset"), timeShort(solar.sunset), Modifier.weight(1f))
            PanchangCard(PIcons.Hourglass, T("rahu"), rahuShort(solar), Modifier.weight(1f),
                tintWarn = true)
        }

        // ---- Live Fuel Prices Card (GPS Location-Based) ----
        Spacer(Modifier.height(10.dp))
        HomeFuelPriceCard(
            isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en",
            onClick = { onOpenTool(Routes.FUEL) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ---- tools ----
        Spacer(Modifier.height(20.dp))
        SectionTitle(
            title = T("utilities"),
            actionLabel = T("all"),
            onAction = { onOpenTool(Routes.TOOLS) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        ToolGrid(onOpenTool)
        Spacer(Modifier.height(110.dp))
    }

    // =========================================================================
    // GLOBAL NEW UPDATE AVAILABLE POP-UP DIALOG
    // =========================================================================
    if (showUpdatePopup && availableUpdate != null) {
        val info = availableUpdate!!
        AlertDialog(
            onDismissRequest = { if (!isDownloadingUpdate) showUpdatePopup = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.neptools.app.R.drawable.app_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isEn) "New Update Available! (v${info.latestVersionName})" else "नयाँ अपडेट उपलब्ध छ! (v${info.latestVersionName})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) "Current: v${info.currentVersionName}" else "हालको: v${info.currentVersionName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isEn) "Latest: v${info.latestVersionName}" else "नयाँ: v${info.latestVersionName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = info.releaseNotes.ifBlank { "Performance improvements and new feature updates." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isDownloadingUpdate) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isEn) "Downloading..." else "डाउनलोड हुँदैछ...",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "$downloadProgress%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (updateError != null) {
                        Text(
                            text = updateError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                if (downloadedApkFile != null && downloadedApkFile!!.exists()) {
                    Button(
                        onClick = {
                            val res = updateManager.installApk(downloadedApkFile!!, info.sha256Checksum)
                            if (res.isFailure) {
                                updateError = res.exceptionOrNull()?.message
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = if (isEn) "Install Update Now" else "अहिले इन्स्टल गर्नुहोस्")
                    }
                } else if (isDownloadingUpdate) {
                    FilledTonalButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "$downloadProgress%")
                    }
                } else {
                    Button(
                        onClick = {
                            isDownloadingUpdate = true
                            updateError = null
                            scope.launch {
                                val dlRes = updateManager.downloadApk(info) { p, _, _ ->
                                    downloadProgress = p
                                }
                                isDownloadingUpdate = false
                                if (dlRes.isSuccess) {
                                    downloadedApkFile = dlRes.getOrNull()
                                    downloadedApkFile?.let {
                                        updateManager.installApk(it, info.sha256Checksum)
                                    }
                                } else {
                                    updateError = dlRes.exceptionOrNull()?.message ?: "Download failed"
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = if (isEn) "Update Now" else "अहिले अपडेट गर्नुहोस्")
                    }
                }
            },
            dismissButton = {
                if (!isDownloadingUpdate) {
                    TextButton(
                        onClick = { showUpdatePopup = false }
                    ) {
                        Text(text = if (isEn) "Later" else "पछि")
                    }
                }
            }
        )
    }
}

@Composable
private fun greeting(hour: Int): String = when (hour) {
    in 4..11 -> T("greet_morning")
    in 12..16 -> T("greet_day")
    in 17..20 -> T("greet_evening")
    else -> T("greet_night")
}

private fun nextFestival(map: Map<Int, List<com.neptools.app.core.calendar.Festival>>, currentDay: Int) =
    map.filterKeys { it > currentDay }.toSortedMap().values.firstOrNull()?.firstOrNull()

@Composable
private fun buildWeek(engine: com.neptools.app.core.calendar.BsCalendarEngine, today: NepaliDate, todayAd: LocalDate, idx: Int): List<NepaliDate> =
    runCatching {
        (-idx.toLong()..(6 - idx.toLong())).map { off -> engine.adToBs(todayAd.plusDays(off)) }
    }.getOrDefault(listOf(today))

@Composable
private fun timeShort(t: java.time.LocalTime?): String {
    t ?: return "—"
    val h12 = run { var h = t.hour % 12; if (h == 0) h = 12; h }
    val mm = t.minute.toString().padStart(2, '0')
    return if (com.neptools.app.ui.theme.ThemePrefs.nepaliDigits.value)
        NepaliNames.toDevanagari("$h12:$mm") else "$h12:$mm"
}

@Composable
private fun rahuShort(solar: SolarDay): String {
    val s = solar.rahuStart ?: return "—"
    val e = solar.rahuEnd ?: return "—"
    val raw = "%d:%02d – %d:%02d".format(s.hour, s.minute, e.hour, e.minute)
    return if (com.neptools.app.ui.theme.ThemePrefs.nepaliDigits.value)
        NepaliNames.toDevanagari(raw) else raw
}

@Composable
private fun WeekStrip(
    weekDates: List<NepaliDate>,
    activeIndex: Int,
    onOpenCalendar: (Int, Int) -> Unit
) {
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
    Row(
        Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weekDates.forEachIndexed { i, nd ->
            val isActive = i == activeIndex
            Column(
                Modifier
                    .weight(1f)
                    .clickable { onOpenCalendar(nd.year, nd.month) }
                    .background(
                        if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.small
                    )
                    .border(
                        1.dp,
                        if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        MaterialTheme.shapes.small
                    )
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isEn) NepaliNames.weekdaysEnShort[i] else NepaliNames.weekdaysNpShort[i],
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.onTertiary
                        i == 6 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    npNum(nd.day),
                    style = MaterialTheme.typography.titleLarge,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.onTertiary
                        i == 6 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onBackground
                    }
                )
            }
        }
    }
}

@Composable
private fun PanchangCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tintWarn: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "panchangScale"
    )
    // Professional palette: sunrise warm amber, sunset cool slate, rahu alert rose
    val iconBg = when {
        tintWarn -> androidx.compose.ui.graphics.Color(0xFFFEE2E2)
        label == T("sunrise") -> androidx.compose.ui.graphics.Color(0xFFFEF3C7)
        label == T("sunset") -> androidx.compose.ui.graphics.Color(0xFFE0F2FE)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    }
    val iconTint = when {
        tintWarn -> androidx.compose.ui.graphics.Color(0xFFE11D48)
        label == T("sunrise") -> androidx.compose.ui.graphics.Color(0xFFD97706)
        label == T("sunset") -> androidx.compose.ui.graphics.Color(0xFF0284C7)
        else -> MaterialTheme.colorScheme.secondary
    }
    val isLong = value.length > 7
    Column(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Box(
            Modifier
                .size(32.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isLong) 12.5.sp else 15.sp,
                lineHeight = if (isLong) 14.sp else 18.sp
            ),
            color = if (tintWarn) androidx.compose.ui.graphics.Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurface,
            maxLines = if (isLong) 2 else 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class TileDef(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val route: String?, val locked: Boolean = false)

@Composable
private fun ToolGrid(onOpenTool: (String) -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    val tiles = remember(isEn) {
        listOf(
            TileDef(PIcons.Car, if (isEn) "License" else "लाइसेन्स", Routes.DRIVING_LICENSE),
            TileDef(PIcons.Radio, if (isEn) "Radio" else "रेडियो", Routes.RADIO),
            TileDef(PIcons.Fuel, if (isEn) "Fuel Prices" else "इन्धन", Routes.FUEL),
            TileDef(PIcons.Leaf, if (isEn) "Kalimati" else "कालिमाटी", Routes.KALIMATI),
            TileDef(PIcons.Swap, if (isEn) "Converter" else "रूपान्तरण", Routes.CONVERTER),
            TileDef(PIcons.Coin, if (isEn) "Forex" else "मुद्रा", Routes.CURRENCY),
            TileDef(PIcons.Stars, if (isEn) "Kundali" else "कुण्डली", Routes.ASTRO),
            TileDef(PIcons.Doc, if (isEn) "Templates" else "निवेदन", Routes.TEMPLATES),
            TileDef(PIcons.Bank, if (isEn) "Loan EMI" else "ऋण", Routes.LOAN_EMI),
            TileDef(PIcons.Phone, if (isEn) "Emergency" else "आपतकालीन", Routes.EMERGENCY),
            TileDef(PIcons.Zap, if (isEn) "Bill Calc" else "महसुल", Routes.BILL_CALC),
            TileDef(PIcons.DecisionWheel, if (isEn) "Decision" else "निर्णय", Routes.DECISION_MAKER),
            TileDef(PIcons.Mail, if (isEn) "Postal" else "हुलाक", Routes.POSTAL),
            TileDef(PIcons.QrCode, if (isEn) "QR Code" else "क्युआर", Routes.QR),
            TileDef(PIcons.Hourglass, if (isEn) "Age Calc" else "उमेर", Routes.AGE),
            TileDef(PIcons.Receipt, if (isEn) "Bill Split" else "स्प्लिटर", Routes.BILL_SPLITTER)
        )
    }
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(4).forEach { rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTiles.forEach { tile ->
                    IconTile(
                        icon = tile.icon,
                        label = tile.label,
                        onClick = tile.route?.let { r -> ({ onOpenTool(r) }) },
                        locked = tile.locked,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun pakshaNp(p: String): String =
    if (com.neptools.app.ui.theme.ThemePrefs.lang.value == "en")
        if (p.startsWith("शुक्ल")) com.neptools.app.ui.strings.tt("shukla") else com.neptools.app.ui.strings.tt("krishna")
    else p

@Composable
private fun HomeWeatherCard(
    isEn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weather by com.neptools.app.core.util.WeatherLocationManager.currentWeather.collectAsState()

    Box(
        modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(androidx.compose.ui.graphics.Color(0xFFE0F2FE), androidx.compose.ui.graphics.Color(0xFFBAE6FD))
                )
            )
            .border(1.dp, androidx.compose.ui.graphics.Color(0xFF7DD3FC), androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .background(androidx.compose.ui.graphics.Color(0xFF0284C7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PIcons.Sun,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.size(width = 10.dp, height = 0.dp))
                    Column {
                        Text(
                            text = if (isEn) weather.nameEn else weather.nameNp,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.ui.graphics.Color(0xFF0369A1)
                        )
                        Text(
                            text = if (isEn) weather.conditionEn else weather.conditionNp,
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color(0xFF0284C7)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${if (isEn) weather.tempC else npNum(weather.tempC)}°C",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = androidx.compose.ui.graphics.Color(0xFF0369A1)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f), androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isEn) "H: ${weather.maxTempC}°  L: ${weather.minTempC}°" else "उ: ${npNum(weather.maxTempC)}°  न्यु: ${npNum(weather.minTempC)}°",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = androidx.compose.ui.graphics.Color(0xFF0369A1)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PIcons.Droplet,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFF0369A1),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${weather.humidityPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = androidx.compose.ui.graphics.Color(0xFF0369A1)
                    )
                }
                Box(
                    Modifier
                        .background(androidx.compose.ui.graphics.Color(0xFFDCFCE7), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isEn) "AQI ${weather.aqi} · ${weather.aqiLabelEn}" else "वायु ${npNum(weather.aqi)} · ${weather.aqiLabelNp}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = androidx.compose.ui.graphics.Color(0xFF15803D)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFuelPriceCard(
    isEn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var fuelRates by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(com.neptools.app.core.data.FuelRepo.loadCached(context)) }
    val weather by com.neptools.app.core.util.WeatherLocationManager.currentWeather.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fuelScale"
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.neptools.app.core.data.FuelRepo.refresh(context) { data ->
            fuelRates = data
        }
    }

    val resLoc = androidx.compose.runtime.remember(fuelRates, weather) {
        com.neptools.app.core.util.FuelLocationResolver.resolveForCurrentLocation(fuelRates)
    }
    val fmt = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val intFmt = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)

    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(androidx.compose.ui.graphics.Color(0xFFFFF7ED), androidx.compose.ui.graphics.Color(0xFFFFEDD5))
                )
            )
            .border(1.dp, androidx.compose.ui.graphics.Color(0xFFFED7AA), androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = androidx.compose.ui.graphics.Color(0xFFEA580C).copy(alpha = 0.15f)
                ),
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(androidx.compose.ui.graphics.Color(0xFFEA580C), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PIcons.Fuel,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.size(width = 8.dp, height = 0.dp))
                    Column {
                        Text(
                            if (isEn) "Live Fuel Prices (NOC)" else "इन्धनको खुद्रा दर",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.ui.graphics.Color(0xFF9A3412)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isEn) "Details ›" else "विस्तृत ›",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = androidx.compose.ui.graphics.Color(0xFFC2410C)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Petrol
                Box(
                    Modifier
                        .weight(1f)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.90f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(9.dp)
                ) {
                    Column {
                        Text(
                            if (isEn) "Petrol" else "पेट्रोल",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.ui.graphics.Color(0xFFDC2626)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (isEn) "Rs. ${fmt.format(resLoc.petrolPrice)}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(resLoc.petrolPrice))}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp),
                            color = androidx.compose.ui.graphics.Color(0xFF1E293B)
                        )
                        Text(
                            if (isEn) "per liter" else "प्रति लिटर",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = androidx.compose.ui.graphics.Color(0xFF64748B)
                        )
                    }
                }

                // Diesel
                Box(
                    Modifier
                        .weight(1f)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.90f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(9.dp)
                ) {
                    Column {
                        Text(
                            if (isEn) "Diesel" else "डिजेल",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.ui.graphics.Color(0xFF2563EB)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (isEn) "Rs. ${fmt.format(resLoc.dieselPrice)}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(resLoc.dieselPrice))}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp),
                            color = androidx.compose.ui.graphics.Color(0xFF1E293B)
                        )
                        Text(
                            if (isEn) "per liter" else "प्रति लिटर",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = androidx.compose.ui.graphics.Color(0xFF64748B)
                        )
                    }
                }

                // LPG Cylinder
                Box(
                    Modifier
                        .weight(1.05f)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.90f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(9.dp)
                ) {
                    Column {
                        Text(
                            if (isEn) "LPG Gas" else "एलपी ग्यास",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.ui.graphics.Color(0xFFD97706)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (isEn) "Rs. ${intFmt.format(resLoc.lpgPrice.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(intFmt.format(resLoc.lpgPrice.toLong()))}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp),
                            color = androidx.compose.ui.graphics.Color(0xFF1E293B)
                        )
                        Text(
                            if (isEn) "per cylinder" else "प्रति सिलिन्डर",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = androidx.compose.ui.graphics.Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}