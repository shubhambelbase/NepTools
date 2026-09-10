package com.neptools.app.ui.screens

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.R
import com.neptools.app.ui.icons.PIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.sin

private data class SplashOrbitalItem(
    val icon: ImageVector,
    val bgColor: Color,
    val tintColor: Color,
    val initialAngleDeg: Double
)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current.density

    // Detect if device has disabled animations (Battery Saver or Accessibility Remove Animations)
    val areAnimationsDisabled = remember {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Throwable) {
            false
        }
    }

    // Motion Drivers
    val orbitSpin = remember { Animatable(0f) }
    val lineProgress = remember { Animatable(0f) }
    val orbitTrackAlpha = remember { Animatable(1f) }
    val sceneryScale = remember { Animatable(1.0f) }
    val logoScale = remember { Animatable(if (areAnimationsDisabled) 1f else 0.94f) }
    val contentAlpha = remember { Animatable(if (areAnimationsDisabled) 1f else 0.85f) }

    // Continuous Wave Driver
    val infiniteTransition = rememberInfiniteTransition(label = "splashBlink")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    val orbitalItems = remember {
        listOf(
            SplashOrbitalItem(PIcons.Calendar, Color(0xFFFEE2E2), Color(0xFFE11D48), 270.0), // Calendar
            SplashOrbitalItem(PIcons.Fuel, Color(0xFFFFEDD5), Color(0xFFEA580C), 342.0),     // Fuel
            SplashOrbitalItem(PIcons.Doc, Color(0xFFF3E8FF), Color(0xFF9333EA), 54.0),       // Documents
            SplashOrbitalItem(PIcons.QrCode, Color(0xFFDCFCE7), Color(0xFF16A34A), 126.0),   // QR Code
            SplashOrbitalItem(PIcons.Bank, Color(0xFFE0F2FE), Color(0xFF0284C7), 198.0)     // Calc
        )
    }

    // Original Authentic Warm Rice Paper & Crimson/Gold Sunrise Palette
    val backgroundBrush = remember {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFAF7F2),
                Color(0xFFF7F3EB),
                Color(0xFFF3ECE0),
                Color(0xFFEDE4D4)
            )
        )
    }

    val haloBrush = remember {
        Brush.radialGradient(
            listOf(
                Color(0xFFE11D48).copy(alpha = 0.18f),
                Color(0xFFF59E0B).copy(alpha = 0.09f),
                Color.Transparent
            )
        )
    }

    // Fail-safe single-fire guard
    val finishedGuard = remember { AtomicBoolean(false) }
    val currentFinish by rememberUpdatedState(onSplashFinished)
    fun safeFinish() {
        if (finishedGuard.compareAndSet(false, true)) {
            currentFinish()
        }
    }

    // Guaranteed execution coroutine lifecycle
    LaunchedEffect(Unit) {
        if (areAnimationsDisabled) {
            // Instant graceful finish for phones with animations disabled or power saver active
            delay(150)
            safeFinish()
            return@LaunchedEffect
        }

        // Absolute wall-clock fallback on Dispatchers.Default (never blocked by Compose frame clock)
        launch(Dispatchers.Default) {
            delay(750)
            withContext(Dispatchers.Main) {
                safeFinish()
            }
        }

        try {
            // Entry pop
            launch {
                try {
                    logoScale.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
                } catch (_: Throwable) {}
            }
            launch {
                try {
                    contentAlpha.animateTo(1f, tween(240, easing = LinearEasing))
                } catch (_: Throwable) {}
            }

            // Bottom scenery subtle zoom
            launch {
                try {
                    sceneryScale.animateTo(1.03f, tween(700, easing = FastOutSlowInEasing))
                } catch (_: Throwable) {}
            }

            // Step 1: Smooth orbit revolution (0 - 350ms)
            launch {
                try {
                    orbitSpin.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    )
                } catch (_: Throwable) {}
            }

            // Step 2: Line convergence (300ms - 580ms)
            delay(300)
            launch {
                try {
                    orbitTrackAlpha.animateTo(0f, tween(100, easing = LinearEasing))
                } catch (_: Throwable) {}
            }
            launch {
                try {
                    lineProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    )
                } catch (_: Throwable) {}
            }

            // Step 3: Brief traveling pulse (580ms - 700ms)
            delay(200)

            // Step 4: Finish splash
            safeFinish()
        } catch (_: Throwable) {
            safeFinish()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .pointerInput(Unit) {
                // Instant 100% reliable tap-to-skip anywhere on screen
                detectTapGestures(onTap = { safeFinish() })
            }
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Dynamic responsive geometry scaling based on 390dp reference
        val responsiveScale = (screenWidth / 390.dp).coerceIn(0.72f, 1.15f)
        val logoBoxSize = (195.dp * responsiveScale).coerceIn(145.dp, 230.dp)
        val logoImageSize = (180.dp * responsiveScale).coerceIn(135.dp, 210.dp)
        val haloSize = (215.dp * responsiveScale).coerceIn(160.dp, 250.dp)
        val orbitRadiusPx = (112.dp.value * density * responsiveScale).coerceIn(78f * density, 142f * density)
        val lineSpacingPx = (45.dp.value * density * responsiveScale).coerceIn(34f * density, 52f * density)
        val lineYPx = (125.dp.value * density * responsiveScale)
        val iconBadgeSize = (44.dp * responsiveScale).coerceIn(36.dp, 50.dp)
        val iconInnerSize = (22.dp * responsiveScale).coerceIn(18.dp, 26.dp)

        // 1. Bottom Heritage Scenery (Edge-anchored)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1.0f)
                    scaleX = sceneryScale.value
                    scaleY = sceneryScale.value
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_bottom_scenery),
                contentDescription = "Nepal Heritage Scenery",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )

            // Ambient sunlight glow behind mountain peaks
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationY = -30f * density
                    }
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFEDD5).copy(alpha = 0.40f),
                                Color(0xFFFDE68A).copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Mountain cloud layer
            Image(
                painter = painterResource(id = R.drawable.splash_clouds_clean),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = 12f * density
                        translationY = 16f * density
                        alpha = 0.65f
                    }
                    .size(width = 200.dp, height = 70.dp)
            )
        }

        // 2. Center Stage: Logo, Orbiting Icons & Brand Typography
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = contentAlpha.value
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(logoBoxSize * 1.55f)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    },
                contentAlignment = Alignment.Center
            ) {
                // Dotted Orbital Track Rings (fades away smoothly during convergence)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = orbitTrackAlpha.value * 0.75f
                            rotationZ = orbitSpin.value * 0.15f
                        }
                ) {
                    if (orbitTrackAlpha.value > 0.01f) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val rOuter = size.width * 0.42f
                        val rInner = size.width * 0.32f

                        val stroke = Stroke(
                            width = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 9f), 0f)
                        )
                        val ringColor = Color(0xFFFCA5A5).copy(alpha = 0.40f)

                        drawCircle(color = ringColor, radius = rOuter, center = center, style = stroke)
                        drawCircle(color = ringColor.copy(alpha = 0.22f), radius = rInner, center = center, style = stroke)
                    }
                }

                // Center Calendar Logo with Warm Ambient Halo
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(haloSize)
                            .background(haloBrush, shape = CircleShape)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.splash_logo),
                        contentDescription = "NepTools Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(logoImageSize)
                    )
                }

                // 5 Feature Icons: Orbit -> Converge -> Wave
                orbitalItems.forEachIndexed { index, item ->
                    val dist = (pulsePhase - index + 5f) % 5f
                    val waveIntensity = if (dist in 0f..1.4f) {
                        (sin(dist / 1.4f * Math.PI.toFloat())).coerceIn(0f, 1f)
                    } else 0f

                    val dynamicScale = 1.0f + (waveIntensity * 0.20f)
                    val dynamicGlow = waveIntensity

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                val currentAngleDeg = item.initialAngleDeg + orbitSpin.value
                                val rad = Math.toRadians(currentAngleDeg)
                                val orbitX = (orbitRadiusPx * cos(rad)).toFloat()
                                val orbitY = (orbitRadiusPx * sin(rad)).toFloat()

                                val lineX = (index - 2) * lineSpacingPx
                                val lineY = lineYPx

                                val prog = lineProgress.value
                                translationX = orbitX + (lineX - orbitX) * prog
                                translationY = orbitY + (lineY - orbitY) * prog

                                scaleX = dynamicScale
                                scaleY = dynamicScale
                            }
                            .size(iconBadgeSize)
                            .shadow(
                                elevation = (4 + (lineProgress.value * 2) + (dynamicGlow * 7)).dp,
                                shape = CircleShape,
                                spotColor = if (dynamicGlow > 0.05f) item.tintColor else item.tintColor.copy(alpha = 0.35f),
                                ambientColor = if (dynamicGlow > 0.05f) item.tintColor.copy(alpha = 0.45f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                            )
                            .background(
                                Color.White,
                                shape = CircleShape
                            )
                            .background(
                                if (dynamicGlow > 0.05f) {
                                    item.tintColor.copy(alpha = 0.22f + (dynamicGlow * 0.45f))
                                } else {
                                    item.bgColor.copy(alpha = 0.70f)
                                },
                                shape = CircleShape
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = item.tintColor,
                            modifier = Modifier.size(iconInnerSize)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 3. Brand Identity & Offline Guarantee
            Text(
                text = "NepTools",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.1.sp
                ),
                color = Color(0xFF22201B)
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = "नेपाली पात्रो तथा डिजिटल सेवा",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )
        }
    }
}
