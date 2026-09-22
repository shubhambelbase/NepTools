package com.neptools.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.LoanEmiRepo
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanEmiScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Loan EMI, 1: Fixed Deposit

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        ToolTopBar(
            title = if (isEn) "Loan EMI & FD Calculator" else "बैंक ऋण ईएमआई तथा मुद्दती",
            subtitle = if (isEn) "Monthly installment, interest & fixed deposit" else "मासिक किस्ता, ब्याज तथा मुद्दती हिसाब",
            onBack = onBack
        )

        // Modern Segmented Pill Selector
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) "Loan EMI" else "कर्जा",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) "Fixed Deposit" else "मुद्दती निक्षेप",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (selectedTab == 0) {
            LoanEmiCalculatorView(isEn = isEn)
        } else {
            FixedDepositCalculatorView(isEn = isEn)
        }
    }
}

@Composable
private fun LoanEmiCalculatorView(isEn: Boolean) {
    var amountStr by remember { mutableStateOf("2500000") } // Rs 25 Lakhs
    var rateStr by remember { mutableStateOf("11.5") } // 11.5%
    var yearsStr by remember { mutableStateOf("15") } // 15 Years

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val years = yearsStr.toIntOrNull() ?: 1
    val tenureMonths = (years * 12).coerceAtLeast(1)

    val emiResult = remember(amount, rate, tenureMonths) {
        LoanEmiRepo.calculateEmi(principal = amount, annualRate = rate, tenureMonths = tenureMonths)
    }

    val fmt = NumberFormat.getNumberInstance(Locale.US)
    val context = LocalContext.current

    fun shareLoanCalculation() {
        val summary = buildString {
            append(if (isEn) "NepTools - Loan EMI Calculation Summary" else "नेपटूल्स - ऋण किस्ता (EMI) विवरण")
            append("\n----------------------------------------\n")
            append("${if (isEn) "Loan Amount:" else "ऋण रकम:"} ${if (isEn) "Rs. ${fmt.format(amount.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(amount.toLong()))}"}\n")
            append("${if (isEn) "Interest Rate:" else "वार्षिक ब्याजदर:"} ${if (isEn) "$rate% p.a." else "${NepaliNames.toDevanagari(rate.toString())}% वार्षिक"}\n")
            append("${if (isEn) "Tenure:" else "ऋण अवधि:"} ${if (isEn) "$years Years ($tenureMonths months)" else "${NepaliNames.toDevanagari(years)} वर्ष (${NepaliNames.toDevanagari(tenureMonths)} महिना)"}\n")
            append("${if (isEn) "Monthly EMI:" else "मासिक किस्ता:"} ${if (isEn) "Rs. ${fmt.format(emiResult.monthlyEmi.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(emiResult.monthlyEmi.toLong()))}"}\n")
            append("${if (isEn) "Total Interest:" else "जम्मा ब्याज:"} ${if (isEn) "Rs. ${fmt.format(emiResult.totalInterest.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(emiResult.totalInterest.toLong()))}"}\n")
            append("${if (isEn) "Total Payable:" else "जम्मा भुक्तानी:"} ${if (isEn) "Rs. ${fmt.format(emiResult.totalPayment.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(emiResult.totalPayment.toLong()))}"}\n")
            append("----------------------------------------\n")
            append(if (isEn) "Calculated via NepTools" else "नेपटूल्स द्वारा हिसाब गरिएको")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share Loan EMI Calculation" else "ऋण हिसाब विवरण पठाउनुहोस्"))
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Result Card
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))))
                    .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isEn) "MONTHLY EMI" else "मासिक किस्ता",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1B5E20)
                        )
                        Box(
                            Modifier
                                .background(Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${if (isEn) "$years" else npNum(years)} ${if (isEn) "Years" else "वर्ष"} (${if (isEn) "$tenureMonths" else npNum(tenureMonths)} ${if (isEn) "mo" else "महिना"})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        if (isEn) "Rs. ${fmt.format(emiResult.monthlyEmi.toLong())}" else "रु ${npNum(emiResult.monthlyEmi.toLong())}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
                        color = Color(0xFF1B5E20)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Ratio Bar
                    Text(
                        "${if (isEn) "Principal:" else "साँवा:"} ${String.format(Locale.US, "%.1f", emiResult.principalPercent)}% · ${if (isEn) "Interest:" else "ब्याज:"} ${String.format(Locale.US, "%.1f", emiResult.interestPercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFEF5350))
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(emiResult.principalPercent / 100f)
                                .background(Color(0xFF2E7D32))
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFA5D6A7))
                    Spacer(Modifier.height(10.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isEn) "Total Interest" else "जम्मा ब्याज", style = MaterialTheme.typography.labelSmall, color = Color(0xFF37474F))
                            Text(if (isEn) "Rs. ${fmt.format(emiResult.totalInterest.toLong())}" else "रु ${npNum(emiResult.totalInterest.toLong())}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC62828))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isEn) "Total Payment" else "जम्मा भुक्तानी", style = MaterialTheme.typography.labelSmall, color = Color(0xFF37474F))
                            Text(if (isEn) "Rs. ${fmt.format(emiResult.totalPayment.toLong())}" else "रु ${npNum(emiResult.totalPayment.toLong())}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1B5E20))
                        }
                    }
                }
            }
        }

        // 1-Tap Share Button
        item {
            Button(
                onClick = { shareLoanCalculation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(PIcons.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEn) "Share Loan Calculation" else "ऋण हिसाब शेयर गर्नुहोस्",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }

        // Inputs Card
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (isEn) "Loan Details" else "ऋणको विवरण",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text(if (isEn) "Loan Amount (Rs.)" else "ऋण रकम (रु)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text(if (isEn) "Interest Rate (% p.a.)" else "वार्षिक ब्याजदर (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = yearsStr,
                        onValueChange = { yearsStr = it },
                        label = { Text(if (isEn) "Tenure in Years" else "ऋण अवधि (वर्ष)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun FixedDepositCalculatorView(isEn: Boolean) {
    var principalStr by remember { mutableStateOf("500000") } // Rs 5 Lakhs
    var annualRateStr by remember { mutableStateOf("8.5") } // 8.5%
    var monthsStr by remember { mutableStateOf("12") } // 12 months

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val annualRate = annualRateStr.toDoubleOrNull() ?: 0.0
    val months = monthsStr.toIntOrNull() ?: 12

    val fdResult = remember(principal, annualRate, months) {
        LoanEmiRepo.calculateFd(principal = principal, annualRate = annualRate, tenureMonths = months)
    }

    val fmt = NumberFormat.getNumberInstance(Locale.US)
    val context = LocalContext.current

    fun shareFdCalculation() {
        val summary = buildString {
            append(if (isEn) "NepTools - Fixed Deposit Calculation Summary" else "नेपटूल्स - मुद्दती निक्षेप विवरण")
            append("\n----------------------------------------\n")
            append("${if (isEn) "Deposit Amount:" else "जम्मा रकम:"} ${if (isEn) "Rs. ${fmt.format(principal.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(principal.toLong()))}"}\n")
            append("${if (isEn) "Interest Rate:" else "वार्षिक ब्याजदर:"} ${if (isEn) "$annualRate% p.a." else "${NepaliNames.toDevanagari(annualRate.toString())}% वार्षिक"}\n")
            append("${if (isEn) "Tenure:" else "अवधि:"} ${if (isEn) "$months Months" else "${NepaliNames.toDevanagari(months)} महिना"}\n")
            append("${if (isEn) "Gross Interest:" else "कुल आर्जित ब्याज:"} ${if (isEn) "Rs. ${fmt.format(fdResult.grossInterest.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(fdResult.grossInterest.toLong()))}"}\n")
            append("${if (isEn) "6% TDS Tax:" else "६% सरकारी कर:"} ${if (isEn) "Rs. ${fmt.format(fdResult.taxDeduction.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(fdResult.taxDeduction.toLong()))}"}\n")
            append("${if (isEn) "Net In-Hand Interest:" else "खुद प्राप्त हुने ब्याज:"} ${if (isEn) "Rs. ${fmt.format(fdResult.netInterest.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(fdResult.netInterest.toLong()))}"}\n")
            append("${if (isEn) "Maturity Amount:" else "परिपक्वता रकम:"} ${if (isEn) "Rs. ${fmt.format(fdResult.maturityAmount.toLong())}" else "रु ${NepaliNames.toDevanagari(fmt.format(fdResult.maturityAmount.toLong()))}"}\n")
            append("----------------------------------------\n")
            append(if (isEn) "Calculated via NepTools" else "नेपटूल्स द्वारा हिसाब गरिएको")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share Fixed Deposit Calculation" else "मुद्दती निक्षेप विवरण पठाउनुहोस्"))
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Result Card
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))
                    .border(2.dp, Color(0xFF64B5F6), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isEn) "MATURITY AMOUNT" else "परिपक्वता रकम",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0D47A1)
                        )
                        Box(
                            Modifier
                                .background(Color(0xFF1565C0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${if (isEn) "$months" else npNum(months)} ${if (isEn) "Months" else "महिना"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        if (isEn) "Rs. ${fmt.format(fdResult.maturityAmount.toLong())}" else "रु ${npNum(fdResult.maturityAmount.toLong())}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
                        color = Color(0xFF0D47A1)
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF90CAF9))
                    Spacer(Modifier.height(10.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isEn) "Gross Interest" else "कुल आर्जित ब्याज", style = MaterialTheme.typography.labelSmall, color = Color(0xFF37474F))
                            Text(if (isEn) "Rs. ${fmt.format(fdResult.grossInterest.toLong())}" else "रु ${npNum(fdResult.grossInterest.toLong())}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1565C0))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isEn) "6% TDS Tax" else "६% सरकारी कर", style = MaterialTheme.typography.labelSmall, color = Color(0xFF37474F))
                            Text(if (isEn) "- Rs. ${fmt.format(fdResult.taxDeduction.toLong())}" else "- रु ${npNum(fdResult.taxDeduction.toLong())}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC62828))
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isEn) "Net In-Hand Interest" else "खुद प्राप्त हुने ब्याज", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1B5E20))
                        Text(if (isEn) "Rs. ${fmt.format(fdResult.netInterest.toLong())}" else "रु ${npNum(fdResult.netInterest.toLong())}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1B5E20))
                    }
                }
            }
        }

        // 1-Tap Share Button
        item {
            Button(
                onClick = { shareFdCalculation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Icon(PIcons.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEn) "Share FD Calculation" else "मुद्दती हिसाब शेयर गर्नुहोस्",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }

        // Inputs Card
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (isEn) "Deposit Details" else "मुद्दती निक्षेप विवरण",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it },
                        label = { Text(if (isEn) "Deposit Amount (Rs.)" else "जम्मा रकम (रु)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = annualRateStr,
                        onValueChange = { annualRateStr = it },
                        label = { Text(if (isEn) "Interest Rate (% p.a.)" else "वार्षिक ब्याजदर (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = monthsStr,
                        onValueChange = { monthsStr = it },
                        label = { Text(if (isEn) "Duration in Months" else "अवधि (महिना)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }
    }
}
