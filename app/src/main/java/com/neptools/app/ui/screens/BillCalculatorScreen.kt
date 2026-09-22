package com.neptools.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun BillCalculatorScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    var mode by remember { mutableIntStateOf(0) } // 0 = NEA Electricity, 1 = Water (KUKL)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        ToolTopBar(
            title = T("bill_title"),
            subtitle = if (isEn) "NEA electricity & KUKL water tariff rates" else "विद्युत (NEA) तथा खानेपानी (KUKL) महसुल हिसाब",
            onBack = onBack
        )

        // Mode Switcher Tabs
        Box(Modifier.padding(horizontal = 16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (mode == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { mode = 0 }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            PIcons.Zap, null,
                            modifier = Modifier.size(18.dp),
                            tint = if (mode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            T("bill_elec"),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (mode == 0) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (mode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (mode == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { mode = 1 }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            PIcons.Droplet, null,
                            modifier = Modifier.size(18.dp),
                            tint = if (mode == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            T("bill_water"),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (mode == 1) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (mode == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (mode == 0) {
                    ElectricityCalculator()
                } else {
                    WaterCalculator()
                }
            }
        }
    }
}

@Composable
private fun ElectricityCalculator() {
    val isEn = ThemePrefs.lang.value == "en"
    var unitsText by remember { mutableStateOf("85") }
    var ampere by remember { mutableIntStateOf(5) } // 5, 15, 30, 60

    val units = unitsText.toIntOrNull() ?: 0
    val bill = calculateNeaBill(units, ampere)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Units Input Card
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(T("bill_units"), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = unitsText,
                onValueChange = { v -> unitsText = v.filter { it.isDigit() }.take(5) },
                label = { Text(T("bill_units_hint")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(20, 50, 100, 200).forEach { u ->
                    val isSel = unitsText == u.toString()
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { unitsText = u.toString() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "$u units",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(T("bill_meter_cap"), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5 to "5A", 15 to "15A", 30 to "30A", 60 to "60A").forEach { (amp, label) ->
                    val selected = ampere == amp
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { ampere = amp }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Summary Breakdown Card
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(T("bill_nea_sum"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(T("bill_min_chg"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (isEn) "Rs. ${bill.minCharge}" else "रु ${npNum(bill.minCharge)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(T("bill_eng_chg"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (isEn) "Rs. ${bill.energyCharge}" else "रु ${npNum(bill.energyCharge)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(T("bill_total"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(if (isEn) "Rs. ${bill.total}" else "रु ${npNum(bill.total)}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(T("bill_disc"), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color(0xFF2E7D32))
                    Text(if (isEn) "Rs. ${bill.discountTotal}" else "रु ${npNum(bill.discountTotal)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(T("bill_fine"), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color(0xFFE65100))
                    Text(if (isEn) "Rs. ${bill.fineTotal}" else "रु ${npNum(bill.fineTotal)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE65100))
                }
            }
        }
    }
}

private data class NeaBillResult(
    val minCharge: Int,
    val energyCharge: Int,
    val total: Int,
    val discountTotal: Int,
    val fineTotal: Int
)

private fun calculateNeaBill(units: Int, ampere: Int): NeaBillResult {
    if (units <= 0) return NeaBillResult(30, 0, 30, 30, 30)

    var minCharge = 30
    var energyCharge = 0.0

    when (ampere) {
        5 -> {
            when {
                units <= 20 -> { minCharge = 30; energyCharge = units * 3.0 }
                units <= 30 -> { minCharge = 50; energyCharge = (20 * 3.0) + (units - 20) * 6.50 }
                units <= 50 -> { minCharge = 50; energyCharge = (20 * 3.0) + (10 * 6.50) + (units - 30) * 8.00 }
                units <= 100 -> { minCharge = 75; energyCharge = (20 * 3.0) + (10 * 6.50) + (20 * 8.00) + (units - 50) * 9.50 }
                units <= 250 -> { minCharge = 100; energyCharge = (20 * 3.0) + (10 * 6.50) + (20 * 8.00) + (50 * 9.50) + (units - 100) * 10.00 }
                else -> { minCharge = 125; energyCharge = (20 * 3.0) + (10 * 6.50) + (20 * 8.00) + (50 * 9.50) + (150 * 10.00) + (units - 250) * 11.00 }
            }
        }
        15 -> {
            minCharge = if (units <= 20) 50 else if (units <= 30) 75 else if (units <= 50) 100 else if (units <= 100) 125 else 150
            energyCharge = units * 9.50
        }
        30 -> {
            minCharge = if (units <= 20) 100 else 200
            energyCharge = units * 10.50
        }
        else -> {
            minCharge = 250
            energyCharge = units * 11.50
        }
    }

    val total = (minCharge + energyCharge).toInt()
    val discount = (total * 0.98).toInt()
    val fine = (total * 1.05).toInt()
    return NeaBillResult(minCharge, energyCharge.toInt(), total, discount, fine)
}

@Composable
private fun WaterCalculator() {
    val isEn = ThemePrefs.lang.value == "en"
    var volumeText by remember { mutableStateOf("15000") }
    var pipeSize by remember { mutableStateOf("1/2\"") }

    val volume = volumeText.toIntOrNull() ?: 0
    val bill = calculateWaterBill(volume, pipeSize)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(T("water_vol"), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = volumeText,
                onValueChange = { v -> volumeText = v.filter { it.isDigit() }.take(7) },
                label = { Text(T("water_vol_hint")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10000 to "10k L", 15000 to "15k L", 25000 to "25k L", 50000 to "50k L").forEach { (liters, label) ->
                    val isSel = volumeText == liters.toString()
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { volumeText = liters.toString() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(T("water_pipe"), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1/2\"" to "1/2 inch", "3/4\"" to "3/4 inch", "1\"" to "1 inch").forEach { (p, label) ->
                    val selected = pipeSize == p
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { pipeSize = p }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Water Summary Card
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(T("water_kukl_sum"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(T("water_min"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (isEn) "Rs. ${bill.minCharge}" else "रु ${npNum(bill.minCharge)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(T("water_extra"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (isEn) "Rs. ${bill.extraCharge}" else "रु ${npNum(bill.extraCharge)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(T("water_sew"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (isEn) "Rs. ${bill.sewageCharge}" else "रु ${npNum(bill.sewageCharge)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(T("bill_total"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(if (isEn) "Rs. ${bill.total}" else "रु ${npNum(bill.total)}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

private data class WaterBillResult(
    val minCharge: Int,
    val extraCharge: Int,
    val sewageCharge: Int,
    val total: Int
)

private fun calculateWaterBill(liters: Int, pipeSize: String): WaterBillResult {
    val minCharge = if (pipeSize == "1/2\"") 100 else if (pipeSize == "3/4\"") 1110 else 3080
    val baseQuota = 10000
    val extraLiters = (liters - baseQuota).coerceAtLeast(0)
    val ratePerThousand = if (pipeSize == "1/2\"") 32 else 56
    val extraCharge = (extraLiters / 1000) * ratePerThousand
    val sewageCharge = ((minCharge + extraCharge) * 0.50).toInt()
    val total = minCharge + extraCharge + sewageCharge
    return WaterBillResult(minCharge, extraCharge, sewageCharge, total)
}
