package com.neptools.app.astrology.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.data.BirthData
import com.neptools.app.core.data.Place
import com.neptools.app.core.data.PlacesRepo
import java.time.LocalTime

@Composable
fun BirthDetailsScreen(
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val saved = AstroRepo.birth.value
    val context = LocalContext.current
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"

    var dateText by remember { mutableStateOf(saved?.date?.toString() ?: "2000-01-01") }
    var timeText by remember { mutableStateOf(saved?.time?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "06:00") }
    var latText by remember { mutableStateOf(saved?.latitude?.toString() ?: "27.7172") }
    var lonText by remember { mutableStateOf(saved?.longitude?.toString() ?: "85.3240") }
    var tzText by remember { mutableStateOf(saved?.tzOffsetHours?.toString() ?: "5.75") }
    var place by remember { mutableStateOf(saved?.placeLabel ?: "") }
    var uncertain by remember { mutableStateOf(saved?.birthTimeUncertain ?: false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    fun submit() {
        val date = runCatching { java.time.LocalDate.parse(dateText.trim()) }.getOrNull()
        val time = runCatching { LocalTime.parse(timeText.trim()) }.getOrNull()
        val lat = latText.toDoubleOrNull()
        val lon = lonText.toDoubleOrNull()
        val tz = tzText.toDoubleOrNull()
        if (date == null) { error = if (isEn) "Date must be in YYYY-MM-DD format" else "मिति YYYY-MM-DD ढाँचामा होस्"; return }
        if (time == null) { error = if (isEn) "Time must be in HH:MM format" else "समय HH:MM ढाँचामा होस्"; return }
        if (lat == null || lon == null || tz == null) { error = if (isEn) "Lat/Lon/TZ must be valid numbers" else "अक्षांश/देशान्तर/समय क्षेत्र संख्या हुनुपर्छ"; return }
        val label = place.ifBlank {
            selectedPlace?.let { "${it.en}, ${it.district}" } ?: "Custom"
        }
        val bd = BirthData(date, time, lat, lon, tz, label, uncertain)
        val err = bd.validationError()
        if (err != null) { error = err; return }
        runCatching { AstroRepo.saveBirth(context, bd) }
            .onFailure { error = it.message; return }
        onDone()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(com.neptools.app.ui.icons.PIcons.ChevronLeft, if (isEn) "Back" else "फिर्ता", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(width = 14.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "Birth Details" else "जन्म विवरण",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (isEn) "Enter date, time & location for precise chart" else "कुण्डलीका लागि मिति, समय र स्थान",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        FieldRow(if (isEn) "Birth Date (AD)" else "जन्म मिति", if (isEn) "YYYY-MM-DD" else "YYYY-MM-DD", dateText) { dateText = it }
        FieldRow(if (isEn) "Birth Time (24h)" else "जन्म समय", if (isEn) "HH:MM" else "HH:MM", timeText) { timeText = it }

        Spacer(Modifier.height(8.dp))
        PlaceSelector(
            initialSelected = selectedPlace,
            onSelected = { p ->
                selectedPlace = p
                place = "${p.en}, ${p.district}".trim(',', ' ')
                latText = "%.4f".format(p.lat)
                lonText = "%.4f".format(p.lon)
                val parsedYear = runCatching { java.time.LocalDate.parse(dateText.trim()).year }.getOrNull()
                tzText = if (parsedYear != null && parsedYear < 1986) "5.5" else PlacesRepo.NEPAL_TZ.toString()
            }
        )

        Spacer(Modifier.height(8.dp))
        FieldRow(if (isEn) "Latitude (Lat)" else "अक्षांश", "-90..90", latText, KeyboardType.Decimal) { latText = it }
        FieldRow(if (isEn) "Longitude (Lon)" else "देशान्तर", "-180..180", lonText, KeyboardType.Decimal) { lonText = it }
        FieldRow(if (isEn) "TZ Offset (hrs)" else "समय क्षेत्र", if (isEn) "Nepal = 5.75 (pre-1986 = 5.5)" else "नेपाल = ५.७५ (१९८६ अघि = ५.५)", tzText, KeyboardType.Decimal) { tzText = it }

        val birthYear = runCatching { java.time.LocalDate.parse(dateText.trim()).year }.getOrNull()
        if (birthYear != null && birthYear < 1986) {
            Text(
                if (isEn) "Note: Nepal standardized to UTC+5:45 in 1986. Prior births used UTC+5:30 (IST)."
                else "जानकारी: नेपालले सन् १९८६ मा मात्र UTC+५:४५ अपनाएको हो। सोभन्दा अघि UTC+५:३० (IST) थियो।",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(22.dp)
                    .border(
                        1.dp,
                        if (uncertain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraSmall
                    )
                    .background(
                        if (uncertain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                    .clickable { uncertain = !uncertain },
                contentAlignment = Alignment.Center
            ) {
                Text(if (uncertain) "✓" else "",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.size(width = 10.dp, height = 0.dp))
            Text(
                if (isEn) "Exact birth time is uncertain (Lagna may vary)"
                else "जन्म समय अनिश्चित छ — लग्न कम भरपर्दो",
                style = MaterialTheme.typography.bodySmall
            )
        }

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(16.dp))
        com.neptools.app.ui.components.InkButton(
            text = if (isEn) "✦ Generate Kundali Chart" else "✦ कुण्डली बनाउनुहोस्",
            onClick = { submit() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun FieldRow(
    label: String,
    hint: String,
    value: String,
    keyboard: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> if (v.length <= 20) onChange(v) },
        label = { Text(label) },
        placeholder = { Text(hint) },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}
