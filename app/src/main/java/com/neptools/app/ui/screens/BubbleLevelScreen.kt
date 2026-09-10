package com.neptools.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.level.BubbleLevelEngine
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlin.math.abs

private val OK_GREEN = Color(0xFF16A34A)
private val WARN_AMBER = Color(0xFFD97706)

@Composable
fun BubbleLevelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    var hasSensor by remember { mutableStateOf(true) }
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    var slope by remember { mutableFloatStateOf(0f) }
    var mode by remember { mutableIntStateOf(0) }
    var frozen by remember { mutableStateOf(false) }
    var soundOn by remember { mutableStateOf(false) }
    var zeroApplied by remember { mutableStateOf(false) }

    var engineRef by remember { mutableStateOf<BubbleLevelEngine?>(null) }

    DisposableEffect(Unit) {
        val eng = BubbleLevelEngine(context.applicationContext, object : BubbleLevelEngine.Listener {
            override fun onUpdate(pitchDeg: Float, rollDeg: Float, slopeDeg: Float) {
                if (frozen) return
                pitch = pitchDeg
                roll = rollDeg
                slope = slopeDeg
            }
        })
        engineRef = eng
        hasSensor = eng.hasSensor
        zeroApplied = eng.hasZero()
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = if (isEn) "Bubble Level" else "बबल लेभल",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
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
                    imageVector = PIcons.Level,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = if (isEn) "Accelerometer not available" else "एक्सेलेरोमिटर उपलब्ध छैन",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeChip(if (isEn) "Auto" else "अटो", mode == 0, Modifier.weight(1f)) { mode = 0 }
            ModeChip(if (isEn) "Flat" else "समतल", mode == 1, Modifier.weight(1f)) { mode = 1 }
            ModeChip(if (isEn) "Edge" else "किनारा", mode == 2, Modifier.weight(1f)) { mode = 2 }
            Spacer(Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (soundOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .clickable { soundOn = !soundOn },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PIcons.SoundWave,
                    contentDescription = null,
                    tint = if (soundOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        val effectiveEdge = when (mode) {
            1 -> false
            2 -> true
            else -> slope > 50f
        }

        if (!effectiveEdge) {
            SurfaceBullseye(
                pitch = pitch,
                roll = roll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp)
                    .aspectRatio(1f)
            )
        } else {
            EdgeVial(
                slopeDeg = slope,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .aspectRatio(1.45f)
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            ReadoutChip(
                label = if (effectiveEdge) {
                    if (isEn) "Slope" else "ढलान"
                } else {
                    if (isEn) "Tilt X" else "झुकाव X"
                },
                value = formatSigned(if (effectiveEdge) slope else roll),
                level = levelState(if (effectiveEdge) abs(slope) else maxOf(abs(pitch), abs(roll))),
                modifier = Modifier.weight(1f)
            )
            if (!effectiveEdge) {
                ReadoutChip(
                    label = if (isEn) "Tilt Y" else "झुकाव Y",
                    value = formatSigned(pitch),
                    level = levelState(maxOf(abs(pitch), abs(roll))),
                    modifier = Modifier.weight(1f)
                )
            } else {
                ReadoutChip(
                    label = if (isEn) "vs Vertical" else "ठाडोबाट",
                    value = formatSigned(90f - abs(slope).coerceIn(0f, 180f)),
                    level = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionPill(
                icon = PIcons.Check,
                label = if (isEn) "Set Zero" else "शून्य सेट",
                modifier = Modifier.weight(1f)
            ) {
                engineRef?.setZero(pitch, roll, slope)
                zeroApplied = true
            }
            ActionPill(
                icon = PIcons.Refresh,
                label = if (isEn) "Reset" else "रिसेट",
                enabled = zeroApplied,
                modifier = Modifier.weight(1f)
            ) {
                engineRef?.clearZero()
                zeroApplied = false
            }
            ActionPill(
                icon = if (frozen) PIcons.Play else PIcons.Timer,
                label = if (frozen) {
                    if (isEn) "Resume" else "जारी"
                } else {
                    if (isEn) "Freeze" else "फ्रीज"
                },
                active = frozen,
                modifier = Modifier.weight(1f)
            ) {
                frozen = !frozen
            }
        }

        Spacer(Modifier.weight(1f))

        val bothLevel = !effectiveEdge && abs(pitch) < 0.6f && abs(roll) < 0.6f
        val prevLevel = remember { mutableStateOf(false) }
        androidx.compose.runtime.LaunchedEffect(bothLevel, soundOn) {
            if (soundOn && bothLevel && !prevLevel.value) {
                BubbleLevelEngine.beepLevel(context)
            }
            prevLevel.value = bothLevel
        }

        Text(
            text = when {
                effectiveEdge -> if (isEn)
                    "Rest any phone edge against the surface to measure its slope."
                else "सतहमा फोनको कुनै किनारा राखेर ढलान नाप्नुहोस्।"
                else -> if (isEn)
                    "Place the phone flat on the surface. Green bubble means level."
                else "फोन सतहमा समतल राख्नुहोस्। हरियो बबल भनेको सतह समतल छ।"
            },
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(bottom = 22.dp)
        )
    }
}

private fun formatSigned(v: Float): String =
    "${if (v >= 0) "" else "-"}${kotlin.math.abs(v).toInt()}"

private fun levelState(maxAbsDeg: Float): Int = when {
    maxAbsDeg < 0.6f -> 0
    maxAbsDeg < 3f -> 1
    else -> 2
}

@Composable
private fun ModeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReadoutChip(
    label: String,
    value: String,
    level: Int,
    modifier: Modifier = Modifier
) {
    val accent = when (level) {
        0 -> OK_GREEN
        1 -> WARN_AMBER
        else -> MaterialTheme.colorScheme.primary
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(accent, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = "$value°",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    active: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    active -> MaterialTheme.colorScheme.primary
                    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SurfaceBullseye(pitch: Float, roll: Float, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val side = minOf(size.width, size.height)
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = side / 2f

        drawCircle(color = Color(0x14000000), radius = outerR)
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = outerR,
            style = Stroke(width = outerR * 0.012f)
        )

        drawLine(
            color = Color.Gray.copy(alpha = 0.35f),
            start = Offset(cx - outerR, cy),
            end = Offset(cx + outerR, cy),
            strokeWidth = outerR * 0.006f
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.35f),
            start = Offset(cx, cy - outerR),
            end = Offset(cx, cy + outerR),
            strokeWidth = outerR * 0.006f
        )

        val scale = outerR / 30f
        val targetR = 0.8f * scale
        val warnR = 4f * scale

        drawCircle(color = OK_GREEN.copy(alpha = 0.18f), radius = warnR, center = Offset(cx, cy))
        drawCircle(color = OK_GREEN, radius = targetR, center = Offset(cx, cy), style = Stroke(width = outerR * 0.008f))
        drawCircle(color = OK_GREEN.copy(alpha = 0.55f), radius = warnR, center = Offset(cx, cy), style = Stroke(width = outerR * 0.006f))

        val bubbleX = cx + (roll.coerceIn(-28f, 28f) / 28f) * (outerR * 0.82f)
        val bubbleY = cy + (pitch.coerceIn(-28f, 28f) / 28f) * (outerR * 0.82f)
        val dist = kotlin.math.hypot((bubbleX - cx) / scale.toDouble(), (bubbleY - cy) / scale.toDouble())
        val bubbleColor = when {
            dist < 0.9 -> OK_GREEN
            dist < 4.2 -> WARN_AMBER
            else -> primaryColor
        }

        drawCircle(color = bubbleColor.copy(alpha = 0.30f), radius = outerR * 0.085f, center = Offset(bubbleX, bubbleY))
        drawCircle(color = bubbleColor, radius = outerR * 0.062f, center = Offset(bubbleX, bubbleY))
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = outerR * 0.018f,
            center = Offset(bubbleX - outerR * 0.02f, bubbleY - outerR * 0.02f)
        )
        drawCircle(
            color = bubbleColor,
            radius = outerR * 0.062f,
            center = Offset(bubbleX, bubbleY),
            style = Stroke(width = outerR * 0.006f)
        )
    }
}

@Composable
private fun EdgeVial(slopeDeg: Float, modifier: Modifier = Modifier) {
    val bigPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val tubeH = h * 0.30f
        val tubeTop = (h - tubeH) / 2f
        val padX = w * 0.06f
        val tubeW = w - padX * 2f

        val tubeRect = androidx.compose.ui.geometry.Rect(padX, tubeTop, padX + tubeW, tubeTop + tubeH)
        drawRoundRect(
            color = Color(0x12000000),
            topLeft = tubeRect.topLeft,
            size = tubeRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(tubeH / 2f, tubeH / 2f)
        )
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.35f),
            topLeft = tubeRect.topLeft,
            size = tubeRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(tubeH / 2f, tubeH / 2f),
            style = Stroke(width = h * 0.008f)
        )

        for (deg in 0..90 step 5) {
            val t = deg / 90f
            val x = padX + tubeW * t
            val isMajor = deg % 15 == 0
            val tickLen = if (isMajor) tubeH * 0.26f else tubeH * 0.14f
            drawLine(
                color = Color.Gray.copy(alpha = if (isMajor) 0.6f else 0.32f),
                start = Offset(x, tubeTop),
                end = Offset(x, tubeTop + tickLen),
                strokeWidth = if (deg % 45 == 0) h * 0.010f else h * 0.006f,
                cap = StrokeCap.Round
            )
            if (deg % 45 == 0 || deg == 90) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.6f),
                    start = Offset(x, tubeTop + tubeH - tickLen),
                    end = Offset(x, tubeTop + tubeH),
                    strokeWidth = h * 0.008f,
                    cap = StrokeCap.Round
                )
            }
        }

        val clamped = slopeDeg.coerceIn(0f, 90f)
        val bx = padX + tubeW * (clamped / 90f)
        val by = tubeTop + tubeH / 2f
        val bubbleColor = when {
            clamped <= 0.6f || clamped >= 89.4f -> OK_GREEN
            clamped < 3f || clamped > 87f -> WARN_AMBER
            else -> primaryColor
        }

        val pointerTop = tubeTop - h * 0.05f
        val tri = Path().apply {
            moveTo(bx, pointerTop + h * 0.055f)
            lineTo(bx - h * 0.032f, pointerTop)
            lineTo(bx + h * 0.032f, pointerTop)
            close()
        }
        drawPath(tri, color = Color.Gray.copy(alpha = 0.75f))

        drawCircle(color = bubbleColor.copy(alpha = 0.30f), radius = tubeH * 0.36f, center = Offset(bx, by))
        drawCircle(color = bubbleColor, radius = tubeH * 0.27f, center = Offset(bx, by))

        bigPaint.textSize = h * 0.135f
        bigPaint.isFakeBoldText = true
        bigPaint.color = onSurfaceColor.toArgb()
        drawContext.canvas.nativeCanvas.drawText(
            "${clamped.toInt()}",
            w / 2f,
            tubeTop + tubeH + h * 0.17f,
            bigPaint
        )

        bigPaint.textSize = h * 0.075f
        bigPaint.isFakeBoldText = false
        bigPaint.color = onSurfaceVariantColor.toArgb()
        drawContext.canvas.nativeCanvas.drawText(
            "degree",
            w / 2f,
            tubeTop + tubeH + h * 0.31f,
            bigPaint
        )
    }
}
