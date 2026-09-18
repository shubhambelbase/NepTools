package com.neptools.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neptools.app.ui.icons.PIcons
import kotlinx.coroutines.launch

/**
 * Premium Animated Refresh Icon Button with smooth infinite spinning when refreshing,
 * and snappy 360° spring/tween rotation when tapped.
 */
@Composable
fun AnimatedRefreshIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.primary,
    iconSize: Dp = 22.dp,
    icon: ImageVector = PIcons.Refresh,
    contentDescription: String? = "Refresh",
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val manualRotation = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "infinite_refresh_spin")
    val infiniteRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    val currentRotation = if (isRefreshing) {
        infiniteRotation
    } else {
        manualRotation.value
    }

    IconButton(
        onClick = {
            if (!isRefreshing && enabled) {
                scope.launch {
                    manualRotation.snapTo(0f)
                    manualRotation.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                    )
                }
                onClick()
            }
        },
        enabled = enabled && !isRefreshing,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.38f),
            modifier = Modifier
                .size(iconSize)
                .rotate(currentRotation)
        )
    }
}

/**
 * Modifier extension to spin any composable icon on refresh or click.
 */
@Composable
fun rememberRefreshRotation(isRefreshing: Boolean): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val infiniteRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )
    return if (isRefreshing) infiniteRotation else 0f
}
