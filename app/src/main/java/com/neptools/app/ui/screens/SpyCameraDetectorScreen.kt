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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.sensor.EmfThreatLevel
import com.neptools.app.core.sensor.SpyCameraDetectorEngine
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpyCameraDetectorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    val engine = remember { SpyCameraDetectorEngine(context) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: EMF, 1: Optical Strobe, 2: IR & Guide

    DisposableEffect(Unit) {
        engine.startEmfScan()
        onDispose {
            engine.release()
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
                    text = if (isEn) "Spy Camera Detector" else "गोप्य क्यामेरा डिटेक्टर",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isEn) "Hotel & Privacy Inspection Suite" else "गोपनीयता तथा सुरक्षा जाँच",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Segmented Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(3.dp)
        ) {
            val tabs = listOf(
                Pair(if (isEn) "EMF" else "चुम्बकीय", 0),
                Pair(if (isEn) "Strobe" else "स्ट्रोब", 1),
                Pair(if (isEn) "IR Night" else "इन्फ्रारेड", 2),
                Pair(if (isEn) "Guide" else "विधि/गाइड", 3)
            )

            tabs.forEach { (title, index) ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> EmfSnifferView(engine = engine, isEn = isEn)
            1 -> OpticalStrobeView(engine = engine, isEn = isEn)
            2 -> InfraredGuideView(isEn = isEn)
            3 -> SpyDetectorMasterGuideView(isEn = isEn)
        }
    }
}

@Composable
private fun EmfSnifferView(
    engine: SpyCameraDetectorEngine,
    isEn: Boolean
) {
    val emfState by engine.emfState.collectAsState()

    val animatedMag by animateFloatAsState(
        targetValue = emfState.magnitudeUt,
        animationSpec = tween(durationMillis = 100),
        label = "animatedMag"
    )

    val (statusTitle, statusColor) = when (emfState.threatLevel) {
        EmfThreatLevel.NORMAL -> Pair(
            if (isEn) "Normal Field (No Spy Cam Detected)" else "सामान्य चुम्बकीय क्षेत्र (सुरक्षित)",
            Color(0xFF16A34A)
        )
        EmfThreatLevel.SUSPICIOUS -> Pair(
            if (isEn) "Suspicious EMF Spike Detected" else "शङ्कास्पद चुम्बकीय तरङ्ग पत्ता लाग्यो",
            Color(0xFFEA580C)
        )
        EmfThreatLevel.HIGH_ALERT -> Pair(
            if (isEn) "High Alert! Active Circuit Nearby" else "उच्च चेतावनी! नजिकै सक्रिय यन्त्र भेटियो",
            Color(0xFFDC2626)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main EMF Gauge
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEn) "ELECTROMAGNETIC FIELD FLUX" else "विद्युत्–चुम्बकीय प्रवाह (EMF)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.size(210.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            // Background arc
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.25f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Active progress (0 to 150 uT)
                            val progress = (animatedMag / 150f).coerceIn(0f, 1f)
                            val activeSweep = progress * 270f

                            if (activeSweep > 0f) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        0.0f to Color(0xFF16A34A),
                                        0.35f to Color(0xFFD97706),
                                        0.65f to Color(0xFFEA580C),
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
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val formatted = String.format(Locale.US, "%.1f", animatedMag)
                            Text(
                                text = if (isEn) formatted else NepaliNames.toDevanagari(formatted),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 44.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "µT (microtesla)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Threat status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }
            }
        }

        // 3-Axis & Peak Card
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
                            text = if (isEn) "Sensor Axes (X, Y, Z)" else "अक्षीय मान (X, Y, Z)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val peakStr = String.format(Locale.US, "%.1f", emfState.peakUt)
                            Text(
                                text = if (isEn) "Peak: $peakStr µT" else "उच्चतम: ${NepaliNames.toDevanagari(peakStr)} µT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { engine.resetEmfPeak() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(PIcons.Refresh, "Reset Peak", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AxisItem("X", emfState.xUt, Modifier.weight(1f), isEn)
                        AxisItem("Y", emfState.yUt, Modifier.weight(1f), isEn)
                        AxisItem("Z", emfState.zUt, Modifier.weight(1f), isEn)
                    }
                }
            }
        }

        // How to use EMF Sniffer
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "How to Inspect with EMF Sniffer" else "चुम्बकीय स्क्यानर कसरी चलाउने?",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isEn)
                            "1. Bring the top edge of your phone within 2 to 5 cm of suspected objects (smoke alarms, power sockets, bedside digital clocks, photo frames).\n" +
                            "2. Natural background field is 30 to 50 µT. A concealed microphone transmitter, hidden lens electronics, or power transformer will cause the reading to spike above 75-100 µT with vibration alerts."
                        else
                            "१. शङ्कास्पद वस्तुहरू (धुवाँ सेन्सर, बिजुलीका सकेट, घडी, फोटो फ्रेम वा टिभी) नजिक फोनको माथिल्लो भाग २ देखि ५ से.मी. नजिक लैजानुहोस्।\n" +
                            "२. सामान्य प्राकृतिक वातावरणमा ३० देखि ५० µT देखाउँछ। गोप्य क्यामेरा वा तार भएको ठाउँमा ७५ देखि १०० µT भन्दा बढी पुग्दा कम्पनसहित चेतावनी दिन्छ।",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AxisItem(label: String, value: Float, modifier: Modifier, isEn: Boolean) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            val formatted = String.format(Locale.US, "%.1f", value)
            Text(
                text = if (isEn) formatted else NepaliNames.toDevanagari(formatted),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun OpticalStrobeView(
    engine: SpyCameraDetectorEngine,
    isEn: Boolean
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var selectedStrobeRate by remember { mutableIntStateOf(0) } // 0: OFF, 1: 3Hz, 2: 6Hz, 3: Steady Torch

    DisposableEffect(Unit) {
        onDispose {
            engine.stopStrobe()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Strobe Control Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "Optical Lens Reflection Strobe" else "अप्टिकल लेन्स रिफ्लेक्सन स्ट्रोब",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isEn)
                            "Flashes the camera LED flashlight. Concave glass optics in hidden pinhole cameras reflect a distinctive pinpoint glint back to your eyes."
                        else
                            "क्यामेराको फ्ल्यासलाइटलाई निश्चित गतिमा चम्काउँछ। गोप्य क्यामेराको सिसाको लेन्सले परावर्तन गर्दा चम्किलो थोप्लो प्रस्ट देखिन्छ।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    if (!hasCameraPermission) {
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isEn) "Grant Camera / Flashlight Permission" else "क्यामेरा तथा फ्ल्यास अनुमति दिनुहोस्")
                        }
                    } else {
                        // Strobe frequency options
                        val options = listOf(
                            Pair(if (isEn) "OFF" else "बन्द", 0),
                            Pair("3 Hz (Slow)", 3),
                            Pair("6 Hz (Fast)", 6),
                            Pair(if (isEn) "Steady Torch" else "निरन्तर बाल्ने", -1)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { (label, rate) ->
                                val isSelected = selectedStrobeRate == rate
                                OutlinedButton(
                                    onClick = {
                                        selectedStrobeRate = rate
                                        when (rate) {
                                            0 -> engine.stopStrobe()
                                            -1 -> engine.setSolidTorch(true)
                                            else -> engine.startStrobe(rate)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) else ButtonDefaults.outlinedButtonColors(),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                             else ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // High-contrast screen filter simulation card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "High-Contrast Red Lens Filter" else "रातो लेन्स फिल्टर दृश्य",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isEn)
                            "Hold the illuminated red preview screen near your eyes in a darkened room. Hidden camera lenses will flash back like tiny red or green stars."
                        else
                            "अँध्यारो कोठामा यो रातो स्क्रिनको छेउबाट हेर्नुहोस्। गोप्य क्यामेराको लेन्समा प्रकाश पर्दा सानो तारा जस्तै चम्कन्छ।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.radialGradient(listOf(Color(0xFFFF0033), Color(0xFF660000))))
                            .border(2.dp, Color(0xFFFF4D4D), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = PIcons.CameraSpy,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isEn) "OPTICAL VIEWING LENS" else "अप्टिकल लेन्स भ्युफाइन्डर",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfraredGuideView(isEn: Boolean) {
    val checklistItems = remember {
        mutableStateMapOf(
            1 to false,
            2 to false,
            3 to false,
            4 to false,
            5 to false,
            6 to false
        )
    }

    val checks = listOf(
        Pair(1, if (isEn) "Smoke detector on ceiling above bed" else "खाटमाथि छतमा भएको स्मोक डिटेक्टर"),
        Pair(2, if (isEn) "Bedside digital alarm clock & USB adapters" else "सिरानी नजिकको डिजिटल घडी तथा चार्जर प्लग"),
        Pair(3, if (isEn) "Bathroom shower head & ventilation fan vents" else "बाथरुमको शावर र भेन्टिलेसन भेन्टहरू"),
        Pair(4, if (isEn) "Television front panel, set-top box & audio bars" else "टिभीको अगाडि, सेटअप बक्स र स्पिकर"),
        Pair(5, if (isEn) "Decorative wall paintings & mirror frames" else "भित्ते फोटो फ्रेम र ऐनाको छेउछाउ"),
        Pair(6, if (isEn) "Fingernail two-way mirror test (check gap between reflection)" else "ऐना परीक्षण (नङ छुवाउँदा प्रतिबिम्बमा खाली ठाउँ छ कि छैन)")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Infrared Guide Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(PIcons.Eye, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (isEn) "Infrared Night-Vision Scan" else "इन्फ्रारेड (IR) नाइट भिजन पहिचान",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isEn)
                            "Night-vision surveillance cameras use 850nm/940nm infrared LEDs that are invisible to the human eye. Most smartphone front cameras do not have an IR filter.\n\n" +
                            "To test: Turn off all room lights so the room is completely dark. Open your front camera and scan around slowly. Night-vision spy cameras will appear on your phone screen as bright glowing purple or white dots."
                        else
                            "नाइट भिजन गोप्य क्यामेराहरूले ८५०/९४० नानोमिटर इन्फ्रारेड बत्ती प्रयोग गर्छन् जुन नाङ्गो आँखाले देखिँदैन। स्मार्टफोनको अगाडिको (सेल्फी) क्यामेरामा इन्फ्रारेड फिल्टर नहुने भएकाले यसलाई सजिलै देख्न सकिन्छ।\n\n" +
                            "जाँच गर्ने तरिका: कोठाको सबै बत्ती बन्द गरेर पूर्ण अँध्यारो बनाउनुहोस्। अगाडिको क्यामेरा खोलेर कोठा वरिपरि घुमाउनुहोस्। नाइट भिजन क्यामेरा स्क्रिनमा चम्किलो बैजनी वा सेतो थोप्लोको रूपमा चम्किनेछ।",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Room Inspection Checklist
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "Hotel Room Safety Checklist" else "होटेल कोठा सुरक्षा चेकलिस्ट",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(12.dp))

                    checks.forEach { (id, desc) ->
                        val checked = checklistItems[id] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checklistItems[id] = !checked }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { checklistItems[id] = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpyDetectorMasterGuideView(isEn: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "Master Privacy & Inspection Guide" else "गोपनीयता तथा क्यामेरा पहिचान विस्तृत गाइड",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isEn)
                            "How to effectively inspect hotel rooms, changing rooms, and rentals to detect hidden cameras, audio bugs, and two-way mirrors."
                        else
                            "होटेल कोठा, चेन्जिङ रुम वा भाडाका कोठामा लुकाइएका गोप्य क्यामेरा, अडियो बग तथा दुईतर्फी ऐना पहिचान गर्ने व्यावहारिक विधि।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 1: The 3 Detection Modes
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isEn) "1. How The 3 Detection Tools Work" else "१. तीनवटा डिटेक्सन प्रविधिको कार्यविधि",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    GuideSubItem(
                        badge = if (isEn) "EMF SNIFFER" else "चुम्बकीय (EMF)",
                        badgeColor = Color(0xFF0284C7),
                        title = if (isEn) "Electromagnetic Radiation Sensor" else "विद्युत्–चुम्बकीय विकिरण सेन्सर",
                        desc = if (isEn)
                            "Every powered electronic circuit, camera microprocessor, and Wi-Fi transmitter leaks an electromagnetic field. Hold your phone within 2-5 cm of an object. If the reading spikes from normal Earth background (30-50 µT) to over 75-100 µT, an active electronic device is concealed inside."
                        else
                            "हरेक विद्युतीय सर्किट र क्यामेराले चुम्बकीय तरङ्ग फाल्छन्। फोनलाई शङ्कास्पद वस्तुको २ देखि ५ से.मी. नजिक लैजानुहोस्। सामान्य ३०-५० µT बाट ७५-१०० µT भन्दा माथि पुगेमा त्यहाँ लुकाइएको विद्युतीय यन्त्र छ भन्ने बुझिन्छ।"
                    )

                    GuideSubItem(
                        badge = if (isEn) "STROBE" else "स्ट्रोब",
                        badgeColor = Color(0xFFEA580C),
                        title = if (isEn) "Optical Lens Reflection" else "अप्टिकल लेन्स परावर्तन",
                        desc = if (isEn)
                            "Pinhole camera lenses are made of curved glass. When illuminated by a blinking flash, the glass reflects a tiny, bright pinpoint glint (retro-reflection) straight back to your eye. The high-contrast red filter enhances this tiny sparkle."
                        else
                            "गोप्य क्यामेराको लेन्स सिसाको हुन्छ। फ्ल्यासलाइट चम्काउँदा उक्त लेन्सले प्रकाशलाई सिधै आँखातर्फ चम्किलो थोप्लो (तारा जस्तै) परावर्तन गर्छ। रातो फिल्टरले यो चमक प्रस्ट देखाउँछ।"
                    )

                    GuideSubItem(
                        badge = if (isEn) "IR NIGHT" else "इन्फ्रारेड",
                        badgeColor = Color(0xFF7C3AED),
                        title = if (isEn) "Infrared Night-Vision Scanner" else "इन्फ्रारेड नाइट भिजन पहिचान",
                        desc = if (isEn)
                            "Night-vision cameras illuminate rooms in pitch darkness using invisible 850nm/940nm LEDs. Smartphone front cameras can see this infrared light. In a pitch dark room, an active night camera will glow like a bright purple or white dot on your phone screen."
                        else
                            "नाइट भिजन क्यामेराले अँध्यारोमा नदेखिने इन्फ्रारेड बत्ती बाल्छन्। सेल्फी क्यामेरामा इन्फ्रारेड फिल्टर नहुने हुनाले पूर्ण अँध्यारो कोठामा क्यामेरा खोलेर हेर्दा नाइट भिजन क्यामेरा स्क्रिनमा चम्किलो बैजनी वा सेतो थोप्लो देखिन्छ।"
                    )
                }
            }
        }

        // Section 2: 4-Step Room Sweep Protocol
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isEn) "2. Standard 4-Step Room Sweep Protocol" else "२. कोठा स्क्यान गर्ने ४-चरणको प्रक्रिया",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    StepRow(
                        step = "1",
                        title = if (isEn) "Physical Eye-Level Inspection" else "भौतिक अवलोकन",
                        desc = if (isEn) "Look for unusual tiny pinholes (1-2mm) in everyday items, odd wires leading into walls, or objects positioned facing directly at the bed or bathroom."
                        else "खाट वा बाथरुम सिधा पर्ने गरी राखिएका वस्तु, १-२ मिलिमिटरका अनौठा प्वाल वा भित्तातिर गएका अनपेक्षित तारहरू हेर्नुहोस्।"
                    )

                    StepRow(
                        step = "2",
                        title = if (isEn) "Close-Range EMF Sweep" else "EMF चुम्बकीय स्क्यान",
                        desc = if (isEn) "Open the EMF Sniffer. Slowly glide the top edge of your phone around smoke alarms, digital alarm clocks, power sockets, and lamp bases."
                        else "EMF Sniffer खोलेर फोनको माथिल्लो भागलाई स्मोक डिटेक्टर, डिजिटल घडी, टिभी बक्स र सकेट वरिपरि २ से.मी. नजिक घुमाउनुहोस्।"
                    )

                    StepRow(
                        step = "3",
                        title = if (isEn) "Pitch Darkness IR Sweep" else "अँध्यारोमा इन्फ्रारेड स्क्यान",
                        desc = if (isEn) "Turn off all lights, draw curtains shut, and open your front camera. Slowly scan 360 degrees for glowing purple or white dots."
                        else "सबै बत्ती र पर्दा बन्द गरी कोठा पूर्ण अँध्यारो बनाउनुहोस्। अगाडिको क्यामेराले वरिपरि हेर्नुहोस् र कुनै बैजनी वा सेतो थोप्लो चम्केको छ कि पत्ता लगाउनुहोस्।"
                    )

                    StepRow(
                        step = "4",
                        title = if (isEn) "Optical Strobe Glint Search" else "स्ट्रोब लेन्स परावर्तन जाँच",
                        desc = if (isEn) "Set the strobe to 3 Hz or 6 Hz in a dim room. Hold phone near eye level and watch for tiny retro-reflective glints."
                        else "स्ट्रोब ३ वा ६ Hz मा अन गर्नुहोस्। फोनलाई आँखाको तहमा राखेर हेर्नुहोस्, लेन्समा प्रकाश पर्दा सानो तारा जस्तै चम्कन्छ।"
                    )
                }
            }
        }

        // Section 3: Two-Way Mirror Fingernail Test
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "3. Two-Way Mirror 'Fingernail Test'" else "३. दुईतर्फी ऐना परीक्षण (नङको विधि)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isEn)
                            "Place your fingernail tip perpendicular against the mirror surface:\n\n" +
                            "• Real Genuine Mirror: There is a CLEAR GAP (1 to 2 mm) between your fingernail and the reflected image because the silver reflection layer is behind the glass.\n\n" +
                            "• Suspicious Two-Way Mirror: There is NO GAP (your nail directly touches the reflection tip-to-tip). Someone could be watching or recording from behind the glass."
                        else
                            "आफ्नो नङको टुप्पो ऐनामा सिधा छुवाउनुहोस्:\n\n" +
                            "• सक्कली साधारण ऐना: तपाईंको नङ र प्रतिबिम्बको बीचमा स्पष्ट खाली ठाउँ (१ देखि २ मि.मी. ग्याप) देखिन्छ, किनकि परावर्तन तह सिसाको पछाडि हुन्छ।\n\n" +
                            "• शङ्कास्पद दुईतर्फी ऐना (Two-Way): नङ र प्रतिबिम्बको बीचमा कुनै ग्याप हुँदैन (नङले सिधै प्रतिबिम्ब छुन्छ)। यस्तो ऐनाको पछाडिबाट मानिसले हेर्न वा क्यामेरा राख्न सक्छ।",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 4: What to do if camera found
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "4. What To Do If You Find A Hidden Camera" else "४. यदि गोप्य क्यामेरा भेटिएमा के गर्ने?",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF991B1B)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isEn)
                            "1. DO NOT touch, disconnect, or smash the device (fingerprints and memory cards are vital forensic evidence).\n" +
                            "2. Cover the lens with a towel, band-aid, or sticker.\n" +
                            "3. Record thorough video and photo evidence of the room and device.\n" +
                            "4. Alert hotel management and report immediately to local police."
                        else
                            "१. यन्त्रलाई नछुनुहोस् वा नफुटाउनुहोस् (औंठाछाप र मेमोरी कार्ड कानुनी प्रमाणका लागि आवश्यक हुन्छन्)।\n" +
                            "२. क्यामेराको लेन्समाथि रुमाल, टेप वा कपडाले छोपिदिनुहोस्।\n" +
                            "३. कोठा र क्यामेराको स्पष्ट भिडियो तथा फोटो प्रमाण खिच्नुहोस्।\n" +
                            "४. तुरुन्त होटेल व्यवस्थापन र नेपाल प्रहरीलाई जानकारी गराउनुहोस्।",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = Color(0xFF7F1D1D)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideSubItem(
    badge: String,
    badgeColor: Color,
    title: String,
    desc: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepRow(
    step: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
