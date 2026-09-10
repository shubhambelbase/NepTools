package com.neptools.app.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

data class DialColors(
    val face: Color,
    val tickMinor: Color,
    val tickMedium: Color,
    val tickMajor: Color,
    val label: Color,
    val cardinal: Color,
    val north: Color,
    val ring: Color,
    val innerRing: Color,
    val accent: Color,
    val needleNorth: Color,
    val needleSouth: Color,
    val hub: Color
)

@Composable
fun rememberDefaultDialColors(
    face: Color,
    label: Color,
    cardinal: Color,
    accent: Color
): DialColors {
    val tickDim = label.copy(alpha = 0.55f)
    val tickMid = label.copy(alpha = 0.75f)
    return remember(face, label, cardinal, accent) {
        DialColors(
            face = face,
            tickMinor = tickDim,
            tickMedium = tickMid,
            tickMajor = label,
            label = label.copy(alpha = 0.85f),
            cardinal = cardinal,
            north = accent,
            ring = label.copy(alpha = 0.35f),
            innerRing = label.copy(alpha = 0.22f),
            accent = accent,
            needleNorth = accent,
            needleSouth = label.copy(alpha = 0.65f),
            hub = cardinal
        )
    }
}

@Composable
fun CompassDial(
    headingDeg: Float,
    colors: DialColors,
    modifier: Modifier = Modifier
) {
    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }
    val smallPaint = remember(textPaint) {
        Paint(textPaint).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
    }

    Canvas(modifier = modifier) {
        val sizeMin = minOf(size.width, size.height)
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = sizeMin / 2f
        val faceR = outerR * 0.94f
        val minorLen = outerR * 0.030f
        val mediumLen = outerR * 0.055f
        val majorLen = outerR * 0.085f

        drawCircle(color = colors.face, radius = faceR)
        drawCircle(
            color = colors.ring,
            radius = faceR,
            style = Stroke(width = outerR * 0.012f)
        )
        drawCircle(
            color = colors.innerRing,
            radius = faceR * 0.62f,
            style = Stroke(width = outerR * 0.006f)
        )

        rotate(degrees = -headingDeg, pivot = Offset(cx, cy)) {
            var deg = 0f
            while (deg < 360f) {
                val isMajor = deg.toInt() % 30 == 0
                val isMedium = !isMajor && deg.toInt() % 10 == 0
                val len = when {
                    isMajor -> majorLen
                    isMedium -> mediumLen
                    else -> minorLen
                }
                val color = when {
                    isMajor -> colors.tickMajor
                    isMedium -> colors.tickMedium
                    else -> colors.tickMinor
                }
                val rad = Math.toRadians(deg.toDouble())
                val dirX = sin(rad).toFloat()
                val dirY = -cos(rad).toFloat()
                val fromR = faceR - outerR * 0.018f
                val toR = fromR - len
                drawLine(
                    color = color,
                    start = Offset(cx + dirX * fromR, cy + dirY * fromR),
                    end = Offset(cx + dirX * toR, cy + dirY * toR),
                    strokeWidth = if (isMajor) outerR * 0.012f else outerR * 0.006f,
                    cap = StrokeCap.Round
                )
                deg += 2f
            }

            for (deg in 0 until 360 step 30) {
                val rad = Math.toRadians(deg.toDouble())
                val dirX = sin(rad).toFloat()
                val dirY = -cos(rad).toFloat()
                val labelR = faceR - majorLen - outerR * 0.055f
                textPaint.color = colors.label.toArgbInt()
                textPaint.textSize = outerR * 0.058f
                drawContext.canvas.nativeCanvas.drawText(
                    deg.toString(),
                    cx + dirX * labelR,
                    cy + dirY * labelR + outerR * 0.02f,
                    textPaint
                )
            }

            val cardinals = listOf("N" to 0, "E" to 90, "S" to 180, "W" to 270)
            for ((letter, deg) in cardinals) {
                val rad = Math.toRadians(deg.toDouble())
                val dirX = sin(rad).toFloat()
                val dirY = -cos(rad).toFloat()
                val labelR = faceR - majorLen - outerR * 0.135f
                textPaint.color = if (letter == "N") colors.north.toArgbInt() else colors.cardinal.toArgbInt()
                textPaint.textSize = outerR * 0.098f
                drawContext.canvas.nativeCanvas.drawText(
                    letter,
                    cx + dirX * labelR,
                    cy + dirY * labelR + outerR * 0.035f,
                    textPaint
                )
            }

            drawLine(
                color = colors.north.copy(alpha = 0.9f),
                start = Offset(cx, cy - faceR * 0.60f),
                end = Offset(cx, cy - faceR * 0.16f),
                strokeWidth = outerR * 0.014f,
                cap = StrokeCap.Round
            )
        }

        val lubX = cx
        val lubTipY = cy - faceR - outerR * 0.005f
        val lubHalfW = outerR * 0.045f
        val lubBaseY = lubTipY + outerR * 0.075f
        val lubber = androidx.compose.ui.graphics.Path().apply {
            moveTo(lubX, lubTipY)
            lineTo(lubX - lubHalfW, lubBaseY)
            lineTo(lubX + lubHalfW, lubBaseY)
            close()
        }
        drawPath(lubber, color = colors.accent)

        drawCircle(color = colors.hub, radius = outerR * 0.022f)
    }
}

private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255f).toInt().coerceIn(0, 255),
    (red * 255f).toInt().coerceIn(0, 255),
    (green * 255f).toInt().coerceIn(0, 255),
    (blue * 255f).toInt().coerceIn(0, 255)
)
