package com.neptools.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.neptools.app.core.audio.DecibelMeterEngine
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SoundMeterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    val engine = remember { DecibelMeterEngine() }
    val state by engine.state.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            engine.start()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            engine.start()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            engine.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PIcons.ChevronLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Sound Level Meter" else "ध्वनि मापक (डेसिबल मिटर)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isEn) "Real-time Acoustic Noise Detector" else "प्रत्यक्ष ध्वनिको तीव्रता मापक",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable { engine.reset() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PIcons.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PIcons.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (isEn) "Microphone Permission Required" else "माइक्रोफोन अनुमति आवश्यक",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isEn)
                                "NepTools requires microphone access to measure real-time ambient noise pressure in decibels (dBA). Audio is analyzed purely on-device and never recorded or transmitted."
                            else
                                "वातावरणीय ध्वनिको तीव्रता (डेसिबल) मापन गर्न माइक्रोफोनको पहुँच आवश्यक छ। ध्वनिको प्रक्रिया पूर्ण रूपमा डिभाइसमै हुन्छ र कुनै अडियो रेकर्ड वा सेभ गरिँदैन।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isEn) "Grant Permission" else "अनुमति दिनुहोस्")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Arc Gauge Card
                item {
                    DecibelGaugeCard(
                        currentDb = state.currentDb,
                        isRunning = state.isRunning,
                        isEn = isEn
                    )
                }

                // Summary Stats Row (Min, Avg, Max)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = if (isEn) "MIN" else "न्यूनतम",
                            value = state.minDb,
                            unit = "dB",
                            color = Color(0xFF16A34A),
                            modifier = Modifier.weight(1f),
                            isEn = isEn
                        )
                        StatCard(
                            label = if (isEn) "AVG" else "औसत",
                            value = state.avgDb,
                            unit = "dB",
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f),
                            isEn = isEn
                        )
                        StatCard(
                            label = if (isEn) "MAX" else "अधिकतम",
                            value = state.maxDb,
                            unit = "dB",
                            color = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f),
                            isEn = isEn
                        )
                    }
                }

                // Real-time Waveform Canvas
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "Live Waveform Oscillograph" else "प्रत्यक्ष ध्वनिको ग्राफ",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.isRunning) (if (isEn) "ACTIVE" else "सक्रिय") else (if (isEn) "PAUSED" else "रोकिएको"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (state.isRunning) Color(0xFF16A34A) else Color(0xFFEA580C)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            SoundWaveformCanvas(
                                history = state.history,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                        }
                    }
                }

                // Noise Threshold Reference Guide
                item {
                    NoiseReferenceCard(currentDb = state.currentDb, isEn = isEn)
                }

                // Controls: Start/Pause & Reset
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (state.isRunning) engine.stop() else engine.start()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isRunning) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (state.isRunning) PIcons.Pause else PIcons.Play,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (state.isRunning) (if (isEn) "Pause" else "रोक्नुहोस्") else (if (isEn) "Start Monitoring" else "मापन सुरु गर्नुहोस्"),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { engine.reset() },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (isEn) "Reset" else "रिसेट")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DecibelGaugeCard(
    currentDb: Float,
    isRunning: Boolean,
    isEn: Boolean
) {
    val animatedDb by animateFloatAsState(
        targetValue = currentDb,
        animationSpec = tween(durationMillis = 80),
        label = "animatedDb"
    )

    val (statusTextNp, statusTextEn, statusColor) = when {
        animatedDb < 35f -> Triple("शान्त वातावरण", "Very Quiet", Color(0xFF16A34A))
        animatedDb < 55f -> Triple("सामान्य कोठा", "Quiet Room", Color(0xFF059669))
        animatedDb < 70f -> Triple("सामान्य कुराकानी", "Normal Conversation", Color(0xFFD97706))
        animatedDb < 85f -> Triple("चर्को ट्राफिक", "Loud Traffic", Color(0xFFEA580C))
        else -> Triple("अत्यधिक चर्को (हानिकारक)", "Hazardous Noise", Color(0xFFDC2626))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background arc (from 135 deg to 405 deg = 270 deg sweep)
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Decibel Arc with gradient
                    val sweepProgress = (animatedDb / 120f).coerceIn(0f, 1f)
                    val activeSweep = sweepProgress * 270f

                    if (activeSweep > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to Color(0xFF16A34A),
                                0.25f to Color(0xFFD97706),
                                0.5f to Color(0xFFEA580C),
                                0.75f to Color(0xFFDC2626),
                                1.0f to Color(0xFFDC2626)
                            ),
                            startAngle = 135f,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Ticks around the arc
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = (size.width - strokeWidth) / 2 - 12.dp.toPx()
                    for (i in 0..12) {
                        val angle = Math.toRadians((135.0 + i * (270.0 / 12.0)))
                        val startTick = Offset(
                            (center.x + (radius - 6.dp.toPx()) * cos(angle)).toFloat(),
                            (center.y + (radius - 6.dp.toPx()) * sin(angle)).toFloat()
                        )
                        val endTick = Offset(
                            (center.x + radius * cos(angle)).toFloat(),
                            (center.y + radius * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.4f),
                            start = startTick,
                            end = endTick,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Numerical readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val formattedNum = String.format(Locale.US, "%.1f", animatedDb)
                    Text(
                        text = if (isEn) formattedNum else NepaliNames.toDevanagari(formattedNum),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 44.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "dBA SPL",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Safety badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.14f))
                    .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isEn) statusTextEn else statusTextNp,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    isEn: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            val formatted = String.format(Locale.US, "%.1f", value)
            Text(
                text = if (isEn) formatted else NepaliNames.toDevanagari(formatted),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SoundWaveformCanvas(
    history: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        if (history.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val step = w / (history.size.coerceAtLeast(2) - 1)

        val path = Path()
        val fillPath = Path()

        history.forEachIndexed { i, db ->
            val norm = (db / 120f).coerceIn(0f, 1f)
            val y = h - (norm * h * 0.9f) - (h * 0.05f)
            val x = i * step

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo((history.size - 1) * step, h)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = 0.25f),
                    Color(0xFF0284C7).copy(alpha = 0.02f)
                )
            )
        )

        drawPath(
            path = path,
            color = Color(0xFF0284C7),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Baseline grid line at 60 dB
        val midY = h * 0.5f
        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(0f, midY),
            end = Offset(w, midY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun NoiseReferenceCard(currentDb: Float, isEn: Boolean) {
    val items = listOf(
        Triple("< 30 dB", if (isEn) "Quiet Whispering / Leaf Rustle" else "सुस्त काउसो / पात हल्लेको", Color(0xFF16A34A)),
        Triple("40-50 dB", if (isEn) "Quiet Living Room / Library" else "शान्त पुस्तकालय / कोठा", Color(0xFF059669)),
        Triple("60-70 dB", if (isEn) "Normal Conversation / Office" else "सामान्य कुराकानी / अफिस", Color(0xFFD97706)),
        Triple("75-85 dB", if (isEn) "Busy City Traffic / Ring Road" else "सडक ट्राफिक / रिङरोड", Color(0xFFEA580C)),
        Triple("> 85 dB", if (isEn) "Heavy Machinery / Dangerous (OSHA)" else "ठूला मेसिन / कानको लागि हानिकारक", Color(0xFFDC2626))
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isEn) "Noise Reference Standards" else "ध्वनि स्तरको मापदण्ड",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            items.forEach { (range, desc, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = range,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
            }
        }
    }
}
