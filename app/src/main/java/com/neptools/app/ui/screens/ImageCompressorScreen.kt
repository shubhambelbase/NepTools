package com.neptools.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.util.CompressionResult
import com.neptools.app.core.util.ImageCompressorEngine
import com.neptools.app.core.util.ImageMeta
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCompressorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalMeta by remember { mutableStateOf<ImageMeta?>(null) }
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressionResult by remember { mutableStateOf<CompressionResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Mode: 0 = Target File Size (KB), 1 = Resize Dimensions (WxH)
    var modeTab by remember { mutableIntStateOf(0) }

    // Target KB Presets
    var targetKbText by remember { mutableStateOf("50") }
    var targetKbPreset by remember { mutableStateOf("< 50 KB (Loksewa)") }

    // Exact Dimension settings
    var customWidthText by remember { mutableStateOf("600") }
    var customHeightText by remember { mutableStateOf("600") }
    var qualitySlider by remember { mutableFloatStateOf(85f) }
    var selectedFormat by remember { mutableStateOf("JPEG") }

    val kbPresets = listOf(
        "< 20 KB (Signatures)" to "20",
        "< 50 KB (Loksewa)" to "50",
        "< 100 KB (Govt Forms)" to "100",
        "< 200 KB (Passport/TU)" to "200",
        "< 240 KB (EDV Visa)" to "240",
        "< 500 KB" to "500",
        "< 1 MB" to "1024"
    )

    val dimensionPresets = listOf(
        "Passport (350×450)" to Pair(350, 450),
        "Square (600×600)" to Pair(600, 600),
        "Document (1200×800)" to Pair(1200, 800)
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            compressionResult = null
            isProcessing = true
            scope.launch(Dispatchers.IO) {
                val meta = ImageCompressorEngine.extractMetadata(context, uri)
                val bmp = ImageCompressorEngine.loadBitmap(context, uri)
                withContext(Dispatchers.Main) {
                    if (bmp == null) {
                        Toast.makeText(context, if (isEn) "Unable to decode image" else "फोटो लोड गर्न सकिएन", Toast.LENGTH_SHORT).show()
                    }
                    originalMeta = meta
                    loadedBitmap = bmp
                    if (meta != null) {
                        customWidthText = meta.originalWidth.toString()
                        customHeightText = meta.originalHeight.toString()
                    }
                    isProcessing = false
                }
            }
        }
    }

    fun executeCompression() {
        val bmp = loadedBitmap ?: return
        isProcessing = true
        scope.launch(Dispatchers.IO) {
            val format = when (selectedFormat) {
                "PNG" -> Bitmap.CompressFormat.PNG
                "WebP" -> if (android.os.Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }

            val result = if (modeTab == 0) {
                val targetKb = targetKbText.toIntOrNull() ?: 50
                ImageCompressorEngine.compressToTargetKb(bmp, targetKb, format)
            } else {
                val w = customWidthText.toIntOrNull() ?: bmp.width
                val h = customHeightText.toIntOrNull() ?: bmp.height
                ImageCompressorEngine.resizeToExactDimensions(bmp, w, h, qualitySlider.toInt(), format)
            }

            withContext(Dispatchers.Main) {
                compressionResult = result
                isProcessing = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
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
                    if (isEn) "Image Resizer & Target KB" else "फोटो साइज तथा KB घटाउने",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pick Card or Loaded Preview
            if (loadedBitmap == null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                PIcons.ImageCompress,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (isEn) "Select Image from Gallery" else "ग्यालरीबाट फोटो छान्नुहोस्",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isEn) "Strict KB limits • 100% Offline & Private" else "१००% सुरक्षित र डिभाइसमै रूपान्तरण",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Image Loaded Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        loadedBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Selected",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                originalMeta?.name ?: "Image",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${originalMeta?.originalWidth ?: 0} × ${originalMeta?.originalHeight ?: 0} px",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                formatSize(originalMeta?.originalSizeBytes ?: 0L, isEn),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            )
                        }
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isEn) "Change" else "फेर्नुहोस्", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Mode Tabs
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (modeTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { modeTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isEn) "Target Size (KB)" else "फाइल साइज",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (modeTab == 0) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (modeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (modeTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { modeTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isEn) "Resize (W×H)" else "लम्बाइ × चौडाइ",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (modeTab == 1) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (modeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mode Settings Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (modeTab == 0) {
                            Text(
                                if (isEn) "Select Maximum File Limit" else "स्वीकृत फाइल साइज सीमा छान्नुहोस्",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Horizontal Pill Selector
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                kbPresets.forEach { (label, kb) ->
                                    val selected = targetKbPreset == label
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            targetKbPreset = label
                                            targetKbText = kb
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = targetKbText,
                                onValueChange = { targetKbText = it.filter { c -> c.isDigit() } },
                                label = { Text(if (isEn) "Custom Max KB Ceiling" else "कस्टम अधिकतम KB") },
                                suffix = { Text("KB") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        } else {
                            Text(
                                if (isEn) "Standard Dimensions Presets" else "प्रचलित फोटो साइज प्रिसेट",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dimensionPresets.forEach { (label, dim) ->
                                    OutlinedButton(
                                        onClick = {
                                            customWidthText = dim.first.toString()
                                            customHeightText = dim.second.toString()
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = customWidthText,
                                    onValueChange = { customWidthText = it.filter { c -> c.isDigit() } },
                                    label = { Text(if (isEn) "Width (px)" else "चौडाइ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customHeightText,
                                    onValueChange = { customHeightText = it.filter { c -> c.isDigit() } },
                                    label = { Text(if (isEn) "Height (px)" else "उचाइ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Compress Action Button
                Button(
                    onClick = { executeCompression() },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEn) "Compressing..." else "साइज घटाउँदै...", style = MaterialTheme.typography.titleSmall)
                    } else {
                        Icon(PIcons.ImageCompress, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEn) "Compress to Target Limit" else "अहिले फोटो घटाउनुहोस्",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Result Card
                AnimatedVisibility(
                    visible = compressionResult != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val result = compressionResult ?: return@AnimatedVisibility
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (isEn) "Ready for Upload" else "स्वीकृत साइज तयार भयो",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF15803D)
                                )
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF22C55E))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${formatSize(result.sizeBytes, isEn)} (${result.reductionPercent}% saved)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            // Preview Image
                            Image(
                                bitmap = result.bitmap.asImageBitmap(),
                                contentDescription = "Compressed Result",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                            )

                            // Action Buttons
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val ext = if (result.format == Bitmap.CompressFormat.PNG) "png" else if (result.format == Bitmap.CompressFormat.WEBP) "webp" else "jpg"
                                        val uri = ImageCompressorEngine.saveToGallery(context, result.byteArray, ext)
                                        if (uri != null) {
                                            Toast.makeText(context, if (isEn) "Saved to Pictures/NepTools!" else "फोटो ग्यालरीमा सेभ भयो!", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                ) {
                                    Icon(PIcons.Download, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isEn) "Save to Gallery" else "ग्यालरीमा सेभ", style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val ext = if (result.format == Bitmap.CompressFormat.PNG) "png" else if (result.format == Bitmap.CompressFormat.WEBP) "webp" else "jpg"
                                        val uri = ImageCompressorEngine.saveToGallery(context, result.byteArray, ext)
                                        if (uri != null) {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = if (ext == "png") "image/png" else "image/jpeg"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(PIcons.Share, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isEn) "Share" else "सेयर गर्नुहोस्", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long, isEn: Boolean): String {
    val df = DecimalFormat("#.##")
    return when {
        bytes >= 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
        bytes >= 1024 -> "${df.format(bytes / 1024.0)} KB"
        else -> "$bytes B"
    }
}
