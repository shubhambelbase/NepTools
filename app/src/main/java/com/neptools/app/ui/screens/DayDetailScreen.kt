package com.neptools.app.ui.screens

import com.neptools.app.ui.strings.T

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.PanchangCalc
import com.neptools.app.core.calendar.SolarCalc
import com.neptools.app.core.calendar.SolarDay
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.core.reminder.UserCalendarEvent
import com.neptools.app.core.reminder.UserEventManager
import com.neptools.app.ui.components.InkButton
import com.neptools.app.ui.components.StampBadge
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import java.time.LocalDate

@Composable
fun DayDetailScreen(
    argYear: Int,
    argMonth: Int,
    argDay: Int,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dataset = PatroRepo.d
    val engine = dataset.engine
    val today = remember { engine.today() }
    val date = remember(argYear, argMonth, argDay) {
        val y = if (engine.supportedRange().contains(argYear)) argYear else today.year
        val m = if (argMonth in 1..12) argMonth else today.month
        val maxD = runCatching { engine.monthLength(y, m) }.getOrDefault(30)
        val d = if (argDay in 1..maxD) argDay else today.day.coerceAtMost(maxD)
        NepaliDate(y, m, d)
    }
    val adDate = remember(date) { engine.bsToAd(date) }
    val weekdayIdx = remember(date) { engine.weekdayIndexOf(date) }
    val currentWeather by com.neptools.app.core.util.WeatherLocationManager.currentWeather.collectAsState()
    val solar = remember(adDate, currentWeather) { SolarCalc.compute(adDate, currentWeather.lat, currentWeather.lon) }
    val panchang = remember(adDate) { PanchangCalc.compute(adDate) }
    val festival = PatroRepo.d.festivalsFor(date.year, date.month)[date.day]

    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"

    var userEvents by remember(date) {
        mutableStateOf(UserEventManager.getEventsForDate(context, date.year, date.month, date.day))
    }

    var showAddEventDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventNote by remember { mutableStateOf("") }
    var newEventReminder by remember { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ToolTopBar(
            title = T("day_detail"),
            subtitle = if (isEn) "Panchang, tithi & daily events" else "पञ्चाङ्ग, तिथि तथा दैनिक विवरण",
            onBack = onBack,
            actions = {
                IconButton(onClick = {
                    val file = com.neptools.app.core.util.PatroGraphicGenerator.createDailyPatroCard(
                        context = context,
                        date = date,
                        adDate = adDate,
                        panchang = panchang,
                        solar = solar,
                        festivals = festival.orEmpty(),
                        isEn = isEn
                    )
                    val shareTitle = if (isEn)
                        "Nepali Patro - ${date.year}/${date.month}/${date.day}"
                    else
                        "दैनिक पञ्चाङ्ग - वि.सं. ${date.year}/${date.month}/${date.day}"
                    com.neptools.app.core.util.PatroGraphicGenerator.shareCardImage(context, file, shareTitle)
                }) {
                    Icon(PIcons.Share, contentDescription = "Share Patro Card", tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            modifier = Modifier.padding(horizontal = 0.dp)
        )

        Box(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(npNum(date.day), style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.size(width = 13.dp, height = 0.dp))
                Column(Modifier.padding(bottom = 8.dp)) {
                    Text(
                        "${if (isEn) NepaliNames.monthsEn[date.month - 1] else NepaliNames.monthsNp[date.month - 1]} ${npNum(date.day)}, ${npNum(date.year)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        if (isEn) "${adDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${adDate.dayOfMonth}, ${adDate.year}"
                        else "${NepaliNames.adMonthsNp[adDate.monthValue - 1]} ${NepaliNames.toDevanagari(adDate.dayOfMonth)}, ${NepaliNames.toDevanagari(adDate.year)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (isEn) NepaliNames.weekdaysEn[weekdayIdx] else NepaliNames.weekdaysNp[weekdayIdx],
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            val tName = if (isEn) panchang.tithiNameEn.ifBlank { panchang.tithiName } else panchang.tithiName
            StampBadge(tName, if (isEn) "Tithi" else "तिथि", Modifier.align(Alignment.TopEnd))
        }

        Spacer(Modifier.height(14.dp))
        val pName = if (isEn) panchang.pakshaEn.ifBlank { panchang.paksha } else panchang.paksha
        val nakName = if (isEn) panchang.nakshatraNameEn.ifBlank { panchang.nakshatraName } else panchang.nakshatraName
        val yogName = if (isEn) panchang.yogaNameEn.ifBlank { panchang.yogaName } else panchang.yogaName

        LedgerCard(rows = listOf(
            (if (isEn) "Paksha" else "पक्ष") to pName,
            (if (isEn) "Nakshatra" else "नक्षत्र") to nakName,
            (if (isEn) "Yoga" else "योग") to yogName,
            (if (isEn) "Sunrise" else "सूर्योदय") to SolarCalc.formatTime(solar.sunrise, isEn, ThemePrefsNp()),
            (if (isEn) "Sunset" else "सूर्यास्त") to SolarCalc.formatTime(solar.sunset, isEn, ThemePrefsNp()),
            (if (isEn) "Rahu Kaal" else "राहु काल") to rahuText(solar, ThemePrefsNp())
        ))
        Spacer(Modifier.height(12.dp))
        if (festival != null) {
            festival.forEach { f ->
                val fName = if (isEn) f.nameEn.ifBlank { f.nameNp } else f.nameNp
                val holidayTag = if (f.isPublicHoliday) (if (isEn) " · Public Holiday" else " · सार्वजनिक बिदा") else ""
                Text(
                    fName + holidayTag,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (f.isPublicHoliday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEn) "Personal Events & Notes" else "व्यक्तिगत कार्यक्रम तथा नोट",
                style = MaterialTheme.typography.titleMedium
            )
            FilledTonalButton(
                onClick = {
                    newEventTitle = ""
                    newEventNote = ""
                    newEventReminder = true
                    showAddEventDialog = true
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (isEn) "+ Add Event" else "+ कार्यक्रम थप्नुहोस्", style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(8.dp))

        if (userEvents.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) "No personal events scheduled for this day" else "यस दिनमा कुनै व्यक्तिगत कार्यक्रम छैन",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                userEvents.forEach { ev ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(ev.title, style = MaterialTheme.typography.titleSmall)
                            if (ev.note.isNotBlank()) {
                                Text(
                                    ev.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (ev.hasReminder) {
                                Text(
                                    if (isEn) "Reminder: 8:00 AM" else "सम्झना: बिहान ८:००",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                UserEventManager.deleteEvent(context, ev.id)
                                userEvents = UserEventManager.getEventsForDate(context, date.year, date.month, date.day)
                            }
                        ) {
                            Icon(
                                PIcons.Trash,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        var scheduled = false
        InkButton(
            text = com.neptools.app.ui.strings.T("set_reminder"),
            onClick = {
                val at = java.time.LocalDateTime.of(adDate, java.time.LocalTime.of(8, 0))
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
                if (at <= System.currentTimeMillis()) {
                    android.widget.Toast.makeText(
                        context,
                        if (isEn) "Cannot set reminder for past dates" else "बितेको मितिमा सम्झना राख्न मिल्दैन",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val mName = if (isEn) NepaliNames.monthsEn[date.month - 1] else NepaliNames.monthsNp[date.month - 1]
                    val festLabel = if (isEn) (festival?.firstOrNull()?.nameEn ?: "Special Day") else (festival?.firstOrNull()?.nameNp ?: "यो दिन विशेष छ")
                    scheduled = com.neptools.app.core.reminder.ReminderHelper.schedule(
                        context,
                        "$mName ${date.day} — $festLabel",
                        at
                    )
                    android.widget.Toast.makeText(
                        context,
                        if (scheduled) (if (isEn) "Reminder set for 8:00 AM" else "सम्झना सेट भयो (बिहान ८:००)") else (if (isEn) "Could not set reminder" else "सम्झना सेट गर्न सकिएन"),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showAddEventDialog) {
            AlertDialog(
                onDismissRequest = { showAddEventDialog = false },
                title = {
                    Text(
                        if (isEn) "Add Personal Event" else "व्यक्तिगत कार्यक्रम थप्नुहोस्",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newEventTitle,
                            onValueChange = { newEventTitle = it },
                            label = { Text(if (isEn) "Event Title" else "कार्यक्रमको शीर्षक") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newEventNote,
                            onValueChange = { newEventNote = it },
                            label = { Text(if (isEn) "Note / Details (optional)" else "विवरण / नोट (ऐच्छिक)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isEn) "Notification Reminder (8:00 AM)" else "सूचना रिमाइन्डर (बिहान ८:००)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Switch(
                                checked = newEventReminder,
                                onCheckedChange = { newEventReminder = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newEventTitle.isNotBlank()) {
                                val ev = UserCalendarEvent(
                                    yearBs = date.year,
                                    monthBs = date.month,
                                    dayBs = date.day,
                                    title = newEventTitle.trim(),
                                    note = newEventNote.trim(),
                                    hasReminder = newEventReminder
                                )
                                UserEventManager.saveEvent(context, ev)
                                userEvents = UserEventManager.getEventsForDate(context, date.year, date.month, date.day)
                                showAddEventDialog = false
                            }
                        },
                        enabled = newEventTitle.isNotBlank()
                    ) {
                        Text(if (isEn) "Save" else "सेभ गर्नुहोस्")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEventDialog = false }) {
                        Text(if (isEn) "Cancel" else "रद्द गर्नुहोस्")
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ThemePrefsNp(): Boolean =
    com.neptools.app.ui.theme.ThemePrefs.nepaliDigits.value

private fun rahuText(solar: SolarDay, nepaliDigits: Boolean): String {
    val s = solar.rahuStart ?: return "—"
    val e = solar.rahuEnd ?: return "—"
    val raw = "%02d:%02d – %02d:%02d".format(s.hour, s.minute, e.hour, e.minute)
    return if (nepaliDigits) NepaliNames.toDevanagari(raw) else raw
}

@Composable
private fun LedgerCard(rows: List<Pair<String, String>>) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(vertical = 4.dp, horizontal = 14.dp)
    ) {
        rows.forEachIndexed { i, (k, v) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(k, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(v, style = MaterialTheme.typography.bodyLarge)
            }
            if (i < rows.lastIndex) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
            }
        }
    }
}
