package com.neptools.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neptools.app.core.calendar.PanchangCalc
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class TithiEvent(val ad: LocalDate, val bs: String, val tithiNp: String, val tithiEn: String, val paksha: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkadashiListScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    val engine = PatroRepo.d.engine
    var filter by remember { mutableStateOf("Ekadashi") } // Ekadashi, Aunsi, Purnima, All
    var selectedYear by remember { mutableStateOf(runCatching { engine.today().year }.getOrDefault(engine.supportedRange().first)) }

    val events = remember(selectedYear) { generateEventsForYear(engine, selectedYear) }
    val filtered = remember(filter, events) {
        when (filter) {
            "Ekadashi" -> events.filter { it.tithiNp == "एकादशी" }
            "Aunsi" -> events.filter { it.tithiNp == "औंसी" }
            "Purnima" -> events.filter { it.tithiNp == "पूर्णिमा" }
            else -> events
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEn) "Ekadashi / Aunsi / Purnima" else "एकादशी / औंसी / पूर्णिमा", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(PIcons.ChevronLeft, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Year picker row
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isEn) "BS Year" else "बि.सं. वर्ष", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { if (selectedYear > engine.supportedRange().first) selectedYear-- }) { Icon(PIcons.ChevronLeft, contentDescription = null) }
                    Text("$selectedYear", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 10.dp))
                    IconButton(onClick = { if (selectedYear < engine.supportedRange().last) selectedYear++ }) { Icon(PIcons.ChevronRight, contentDescription = null) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter == "Ekadashi", onClick = { filter = "Ekadashi" }, label = { Text(if (isEn) "Ekadashi" else "एकादशी") })
                FilterChip(selected = filter == "Aunsi", onClick = { filter = "Aunsi" }, label = { Text(if (isEn) "Aunsi" else "औंसी") })
                FilterChip(selected = filter == "Purnima", onClick = { filter = "Purnima" }, label = { Text(if (isEn) "Purnima" else "पूर्णिमा") })
                FilterChip(selected = filter == "All", onClick = { filter = "All" }, label = { Text(if (isEn) "All" else "सबै") })
            }

            Text(
                text = if (isEn) "BS $selectedYear • ${filtered.size} events" else "बि.सं. $selectedYear • ${filtered.size} वटा",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { e ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${e.tithiNp} • ${e.paksha}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("${e.tithiEn}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text("AD ${e.ad.format(DateTimeFormatter.ISO_DATE)}  •  BS ${e.bs}", style = MaterialTheme.typography.bodySmall)
                            }
                            AssistChip(onClick = {}, label = { Text(if (isEn) e.tithiEn else e.tithiNp) })
                        }
                    }
                }
            }
        }
    }
}

private fun generateEventsForYear(engine: com.neptools.app.core.calendar.BsCalendarEngine, bsYear: Int): List<TithiEvent> {
    val out = mutableListOf<TithiEvent>()
    for (m in 1..12) {
        val len = runCatching { engine.monthLength(bsYear, m) }.getOrDefault(0)
        for (d in 1..len) {
            val np = com.neptools.app.core.calendar.NepaliDate(bsYear, m, d)
            val ad = runCatching { engine.bsToAd(np) }.getOrNull() ?: continue
            val p = PanchangCalc.compute(ad)
            val t = p.tithiName
            if (t == "एकादशी" || t == "औंसी" || t == "पूर्णिमा") {
                out.add(TithiEvent(ad, "$bsYear/$m/$d", t, p.tithiNameEn, p.paksha))
            }
        }
    }
    return out
}
