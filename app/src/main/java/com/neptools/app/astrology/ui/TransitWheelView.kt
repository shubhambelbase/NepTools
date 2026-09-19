package com.neptools.app.astrology.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.TransitInfo
import com.neptools.app.astrology.vedic.Signs
import com.neptools.app.ui.components.PlanetStyle
import com.neptools.app.ui.icons.PIcons
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun TransitWheelView(
    chart: NatalChart,
    transits: List<TransitInfo>,
    selectedPlanet: Planet?,
    onSelectPlanet: (Planet) -> Unit,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val ringBg = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = Color(0xFFD97706)

    // Pre-calculate aspects for selected planet
    val aspects = remember(selectedPlanet, transits, chart) {
        if (selectedPlanet == null) emptyList()
        else calculateVedicAspects(selectedPlanet, transits, chart)
    }

    val rashiPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }

    val planetPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }

    val degreePaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(transits, chart) {
                    detectTapGestures { offset ->
                        val sizeMin = minOf(size.width, size.height)
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val dist = sqrt(dx * dx + dy * dy)
                        val outerR = sizeMin / 2f

                        // Check if tap was in the outer transit or inner natal rings
                        if (dist in (outerR * 0.35f)..(outerR * 0.98f)) {
                            // Calculate angle in 0..360 sidereal degrees
                            // 0 deg Aries is at top (-90 degrees in standard cartesian)
                            var angDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                            if (angDeg < 0f) angDeg += 360f

                            // Find closest planet (transit or natal)
                            var closestPlanet: Planet? = null
                            var minDiff = 18f

                            for (t in transits) {
                                var diff = kotlin.math.abs(t.siderealLon.toFloat() - angDeg)
                                if (diff > 180f) diff = 360f - diff
                                if (diff < minDiff) {
                                    minDiff = diff
                                    closestPlanet = t.planet
                                }
                            }
                            for ((p, pos) in chart.positions) {
                                var diff = kotlin.math.abs(pos.siderealLon.toFloat() - angDeg)
                                if (diff > 180f) diff = 360f - diff
                                if (diff < minDiff) {
                                    minDiff = diff
                                    closestPlanet = p
                                }
                            }

                            if (closestPlanet != null) {
                                onSelectPlanet(closestPlanet)
                            }
                        }
                    }
                }
        ) {
            val sizeMin = minOf(size.width, size.height)
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = sizeMin / 2f

            val rTransitOuter = outerR * 0.98f
            val rTransitInner = outerR * 0.74f
            val rZodiacInner = outerR * 0.54f
            val rNatalInner = outerR * 0.34f

            // 1. Draw Zodiac Sector Ring (12 Rashis)
            for (i in 0 until 12) {
                val startAng = i * 30f - 90f
                val sweepAng = 30f
                val isEven = i % 2 == 0

                // Background tint for alternate Rashis
                val sectorBg = if (isEven) ringBg else ringBg.copy(alpha = 0.85f)
                drawArc(
                    color = sectorBg,
                    startAngle = startAng,
                    sweepAngle = sweepAng,
                    useCenter = false,
                    topLeft = Offset(cx - rTransitOuter, cy - rTransitOuter),
                    size = androidx.compose.ui.geometry.Size(rTransitOuter * 2, rTransitOuter * 2)
                )

                // Dividing Spoke Lines
                val rad = Math.toRadians((startAng).toDouble())
                val cosA = cos(rad).toFloat()
                val sinA = sin(rad).toFloat()
                drawLine(
                    color = outlineColor,
                    start = Offset(cx + cosA * rNatalInner, cy + sinA * rNatalInner),
                    end = Offset(cx + cosA * rTransitOuter, cy + sinA * rTransitOuter),
                    strokeWidth = 1.2f
                )

                // Zodiac Rashi Name
                val midAng = Math.toRadians((startAng + 15f).toDouble())
                val rLabel = (rTransitInner + rZodiacInner) / 2f
                val lx = cx + cos(midAng).toFloat() * rLabel
                val ly = cy + sin(midAng).toFloat() * rLabel

                rashiPaint.color = onSurface.copy(alpha = 0.75f).toArgbInt()
                rashiPaint.textSize = if (isEn) outerR * 0.040f else outerR * 0.046f
                val rashiLabel = if (isEn) "${Signs.en[i].take(3)} • ${Signs.np[i]}" else Signs.np[i]
                drawContext.canvas.nativeCanvas.drawText(
                    rashiLabel,
                    lx,
                    ly + outerR * 0.015f,
                    rashiPaint
                )
            }

            // Concentric Dividing Rings
            drawCircle(color = outlineColor, radius = rTransitOuter, style = Stroke(width = 1.5f))
            drawCircle(color = outlineColor, radius = rTransitInner, style = Stroke(width = 1.2f))
            drawCircle(color = outlineColor, radius = rZodiacInner, style = Stroke(width = 1.2f))
            drawCircle(color = outlineColor, radius = rNatalInner, style = Stroke(width = 1.5f))

            // 2. Draw Aspect & Conjunction Rays in the Center Hub
            for (aspect in aspects) {
                val tRad = Math.toRadians(aspect.transitLon - 90.0)
                val nRad = Math.toRadians(aspect.natalLon - 90.0)

                val p1 = Offset(cx + cos(tRad).toFloat() * rTransitInner, cy + sin(tRad).toFloat() * rTransitInner)
                val p2 = Offset(cx + cos(nRad).toFloat() * rNatalInner, cy + sin(nRad).toFloat() * rNatalInner)

                val rayColor = if (aspect.isConjunction) Color(0xFFEAB308) else Color(0xFF0284C7)

                drawLine(
                    color = rayColor,
                    start = p1,
                    end = p2,
                    strokeWidth = if (aspect.isConjunction) 2.5f else 1.8f,
                    pathEffect = if (aspect.isConjunction) null else PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                    cap = StrokeCap.Round
                )

                // Connection indicator dots
                drawCircle(color = rayColor, radius = outerR * 0.018f, center = p1)
                drawCircle(color = rayColor, radius = outerR * 0.018f, center = p2)
            }

            // Center Label
            rashiPaint.color = primaryColor.toArgbInt()
            rashiPaint.textSize = outerR * 0.048f
            drawContext.canvas.nativeCanvas.drawText(
                if (isEn) "Transit Wheel" else "सजीव गोचर चक्र",
                cx,
                cy - outerR * 0.02f,
                rashiPaint
            )
            degreePaint.color = onSurface.copy(alpha = 0.55f).toArgbInt()
            degreePaint.textSize = outerR * 0.034f
            drawContext.canvas.nativeCanvas.drawText(
                if (isEn) "Birth ↔ Transit" else "जन्म ↔ गोचर",
                cx,
                cy + outerR * 0.035f,
                degreePaint
            )

            // 3. Draw Inner Ring: Natal Birth Chart Planets
            val rNatalPlanet = (rZodiacInner + rNatalInner) / 2f

            // Natal Lagna (Ascendant) Marker
            val lagnaRad = Math.toRadians(chart.lagnaSiderealLon - 90.0)
            val lagnaX = cx + cos(lagnaRad).toFloat() * rNatalPlanet
            val lagnaY = cy + sin(lagnaRad).toFloat() * rNatalPlanet

            drawCircle(color = goldColor.copy(alpha = 0.25f), radius = outerR * 0.038f, center = Offset(lagnaX, lagnaY))
            drawCircle(color = goldColor, radius = outerR * 0.038f, center = Offset(lagnaX, lagnaY), style = Stroke(width = 1.5f))
            planetPaint.color = goldColor.toArgbInt()
            planetPaint.textSize = outerR * 0.040f
            drawContext.canvas.nativeCanvas.drawText(
                if (isEn) "Asc" else "ल",
                lagnaX,
                lagnaY + outerR * 0.014f,
                planetPaint
            )

            // Natal Planets
            for ((p, pos) in chart.positions) {
                val rad = Math.toRadians(pos.siderealLon - 90.0)
                val px = cx + cos(rad).toFloat() * rNatalPlanet
                val py = cy + sin(rad).toFloat() * rNatalPlanet

                val isSelected = p == selectedPlanet
                val color = PlanetStyle.color(p)

                if (isSelected) {
                    drawCircle(color = color.copy(alpha = 0.35f), radius = outerR * 0.045f, center = Offset(px, py))
                }
                drawCircle(color = color, radius = outerR * 0.035f, center = Offset(px, py))

                planetPaint.color = android.graphics.Color.WHITE
                planetPaint.textSize = outerR * 0.038f
                val symbol = if (isEn) p.name.take(2) else PlanetStyle.nameNp(p).take(1)
                drawContext.canvas.nativeCanvas.drawText(symbol, px, py + outerR * 0.013f, planetPaint)
            }

            // 4. Draw Outer Ring: Live Transit Planets
            val rTransitPlanet = (rTransitOuter + rTransitInner) / 2f

            for (t in transits) {
                val rad = Math.toRadians(t.siderealLon - 90.0)
                val px = cx + cos(rad).toFloat() * rTransitPlanet
                val py = cy + sin(rad).toFloat() * rTransitPlanet

                val isSelected = t.planet == selectedPlanet
                val color = PlanetStyle.color(t.planet)

                // Highlighting glow for selected planet
                if (isSelected) {
                    drawCircle(color = color.copy(alpha = 0.40f), radius = outerR * 0.052f, center = Offset(px, py))
                    drawCircle(color = Color.White, radius = outerR * 0.042f, center = Offset(px, py), style = Stroke(width = 2.0f))
                }

                drawCircle(color = color, radius = outerR * 0.038f, center = Offset(px, py))

                // Planet Name
                planetPaint.color = android.graphics.Color.WHITE
                planetPaint.textSize = outerR * 0.039f
                val symbol = if (isEn) t.planet.name.take(2) else PlanetStyle.nameNp(t.planet).take(1)
                drawContext.canvas.nativeCanvas.drawText(symbol, px, py + outerR * 0.013f, planetPaint)

                // Retrograde "व" or "Rx" Badge
                if (t.retrograde) {
                    val rxOffset = Offset(px + outerR * 0.032f, py - outerR * 0.032f)
                    drawCircle(color = Color(0xFFDC2626), radius = outerR * 0.018f, center = rxOffset)
                    degreePaint.color = android.graphics.Color.WHITE
                    degreePaint.textSize = outerR * 0.024f
                    drawContext.canvas.nativeCanvas.drawText(
                        if (isEn) "R" else "व",
                        rxOffset.x,
                        rxOffset.y + outerR * 0.008f,
                        degreePaint
                    )
                }
            }
        }
    }
}

@Composable
fun TransitPlanetSelectorRow(
    selectedPlanet: Planet?,
    onSelect: (Planet) -> Unit,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (p in Planet.NINE) {
            val isSel = p == selectedPlanet
            val color = PlanetStyle.color(p)

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isSel) color else color.copy(alpha = 0.15f))
                    .border(
                        width = if (isSel) 2.dp else 1.dp,
                        color = if (isSel) color else color.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { onSelect(p) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEn) p.name.take(2) else PlanetStyle.nameNp(p).take(1),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSel) Color.White else color
                )
            }
        }
    }
}

@Composable
fun TransitInspectionCard(
    planet: Planet,
    transit: TransitInfo,
    natalPos: com.neptools.app.astrology.data.PlanetPosition,
    conjunctionPlanet: Planet?,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val planetColor = PlanetStyle.color(planet)
    val transitSign = if (isEn) "${Signs.en[transit.signIndex]} (${Signs.np[transit.signIndex]})" else Signs.np[transit.signIndex]
    val natalSign = if (isEn) "${Signs.en[natalPos.signIndex]} (${Signs.np[natalPos.signIndex]})" else Signs.np[natalPos.signIndex]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(planetColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEn) planet.name.take(2) else PlanetStyle.nameNp(planet).take(1),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isEn) PlanetStyle.nameEn(planet) else PlanetStyle.nameNp(planet),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (transit.retrograde) {
                            if (isEn) "Retrograde (Rx)" else "वक्री चाल"
                        } else {
                            if (isEn) "Direct" else "मार्गी चाल"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (transit.retrograde) Color(0xFFDC2626) else Color(0xFF16A34A)
                        )
                    )
                }
            }

            // Transit Favorability Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (transit.favorable) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (transit.favorable) {
                        if (isEn) "Favorable" else "अनुकूल गोचर"
                    } else {
                        if (isEn) "Caution" else "सावधानी"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (transit.favorable) Color(0xFF15803D) else Color(0xFFB45309)
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Live Transit vs Natal Comparison Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Transit Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp)
            ) {
                Text(
                    text = if (isEn) "Live Transit" else "वर्तमान गोचर स्थिति",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$transitSign ${"%.1f".format(transit.degreeInSign)}°",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEn) "${transit.natalHouseFromMoon}th from Moon" else "चन्द्रबाट ${transit.natalHouseFromMoon} औं घर",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Natal Birth Position Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp)
            ) {
                Text(
                    text = if (isEn) "Natal Birth" else "जन्म कुण्डली स्थिति",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD97706)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$natalSign ${"%.1f".format(natalPos.degreeInSign)}°",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEn) "${natalPos.houseFromLagna}th from Lagna" else "लग्नबाट ${natalPos.houseFromLagna} औं घर",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Conjunctions or Aspects
        if (conjunctionPlanet != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        PIcons.Sparkle,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isEn) {
                            "Conjunction: Transit ${PlanetStyle.nameEn(planet)} conjuncts Natal ${PlanetStyle.nameEn(conjunctionPlanet)} within 8° in $transitSign"
                        } else {
                            "सक्रिय युति: वर्तमान ${PlanetStyle.nameNp(planet)} र जन्म ${PlanetStyle.nameNp(conjunctionPlanet)} एउटै राशिमा (८° भित्र)"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFB45309)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Classical Guidance Note
        Text(
            text = "${if (isEn) "Vedic Guidance (फलकथन)" else "फलकथन तथा दिनचर्या"}: ${if (isEn && transit.noteEn.isNotBlank()) "${transit.noteEn} (${transit.note})" else transit.note}",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class ActiveAspect(
    val transitPlanet: Planet,
    val transitLon: Double,
    val natalPlanet: Planet,
    val natalLon: Double,
    val isConjunction: Boolean,
    val description: String
)

private fun calculateVedicAspects(
    selectedPlanet: Planet,
    transits: List<TransitInfo>,
    chart: NatalChart
): List<ActiveAspect> {
    val list = mutableListOf<ActiveAspect>()
    val currentTransit = transits.firstOrNull { it.planet == selectedPlanet } ?: return list

    val tLon = currentTransit.siderealLon

    for ((np, pos) in chart.positions) {
        val nLon = pos.siderealLon
        var diffDeg = kotlin.math.abs(tLon - nLon)
        if (diffDeg > 180.0) diffDeg = 360.0 - diffDeg

        // 1. Conjunction (युति): within 8 degrees
        if (diffDeg <= 8.0) {
            list.add(
                ActiveAspect(
                    transitPlanet = selectedPlanet,
                    transitLon = tLon,
                    natalPlanet = np,
                    natalLon = nLon,
                    isConjunction = true,
                    description = "युति"
                )
            )
        } else {
            // 2. Full Graha Aspects (पूर्ण दृष्टि)
            // 7th opposition (180 deg) applies to all planets
            val isSeventh = kotlin.math.abs(diffDeg - 180.0) <= 9.0

            // Special Mars aspects: 4th (90 deg), 8th (210 deg)
            val isMarsAspect = selectedPlanet == Planet.MARS &&
                    (kotlin.math.abs(diffDeg - 90.0) <= 9.0 || kotlin.math.abs(diffDeg - 210.0) <= 9.0)

            // Special Jupiter / Rahu / Ketu: 5th (120 deg), 9th (240 deg)
            val isTrikonaAspect = (selectedPlanet == Planet.JUPITER || selectedPlanet == Planet.RAHU || selectedPlanet == Planet.KETU) &&
                    (kotlin.math.abs(diffDeg - 120.0) <= 9.0 || kotlin.math.abs(diffDeg - 240.0) <= 9.0)

            // Special Saturn aspects: 3rd (60 deg), 10th (270 deg)
            val isSaturnAspect = selectedPlanet == Planet.SATURN &&
                    (kotlin.math.abs(diffDeg - 60.0) <= 9.0 || kotlin.math.abs(diffDeg - 270.0) <= 9.0)

            if (isSeventh || isMarsAspect || isTrikonaAspect || isSaturnAspect) {
                list.add(
                    ActiveAspect(
                        transitPlanet = selectedPlanet,
                        transitLon = tLon,
                        natalPlanet = np,
                        natalLon = nLon,
                        isConjunction = false,
                        description = "दृष्टि"
                    )
                )
            }
        }
    }
    return list
}

private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255f).toInt().coerceIn(0, 255),
    (red * 255f).toInt().coerceIn(0, 255),
    (green * 255f).toInt().coerceIn(0, 255),
    (blue * 255f).toInt().coerceIn(0, 255)
)
