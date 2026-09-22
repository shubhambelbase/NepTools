package com.neptools.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.ripple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.neptools.app.core.calendar.CalendarCell
import com.neptools.app.ui.components.npNum

@Composable
internal fun NavArrow(
    glyph: String,
    contentDescription: String? = null,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navArrowScale"
    )

    Box(
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(38.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.small
            )
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            glyph,
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun DayCell(cell: CalendarCell, onOpenDay: (Int, Int, Int) -> Unit) {
    val isHoliday = cell.isHoliday
    val hasFest = cell.festivals.isNotEmpty()
    val isToday = cell.isToday
    val adDay = cell.adDate.dayOfMonth
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
    val tithiDisplay = if (isEn) cell.tithiNameEn.ifBlank { cell.tithiName } else cell.tithiName

    val cellDescription = if (isEn) {
        "${cell.dayOfMonth} ${com.neptools.app.core.calendar.NepaliNames.monthsEn[cell.nepaliDate.month - 1]}${if (isToday) ", Today" else ""}${if (tithiDisplay.isNotBlank()) ", $tithiDisplay" else ""}${if (hasFest) ", ${cell.festivals.firstOrNull()?.nameEn ?: ""}" else ""}${if (isHoliday) ", Public Holiday" else ""}"
    } else {
        "${npNum(cell.dayOfMonth)} ${com.neptools.app.core.calendar.NepaliNames.monthsNp[cell.nepaliDate.month - 1]}${if (isToday) ", आज" else ""}${if (tithiDisplay.isNotBlank()) ", $tithiDisplay" else ""}${if (hasFest) ", ${cell.festivals.firstOrNull()?.nameNp ?: ""}" else ""}${if (isHoliday) ", सार्वजनिक बिदा" else ""}"
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dayCellScale"
    )

    Box(
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxSize()
            .semantics(mergeDescendants = true) {
                this.contentDescription = cellDescription
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = if (isToday) MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) { onOpenDay(cell.nepaliDate.year, cell.nepaliDate.month, cell.dayOfMonth) }
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.tertiary
                    isHoliday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else -> MaterialTheme.colorScheme.surface
                },
                MaterialTheme.shapes.small
            )
            .padding(vertical = 2.dp, horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(horizontal = 1.dp, vertical = 2.dp)
        ) {
            Text(
                npNum(cell.dayOfMonth),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = when {
                    isToday -> MaterialTheme.colorScheme.onTertiary
                    isHoliday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (tithiDisplay.isNotBlank()) {
                Text(
                    tithiDisplay,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = if (isHoliday) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.9f)
                        isHoliday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    }
                )
            }
            Spacer(Modifier.height(1.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    if (isEn) adDay.toString() else com.neptools.app.core.calendar.NepaliNames.toDevanagari(adDay),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.85f)
                        isHoliday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    }
                )
                if (hasFest && !isToday) {
                    Spacer(Modifier.width(2.5.dp))
                    Box(
                        Modifier
                            .size(3.5.dp)
                            .background(
                                if (isHoliday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary,
                                androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
                if (cell.hasUserEvent && !isToday) {
                    Spacer(Modifier.width(2.dp))
                    Box(
                        Modifier
                            .size(3.5.dp)
                            .background(
                                MaterialTheme.colorScheme.tertiary,
                                androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
internal fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).background(color, MaterialTheme.shapes.extraSmall))
        Spacer(Modifier.size(width = 6.dp, height = 0.dp))
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun FestivalRow(day: Int, nameNp: String, nameEn: String, publicHoliday: Boolean, onOpenDay: () -> Unit) {
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
    val primaryName = if (isEn) nameEn.ifBlank { nameNp } else nameNp
    val secondaryName = if (isEn) nameNp else nameEn

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "festRowScale"
    )

    Row(
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                onClick = onOpenDay
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(width = 42.dp, height = 42.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(npNum(day), style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.size(width = 14.dp, height = 0.dp))
        Column(Modifier.weight(1f)) {
            Text(primaryName, style = MaterialTheme.typography.titleMedium)
            if (secondaryName.isNotBlank() && secondaryName != primaryName) {
                Text(secondaryName, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TagChip(
            text = if (publicHoliday) (if (isEn) "Public Holiday" else "सार्वजनिक बिदा") else (if (isEn) "Festival" else "पर्व"),
            isHoliday = publicHoliday
        )
    }
}

@Composable
private fun TagChip(text: String, isHoliday: Boolean = false) {
    val bg = if (isHoliday) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    val fg = if (isHoliday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    Box(
        Modifier
            .background(bg, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}
