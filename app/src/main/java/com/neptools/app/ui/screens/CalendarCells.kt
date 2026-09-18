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
import com.neptools.app.core.calendar.CalendarCell
import com.neptools.app.ui.components.npNum

@Composable
internal fun NavArrow(glyph: String, enabled: Boolean, onClick: () -> Unit) {
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
    val hasFest = cell.festivals.isNotEmpty()
    val isToday = cell.isToday
    val adDay = cell.adDate.dayOfMonth

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
                    hasFest -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                MaterialTheme.shapes.small
            )
            .padding(vertical = 3.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                npNum(cell.dayOfMonth),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = when {
                    isToday -> MaterialTheme.colorScheme.onTertiary
                    hasFest || cell.isSaturday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(Modifier.height(1.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    adDay.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.85f)
                        hasFest -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        cell.isSaturday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    }
                )
                if (hasFest && !isToday) {
                    Spacer(Modifier.width(3.dp))
                    Box(
                        Modifier
                            .size(4.dp)
                            .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                    )
                }
                if (cell.hasUserEvent && !isToday) {
                    Spacer(Modifier.width(2.dp))
                    Box(
                        Modifier
                            .size(4.dp)
                            .background(MaterialTheme.colorScheme.secondary, androidx.compose.foundation.shape.CircleShape)
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
        TagChip(if (publicHoliday) (if (isEn) "Holiday" else "बिदा") else (if (isEn) "Festival" else "पर्व"))
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        Modifier
            .background(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                MaterialTheme.shapes.extraSmall
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary)
    }
}
