package com.neptools.app.ui.screens

import com.neptools.app.ui.strings.T

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.SectionTitle
import com.neptools.app.ui.components.npNum

@Composable
fun CalendarScreen(
    argYear: Int,
    argMonth: Int,
    onOpenDay: (Int, Int, Int) -> Unit
) {
    val engine = PatroRepo.d.engine
    val today = remember { engine.today() }
    var year by remember { mutableIntStateOf(if (argYear in engine.supportedRange()) argYear else today.year) }
    var month by remember { mutableIntStateOf(if (argMonth in 1..12) argMonth else today.month) }

    LaunchedEffect(argYear, argMonth) {
        if (argYear in engine.supportedRange()) year = argYear
        if (argMonth in 1..12) month = argMonth
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val festivals = remember(year, month) { PatroRepo.d.festivalsFor(year, month) }
    val userEvents = remember(year, month) {
        com.neptools.app.core.reminder.UserEventManager.getEventDaysForMonth(context, year, month)
    }
    val grid = remember(year, month, userEvents) {
        engine.buildMonthGrid(year, month, today, festivals, userEvents)
    }
    val firstAd = remember(year, month) { engine.bsToAd(NepaliDate(year, month, 1)) }
    val lastDay = remember(year, month) { engine.monthLength(year, month) }
    val lastAd = remember(year, month, lastDay) { engine.bsToAd(NepaliDate(year, month, lastDay)) }

    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        if (isEn) NepaliNames.monthsEn[month - 1] else NepaliNames.monthsNp[month - 1],
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(Modifier.size(width = 8.dp, height = 0.dp))
                    Text(
                        npNum(year),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    if (isEn) "${firstAd.month.name.take(3)} ${firstAd.dayOfMonth} – ${lastAd.month.name.take(3)} ${lastAd.dayOfMonth}, ${lastAd.year}"
                    else "${npNum(firstAd.dayOfMonth)} – ${npNum(lastAd.dayOfMonth)}, ${npNum(lastAd.year)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NavArrow("‹", enabled = month > 1 || year > engine.supportedRange().first) {
                    if (month == 1) { year -= 1; month = 12 } else month -= 1
                }
                NavArrow("›", enabled = month < 12 || year < engine.supportedRange().last) {
                    if (month == 12) { year += 1; month = 1 } else month += 1
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        val weekdays = if (isEn) NepaliNames.weekdaysEnShort else NepaliNames.weekdaysNpShort
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            weekdays.forEachIndexed { i, d ->
                Text(
                    d,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        grid.forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { cell ->
                    Box(Modifier.weight(1f).aspectRatio(0.92f)) {
                        cell?.let { DayCell(it, onOpenDay) }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(MaterialTheme.colorScheme.primary, if (isEn) "Festival / Holiday" else "पर्व / बिदा")
            LegendDot(MaterialTheme.colorScheme.tertiary, if (isEn) "Today" else "आज")
        }

        Spacer(Modifier.height(14.dp))
        SectionTitle(T("fest_this_month"), T("fest_sub"))
        Spacer(Modifier.height(8.dp))
        festivals.toSortedMap().forEach { (_, list) ->
            list.forEach { f ->
                FestivalRow(f.day, f.nameNp, f.nameEn, f.isPublicHoliday) {
                    onOpenDay(year, month, f.day)
                }
                Spacer(Modifier.height(6.dp))
            }
        }
        if (festivals.isEmpty()) {
            Text(
                if (isEn) "No recorded festivals this month" else "यस महिनामा दर्ता पर्व छैन",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}
