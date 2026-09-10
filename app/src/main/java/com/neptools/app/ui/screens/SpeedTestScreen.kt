package com.neptools.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.network.NetworkAnalyzerEngine
import com.neptools.app.core.network.NetworkDetails
import com.neptools.app.core.network.TestStage
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
fun SpeedTestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"
    val scope = rememberCoroutineScope()

    val testState by NetworkAnalyzerEngine.testState.collectAsState()
    var netDetails by remember { mutableStateOf(NetworkAnalyzerEngine.getNetworkDetails(context)) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        netDetails = NetworkAnalyzerEngine.getNetworkDetails(context)
        val (ip, isp) = NetworkAnalyzerEngine.fetchPublicIp()
        netDetails = netDetails.copy(publicIp = ip, ispName = isp)
    }

    val animatedSpeed by animateFloatAsState(
        targetValue = testState.currentSpeedMbps.toFloat(),
        animationSpec = tween(150),
        label = "speed_needle"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
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
                Icon(PIcons.ChevronLeft, if (isEn) "Back" else "फिर्ता", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "Speed Test & Diagnostics" else "स्पीड टेस्ट तथा नेटवर्क विवरण",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
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
                    onClick = {
                        scope.launch {
                            netDetails = NetworkAnalyzerEngine.getNetworkDetails(context)
                            val (ip, isp) = NetworkAnalyzerEngine.fetchPublicIp()
                            netDetails = netDetails.copy(publicIp = ip, ispName = isp)
                        }
                    },
                    iconSize = 18.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Tabs
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
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
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) "Speed Test" else "गति मापन",
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
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) "Network Analyzer" else "नेटवर्क प्यारामिटर",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (selectedTab == 0) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connection Pill
                item {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(if (netDetails.isConnected) Color(0xFF22C55E) else Color(0xFFEF4444), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${netDetails.connectionType} • ${netDetails.ssid}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Speedometer Gauge
                item {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SpeedometerGauge(speed = animatedSpeed, maxSpeed = 100f)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val df = DecimalFormat("#.0")
                            val displaySpeed = if (testState.stage == TestStage.IDLE) "0.0"
                            else if (testState.stage == TestStage.DONE) df.format(testState.downloadSpeedMbps)
                            else df.format(testState.currentSpeedMbps)

                            Text(
                                displaySpeed,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 42.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Mbps",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                testState.statusMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Metric Cards
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = if (isEn) "PING" else "पिङ",
                            value = "${testState.pingMs} ms",
                            sub = "Jitter: ${testState.jitterMs}ms",
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = if (isEn) "DOWNLOAD" else "डाउनलोड",
                            value = "${DecimalFormat("#.#").format(testState.downloadSpeedMbps)} Mbps",
                            sub = if (testState.stage == TestStage.DOWNLOAD) "Testing..." else "Peak Rate",
                            color = Color(0xFF16A34A),
                            modifier = Modifier.weight(1.2f)
                        )
                        StatCard(
                            label = if (isEn) "UPLOAD" else "अपलोड",
                            value = "${DecimalFormat("#.#").format(testState.uploadSpeedMbps)} Mbps",
                            sub = if (testState.stage == TestStage.UPLOAD) "Testing..." else "Peak Rate",
                            color = Color(0xFF9333EA),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                // Start Test Action
                item {
                    val isTesting = testState.stage == TestStage.PING || testState.stage == TestStage.DOWNLOAD || testState.stage == TestStage.UPLOAD

                    Button(
                        onClick = { scope.launch { NetworkAnalyzerEngine.runSpeedTest() } },
                        enabled = !isTesting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(if (isEn) "Testing Speed..." else "मापन हुँदैछ...", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        } else {
                            Icon(PIcons.Speedometer, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (testState.stage == TestStage.DONE) (if (isEn) "Test Again" else "पुनः मापन गर्नुहोस्")
                                else (if (isEn) "Start Speed Test" else "स्पीड टेस्ट सुरु गर्नुहोस्"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        } else {
            // Diagnostics
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ParamRow(if (isEn) "Connection Type" else "जडान प्रकार", netDetails.connectionType)
                            ParamRow(if (isEn) "Wi-Fi SSID" else "वाईफाई नाम", netDetails.ssid)
                            ParamRow(if (isEn) "Frequency Band" else "फ्रिक्वेन्सी ब्यान्ड", netDetails.frequencyBand)
                            ParamRow(if (isEn) "Signal Strength (RSSI)" else "सिग्नल शक्ति", "${netDetails.rssiDbm} dBm (${netDetails.signalPercent}%)")
                            ParamRow(if (isEn) "Link Speed" else "लिंक गति", "${netDetails.linkSpeedMbps} Mbps")
                            ParamRow(if (isEn) "Local Device IP" else "डिभाइस IP", netDetails.localIp)
                            ParamRow(if (isEn) "Gateway / Router IP" else "राउटर IP", netDetails.gatewayIp)
                            ParamRow(if (isEn) "Subnet Mask" else "सबनेट मास्क", netDetails.subnetMask)
                            ParamRow(if (isEn) "Public IP Address" else "सार्वजनिक IP", netDetails.publicIp)
                            if (netDetails.ispName.isNotEmpty()) {
                                ParamRow(if (isEn) "ISP Provider" else "इन्टरनेट प्रदायक", netDetails.ispName)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedometerGauge(speed: Float, maxSpeed: Float = 100f) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.minDimension / 2f) - 16f

        drawArc(
            color = trackColor,
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 16f, cap = StrokeCap.Round)
        )

        val progressSweep = ((speed / maxSpeed).coerceIn(0f, 1f)) * 240f
        drawArc(
            brush = Brush.sweepGradient(
                listOf(Color(0xFF38BDF8), Color(0xFF22C55E), Color(0xFFE11D48)),
                center = center
            ),
            startAngle = 150f,
            sweepAngle = progressSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 16f, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, sub: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
            Text(sub, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ParamRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurface)
    }
}
