package com.neptools.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.audio.PetWhistleEngine
import com.neptools.app.core.audio.WhistlePulsePattern
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat

data class WhistlePreset(
    val titleNp: String,
    val titleEn: String,
    val freqHz: Int,
    val pattern: WhistlePulsePattern,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetWhistleScreen(
    onBack: () -> Unit
) {
    val isEn = ThemePrefs.lang.value == "en"
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var frequencyHz by remember { mutableIntStateOf(16000) }
    var volume by remember { mutableFloatStateOf(0.85f) }
    var selectedPattern by remember { mutableStateOf(WhistlePulsePattern.CONTINUOUS) }
    var isPlaying by remember { mutableStateOf(false) }
    var selectedTimerSec by remember { mutableIntStateOf(0) } // 0 = continuous
    var remainingTimerSec by remember { mutableIntStateOf(0) }

    val presets = remember {
        listOf(
            WhistlePreset("कुकुर बोलाउने", "Dog Recall", 16000, WhistlePulsePattern.CONTINUOUS, PIcons.Whistle, Color(0xFF2563EB)),
            WhistlePreset("कुकुर भुक्न रोक्ने", "Stop Barking", 18500, WhistlePulsePattern.RAPID_BEEP, PIcons.SoundWave, Color(0xFFD97706)),
            WhistlePreset("बिरालो ध्यान", "Cat Attention", 21000, WhistlePulsePattern.SLOW_PULSE, PIcons.Whistle, Color(0xFF7C3AED)),
            WhistlePreset("लामखुट्टे भगाउने", "Mosquito Repel", 19500, WhistlePulsePattern.STROBE, PIcons.SoundWave, Color(0xFF059669)),
            WhistlePreset("मानव कान क्षमता", "Human Hearing (14k)", 14000, WhistlePulsePattern.CONTINUOUS, PIcons.Bell, Color(0xFFE11D48)),
            WhistlePreset("स्पीकर परीक्षण", "Speaker Test (10k)", 10000, WhistlePulsePattern.CONTINUOUS, PIcons.Radio, Color(0xFF0891B2))
        )
    }

    // Sync state with engine
    LaunchedEffect(frequencyHz) {
        PetWhistleEngine.frequencyHz = frequencyHz
    }

    LaunchedEffect(volume) {
        PetWhistleEngine.volume = volume
    }

    LaunchedEffect(selectedPattern) {
        PetWhistleEngine.pulsePattern = selectedPattern
    }

    // Auto-stop countdown timer
    LaunchedEffect(isPlaying, selectedTimerSec) {
        if (isPlaying && selectedTimerSec > 0) {
            remainingTimerSec = selectedTimerSec
            while (remainingTimerSec > 0 && isPlaying) {
                delay(1000)
                if (isPlaying) {
                    remainingTimerSec--
                    if (remainingTimerSec <= 0) {
                        PetWhistleEngine.stopPlayback()
                        isPlaying = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            }
        } else {
            remainingTimerSec = 0
        }
    }

    // Stop engine on unmount
    DisposableEffect(Unit) {
        onDispose {
            PetWhistleEngine.stopPlayback()
        }
    }

    fun togglePlayback() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (isPlaying) {
            PetWhistleEngine.stopPlayback()
            isPlaying = false
        } else {
            PetWhistleEngine.startPlayback {
                isPlaying = false
            }
            isPlaying = true
        }
    }

    val themeColor = when {
        frequencyHz < 12000 -> Color(0xFF0891B2) // Cyan
        frequencyHz < 16000 -> Color(0xFF2563EB) // Blue
        frequencyHz < 19000 -> Color(0xFFD97706) // Amber
        frequencyHz < 21000 -> Color(0xFF7C3AED) // Violet
        else -> Color(0xFFE11D48) // Rose
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = 130f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        ToolTopBar(
            title = if (isEn) "High-Frequency Whistle" else "उच्च-फ्रिक्वेन्सी सिट्टी",
            subtitle = if (isEn) "Dog recall, repellent & frequency generator" else "कुकुर तालिम, लामखुट्टे तथा अडियो फ्रिक्वेन्सी",
            onBack = {
                PetWhistleEngine.stopPlayback()
                onBack()
            }
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Ultrasonic Radar / Sound Visualizer
            Box(
                Modifier
                    .size(240.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Canvas(Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = themeColor.copy(alpha = pulseAlpha),
                            radius = pulseRadius.dp.toPx(),
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = themeColor.copy(alpha = (pulseAlpha * 0.6f)),
                            radius = (pulseRadius * 0.7f).dp.toPx(),
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Center Power Button
                Box(
                    Modifier
                        .size(140.dp)
                        .shadow(16.dp, CircleShape, spotColor = themeColor.copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (isPlaying) themeColor else MaterialTheme.colorScheme.surface,
                                    if (isPlaying) themeColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .border(
                            4.dp,
                            if (isPlaying) Color.White.copy(alpha = 0.9f) else themeColor.copy(alpha = 0.4f),
                            CircleShape
                        )
                        .clickable { togglePlayback() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (isPlaying) PIcons.SoundWave else PIcons.Whistle,
                            null,
                            tint = if (isPlaying) Color.White else themeColor,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isPlaying) (if (isEn) "EMITTING" else "बज्दैछ") else (if (isEn) "TAP TO PLAY" else "बजाउनुहोस्"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isPlaying) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Readout Badge
            val freqKhz = frequencyHz / 1000.0
            val df = DecimalFormat("#.#")
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(themeColor.copy(alpha = 0.12f))
                    .border(1.dp, themeColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${df.format(freqKhz)} kHz",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = themeColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "(${if (isEn) frequencyHz else npNum(frequencyHz)} Hz)",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Presets Horizontal Row
            Text(
                if (isEn) "Quick Training & Testing Presets" else "छिटो तालिम तथा परीक्षण विकल्पहरू",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            LazyRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = frequencyHz == preset.freqHz && selectedPattern == preset.pattern
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) preset.color else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (isSelected) preset.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                frequencyHz = preset.freqHz
                                selectedPattern = preset.pattern
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else preset.color,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    if (isEn) preset.titleEn else preset.titleNp,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    "${preset.freqHz / 1000} kHz",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Fine Frequency Slider & Step Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isEn) "Frequency Slider" else "फ्रिक्वेन्सी समायोजन", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("8 kHz – 22 kHz", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(8.dp))

                    Slider(
                        value = frequencyHz.toFloat(),
                        onValueChange = { frequencyHz = it.toInt() },
                        valueRange = 8000f..22000f,
                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor)
                    )

                    // Fine Step +/- 100Hz and +/- 500Hz buttons
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StepChip("-500") { frequencyHz = (frequencyHz - 500).coerceAtLeast(1000) }
                            StepChip("-100") { frequencyHz = (frequencyHz - 100).coerceAtLeast(1000) }
                        }
                        Text(if (isEn) "Fine Tune" else "फाइन ट्युन", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StepChip("+100") { frequencyHz = (frequencyHz + 100).coerceAtMost(24000) }
                            StepChip("+500") { frequencyHz = (frequencyHz + 500).coerceAtMost(24000) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Pulse Pattern Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (isEn) "Tone Pattern" else "पल्स ढाँचा रोज्नुहोस्",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WhistlePulsePattern.values().forEach { pattern ->
                            val isSelected = selectedPattern == pattern
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedPattern = pattern }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (pattern) {
                                        WhistlePulsePattern.CONTINUOUS -> if (isEn) "Steady" else "स्थिर"
                                        WhistlePulsePattern.SLOW_PULSE -> if (isEn) "Slow" else "सुस्त"
                                        WhistlePulsePattern.RAPID_BEEP -> if (isEn) "Rapid" else "द्रुत"
                                        WhistlePulsePattern.STROBE -> if (isEn) "Strobe" else "स्ट्रोब"
                                        WhistlePulsePattern.SOS -> "SOS"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Auto-Stop Timer & Volume
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Timer row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isEn) "Auto-Stop Timer" else "अटो-स्टप टाइमर", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        if (isPlaying && remainingTimerSec > 0) {
                            Text(
                                "${if (isEn) "Stopping in " else "रोकिने समय: "}${if (isEn) remainingTimerSec else npNum(remainingTimerSec)}s",
                                style = MaterialTheme.typography.labelSmall.copy(color = themeColor, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 5, 10, 30, 60).forEach { sec ->
                            val isSelected = selectedTimerSec == sec
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedTimerSec = sec }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (sec == 0) (if (isEn) "Off" else "बन्द") else "${sec}s",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(Modifier.height(14.dp))

                    // Volume row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isEn) "Audio Volume" else "ध्वनी भोल्युम", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text("${(volume * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(color = themeColor, fontWeight = FontWeight.Bold))
                    }

                    Slider(
                        value = volume,
                        onValueChange = { volume = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Safety & Pet Training Guide Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Info, null, tint = themeColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEn) "Pet Training & Safety Notes" else "जनावर तालिम तथा सुरक्षा जानकारी",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (isEn) "• Dogs can hear up to 45 kHz, while adult humans usually hear up to 15-17 kHz.\n• Pair whistle pulses with positive rewards (treats & praise) for quick recall conditioning.\n• Avoid prolonged continuous playback near pet ears to protect their sensitive hearing."
                        else "• कुकुरहरूले ४५ किलोहर्जसम्मको ध्वनि सुन्न सक्छन् भने वयस्क मानिसले १५-१७ किलोहर्जसम्म मात्र सुन्न सक्छन्।\n• सिट्टी बजाएर बोलाउँदा खाजा वा प्रशंसा दिएर तालिम गर्दा कुकुरले छिट्टै सिक्छ।\n• जनावरको कान नजिक लामो समयसम्म ठूलो आवाजमा नबजाउनुहोस्।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepChip(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
}
