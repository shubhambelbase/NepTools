package com.neptools.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.updates.RecentUpdateRecord
import com.neptools.app.core.updates.RecentUpdatesManager
import com.neptools.app.core.updates.RelativeTimeFormatter
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.delay

@Composable
fun RecentUpdatesScreen(
    onBack: () -> Unit,
    onOpenTool: (String) -> Unit
) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    LaunchedEffect(Unit) {
        RecentUpdatesManager.load(context)
        RecentUpdatesManager.markAllSeen(context)
    }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val updateList = RecentUpdatesManager.updates

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        ToolTopBar(
            title = if (isEn) "Recent Updates" else "हालै अद्यावधिक",
            subtitle = if (isEn) "Live data freshness & sync log" else "सजीव डाटा अद्यावधिक विवरण",
            onBack = onBack,
            actions = if (updateList.isNotEmpty()) {
                {
                    Box(
                        Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isEn) "${updateList.size} updates" else "${NepaliNames.toDevanagari(updateList.size)} अद्यावधिक",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else null
        )

        if (updateList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PIcons.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = if (isEn) "No recent updates" else "हालैका अद्यावधिक छैनन्",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEn)
                            "Live data services will record timestamps here once fresh data is fetched and verified."
                        else
                            "नयाँ डाटा प्राप्त र प्रमाणित भएपछि यहाँ अद्यावधिक विवरण देखिनेछ।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(updateList, key = { it.serviceId }) { item ->
                    RecentUpdateDetailCard(
                        item = item,
                        isEn = isEn,
                        nowMillis = nowMillis,
                        onClick = { onOpenTool(item.route) }
                    )
                }
                item {
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentUpdateDetailCard(
    item: RecentUpdateRecord,
    isEn: Boolean,
    nowMillis: Long,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "updateDetailCardScale"
    )

    val icon: ImageVector = when (item.iconType) {
        "sun" -> PIcons.Sun
        "fuel" -> PIcons.Fuel
        "leaf" -> PIcons.Leaf
        "coin" -> PIcons.Coin
        else -> PIcons.Refresh
    }

    val (iconTint, iconBg) = when (item.iconType) {
        "sun" -> Color(0xFF0284C7) to Color(0xFFE0F2FE)
        "fuel" -> Color(0xFFEA580C) to Color(0xFFFFEDD5)
        "leaf" -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
        "coin" -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
    }

    val relativeTime = remember(item.timestampMillis, isEn, nowMillis) {
        RelativeTimeFormatter.format(item.timestampMillis, isEn, nowMillis)
    }

    val exactTime = remember(item.timestampMillis, isEn) {
        RelativeTimeFormatter.formatExact(item.timestampMillis, isEn)
    }

    val statusText = if (isEn) item.statusEn else item.statusNp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = iconTint.copy(alpha = 0.15f)),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 0.dp else 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isEn) item.nameEn else item.nameNp,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isUnread) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = relativeTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )

                if (!statusText.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$statusText · $exactTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = PIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
