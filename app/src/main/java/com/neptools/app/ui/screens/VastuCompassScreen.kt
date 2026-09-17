package com.neptools.app.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.compass.CompassEngine
import com.neptools.app.core.vastu.VastuDirection
import com.neptools.app.core.vastu.VastuEngine
import com.neptools.app.core.vastu.VastuRoomRule
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VastuCompassScreen(
    onBack: () -> Unit,
    onOpenStandardCompass: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    var hasSensor by remember { mutableStateOf(true) }
    var magneticHeading by remember { mutableFloatStateOf(0f) }
    var tilt by remember { mutableFloatStateOf(0f) }
    var unreliable by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedRoomId by remember { mutableStateOf("pooja") }

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

    val currentVastu = remember(magneticHeading) {
        VastuEngine.getVastuDirection(magneticHeading)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
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
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Vastu Shastra Compass" else "वास्तु शास्त्र कम्पास",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isEn) "Vedic Architecture & Room Directions" else "वैदिक दिशा सूचक तथा गृह निर्माण नियम",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onOpenStandardCompass != null) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable(onClick = onOpenStandardCompass)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isEn) "Standard" else "साधारण",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
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
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (isEn) "Magnetometer sensor not detected" else "कम्पास सेन्सर उपलब्ध छैन",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isEn)
                        "Hardware sensor is missing. You can still explore the Vastu House Guide in the second tab."
                    else "यो डिभाइसमा म्याग्नेटोमिटर सेन्सर छैन। दोस्रो ट्याबमा गएर घरको वास्तु नियमहरू अध्ययन गर्न सक्नुहुन्छ।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        if (isEn) "Live Direction (दिशा सूचक)" else "दिशा सूचक",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        if (isEn) "House Guide (वास्तु नियम)" else "वास्तु नियम गाइड",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // Live Vastu Compass Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Vastu Dial
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        VastuDial(
                            headingDeg = magneticHeading,
                            isEn = isEn,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Center Bearing Info
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${magneticHeading.toInt()}°",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) "${currentVastu.nameEn} • ${currentVastu.nameNp}" else currentVastu.nameNp,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (unreliable || tilt > 25f) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (tilt > 25f) {
                                    if (isEn) "Hold phone flat for accurate Vastu reading." else "सटीक वास्तु नापका लागि फोन समतल राख्नुहोस्।"
                                } else {
                                    if (isEn) "Move phone in figure-8 motion to calibrate." else "सेन्सर क्यालिब्रेट गर्न फोनलाई ८ आकारमा घुमाउनुहोस्।"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    // Current Direction Detailed Card
                    VastuDirectionCard(
                        direction = currentVastu,
                        isEn = isEn
                    )
                }

                item {
                    // Quick Room Suitability Checker
                    RoomSuitabilitySection(
                        selectedRoomId = selectedRoomId,
                        onSelectRoom = { selectedRoomId = it },
                        currentHeading = magneticHeading,
                        isEn = isEn
                    )
                }
            }
        } else {
            // Complete House Vastu Rules Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (isEn) "Nepali House Vastu Architecture" else "नेपाली घर निर्माणका मुख्य वास्तु नियम",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isEn)
                            "Standard classical guidelines for placing rooms, water bodies, and utilities."
                        else "कोठा, पानी ट्याङ्की, भान्सा र पूजा कोठाको शास्त्रीय स्थान निर्देशिका।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(VastuEngine.roomRules, key = { it.id }) { rule ->
                    RoomRuleCard(rule = rule, isEn = isEn)
                }
            }
        }
    }
}

@Composable
private fun VastuDial(
    headingDeg: Float,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val dialColor = MaterialTheme.colorScheme.surface
    val ringColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val accentColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface.toArgbInt()
    val northColor = MaterialTheme.colorScheme.error.toArgbInt()

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }

    val sanskritPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }

    Canvas(modifier = modifier) {
        val sizeMin = minOf(size.width, size.height)
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = sizeMin / 2f
        val faceR = outerR * 0.94f

        // Draw dial base
        drawCircle(color = dialColor, radius = faceR)
        drawCircle(
            color = ringColor,
            radius = faceR,
            style = Stroke(width = outerR * 0.014f)
        )
        drawCircle(
            color = ringColor.copy(alpha = 0.18f),
            radius = faceR * 0.64f,
            style = Stroke(width = outerR * 0.008f)
        )

        rotate(degrees = -headingDeg, pivot = Offset(cx, cy)) {
            // Ticks
            var deg = 0f
            while (deg < 360f) {
                val isMajor = deg.toInt() % 45 == 0
                val isMedium = !isMajor && deg.toInt() % 15 == 0
                val len = when {
                    isMajor -> outerR * 0.075f
                    isMedium -> outerR * 0.045f
                    else -> outerR * 0.025f
                }
                val rad = Math.toRadians(deg.toDouble())
                val dirX = sin(rad).toFloat()
                val dirY = -cos(rad).toFloat()
                val fromR = faceR - outerR * 0.02f
                val toR = fromR - len

                drawLine(
                    color = if (isMajor) accentColor else ringColor,
                    start = Offset(cx + dirX * fromR, cy + dirY * fromR),
                    end = Offset(cx + dirX * toR, cy + dirY * toR),
                    strokeWidth = if (isMajor) outerR * 0.012f else outerR * 0.006f,
                    cap = StrokeCap.Round
                )
                deg += 5f
            }

            // Sanskrit & Cardinal Labels
            val vastuLabels = if (isEn) listOf(
                Pair("N • उत्तर", 0),
                Pair("NE • ईशान", 45),
                Pair("E • पूर्व", 90),
                Pair("SE • आग्नेय", 135),
                Pair("S • दक्षिण", 180),
                Pair("SW • नैऋत्य", 225),
                Pair("W • पश्चिम", 270),
                Pair("NW • वायव्य", 315)
            ) else listOf(
                Pair("उत्तर (N)", 0),
                Pair("ईशान (NE)", 45),
                Pair("पूर्व (E)", 90),
                Pair("आग्नेय (SE)", 135),
                Pair("दक्षिण (S)", 180),
                Pair("नैऋत्य (SW)", 225),
                Pair("पश्चिम (W)", 270),
                Pair("वायव्य (NW)", 315)
            )

            for ((label, ang) in vastuLabels) {
                val rad = Math.toRadians(ang.toDouble())
                val dirX = sin(rad).toFloat()
                val dirY = -cos(rad).toFloat()
                val labelR = faceR - outerR * 0.145f

                sanskritPaint.color = if (ang == 0) northColor else textColor
                sanskritPaint.textSize = outerR * 0.055f

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    cx + dirX * labelR,
                    cy + dirY * labelR + outerR * 0.018f,
                    sanskritPaint
                )
            }

            // North Pointer Line
            drawLine(
                color = Color(0xFFDC2626),
                start = Offset(cx, cy - faceR * 0.62f),
                end = Offset(cx, cy - faceR * 0.20f),
                strokeWidth = outerR * 0.012f,
                cap = StrokeCap.Round
            )
        }

        // Top Lubber Indicator
        val lubX = cx
        val lubTipY = cy - faceR - outerR * 0.005f
        val lubHalfW = outerR * 0.045f
        val lubBaseY = lubTipY + outerR * 0.075f
        val lubber = Path().apply {
            moveTo(lubX, lubTipY)
            lineTo(lubX - lubHalfW, lubBaseY)
            lineTo(lubX + lubHalfW, lubBaseY)
            close()
        }
        drawPath(lubber, color = accentColor)
    }
}

@Composable
private fun VastuDirectionCard(
    direction: VastuDirection,
    isEn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = if (isEn) "${direction.nameEn} • ${direction.nameNp}" else direction.nameNp,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${if (isEn) "Deity (अधिपति)" else "अधिपति"}: ${if (isEn) "${direction.deityEn} • ${direction.deityNp}" else direction.deityNp}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isEn) "${direction.elementEn} • ${direction.elementNp}" else direction.elementNp,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Best Rooms
        Text(
            text = if (isEn) "Auspicious Placements (शुभ निर्माण):" else "शुभ तथा उत्तम निर्माण:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF16A34A)
        )
        Spacer(Modifier.height(4.dp))
        val bestList = if (isEn) direction.bestRoomsEn.mapIndexed { idx, en ->
            val np = direction.bestRoomsNp.getOrNull(idx)
            if (np != null) "$en ($np)" else en
        } else direction.bestRoomsNp
        for (item in bestList) {
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    PIcons.Check,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Avoid Rooms
        Text(
            text = if (isEn) "Strictly Avoid (वर्जित दोष):" else "वर्जित तथा नराख्नुपर्ने (दोष):",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(4.dp))
        val avoidList = if (isEn) direction.avoidRoomsEn.mapIndexed { idx, en ->
            val np = direction.avoidRoomsNp.getOrNull(idx)
            if (np != null) "$en ($np)" else en
        } else direction.avoidRoomsNp
        for (item in avoidList) {
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    PIcons.Cross,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Advice Note
        Text(
            text = if (isEn) direction.adviceEn else direction.adviceNp,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RoomSuitabilitySection(
    selectedRoomId: String,
    onSelectRoom: (String) -> Unit,
    currentHeading: Float,
    isEn: Boolean
) {
    val currentVastu = VastuEngine.getVastuDirection(currentHeading)
    val roomRules = VastuEngine.roomRules
    val activeRule = roomRules.firstOrNull { it.id == selectedRoomId } ?: roomRules[0]

    // Determine status
    val isIdeal = currentVastu.bestRoomsEn.any { it.contains(activeRule.roomEn, ignoreCase = true) } ||
            currentVastu.bestRoomsNp.any { it.contains(activeRule.roomNp, ignoreCase = true) }
    val isForbidden = currentVastu.avoidRoomsEn.any { it.contains(activeRule.roomEn, ignoreCase = true) } ||
            currentVastu.avoidRoomsNp.any { it.contains(activeRule.roomNp, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (isEn) "Quick Room Suitability Verifier (वास्तु अनुकूलता)" else "कोठा अनुसार उपयुक्त दिशा जाँच",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        // Quick Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(roomRules, key = { it.id }) { rule ->
                val isSelected = rule.id == selectedRoomId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { onSelectRoom(rule.id) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (isEn) "${rule.roomEn} (${rule.roomNp})" else rule.roomNp,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Verdict Banner
        val (verdictTitle, verdictDesc, bannerBg, bannerColor) = when {
            isIdeal -> Quadruple(
                if (isEn) "Ideal & Auspicious (सर्वोत्तम तथा शुभ)" else "सर्वोत्तम तथा शुभ स्थान",
                if (isEn) "This direction is classically recommended for ${activeRule.roomEn} (${activeRule.roomNp})."
                else "यो दिशा ${activeRule.roomNp} को लागि वास्तु अनुसार सर्वोत्तम मानिन्छ।",
                Color(0xFFDCFCE7),
                Color(0xFF15803D)
            )
            isForbidden -> Quadruple(
                if (isEn) "Inauspicious / Avoid (वर्जित तथा अशुभ)" else "वर्जित तथा अशुभ स्थान",
                if (isEn) "Placing ${activeRule.roomEn} (${activeRule.roomNp}) here creates serious Vastu defect."
                else "यस दिशामा ${activeRule.roomNp} राख्दा ठूलो वास्तु दोष उत्पन्न हुन सक्छ।",
                Color(0xFFFEE2E2),
                Color(0xFFB91C1C)
            )
            else -> Quadruple(
                if (isEn) "Secondary / Neutral (मध्यम वा वैकल्पिक)" else "मध्यम वा वैकल्पिक स्थान",
                if (isEn) "Acceptable if the ideal direction (${activeRule.bestDirectionEn} • ${activeRule.bestDirectionNp}) is unavailable."
                else "उत्कृष्ट दिशा (${activeRule.bestDirectionNp}) उपलब्ध नभएमा स्वीकार्य मानिन्छ।",
                Color(0xFFFEF3C7),
                Color(0xFFB45309)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bannerBg)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = verdictTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = bannerColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = verdictDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = bannerColor
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "${if (isEn) "Ideal Zone (सर्वोत्तम दिशा)" else "सर्वोत्तम दिशा"}: ${if (isEn) "${activeRule.bestDirectionEn} • ${activeRule.bestDirectionNp}" else activeRule.bestDirectionNp}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${if (isEn) "Alternative (वैकल्पिक दिशा)" else "वैकल्पिक दिशा"}: ${if (isEn) "${activeRule.alternativeDirectionEn} • ${activeRule.alternativeDirectionNp}" else activeRule.alternativeDirectionNp}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RoomRuleCard(
    rule: VastuRoomRule,
    isEn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = if (isEn) "${rule.roomEn} (${rule.roomNp})" else rule.roomNp,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isEn) "Best (उत्तम):" else "उत्तम:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF16A34A)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isEn) "${rule.bestDirectionEn} • ${rule.bestDirectionNp}" else rule.bestDirectionNp,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isEn) "Alternative (विकल्प):" else "विकल्प:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isEn) "${rule.alternativeDirectionEn} • ${rule.alternativeDirectionNp}" else rule.alternativeDirectionNp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = if (isEn) "Avoid (वर्जित):" else "वर्जित:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isEn) "${rule.strictlyAvoidEn} • ${rule.strictlyAvoidNp}" else rule.strictlyAvoidNp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isEn) rule.guidelineEn else rule.guidelineNp,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255f).toInt().coerceIn(0, 255),
    (red * 255f).toInt().coerceIn(0, 255),
    (green * 255f).toInt().coerceIn(0, 255),
    (blue * 255f).toInt().coerceIn(0, 255)
)
