package com.neptools.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.neptools.app.core.util.ReceiptGraphicGenerator
import java.io.File
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.text.NumberFormat
import java.util.Locale

private enum class SplitterMode {
    QUICK_SPLIT, ITEMIZED_SPLIT
}

data class GroupMember(
    val id: String,
    val name: String,
    val color: Color
)

data class DishItem(
    val id: String,
    val name: String,
    val price: Double,
    val assignedMemberIds: List<String> = emptyList()
)

private val MEMBER_COLORS = listOf(
    Color(0xFF2563EB), // Blue
    Color(0xFF059669), // Emerald
    Color(0xFFE11D48), // Rose
    Color(0xFFD97706), // Amber
    Color(0xFF7C3AED), // Violet
    Color(0xFF0891B2), // Cyan
    Color(0xFFEA580C), // Orange
    Color(0xFF16A34A)  // Green
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSplitterScreen(
    onBack: () -> Unit
) {
    val isEn = ThemePrefs.lang.value == "en"
    var currentMode by remember { mutableStateOf(SplitterMode.QUICK_SPLIT) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        ToolTopBar(
            title = if (isEn) "Bill & Tip Splitter" else "बिल तथा टिप बाँडफाँड",
            subtitle = if (isEn) "Split restaurant bills, tip & itemized shares" else "व्यक्तिगत हिस्सा, टिप तथा समूह खर्च हिसाब",
            onBack = onBack
        )
            TabRow(
                selectedTabIndex = currentMode.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentMode.ordinal]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = currentMode == SplitterMode.QUICK_SPLIT,
                    onClick = { currentMode = SplitterMode.QUICK_SPLIT },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(PIcons.Zap, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEn) "Quick Split" else "द्रुत विभाजन", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = currentMode == SplitterMode.ITEMIZED_SPLIT,
                    onClick = { currentMode = SplitterMode.ITEMIZED_SPLIT },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(PIcons.Receipt, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEn) "Itemized Dishes" else "व्यक्तिगत परिकार", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            AnimatedContent(
                targetState = currentMode,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                },
                modifier = Modifier.fillMaxSize(),
                label = "SplitterMode"
            ) { mode ->
                when (mode) {
                    SplitterMode.QUICK_SPLIT -> QuickSplitView(isEn = isEn)
                    SplitterMode.ITEMIZED_SPLIT -> ItemizedSplitView(isEn = isEn)
                }
            }
        }
}

// -------------------------------------------------------------
// 1. QUICK SPLIT VIEW
// -------------------------------------------------------------

@Composable
private fun QuickSplitView(isEn: Boolean) {
    val context = LocalContext.current
    val fmt = remember { NumberFormat.getNumberInstance(Locale.US) }

    var billAmountStr by remember { mutableStateOf("3600") }
    var tipPercent by remember { mutableFloatStateOf(10f) }
    var includeServiceCharge by remember { mutableStateOf(false) } // 10%
    var includeVat by remember { mutableStateOf(false) } // 13%
    var numPeople by remember { mutableIntStateOf(4) }
    var roundUp by remember { mutableStateOf(false) }

    val rawBill = billAmountStr.toDoubleOrNull() ?: 0.0
    val serviceChargeAmount = if (includeServiceCharge) rawBill * 0.10 else 0.0
    val vatAmount = if (includeVat) (rawBill + serviceChargeAmount) * 0.13 else 0.0
    val billWithTax = rawBill + serviceChargeAmount + vatAmount
    val tipAmount = billWithTax * (tipPercent / 100.0)
    val totalBill = billWithTax + tipAmount

    val rawPerPerson = if (numPeople > 0) totalBill / numPeople else 0.0
    val perPersonFinal = if (roundUp) Math.ceil(rawPerPerson / 10.0) * 10.0 else rawPerPerson

    fun shareGraphicReceipt() {
        val file = ReceiptGraphicGenerator.createQuickSplitReceipt(
            context = context,
            rawBill = rawBill,
            tipPercent = tipPercent,
            tipAmount = tipAmount,
            serviceChargeAmount = serviceChargeAmount,
            vatAmount = vatAmount,
            totalBill = totalBill,
            numPeople = numPeople,
            perPersonAmount = perPersonFinal,
            isEn = isEn
        )
        runCatching {
            ReceiptGraphicGenerator.shareReceiptImage(context, file, if (isEn) "Share Receipt Graphic" else "रसिद कार्ड शेयर गर्नुहोस्")
        }.onFailure {
            android.widget.Toast.makeText(
                context,
                if (isEn) "Could not share the receipt image" else "रसिद शेयर गर्न सकिएन",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun shareSummary() {
        val summary = buildString {
            append(if (isEn) "NepTools - Bill Split Summary" else "नेपटूल्स - बिल बाँडफाँड विवरण")
            append("\n━━━━━━━━━━━━━━━━━━━\n")
            append("${if (isEn) "Total Bill:" else "जम्मा बिल:"} ${if (isEn) "Rs. ${fmt.format(totalBill.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(totalBill.toLong()))}"}\n")
            append("${if (isEn) "Number of People:" else "मानिस सङ्ख्या:"} ${if (isEn) "$numPeople" else com.neptools.app.core.calendar.NepaliNames.toDevanagari(numPeople.toString())}\n")
            append("${if (isEn) "Tip (${tipPercent.toInt()}%):" else "टिप (${com.neptools.app.core.calendar.NepaliNames.toDevanagari(tipPercent.toInt().toString())}%):"} ${if (isEn) "Rs. ${fmt.format(tipAmount.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(tipAmount.toLong()))}"}\n")
            if (includeServiceCharge) append("${if (isEn) "Service Charge (10%):" else "सेवा शुल्क (१०%):"} ${if (isEn) "Rs. ${fmt.format(serviceChargeAmount.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(serviceChargeAmount.toLong()))}"}\n")
            if (includeVat) append("${if (isEn) "Govt VAT (13%):" else "सरकारी भ्याट (१३%):"} ${if (isEn) "Rs. ${fmt.format(vatAmount.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(vatAmount.toLong()))}"}\n")
            append("━━━━━━━━━━━━━━━━━━━\n")
            append("${if (isEn) "EACH PERSON PAYS:" else "प्रत्येकले तिर्नुपर्ने रकम:"} ${if (isEn) "Rs. ${fmt.format(perPersonFinal.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(perPersonFinal.toLong()))}"}\n")
            append("━━━━━━━━━━━━━━━━━━━\n")
            append(if (isEn) "Calculated via NepTools" else "नेपटूल्स द्वारा हिसाब गरिएको")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share Bill Split" else "बिल विवरण पठाउनुहोस्"))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Per-Person Card
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isEn) "PER PERSON PAYS" else "प्रत्येकले तिर्नुपर्ने रकम",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color(0xFF94A3B8)
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${if (isEn) "$numPeople People" else "${npNum(numPeople)} जना"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    if (isEn) "Rs. ${fmt.format(perPersonFinal.toLong())}" else "रु ${npNum(perPersonFinal.toLong())}",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 34.sp),
                    color = Color.White
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BreakdownMiniCol(if (isEn) "Base per person" else "मूल हिसाब", if (isEn) "Rs. ${fmt.format((rawBill / numPeople).toLong())}" else "रु ${npNum((rawBill / numPeople).toLong())}")
                    BreakdownMiniCol(if (isEn) "Tip per person" else "टिप हिस्सा", if (isEn) "Rs. ${fmt.format((tipAmount / numPeople).toLong())}" else "रु ${npNum((tipAmount / numPeople).toLong())}")
                    BreakdownMiniCol(if (isEn) "Total Bill" else "कुल बिल", if (isEn) "Rs. ${fmt.format(totalBill.toLong())}" else "रु ${npNum(totalBill.toLong())}")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 1. Bill Input
        OutlinedTextField(
            value = billAmountStr,
            onValueChange = { billAmountStr = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text(if (isEn) "Total Bill Amount (Rs.)" else "कुल बिल रकम (रु.)") },
            leadingIcon = { Text(if (isEn) "Rs." else "रु", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(Modifier.height(18.dp))

        // 2. Number of People Counter
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(if (isEn) "Number of People" else "मानिसहरूको सङ्ख्या", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(if (isEn) "Split bill equally" else "समान रूपमा बाँड्नुहोस्", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (numPeople > 1) numPeople-- },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Text("−", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                    Text(
                        "${if (isEn) numPeople else npNum(numPeople)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    IconButton(
                        onClick = { if (numPeople < 50) numPeople++ },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Text("+", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // 3. Tip Selector
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEn) "Tip Percentage" else "टिप प्रतिशत", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(if (isEn) "${tipPercent.toInt()}% (Rs. ${fmt.format(tipAmount.toLong())})" else "${npNum(tipPercent.toInt())}% (रु ${npNum(tipAmount.toLong())})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0f, 5f, 10f, 15f, 20f).forEach { pct ->
                        val isSelected = tipPercent == pct
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { tipPercent = pct }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${pct.toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Slider(
                    value = tipPercent,
                    onValueChange = { tipPercent = it },
                    valueRange = 0f..30f,
                    steps = 29,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 4. Restaurant Tax & VAT Switches
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEn) "10% Service Charge" else "१०% सेवा शुल्क", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = includeServiceCharge, onCheckedChange = { includeServiceCharge = it })
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEn) "13% Govt VAT" else "१३% सरकारी भ्याट", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = includeVat, onCheckedChange = { includeVat = it })
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEn) "Round up per person" else "रकम राउन्ड अप", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = roundUp, onCheckedChange = { roundUp = it })
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action Hub: Graphic Receipt Share & Copy
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { shareGraphicReceipt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                Icon(PIcons.CameraShare, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEn) "Share Graphic Receipt Card" else "रसिद कार्ड शेयर गर्नुहोस्",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(if (isEn) "Bill Split" else "बिल बाँडफाँड", if (isEn) "Rs. ${fmt.format(perPersonFinal.toLong())} per person ($numPeople people, Total Rs. ${fmt.format(totalBill.toLong())})" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(perPersonFinal.toLong()))} प्रति व्यक्ति (${com.neptools.app.core.calendar.NepaliNames.toDevanagari(numPeople.toString())} जना, जम्मा रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(totalBill.toLong()))})")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, if (isEn) "Copied to clipboard!" else "कपी भयो!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(PIcons.Copy, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isEn) "Copy Text" else "कपी विवरण", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { shareSummary() },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(PIcons.Share, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isEn) "Share Text" else "टेक्स्ट शेयर", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun BreakdownMiniCol(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

// -------------------------------------------------------------
// 2. ITEMIZED DISH SPLIT VIEW
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemizedSplitView(isEn: Boolean) {
    val context = LocalContext.current
    val fmt = remember { NumberFormat.getNumberInstance(Locale.US) }

    val members = remember(isEn) {
        mutableStateListOf(
            GroupMember("m1", if (isEn) "Ram" else "राम", MEMBER_COLORS[0]),
            GroupMember("m2", if (isEn) "Sita" else "सीता", MEMBER_COLORS[1]),
            GroupMember("m3", if (isEn) "Shyam" else "श्याम", MEMBER_COLORS[2])
        )
    }

    val dishes = remember(isEn) {
        mutableStateListOf(
            DishItem("d1", if (isEn) "Buff Momo" else "बफ मोमो", 320.0, listOf("m1", "m2")),
            DishItem("d2", if (isEn) "Chicken Thakali" else "चिकेन थकाली", 480.0, listOf("m1")),
            DishItem("d3", if (isEn) "Mutton Khaja Set" else "मटन खाजा सेट", 550.0, listOf("m3")),
            DishItem("d4", if (isEn) "Cold Drinks" else "कोल्ड ड्रिंक्स", 180.0, listOf("m1", "m2", "m3"))
        )
    }

    var tipPercent by remember { mutableFloatStateOf(10f) }
    var includeVat by remember { mutableStateOf(false) } // 13%
    var includeServiceCharge by remember { mutableStateOf(false) } // 10%

    var showAddMemberSheet by remember { mutableStateOf(false) }
    var showAddDishSheet by remember { mutableStateOf(false) }

    // Calculate individual totals
    val subtotalAll = dishes.sumOf { it.price }
    val taxMultiplier = (if (includeServiceCharge) 0.10 else 0.0) + (if (includeVat) 0.13 else 0.0)
    val tipMultiplier = tipPercent / 100.0
    val totalMultiplier = 1.0 + taxMultiplier + tipMultiplier

    val memberSubtotals = members.associate { member ->
        val memberDishesSubtotal = dishes.filter { it.assignedMemberIds.contains(member.id) }.sumOf { dish ->
            dish.price / dish.assignedMemberIds.size.coerceAtLeast(1)
        }
        member.id to memberDishesSubtotal
    }

    fun shareGraphicItemizedReceipt() {
        val file = ReceiptGraphicGenerator.createItemizedReceipt(
            context = context,
            members = members,
            dishes = dishes,
            memberSubtotals = memberSubtotals,
            totalMultiplier = totalMultiplier,
            subtotalAll = subtotalAll,
            isEn = isEn
        )
        runCatching {
            ReceiptGraphicGenerator.shareReceiptImage(context, file, if (isEn) "Share Group Settlement Graphic" else "ग्रुप हिसाब कार्ड शेयर गर्नुहोस्")
        }.onFailure {
            android.widget.Toast.makeText(
                context,
                if (isEn) "Could not share the settlement card" else "ग्रुप हिसाब कार्ड शेयर गर्न सकिएन",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun shareItemizedSummary() {
        val summary = buildString {
            append(if (isEn) "NepTools - Itemized Group Bill Settlement" else "नेपटूल्स - व्यक्तिगत परिकार बाँडफाँड विवरण")
            append("\n━━━━━━━━━━━━━━━━━━━\n")
            members.forEach { member ->
                val base = memberSubtotals[member.id] ?: 0.0
                val finalTotal = base * totalMultiplier
                append("${member.name}: ${if (isEn) "Rs. ${fmt.format(finalTotal.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(finalTotal.toLong()))}"}\n")
            }
            append("━━━━━━━━━━━━━━━━━━━\n")
            val grandTotal = subtotalAll * totalMultiplier
            append("${if (isEn) "Grand Total:" else "जम्मा कुल बिल:"} ${if (isEn) "Rs. ${fmt.format(grandTotal.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(grandTotal.toLong()))}"}\n")
            append("━━━━━━━━━━━━━━━━━━━\n")
            append(if (isEn) "Calculated via NepTools" else "नेपटूल्स द्वारा हिसाब गरिएको")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share Settlement" else "हिसाब पठाउनुहोस्"))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Group Members Strip
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEn) "Group Members (${members.size})" else "साथीहरू (${members.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            OutlinedButton(
                onClick = { showAddMemberSheet = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(PIcons.Users, null, Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isEn) "+ Add Person" else "+ साथी थप्नुहोस्", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(members) { member ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(member.color.copy(alpha = 0.15f))
                        .border(1.dp, member.color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(member.color, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(member.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Dishes & Assignments Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEn) "Ordered Dishes (${dishes.size})" else "अर्डर गरेका परिकारहरू (${dishes.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Button(
                onClick = { showAddDishSheet = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isEn) "+ Add Dish" else "+ परिकार थप्नुहोस्", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Dishes List
        dishes.forEachIndexed { dishIdx, dish ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(dish.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(if (isEn) "Rs. ${fmt.format(dish.price.toLong())}" else "रु ${npNum(dish.price.toLong())}", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
                        }
                        IconButton(onClick = { dishes.removeAt(dishIdx) }) {
                            Icon(PIcons.Cross, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (isEn) "Tap to assign who ate this dish:" else "यो परिकार कस–कसले खाए? रोज्नुहोस्:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))

                    // Member toggles for this dish
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        members.forEach { member ->
                            val isAssigned = dish.assignedMemberIds.contains(member.id)
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAssigned) member.color else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val current = dish.assignedMemberIds.toMutableList()
                                        if (isAssigned) current.remove(member.id) else current.add(member.id)
                                        dishes[dishIdx] = dish.copy(assignedMemberIds = current)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    member.name.split(" ").firstOrNull() ?: member.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAssigned) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Tax & Tip Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEn) "Tip Percentage: ${tipPercent.toInt()}%" else "टिप प्रतिशत: ${npNum(tipPercent.toInt())}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0f, 5f, 10f, 15f).forEach { pct ->
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (tipPercent == pct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { tipPercent = pct }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("${pct.toInt()}%", style = MaterialTheme.typography.labelSmall.copy(color = if (tipPercent == pct) Color.White else MaterialTheme.colorScheme.onSurface))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEn) "10% Service Charge + 13% VAT" else "१०% सेवा शुल्क + १३% भ्याट", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = includeVat, onCheckedChange = { includeVat = it; includeServiceCharge = it })
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Individual Settlement Cards
        Text(
            if (isEn) "Individual Settlement Breakdown" else "व्यक्तिगत भुक्तानी हिसाब",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(10.dp))

        members.forEach { member ->
            val base = memberSubtotals[member.id] ?: 0.0
            val memberTotal = base * totalMultiplier
            val memberDishes = dishes.filter { it.assignedMemberIds.contains(member.id) }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(member.color.copy(alpha = 0.08f))
                    .border(1.5.dp, member.color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).background(member.color, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(member.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Text(
                            if (isEn) "Rs. ${fmt.format(memberTotal.toLong())}" else "रु ${npNum(memberTotal.toLong())}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = member.color)
                        )
                    }

                    if (memberDishes.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        memberDishes.forEach { dish ->
                            val splitCount = dish.assignedMemberIds.size
                            val splitLabel = if (splitCount > 1) (if (isEn) " (1/$splitCount shared)" else " (${npNum(splitCount)} मा साझा)") else ""
                            Text(
                                "• ${dish.name}: ${if (isEn) "Rs. ${fmt.format((dish.price / splitCount).toLong())}" else "रु ${npNum((dish.price / splitCount).toLong())}"}$splitLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action Hub: Graphic Settlement Card Share & Copy
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { shareGraphicItemizedReceipt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(PIcons.CameraShare, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEn) "Share Graphic Settlement Card" else "ग्रुप हिसाब कार्ड शेयर गर्नुहोस्",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val summary = buildString {
                            append("${if (isEn) "Itemized Group Bill Settlement" else "व्यक्तिगत परिकार बाँडफाँड विवरण"}\n")
                            append("━━━━━━━━━━━━━━━━━━━\n")
                            members.forEach { member ->
                                val base = memberSubtotals[member.id] ?: 0.0
                                val finalTotal = base * totalMultiplier
                                append("${member.name}: ${if (isEn) "Rs. ${fmt.format(finalTotal.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(finalTotal.toLong()))}"}\n")
                            }
                            append("━━━━━━━━━━━━━━━━━━━\n")
                            val grandTotal = subtotalAll * totalMultiplier
                            append("${if (isEn) "Grand Total:" else "जम्मा कुल बिल:"} ${if (isEn) "Rs. ${fmt.format(grandTotal.toLong())}" else "रु ${com.neptools.app.core.calendar.NepaliNames.toDevanagari(fmt.format(grandTotal.toLong()))}"}\n")
                            append("━━━━━━━━━━━━━━━━━━━\n")
                            append(if (isEn) "Calculated via NepTools" else "नेपटूल्स द्वारा हिसाब गरिएको")
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(if (isEn) "Itemized Split" else "व्यक्तिगत बाँडफाँड", summary)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, if (isEn) "Copied to clipboard!" else "कपी भयो!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(PIcons.Copy, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isEn) "Copy Summary" else "कपी हिसाब", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { shareItemizedSummary() },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(PIcons.Share, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isEn) "Share Text" else "टेक्स्ट शेयर", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // Add Member Bottom Sheet
    if (showAddMemberSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddMemberSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            var newName by remember { mutableStateOf("") }
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(if (isEn) "Add Group Member" else "नयाँ साथी थप्नुहोस्", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text(if (isEn) "Name (e.g. Anish)" else "नाम (उदा. अनिश)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val newColor = MEMBER_COLORS[members.size % MEMBER_COLORS.size]
                            members.add(GroupMember("m_${System.currentTimeMillis()}", newName.trim(), newColor))
                            showAddMemberSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = newName.isNotBlank()
                ) {
                    Text(if (isEn) "Add Member" else "थप्नुहोस्")
                }
            }
        }
    }

    // Add Dish Bottom Sheet
    if (showAddDishSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddDishSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            var dishName by remember { mutableStateOf("") }
            var dishPriceStr by remember { mutableStateOf("") }

            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(if (isEn) "Add Dish / Item" else "नयाँ परिकार थप्नुहोस्", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    placeholder = { Text(if (isEn) "Dish Name (e.g. Pizza / Momo)" else "परिकारको नाम (उदा. पिझ्जा / मोमो)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = dishPriceStr,
                    onValueChange = { dishPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = { Text(if (isEn) "Price (e.g. 450)" else "मूल्य (उदा. ४५०)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val p = dishPriceStr.toDoubleOrNull() ?: 0.0
                        if (dishName.isNotBlank() && p > 0) {
                            dishes.add(DishItem("d_${System.currentTimeMillis()}", dishName.trim(), p, members.map { it.id }))
                            showAddDishSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = dishName.isNotBlank() && (dishPriceStr.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(if (isEn) "Add Dish" else "परिकार थप्नुहोस्")
                }
            }
        }
    }
}
