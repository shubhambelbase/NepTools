package com.neptools.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.FuelRepo
import com.neptools.app.ui.components.InkButton
import com.neptools.app.ui.components.SoftCard
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.theme.ThemePrefs

private data class FuelProduct(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val price: Double,
    val unitNp: String,
    val unitEn: String,
    val primaryColor: Color,
    val lightColor: Color
)

@Composable
fun FuelPriceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEn = ThemePrefs.lang.value == "en"

    val weather by com.neptools.app.core.util.WeatherLocationManager.currentWeather.collectAsState()
    var fuelRates by remember { mutableStateOf(FuelRepo.loadCached(context)) }
    
    val gpsResolved = remember(fuelRates, weather) {
        com.neptools.app.core.util.FuelLocationResolver.resolveForCurrentLocation(fuelRates)
    }
    var selectedRegion by remember { mutableStateOf(gpsResolved.categoryKey) }
    var loading by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(gpsResolved.categoryKey) {
        selectedRegion = gpsResolved.categoryKey
    }

    fun refreshRates() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        loading = true
        com.neptools.app.core.util.WeatherLocationManager.requestLocationWeather(context, force = true)
        FuelRepo.refresh(context) { rs ->
            loading = false
            fuelRates = rs
            Toast.makeText(context, if (isEn) "Fuel rates updated successfully" else "इन्धन दरहरू सफलतापूर्वक अपडेट गरियो", Toast.LENGTH_SHORT).show()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        FuelRepo.refresh(context) { rs ->
            fuelRates = rs
        }
    }

    val regionPrices = fuelRates.categoryPrices[selectedRegion] ?: FuelRepo.defaultRates.categoryPrices["third"]!!

    val fuels = listOf(
        FuelProduct(
            id = "petrol",
            nameNp = "पेट्रोल",
            nameEn = "Petrol",
            price = regionPrices["petrol"] ?: 200.00,
            unitNp = "प्रति लिटर",
            unitEn = "per liter",
            primaryColor = Color(0xFFD32F2F),
            lightColor = Color(0xFFFFEBEE)
        ),
        FuelProduct(
            id = "diesel",
            nameNp = "डिजेल",
            nameEn = "Diesel",
            price = regionPrices["diesel"] ?: 200.00,
            unitNp = "प्रति लिटर",
            unitEn = "per liter",
            primaryColor = Color(0xFF1976D2),
            lightColor = Color(0xFFE3F2FD)
        ),
        FuelProduct(
            id = "kerosene",
            nameNp = "मट्टितेल",
            nameEn = "Kerosene",
            price = regionPrices["kerosene"] ?: 200.00,
            unitNp = "प्रति लिटर",
            unitEn = "per liter",
            primaryColor = Color(0xFF00897B),
            lightColor = Color(0xFFE0F2F1)
        ),
        FuelProduct(
            id = "lpg",
            nameNp = "एलपी ग्यास",
            nameEn = "LPG Cylinder",
            price = regionPrices["lpg"] ?: 2060.00,
            unitNp = "प्रति सिलिन्डर",
            unitEn = "per cylinder",
            primaryColor = Color(0xFFE65100),
            lightColor = Color(0xFFFFF3E0)
        ),
        FuelProduct(
            id = "atf",
            nameNp = "हवाई इन्धन",
            nameEn = "Aviation Fuel",
            price = regionPrices["atf"] ?: 249.00,
            unitNp = "प्रति लिटर",
            unitEn = "per liter",
            primaryColor = Color(0xFF7B1FA2),
            lightColor = Color(0xFFF3E5F5)
        )
    )

    val regions = listOf(
        Triple("third", if (isEn) "3rd Category" else "तेस्रो वर्ग", if (isEn) "Kathmandu, Pokhara, Dipayal" else "काठमाडौं, पोखरा, दिपायल"),
        Triple("second", if (isEn) "2nd Category" else "दोस्रो वर्ग", if (isEn) "Surkhet, Dang" else "सुर्खेत, दाङ"),
        Triple("first", if (isEn) "1st Category" else "पहिलो वर्ग", if (isEn) "Biratnagar, Birgunj, Bhairahawa…" else "विराटनगर, वीरगञ्ज, भैरहवा…")
    )

    var calcFuelIndex by remember { mutableIntStateOf(0) }
    var calcMode by remember { mutableIntStateOf(0) } // 0 = Amount to Liters, 1 = Liters to Amount, 2 = Trip Distance
    var inputAmount by remember { mutableStateOf("1000") }
    var inputLiters by remember { mutableStateOf("5") }
    var inputDistance by remember { mutableStateOf("120") }
    var inputMileage by remember { mutableStateOf("35") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.ChevronLeft, "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    T("fuel_title"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                Modifier
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isEn) "● LIVE" else "● लाइभ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2E7D32)
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                com.neptools.app.ui.components.AnimatedRefreshIconButton(
                    onClick = { refreshRates() },
                    isRefreshing = loading,
                    iconSize = 18.dp,
                    tint = if (loading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Region / Depot Category Selector with GPS Detection
            item {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            T("fuel_region"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (selectedRegion != gpsResolved.categoryKey) {
                            Text(
                                if (isEn) "Auto (GPS)" else "स्वतः",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedRegion = gpsResolved.categoryKey }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val selectedIndex = regions.indexOfFirst { it.first == selectedRegion }.coerceAtLeast(0)
                    BoxWithConstraints(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(4.dp)
                    ) {
                        val pillWidth = (maxWidth - 8.dp) / regions.size
                        val pillOffset by animateDpAsState(
                            targetValue = pillWidth * selectedIndex,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "depot_pill_offset"
                        )

                        // Fluid Sliding Pill Surface
                        Box(
                            Modifier
                                .offset(x = pillOffset)
                                .width(pillWidth)
                                .height(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    RoundedCornerShape(10.dp)
                                )
                        )

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            regions.forEachIndexed { idx, (key, shortTitle, desc) ->
                                val isSelected = selectedRegion == key
                                val textCol by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    animationSpec = tween(200),
                                    label = "tab_text_color"
                                )

                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (selectedRegion != key) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedRegion = key
                                            }
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            shortTitle,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = textCol
                                        )
                                        Text(
                                            desc.take(16) + if (desc.length > 16) "…" else "",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fuel Product Rate Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    fuels.forEachIndexed { index, fuel ->
                        FuelPriceCard(fuel = fuel, isEn = isEn, isUpdating = loading, index = index)
                    }
                }
            }

            // Refresh Rates Button
            item {
                InkButton(
                    text = if (loading) (if (isEn) "Fetching live prices…" else "ताजा दर लोड हुँदैछ…") else T("fuel_refresh"),
                    onClick = { refreshRates() },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Fuel Calculator Section
            item {
                Spacer(Modifier.height(8.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(PIcons.Zap, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            T("fuel_calc_head"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    // Calculator Mode Selector with Sliding Pill
                    BoxWithConstraints(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(3.dp)
                    ) {
                        val tabWidth = (maxWidth - 6.dp) / 3
                        val tabOffset by animateDpAsState(
                            targetValue = tabWidth * calcMode,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "calc_mode_tab_offset"
                        )

                        // Fluid Sliding Tab Surface
                        Box(
                            Modifier
                                .offset(x = tabOffset)
                                .width(tabWidth)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        )

                        Row(Modifier.fillMaxWidth()) {
                            listOf(T("fuel_tab_amt"), T("fuel_tab_lit"), T("fuel_tab_trip")).forEachIndexed { i, title ->
                                val isSel = calcMode == i
                                val col by animateColorAsState(
                                    targetValue = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    animationSpec = tween(200),
                                    label = "calc_tab_text_color"
                                )
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            calcMode = i
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = col
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    // Fuel Type Pill Selection for Calculator with Kinetic Color & Scale Animation
                    val calcFuels = listOf(fuels[0], fuels[1], fuels[2])
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        calcFuels.forEachIndexed { idx, f ->
                            val sel = calcFuelIndex == idx
                            val bgCol by animateColorAsState(
                                targetValue = if (sel) f.primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                animationSpec = tween(200),
                                label = "fuel_chip_bg"
                            )
                            val textCol by animateColorAsState(
                                targetValue = if (sel) Color.White else MaterialTheme.colorScheme.onSurface,
                                animationSpec = tween(200),
                                label = "fuel_chip_text"
                            )
                            val scale by animateFloatAsState(
                                targetValue = if (sel) 1.02f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "fuel_chip_scale"
                            )

                            Box(
                                Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgCol)
                                    .border(
                                        1.dp,
                                        if (sel) f.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        calcFuelIndex = idx
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (isEn) f.nameEn else f.nameNp.substringBefore(" "),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = textCol
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    val selectedCalcFuel = calcFuels[calcFuelIndex]

                    when (calcMode) {
                        0 -> { // Amount -> Liters
                            OutlinedTextField(
                                value = inputAmount,
                                onValueChange = { v -> inputAmount = v.filter { it.isDigit() || it == '.' }.take(7) },
                                label = { Text(T("fuel_amt_hint")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            val amt = inputAmount.toDoubleOrNull() ?: 0.0
                            val liters = if (selectedCalcFuel.price > 0) amt / selectedCalcFuel.price else 0.0

                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(selectedCalcFuel.lightColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(T("fuel_res_lit"), style = MaterialTheme.typography.labelMedium, color = Color(0xFF333333))
                                        Text(if (isEn) "@ Rs. ${"%.2f".format(selectedCalcFuel.price)} / L" else "@ रु ${NepaliNames.toDevanagari("%.2f".format(selectedCalcFuel.price))} / लिटर", style = MaterialTheme.typography.labelSmall, color = Color(0xFF666666))
                                    }
                                    AnimatedContent(
                                        targetState = "%.2f".format(liters),
                                        transitionSpec = {
                                            (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeIn())
                                                .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeOut())
                                        },
                                        label = "calc_liters_result"
                                    ) { litStr ->
                                        Text(
                                            if (isEn) "$litStr L" else "${NepaliNames.toDevanagari(litStr)} लिटर",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = selectedCalcFuel.primaryColor
                                        )
                                    }
                                }
                            }
                        }
                        1 -> { // Liters -> Amount
                            OutlinedTextField(
                                value = inputLiters,
                                onValueChange = { v -> inputLiters = v.filter { it.isDigit() || it == '.' }.take(6) },
                                label = { Text(T("fuel_lit_hint")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            val lts = inputLiters.toDoubleOrNull() ?: 0.0
                            val totalCost = lts * selectedCalcFuel.price

                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(selectedCalcFuel.lightColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(T("fuel_res_amt"), style = MaterialTheme.typography.labelMedium, color = Color(0xFF333333))
                                        Text(if (isEn) "${inputLiters} L × Rs. ${"%.2f".format(selectedCalcFuel.price)}" else "${NepaliNames.toDevanagari(inputLiters)} लिटर × रु ${NepaliNames.toDevanagari("%.2f".format(selectedCalcFuel.price))}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF666666))
                                    }
                                    AnimatedContent(
                                        targetState = "%.2f".format(totalCost),
                                        transitionSpec = {
                                            (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeIn())
                                                .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeOut())
                                        },
                                        label = "calc_cost_result"
                                    ) { costStr ->
                                        Text(
                                            if (isEn) "Rs. $costStr" else "रु ${NepaliNames.toDevanagari(costStr)}",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = selectedCalcFuel.primaryColor
                                        )
                                    }
                                }
                            }
                        }
                        else -> { // Trip Cost
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = inputDistance,
                                    onValueChange = { v -> inputDistance = v.filter { it.isDigit() || it == '.' }.take(6) },
                                    label = { Text(T("fuel_dist_hint")) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = inputMileage,
                                    onValueChange = { v -> inputMileage = v.filter { it.isDigit() || it == '.' }.take(4) },
                                    label = { Text(T("fuel_mil_hint")) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            val dist = inputDistance.toDoubleOrNull() ?: 0.0
                            val mileage = (inputMileage.toDoubleOrNull() ?: 1.0).coerceAtLeast(0.1)
                            val reqLiters = dist / mileage
                            val tripCost = reqLiters * selectedCalcFuel.price

                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(selectedCalcFuel.lightColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(T("fuel_res_req"), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF444444))
                                        Text(if (isEn) "${"%.2f".format(reqLiters)} L" else "${NepaliNames.toDevanagari("%.2f".format(reqLiters))} लिटर", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF222222))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(T("fuel_res_est"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF222222))
                                        AnimatedContent(
                                            targetState = "%.2f".format(tripCost),
                                            transitionSpec = {
                                                (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeIn())
                                                    .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeOut())
                                            },
                                            label = "trip_cost_result"
                                        ) { tripStr ->
                                            Text(
                                                if (isEn) "Rs. $tripStr" else "रु ${NepaliNames.toDevanagari(tripStr)}",
                                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                                color = selectedCalcFuel.primaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Policy & Geographic Pricing Note
            item {
                Text(
                    T("fuel_note"),
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FuelPriceCard(
    fuel: FuelProduct,
    isEn: Boolean,
    isUpdating: Boolean = false,
    index: Int = 0
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fuel_card_press"
    )

    val shimmerTransition = rememberInfiniteTransition(label = "fuel_card_shimmer")
    val shimmerOffset by if (isUpdating) {
        shimmerTransition.animateFloat(
            initialValue = -350f,
            targetValue = 900f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_offset"
        )
    } else {
        remember { mutableFloatStateOf(-350f) }
    }

    val shimmerBrush = if (isUpdating) {
        Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                fuel.primaryColor.copy(alpha = 0.14f),
                Color.Transparent
            ),
            start = Offset(shimmerOffset + (index * 60f), 0f),
            end = Offset(shimmerOffset + 240f + (index * 60f), 240f)
        )
    } else null

    Box(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(if (shimmerBrush != null) Modifier.background(shimmerBrush) else Modifier)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { }
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                Modifier
                    .size(42.dp)
                    .background(fuel.lightColor, CircleShape)
                    .border(1.dp, fuel.primaryColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.Fuel, null, tint = fuel.primaryColor, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            // Product Name (Clean single label)
            Text(
                if (isEn) fuel.nameEn else fuel.nameNp,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(10.dp))

            // Price Block (Clean Right-Aligned Box with Odometer Tumbler)
            Column(horizontalAlignment = Alignment.End) {
                AnimatedContent(
                    targetState = fuel.price,
                    transitionSpec = {
                        if (targetState >= initialState) {
                            (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut())
                        } else {
                            (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeOut())
                        }
                    },
                    label = "fuel_price_odometer"
                ) { targetPrice ->
                    Text(
                        if (isEn) "Rs. ${"%.2f".format(targetPrice)}" else "रु ${NepaliNames.toDevanagari("%.2f".format(targetPrice))}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = fuel.primaryColor,
                        softWrap = false
                    )
                }
                Spacer(Modifier.height(1.dp))
                Text(
                    if (isEn) fuel.unitEn else fuel.unitNp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
