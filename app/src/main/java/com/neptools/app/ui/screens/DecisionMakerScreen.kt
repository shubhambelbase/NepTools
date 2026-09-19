package com.neptools.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class DecisionToolMode {
    WHEEL, COIN, DICE
}

data class WheelChoice(
    val id: String,
    val text: String,
    val color: Color,
    val enabled: Boolean = true
)

private val WHEEL_PALETTE = listOf(
    Color(0xFFE11D48), // Vibrant Rose
    Color(0xFF2563EB), // Royal Blue
    Color(0xFF059669), // Emerald
    Color(0xFFD97706), // Warm Amber
    Color(0xFF7C3AED), // Violet
    Color(0xFF0891B2), // Cyan
    Color(0xFFEA580C), // Orange
    Color(0xFF4F46E5), // Indigo
    Color(0xFF16A34A), // Forest Green
    Color(0xFFDB2777), // Deep Pink
    Color(0xFF0D9488), // Teal
    Color(0xFFB45309)  // Bronze
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionMakerScreen(
    onBack: () -> Unit
) {
    val isEn = ThemePrefs.lang.value == "en"
    var currentMode by remember { mutableStateOf(DecisionToolMode.WHEEL) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Navigation Bar
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
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PIcons.ChevronLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Decision Wheel & Dice" else "निर्णय चक्र, सिक्का तथा पासा",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Mode Switcher Tabs
        TabRow(
            selectedTabIndex = currentMode.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[currentMode.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = currentMode == DecisionToolMode.WHEEL,
                onClick = { currentMode = DecisionToolMode.WHEEL },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.DecisionWheel, null, Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isEn) "Wheel" else "भाग्य चक्र", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = currentMode == DecisionToolMode.COIN,
                onClick = { currentMode = DecisionToolMode.COIN },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Coin, null, Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isEn) "Coin Flip" else "सिक्का टस", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = currentMode == DecisionToolMode.DICE,
                onClick = { currentMode = DecisionToolMode.DICE },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Dice, null, Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isEn) "Dice Roller" else "पासा रोल", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        AnimatedContent(
            targetState = currentMode,
            transitionSpec = {
                fadeIn(tween(180)) togetherWith fadeOut(tween(140))
            },
            modifier = Modifier.fillMaxSize(),
            label = "DecisionToolMode"
        ) { mode ->
            when (mode) {
                DecisionToolMode.WHEEL -> DecisionWheelView(isEn = isEn)
                DecisionToolMode.COIN -> CoinFlipperView(isEn = isEn)
                DecisionToolMode.DICE -> DiceRollerView(isEn = isEn)
            }
        }
    }
}

// =============================================================================
// 1. ENHANCED DECISION WHEEL VIEW WITH PARTICLES & CELEBRATION
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecisionWheelView(isEn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val defaultChoices = remember(isEn) {
        val list = if (isEn) {
            listOf("Momo", "Thakali", "Chowmein", "Pizza", "Sekuwa", "Biryani", "Burger", "Dal Bhat")
        } else {
            listOf("मोमो", "थकाली", "चाउमिन", "पिझ्जा", "सेकुवा", "बिरयानी", "बर्गर", "दालभात")
        }
        list.mapIndexed { idx, txt ->
            WheelChoice(
                id = "choice_$idx",
                text = txt,
                color = WHEEL_PALETTE[idx % WHEEL_PALETTE.size]
            )
        }
    }

    val choices = remember { mutableStateListOf<WheelChoice>().apply { addAll(defaultChoices) } }
    val activeChoices = choices.filter { it.enabled }

    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var winningChoice by remember { mutableStateOf<WheelChoice?>(null) }
    var showWinnerDialog by remember { mutableStateOf(false) }
    var showCustomizerSheet by remember { mutableStateOf(false) }

    fun spinWheel() {
        if (isSpinning || activeChoices.isEmpty()) return
        isSpinning = true
        winningChoice = null
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        scope.launch {
            val totalActive = activeChoices.size
            val sliceAngle = 360f / totalActive
            val winningIndex = Random.nextInt(totalActive)
            val winner = activeChoices[winningIndex]

            // Wheel top pointer is located at 270 deg (top center).
            // Center of winning slice must rest at 270 deg upon landing.
            val sliceCenter = (winningIndex * sliceAngle) + (sliceAngle / 2f)
            val targetPointerOffset = (270f - sliceCenter + 360f) % 360f

            val currentMod = rotation.value % 360f
            val fullSpins = (6..9).random() * 360f
            val finalTarget = rotation.value + (360f - currentMod) + fullSpins + targetPointerOffset

            // Haptic ticking sequence while spinning
            launch {
                val startTime = System.currentTimeMillis()
                val duration = 4000L
                var delayMs = 45L
                while (System.currentTimeMillis() - startTime < duration) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    delay(delayMs)
                    val elapsed = System.currentTimeMillis() - startTime
                    delayMs = (45L + (elapsed * 0.08f).toLong()).coerceAtMost(250L)
                }
            }

            rotation.animateTo(
                targetValue = finalTarget,
                animationSpec = tween(
                    durationMillis = 4000,
                    easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f)
                )
            )

            isSpinning = false
            winningChoice = winner
            showWinnerDialog = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preset selector bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEn) "Quick Presets" else "छिटो विकल्पहरू",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(
                onClick = { showCustomizerSheet = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(PIcons.Edit, null, Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isEn) "Edit Options (${activeChoices.size})" else "विकल्प थप्नुहोस् (${activeChoices.size})",
                    fontSize = 12.sp
                )
            }
        }

        LazyRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 14.dp)
        ) {
            item {
                PresetChip(if (isEn) "Nepali Food" else "खाना / खाजा", onClick = {
                    choices.clear()
                    val list = if (isEn) {
                        listOf("Momo", "Thakali", "Chowmein", "Pizza", "Sekuwa", "Biryani", "Burger", "Khaja Set")
                    } else {
                        listOf("मोमो", "थकाली", "चाउमिन", "पिझ्जा", "सेकुवा", "बिरयानी", "बर्गर", "खाजा सेट")
                    }
                    choices.addAll(list.mapIndexed { i, t ->
                        WheelChoice("food_$i", t, WHEEL_PALETTE[i % WHEEL_PALETTE.size])
                    })
                })
            }
            item {
                PresetChip(if (isEn) "Yes / No / Maybe" else "हो / होइन / सायद", onClick = {
                    choices.clear()
                    val list = if (isEn) {
                        listOf("YES", "NO", "MAYBE", "DEFINITELY")
                    } else {
                        listOf("हो", "होइन", "सायद", "पक्कै")
                    }
                    choices.addAll(list.mapIndexed { i, t ->
                        WheelChoice("yn_$i", t, WHEEL_PALETTE[i % WHEEL_PALETTE.size])
                    })
                })
            }
            item {
                PresetChip(if (isEn) "Who Pays?" else "कसले तिर्ने?", onClick = {
                    choices.clear()
                    val list = if (isEn) {
                        listOf("Me", "You", "50 / 50 Split", "Next Time")
                    } else {
                        listOf("मैले", "तिमीले", "आधा-आधा", "अर्को पटक")
                    }
                    choices.addAll(list.mapIndexed { i, t ->
                        WheelChoice("pay_$i", t, WHEEL_PALETTE[i % WHEEL_PALETTE.size])
                    })
                })
            }
            item {
                PresetChip(if (isEn) "Travel Spots" else "घुम्न जाने ठाउँ", onClick = {
                    choices.clear()
                    val list = if (isEn) {
                        listOf("Pokhara", "Mustang", "Chitwan", "Nagarkot", "Bandipur", "Ilam", "Rara")
                    } else {
                        listOf("पोखरा", "मुस्ताङ", "चितवन", "नगरकोट", "बन्दीपुर", "इलाम", "रारा")
                    }
                    choices.addAll(list.mapIndexed { i, t ->
                        WheelChoice("trv_$i", t, WHEEL_PALETTE[i % WHEEL_PALETTE.size])
                    })
                })
            }
            item {
                PresetChip(if (isEn) "Truth or Dare" else "सत्य वा चुनौती", onClick = {
                    choices.clear()
                    val list = if (isEn) {
                        listOf("Truth", "Dare", "Pass", "Double Dare")
                    } else {
                        listOf("सत्य", "चुनौती", "पास", "दोहोरो चुनौती")
                    }
                    choices.addAll(list.mapIndexed { i, t ->
                        WheelChoice("td_$i", t, WHEEL_PALETTE[i % WHEEL_PALETTE.size])
                    })
                })
            }
            item {
                PresetChip(if (isEn) "1 to 6" else "१ देखि ६", onClick = {
                    choices.clear()
                    val list = if (isEn) (1..6).map { "$it" } else listOf("१", "२", "३", "४", "५", "६")
                    choices.addAll(
                        list.mapIndexed { i, t ->
                            WheelChoice("num_$i", t, WHEEL_PALETTE[i % WHEEL_PALETTE.size])
                        }
                    )
                })
            }
        }

        Spacer(Modifier.height(8.dp))

        // Wheel Surface with Metallic Studded Rim
        Box(
            Modifier
                .size(316.dp)
                .shadow(24.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                Modifier
                    .size(306.dp)
                    .clip(CircleShape)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                val sliceAngle = if (activeChoices.isNotEmpty()) 360f / activeChoices.size else 360f

                rotate(degrees = rotation.value, pivot = center) {
                    if (activeChoices.isEmpty()) {
                        drawCircle(Color(0xFFCBD5E1), radius = radius, center = center)
                    } else {
                        activeChoices.forEachIndexed { index, choice ->
                            val startAngle = index * sliceAngle
                            // Draw Slice
                            drawArc(
                                color = choice.color,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                size = Size(size.width, size.height),
                                topLeft = Offset.Zero
                            )

                            // Slice Divider Line
                            drawArc(
                                color = Color.White.copy(alpha = 0.45f),
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                style = Stroke(width = 2.5.dp.toPx()),
                                size = Size(size.width, size.height),
                                topLeft = Offset.Zero
                            )

                            // Radial text placement
                            val textAngleRad = Math.toRadians((startAngle + sliceAngle / 2f).toDouble())
                            val textDist = radius * 0.62f
                            val textX = (center.x + textDist * cos(textAngleRad)).toFloat()
                            val textY = (center.y + textDist * sin(textAngleRad)).toFloat()

                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = if (activeChoices.size > 8) 30f else 38f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                setShadowLayer(6f, 1f, 1f, android.graphics.Color.argb(160, 0, 0, 0))
                            }

                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.rotate(
                                (startAngle + sliceAngle / 2f + 90f),
                                textX,
                                textY
                            )
                            val displayText = if (choice.text.length > 11) choice.text.take(9) + "…" else choice.text
                            drawContext.canvas.nativeCanvas.drawText(displayText, textX, textY, paint)
                            drawContext.canvas.nativeCanvas.restore()
                        }
                    }
                }

                // Outer Metallic Gold Beaded Rim
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFE4B5), Color(0xFFFFD700))
                    ),
                    radius = radius - 3.dp.toPx(),
                    center = center,
                    style = Stroke(width = 6.dp.toPx())
                )

                // Beaded Rim Studs
                val numStuds = 24
                for (s in 0 until numStuds) {
                    val studAngleRad = Math.toRadians((s * (360f / numStuds)).toDouble())
                    val studDist = radius - 3.dp.toPx()
                    val sx = (center.x + studDist * cos(studAngleRad)).toFloat()
                    val sy = (center.y + studDist * sin(studAngleRad)).toFloat()
                    drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(sx, sy))
                }

                // Center Metallic Hub
                drawCircle(Color.White, radius = 26.dp.toPx(), center = center)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFF996515))),
                    radius = 20.dp.toPx(),
                    center = center
                )
                drawCircle(Color(0xFF1E293B), radius = 10.dp.toPx(), center = center)
            }

            // Top Pointer Needle with 3D Drop Shadow
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 1.dp)
                    .size(width = 34.dp, height = 40.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, size.height) // Arrow Tip
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, Color(0xFFE11D48), style = Fill)
                    drawPath(path, Color.White, style = Stroke(width = 2.5.dp.toPx()))
                    drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(size.width / 2f, 10.dp.toPx()))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Compact & Elegant Result Card (Non-intrusive, directly in screen flow)
        AnimatedVisibility(
            visible = winningChoice != null,
            enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { -15 }) + scaleIn(initialScale = 0.92f),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.92f)
        ) {
            if (winningChoice != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    winningChoice!!.color.copy(alpha = 0.12f),
                                    winningChoice!!.color.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(1.5.dp, winningChoice!!.color.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                PIcons.Award,
                                contentDescription = null,
                                tint = winningChoice!!.color,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "The Wheel Decided:" else "भाग्य चक्रको निर्णय:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Winning Choice Name Banner
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(winningChoice!!.color, winningChoice!!.color.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = winningChoice!!.text,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Quick Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    choices.remove(winningChoice)
                                    winningChoice = null
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(if (isEn) "Remove" else "हटाउनुहोस्", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { spinWheel() },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = winningChoice!!.color),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(if (isEn) "Spin Again" else "फेरि घुमाउनुहोस्", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Spin Action Button
        Button(
            onClick = { spinWheel() },
            enabled = !isSpinning && activeChoices.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                PIcons.DecisionWheel,
                null,
                Modifier
                    .size(22.dp)
                    .rotate(if (isSpinning) rotation.value else 0f)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                if (isSpinning) (if (isEn) "Spinning Fate…" else "भाग्य चक्र घुम्दैछ…")
                else (if (isEn) "SPIN THE WHEEL" else "भाग्य चक्र घुमाउनुहोस्"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Active Choices FlowRow
        Text(
            if (isEn) "Active Wheel Options:" else "सक्रिय विकल्पहरू:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))

        FlowRowChoices(choices = activeChoices)
    }

    // Customizer Bottom Sheet
    if (showCustomizerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomizerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            WheelCustomizerSheetContent(
                choices = choices,
                isEn = isEn,
                onAddChoice = { text ->
                    val newIdx = choices.size
                    choices.add(WheelChoice("c_${System.currentTimeMillis()}", text, WHEEL_PALETTE[newIdx % WHEEL_PALETTE.size]))
                },
                onToggleChoice = { choice ->
                    val idx = choices.indexOf(choice)
                    if (idx >= 0) {
                        choices[idx] = choice.copy(enabled = !choice.enabled)
                    }
                },
                onDeleteChoice = { choice -> choices.remove(choice) },
                onClose = { showCustomizerSheet = false }
            )
        }
    }
}



// =============================================================================
// 2. ENHANCED COIN FLIPPER VIEW (AUTHENTIC NEPALI COIN WITH 3D TUMBLE)
// =============================================================================

@Composable
private fun CoinFlipperView(isEn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isFlipping by remember { mutableStateOf(false) }
    var currentSide by remember { mutableStateOf("HEADS") }
    var totalFlips by remember { mutableIntStateOf(0) }
    var headsCount by remember { mutableIntStateOf(0) }
    var tailsCount by remember { mutableIntStateOf(0) }
    var streakCount by remember { mutableIntStateOf(0) }
    var streakSide by remember { mutableStateOf("HEADS") }
    var flipHistory by remember { mutableStateOf(listOf<String>()) }

    val rotationY = remember { Animatable(0f) }
    val rotationX = remember { Animatable(0f) }
    val translationY = remember { Animatable(0f) }

    fun flipCoin() {
        if (isFlipping) return
        isFlipping = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        scope.launch {
            val outcomeIsHeads = Random.nextBoolean()
            val finalTargetSide = if (outcomeIsHeads) "HEADS" else "TAILS"
            val totalSpinsY = (6..10).random() * 360f + (if (outcomeIsHeads) 0f else 180f)
            val wobbleX = (2..4).random() * 360f

            // Gravity arc jump
            launch {
                translationY.animateTo(-180f, tween(360, easing = LinearOutSlowInEasing))
                translationY.animateTo(0f, tween(440, easing = FastOutSlowInEasing))
            }

            // Multi-axis 3D tumbling
            launch {
                rotationX.animateTo(rotationX.value + wobbleX, tween(800, easing = FastOutSlowInEasing))
            }

            rotationY.animateTo(
                targetValue = rotationY.value + totalSpinsY,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )

            currentSide = finalTargetSide
            totalFlips++
            if (outcomeIsHeads) headsCount++ else tailsCount++

            if (finalTargetSide == streakSide) {
                streakCount++
            } else {
                streakSide = finalTargetSide
                streakCount = 1
            }

            flipHistory = listOf(finalTargetSide) + flipHistory.take(7)
            isFlipping = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // 3D Authentic Nepali Coin
        Box(
            Modifier
                .size(230.dp)
                .graphicsLayer {
                    this.rotationY = rotationY.value
                    this.rotationX = rotationX.value
                    this.translationY = translationY.value
                    cameraDistance = 16f * density
                }
                .shadow(24.dp, CircleShape, spotColor = Color(0xFFD97706).copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            val normalizedAngle = ((rotationY.value % 360f) + 360f) % 360f
            val isFrontVisible = normalizedAngle in 0f..90f || normalizedAngle in 270f..360f

            if (isFrontVisible) {
                // HEADS: Authentic Golden Nepal Rs 1 Royal Emblem
                NepaliHeadsCoinFace(isEn = isEn)
            } else {
                // TAILS: Silver Mt. Everest & Chandra-Surya Flag
                NepaliTailsCoinFace(isEn = isEn)
            }
        }

        Spacer(Modifier.height(28.dp))

        // Outcome Badge
        if (totalFlips > 0) {
            val isHead = currentSide == "HEADS"
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isHead) Color(0xFFFEF3C7) else Color(0xFFE2E8F0)
                    )
                    .border(
                        1.dp,
                        if (isHead) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isHead) (if (isEn) "Result: HEADS" else "नतिजा: राजा")
                           else (if (isEn) "Result: TAILS" else "नतिजा: सगरमाथा"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isHead) Color(0xFFB45309) else Color(0xFF334155)
                    )
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // Flip Action Button
        Button(
            onClick = { flipCoin() },
            enabled = !isFlipping,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
        ) {
            Icon(PIcons.Coin, null, Modifier.size(24.dp), tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text(
                if (isFlipping) (if (isEn) "Tossing Coin in the Air…" else "सिक्का उड्दैछ…")
                else (if (isEn) "TOSS COIN" else "सिक्का टस गर्नुहोस्"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Statistics Dashboard
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isEn) "Flip Statistics" else "टस तथ्याङ्क",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "${if (isEn) "Total: " else "जम्मा: "}${if (isEn) totalFlips else npNum(totalFlips)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBox(
                        label = if (isEn) "Heads" else "राजा",
                        count = headsCount,
                        percent = if (totalFlips > 0) (headsCount * 100 / totalFlips) else 0,
                        color = Color(0xFFD97706),
                        isEn = isEn
                    )
                    StatBox(
                        label = if (isEn) "Tails" else "सगरमाथा",
                        count = tailsCount,
                        percent = if (totalFlips > 0) (tailsCount * 100 / totalFlips) else 0,
                        color = Color(0xFF475569),
                        isEn = isEn
                    )
                }

                if (flipHistory.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        if (isEn) "Recent Flips:" else "अघिल्ला टसहरू:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        flipHistory.forEach { side ->
                            val isH = side == "HEADS"
                            Box(
                                Modifier
                                    .size(30.dp)
                                    .background(
                                        if (isH) Color(0xFFFEF3C7) else Color(0xFFE2E8F0),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (isH) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (isH) "H" else "T",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isH) Color(0xFFB45309) else Color(0xFF334155)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NepaliHeadsCoinFace(isEn: Boolean) {
    Box(
        Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFDF00), Color(0xFFD4AF37), Color(0xFF996515), Color(0xFF664200))
                )
            )
            .border(6.dp, Color(0xFFFFE082), CircleShape)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (isEn) "SHREE BHAVANI" else "श्री भवानी",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            )
            Spacer(Modifier.height(4.dp))
            Icon(PIcons.Award, null, tint = Color.White, modifier = Modifier.size(54.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                if (isEn) "HEADS" else "राजा",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
            Text(
                if (isEn) "NEPAL · Rs 1" else "नेपाल · १ रुपैयाँ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@Composable
private fun NepaliTailsCoinFace(isEn: Boolean) {
    Box(
        Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1), Color(0xFF64748B), Color(0xFF334155))
                )
            )
            .border(6.dp, Color(0xFFE2E8F0), CircleShape)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (isEn) "MT. EVEREST" else "सगरमाथा",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            )
            Spacer(Modifier.height(4.dp))
            Icon(PIcons.Stars, null, tint = Color.White, modifier = Modifier.size(54.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                if (isEn) "TAILS" else "नेपाल",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
            Text(
                if (isEn) "EVEREST · Rs 2" else "चन्द्र–सूर्य · २ रुपैयाँ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

// =============================================================================
// 3. ENHANCED DICE ROLLER VIEW (UP TO 6 DICE WITH THEMES)
// =============================================================================

private enum class DiceSkin {
    IVORY, EMERALD, RUBY, MIDNIGHT
}

@Composable
private fun DiceRollerView(isEn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var numDice by remember { mutableIntStateOf(2) }
    var diceValues by remember { mutableStateOf(listOf(4, 5)) }
    var isRolling by remember { mutableStateOf(false) }
    var rollHistory by remember { mutableStateOf(listOf<List<Int>>()) }
    var activeSkin by remember { mutableStateOf(DiceSkin.IVORY) }

    val rotation = remember { Animatable(0f) }
    val wobbleAnim = remember { Animatable(0f) }

    fun rollDice() {
        if (isRolling) return
        isRolling = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        scope.launch {
            // Rapid shuffling and haptic rattle
            launch {
                repeat(8) {
                    diceValues = (1..numDice).map { Random.nextInt(1, 7) }
                    wobbleAnim.animateTo((it % 2 * 10f) - 5f, tween(40))
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    delay(50)
                }
                wobbleAnim.animateTo(0f, tween(80))
            }

            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = tween(480, easing = FastOutSlowInEasing)
            )

            val finalDice = (1..numDice).map { Random.nextInt(1, 7) }
            diceValues = finalDice
            rollHistory = listOf(finalDice) + rollHistory.take(6)
            isRolling = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Number of dice selector (1 to 6)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEn) "Number of Dice:" else "पासाको सङ्ख्या:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..6).forEach { count ->
                    val isSelected = numDice == count
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                numDice = count
                                diceValues = (1..count).map { Random.nextInt(1, 7) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isEn) "$count" else npNum(count),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Skin Theme Selector
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEn) "Skin:" else "रंग:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DiceSkinChip("Ivory", activeSkin == DiceSkin.IVORY) { activeSkin = DiceSkin.IVORY }
            DiceSkinChip("Emerald", activeSkin == DiceSkin.EMERALD) { activeSkin = DiceSkin.EMERALD }
            DiceSkinChip("Ruby", activeSkin == DiceSkin.RUBY) { activeSkin = DiceSkin.RUBY }
            DiceSkinChip("Midnight", activeSkin == DiceSkin.MIDNIGHT) { activeSkin = DiceSkin.MIDNIGHT }
        }

        Spacer(Modifier.height(24.dp))

        // Render Dice Grid / Row
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val chunks = diceValues.chunked(3)
                chunks.forEach { rowDice ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowDice.forEach { value ->
                            DiceCube(
                                value = value,
                                rotationAngle = if (isRolling) rotation.value else 0f,
                                wobble = wobbleAnim.value,
                                skin = activeSkin
                            )
                            Spacer(Modifier.width(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Total Sum Badge
        val totalSum = diceValues.sum()
        Box(
            Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = "${if (isEn) "Total Sum: " else "जम्मा जोड: "}${if (isEn) totalSum else npNum(totalSum)}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        // Roll Action Button
        Button(
            onClick = { rollDice() },
            enabled = !isRolling,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
        ) {
            Icon(PIcons.Dice, null, Modifier.size(24.dp), tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text(
                if (isRolling) (if (isEn) "Rolling Dice…" else "पासा गुड्दैछ…")
                else (if (isEn) "ROLL DICE" else "पासा रोल गर्नुहोस्"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Roll History Log
        if (rollHistory.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        if (isEn) "Recent Rolls" else "अघिल्ला रोलहरू",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(10.dp))
                    rollHistory.forEachIndexed { idx, roll ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${if (isEn) "Roll #${idx + 1}" else "रोल #${npNum(idx + 1)}"} [${roll.joinToString(", ")}]",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Total = ${if (isEn) roll.sum() else npNum(roll.sum())}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiceSkinChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun DiceCube(
    value: Int,
    rotationAngle: Float,
    wobble: Float,
    skin: DiceSkin
) {
    val bgBrush = when (skin) {
        DiceSkin.IVORY -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)))
        DiceSkin.EMERALD -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        DiceSkin.RUBY -> Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))
        DiceSkin.MIDNIGHT -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
    }

    val dotColor = when (skin) {
        DiceSkin.IVORY -> Color(0xFF0F172A)
        DiceSkin.EMERALD -> Color(0xFFFFD700)
        DiceSkin.RUBY -> Color(0xFFFFFFFF)
        DiceSkin.MIDNIGHT -> Color(0xFF38BDF8)
    }

    val borderColor = when (skin) {
        DiceSkin.IVORY -> Color(0xFFCBD5E1)
        DiceSkin.EMERALD -> Color(0xFF059669)
        DiceSkin.RUBY -> Color(0xFFE11D48)
        DiceSkin.MIDNIGHT -> Color(0xFF334155)
    }

    Box(
        Modifier
            .size(74.dp)
            .rotate(rotationAngle + wobble)
            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF6366F1).copy(alpha = 0.35f))
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(2.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(9.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val dotRadius = 4.8.dp.toPx()
            val w = size.width
            val h = size.height

            val c = Offset(w / 2f, h / 2f)
            val tl = Offset(w * 0.25f, h * 0.25f)
            val tr = Offset(w * 0.75f, h * 0.25f)
            val bl = Offset(w * 0.25f, h * 0.75f)
            val br = Offset(w * 0.75f, h * 0.75f)
            val ml = Offset(w * 0.25f, h * 0.5f)
            val mr = Offset(w * 0.75f, h * 0.5f)

            when (value) {
                1 -> drawCircle(dotColor, dotRadius, c)
                2 -> {
                    drawCircle(dotColor, dotRadius, tl)
                    drawCircle(dotColor, dotRadius, br)
                }
                3 -> {
                    drawCircle(dotColor, dotRadius, tl)
                    drawCircle(dotColor, dotRadius, c)
                    drawCircle(dotColor, dotRadius, br)
                }
                4 -> {
                    drawCircle(dotColor, dotRadius, tl)
                    drawCircle(dotColor, dotRadius, tr)
                    drawCircle(dotColor, dotRadius, bl)
                    drawCircle(dotColor, dotRadius, br)
                }
                5 -> {
                    drawCircle(dotColor, dotRadius, tl)
                    drawCircle(dotColor, dotRadius, tr)
                    drawCircle(dotColor, dotRadius, c)
                    drawCircle(dotColor, dotRadius, bl)
                    drawCircle(dotColor, dotRadius, br)
                }
                6 -> {
                    drawCircle(dotColor, dotRadius, tl)
                    drawCircle(dotColor, dotRadius, tr)
                    drawCircle(dotColor, dotRadius, ml)
                    drawCircle(dotColor, dotRadius, mr)
                    drawCircle(dotColor, dotRadius, bl)
                    drawCircle(dotColor, dotRadius, br)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER COMPONENTS
// -------------------------------------------------------------

@Composable
private fun PresetChip(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChoices(choices: List<WheelChoice>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        choices.forEach { choice ->
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(choice.color.copy(alpha = 0.15f))
                    .border(1.dp, choice.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(choice.color, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        choice.text,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, count: Int, percent: Int, color: Color, isEn: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(
            if (isEn) "$count ($percent%)" else "${npNum(count)} (${npNum(percent)}%)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color)
        )
    }
}

@Composable
private fun WheelCustomizerSheetContent(
    choices: List<WheelChoice>,
    isEn: Boolean,
    onAddChoice: (String) -> Unit,
    onToggleChoice: (WheelChoice) -> Unit,
    onDeleteChoice: (WheelChoice) -> Unit,
    onClose: () -> Unit
) {
    var newText by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEn) "Customize Wheel Options" else "विकल्पहरू व्यवस्थापन गर्नुहोस्",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onClose) {
                Icon(PIcons.Cross, null)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Add new choice input
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                placeholder = { Text(if (isEn) "Enter new choice (e.g. Biryani)" else "नयाँ विकल्प लेख्नुहोस् (उदा. बिरयानी)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newText.isNotBlank()) {
                        onAddChoice(newText.trim())
                        newText = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                enabled = newText.isNotBlank()
            ) {
                Text(if (isEn) "Add" else "थप्नुहोस्")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            Modifier
                .fillMaxWidth()
                .height(300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(choices) { choice ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .background(choice.color, CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            choice.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (choice.enabled) FontWeight.Bold else FontWeight.Normal,
                                color = if (choice.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = choice.enabled,
                            onCheckedChange = { onToggleChoice(choice) },
                            colors = SwitchDefaults.colors(checkedThumbColor = choice.color)
                        )
                        IconButton(onClick = { onDeleteChoice(choice) }) {
                            Icon(PIcons.Cross, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isEn) "Done & Close" else "सम्पन्न भयो")
        }
        Spacer(Modifier.height(16.dp))
    }
}
