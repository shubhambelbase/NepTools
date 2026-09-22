package com.neptools.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.converter.LandConverter
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.util.Locale

private enum class LandInputMode {
    HILLY,   // Ropani - Aana - Paisa - Daam
    TERAI,   // Bigha - Katha - Dhur - Kanwa
    SQFT,    // Square Feet
    SQM      // Square Meter
}

@Composable
fun LandConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val isEn = ThemePrefs.lang.value == "en"

    var selectedTab by remember { mutableIntStateOf(0) }

    // Converter Inputs
    var inputMode by remember { mutableStateOf(LandInputMode.HILLY) }

    // Hilly inputs
    var inRopani by remember { mutableStateOf("1") }
    var inAana by remember { mutableStateOf("0") }
    var inPaisa by remember { mutableStateOf("0") }
    var inDaam by remember { mutableStateOf("0") }

    // Terai inputs
    var inBigha by remember { mutableStateOf("0") }
    var inKatha by remember { mutableStateOf("1") }
    var inDhur by remember { mutableStateOf("0") }
    var inKanwa by remember { mutableStateOf("0") }

    // Metric / Imperial inputs
    var inSqFt by remember { mutableStateOf("5476") }
    var inSqM by remember { mutableStateOf("508.74") }

    // Derived total square feet based on current input mode
    val currentTotalSqFt = remember(
        inputMode, inRopani, inAana, inPaisa, inDaam,
        inBigha, inKatha, inDhur, inKanwa, inSqFt, inSqM
    ) {
        when (inputMode) {
            LandInputMode.HILLY -> {
                val r = inRopani.toIntOrNull() ?: 0
                val a = inAana.toIntOrNull() ?: 0
                val p = inPaisa.toIntOrNull() ?: 0
                val d = inDaam.toDoubleOrNull() ?: 0.0
                LandConverter.ropaniToSqFt(r, a, p, d)
            }
            LandInputMode.TERAI -> {
                val b = inBigha.toIntOrNull() ?: 0
                val k = inKatha.toIntOrNull() ?: 0
                val dh = inDhur.toIntOrNull() ?: 0
                val kw = inKanwa.toDoubleOrNull() ?: 0.0
                LandConverter.bighaToSqFt(b, k, dh, kw)
            }
            LandInputMode.SQFT -> {
                inSqFt.toDoubleOrNull() ?: 0.0
            }
            LandInputMode.SQM -> {
                val m = inSqM.toDoubleOrNull() ?: 0.0
                LandConverter.sqMetersToSqFt(m)
            }
        }
    }

    val calculation = remember(currentTotalSqFt) {
        LandConverter.calculateFromSqFt(currentTotalSqFt)
    }

    fun shareLandBreakdown() {
        val rb = calculation.ropaniBreakdown
        val bb = calculation.bighaBreakdown
        val dStr = if (rb.daam % 1.0 == 0.0) rb.daam.toInt().toString() else "%.2f".format(Locale.US, rb.daam)
        val kStr = if (bb.kanwa % 1.0 == 0.0) bb.kanwa.toInt().toString() else "%.2f".format(Locale.US, bb.kanwa)
        val sqFtStr = "%,.2f".format(Locale.US, calculation.sqFt)
        val sqMStr = "%,.2f".format(Locale.US, calculation.sqMeters)
        val acresStr = "%.4f".format(Locale.US, calculation.acres)
        val haStr = "%.4f".format(Locale.US, calculation.hectares)

        val summary = buildString {
            append(if (isEn) "NepTools - Land Area Conversion Breakdown" else "नेपटूल्स - जग्गा नाप रूपान्तरण विवरण")
            append("\n----------------------------------------\n")
            if (isEn) {
                append("Hilly / Valley System:\n")
                append("  • Ropani-Aana-Paisa-Daam: ${rb.formatCompact()}\n")
                append("  • Breakdown: ${rb.ropani} Ropani, ${rb.aana} Aana, ${rb.paisa} Paisa, $dStr Daam\n\n")
                append("Terai System:\n")
                append("  • Bigha-Katha-Dhur-Kanwa: ${bb.formatCompact()}\n")
                append("  • Breakdown: ${bb.bigha} Bigha, ${bb.katha} Katha, ${bb.dhur} Dhur, $kStr Kanwa\n\n")
                append("Metric & Imperial Units:\n")
                append("  • Square Feet: $sqFtStr sq. ft.\n")
                append("  • Square Metres: $sqMStr sq. m.\n")
                append("  • Acres: $acresStr acres\n")
                append("  • Hectares: $haStr ha\n")
            } else {
                append("पहाडी प्रणाली (काठमाडौं उपत्यका र पहाड):\n")
                append("  • रोपनी-आना-पैसा-दाम: ${NepaliNames.toDevanagari(rb.ropani)}-${NepaliNames.toDevanagari(rb.aana)}-${NepaliNames.toDevanagari(rb.paisa)}-${NepaliNames.toDevanagari(dStr)}\n")
                append("  • विस्तृत: ${NepaliNames.toDevanagari(rb.ropani)} रोपनी, ${NepaliNames.toDevanagari(rb.aana)} आना, ${NepaliNames.toDevanagari(rb.paisa)} पैसा, ${NepaliNames.toDevanagari(dStr)} दाम\n\n")
                append("तराई प्रणाली (भित्री मधेस तथा तराई):\n")
                append("  • बिघा-कठ्ठा-धुर-कन्वा: ${NepaliNames.toDevanagari(bb.bigha)}-${NepaliNames.toDevanagari(bb.katha)}-${NepaliNames.toDevanagari(bb.dhur)}-${NepaliNames.toDevanagari(kStr)}\n")
                append("  • विस्तृत: ${NepaliNames.toDevanagari(bb.bigha)} बिघा, ${NepaliNames.toDevanagari(bb.katha)} कठ्ठा, ${NepaliNames.toDevanagari(bb.dhur)} धुर, ${NepaliNames.toDevanagari(kStr)} कन्वा\n\n")
                append("मेट्रिक तथा अन्तर्राष्ट्रिय एकाइहरू:\n")
                append("  • वर्ग फिट: ${NepaliNames.toDevanagari(sqFtStr)} वर्ग फिट\n")
                append("  • वर्ग मिटर: ${NepaliNames.toDevanagari(sqMStr)} वर्ग मिटर\n")
                append("  • एकड: ${NepaliNames.toDevanagari(acresStr)} एकड\n")
                append("  • हेक्टर: ${NepaliNames.toDevanagari(haStr)} हेक्टर\n")
            }
            append("----------------------------------------\n")
            append(if (isEn) "Calculated via NepTools" else "नेपटूल्स द्वारा हिसाब गरिएको")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share Land Area Breakdown" else "जग्गा विवरण शेयर गर्नुहोस्"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        ToolTopBar(
            title = if (isEn) "Land Area Converter" else "जग्गा नाप रूपान्तरण",
            subtitle = if (isEn) "Ropani, Bigha, Sq. Ft & Sq. Metre" else "रोपनी, आना, पैसा, दाम र बिघा, कठ्ठा, धुर",
            onBack = onBack
        )

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        if (isEn) "Converter" else "रूपान्तरण",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        if (isEn) "Parcel Math" else "जग्गा जोड / घटाउ",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        if (isEn) "Reference Table" else "नाप नक्शा तालिका",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        when (selectedTab) {
            0 -> {
                // Tab 0: Cross Converter
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Input Mode Selector Chips
                        Column {
                            Text(
                                text = if (isEn) "Select Input Unit:" else "नाप प्रविष्ट गर्ने एकाइ छान्नुहोस्:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    UnitChip(
                                        label = if (isEn) "Hilly (Ropani)" else "पहाड (रोपनी-आना-पैसा-दाम)",
                                        selected = inputMode == LandInputMode.HILLY,
                                        onClick = { inputMode = LandInputMode.HILLY }
                                    )
                                }
                                item {
                                    UnitChip(
                                        label = if (isEn) "Terai (Bigha)" else "तराई (बिघा-कठ्ठा-धुर)",
                                        selected = inputMode == LandInputMode.TERAI,
                                        onClick = { inputMode = LandInputMode.TERAI }
                                    )
                                }
                                item {
                                    UnitChip(
                                        label = if (isEn) "Square Feet" else "वर्ग फिट",
                                        selected = inputMode == LandInputMode.SQFT,
                                        onClick = { inputMode = LandInputMode.SQFT }
                                    )
                                }
                                item {
                                    UnitChip(
                                        label = if (isEn) "Square Metres" else "वर्ग मिटर",
                                        selected = inputMode == LandInputMode.SQM,
                                        onClick = { inputMode = LandInputMode.SQM }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Input Fields Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            when (inputMode) {
                                LandInputMode.HILLY -> {
                                    Text(
                                        text = if (isEn) "Enter Hilly / Valley Land Area" else "पहाडी जग्गाको क्षेत्रफल प्रविष्ट गर्नुहोस्",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        LandInputField(value = inRopani, onValueChange = { inRopani = it }, label = if (isEn) "Ropani" else "रोपनी", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                        LandInputField(value = inAana, onValueChange = { inAana = it }, label = if (isEn) "Aana" else "आना", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                        LandInputField(value = inPaisa, onValueChange = { inPaisa = it }, label = if (isEn) "Paisa" else "पैसा", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                        LandInputField(value = inDaam, onValueChange = { inDaam = it }, label = if (isEn) "Daam" else "दाम", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                    }
                                }
                                LandInputMode.TERAI -> {
                                    Text(
                                        text = if (isEn) "Enter Terai Land Area" else "तराई जग्गाको क्षेत्रफल प्रविष्ट गर्नुहोस्",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        LandInputField(value = inBigha, onValueChange = { inBigha = it }, label = if (isEn) "Bigha" else "बिघा", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                        LandInputField(value = inKatha, onValueChange = { inKatha = it }, label = if (isEn) "Katha" else "कठ्ठा", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                        LandInputField(value = inDhur, onValueChange = { inDhur = it }, label = if (isEn) "Dhur" else "धुर", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                        LandInputField(value = inKanwa, onValueChange = { inKanwa = it }, label = if (isEn) "Kanwa" else "कन्वा", modifier = Modifier.weight(1f), onDone = { focusManager.clearFocus() })
                                    }
                                }
                                LandInputMode.SQFT -> {
                                    Text(
                                        text = if (isEn) "Enter Square Feet" else "वर्ग फिट प्रविष्ट गर्नुहोस्",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    LandInputField(
                                        value = inSqFt,
                                        onValueChange = { inSqFt = it },
                                        label = if (isEn) "Total Square Feet" else "जम्मा वर्ग फिट",
                                        modifier = Modifier.fillMaxWidth(),
                                        onDone = { focusManager.clearFocus() }
                                    )
                                }
                                LandInputMode.SQM -> {
                                    Text(
                                        text = if (isEn) "Enter Square Metres" else "वर्ग मिटर प्रविष्ट गर्नुहोस्",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    LandInputField(
                                        value = inSqM,
                                        onValueChange = { inSqM = it },
                                        label = if (isEn) "Total Square Metres" else "जम्मा वर्ग मिटर",
                                        modifier = Modifier.fillMaxWidth(),
                                        onDone = { focusManager.clearFocus() }
                                    )
                                }
                            }
                        }
                    }

                    // Hilly Output Card
                    item {
                        AreaResultCard(
                            systemTitle = if (isEn) "Hilly / Valley System" else "पहाडी प्रणाली (काठमाडौं उपत्यका र पहाड)",
                            primaryText = calculation.ropaniBreakdown.formatDescriptive(isEn),
                            compactTag = calculation.ropaniBreakdown.formatCompact(),
                            detailLines = listOf(
                                (if (isEn) "Ropani" else "रोपनी") to "${calculation.ropaniBreakdown.ropani}",
                                (if (isEn) "Aana" else "आना") to "${calculation.ropaniBreakdown.aana}",
                                (if (isEn) "Paisa" else "पैसा") to "${calculation.ropaniBreakdown.paisa}",
                                (if (isEn) "Daam" else "दाम") to "${calculation.ropaniBreakdown.daam}"
                            ),
                            accentColor = Color(0xFF0284C7),
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(calculation.ropaniBreakdown.formatDescriptive(isEn)))
                                Toast.makeText(context, if (isEn) "Copied Hilly Area" else "पहाडी जग्गाको विवरण कपी भयो", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Terai Output Card
                    item {
                        AreaResultCard(
                            systemTitle = if (isEn) "Terai System" else "तराई प्रणाली (भित्री मधेस तथा तराई)",
                            primaryText = calculation.bighaBreakdown.formatDescriptive(isEn),
                            compactTag = calculation.bighaBreakdown.formatCompact(),
                            detailLines = listOf(
                                (if (isEn) "Bigha" else "बिघा") to "${calculation.bighaBreakdown.bigha}",
                                (if (isEn) "Katha" else "कठ्ठा") to "${calculation.bighaBreakdown.katha}",
                                (if (isEn) "Dhur" else "धुर") to "${calculation.bighaBreakdown.dhur}",
                                (if (isEn) "Kanwa" else "कन्वा") to "${calculation.bighaBreakdown.kanwa}"
                            ),
                            accentColor = Color(0xFF16A34A),
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(calculation.bighaBreakdown.formatDescriptive(isEn)))
                                Toast.makeText(context, if (isEn) "Copied Terai Area" else "तराई जग्गाको विवरण कपी भयो", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Metric and Imperial Card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (isEn) "International & Metric Units" else "अन्तर्राष्ट्रिय तथा मेट्रिक एकाइहरू",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))

                            MetricRow(label = if (isEn) "Square Feet" else "वर्ग फिट", value = "%.2f".format(calculation.sqFt))
                            MetricRow(label = if (isEn) "Square Metres" else "वर्ग मिटर", value = "%.2f".format(calculation.sqMeters))
                            MetricRow(label = if (isEn) "Acres" else "एकड", value = "%.4f".format(calculation.acres))
                            MetricRow(label = if (isEn) "Hectares" else "हेक्टर", value = "%.4f".format(calculation.hectares))
                        }
                    }

                    // 1-Tap Share Breakdown Button
                    item {
                        Button(
                            onClick = { shareLandBreakdown() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(PIcons.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isEn) "Share Area Breakdown" else "जग्गा नाप विवरण शेयर गर्नुहोस्",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                    }
                }
            }
            1 -> {
                // Tab 1: Land Parcel Addition and Subtraction
                LandParcelMathSection(isEn = isEn)
            }
            2 -> {
                // Tab 2: Official Reference Units
                LandReferenceSection(isEn = isEn)
            }
        }
    }
}

@Composable
private fun UnitChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LandInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun AreaResultCard(
    systemTitle: String,
    primaryText: String,
    compactTag: String,
    detailLines: List<Pair<String, String>>,
    accentColor: Color,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = systemTitle,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .clickable(onClick = onCopy)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(PIcons.Copy, contentDescription = null, tint = accentColor, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = compactTag,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = primaryText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for ((name, value) in detailLines) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun LandParcelMathSection(isEn: Boolean) {
    val focusManager = LocalFocusManager.current

    // Mode: Hilly or Terai for calculation
    var isHillyMath by remember { mutableStateOf(true) }

    // Parcel 1 (Base Land)
    var p1A by remember { mutableStateOf("2") }
    var p1B by remember { mutableStateOf("4") }
    var p1C by remember { mutableStateOf("1") }
    var p1D by remember { mutableStateOf("0") }

    // Parcel 2 (Add Land)
    var p2A by remember { mutableStateOf("1") }
    var p2B by remember { mutableStateOf("8") }
    var p2C by remember { mutableStateOf("2") }
    var p2D by remember { mutableStateOf("1") }

    // Parcel 3 (Deduct / Sold Land)
    var p3A by remember { mutableStateOf("0") }
    var p3B by remember { mutableStateOf("6") }
    var p3C by remember { mutableStateOf("0") }
    var p3D by remember { mutableStateOf("0") }

    val netResult = remember(isHillyMath, p1A, p1B, p1C, p1D, p2A, p2B, p2C, p2D, p3A, p3B, p3C, p3D) {
        if (isHillyMath) {
            val parcel1 = LandConverter.RopaniBreakdown(p1A.toIntOrNull() ?: 0, p1B.toIntOrNull() ?: 0, p1C.toIntOrNull() ?: 0, p1D.toDoubleOrNull() ?: 0.0)
            val parcel2 = LandConverter.RopaniBreakdown(p2A.toIntOrNull() ?: 0, p2B.toIntOrNull() ?: 0, p2C.toIntOrNull() ?: 0, p2D.toDoubleOrNull() ?: 0.0)
            val parcel3 = LandConverter.RopaniBreakdown(p3A.toIntOrNull() ?: 0, p3B.toIntOrNull() ?: 0, p3C.toIntOrNull() ?: 0, p3D.toDoubleOrNull() ?: 0.0)

            val added = LandConverter.addRopani(listOf(parcel1, parcel2))
            val net = LandConverter.subtractRopani(added, parcel3)
            Pair(net.formatDescriptive(isEn), "${"%.2f".format(net.totalSqFt)} Sq. Ft")
        } else {
            val parcel1 = LandConverter.BighaBreakdown(p1A.toIntOrNull() ?: 0, p1B.toIntOrNull() ?: 0, p1C.toIntOrNull() ?: 0, p1D.toDoubleOrNull() ?: 0.0)
            val parcel2 = LandConverter.BighaBreakdown(p2A.toIntOrNull() ?: 0, p2B.toIntOrNull() ?: 0, p2C.toIntOrNull() ?: 0, p2D.toDoubleOrNull() ?: 0.0)
            val parcel3 = LandConverter.BighaBreakdown(p3A.toIntOrNull() ?: 0, p3B.toIntOrNull() ?: 0, p3C.toIntOrNull() ?: 0, p3D.toDoubleOrNull() ?: 0.0)

            val added = LandConverter.addBigha(listOf(parcel1, parcel2))
            val net = LandConverter.subtractBigha(added, parcel3)
            Pair(net.formatDescriptive(isEn), "${"%.2f".format(net.totalSqFt)} Sq. Ft")
        }
    }

    val hillyLabels = if (isEn) listOf("Ropani", "Aana", "Paisa", "Daam") else listOf("रोपनी", "आना", "पैसा", "दाम")
    val teraiLabels = if (isEn) listOf("Bigha", "Katha", "Dhur", "Kanwa") else listOf("बिघा", "कठ्ठा", "धुर", "कन्वा")
    val activeLabels = if (isHillyMath) hillyLabels else teraiLabels

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UnitChip(
                    label = if (isEn) "Hilly (Ropani)" else "पहाडी (रोपनी/आना)",
                    selected = isHillyMath,
                    onClick = { isHillyMath = true }
                )
                UnitChip(
                    label = if (isEn) "Terai (Bigha)" else "तराई (बिघा/कठ्ठा)",
                    selected = !isHillyMath,
                    onClick = { isHillyMath = false }
                )
            }
        }

        item {
            // Net Land Calculation Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isEn) "Net Remaining Land:" else "जम्मा बाँकी जग्गाको नाप:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = netResult.first,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = netResult.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Parcel 1 Input
        item {
            ParcelInputCard(
                title = if (isEn) "Parcel 1 (Primary Land)" else "कित्ता १ (मुख्य जग्गा)",
                valA = p1A, onValAChange = { p1A = it },
                valB = p1B, onValBChange = { p1B = it },
                valC = p1C, onValCChange = { p1C = it },
                valD = p1D, onValDChange = { p1D = it },
                labels = activeLabels,
                onDone = { focusManager.clearFocus() }
            )
        }

        // Parcel 2 Input
        item {
            ParcelInputCard(
                title = if (isEn) "+ Parcel 2 (Additional Plot)" else "+ कित्ता २ (थप जग्गा)",
                valA = p2A, onValAChange = { p2A = it },
                valB = p2B, onValBChange = { p2B = it },
                valC = p2C, onValCChange = { p2C = it },
                valD = p2D, onValDChange = { p2D = it },
                labels = activeLabels,
                onDone = { focusManager.clearFocus() }
            )
        }

        // Parcel 3 Input (Deduction)
        item {
            ParcelInputCard(
                title = if (isEn) "- Deduct / Road / Partition" else "- बेचिएको वा बाटोमा काटिएको जग्गा",
                valA = p3A, onValAChange = { p3A = it },
                valB = p3B, onValBChange = { p3B = it },
                valC = p3C, onValCChange = { p3C = it },
                valD = p3D, onValDChange = { p3D = it },
                labels = activeLabels,
                onDone = { focusManager.clearFocus() }
            )
        }
    }
}

@Composable
private fun ParcelInputCard(
    title: String,
    valA: String, onValAChange: (String) -> Unit,
    valB: String, onValBChange: (String) -> Unit,
    valC: String, onValCChange: (String) -> Unit,
    valD: String, onValDChange: (String) -> Unit,
    labels: List<String>,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LandInputField(value = valA, onValueChange = onValAChange, label = labels[0], modifier = Modifier.weight(1f), onDone = onDone)
            LandInputField(value = valB, onValueChange = onValBChange, label = labels[1], modifier = Modifier.weight(1f), onDone = onDone)
            LandInputField(value = valC, onValueChange = onValCChange, label = labels[2], modifier = Modifier.weight(1f), onDone = onDone)
            LandInputField(value = valD, onValueChange = onValDChange, label = labels[3], modifier = Modifier.weight(1f), onDone = onDone)
        }
    }
}

@Composable
private fun LandReferenceSection(isEn: Boolean) {
    val hillyStandards = if (isEn) listOf(
        Pair("1 Ropani", "16 Aana = 64 Paisa = 256 Daam = 5,476 Sq. Ft (508.74 m²)"),
        Pair("1 Aana", "4 Paisa = 16 Daam = 342.25 Sq. Ft (31.80 m²)"),
        Pair("1 Paisa", "4 Daam = 85.56 Sq. Ft (7.95 m²)"),
        Pair("1 Daam", "21.39 Sq. Ft (1.99 m²)")
    ) else listOf(
        Pair("१ रोपनी", "१६ आना = ६४ पैसा = २५६ दाम = ५,४७६ वर्ग फिट (५०८.७४ वर्ग मिटर)"),
        Pair("१ आना", "४ पैसा = १६ दाम = ३४२.२५ वर्ग फिट (३१.८० वर्ग मिटर)"),
        Pair("१ पैसा", "४ दाम = ८५.५६ वर्ग फिट (७.९५ वर्ग मिटर)"),
        Pair("१ दाम", "२१.३९ वर्ग फिट (१.९९ वर्ग मिटर)")
    )

    val teraiStandards = if (isEn) listOf(
        Pair("1 Bigha", "20 Katha = 400 Dhur = 1,600 Kanwa = 72,900 Sq. Ft (6,772.63 m²)"),
        Pair("1 Katha", "20 Dhur = 80 Kanwa = 3,645 Sq. Ft (338.63 m²)"),
        Pair("1 Dhur", "4 Kanwa = 182.25 Sq. Ft (16.93 m²)"),
        Pair("1 Kanwa", "45.56 Sq. Ft (4.23 m²)")
    ) else listOf(
        Pair("१ बिघा", "२० कठ्ठा = ४०० धुर = १,६०० कन्वा = ७२,९०० वर्ग फिट (६,७७२.६३ वर्ग मिटर)"),
        Pair("१ कठ्ठा", "२० धुर = ८० कन्वा = ३,६४५ वर्ग फिट (३३८.६३ वर्ग मिटर)"),
        Pair("१ धुर", "४ कन्वा = १८२.२५ वर्ग फिट (१६.९३ वर्ग मिटर)"),
        Pair("१ कन्वा", "४५.५६ वर्ग फिट (४.२३ वर्ग मिटर)")
    )

    val crossStandards = if (isEn) listOf(
        Pair("1 Bigha", "13.31 Ropani (13 Ropani 5 Aana)"),
        Pair("1 Ropani", "0.075 Bigha (1.50 Katha / 30.05 Dhur)"),
        Pair("1 Acre", "43,560 Sq. Ft = 7.95 Ropani = 0.60 Bigha"),
        Pair("1 Hectare", "107,639 Sq. Ft = 10,000 m² = 19.66 Ropani = 1.48 Bigha")
    ) else listOf(
        Pair("१ बिघा बराबर", "१३.३१ रोपनी (१३ रोपनी ५ आना)"),
        Pair("१ रोपनी बराबर", "०.०७५ बिघा (१.५० कठ्ठा वा ३०.०५ धुर)"),
        Pair("१ एकड", "४३,५६० वर्ग फिट = ७.९५ रोपनी = ०.६० बिघा"),
        Pair("१ हेक्टर", "१,०७,६३९ वर्ग फिट = १०,००० वर्ग मिटर = १९.६६ रोपनी = १.४८ बिघा")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (isEn) "Official Survey Standards" else "नेपाल सरकार नापी विभाग आधिकारिक मापदण्ड",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            ReferenceCard(
                title = if (isEn) "Hilly / Valley Standards" else "पहाडी तथा काठमाडौं उपत्यका नाप",
                items = hillyStandards
            )
        }

        item {
            ReferenceCard(
                title = if (isEn) "Terai Standards" else "तराई तथा भित्री मधेस नाप",
                items = teraiStandards
            )
        }

        item {
            ReferenceCard(
                title = if (isEn) "Cross System & Global Units" else "आपसी रूपान्तरण तथा अन्तर्राष्ट्रिय एकाइ",
                items = crossStandards
            )
        }
    }
}

@Composable
private fun ReferenceCard(title: String, items: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        for ((unit, formula) in items) {
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(text = unit, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = formula, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
