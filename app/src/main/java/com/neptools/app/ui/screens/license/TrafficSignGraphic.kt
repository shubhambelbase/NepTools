package com.neptools.app.ui.screens.license

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neptools.app.core.data.license.SignCategory
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.neptools.app.R

val OfficialSignDrawables: Map<String, Int> = mapOf(
    // MANDATORY SIGNS (Official Nepal DoTM Chart)
    "m_stop" to R.drawable.dotm_mand_01,
    "m_stop_give_way" to R.drawable.dotm_mand_01,
    "m_no_right_turn" to R.drawable.dotm_mand_02,
    "m_no_entry" to R.drawable.dotm_mand_03,
    "m_weight_17t" to R.drawable.dotm_mand_04,
    "m_weight_limit" to R.drawable.dotm_mand_04,
    "m_speed_40" to R.drawable.dotm_mand_05,
    "m_no_left_turn" to R.drawable.dotm_mand_06,
    "m_height_4_4m" to R.drawable.dotm_mand_07,
    "m_height_limit" to R.drawable.dotm_mand_07,
    "m_no_parking" to R.drawable.dotm_mand_08,
    "m_stop_look_go" to R.drawable.dotm_mand_09,
    "m_no_overtaking" to R.drawable.dotm_mand_10,
    "m_axle_4t" to R.drawable.dotm_mand_11,
    "m_axle_limit" to R.drawable.dotm_mand_11,
    "m_length_10m" to R.drawable.dotm_mand_12,
    "m_length_limit" to R.drawable.dotm_mand_12,
    "m_no_truck" to R.drawable.dotm_mand_13,
    "m_no_u_turn" to R.drawable.dotm_mand_14,
    "m_no_stopping" to R.drawable.dotm_mand_15,
    "m_no_motor_vehicles" to R.drawable.dotm_mand_16,
    "m_no_vehicles" to R.drawable.dotm_mand_16,
    "m_end_speed_40" to R.drawable.dotm_mand_17,
    "m_ahead_turn_left" to R.drawable.dotm_mand_18,
    "m_ahead_only" to R.drawable.dotm_mand_19,
    "m_go_temp" to R.drawable.dotm_mand_20,
    "m_one_way_mand" to R.drawable.dotm_mand_21,
    "m_turn_left_only" to R.drawable.dotm_mand_22,
    "m_end_restriction" to R.drawable.dotm_mand_23,
    "m_stop_temp" to R.drawable.dotm_mand_24,
    "m_pass_either_side" to R.drawable.dotm_mand_25,
    "m_give_way" to R.drawable.dotm_mand_26,
    "m_roundabout_compulsory" to R.drawable.dotm_mand_27,
    "m_keep_left" to R.drawable.dotm_mand_28,

    // WARNING SIGNS (Official Nepal DoTM Chart)
    "w_cross_road_minor" to R.drawable.dotm_warn_01,
    "w_cross_road_major" to R.drawable.dotm_warn_02,
    "w_cross_road" to R.drawable.dotm_warn_02,
    "w_side_road_right" to R.drawable.dotm_warn_03,
    "w_pedestrian_crossing" to R.drawable.dotm_warn_04,
    "w_roundabout_ahead" to R.drawable.dotm_warn_05,
    "w_sharp_right" to R.drawable.dotm_warn_06,
    "w_hairpin_right" to R.drawable.dotm_warn_07,
    "w_double_bend_left" to R.drawable.dotm_warn_08,
    "w_narrow_road" to R.drawable.dotm_warn_09,
    "w_narrow_road_right" to R.drawable.dotm_warn_10,
    "w_checkpost" to R.drawable.dotm_warn_11,
    "w_barrier_ahead" to R.drawable.dotm_warn_11,
    "w_narrow_bridge" to R.drawable.dotm_warn_12,
    "w_two_way_cross" to R.drawable.dotm_warn_13,
    "w_two_way_straight" to R.drawable.dotm_warn_14,
    "w_height_limit_warn" to R.drawable.dotm_warn_15,
    "w_steep_descent" to R.drawable.dotm_warn_16,
    "w_dual_carriageway_end" to R.drawable.dotm_warn_17,
    "w_cattle_crossing" to R.drawable.dotm_warn_18,
    "w_pedestrians_ahead" to R.drawable.dotm_warn_19,
    "w_school_ahead" to R.drawable.dotm_warn_19,
    "w_low_flying_aircraft" to R.drawable.dotm_warn_20,
    "w_t_junction" to R.drawable.dotm_warn_21,
    "w_y_junction" to R.drawable.dotm_warn_22,
    "w_side_road_merge_right" to R.drawable.dotm_warn_23,
    "w_side_road_merge_left" to R.drawable.dotm_warn_24,
    "w_side_road_left" to R.drawable.dotm_warn_24,
    "w_dip_causeway" to R.drawable.dotm_warn_25,
    "w_rough_road" to R.drawable.dotm_warn_25,
    "w_traffic_signals" to R.drawable.dotm_warn_26,
    "w_speed_breaker" to R.drawable.dotm_warn_27,
    "w_unguarded_rail" to R.drawable.dotm_warn_28,
    "w_steep_ascent" to R.drawable.dotm_warn_29,
    "w_loose_gravel" to R.drawable.dotm_warn_30,
    "w_river_bank" to R.drawable.dotm_warn_31,
    "w_staggered_junction" to R.drawable.dotm_warn_32,
    "w_slippery_road" to R.drawable.dotm_warn_33,
    "w_danger" to R.drawable.dotm_warn_34,
    "w_hazard_marker_left" to R.drawable.dotm_warn_35,
    "w_diversion_arrow" to R.drawable.dotm_warn_37,
    "w_diversion_ahead" to R.drawable.dotm_warn_38,
    "w_chevron_t_junction" to R.drawable.dotm_warn_39,
    "w_chevron_sharp_bend" to R.drawable.dotm_warn_40,

    // INFORMATORY SIGNS (Official Nepal DoTM Chart)
    "i_dead_end" to R.drawable.dotm_info_01,
    "i_pedestrian_cross_info" to R.drawable.dotm_info_02,
    "i_pedestrian_path" to R.drawable.dotm_info_02,
    "i_parking_all" to R.drawable.dotm_info_03,
    "i_passing_bay" to R.drawable.dotm_info_04,
    "i_telephone" to R.drawable.dotm_info_05,
    "i_workshop" to R.drawable.dotm_info_06,
    "i_petrol_pump" to R.drawable.dotm_info_07,
    "i_resting_place" to R.drawable.dotm_info_08,
    "i_restaurant" to R.drawable.dotm_info_09,
    "i_refreshment" to R.drawable.dotm_info_10,
    "i_hospital" to R.drawable.dotm_info_11,
    "i_cycle_track" to R.drawable.dotm_info_12,
    "i_picnic_spot" to R.drawable.dotm_info_13,
    "i_pedestrian_way" to R.drawable.dotm_info_14,
    "i_pedestrian_only" to R.drawable.dotm_info_14,
    "i_pedestrian_cycle_track" to R.drawable.dotm_info_15,
    "i_pedestrian_cycle" to R.drawable.dotm_info_15,
    "i_bus_stop" to R.drawable.dotm_info_16,
    "i_one_way_info" to R.drawable.dotm_info_17,
    "i_one_way" to R.drawable.dotm_info_17,
    "i_taxi_park" to R.drawable.dotm_info_18,
    "i_parking_taxi" to R.drawable.dotm_info_18,
    "i_place_name_duhabi" to R.drawable.dotm_info_19,
    "i_place_name" to R.drawable.dotm_info_19
)

/**
 * Official Nepal DoTM Traffic Signs & Markings Engine.
 * Renders official high-resolution DoTM chart imagery with seamless native vector fallback.
 */
@Composable
fun TrafficSignGraphic(
    signKey: String,
    category: SignCategory,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    drawableRes: Int? = null
) {
    val resId = drawableRes ?: OfficialSignDrawables[signKey]

    Box(
        modifier = modifier
            .size(size)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        if (resId != null) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            val resolvedCategory = when {
                signKey.startsWith("m_") -> SignCategory.MANDATORY
                signKey.startsWith("w_") || signKey.startsWith("c_") -> SignCategory.CAUTIONARY
                signKey.startsWith("i_") -> SignCategory.INFORMATORY
                signKey.startsWith("r_") -> SignCategory.ROAD_MARKING
                signKey.startsWith("p_") || signKey.startsWith("tl_") || signKey.startsWith("l_") -> SignCategory.TRAFFIC_LIGHT
                else -> category
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                when (resolvedCategory) {
                    SignCategory.MANDATORY -> drawMandatorySign(signKey)
                    SignCategory.CAUTIONARY -> drawCautionarySign(signKey)
                    SignCategory.INFORMATORY -> drawInformatorySign(signKey)
                    SignCategory.ROAD_MARKING -> drawRoadMarkingSign(signKey)
                    SignCategory.TRAFFIC_LIGHT -> drawTrafficLightSign(signKey)
                }
            }
        }
    }
}

// =============================================================================
// 1. MANDATORY / REGULATORY SIGNS (DoTM Standard)
// =============================================================================
private fun DrawScope.drawMandatorySign(signKey: String) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)
    val radius = w * 0.48f

    val redColor = Color(0xFFDC2626)
    val blueColor = Color(0xFF0284C7)
    val whiteColor = Color(0xFFFFFFFF)
    val darkColor = Color(0xFF0F172A)
    val borderThick = w * 0.11f

    when (signKey) {
        "m_stop" -> {
            // Octagonal STOP sign
            val path = Path()
            val r = w * 0.48f
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0) + 22.5)
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = redColor, style = Fill)
            // White inset border
            val insetPath = Path()
            val inR = r * 0.88f
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0) + 22.5)
                val x = center.x + (inR * cos(angle)).toFloat()
                val y = center.y + (inR * sin(angle)).toFloat()
                if (i == 0) insetPath.moveTo(x, y) else insetPath.lineTo(x, y)
            }
            insetPath.close()
            drawPath(insetPath, color = whiteColor, style = Stroke(width = w * 0.035f))

            // Pristine text "STOP"
            drawNativeText("STOP", center, w * 0.28f, android.graphics.Color.WHITE, isBold = true)
        }

        "m_give_way" -> {
            // Inverted triangle
            val path = Path().apply {
                moveTo(w * 0.06f, h * 0.10f)
                lineTo(w * 0.94f, h * 0.10f)
                lineTo(w * 0.50f, h * 0.94f)
                close()
            }
            drawPath(path, color = whiteColor, style = Fill)
            drawPath(
                path,
                color = redColor,
                style = Stroke(width = borderThick, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        "m_no_entry" -> {
            drawCircle(color = redColor, radius = radius, center = center)
            val barW = w * 0.70f
            val barH = h * 0.20f
            drawRoundRect(
                color = whiteColor,
                topLeft = Offset(center.x - barW / 2f, center.y - barH / 2f),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barH / 4f, barH / 4f)
            )
        }

        "m_no_stopping" -> {
            // Clearway: Blue circle + Red outer ring + Red X
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = redColor, radius = radius - borderThick / 2f, center = center, style = Stroke(width = borderThick))
            val pad = w * 0.20f
            drawLine(color = redColor, start = Offset(pad, pad), end = Offset(w - pad, h - pad), strokeWidth = borderThick * 0.85f, cap = StrokeCap.Round)
            drawLine(color = redColor, start = Offset(w - pad, pad), end = Offset(pad, h - pad), strokeWidth = borderThick * 0.85f, cap = StrokeCap.Round)
        }

        "m_no_parking" -> {
            // Blue circle + Red ring + White P + Red slash
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = redColor, radius = radius - borderThick / 2f, center = center, style = Stroke(width = borderThick))
            drawNativeText("P", Offset(center.x, center.y - w * 0.02f), w * 0.44f, android.graphics.Color.WHITE, isBold = true)
            val pad = w * 0.18f
            drawLine(color = redColor, start = Offset(pad, pad), end = Offset(w - pad, h - pad), strokeWidth = borderThick * 0.85f, cap = StrokeCap.Round)
        }

        // Compulsory Blue Discs (Ahead, Left, Right, Roundabout, etc.)
        "m_ahead_only" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawArrowUp(center, w, whiteColor)
        }

        "m_turn_left_only" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawArrowLeft90(center, w, whiteColor)
        }

        "m_turn_right_only" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawArrowRight90(center, w, whiteColor)
        }

        "m_ahead_or_left" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawArrowAheadOrLeft(center, w, whiteColor)
        }

        "m_ahead_or_right" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawArrowAheadOrRight(center, w, whiteColor)
        }

        "m_keep_left" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawArrowKeepLeft(center, w, whiteColor)
        }

        "m_keep_right" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawArrowKeepRight(center, w, whiteColor)
        }

        "m_roundabout_compulsory" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawRoundaboutArrows(center, w, whiteColor)
        }

        "m_sound_horn" -> {
            drawCircle(color = blueColor, radius = radius, center = center)
            drawCircle(color = whiteColor, radius = radius * 0.96f, center = center, style = Stroke(width = 2f))
            drawHornSymbol(center, w, whiteColor)
        }

        "m_end_restriction" -> {
            drawCircle(color = whiteColor, radius = radius, center = center)
            drawCircle(color = darkColor, radius = radius - borderThick / 3f, center = center, style = Stroke(width = borderThick / 3f))
            val pad = w * 0.20f
            for (off in listOf(-w * 0.10f, -w * 0.03f, w * 0.03f, w * 0.10f)) {
                drawLine(
                    color = darkColor,
                    start = Offset(pad + off, h - pad + off),
                    end = Offset(w - pad + off, pad + off),
                    strokeWidth = w * 0.035f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Speed Limits (20, 30, 40, 50, 60, 80)
        "m_speed_20", "m_speed_30", "m_speed_40", "m_speed_50", "m_speed_60", "m_speed_80" -> {
            drawCircle(color = whiteColor, radius = radius, center = center)
            drawCircle(color = redColor, radius = radius - borderThick / 2f, center = center, style = Stroke(width = borderThick))
            val speedNum = signKey.substringAfter("m_speed_")
            drawNativeText(speedNum, center, w * 0.38f, android.graphics.Color.parseColor("#0F172A"), isBold = true)
        }

        // Prohibitions with Diagonal Red Slash
        else -> {
            drawCircle(color = whiteColor, radius = radius, center = center)
            drawCircle(color = redColor, radius = radius - borderThick / 2f, center = center, style = Stroke(width = borderThick))

            // Inner Glyph
            when (signKey) {
                "m_no_right_turn" -> drawArrowRight90(center, w * 0.85f, darkColor)
                "m_no_left_turn" -> drawArrowLeft90(center, w * 0.85f, darkColor)
                "m_no_u_turn" -> drawArrowUTurn(center, w * 0.85f, darkColor)
                "m_no_overtaking" -> drawOvertakingGlyph(center, w)
                "m_no_horn" -> drawHornSymbol(center, w, darkColor)
                "m_no_pedestrian" -> drawPedestrianGlyph(center, w * 0.85f, darkColor)
                "m_no_bicycle" -> drawBicycleGlyph(center, w * 0.85f, darkColor)
                "m_no_motorcycle" -> drawMotorcycleGlyph(center, w * 0.85f, darkColor)
                "m_no_truck" -> drawTruckGlyph(center, w * 0.85f, darkColor)
                "m_no_vehicles" -> drawCarFrontGlyph(center, w * 0.85f, darkColor)
                "m_width_limit" -> drawDimensionSign(center, w, "2.5m", isHorizontal = true)
                "m_height_limit" -> drawDimensionSign(center, w, "3.5m", isHorizontal = false)
                "m_weight_limit" -> drawWeightSign(center, w, "5 T")
                "m_axle_limit" -> drawWeightSign(center, w, "4 T")
                "m_length_limit" -> drawDimensionSign(center, w, "10m", isHorizontal = true)
                else -> drawGenericProhibition(center, w)
            }

            // Diagonal Red Slash Crossing
            val pad = w * 0.16f
            drawLine(
                color = redColor,
                start = Offset(pad, pad),
                end = Offset(w - pad, h - pad),
                strokeWidth = borderThick * 0.85f,
                cap = StrokeCap.Round
            )
        }
    }
}

// =============================================================================
// 2. CAUTIONARY / WARNING SIGNS (Equilateral Triangle - DoTM Standard)
// =============================================================================
private fun DrawScope.drawCautionarySign(signKey: String) {
    val w = size.width
    val h = size.height
    // Centroid of warning triangle is at roughly 60% of total height
    val center = Offset(w / 2f, h * 0.60f)

    val redColor = Color(0xFFDC2626)
    val bgYellow = Color(0xFFFFFBEB)
    val darkColor = Color(0xFF0F172A)
    val borderThick = w * 0.10f

    // Equilateral Warning Triangle
    val path = Path().apply {
        moveTo(w * 0.50f, h * 0.08f)
        lineTo(w * 0.94f, h * 0.90f)
        lineTo(w * 0.06f, h * 0.90f)
        close()
    }
    drawPath(path, color = bgYellow, style = Fill)
    drawPath(path, color = redColor, style = Stroke(width = borderThick, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Inside Warning Glyph
    when (signKey) {
        "w_sharp_left" -> drawCurveArrow(center, w, isLeft = true, isSharp = true)
        "w_sharp_right" -> drawCurveArrow(center, w, isLeft = false, isSharp = true)
        "w_hairpin_left" -> drawHairpinCurve(center, w, isLeft = true)
        "w_hairpin_right" -> drawHairpinCurve(center, w, isLeft = false)
        "w_double_bend_left" -> drawDoubleBend(center, w, firstLeft = true)
        "w_double_bend_right" -> drawDoubleBend(center, w, firstLeft = false)
        "w_steep_ascent" -> drawSlopeHill(center, w, isAscent = true)
        "w_steep_descent" -> drawSlopeHill(center, w, isAscent = false)
        "w_narrow_road" -> drawNarrowRoad(center, w)
        "w_road_widens" -> drawRoadWidens(center, w)
        "w_narrow_bridge" -> drawNarrowBridge(center, w)
        "w_slippery_road" -> drawSlipperyCar(center, w)
        "w_loose_gravel" -> drawLooseGravel(center, w)
        "w_pedestrian_crossing" -> drawPedestrianCrossingWarning(center, w)
        "w_school_ahead" -> drawSchoolChildren(center, w)
        "w_cycle_crossing" -> drawBicycleGlyph(center, w * 0.85f, darkColor)
        "w_cattle_crossing" -> drawCattleSilhouette(center, w)
        "w_road_work" -> drawWorkerWithShovel(center, w)
        "w_traffic_signals" -> drawMiniTrafficLight(center, w)
        "w_roundabout_ahead" -> drawRoundaboutArrows(center, w * 0.75f, darkColor)
        "w_cross_road" -> drawIntersectionPlus(center, w)
        "w_t_junction" -> drawIntersectionT(center, w)
        "w_y_junction" -> drawIntersectionY(center, w)
        "w_side_road_left" -> drawSideRoad(center, w, isLeft = true)
        "w_side_road_right" -> drawSideRoad(center, w, isLeft = false)
        "w_staggered_junction" -> drawStaggeredJunction(center, w)
        "w_speed_breaker" -> drawSpeedHump(center, w)
        "w_rough_road" -> drawRoughRoad(center, w)
        "w_falling_rocks" -> drawFallingRocks(center, w)
        "w_river_bank" -> drawRiverBank(center, w)
        "w_barrier_ahead" -> drawRailwayBarrier(center, w)
        "w_unguarded_rail" -> drawTrainEngine(center, w)
        "w_guarded_rail" -> drawFencePicket(center, w)
        "w_gap_in_median" -> drawMedianGap(center, w)
        else -> drawGenericWarning(center, w)
    }
}

// =============================================================================
// 3. INFORMATORY SIGNS (Blue & Green Rounded Placards)
// =============================================================================
private fun DrawScope.drawInformatorySign(signKey: String) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    val blueBg = Color(0xFF0284C7)
    val whiteColor = Color(0xFFFFFFFF)
    val greenBg = Color(0xFF059669)

    val isGreen = signKey.startsWith("i_dispensary") || signKey.startsWith("i_highway")
    val bgColor = if (isGreen) greenBg else blueBg

    drawRoundRect(
        color = bgColor,
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = whiteColor,
        topLeft = Offset(w * 0.07f, h * 0.07f),
        size = Size(w * 0.86f, h * 0.86f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 2.5f)
    )

    when (signKey) {
        "i_hospital" -> drawNativeText("H", center, w * 0.44f, android.graphics.Color.WHITE, isBold = true)
        "i_first_aid" -> drawCrossPlus(center, w, Color(0xFFDC2626), inWhiteBox = true)
        "i_dispensary" -> drawCrossPlus(center, w, whiteColor, inWhiteBox = false)
        "i_petrol_pump" -> drawFuelPump(center, w, whiteColor)
        "i_telephone" -> drawTelephone(center, w, whiteColor)
        "i_restaurant" -> drawForkAndKnife(center, w, whiteColor)
        "i_refreshment" -> drawCoffeeCup(center, w, whiteColor)
        "i_resting_place" -> drawBedRest(center, w, whiteColor)
        "i_public_toilet" -> drawNativeText("WC", center, w * 0.36f, android.graphics.Color.WHITE, isBold = true)
        "i_parking_all" -> drawNativeText("P", center, w * 0.48f, android.graphics.Color.WHITE, isBold = true)
        "i_parking_bike" -> drawParkingWithSub(center, w, "BIKE")
        "i_parking_car" -> drawParkingWithSub(center, w, "CAR")
        "i_parking_taxi" -> drawParkingWithSub(center, w, "TAXI")
        "i_bus_stop" -> drawBusFront(center, w, whiteColor)
        "i_railway_station" -> drawTrainFront(center, w, whiteColor)
        "i_airport" -> drawAirplane(center, w, whiteColor)
        "i_police_station" -> drawPoliceBadge(center, w, whiteColor)
        "i_dead_end" -> drawDeadEndT(center, w)
        "i_no_through_side" -> drawDeadEndSide(center, w)
        "i_highway_start" -> drawHighwayBridge(center, w, whiteColor)
        else -> drawNativeText("i", center, w * 0.45f, android.graphics.Color.WHITE, isBold = true)
    }
}

// =============================================================================
// 4. ROAD SURFACE MARKINGS (Asphalt Background + Crisp Pavement Stripes)
// =============================================================================
private fun DrawScope.drawRoadMarkingSign(signKey: String) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    // Dark asphalt road segment
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(12f, 12f)
    )

    // Road Kerbs
    drawLine(color = Color(0xFF64748B), start = Offset(w * 0.12f, h * 0.06f), end = Offset(w * 0.12f, h * 0.94f), strokeWidth = w * 0.03f)
    drawLine(color = Color(0xFF64748B), start = Offset(w * 0.88f, h * 0.06f), end = Offset(w * 0.88f, h * 0.94f), strokeWidth = w * 0.03f)

    val white = Color.White
    val yellow = Color(0xFFFACC15)

    when (signKey) {
        "r_broken_white" -> {
            // Dashed center line
            val lineW = w * 0.06f
            for (i in 0..2) {
                val yStart = h * (0.12f + i * 0.28f)
                drawLine(color = white, start = Offset(center.x, yStart), end = Offset(center.x, yStart + h * 0.18f), strokeWidth = lineW, cap = StrokeCap.Square)
            }
        }

        "r_solid_white" -> {
            drawLine(color = white, start = Offset(center.x, h * 0.06f), end = Offset(center.x, h * 0.94f), strokeWidth = w * 0.07f)
        }

        "r_double_yellow" -> {
            val off = w * 0.05f
            val thick = w * 0.05f
            drawLine(color = yellow, start = Offset(center.x - off, h * 0.06f), end = Offset(center.x - off, h * 0.94f), strokeWidth = thick)
            drawLine(color = yellow, start = Offset(center.x + off, h * 0.06f), end = Offset(center.x + off, h * 0.94f), strokeWidth = thick)
        }

        "r_solid_broken_combo" -> {
            val off = w * 0.05f
            val thick = w * 0.05f
            // Solid on left
            drawLine(color = white, start = Offset(center.x - off, h * 0.06f), end = Offset(center.x - off, h * 0.94f), strokeWidth = thick)
            // Broken on right
            for (i in 0..2) {
                val yStart = h * (0.12f + i * 0.28f)
                drawLine(color = white, start = Offset(center.x + off, yStart), end = Offset(center.x + off, yStart + h * 0.18f), strokeWidth = thick)
            }
        }

        "r_zebra_crossing" -> {
            // White pedestrian stripes
            val barH = h * 0.10f
            for (i in 0..3) {
                val y = h * (0.16f + i * 0.19f)
                drawRect(color = white, topLeft = Offset(w * 0.22f, y), size = Size(w * 0.56f, barH))
            }
        }

        "r_stop_line" -> {
            // Thick transverse stop line
            drawRect(color = white, topLeft = Offset(w * 0.12f, h * 0.52f), size = Size(w * 0.76f, w * 0.12f))
            drawNativeText("STOP", Offset(center.x, h * 0.32f), w * 0.18f, android.graphics.Color.WHITE, isBold = true)
        }

        "r_give_way_triangle" -> {
            val tri = Path().apply {
                moveTo(center.x, h * 0.72f)
                lineTo(center.x - w * 0.22f, h * 0.28f)
                lineTo(center.x + w * 0.22f, h * 0.28f)
                close()
            }
            drawPath(tri, color = white, style = Stroke(width = w * 0.06f))
        }

        "r_yellow_box" -> {
            // Yellow box junction grid
            drawRoundRect(
                color = yellow,
                topLeft = Offset(w * 0.18f, h * 0.18f),
                size = Size(w * 0.64f, h * 0.64f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = w * 0.04f)
            )
            drawLine(color = yellow, start = Offset(w * 0.18f, h * 0.18f), end = Offset(w * 0.82f, h * 0.82f), strokeWidth = w * 0.035f)
            drawLine(color = yellow, start = Offset(w * 0.82f, h * 0.18f), end = Offset(w * 0.18f, h * 0.82f), strokeWidth = w * 0.035f)
            drawLine(color = yellow, start = Offset(center.x, h * 0.18f), end = Offset(center.x, h * 0.82f), strokeWidth = w * 0.03f)
            drawLine(color = yellow, start = Offset(w * 0.18f, center.y), end = Offset(w * 0.82f, center.y), strokeWidth = w * 0.03f)
        }
    }
}



// =============================================================================
// 5. TRAFFIC LIGHTS & POLICE SIGNALS (DoTM Standard - Rebuilt Vector Engine)
// =============================================================================
private fun DrawScope.drawTrafficLightSign(signKey: String) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    when {
        // Pedestrian Signals
        signKey.contains("pedestrian") || signKey.contains("ped_") -> {
            val isWalk = signKey.contains("green") || signKey.contains("walk")
            drawPedestrianSignalGraphic(center, w, h, isWalk)
        }
        // Police Hand Gestures
        signKey.contains("police") || signKey.contains("p_stop") || signKey.contains("p_beckon") || signKey.contains("p_vip") -> {
            drawPoliceOfficerGestureCard(center, w, h, signKey)
        }
        // Standard Vehicular Traffic Lights (Red, Amber, Green, Flashing, Arrow)
        else -> {
            drawVehicularTrafficLightGraphic(center, w, h, signKey)
        }
    }
}

/**
 * 3-Aspect High-Fidelity Vehicular Traffic Signal Box
 */
private fun DrawScope.drawVehicularTrafficLightGraphic(
    center: Offset,
    w: Float,
    h: Float,
    signKey: String
) {
    val boxW = w * 0.44f
    val boxH = h * 0.90f
    val boxLeft = center.x - boxW / 2f
    val boxTop = center.y - boxH / 2f

    // Bottom mounting pole
    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(center.x - boxW * 0.10f, boxTop + boxH),
        size = Size(boxW * 0.20f, h * 0.05f)
    )

    // Side Backplate Flanges (Classic traffic light frame)
    val flangeW = boxW * 0.14f
    val flangeH = boxH * 0.35f
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(boxLeft - flangeW * 0.7f, center.y - flangeH / 2f),
        size = Size(flangeW, flangeH),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(boxLeft + boxW - flangeW * 0.3f, center.y - flangeH / 2f),
        size = Size(flangeW, flangeH),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Main Signal Housing Box
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 2f)
    )

    val lightRadius = boxW * 0.29f
    val yRed = boxTop + boxH * 0.20f
    val yYellow = boxTop + boxH * 0.50f
    val yGreen = boxTop + boxH * 0.80f

    val isRed = signKey.contains("red")
    val isYellow = signKey.contains("yellow") || signKey.contains("amber")
    val isGreen = signKey.contains("green")
    val isFlashing = signKey.contains("flashing")
    val isArrow = signKey.contains("arrow")

    val centersY = listOf(yRed, yYellow, yGreen)

    // Draw Dark Visor Hoods (Overhang caps over each lamp)
    for (cy in centersY) {
        val hoodPath = Path().apply {
            moveTo(center.x - lightRadius - 3f, cy - lightRadius * 0.3f)
            quadraticBezierTo(center.x, cy - lightRadius - 6f, center.x + lightRadius + 3f, cy - lightRadius * 0.3f)
        }
        drawPath(
            path = hoodPath,
            color = Color(0xFF020617),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
    }

    // 1. RED LAMP
    if (isRed) {
        // Radiant Glow
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = 0.30f),
            radius = lightRadius * 1.55f,
            center = Offset(center.x, yRed)
        )
        // Flashing burst rays
        if (isFlashing) {
            for (angle in 0 until 360 step 45) {
                val rad = Math.toRadians(angle.toDouble())
                val r1 = lightRadius + 4f
                val r2 = lightRadius + 12f
                drawLine(
                    color = Color(0xFFFCA5A5),
                    start = Offset((center.x + r1 * cos(rad)).toFloat(), (yRed + r1 * sin(rad)).toFloat()),
                    end = Offset((center.x + r2 * cos(rad)).toFloat(), (yRed + r2 * sin(rad)).toFloat()),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
        // Active Red Lens
        drawCircle(color = Color(0xFFEF4444), radius = lightRadius, center = Offset(center.x, yRed))
        drawCircle(color = Color(0xFFB91C1C), radius = lightRadius, center = Offset(center.x, yRed), style = Stroke(width = 2f))
        // Inner fresnel texture ring
        drawCircle(color = Color.White.copy(alpha = 0.25f), radius = lightRadius * 0.65f, center = Offset(center.x, yRed), style = Stroke(width = 1.5f))
        // Specular highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.70f),
            radius = lightRadius * 0.28f,
            center = Offset(center.x - lightRadius * 0.35f, yRed - lightRadius * 0.35f)
        )
    } else {
        // Inactive Dark Red Lens
        drawCircle(color = Color(0xFF450A0A), radius = lightRadius, center = Offset(center.x, yRed))
        drawCircle(color = Color(0xFF1E293B), radius = lightRadius, center = Offset(center.x, yRed), style = Stroke(width = 1.5f))
    }

    // 2. YELLOW / AMBER LAMP
    if (isYellow) {
        // Radiant Glow
        drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = 0.32f),
            radius = lightRadius * 1.55f,
            center = Offset(center.x, yYellow)
        )
        // Flashing burst rays
        if (isFlashing) {
            for (angle in 0 until 360 step 45) {
                val rad = Math.toRadians(angle.toDouble())
                val r1 = lightRadius + 4f
                val r2 = lightRadius + 12f
                drawLine(
                    color = Color(0xFFFDE68A),
                    start = Offset((center.x + r1 * cos(rad)).toFloat(), (yYellow + r1 * sin(rad)).toFloat()),
                    end = Offset((center.x + r2 * cos(rad)).toFloat(), (yYellow + r2 * sin(rad)).toFloat()),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
        // Active Amber Lens
        drawCircle(color = Color(0xFFF59E0B), radius = lightRadius, center = Offset(center.x, yYellow))
        drawCircle(color = Color(0xFFD97706), radius = lightRadius, center = Offset(center.x, yYellow), style = Stroke(width = 2f))
        // Inner fresnel texture ring
        drawCircle(color = Color.White.copy(alpha = 0.25f), radius = lightRadius * 0.65f, center = Offset(center.x, yYellow), style = Stroke(width = 1.5f))
        // Specular highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.70f),
            radius = lightRadius * 0.28f,
            center = Offset(center.x - lightRadius * 0.35f, yYellow - lightRadius * 0.35f)
        )
    } else {
        // Inactive Dark Amber Lens
        drawCircle(color = Color(0xFF451A03), radius = lightRadius, center = Offset(center.x, yYellow))
        drawCircle(color = Color(0xFF1E293B), radius = lightRadius, center = Offset(center.x, yYellow), style = Stroke(width = 1.5f))
    }

    // 3. GREEN LAMP
    if (isGreen) {
        // Radiant Glow
        drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.32f),
            radius = lightRadius * 1.55f,
            center = Offset(center.x, yGreen)
        )
        // Active Green Lens
        drawCircle(color = Color(0xFF10B981), radius = lightRadius, center = Offset(center.x, yGreen))
        drawCircle(color = Color(0xFF047857), radius = lightRadius, center = Offset(center.x, yGreen), style = Stroke(width = 2f))

        if (isArrow) {
            // Illuminated Green Turn Arrow
            val arrowPath = Path().apply {
                moveTo(center.x - lightRadius * 0.45f, yGreen - lightRadius * 0.18f)
                lineTo(center.x + lightRadius * 0.10f, yGreen - lightRadius * 0.18f)
                lineTo(center.x + lightRadius * 0.10f, yGreen - lightRadius * 0.48f)
                lineTo(center.x + lightRadius * 0.60f, yGreen)
                lineTo(center.x + lightRadius * 0.10f, yGreen + lightRadius * 0.48f)
                lineTo(center.x + lightRadius * 0.10f, yGreen + lightRadius * 0.18f)
                lineTo(center.x - lightRadius * 0.45f, yGreen + lightRadius * 0.18f)
                close()
            }
            drawPath(arrowPath, color = Color.White)
        } else {
            // Inner fresnel texture ring
            drawCircle(color = Color.White.copy(alpha = 0.25f), radius = lightRadius * 0.65f, center = Offset(center.x, yGreen), style = Stroke(width = 1.5f))
            // Specular highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.70f),
                radius = lightRadius * 0.28f,
                center = Offset(center.x - lightRadius * 0.35f, yGreen - lightRadius * 0.35f)
            )
        }
    } else {
        // Inactive Dark Green Lens
        drawCircle(color = Color(0xFF022C22), radius = lightRadius, center = Offset(center.x, yGreen))
        drawCircle(color = Color(0xFF1E293B), radius = lightRadius, center = Offset(center.x, yGreen), style = Stroke(width = 1.5f))
    }
}

/**
 * 2-Aspect Pedestrian Crosswalk Signal Box (Red Don't Walk Man & Green Walk Man)
 */
private fun DrawScope.drawPedestrianSignalGraphic(
    center: Offset,
    w: Float,
    h: Float,
    isWalk: Boolean
) {
    val boxW = w * 0.44f
    val boxH = h * 0.82f
    val boxLeft = center.x - boxW / 2f
    val boxTop = center.y - boxH / 2f

    // Housing Box
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 2f)
    )

    val lightRadius = boxW * 0.32f
    val cyRed = boxTop + boxH * 0.28f
    val cyGreen = boxTop + boxH * 0.72f

    // Visors
    for (cy in listOf(cyRed, cyGreen)) {
        val hood = Path().apply {
            moveTo(center.x - lightRadius - 2f, cy - lightRadius * 0.3f)
            quadraticBezierTo(center.x, cy - lightRadius - 5f, center.x + lightRadius + 2f, cy - lightRadius * 0.3f)
        }
        drawPath(hood, color = Color(0xFF020617), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
    }

    // Top Lens (Don't Walk - Red Standing Man)
    val redActive = !isWalk
    drawCircle(
        color = if (redActive) Color(0xFF220505) else Color(0xFF150303),
        radius = lightRadius,
        center = Offset(center.x, cyRed)
    )
    if (redActive) {
        drawCircle(color = Color(0xFFEF4444).copy(alpha = 0.25f), radius = lightRadius * 1.4f, center = Offset(center.x, cyRed))
    }
    // Red Standing Silhouette
    val rCol = if (redActive) Color(0xFFEF4444) else Color(0xFF5A1A1A)
    // Head
    drawCircle(color = rCol, radius = lightRadius * 0.22f, center = Offset(center.x, cyRed - lightRadius * 0.50f))
    // Torso & Arms
    drawLine(color = rCol, start = Offset(center.x, cyRed - lightRadius * 0.25f), end = Offset(center.x, cyRed + lightRadius * 0.15f), strokeWidth = lightRadius * 0.30f, cap = StrokeCap.Round)
    drawLine(color = rCol, start = Offset(center.x - lightRadius * 0.32f, cyRed - lightRadius * 0.10f), end = Offset(center.x + lightRadius * 0.32f, cyRed - lightRadius * 0.10f), strokeWidth = lightRadius * 0.16f, cap = StrokeCap.Round)
    // Legs
    drawLine(color = rCol, start = Offset(center.x - lightRadius * 0.14f, cyRed + lightRadius * 0.15f), end = Offset(center.x - lightRadius * 0.14f, cyRed + lightRadius * 0.65f), strokeWidth = lightRadius * 0.16f, cap = StrokeCap.Round)
    drawLine(color = rCol, start = Offset(center.x + lightRadius * 0.14f, cyRed + lightRadius * 0.15f), end = Offset(center.x + lightRadius * 0.14f, cyRed + lightRadius * 0.65f), strokeWidth = lightRadius * 0.16f, cap = StrokeCap.Round)

    // Bottom Lens (Walk - Green Dynamic Walking Man)
    val greenActive = isWalk
    drawCircle(
        color = if (greenActive) Color(0xFF032115) else Color(0xFF02120C),
        radius = lightRadius,
        center = Offset(center.x, cyGreen)
    )
    if (greenActive) {
        drawCircle(color = Color(0xFF10B981).copy(alpha = 0.25f), radius = lightRadius * 1.4f, center = Offset(center.x, cyGreen))
    }
    // Green Walking Silhouette
    val gCol = if (greenActive) Color(0xFF10B981) else Color(0xFF064E3B)
    // Head tilted slightly forward
    drawCircle(color = gCol, radius = lightRadius * 0.22f, center = Offset(center.x + lightRadius * 0.10f, cyGreen - lightRadius * 0.50f))
    // Leaning Torso
    drawLine(color = gCol, start = Offset(center.x + lightRadius * 0.08f, cyGreen - lightRadius * 0.25f), end = Offset(center.x - lightRadius * 0.05f, cyGreen + lightRadius * 0.15f), strokeWidth = lightRadius * 0.28f, cap = StrokeCap.Round)
    // Swinging Arms
    drawLine(color = gCol, start = Offset(center.x + lightRadius * 0.05f, cyGreen - lightRadius * 0.15f), end = Offset(center.x + lightRadius * 0.45f, cyGreen + lightRadius * 0.10f), strokeWidth = lightRadius * 0.16f, cap = StrokeCap.Round)
    drawLine(color = gCol, start = Offset(center.x + lightRadius * 0.05f, cyGreen - lightRadius * 0.15f), end = Offset(center.x - lightRadius * 0.35f, cyGreen + lightRadius * 0.05f), strokeWidth = lightRadius * 0.16f, cap = StrokeCap.Round)
    // Striding Legs
    drawLine(color = gCol, start = Offset(center.x - lightRadius * 0.05f, cyGreen + lightRadius * 0.15f), end = Offset(center.x + lightRadius * 0.40f, cyGreen + lightRadius * 0.65f), strokeWidth = lightRadius * 0.16f, cap = StrokeCap.Round)
    drawLine(color = gCol, start = Offset(center.x - lightRadius * 0.05f, cyGreen + lightRadius * 0.15f), end = Offset(center.x - lightRadius * 0.40f, cyGreen + lightRadius * 0.58f), strokeWidth = lightRadius * 0.16f, cap = StrokeCap.Round)
}

/**
 * Traffic Police Officer Hand Gesture Signal Card (DoTM & Nepal Police Standard)
 */
private fun DrawScope.drawPoliceOfficerGestureCard(
    center: Offset,
    w: Float,
    h: Float,
    signKey: String
) {
    // Card Background
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 1.5f)
    )

    // Road dashed guideline at bottom
    drawLine(
        color = Color(0xFF475569),
        start = Offset(center.x, h * 0.82f),
        end = Offset(center.x, h * 0.92f),
        strokeWidth = 3f
    )

    val bodyY = center.y + h * 0.08f

    // Legs / Navy Trousers
    drawRoundRect(
        color = Color(0xFF1E3A8A),
        topLeft = Offset(center.x - w * 0.12f, bodyY + h * 0.12f),
        size = Size(w * 0.10f, h * 0.22f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = Color(0xFF1E3A8A),
        topLeft = Offset(center.x + w * 0.02f, bodyY + h * 0.12f),
        size = Size(w * 0.10f, h * 0.22f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Torso (Official Deep Navy Blue Police Shirt)
    drawRoundRect(
        color = Color(0xFF1E3A8A),
        topLeft = Offset(center.x - w * 0.18f, bodyY - h * 0.12f),
        size = Size(w * 0.36f, h * 0.24f),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // Fluorescent High-Visibility Lime/Yellow Cross-Belt & Vest
    val vestPath = Path().apply {
        moveTo(center.x - w * 0.14f, bodyY - h * 0.12f)
        lineTo(center.x + w * 0.14f, bodyY + h * 0.10f)
        lineTo(center.x + w * 0.08f, bodyY + h * 0.10f)
        lineTo(center.x - w * 0.18f, bodyY - h * 0.12f)
        close()
        moveTo(center.x + w * 0.14f, bodyY - h * 0.12f)
        lineTo(center.x - w * 0.14f, bodyY + h * 0.10f)
        lineTo(center.x - w * 0.08f, bodyY + h * 0.10f)
        lineTo(center.x + w * 0.18f, bodyY - h * 0.12f)
        close()
    }
    drawPath(vestPath, color = Color(0xFFA3E635))
    // Belt
    drawRect(
        color = Color(0xFFEAB308),
        topLeft = Offset(center.x - w * 0.16f, bodyY + h * 0.05f),
        size = Size(w * 0.32f, h * 0.05f)
    )

    // Head & Nepal Police Peaked Cap
    val headY = bodyY - h * 0.22f
    // Face skin
    drawCircle(color = Color(0xFFFED7AA), radius = w * 0.09f, center = Offset(center.x, headY))
    // Cap crown
    drawRoundRect(
        color = Color(0xFF1E3A8A),
        topLeft = Offset(center.x - w * 0.11f, headY - h * 0.14f),
        size = Size(w * 0.22f, h * 0.08f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Gold cap band
    drawRect(
        color = Color(0xFFEAB308),
        topLeft = Offset(center.x - w * 0.11f, headY - h * 0.07f),
        size = Size(w * 0.22f, h * 0.02f)
    )
    // Cap visor peak
    drawRect(
        color = Color(0xFF020617),
        topLeft = Offset(center.x - w * 0.13f, headY - h * 0.05f),
        size = Size(w * 0.26f, h * 0.02f)
    )

    // Gesture Arm Configurations with White Gloves & Stop Discs / Flow Arrows
    val uniformColor = Color(0xFF1E3A8A)
    val whiteGlove = Color.White
    val stopRed = Color(0xFFEF4444)

    when {
        // STOP TRAFFIC FROM FRONT
        signKey.contains("stop_front") || signKey.contains("front") -> {
            // Left arm down at side
            drawLine(color = uniformColor, start = Offset(center.x - w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x - w * 0.18f, bodyY + h * 0.10f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.045f, center = Offset(center.x - w * 0.18f, bodyY + h * 0.10f))

            // Right arm RAISED VERTICAL (Stop Front Traffic)
            drawLine(color = uniformColor, start = Offset(center.x + w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x + w * 0.20f, headY - h * 0.08f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            // Big White glove with Red Stop Palm Disc
            val glovePos = Offset(center.x + w * 0.20f, headY - h * 0.14f)
            drawCircle(color = whiteGlove, radius = w * 0.08f, center = glovePos)
            drawCircle(color = stopRed, radius = w * 0.05f, center = glovePos)
        }

        // STOP TRAFFIC FROM REAR / BEHIND
        signKey.contains("stop_rear") || signKey.contains("rear") -> {
            // Right arm down at side
            drawLine(color = uniformColor, start = Offset(center.x + w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x + w * 0.18f, bodyY + h * 0.10f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.045f, center = Offset(center.x + w * 0.18f, bodyY + h * 0.10f))

            // Left arm EXTENDED HORIZONTALLY (Stop Rear Traffic)
            val leftEnd = Offset(center.x - w * 0.36f, bodyY - h * 0.08f)
            drawLine(color = uniformColor, start = Offset(center.x - w * 0.15f, bodyY - h * 0.08f), end = leftEnd, strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            // White glove with Red Stop Disc
            drawCircle(color = whiteGlove, radius = w * 0.075f, center = leftEnd)
            drawCircle(color = stopRed, radius = w * 0.045f, center = leftEnd)
        }

        // STOP TRAFFIC FROM BOTH FRONT & REAR
        signKey.contains("stop_both") || signKey.contains("both") -> {
            // Right arm UP (Front Stop)
            val rightEnd = Offset(center.x + w * 0.20f, headY - h * 0.14f)
            drawLine(color = uniformColor, start = Offset(center.x + w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x + w * 0.20f, headY - h * 0.08f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.08f, center = rightEnd)
            drawCircle(color = stopRed, radius = w * 0.05f, center = rightEnd)

            // Left arm HORIZONTAL (Rear Stop)
            val leftEnd = Offset(center.x - w * 0.36f, bodyY - h * 0.08f)
            drawLine(color = uniformColor, start = Offset(center.x - w * 0.15f, bodyY - h * 0.08f), end = leftEnd, strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.075f, center = leftEnd)
            drawCircle(color = stopRed, radius = w * 0.045f, center = leftEnd)
        }

        // BECKON / PASS TRAFFIC FROM LEFT
        signKey.contains("beckon_left") || signKey.contains("pass_left") -> {
            // Left arm directing
            drawLine(color = uniformColor, start = Offset(center.x - w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x - w * 0.30f, bodyY), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.05f, center = Offset(center.x - w * 0.30f, bodyY))

            // Right arm beckoning across chest
            drawLine(color = uniformColor, start = Offset(center.x + w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x, bodyY - h * 0.02f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.05f, center = Offset(center.x, bodyY - h * 0.02f))

            // Green flow motion arrow
            val greenFlow = Color(0xFF22C55E)
            val flowArc = Path().apply {
                moveTo(center.x - w * 0.32f, bodyY - h * 0.20f)
                quadraticBezierTo(center.x, bodyY - h * 0.30f, center.x + w * 0.28f, bodyY - h * 0.15f)
            }
            drawPath(flowArc, color = greenFlow, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            // Arrowhead
            val arr = Path().apply {
                moveTo(center.x + w * 0.28f, bodyY - h * 0.15f)
                lineTo(center.x + w * 0.18f, bodyY - h * 0.22f)
                lineTo(center.x + w * 0.20f, bodyY - h * 0.08f)
                close()
            }
            drawPath(arr, color = greenFlow)
        }

        // BECKON / PASS TRAFFIC FROM RIGHT OR VIP
        else -> {
            // Right arm directing
            drawLine(color = uniformColor, start = Offset(center.x + w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x + w * 0.30f, bodyY), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.05f, center = Offset(center.x + w * 0.30f, bodyY))

            // Left arm beckoning
            drawLine(color = uniformColor, start = Offset(center.x - w * 0.15f, bodyY - h * 0.08f), end = Offset(center.x, bodyY - h * 0.02f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawCircle(color = whiteGlove, radius = w * 0.05f, center = Offset(center.x, bodyY - h * 0.02f))

            // Green flow motion arrow
            val greenFlow = Color(0xFF22C55E)
            val flowArc = Path().apply {
                moveTo(center.x + w * 0.32f, bodyY - h * 0.20f)
                quadraticBezierTo(center.x, bodyY - h * 0.30f, center.x - w * 0.28f, bodyY - h * 0.15f)
            }
            drawPath(flowArc, color = greenFlow, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            val arr = Path().apply {
                moveTo(center.x - w * 0.28f, bodyY - h * 0.15f)
                lineTo(center.x - w * 0.18f, bodyY - h * 0.22f)
                lineTo(center.x - w * 0.20f, bodyY - h * 0.08f)
                close()
            }
            drawPath(arr, color = greenFlow)
        }
    }
}

// =============================================================================
// PRISTINE NATIVE TEXT HELPER
// =============================================================================
private fun DrawScope.drawNativeText(
    text: String,
    center: Offset,
    textSizePx: Float,
    colorInt: Int,
    isBold: Boolean = true
) {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = colorInt
            textSize = textSizePx
            typeface = if (isBold) Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) else Typeface.SANS_SERIF
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val yOffset = (paint.descent() + paint.ascent()) / 2f
        canvas.nativeCanvas.drawText(text, center.x, center.y - yOffset, paint)
    }
}

// =============================================================================
// VECTOR GLYPH DRAWING HELPERS
// =============================================================================

private fun DrawScope.drawArrowUp(center: Offset, w: Float, color: Color) {
    val stemW = w * 0.12f
    val stemH = w * 0.36f
    drawRect(
        color = color,
        topLeft = Offset(center.x - stemW / 2f, center.y - stemH * 0.15f),
        size = Size(stemW, stemH * 0.65f)
    )
    val head = Path().apply {
        moveTo(center.x, center.y - stemH * 0.65f)
        lineTo(center.x + w * 0.22f, center.y - stemH * 0.15f)
        lineTo(center.x - w * 0.22f, center.y - stemH * 0.15f)
        close()
    }
    drawPath(head, color = color, style = Fill)
}

private fun DrawScope.drawArrowLeft90(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x + w * 0.14f, center.y + w * 0.18f)
        lineTo(center.x + w * 0.14f, center.y - w * 0.04f)
        quadraticBezierTo(center.x + w * 0.14f, center.y - w * 0.14f, center.x, center.y - w * 0.14f)
        lineTo(center.x - w * 0.10f, center.y - w * 0.14f)
    }
    drawPath(path, color = color, style = Stroke(width = w * 0.10f, cap = StrokeCap.Square))

    val head = Path().apply {
        moveTo(center.x - w * 0.24f, center.y - w * 0.14f)
        lineTo(center.x - w * 0.08f, center.y - w * 0.26f)
        lineTo(center.x - w * 0.08f, center.y - w * 0.02f)
        close()
    }
    drawPath(head, color = color, style = Fill)
}

private fun DrawScope.drawArrowRight90(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - w * 0.14f, center.y + w * 0.18f)
        lineTo(center.x - w * 0.14f, center.y - w * 0.04f)
        quadraticBezierTo(center.x - w * 0.14f, center.y - w * 0.14f, center.x, center.y - w * 0.14f)
        lineTo(center.x + w * 0.10f, center.y - w * 0.14f)
    }
    drawPath(path, color = color, style = Stroke(width = w * 0.10f, cap = StrokeCap.Square))

    val head = Path().apply {
        moveTo(center.x + w * 0.24f, center.y - w * 0.14f)
        lineTo(center.x + w * 0.08f, center.y - w * 0.26f)
        lineTo(center.x + w * 0.08f, center.y - w * 0.02f)
        close()
    }
    drawPath(head, color = color, style = Fill)
}

private fun DrawScope.drawArrowAheadOrLeft(center: Offset, w: Float, color: Color) {
    drawArrowUp(Offset(center.x + w * 0.08f, center.y), w * 0.8f, color)
    drawArrowLeft90(Offset(center.x - w * 0.04f, center.y + w * 0.04f), w * 0.8f, color)
}

private fun DrawScope.drawArrowAheadOrRight(center: Offset, w: Float, color: Color) {
    drawArrowUp(Offset(center.x - w * 0.08f, center.y), w * 0.8f, color)
    drawArrowRight90(Offset(center.x + w * 0.04f, center.y + w * 0.04f), w * 0.8f, color)
}

private fun DrawScope.drawArrowKeepLeft(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x + w * 0.14f, center.y - w * 0.16f)
        lineTo(center.x - w * 0.12f, center.y + w * 0.12f)
    }
    drawPath(path, color = color, style = Stroke(width = w * 0.10f, cap = StrokeCap.Round))

    val head = Path().apply {
        moveTo(center.x - w * 0.22f, center.y + w * 0.20f)
        lineTo(center.x - w * 0.22f, center.y + w * 0.02f)
        lineTo(center.x - w * 0.04f, center.y + w * 0.20f)
        close()
    }
    drawPath(head, color = color, style = Fill)
}

private fun DrawScope.drawArrowKeepRight(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - w * 0.14f, center.y - w * 0.16f)
        lineTo(center.x + w * 0.12f, center.y + w * 0.12f)
    }
    drawPath(path, color = color, style = Stroke(width = w * 0.10f, cap = StrokeCap.Round))

    val head = Path().apply {
        moveTo(center.x + w * 0.22f, center.y + w * 0.20f)
        lineTo(center.x + w * 0.22f, center.y + w * 0.02f)
        lineTo(center.x + w * 0.04f, center.y + w * 0.20f)
        close()
    }
    drawPath(head, color = color, style = Fill)
}

private fun DrawScope.drawRoundaboutArrows(center: Offset, w: Float, color: Color) {
    val r = w * 0.26f
    drawCircle(color = color, radius = r, center = center, style = Stroke(width = w * 0.07f))
    for (i in 0 until 3) {
        val angle = Math.toRadians((i * 120.0) - 30.0)
        val tipX = center.x + (r * cos(angle)).toFloat()
        val tipY = center.y + (r * sin(angle)).toFloat()
        drawCircle(color = color, radius = w * 0.055f, center = Offset(tipX, tipY))
    }
}

private fun DrawScope.drawArrowUTurn(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x + w * 0.12f, center.y + w * 0.18f)
        lineTo(center.x + w * 0.12f, center.y - w * 0.04f)
        arcTo(
            rect = Rect(center.x - w * 0.14f, center.y - w * 0.20f, center.x + w * 0.14f, center.y + w * 0.08f),
            startAngleDegrees = 0f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(center.x - w * 0.14f, center.y + w * 0.10f)
    }
    drawPath(path, color = color, style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))

    val head = Path().apply {
        moveTo(center.x - w * 0.14f, center.y + w * 0.20f)
        lineTo(center.x - w * 0.22f, center.y + w * 0.06f)
        lineTo(center.x - w * 0.06f, center.y + w * 0.06f)
        close()
    }
    drawPath(head, color = color, style = Fill)
}

private fun DrawScope.drawHornSymbol(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - w * 0.18f, center.y - w * 0.04f)
        lineTo(center.x - w * 0.06f, center.y - w * 0.04f)
        lineTo(center.x + w * 0.12f, center.y - w * 0.16f)
        lineTo(center.x + w * 0.12f, center.y + w * 0.16f)
        lineTo(center.x - w * 0.06f, center.y + w * 0.04f)
        lineTo(center.x - w * 0.18f, center.y + w * 0.04f)
        close()
    }
    drawPath(path, color = color, style = Fill)
    // Sound wave arcs
    drawArc(
        color = color,
        startAngle = -45f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(center.x + w * 0.08f, center.y - w * 0.14f),
        size = Size(w * 0.14f, w * 0.28f),
        style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawOvertakingGlyph(center: Offset, w: Float) {
    // Red overtaking car on right
    drawCarFrontGlyph(Offset(center.x + w * 0.11f, center.y - w * 0.02f), w * 0.65f, Color(0xFFDC2626))
    // Black car on left
    drawCarFrontGlyph(Offset(center.x - w * 0.11f, center.y + w * 0.02f), w * 0.65f, Color(0xFF0F172A))
}

private fun DrawScope.drawDimensionSign(center: Offset, w: Float, text: String, isHorizontal: Boolean) {
    val dark = Color(0xFF0F172A)
    if (isHorizontal) {
        drawLine(color = dark, start = Offset(center.x - w * 0.32f, center.y), end = Offset(center.x - w * 0.18f, center.y), strokeWidth = w * 0.04f)
        drawLine(color = dark, start = Offset(center.x + w * 0.32f, center.y), end = Offset(center.x + w * 0.18f, center.y), strokeWidth = w * 0.04f)
    } else {
        drawLine(color = dark, start = Offset(center.x, center.y - w * 0.32f), end = Offset(center.x, center.y - w * 0.18f), strokeWidth = w * 0.04f)
        drawLine(color = dark, start = Offset(center.x, center.y + w * 0.32f), end = Offset(center.x, center.y + w * 0.18f), strokeWidth = w * 0.04f)
    }
    drawNativeText(text, center, w * 0.26f, android.graphics.Color.parseColor("#0F172A"), isBold = true)
}

private fun DrawScope.drawWeightSign(center: Offset, w: Float, weightText: String) {
    drawNativeText(weightText, center, w * 0.32f, android.graphics.Color.parseColor("#0F172A"), isBold = true)
}

private fun DrawScope.drawParkingWithSub(center: Offset, w: Float, subText: String) {
    drawNativeText("P", Offset(center.x, center.y - w * 0.08f), w * 0.38f, android.graphics.Color.WHITE, isBold = true)
    drawNativeText(subText, Offset(center.x, center.y + w * 0.22f), w * 0.14f, android.graphics.Color.WHITE, isBold = true)
}

private fun DrawScope.drawCurveArrow(center: Offset, w: Float, isLeft: Boolean, isSharp: Boolean) {
    val dir = if (isLeft) -1f else 1f
    val path = Path().apply {
        moveTo(center.x - dir * w * 0.12f, center.y + w * 0.16f)
        lineTo(center.x - dir * w * 0.12f, center.y)
        if (isSharp) {
            lineTo(center.x + dir * w * 0.14f, center.y)
        } else {
            quadraticBezierTo(center.x - dir * w * 0.12f, center.y - w * 0.12f, center.x + dir * w * 0.14f, center.y - w * 0.12f)
        }
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.09f, cap = StrokeCap.Square))

    val head = Path().apply {
        moveTo(center.x + dir * w * 0.22f, center.y - (if (isSharp) 0f else w * 0.12f))
        lineTo(center.x + dir * w * 0.08f, center.y - (if (isSharp) 0f else w * 0.12f) - w * 0.10f)
        lineTo(center.x + dir * w * 0.08f, center.y - (if (isSharp) 0f else w * 0.12f) + w * 0.10f)
        close()
    }
    drawPath(head, color = Color(0xFF0F172A), style = Fill)
}

private fun DrawScope.drawHairpinCurve(center: Offset, w: Float, isLeft: Boolean) {
    val dir = if (isLeft) -1f else 1f
    val path = Path().apply {
        moveTo(center.x - dir * w * 0.12f, center.y + w * 0.16f)
        lineTo(center.x - dir * w * 0.12f, center.y - w * 0.04f)
        arcTo(
            rect = Rect(center.x - w * 0.18f, center.y - w * 0.20f, center.x + w * 0.18f, center.y + w * 0.02f),
            startAngleDegrees = if (isLeft) 0f else 180f,
            sweepAngleDegrees = if (isLeft) -180f else 180f,
            forceMoveTo = false
        )
        lineTo(center.x + dir * w * 0.12f, center.y + w * 0.10f)
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
}

private fun DrawScope.drawDoubleBend(center: Offset, w: Float, firstLeft: Boolean) {
    val dir = if (firstLeft) -1f else 1f
    val path = Path().apply {
        moveTo(center.x, center.y + w * 0.16f)
        lineTo(center.x, center.y + w * 0.04f)
        lineTo(center.x + dir * w * 0.14f, center.y - w * 0.06f)
        lineTo(center.x + dir * w * 0.14f, center.y - w * 0.16f)
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.09f, cap = StrokeCap.Square))

    val head = Path().apply {
        moveTo(center.x + dir * w * 0.14f, center.y - w * 0.24f)
        lineTo(center.x + dir * w * 0.04f, center.y - w * 0.12f)
        lineTo(center.x + dir * w * 0.24f, center.y - w * 0.12f)
        close()
    }
    drawPath(head, color = Color(0xFF0F172A), style = Fill)
}

private fun DrawScope.drawSlopeHill(center: Offset, w: Float, isAscent: Boolean) {
    val path = Path().apply {
        if (isAscent) {
            moveTo(center.x - w * 0.24f, center.y + w * 0.14f)
            lineTo(center.x + w * 0.24f, center.y - w * 0.14f)
            lineTo(center.x + w * 0.24f, center.y + w * 0.14f)
        } else {
            moveTo(center.x - w * 0.24f, center.y - w * 0.14f)
            lineTo(center.x + w * 0.24f, center.y + w * 0.14f)
            lineTo(center.x - w * 0.24f, center.y + w * 0.14f)
        }
        close()
    }
    drawPath(path, color = Color(0xFF0F172A), style = Fill)
}

private fun DrawScope.drawNarrowRoad(center: Offset, w: Float) {
    val dark = Color(0xFF0F172A)
    val path1 = Path().apply {
        moveTo(center.x - w * 0.22f, center.y + w * 0.16f)
        lineTo(center.x - w * 0.22f, center.y + w * 0.04f)
        lineTo(center.x - w * 0.10f, center.y - w * 0.08f)
        lineTo(center.x - w * 0.10f, center.y - w * 0.20f)
    }
    drawPath(path1, color = dark, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))

    val path2 = Path().apply {
        moveTo(center.x + w * 0.22f, center.y + w * 0.16f)
        lineTo(center.x + w * 0.22f, center.y + w * 0.04f)
        lineTo(center.x + w * 0.10f, center.y - w * 0.08f)
        lineTo(center.x + w * 0.10f, center.y - w * 0.20f)
    }
    drawPath(path2, color = dark, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
}

private fun DrawScope.drawRoadWidens(center: Offset, w: Float) {
    val dark = Color(0xFF0F172A)
    val path1 = Path().apply {
        moveTo(center.x - w * 0.10f, center.y + w * 0.16f)
        lineTo(center.x - w * 0.10f, center.y + w * 0.04f)
        lineTo(center.x - w * 0.22f, center.y - w * 0.08f)
        lineTo(center.x - w * 0.22f, center.y - w * 0.20f)
    }
    drawPath(path1, color = dark, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))

    val path2 = Path().apply {
        moveTo(center.x + w * 0.10f, center.y + w * 0.16f)
        lineTo(center.x + w * 0.10f, center.y + w * 0.04f)
        lineTo(center.x + w * 0.22f, center.y - w * 0.08f)
        lineTo(center.x + w * 0.22f, center.y - w * 0.20f)
    }
    drawPath(path2, color = dark, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
}

private fun DrawScope.drawNarrowBridge(center: Offset, w: Float) {
    val dark = Color(0xFF0F172A)
    val path1 = Path().apply {
        moveTo(center.x - w * 0.20f, center.y + w * 0.16f)
        lineTo(center.x - w * 0.08f, center.y + w * 0.05f)
        lineTo(center.x - w * 0.08f, center.y - w * 0.05f)
        lineTo(center.x - w * 0.20f, center.y - w * 0.16f)
    }
    drawPath(path1, color = dark, style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))

    val path2 = Path().apply {
        moveTo(center.x + w * 0.20f, center.y + w * 0.16f)
        lineTo(center.x + w * 0.08f, center.y + w * 0.05f)
        lineTo(center.x + w * 0.08f, center.y - w * 0.05f)
        lineTo(center.x + w * 0.20f, center.y - w * 0.16f)
    }
    drawPath(path2, color = dark, style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))
}

private fun DrawScope.drawSpeedHump(center: Offset, w: Float) {
    val path = Path().apply {
        moveTo(center.x - w * 0.28f, center.y + w * 0.08f)
        lineTo(center.x - w * 0.14f, center.y + w * 0.08f)
        quadraticBezierTo(center.x, center.y - w * 0.14f, center.x + w * 0.14f, center.y + w * 0.08f)
        lineTo(center.x + w * 0.28f, center.y + w * 0.08f)
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
}

private fun DrawScope.drawRoughRoad(center: Offset, w: Float) {
    val path = Path().apply {
        moveTo(center.x - w * 0.28f, center.y + w * 0.06f)
        quadraticBezierTo(center.x - w * 0.14f, center.y - w * 0.10f, center.x, center.y + w * 0.06f)
        quadraticBezierTo(center.x + w * 0.14f, center.y - w * 0.10f, center.x + w * 0.28f, center.y + w * 0.06f)
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
}

private fun DrawScope.drawIntersectionPlus(center: Offset, w: Float) {
    val l = w * 0.38f
    val t = w * 0.11f
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - l / 2f, center.y - t / 2f), size = Size(l, t))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - t / 2f, center.y - l / 2f), size = Size(t, l))
}

private fun DrawScope.drawIntersectionT(center: Offset, w: Float) {
    val l = w * 0.38f
    val t = w * 0.11f
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - l / 2f, center.y - t / 2f), size = Size(l, t))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - t / 2f, center.y - l / 2f), size = Size(t, l))
}

private fun DrawScope.drawIntersectionY(center: Offset, w: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y + w * 0.18f)
        lineTo(center.x, center.y)
        lineTo(center.x - w * 0.16f, center.y - w * 0.16f)
        moveTo(center.x, center.y)
        lineTo(center.x + w * 0.16f, center.y - w * 0.16f)
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.10f, cap = StrokeCap.Round))
}

private fun DrawScope.drawSideRoad(center: Offset, w: Float, isLeft: Boolean) {
    val l = w * 0.40f
    val t = w * 0.11f
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - t / 2f, center.y - l / 2f), size = Size(t, l))
    if (isLeft) {
        drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - l / 2f, center.y - t / 2f), size = Size(l / 2f, t))
    } else {
        drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x, center.y - t / 2f), size = Size(l / 2f, t))
    }
}

private fun DrawScope.drawStaggeredJunction(center: Offset, w: Float) {
    val l = w * 0.40f
    val t = w * 0.10f
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - t / 2f, center.y - l / 2f), size = Size(t, l))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - l * 0.4f, center.y + w * 0.05f), size = Size(l * 0.4f, t))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x, center.y - w * 0.12f), size = Size(l * 0.4f, t))
}

private fun DrawScope.drawFallingRocks(center: Offset, w: Float) {
    val path = Path().apply {
        moveTo(center.x - w * 0.24f, center.y - w * 0.18f)
        lineTo(center.x - w * 0.08f, center.y + w * 0.18f)
        lineTo(center.x - w * 0.24f, center.y + w * 0.18f)
        close()
    }
    drawPath(path, color = Color(0xFF0F172A), style = Fill)
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.045f, center = Offset(center.x + w * 0.06f, center.y - w * 0.06f))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.035f, center = Offset(center.x + w * 0.15f, center.y + w * 0.08f))
}

private fun DrawScope.drawRiverBank(center: Offset, w: Float) {
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - w * 0.26f, center.y - w * 0.04f), size = Size(w * 0.30f, w * 0.22f))
    drawLine(color = Color(0xFF0284C7), start = Offset(center.x + w * 0.06f, center.y + w * 0.08f), end = Offset(center.x + w * 0.26f, center.y + w * 0.08f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF0284C7), start = Offset(center.x + w * 0.06f, center.y + w * 0.16f), end = Offset(center.x + w * 0.26f, center.y + w * 0.16f), strokeWidth = w * 0.04f)
}

private fun DrawScope.drawRailwayBarrier(center: Offset, w: Float) {
    val barW = w * 0.48f
    val barH = w * 0.09f
    drawRect(color = Color(0xFFDC2626), topLeft = Offset(center.x - barW / 2f, center.y - barH / 2f), size = Size(barW, barH))
    drawRect(color = Color.White, topLeft = Offset(center.x - barW * 0.30f, center.y - barH / 2f), size = Size(barW * 0.15f, barH))
    drawRect(color = Color.White, topLeft = Offset(center.x + barW * 0.10f, center.y - barH / 2f), size = Size(barW * 0.15f, barH))
}

private fun DrawScope.drawTrainEngine(center: Offset, w: Float) {
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(center.x - w * 0.20f, center.y - w * 0.10f),
        size = Size(w * 0.40f, w * 0.20f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - w * 0.15f, center.y - w * 0.18f), size = Size(w * 0.08f, w * 0.08f))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.055f, center = Offset(center.x - w * 0.10f, center.y + w * 0.13f))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.055f, center = Offset(center.x + w * 0.10f, center.y + w * 0.13f))
}

private fun DrawScope.drawFencePicket(center: Offset, w: Float) {
    val pickW = w * 0.07f
    val pickH = w * 0.32f
    for (i in -2..2) {
        val px = center.x + i * (w * 0.09f)
        drawRect(color = Color(0xFF0F172A), topLeft = Offset(px - pickW / 2f, center.y - pickH / 2f), size = Size(pickW, pickH))
    }
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - w * 0.24f, center.y - w * 0.08f), size = Size(w * 0.48f, w * 0.05f))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - w * 0.24f, center.y + w * 0.04f), size = Size(w * 0.48f, w * 0.05f))
}

private fun DrawScope.drawMedianGap(center: Offset, w: Float) {
    val path = Path().apply {
        moveTo(center.x + w * 0.10f, center.y + w * 0.16f)
        lineTo(center.x + w * 0.10f, center.y - w * 0.04f)
        arcTo(
            rect = Rect(center.x - w * 0.14f, center.y - w * 0.18f, center.x + w * 0.14f, center.y + w * 0.06f),
            startAngleDegrees = 0f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(center.x - w * 0.10f, center.y + w * 0.16f)
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
}

private fun DrawScope.drawMiniTrafficLight(center: Offset, w: Float) {
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(center.x - w * 0.10f, center.y - w * 0.20f),
        size = Size(w * 0.20f, w * 0.40f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawCircle(color = Color(0xFFEF4444), radius = w * 0.045f, center = Offset(center.x, center.y - w * 0.11f))
    drawCircle(color = Color(0xFFFBBF24), radius = w * 0.045f, center = Offset(center.x, center.y))
    drawCircle(color = Color(0xFF22C55E), radius = w * 0.045f, center = Offset(center.x, center.y + w * 0.11f))
}

private fun DrawScope.drawSlipperyCar(center: Offset, w: Float) {
    drawCarFrontGlyph(Offset(center.x, center.y - w * 0.04f), w * 0.75f, Color(0xFF0F172A))
    val path = Path().apply {
        moveTo(center.x - w * 0.20f, center.y + w * 0.14f)
        quadraticBezierTo(center.x - w * 0.10f, center.y + w * 0.06f, center.x, center.y + w * 0.14f)
        quadraticBezierTo(center.x + w * 0.10f, center.y + w * 0.22f, center.x + w * 0.20f, center.y + w * 0.14f)
    }
    drawPath(path, color = Color(0xFF0F172A), style = Stroke(width = w * 0.05f, cap = StrokeCap.Round))
}

private fun DrawScope.drawLooseGravel(center: Offset, w: Float) {
    drawCarFrontGlyph(Offset(center.x - w * 0.05f, center.y), w * 0.65f, Color(0xFF0F172A))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.035f, center = Offset(center.x + w * 0.14f, center.y + w * 0.04f))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.030f, center = Offset(center.x + w * 0.20f, center.y - w * 0.02f))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.025f, center = Offset(center.x + w * 0.16f, center.y + w * 0.12f))
}

private fun DrawScope.drawPedestrianCrossingWarning(center: Offset, w: Float) {
    val barH = w * 0.04f
    for (i in -1..1) {
        val y = center.y + w * 0.12f + i * (w * 0.07f)
        drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - w * 0.20f, y), size = Size(w * 0.40f, barH))
    }
    drawPedestrianGlyph(Offset(center.x, center.y - w * 0.06f), w * 0.75f, Color(0xFF0F172A))
}

private fun DrawScope.drawPedestrianGlyph(center: Offset, w: Float, color: Color) {
    drawCircle(color = color, radius = w * 0.07f, center = Offset(center.x, center.y - w * 0.18f))
    drawLine(color = color, start = Offset(center.x, center.y - w * 0.10f), end = Offset(center.x, center.y + w * 0.05f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    drawLine(color = color, start = Offset(center.x, center.y + w * 0.05f), end = Offset(center.x - w * 0.10f, center.y + w * 0.20f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
    drawLine(color = color, start = Offset(center.x, center.y + w * 0.05f), end = Offset(center.x + w * 0.10f, center.y + w * 0.20f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
    drawLine(color = color, start = Offset(center.x - w * 0.10f, center.y - w * 0.02f), end = Offset(center.x + w * 0.10f, center.y), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
}

private fun DrawScope.drawSchoolChildren(center: Offset, w: Float) {
    drawPedestrianGlyph(Offset(center.x - w * 0.08f, center.y - w * 0.02f), w * 0.75f, Color(0xFF0F172A))
    drawPedestrianGlyph(Offset(center.x + w * 0.12f, center.y + w * 0.06f), w * 0.50f, Color(0xFF0F172A))
}

private fun DrawScope.drawWorkerWithShovel(center: Offset, w: Float) {
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.06f, center = Offset(center.x - w * 0.06f, center.y - w * 0.16f))
    val body = Path().apply {
        moveTo(center.x - w * 0.06f, center.y - w * 0.10f)
        lineTo(center.x, center.y + w * 0.05f)
        lineTo(center.x - w * 0.10f, center.y + w * 0.18f)
        moveTo(center.x, center.y + w * 0.05f)
        lineTo(center.x + w * 0.08f, center.y + w * 0.18f)
    }
    drawPath(body, color = Color(0xFF0F172A), style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))
    drawLine(color = Color(0xFF0F172A), start = Offset(center.x - w * 0.08f, center.y - w * 0.02f), end = Offset(center.x + w * 0.16f, center.y + w * 0.16f), strokeWidth = w * 0.04f)
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.045f, center = Offset(center.x + w * 0.16f, center.y + w * 0.16f))
}

private fun DrawScope.drawBicycleGlyph(center: Offset, w: Float, color: Color) {
    val r = w * 0.09f
    drawCircle(color = color, radius = r, center = Offset(center.x - w * 0.16f, center.y + w * 0.08f), style = Stroke(width = w * 0.035f))
    drawCircle(color = color, radius = r, center = Offset(center.x + w * 0.16f, center.y + w * 0.08f), style = Stroke(width = w * 0.035f))

    val frame = Path().apply {
        moveTo(center.x - w * 0.16f, center.y + w * 0.08f)
        lineTo(center.x, center.y + w * 0.08f)
        lineTo(center.x + w * 0.09f, center.y - w * 0.07f)
        lineTo(center.x - w * 0.07f, center.y - w * 0.07f)
        lineTo(center.x - w * 0.16f, center.y + w * 0.08f)
        moveTo(center.x, center.y + w * 0.08f)
        lineTo(center.x - w * 0.07f, center.y - w * 0.07f)
        moveTo(center.x + w * 0.16f, center.y + w * 0.08f)
        lineTo(center.x + w * 0.09f, center.y - w * 0.07f)
    }
    drawPath(frame, color = color, style = Stroke(width = w * 0.035f, cap = StrokeCap.Round))
}

private fun DrawScope.drawMotorcycleGlyph(center: Offset, w: Float, color: Color) {
    drawBicycleGlyph(center, w * 0.95f, color)
    drawRect(color = color, topLeft = Offset(center.x - w * 0.06f, center.y), size = Size(w * 0.12f, w * 0.08f))
}

private fun DrawScope.drawCarFrontGlyph(center: Offset, w: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w * 0.15f, center.y - w * 0.14f),
        size = Size(w * 0.30f, w * 0.15f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w * 0.20f, center.y - w * 0.02f),
        size = Size(w * 0.40f, w * 0.15f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawCircle(color = Color.White, radius = w * 0.03f, center = Offset(center.x - w * 0.14f, center.y + w * 0.04f))
    drawCircle(color = Color.White, radius = w * 0.03f, center = Offset(center.x + w * 0.14f, center.y + w * 0.04f))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - w * 0.18f, center.y + w * 0.11f), size = Size(w * 0.06f, w * 0.04f))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x + w * 0.12f, center.y + w * 0.11f), size = Size(w * 0.06f, w * 0.04f))
}

private fun DrawScope.drawTruckGlyph(center: Offset, w: Float, color: Color) {
    drawRect(color = color, topLeft = Offset(center.x - w * 0.22f, center.y - w * 0.11f), size = Size(w * 0.28f, w * 0.19f))
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x + w * 0.07f, center.y - w * 0.07f),
        size = Size(w * 0.15f, w * 0.15f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.045f, center = Offset(center.x - w * 0.12f, center.y + w * 0.11f))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.045f, center = Offset(center.x + w * 0.14f, center.y + w * 0.11f))
}

private fun DrawScope.drawCattleSilhouette(center: Offset, w: Float) {
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(center.x - w * 0.16f, center.y - w * 0.07f),
        size = Size(w * 0.32f, w * 0.15f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.055f, center = Offset(center.x + w * 0.16f, center.y - w * 0.07f))
    drawLine(color = Color(0xFF0F172A), start = Offset(center.x - w * 0.10f, center.y + w * 0.08f), end = Offset(center.x - w * 0.10f, center.y + w * 0.18f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF0F172A), start = Offset(center.x + w * 0.10f, center.y + w * 0.08f), end = Offset(center.x + w * 0.10f, center.y + w * 0.18f), strokeWidth = w * 0.04f)
}

private fun DrawScope.drawFuelPump(center: Offset, w: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w * 0.15f, center.y - w * 0.18f),
        size = Size(w * 0.22f, w * 0.36f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRect(color = Color(0xFF0284C7), topLeft = Offset(center.x - w * 0.11f, center.y - w * 0.13f), size = Size(w * 0.14f, w * 0.09f))
    val hose = Path().apply {
        moveTo(center.x + w * 0.07f, center.y - w * 0.07f)
        lineTo(center.x + w * 0.16f, center.y - w * 0.07f)
        lineTo(center.x + w * 0.16f, center.y + w * 0.10f)
        lineTo(center.x + w * 0.11f, center.y + w * 0.05f)
    }
    drawPath(hose, color = color, style = Stroke(width = w * 0.04f, cap = StrokeCap.Round))
}

private fun DrawScope.drawTelephone(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - w * 0.14f, center.y + w * 0.07f)
        quadraticBezierTo(center.x - w * 0.16f, center.y - w * 0.14f, center.x + w * 0.07f, center.y - w * 0.16f)
        lineTo(center.x + w * 0.14f, center.y - w * 0.09f)
        lineTo(center.x + w * 0.07f, center.y - w * 0.04f)
        lineTo(center.x + w * 0.02f, center.y - w * 0.07f)
        quadraticBezierTo(center.x - w * 0.05f, center.y, center.x - w * 0.03f, center.y + w * 0.05f)
        lineTo(center.x - w * 0.09f, center.y + w * 0.12f)
        close()
    }
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawForkAndKnife(center: Offset, w: Float, color: Color) {
    val forkX = center.x - w * 0.10f
    drawLine(color = color, start = Offset(forkX, center.y - w * 0.16f), end = Offset(forkX, center.y + w * 0.16f), strokeWidth = w * 0.04f)
    drawLine(color = color, start = Offset(forkX - w * 0.05f, center.y - w * 0.16f), end = Offset(forkX - w * 0.05f, center.y - w * 0.05f), strokeWidth = w * 0.03f)
    drawLine(color = color, start = Offset(forkX + w * 0.05f, center.y - w * 0.16f), end = Offset(forkX + w * 0.05f, center.y - w * 0.05f), strokeWidth = w * 0.03f)

    val knifeX = center.x + w * 0.10f
    drawLine(color = color, start = Offset(knifeX, center.y - w * 0.16f), end = Offset(knifeX, center.y + w * 0.16f), strokeWidth = w * 0.04f)
}

private fun DrawScope.drawCoffeeCup(center: Offset, w: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w * 0.14f, center.y - w * 0.07f),
        size = Size(w * 0.25f, w * 0.18f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawLine(color = color, start = Offset(center.x - w * 0.20f, center.y + w * 0.14f), end = Offset(center.x + w * 0.20f, center.y + w * 0.14f), strokeWidth = w * 0.04f, cap = StrokeCap.Round)
}

private fun DrawScope.drawBedRest(center: Offset, w: Float, color: Color) {
    drawLine(color = color, start = Offset(center.x - w * 0.20f, center.y + w * 0.09f), end = Offset(center.x + w * 0.20f, center.y + w * 0.09f), strokeWidth = w * 0.05f)
    drawLine(color = color, start = Offset(center.x - w * 0.20f, center.y - w * 0.05f), end = Offset(center.x - w * 0.20f, center.y + w * 0.16f), strokeWidth = w * 0.05f)
    drawCircle(color = color, radius = w * 0.055f, center = Offset(center.x - w * 0.09f, center.y - w * 0.02f))
}

private fun DrawScope.drawBusFront(center: Offset, w: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w * 0.16f, center.y - w * 0.18f),
        size = Size(w * 0.32f, w * 0.34f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRect(color = Color(0xFF0284C7), topLeft = Offset(center.x - w * 0.13f, center.y - w * 0.15f), size = Size(w * 0.26f, w * 0.12f))
    drawCircle(color = Color(0xFFFACC15), radius = w * 0.028f, center = Offset(center.x - w * 0.10f, center.y + w * 0.07f))
    drawCircle(color = Color(0xFFFACC15), radius = w * 0.028f, center = Offset(center.x + w * 0.10f, center.y + w * 0.07f))
}

private fun DrawScope.drawTrainFront(center: Offset, w: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - w * 0.16f, center.y - w * 0.18f),
        size = Size(w * 0.32f, w * 0.34f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRect(color = Color(0xFF0284C7), topLeft = Offset(center.x - w * 0.12f, center.y - w * 0.13f), size = Size(w * 0.24f, w * 0.11f))
    drawCircle(color = Color(0xFFEF4444), radius = w * 0.03f, center = Offset(center.x, center.y + w * 0.06f))
}

private fun DrawScope.drawAirplane(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - w * 0.20f)
        lineTo(center.x + w * 0.04f, center.y - w * 0.05f)
        lineTo(center.x + w * 0.20f, center.y + w * 0.04f)
        lineTo(center.x + w * 0.04f, center.y + w * 0.06f)
        lineTo(center.x + w * 0.04f, center.y + w * 0.14f)
        lineTo(center.x + w * 0.10f, center.y + w * 0.20f)
        lineTo(center.x - w * 0.10f, center.y + w * 0.20f)
        lineTo(center.x - w * 0.04f, center.y + w * 0.14f)
        lineTo(center.x - w * 0.04f, center.y + w * 0.06f)
        lineTo(center.x - w * 0.20f, center.y + w * 0.04f)
        lineTo(center.x - w * 0.04f, center.y - w * 0.05f)
        close()
    }
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawPoliceBadge(center: Offset, w: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - w * 0.18f)
        lineTo(center.x + w * 0.16f, center.y - w * 0.09f)
        lineTo(center.x + w * 0.12f, center.y + w * 0.09f)
        lineTo(center.x, center.y + w * 0.20f)
        lineTo(center.x - w * 0.12f, center.y + w * 0.09f)
        lineTo(center.x - w * 0.16f, center.y - w * 0.09f)
        close()
    }
    drawPath(path, color = color, style = Stroke(width = w * 0.05f))
    drawCircle(color = color, radius = w * 0.035f, center = center)
}

private fun DrawScope.drawDeadEndT(center: Offset, w: Float) {
    val l = w * 0.36f
    val t = w * 0.10f
    drawRect(color = Color.White, topLeft = Offset(center.x - t / 2f, center.y - l / 2f), size = Size(t, l))
    drawRect(color = Color(0xFFDC2626), topLeft = Offset(center.x - l / 2f, center.y - l / 2f), size = Size(l, t))
}

private fun DrawScope.drawDeadEndSide(center: Offset, w: Float) {
    val l = w * 0.36f
    val t = w * 0.09f
    drawRect(color = Color.White, topLeft = Offset(center.x - t / 2f, center.y - l / 2f), size = Size(t, l))
    drawRect(color = Color.White, topLeft = Offset(center.x, center.y - t / 2f), size = Size(l * 0.40f, t))
    drawRect(color = Color(0xFFDC2626), topLeft = Offset(center.x + l * 0.40f - t, center.y - l * 0.18f), size = Size(t, l * 0.36f))
}

private fun DrawScope.drawHighwayBridge(center: Offset, w: Float, color: Color) {
    drawLine(color = color, start = Offset(center.x - w * 0.12f, center.y - w * 0.16f), end = Offset(center.x - w * 0.12f, center.y + w * 0.16f), strokeWidth = w * 0.05f)
    drawLine(color = color, start = Offset(center.x + w * 0.12f, center.y - w * 0.16f), end = Offset(center.x + w * 0.12f, center.y + w * 0.16f), strokeWidth = w * 0.05f)
    drawRect(color = color, topLeft = Offset(center.x - w * 0.20f, center.y - w * 0.04f), size = Size(w * 0.40f, w * 0.08f))
}

private fun DrawScope.drawCrossPlus(center: Offset, w: Float, color: Color, inWhiteBox: Boolean) {
    if (inWhiteBox) {
        val boxSize = w * 0.48f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(center.x - boxSize / 2f, center.y - boxSize / 2f),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }
    val armL = w * 0.30f
    val armT = w * 0.10f
    drawRect(color = color, topLeft = Offset(center.x - armL / 2f, center.y - armT / 2f), size = Size(armL, armT))
    drawRect(color = color, topLeft = Offset(center.x - armT / 2f, center.y - armL / 2f), size = Size(armT, armL))
}

private fun DrawScope.drawPoliceOfficerGesture(center: Offset, w: Float, signKey: String) {
    val white = Color.White
    drawCircle(color = white, radius = w * 0.07f, center = Offset(center.x, center.y - w * 0.18f))
    drawRect(color = Color(0xFF38BDF8), topLeft = Offset(center.x - w * 0.08f, center.y - w * 0.23f), size = Size(w * 0.16f, w * 0.04f))
    drawLine(color = white, start = Offset(center.x, center.y - w * 0.11f), end = Offset(center.x, center.y + w * 0.08f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)

    when (signKey) {
        "l_police_stop_front" -> {
            drawLine(color = white, start = Offset(center.x, center.y - w * 0.04f), end = Offset(center.x + w * 0.18f, center.y - w * 0.14f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
            drawCircle(color = Color(0xFFEF4444), radius = w * 0.05f, center = Offset(center.x + w * 0.18f, center.y - w * 0.14f))
        }
        "l_police_stop_back" -> {
            drawLine(color = white, start = Offset(center.x, center.y - w * 0.04f), end = Offset(center.x - w * 0.20f, center.y - w * 0.04f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
            drawCircle(color = Color(0xFFEF4444), radius = w * 0.05f, center = Offset(center.x - w * 0.20f, center.y - w * 0.04f))
        }
        else -> {
            drawLine(color = white, start = Offset(center.x, center.y - w * 0.04f), end = Offset(center.x + w * 0.18f, center.y - w * 0.02f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
            drawLine(color = white, start = Offset(center.x, center.y - w * 0.04f), end = Offset(center.x - w * 0.14f, center.y + w * 0.10f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawGenericProhibition(center: Offset, w: Float) {
    drawCircle(color = Color(0xFFDC2626), radius = w * 0.10f, center = center)
}

private fun DrawScope.drawGenericWarning(center: Offset, w: Float) {
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(center.x - w * 0.04f, center.y - w * 0.14f), size = Size(w * 0.08f, w * 0.18f))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.04f, center = Offset(center.x, center.y + w * 0.12f))
}
