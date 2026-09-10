package com.neptools.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

private data class MuhuratPurpose(val np: String, val en: String, val avoidTithis: Set<String>, val avoidYogas: Set<String>)
private data class MuhuratResult(val ad: LocalDate, val bsText: String, val panchang: com.neptools.app.core.calendar.Panchang, val score: Int, val reason: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuhuratFinderScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    val engine = PatroRepo.d.engine
    val purposes = remember {
        listOf(
            MuhuratPurpose("विवाह (Bihe)", "Marriage", setOf("औंसी", "चतुर्दशी"), setOf("व्यतिपात", "वैधृति")),
            MuhuratPurpose("ब्रतबन्ध", "Bratabandha", setOf("औंसी", "पूर्णिमा"), setOf("व्यतिपात")),
            MuhuratPurpose("पास्नी (Annaprashana)", "Pasni", setOf("औंसी", "चतुर्थी"), setOf("व्यतिपात", "परिघ")),
            MuhuratPurpose("गृह प्रवेश", "Griha Pravesh", setOf("औंसी"), setOf("व्यतिपात", "वैधृति", "परिघ")),
            MuhuratPurpose("व्यापार सुरुवात", "Business Start", setOf("औंसी"), setOf("व्यतिपात"))
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(purposes[0]) }

    val results = remember(selected) {
        val today = LocalDate.now()
        // Till end of world: up to engine last supported BS date (2099/2100) converted to AD
        val lastBsYear = engine.supportedRange().last
        val lastBsMonth = 12
        val lastBsDay = runCatching { engine.monthLength(lastBsYear, lastBsMonth) }.getOrDefault(30)
        val lastAd = runCatching { engine.bsToAd(com.neptools.app.core.calendar.NepaliDate(lastBsYear, lastBsMonth, lastBsDay)) }.getOrDefault(today.plusYears(50))
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
            val badDay = when (selected.np) {
                "विवाह (Bihe)" -> tBad || yBad || isSaturday
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEn) "Muhurat Finder" else "साइत खोजकर्ता", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(PIcons.ChevronLeft, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (isEn) "Select Purpose" else "कार्य छान्नुहोस्", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = if (isEn) selected.en else selected.np,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            purposes.forEach { p ->
                                DropdownMenuItem(text = { Text(if (isEn) p.en else p.np) }, onClick = { selected = p; expanded = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isEn) "Uses PanchangCalc (tithi/nakshatra/yoga) to avoid inauspicious combinations. For precise muhurat consult local jyotish." else "पञ्चाङ्ग (तिथि/नक्षत्र/योग) अनुसार अशुभ योग हटाएर सुझाव। अन्तिम साइतका लागि स्थानीय ज्योतिषसँग परामर्श गर्नुहोस्।",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(if (isEn) "Next 20 Auspicious Dates" else "अर्को २० शुभ मिति", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            results.forEach { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${r.ad}  •  BS ${r.bsText}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("${r.score}/100", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Text("${r.panchang.paksha} • ${r.panchang.tithiName} (${r.panchang.tithiNameEn})", style = MaterialTheme.typography.bodyMedium)
                        Text("${r.panchang.nakshatraName} • ${r.panchang.yogaName}  —  ${r.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (results.isEmpty()) {
                Text(if (isEn) "No auspicious date found till end of calendar." else "क्यालेन्डरको अन्त्यसम्म शुभ साइत भेटिएन।", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
