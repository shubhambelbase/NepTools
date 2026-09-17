package com.neptools.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.compass.CompassEngine
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs

private val DIR_EN = arrayOf("North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest")
private val DIR_SHORT = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
private val DIR_NP = arrayOf("उत्तर", "उत्तर-पूर्व", "पूर्व", "दक्षिण-पूर्व", "दक्षिण", "दक्षिण-पश्चिम", "पश्चिम", "उत्तर-पश्चिम")

private fun directionIndex(heading: Float): Int {
    val idx = ((heading + 22.5f) / 45f).toInt() % 8
    return if (idx < 0) idx + 8 else idx
}

@Composable
fun CompassScreen(
    onBack: () -> Unit,
    onOpenVastu: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    var hasSensor by remember { mutableStateOf(true) }
    var magneticHeading by remember { mutableFloatStateOf(0f) }
    var tilt by remember { mutableFloatStateOf(0f) }
    var unreliable by remember { mutableStateOf(false) }
    var trueNorth by remember { mutableStateOf(false) }
    var declination by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val eng = CompassEngine(context.applicationContext, object : CompassEngine.Listener {
            override fun onHeading(headingDeg: Float, tiltDeg: Float) {
                magneticHeading = headingDeg
                tilt = tiltDeg
            }

            override fun onUnreliable(unreliableFlag: Boolean) {
                unreliable = unreliableFlag
            }
        })
        hasSensor = eng.hasSensor
        if (eng.hasSensor) eng.start()
        onDispose { eng.stop() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                Icon(
                    PIcons.ChevronLeft,
                    contentDescription = if (isEn) "Back" else "फिर्ता",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = if (isEn) "Compass" else "कम्पास",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (onOpenVastu != null) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable(onClick = onOpenVastu)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isEn) "Vastu Mode" else "वास्तु मोड",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (!hasSensor) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = PIcons.Compass,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = if (isEn) "Compass sensor not available" else "कम्पास सेन्सर उपलब्ध छैन",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isEn)
                        "This device has no magnetometer, which is required to detect direction."
                    else "यो डिभाइसमा दिशा पत्ता लगाउन आवश्यक म्याग्नेटोमिटर सेन्सर छैन।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        val displayHeading = (((magneticHeading + if (trueNorth) declination else 0f) % 360f) + 360f) % 360f
        val dirIdx = directionIndex(displayHeading)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 4.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            CompassDial(
                headingDeg = displayHeading,
                colors = rememberDefaultDialColors(
                    face = MaterialTheme.colorScheme.surface,
                    label = MaterialTheme.colorScheme.onSurface,
                    cardinal = MaterialTheme.colorScheme.onSurface,
                    accent = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxSize()
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatDegrees(displayHeading, ThemePrefs.nepaliDigits.value),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEn) DIR_EN[dirIdx] else DIR_NP[dirIdx],
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (trueNorth) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                    .clickable {
                        if (!trueNorth) {
                            val coords = CompassEngine.lastKnownCoords(context)
                            if (coords == null) {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Location unavailable - magnetic north shown"
                                    else "स्थान उपलब्ध छैन - चुम्बकीय उत्तर देखाइँदैछ",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                declination = CompassEngine.declinationFor(coords.first, coords.second)
                                trueNorth = true
                            }
                        } else {
                            trueNorth = false
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = if (trueNorth) {
                        if (isEn) "True North (+${"%.1f".format(declination)}°)" else "साँचो उत्तर (+${"%.1f".format(declination)}°)"
                    } else {
                        if (isEn) "Magnetic North" else "चुम्बकीय उत्तर"
                    },
                    color = if (trueNorth) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = when {
                    unreliable -> if (isEn) "Needs calibration" else "क्यालिब्रेसन आवश्यक"
                    tilt > 25f -> if (isEn) "Hold phone flat" else "फोन समतल राख्नुहोस्"
                    trueNorth -> if (isEn) "True north" else "साँचो उत्तर"
                    else -> if (isEn) "Magnetic north" else "चुम्बकीय उत्तर"
                },
                fontSize = 12.5.sp,
                color = when {
                    unreliable -> MaterialTheme.colorScheme.primary
                    tilt > 25f -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (unreliable) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        if (unreliable) {
            Text(
                text = if (isEn)
                    "Move the phone in a figure-8 motion to recalibrate the sensor."
                else "सेन्सर पुनः क्यालिब्रेट गर्न फोनलाई आठको आकारमा घुमाउनुहोस्।",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = if (isEn) "Heading" else "दिशा",
                value = "${if (isEn) DIR_SHORT[dirIdx] else DIR_NP[dirIdx]} ${formatDegrees(displayHeading, ThemePrefs.nepaliDigits.value)}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = if (isEn) "Declination" else "डिक्लिनेसन",
                value = if (trueNorth) "${if (declination >= 0) "+" else "-"}${"%.1f".format(kotlin.math.abs(declination))}°"
                else "0.0°",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = if (isEn) "Tilt" else "झुकाव",
                value = "${formatDegrees(tilt.coerceIn(0f, 180f), ThemePrefs.nepaliDigits.value)}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatDegrees(value: Float, nepaliDigits: Boolean): String {
    val normalized = ((value % 360f) + 360f) % 360f
    val text = "${normalized.toInt()}°"
    if (!nepaliDigits) return text
    val devanagari = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
    return text.map { c ->
        if (c.isDigit()) devanagari[c - '0'] else c
    }.joinToString("")
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
