package com.neptools.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.data.ApplicationTemplatesRepo
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.core.util.PdfExporter
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs

// Category color map for visual distinction
private val CATEGORY_COLORS = mapOf(
    "ward" to Color(0xFF6C63FF),
    "legal" to Color(0xFF4CAF50),
    "bank" to Color(0xFF9C27B0),
    "leave" to Color(0xFFFF9800)
)

@Composable
fun ApplicationTemplatesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    var selectedTemplate by remember { mutableStateOf(ApplicationTemplatesRepo.templates.first()) }
    var selectedCategory by remember { mutableStateOf("all") }

    val formValues = remember { mutableStateMapOf<String, String>() }

    val todayBsDate = remember {
        try {
            val t = PatroRepo.d.engine.today()
            "${t.year}/${t.month.toString().padStart(2, '0')}/${t.day.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            "2081/12/10"
        }
    }

    val activeLetter = remember(selectedTemplate, formValues.toMap(), todayBsDate) {
        val m = formValues.toMutableMap()
        if (m["app_date"].isNullOrBlank()) {
            m["app_date"] = todayBsDate
        }
        selectedTemplate.generateLetter(m)
    }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == "all") ApplicationTemplatesRepo.templates
        else ApplicationTemplatesRepo.templates.filter { it.category == selectedCategory }
    }

    val categories = listOf(
        "all" to (if (isEn) "All" else "सबै"),
        "ward" to (if (isEn) "Ward Office" else "वडा कार्यालय"),
        "legal" to (if (isEn) "Legal" else "कानुनी"),
        "bank" to (if (isEn) "Bank" else "बैंक"),
        "leave" to (if (isEn) "Leave" else "बिदा")
    )

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Nepali Application", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            if (isEn) "Application text copied" else "निवेदनको व्यहोरा कपी भयो",
            Toast.LENGTH_SHORT
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact inline header
        ToolTopBar(
            title = if (isEn) "Application Templates" else "सरकारी निवेदन ढाँचा",
            subtitle = if (isEn) "Ward office, legal, banking & leave samples" else "वडा कार्यालय, कानुनी, बैंक तथा बिदाका ढाँचाहरू",
            onBack = onBack
        )

        // Category filter chips (horizontal scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { (catKey, catLabel) ->
                val isSel = selectedCategory == catKey
                val catColor = if (catKey == "all") MaterialTheme.colorScheme.primary
                else CATEGORY_COLORS[catKey] ?: MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSel) catColor
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            1.dp,
                            if (isSel) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            selectedCategory = catKey
                            // If selected template is filtered out, select first visible
                            val newFiltered = if (catKey == "all") ApplicationTemplatesRepo.templates
                            else ApplicationTemplatesRepo.templates.filter { it.category == catKey }
                            if (newFiltered.none { it.id == selectedTemplate.id }) {
                                newFiltered.firstOrNull()?.let {
                                    selectedTemplate = it
                                    formValues.clear()
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = catLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSel) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Inline Template Selector (replaces bottom sheet)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isEn) "Select Template" else "ढाँचा छान्नुहोस्",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))

                        filteredTemplates.forEach { tmpl ->
                            val isSel = tmpl.id == selectedTemplate.id
                            val catColor = CATEGORY_COLORS[tmpl.category]
                                ?: MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) catColor.copy(alpha = 0.10f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        if (isSel) 1.dp else 0.dp,
                                        if (isSel) catColor.copy(alpha = 0.4f) else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedTemplate = tmpl
                                        formValues.clear()
                                    }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Color-coded icon
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(catColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = PIcons.Doc,
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) tmpl.titleEn else tmpl.titleNp,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (isSel) catColor
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isEn) tmpl.descriptionEn else tmpl.descriptionNp,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    if (isSel) {
                                        Icon(
                                            imageVector = PIcons.CheckCircle,
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Form Fields Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isEn) "Fill Details" else "विवरण भर्नुहोस्",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        selectedTemplate.fields.forEach { field ->
                            val curVal = formValues[field.key] ?: if (field.key == "app_date") todayBsDate else ""
                            OutlinedTextField(
                                value = curVal,
                                onValueChange = { formValues[field.key] = it },
                                label = { Text(if (isEn) field.labelEn else field.labelNp) },
                                placeholder = { Text(if (isEn) field.hintEn else field.hintNp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }

            // Letter Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = PIcons.Doc,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Letter Preview" else "निवेदन पूर्वावलोकन",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Official Format" else "प्रमाणित ढाँचा",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(10.dp))

                        // Formatted Nepali Letter
                        Text(
                            text = activeLetter,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 22.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))

                        // Action Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    PdfExporter.printDocument(
                                        context = context,
                                        title = if (isEn) selectedTemplate.titleEn else selectedTemplate.titleNp,
                                        bodyText = activeLetter
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = PIcons.Pdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Print / Save PDF" else "PDF सेभ / प्रिन्ट",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        PdfExporter.generateAndOpenPdf(
                                            context = context,
                                            title = if (isEn) selectedTemplate.titleEn else selectedTemplate.titleNp,
                                            bodyText = activeLetter,
                                            isShare = true
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(PIcons.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        if (isEn) "Share PDF" else "PDF शेयर",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                OutlinedButton(
                                    onClick = { copyToClipboard(activeLetter) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(PIcons.Copy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        if (isEn) "Copy Text" else "कपी गर्नुहोस्",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
