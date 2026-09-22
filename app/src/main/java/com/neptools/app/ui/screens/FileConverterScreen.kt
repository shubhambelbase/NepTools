package com.neptools.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.converter.ConverterPageSize
import com.neptools.app.core.converter.FileConverterMode
import com.neptools.app.core.converter.ImageToPdfEngine
import com.neptools.app.core.converter.PdfImageFormat
import com.neptools.app.core.converter.PdfToImageEngine
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun FileConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val isEn = ThemePrefs.lang.value == "en"
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(FileConverterMode.IMAGE_TO_PDF) }

    // Image to PDF state
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pageSize by remember { mutableStateOf(ConverterPageSize.A4) }
    var imgQuality by remember { mutableFloatStateOf(85f) }

    // PDF to Image state
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfPageCount by remember { mutableIntStateOf(0) }
    var pdfThumbs by remember { mutableStateOf<Map<Int, Bitmap>>(emptyMap()) }
    var selectedPages by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var imageFormat by remember { mutableStateOf(PdfImageFormat.JPG) }
    var dpiIndex by remember { mutableIntStateOf(1) } // 0: 150, 1: 200, 2: 300
    var jpgQuality by remember { mutableFloatStateOf(90f) }

    // Processing & Dialog state
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var progressLabel by remember { mutableStateOf("") }
    var lastPdfFile by remember { mutableStateOf<java.io.File?>(null) }
    var showSavePdfDialog by remember { mutableStateOf(false) }
    var showSaveImageDialog by remember { mutableStateOf(false) }
    var singleRenderedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val dpiValues = intArrayOf(150, 200, 300)

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            imageUris = (imageUris + uris).distinctBy { it.toString() }
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pdfUri = uri
            pdfThumbs = emptyMap()
            selectedPages = emptySet()
            pdfPageCount = 0
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch(Dispatchers.Default) {
                val count = PdfToImageEngine.getPageCount(context, uri)
                withContext(Dispatchers.Main) {
                    pdfPageCount = count
                    selectedPages = (0 until count).toSet()
                }
                val thumbs = mutableMapOf<Int, Bitmap>()
                PdfToImageEngine.renderThumbnails(context, uri, 56) { idx, bmp ->
                    thumbs[idx] = bmp
                }
                withContext(Dispatchers.Main) {
                    pdfThumbs = thumbs.toMap()
                }
            }
        }
    }

    val createPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null && lastPdfFile != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        lastPdfFile!!.inputStream().use { it.copyTo(out) }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, if (isEn) "PDF saved successfully!" else "PDF सुरक्षित भयो!", Toast.LENGTH_SHORT).show()
                    }
                    try {
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }, if (isEn) "Open PDF" else "PDF खोल्नुहोस्"))
                    } catch (_: Throwable) {}
                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, e.localizedMessage ?: if (isEn) "Save failed" else "सेभ असफल भयो", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val createImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/*")) { uri ->
        if (uri != null && singleRenderedBitmap != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        val fmt = if (imageFormat == PdfImageFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                        singleRenderedBitmap!!.compress(fmt, jpgQuality.toInt(), out)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, if (isEn) "Image saved successfully!" else "तस्बिर सुरक्षित भयो!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, e.localizedMessage ?: if (isEn) "Save failed" else "सेभ असफल भयो", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val createZipLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri == null || pdfUri == null || selectedPages.isEmpty()) return@rememberLauncherForActivityResult
        isProcessing = true
        progressLabel = if (isEn) "Preparing ZIP..." else "ZIP बनाउँदै..."
        val pdf = pdfUri!!
        val pages = selectedPages.sorted()
        scope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    ZipOutputStream(out).use { zos ->
                        for ((idx, pageIdx) in pages.withIndex()) {
                            withContext(Dispatchers.Main) {
                                progress = (idx + 1) / pages.size.toFloat()
                                progressLabel = if (isEn) "Page ${pageIdx + 1} of ${pages.size}..." else "पृष्ठ ${pageIdx + 1} / ${pages.size} निकाल्दै..."
                            }
                            val bmp = PdfToImageEngine.renderPage(context, pdf, pageIdx, dpiValues[dpiIndex], imageFormat) ?: continue
                            val entryName = "page_${pageIdx + 1}.${if (imageFormat == PdfImageFormat.PNG) "png" else "jpg"}"
                            zos.putNextEntry(ZipEntry(entryName))
                            val fmt = if (imageFormat == PdfImageFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                            val baos = ByteArrayOutputStream()
                            bmp.compress(fmt, jpgQuality.toInt(), baos)
                            zos.write(baos.toByteArray())
                            zos.closeEntry()
                            bmp.recycle()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, if (isEn) "ZIP saved successfully!" else "ZIP सुरक्षित भयो!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.localizedMessage ?: if (isEn) "ZIP failed" else "ZIP असफल भयो", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    progress = 0f
                }
            }
        }
    }

    fun doImageToPdf() {
        if (imageUris.isEmpty()) {
            Toast.makeText(context, if (isEn) "Select at least one image" else "कम्तीमा एउटा तस्बिर छान्नुहोस्", Toast.LENGTH_SHORT).show()
            return
        }
        isProcessing = true
        progressLabel = if (isEn) "Generating PDF..." else "PDF बनाउँदै..."
        scope.launch(Dispatchers.IO) {
            try {
                val file = ImageToPdfEngine.createPdf(
                    context = context,
                    imageUris = imageUris,
                    pageSize = pageSize,
                    quality = imgQuality.toInt(),
                    onProgress = { cur, total ->
                        scope.launch(Dispatchers.Main) {
                            progress = cur / total.toFloat()
                            progressLabel = if (isEn) "Adding page $cur of $total..." else "पृष्ठ $cur / $total जोड्दै..."
                        }
                    }
                )
                withContext(Dispatchers.Main) {
                    if (file != null) {
                        lastPdfFile = file
                        showSavePdfDialog = true
                    } else {
                        Toast.makeText(context, if (isEn) "Failed to create PDF" else "PDF बनाउन असफल भयो", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.localizedMessage ?: if (isEn) "Error creating PDF" else "PDF बनाउँदा त्रुटि भयो", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    progress = 0f
                }
            }
        }
    }

    fun doPdfToSingleImage() {
        val pdf = pdfUri ?: return
        val pageIdx = selectedPages.firstOrNull() ?: return
        isProcessing = true
        progressLabel = if (isEn) "Rendering page..." else "पृष्ठ रेन्डर गर्दै..."
        scope.launch(Dispatchers.IO) {
            val bmp = PdfToImageEngine.renderPage(context, pdf, pageIdx, dpiValues[dpiIndex], imageFormat)
            withContext(Dispatchers.Main) {
                isProcessing = false
                if (bmp != null) {
                    singleRenderedBitmap = bmp
                    showSaveImageDialog = true
                } else {
                    Toast.makeText(context, if (isEn) "Rendering failed" else "रेन्डर असफल भयो", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Top Bar
        ToolTopBar(
            title = if (isEn) "File Converter" else "फाइल कन्भर्टर",
            subtitle = if (isEn) "PDF & image conversion offline" else "अफलाइन पीडीएफ तथा तस्बिर रूपान्तरण",
            onBack = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onBack()
            },
            actions = {
                // On-Device status pill
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            PIcons.Shield,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isEn) "Offline" else "अफलाइन",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )

        // Mode Switcher Tabs
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isImageToPdf = mode == FileConverterMode.IMAGE_TO_PDF

                // Image -> PDF Tab
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isImageToPdf) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            mode = FileConverterMode.IMAGE_TO_PDF
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            PIcons.ImageCompress,
                            null,
                            tint = if (isImageToPdf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isEn) "Image to PDF" else "तस्बिर → PDF",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isImageToPdf) FontWeight.Bold else FontWeight.Medium,
                                color = if (isImageToPdf) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // PDF -> Image Tab
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isImageToPdf) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            mode = FileConverterMode.PDF_TO_IMAGE
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            PIcons.Pdf,
                            null,
                            tint = if (!isImageToPdf) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isEn) "PDF to Image" else "PDF → तस्बिर",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (!isImageToPdf) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isImageToPdf) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Main Scrollable Content
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedContent(
                targetState = mode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "modeTransition"
            ) { currentMode ->
                if (currentMode == FileConverterMode.IMAGE_TO_PDF) {
                    // ==========================================
                    // IMAGE TO PDF SECTION
                    // ==========================================
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 1. Upload Card (When empty) or Image Carousel (When images selected)
                        if (imageUris.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { imagePicker.launch(arrayOf("image/*")) },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp, horizontal = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            PIcons.ImageCompress,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (isEn) "Select Images from Gallery" else "ग्यालरीबाट तस्बिरहरू छान्नुहोस्",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = if (isEn) "Convert JPG, PNG, or WEBP photos to PDF" else "तस्बिरहरूलाई PDF मा बदल्नुहोस्",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Button(
                                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(PIcons.Plus, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isEn) "Choose Images" else "तस्बिर छान्नुहोस्",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Selected Images Header & Carousel
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "${imageUris.size} Images Selected" else "${imageUris.size} तस्बिर छानियो",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { imagePicker.launch(arrayOf("image/*")) },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(PIcons.Plus, null, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(if (isEn) "Add" else "थप्नुहोस्", style = MaterialTheme.typography.labelSmall)
                                            }

                                            OutlinedButton(
                                                onClick = { imageUris = emptyList() },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(if (isEn) "Clear" else "खाली", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    // Thumbnails Row with Reorder
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        itemsIndexed(imageUris) { idx, uri ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 110.dp, height = 140.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                                ) {
                                                    AsyncImageThumb(uri = uri, modifier = Modifier.fillMaxSize())

                                                    // Page Badge
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(6.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.Black.copy(0.7f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(if (isEn) "#${idx + 1}" else "#${idx + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    // Delete button
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(6.dp)
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Black.copy(0.7f))
                                                            .clickable {
                                                                imageUris = imageUris.filterIndexed { i, _ -> i != idx }
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(PIcons.Cross, null, tint = Color.White, modifier = Modifier.size(11.dp))
                                                    }
                                                }

                                                // Reorder arrows
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    val canLeft = idx > 0
                                                    val canRight = idx < imageUris.size - 1

                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(if (canLeft) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                                            .clickable(enabled = canLeft) {
                                                                val m = imageUris.toMutableList()
                                                                val tmp = m[idx - 1]
                                                                m[idx - 1] = m[idx]
                                                                m[idx] = tmp
                                                                imageUris = m
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(PIcons.ChevronLeft, null, tint = if (canLeft) MaterialTheme.colorScheme.onSurface else Color.Transparent, modifier = Modifier.size(13.dp))
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(if (canRight) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                                            .clickable(enabled = canRight) {
                                                                val m = imageUris.toMutableList()
                                                                val tmp = m[idx + 1]
                                                                m[idx + 1] = m[idx]
                                                                m[idx] = tmp
                                                                imageUris = m
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(PIcons.ChevronRight, null, tint = if (canRight) MaterialTheme.colorScheme.onSurface else Color.Transparent, modifier = Modifier.size(13.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Page Format & Quality Settings Card (Always visible so screen feels complete and useful)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        PIcons.Ruler,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Page Setup" else "पृष्ठ ढाँचा",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Page size options
                                Text(
                                    text = if (isEn) "Page Size" else "पृष्ठको आकार",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ConverterPageSize.entries.forEach { ps ->
                                        val isSelected = pageSize == ps
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                )
                                                .clickable { pageSize = ps }
                                                .padding(horizontal = 14.dp, vertical = 9.dp)
                                        ) {
                                            Text(
                                                text = if (isEn) ps.labelEn else ps.labelNp,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(2.dp))

                                // Image Quality Slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isEn) "Image Quality & Compression" else "तस्बिरको गुणस्तर",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${imgQuality.toInt()}%",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }

                                Slider(
                                    value = imgQuality,
                                    onValueChange = { imgQuality = it },
                                    valueRange = 50f..100f,
                                    steps = 9,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )

                                Text(
                                    text = if (isEn) "Lower quality produces smaller PDF files suitable for sharing on WhatsApp or email."
                                    else "कम गुणस्तरले फाइल सानो बनाउँछ, जसले ह्वाट्सएप वा इमेलमा पठाउन सजिलो हुन्छ।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 3. Action Button
                        if (imageUris.isNotEmpty()) {
                            Button(
                                onClick = { doImageToPdf() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !isProcessing,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(progressLabel, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(PIcons.Doc, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isEn) "Convert ${imageUris.size} Images to PDF" else "${imageUris.size} तस्बिरलाई PDF बनाउनुहोस्",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        // 4. Feature summary card (makes the screen rich and complete)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FeatureRow(
                                    icon = PIcons.Shield,
                                    title = if (isEn) "100% Offline & Private" else "पूर्ण अफलाइन तथा सुरक्षित",
                                    desc = if (isEn) "All PDF generation happens directly on your device." else "तपाईंको डेटा कुनै सर्भरमा जाँदैन, डिभाइसमै रूपान्तरण हुन्छ।"
                                )
                                FeatureRow(
                                    icon = PIcons.Swap,
                                    title = if (isEn) "Custom Page Reordering" else "पृष्ठहरूको क्रम मिलाउनुहोस्",
                                    desc = if (isEn) "Reorder images before generating the final PDF." else "PDF बनाउनुअघि तीर चिन्हले पृष्ठहरूको क्रम सजिलै मिलाउनुहोस्।"
                                )
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // PDF TO IMAGE SECTION
                    // ==========================================
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 1. PDF Upload / Info Box
                        if (pdfUri == null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { pdfPicker.launch(arrayOf("application/pdf")) },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.5.dp,
                                    color = Color(0xFFDC2626).copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp, horizontal = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDC2626).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            PIcons.Pdf,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (isEn) "Select PDF Document" else "PDF फाइल छान्नुहोस्",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = if (isEn) "Extract pages to high-resolution JPG, PNG or ZIP" else "PDF का पृष्ठहरू उच्च गुणस्तरमा तस्बिर वा ZIP मा सुरक्षित गर्नुहोस्",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Button(
                                        onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                    ) {
                                        Icon(PIcons.Doc, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isEn) "Choose PDF File" else "PDF फाइल छान्नुहोस्",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Loaded PDF Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(PIcons.Pdf, null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                    }

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) "$pdfPageCount Pages Document" else "$pdfPageCount पृष्ठको PDF फाइल",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isEn) "${selectedPages.size} pages selected" else "${selectedPages.size} पृष्ठहरू छानिएका छन्",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(if (isEn) "Change" else "फेर्नुहोस्", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            // Page Selection Carousel
                            if (pdfPageCount > 0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = { selectedPages = (0 until pdfPageCount).toSet() },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text(if (isEn) "All" else "सबै", style = MaterialTheme.typography.labelSmall)
                                                }
                                                OutlinedButton(
                                                    onClick = { selectedPages = emptySet() },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text(if (isEn) "None" else "खाली", style = MaterialTheme.typography.labelSmall)
                                                }
                                                OutlinedButton(
                                                    onClick = { selectedPages = (0 until pdfPageCount).filter { it !in selectedPages }.toSet() },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text(if (isEn) "Invert" else "उल्टाउनु", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }

                                            Text(
                                                text = "${selectedPages.size} / $pdfPageCount",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFDC2626)
                                                )
                                            )
                                        }

                                        // Horizontal Thumbnails Row
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(pdfPageCount) { idx ->
                                                val isSelected = idx in selectedPages
                                                val thumb = pdfThumbs[idx]

                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 100.dp, height = 134.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .border(
                                                            width = if (isSelected) 2.5.dp else 1.dp,
                                                            color = if (isSelected) Color(0xFFDC2626) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable {
                                                            selectedPages = if (isSelected) selectedPages - idx else selectedPages + idx
                                                        }
                                                ) {
                                                    if (thumb != null) {
                                                        Image(
                                                            bitmap = thumb.asImageBitmap(),
                                                            contentDescription = null,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                            CircularProgressIndicator(
                                                                strokeWidth = 2.dp,
                                                                modifier = Modifier.size(20.dp),
                                                                color = Color(0xFFDC2626)
                                                            )
                                                        }
                                                    }

                                                    // Checked badge
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(6.dp)
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color(0xFFDC2626) else Color.Black.copy(0.4f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isSelected) {
                                                            Icon(PIcons.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                        }
                                                    }

                                                    // Page number
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(bottom = 6.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.Black.copy(0.75f))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(if (isEn) "Page ${idx + 1}" else "पृष्ठ ${idx + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Export Format & Resolution Settings Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        PIcons.Gear,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Export Options" else "निर्यात विकल्प",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Format
                                Text(
                                    text = if (isEn) "Image Format" else "तस्बिर ढाँचा",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PdfImageFormat.entries.forEach { fmt ->
                                        val isSelected = imageFormat == fmt
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) Color(0xFFDC2626)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                )
                                                .clickable { imageFormat = fmt }
                                                .padding(horizontal = 18.dp, vertical = 9.dp)
                                        ) {
                                            Text(
                                                text = fmt.label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(2.dp))

                                // Resolution
                                Text(
                                    text = if (isEn) "Resolution (DPI)" else "रिजोलुसन",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val dpiLabels = if (isEn) listOf("150 (Web)", "200 (Standard)", "300 (Print)") else listOf("१५० (स्क्रिन)", "२०० (मध्यम)", "३०० (उच्च)")
                                    dpiValues.forEachIndexed { i, v ->
                                        val isSelected = dpiIndex == i
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) Color(0xFFDC2626)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                )
                                                .clickable { dpiIndex = i }
                                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                        ) {
                                            Text(
                                                text = dpiLabels[i],
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }

                                if (imageFormat == PdfImageFormat.JPG) {
                                    Spacer(Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "JPG Quality" else "JPG गुणस्तर",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${jpgQuality.toInt()}%",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFDC2626)
                                            )
                                        )
                                    }

                                    Slider(
                                        value = jpgQuality,
                                        onValueChange = { jpgQuality = it },
                                        valueRange = 60f..100f,
                                        steps = 7,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFFDC2626),
                                            activeTrackColor = Color(0xFFDC2626),
                                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }

                        // 3. Export Action Buttons
                        if (pdfUri != null && selectedPages.isNotEmpty()) {
                            val canConvert = !isProcessing
                            if (selectedPages.size == 1) {
                                Button(
                                    onClick = { doPdfToSingleImage() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    enabled = canConvert,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(Modifier.width(10.dp))
                                        Text(progressLabel, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(PIcons.Download, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isEn) "Export Page ${selectedPages.first() + 1} as Image" else "पृष्ठ ${selectedPages.first() + 1} तस्बिर निकाल्नुहोस्",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        createZipLauncher.launch("NepTools_PDF_Pages_${System.currentTimeMillis()}.zip")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    enabled = canConvert,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(Modifier.width(10.dp))
                                        Text(progressLabel, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(PIcons.Download, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isEn) "Export ${selectedPages.size} Pages as ZIP" else "${selectedPages.size} पृष्ठ ZIP मा सुरक्षित गर्नुहोस्",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Feature info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FeatureRow(
                                    icon = PIcons.Download,
                                    title = if (isEn) "Batch ZIP Export" else "एकैपटक ZIP मा सुरक्षित",
                                    desc = if (isEn) "Export multiple pages directly into an organized ZIP archive." else "धेरै पृष्ठहरूलाई एकैपटक ZIP फाइल बनाएर डाउनलोड गर्नुहोस्।"
                                )
                                FeatureRow(
                                    icon = PIcons.Shield,
                                    title = if (isEn) "Fast Local Rendering" else "छिटो र अफलाइन रेन्डर",
                                    desc = if (isEn) "High-definition image extraction without internet." else "इन्टरनेट बिना नै उच्च रिजोलुसनमा पृष्ठहरू निकालिन्छ।"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Save PDF Dialog
    if (showSavePdfDialog) {
        AlertDialog(
            onDismissRequest = { showSavePdfDialog = false },
            icon = {
                Icon(
                    PIcons.Pdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isEn) "PDF Ready to Save" else "PDF तयार भयो",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) "Your PDF document has been created. Choose where to save it on your device."
                    else "तपाईंको PDF तयार भएको छ। यसलाई आफ्नो डिभाइसमा कहाँ सुरक्षित गर्ने छान्नुहोस्।"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSavePdfDialog = false
                    createPdfLauncher.launch("NepTools_${System.currentTimeMillis()}.pdf")
                }) {
                    Text(
                        if (isEn) "Save PDF" else "सुरक्षित गर्नुहोस्",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePdfDialog = false }) {
                    Text(if (isEn) "Cancel" else "रद्द")
                }
            }
        )
    }

    // Save Image Dialog
    if (showSaveImageDialog) {
        AlertDialog(
            onDismissRequest = { showSaveImageDialog = false },
            icon = {
                Icon(
                    PIcons.ImageCompress,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isEn) "Image Extracted" else "तस्बिर तयार भयो",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) "Rendered page is ready. Choose where to save the image."
                    else "निकालेको तस्बिर तयार भयो। डिभाइसमा कहाँ सुरक्षित गर्ने छान्नुहोस्।"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSaveImageDialog = false
                    val ext = if (imageFormat == PdfImageFormat.PNG) "png" else "jpg"
                    val pageNum = selectedPages.firstOrNull()?.plus(1) ?: 1
                    createImageLauncher.launch("NepTools_page_${pageNum}_${System.currentTimeMillis()}.$ext")
                }) {
                    Text(
                        if (isEn) "Save Image" else "सुरक्षित गर्नुहोस्",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveImageDialog = false }) {
                    Text(if (isEn) "Cancel" else "रद्द")
                }
            }
        )
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AsyncImageThumb(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bmp by remember(uri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            val decoded = ImageToPdfEngine.decodeWithExif(context, uri)?.let { full ->
                val scale = 120f / maxOf(full.width, full.height).toFloat()
                if (scale < 1f) {
                    val w = (full.width * scale).toInt().coerceAtLeast(1)
                    val h = (full.height * scale).toInt().coerceAtLeast(1)
                    val scaled = Bitmap.createScaledBitmap(full, w, h, true)
                    if (scaled !== full) full.recycle()
                    scaled
                } else full
            }
            withContext(Dispatchers.Main) {
                bmp = decoded
            }
        }
    }

    val b = bmp
    if (b != null) {
        Image(
            bitmap = b.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier.background(Color.LightGray.copy(0.2f)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 1.5.dp, modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.primary)
        }
    }
}
