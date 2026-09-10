package com.neptools.app.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.theme.ThemePrefs

import androidx.compose.runtime.collectAsState
import com.neptools.app.core.radio.RadioManager
import com.neptools.app.core.radio.RadioStation
import com.neptools.app.core.radio.RadioStations

val stations = RadioStations.all

@Composable
fun RadioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    val playerState by RadioManager.state.collectAsState()
    val activeStation = playerState.activeStation
    val isPlaying = playerState.isPlaying
    val isBuffering = playerState.isBuffering

    var selectedCategory by remember { mutableStateOf("all") }

    fun playStation(station: RadioStation) {
        if (activeStation?.id == station.id && isPlaying) {
            RadioManager.togglePlayPause(context)
        } else {
            RadioManager.play(context, station)
        }
    }

    fun togglePlayPause() {
        if (activeStation == null) {
            RadioManager.play(context, stations.first())
        } else {
            RadioManager.togglePlayPause(context)
        }
    }

    val categories = listOf(
        "all" to T("radio_cat_all"),
        "national" to T("radio_cat_nat"),
        "news" to T("radio_cat_news"),
        "music" to T("radio_cat_mus")
    )

    val filteredStations = remember(selectedCategory) {
        if (selectedCategory == "all") stations
        else stations.filter { it.category == selectedCategory }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        Row(
            Modifier
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
                Icon(PIcons.ChevronLeft, "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    T("radio_title"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                Modifier
                    .background(
                        if (isPlaying) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPlaying) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(Color(0xFF2E7D32), CircleShape)
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        if (isPlaying) "ON AIR" else "FM LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPlaying) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Category Filter Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (key, label) ->
                val sel = selectedCategory == key
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = key }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Box(Modifier.fillMaxSize()) {
            // Stations List (clean without top duplicate)
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = if (activeStation != null) 140.dp else 90.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredStations) { station ->
                    val isCurrent = activeStation == station
                    StationCard(
                        station = station,
                        isEn = isEn,
                        isActive = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        isBuffering = isCurrent && isBuffering,
                        onClick = { playStation(station) }
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        T("radio_note"),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Floating Bottom Now-Playing Dock
            androidx.compose.animation.AnimatedVisibility(
                visible = activeStation != null,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
            ) {
                activeStation?.let { st ->
                    NowPlayingDock(
                        station = st,
                        isEn = isEn,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        onTogglePlayPause = { togglePlayPause() },
                        onStop = {
                            RadioManager.stop(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingDock(
    station: RadioStation,
    isEn: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable(onClick = onTogglePlayPause)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visualizer Icon Box
            Box(
                Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    BufferingPulseIndicator(color = MaterialTheme.colorScheme.primary, sizeDp = 22)
                } else if (isPlaying) {
                    AudioEqualizerWaveform(color = MaterialTheme.colorScheme.primary, isPlaying = true)
                } else {
                    Icon(
                        PIcons.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Station Title & Status
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) station.nameEn else station.nameNp,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPlaying) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(Color(0xFFE53935), CircleShape)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "LIVE · ${station.frequency}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF2E7D32)
                        )
                    } else if (isBuffering) {
                        Text(
                            if (isEn) "Connecting stream..." else "स्ट्रिम जोड्दैछ...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            if (isEn) "Paused · Tap to resume" else "रोकिएको · सुरु गर्न थिच्नुहोस्",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Play / Pause Circle Button
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onTogglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    BufferingPulseIndicator(color = MaterialTheme.colorScheme.onPrimary, sizeDp = 20)
                } else {
                    Icon(
                        if (isPlaying) PIcons.Pause else PIcons.Play,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StationCard(
    station: RadioStation,
    isEn: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                if (isActive) 1.5.dp else 1.dp,
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon / Equalizer Box
        Box(
            Modifier
                .size(44.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && isBuffering) {
                BufferingPulseIndicator(color = MaterialTheme.colorScheme.primary, sizeDp = 20)
            } else if (isActive && isPlaying) {
                AudioEqualizerWaveform(color = MaterialTheme.colorScheme.primary, isPlaying = true)
            } else {
                Icon(
                    PIcons.Radio,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Station Details
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isEn) station.nameEn else station.nameNp,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive && isPlaying) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "${station.frequency} · ${station.location}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(10.dp))

        // Play / Pause Circle
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isActive && isPlaying) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && isBuffering) {
                BufferingPulseIndicator(color = MaterialTheme.colorScheme.onPrimary, sizeDp = 18)
            } else {
                Icon(
                    if (isActive && isPlaying) PIcons.Pause else PIcons.Play,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AudioEqualizerWaveform(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    isPlaying: Boolean = true
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "Equalizer")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "b1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.30f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "b2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(510, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "b3"
    )
    val bar4Height by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 0.20f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(460, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "b4"
    )

    Row(
        modifier = modifier.size(width = 22.dp, height = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val heights = if (isPlaying) listOf(bar1Height, bar2Height, bar3Height, bar4Height)
                      else listOf(0.4f, 0.4f, 0.4f, 0.4f)
        heights.forEach { fraction ->
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight(fraction)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun BufferingPulseIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    sizeDp: Int = 20
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "Buffering")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(850, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "rot"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .size(sizeDp.dp)
            .padding(2.dp)
    ) {
        drawArc(
            color = color.copy(alpha = 0.25f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
        )
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = 100f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}
