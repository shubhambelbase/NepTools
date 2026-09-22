package com.neptools.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun npNum(value: Int): String =
    if (ThemePrefs.nepaliDigits.value) NepaliNames.toDevanagari(value) else value.toString()

@Composable
fun npNum(value: Long): String =
    if (ThemePrefs.nepaliDigits.value) NepaliNames.toDevanagari(value.toString()) else value.toString()

@Composable
fun HairLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            Modifier
                .weight(1f)
                .padding(start = 10.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier
            .shadow(2.dp, shape)
            .border(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SectionTitle(
    title: String,
    subtitleEn: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.15).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitleEn != null) {
                Text(
                    text = subtitleEn,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onAction).padding(4.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ToolTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val interaction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(bounded = true, radius = 18.dp),
                    onClick = onBack
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.neptools.app.ui.icons.PIcons.ChevronLeft,
                contentDescription = if (ThemePrefs.lang.value == "en") "Back" else "फिर्ता",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 16.5.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        if (actions != null) {
            Spacer(Modifier.width(8.dp))
            actions()
        }
    }
}

@Composable
fun LatticeBand(modifier: Modifier = Modifier) {
    // Clean transparent spacing without harsh grey gradient line
    Spacer(modifier.height(4.dp))
}

@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scaleTarget = if (pressed) 0.985f else 1f
    val lift by animateFloatAsState(scaleTarget, label = "inkBtn")
    val shape = MaterialTheme.shapes.medium
    val bg = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier
            .alpha(lift)
            .background(bg, shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(width = 9.dp, height = 0.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = fg)
        }
    }
}

@Composable
fun StampBadge(
    textTop: String,
    textBottom: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(58.dp)
            .rotate(4f)
            .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f), MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(textTop, style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary)
            Text(textBottom, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun Meter(score: Int, modifier: Modifier = Modifier, tint: Color? = null) {
    val animated by animateFloatAsState(score / 100f, label = "meter")
    val fill = tint ?: when {
        score >= 65 -> MaterialTheme.colorScheme.secondary
        score <= 45 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier
            .height(6.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction = animated.coerceIn(0.02f, 1f))
                .height(6.dp)
                .background(fill, RoundedCornerShape(3.dp))
        )
    }
}

@Composable
fun PulsingLoader(label: String, modifier: Modifier = Modifier) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(46.dp)
                .alpha(alpha)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
        )
        Spacer(Modifier.height(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun IconTile(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    locked: Boolean = false
) {
    val alpha = if (locked) 0.55f else 1f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null && !locked) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tileScale"
    )

    Column(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .height(94.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .then(
                if (onClick != null && !locked) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(
                            bounded = true,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (locked) MaterialTheme.colorScheme.onSurfaceVariant
                       else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 13.sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
