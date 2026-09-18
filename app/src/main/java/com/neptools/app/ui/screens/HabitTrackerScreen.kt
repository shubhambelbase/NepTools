package com.neptools.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.core.habit.DayHeatmapCell
import com.neptools.app.core.habit.Habit
import com.neptools.app.core.habit.HabitLog
import com.neptools.app.core.habit.HabitRepository
import com.neptools.app.core.habit.HabitStats
import com.neptools.app.core.habit.HabitType
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.launch
import java.time.LocalDate

private val COLOR_PALETTE = listOf(
    0xFFC73E2E, // Vermillion Red
    0xFF16697A, // Teal Ink
    0xFF4F46E5, // Royal Indigo
    0xFFD97706, // Golden Amber
    0xFF16A34A, // Forest Green
    0xFF9333EA, // Deep Violet
    0xFF0284C7, // Sky Blue
    0xFFEA580C  // Sunset Orange
)

private val CATEGORY_ICON_TAGS = listOf(
    "WALK", "WATER", "READ", "ZEN", "DIET", "FOCUS", "FIT", "RUN",
    "WORK", "STUDY", "CASH", "REST", "GOAL", "TIMER", "HEALTH", "TASK"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEn = ThemePrefs.lang.value == "en"
    val repo = remember { HabitRepository.get(context) }

    var habits by remember { mutableStateOf(repo.getHabits()) }
    var selectedHabitFilter by remember { mutableStateOf<String?>(null) } // null = All Habits
    var isBsMode by remember { mutableStateOf(true) }

    val todayAd = remember { LocalDate.now() }
    val todayIso = remember { todayAd.toString() }
    val engine = PatroRepo.d.engine
    val todayBs = remember {
        try {
            engine.adToBs(todayAd)
        } catch (e: Exception) {
            NepaliDate(2083, 5, 14)
        }
    }

    var currentYear by remember { mutableIntStateOf(if (isBsMode) todayBs.year else todayAd.year) }

    // Sync year when switching BS / AD
    LaunchedEffect(isBsMode) {
        currentYear = if (isBsMode) todayBs.year else todayAd.year
    }

    // Refresh trigger
    var updateTick by remember { mutableIntStateOf(0) }

    // Computed stats and heatmap
    val stats by remember(updateTick, selectedHabitFilter, habits) {
        derivedStateOf {
            repo.computeStats(selectedHabitFilter)
        }
    }

    val heatmapCells by remember(updateTick, isBsMode, currentYear, selectedHabitFilter, habits) {
        derivedStateOf {
            repo.buildHeatmap(isBs = isBsMode, targetYear = currentYear, habitFilterId = selectedHabitFilter)
        }
    }

    val todayLogs by remember(updateTick, habits) {
        derivedStateOf {
            repo.getLogsForDate(todayIso)
        }
    }

    // Sheet states
    var showCreateModal by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var inspectingDay by remember { mutableStateOf<DayHeatmapCell?>(null) }

    fun refresh() {
        habits = repo.getHabits()
        updateTick++
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEn) "Habit Tracker" else "बानी ट्र्याकर",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PIcons.ChevronLeft, contentDescription = "Back", modifier = Modifier.size(24.dp))
                    }
                },
                actions = {
                    // BS / AD Toggle chip
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                isBsMode = !isBsMode
                                currentYear = if (isBsMode) todayBs.year else todayAd.year
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBsMode) "विक्रम संवत्" else "AD Calendar",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingHabit = null
                    showCreateModal = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(PIcons.Plus, contentDescription = "Add Habit", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HERO STREAK & SUMMARY CARD
            item {
                HeroStreakCard(
                    stats = stats,
                    isEn = isEn,
                    onAddHabitClick = {
                        editingHabit = null
                        showCreateModal = true
                    }
                )
            }

            // 2. HABIT FILTER ROW (All vs Single habit)
            if (habits.isNotEmpty()) {
                item {
                    HabitFilterRow(
                        habits = habits,
                        selectedId = selectedHabitFilter,
                        isEn = isEn,
                        onSelect = { selectedHabitFilter = it }
                    )
                }
            }

            // 3. GITHUB-STYLE CONTRIBUTION HEATMAP MATRIX
            item {
                HeatmapMatrixCard(
                    cells = heatmapCells,
                    isBs = isBsMode,
                    year = currentYear,
                    isEn = isEn,
                    onYearChange = { currentYear = it },
                    onCellClick = { cell ->
                        inspectingDay = cell
                    }
                )
            }

            // 4. TODAY'S HABITS SECTION HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEn) "Today's Checklist" else "आजको बानी सूची",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBsMode) {
                                "${npNum(todayBs.day)} ${NepaliNames.monthsNp[todayBs.month - 1]} ${npNum(todayBs.year)}"
                            } else {
                                "${todayAd.dayOfMonth} ${todayAd.month.name.take(3)} ${todayAd.year}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Progress indicator
                    if (stats.todayTotalCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${npNum(stats.todayCompletedCount)} / ${npNum(stats.todayTotalCount)} " +
                                        (if (isEn) "Done" else "सम्पन्न"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 5. HABIT ITEMS LIST
            if (habits.isEmpty()) {
                item {
                    EmptyHabitsCard(
                        isEn = isEn,
                        onAddClick = {
                            editingHabit = null
                            showCreateModal = true
                        }
                    )
                }
            } else {
                val filteredHabits = if (selectedHabitFilter == null) habits else habits.filter { it.id == selectedHabitFilter }
                items(filteredHabits, key = { it.id }) { habit ->
                    val log = todayLogs[habit.id]
                    val isCompleted = log?.completed == true
                    val curValue = log?.value ?: 0f

                    HabitItemCard(
                        habit = habit,
                        isCompleted = isCompleted,
                        currentValue = curValue,
                        isEn = isEn,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            repo.toggleHabitToday(habit.id, todayIso)
                            refresh()
                        },
                        onUpdateValue = { newVal ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            repo.updateHabitValue(habit.id, todayIso, newVal)
                            refresh()
                        },
                        onEdit = {
                            editingHabit = habit
                            showCreateModal = true
                        },
                        onDelete = {
                            repo.deleteHabit(habit.id)
                            if (selectedHabitFilter == habit.id) selectedHabitFilter = null
                            refresh()
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(48.dp))
            }
        }
    }

    // MODAL: CREATE / EDIT HABIT
    if (showCreateModal) {
        HabitEditorBottomSheet(
            habit = editingHabit,
            isEn = isEn,
            onDismiss = { showCreateModal = false },
            onSave = { nameNp, nameEn, icon, colorHex, type, targetVal, unitNp, unitEn, freq ->
                if (editingHabit != null) {
                    val updated = editingHabit!!.copy(
                        nameNp = nameNp,
                        nameEn = nameEn,
                        icon = icon,
                        colorHex = colorHex,
                        type = type,
                        targetValue = targetVal,
                        unitNp = unitNp,
                        unitEn = unitEn,
                        frequencyDays = freq
                    )
                    repo.saveHabit(updated)
                } else {
                    repo.createNewHabit(
                        nameNp = nameNp,
                        nameEn = nameEn,
                        icon = icon,
                        colorHex = colorHex,
                        type = type,
                        targetValue = targetVal,
                        unitNp = unitNp,
                        unitEn = unitEn,
                        frequencyDays = freq
                    )
                }
                showCreateModal = false
                refresh()
            }
        )
    }

    // MODAL: DAY INSPECTION BOTTOM SHEET
    inspectingDay?.let { cell ->
        DayInspectionBottomSheet(
            cell = cell,
            habits = habits,
            isEn = isEn,
            onDismiss = { inspectingDay = null },
            onToggleHabit = { habitId ->
                repo.toggleHabitToday(habitId, cell.dateIso)
                refresh()
            }
        )
    }
}

// -----------------------------------------------------------------------------
// HERO STREAK & SUMMARY CARD
// -----------------------------------------------------------------------------
@Composable
private fun HeroStreakCard(
    stats: HabitStats,
    isEn: Boolean,
    onAddHabitClick: () -> Unit
) {
    val completionFraction = if (stats.todayTotalCount > 0) {
        stats.todayCompletedCount.toFloat() / stats.todayTotalCount
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = completionFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "todayProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flame & Streak counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFEDD5),
                                        Color(0xFFFED7AA)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(1.5.dp, Color(0xFFEA580C).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PIcons.Flame,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = npNum(stats.currentStreak),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Days Streak" else "दिनको स्ट्रिक",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFEA580C),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = if (stats.currentStreak > 0) {
                                if (isEn) "Consistency is key! Keep going." else "निरन्तरता नै सफलता हो! कायम राख्नुहोस्।"
                            } else {
                                if (isEn) "Complete a habit today to start streak!" else "आजै बानी पूरा गरी नयाँ स्ट्रिक थाल्नुहोस्!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            // 3 Quick Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricMiniItem(
                    icon = PIcons.Trophy,
                    iconTint = Color(0xFFD97706),
                    value = "${npNum(stats.longestStreak)} " + (if (isEn) "Days" else "दिन"),
                    label = if (isEn) "Best Streak" else "सबैभन्दा लामो"
                )

                MetricMiniItem(
                    icon = PIcons.TrendingUp,
                    iconTint = Color(0xFF16A34A),
                    value = "${npNum(stats.completionRateMonth)}%",
                    label = if (isEn) "Month Rate" else "मासिक दर"
                )

                MetricMiniItem(
                    icon = PIcons.Target,
                    iconTint = Color(0xFF4F46E5),
                    value = "${npNum(stats.totalActiveDays)} " + (if (isEn) "Days" else "दिन"),
                    label = if (isEn) "Active Days" else "सक्रिय दिन"
                )
            }

            if (stats.todayTotalCount > 0) {
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) "Today's Goal Progress" else "आजको लक्ष्य प्रगति",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (animatedProgress >= 1f) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricMiniItem(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -----------------------------------------------------------------------------
// HABIT FILTER ROW (HORIZONTAL CHIPS)
// -----------------------------------------------------------------------------
@Composable
private fun HabitFilterRow(
    habits: List<Habit>,
    selectedId: String?,
    isEn: Boolean,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = {
                    Text(
                        text = if (isEn) "All Habits (Combined)" else "सबै बानीहरू (संयुक्त)",
                        fontWeight = if (selectedId == null) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(PIcons.Grid, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        items(habits) { habit ->
            val isSelected = selectedId == habit.id
            val habitColor = Color(habit.colorHex)

            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) null else habit.id) },
                label = {
                    Text(
                        text = if (isEn) habit.nameEn else habit.nameNp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Text(
                        text = habit.icon.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = habitColor
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = habitColor.copy(alpha = 0.18f),
                    selectedLabelColor = habitColor
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------
// GITHUB-STYLE CONTRIBUTION HEATMAP MATRIX
// -----------------------------------------------------------------------------
@Composable
private fun HeatmapMatrixCard(
    cells: List<DayHeatmapCell>,
    isBs: Boolean,
    year: Int,
    isEn: Boolean,
    onYearChange: (Int) -> Unit,
    onCellClick: (DayHeatmapCell) -> Unit
) {
    val scrollState = rememberScrollState()

    // Group cells into 7 rows (by weekday 0..6)
    // There are ~53 columns
    val columns = remember(cells) {
        if (cells.isEmpty()) return@remember emptyList<List<DayHeatmapCell?>>()
        val cols = mutableListOf<MutableList<DayHeatmapCell?>>()
        var curCol = MutableList<DayHeatmapCell?>(7) { null }

        cells.forEach { cell ->
            val wd = cell.weekdayIndex.coerceIn(0, 6)
            if (wd == 0 && curCol.any { it != null }) {
                cols.add(curCol)
                curCol = MutableList(7) { null }
            }
            curCol[wd] = cell
        }
        if (curCol.any { it != null }) {
            cols.add(curCol)
        }
        cols
    }

    val weekdayShort = if (isEn) {
        listOf("S", "M", "T", "W", "T", "F", "S")
    } else {
        listOf("आ", "सो", "मं", "बु", "बि", "शु", "श")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header with Year Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEn) "Annual Consistency Grid" else "वार्षिक निरन्तरता ग्रिड",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEn) "Tap any day to inspect or log" else "विवरण हेर्न कुनै पनि दिनमा छुनुहोस्",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Year selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onYearChange(year - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(PIcons.ChevronLeft, contentDescription = "Prev Year", modifier = Modifier.size(18.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${npNum(year)} " + (if (isBs) "वि.सं." else "AD"),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { onYearChange(year + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(PIcons.ChevronRight, contentDescription = "Next Year", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // THE GRID
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Weekday Labels on Left
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 18.dp, end = 6.dp)
                ) {
                    weekdayShort.forEach { dayLabel ->
                        Box(
                            modifier = Modifier.size(13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Horizontally Scrolling Contribution Heatmap
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    Column {
                        // Month Headers Row
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            columns.forEachIndexed { colIdx, colCells ->
                                val firstCell = colCells.firstOrNull { it != null }
                                val showMonthHeader = if (firstCell != null) {
                                    if (isBs) {
                                        firstCell.bsDay <= 7 && colIdx % 4 == 0
                                    } else {
                                        firstCell.dayOfMonth <= 7 && colIdx % 4 == 0
                                    }
                                } else false

                                Box(
                                    modifier = Modifier.width(13.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (showMonthHeader && firstCell != null) {
                                        val mName = if (isBs) {
                                            if (firstCell.bsMonth in 1..12) {
                                                if (isEn) NepaliNames.monthsEn[firstCell.bsMonth - 1].take(3)
                                                else NepaliNames.monthsNp[firstCell.bsMonth - 1]
                                            } else ""
                                        } else {
                                            LocalDate.parse(firstCell.dateIso).month.name.take(3)
                                        }
                                        Text(
                                            text = mName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // 7 Weekday rows of squares
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            columns.forEach { colCells ->
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    for (dayIdx in 0..6) {
                                        val cell = colCells.getOrNull(dayIdx)
                                        HeatmapSquare(cell = cell, onClick = {
                                            if (cell != null) onCellClick(cell)
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Grid Legend (Less -> More)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) "Less" else "कम",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                listOf(0.0f, 0.25f, 0.50f, 0.75f, 1.0f).forEach { ratio ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(getHeatmapColor(ratio = ratio, isFuture = false, isToday = false))
                    )
                    Spacer(Modifier.width(2.5.dp))
                }
                Spacer(Modifier.width(2.dp))
                Text(
                    text = if (isEn) "More" else "बढी",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeatmapSquare(
    cell: DayHeatmapCell?,
    onClick: () -> Unit
) {
    if (cell == null) {
        Box(modifier = Modifier.size(13.dp))
        return
    }

    val cellColor = getHeatmapColor(
        ratio = cell.completionRatio,
        isFuture = cell.isFuture,
        isToday = cell.isToday
    )

    Box(
        modifier = Modifier
            .size(13.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(cellColor)
            .then(
                if (cell.isToday) {
                    Modifier.border(1.2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                } else Modifier
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun getHeatmapColor(ratio: Float, isFuture: Boolean, isToday: Boolean): Color {
    val emptyBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    if (isFuture) return MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    if (ratio <= 0f) return emptyBg

    val baseColor = MaterialTheme.colorScheme.primary
    return when {
        ratio >= 0.99f -> baseColor
        ratio >= 0.66f -> baseColor.copy(alpha = 0.75f)
        ratio >= 0.33f -> baseColor.copy(alpha = 0.50f)
        else -> baseColor.copy(alpha = 0.25f)
    }
}

// -----------------------------------------------------------------------------
// HABIT ITEM CARD (TODAY'S CHECKLIST)
// -----------------------------------------------------------------------------
@Composable
private fun HabitItemCard(
    habit: Habit,
    isCompleted: Boolean,
    currentValue: Float,
    isEn: Boolean,
    onToggle: () -> Unit,
    onUpdateValue: (Float) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val habitColor = Color(habit.colorHex)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    val targetVal = habit.targetValue
    val progress = (currentValue / targetVal).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) habitColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isCompleted) habitColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // EMOJI BADGE
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(habitColor.copy(alpha = 0.15f))
                    .border(1.dp, habitColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.icon.take(3).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = habitColor
                )
            }

            Spacer(Modifier.width(12.dp))

            // TITLE & PROGRESS DETAILS
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isEn) habit.nameEn else habit.nameNp,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(2.dp))

                // Target text
                val unitText = if (isEn) habit.unitEn else habit.unitNp
                val targetText = when (habit.type) {
                    HabitType.BOOLEAN -> if (isCompleted) (if (isEn) "Completed" else "सम्पन्न भयो") else (if (isEn) "Daily target: Once" else "दैनिक लक्ष्य: १ पटक")
                    HabitType.NUMERIC -> "${npNum(currentValue.toInt())} / ${npNum(targetVal.toInt())} $unitText"
                    HabitType.TIMER -> "${npNum(currentValue.toInt())} / ${npNum(targetVal.toInt())} $unitText"
                }

                Text(
                    text = targetText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted) habitColor else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (habit.type != HabitType.BOOLEAN) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = habitColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // RIGHT ACTION CONTROLS
            when (habit.type) {
                HabitType.BOOLEAN -> {
                    // Large Checkbox Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) habitColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .clickable(onClick = onToggle),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = PIcons.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HabitType.NUMERIC, HabitType.TIMER -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Minus
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .clickable {
                                    val step = if (habit.type == HabitType.TIMER) 5f else 1f
                                    onUpdateValue((currentValue - step).coerceAtLeast(0f))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        // Plus
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) habitColor else MaterialTheme.colorScheme.primary)
                                .clickable {
                                    val step = if (habit.type == HabitType.TIMER) 5f else 1f
                                    onUpdateValue(currentValue + step)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            // 3-Dots Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = PIcons.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isEn) "Edit Habit" else "सम्पादन गर्नुहोस्") },
                        leadingIcon = { Icon(PIcons.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isEn) "Delete Habit" else "हटाउनुहोस्", color = Color(0xFFDC2626)) },
                        leadingIcon = { Icon(PIcons.Trash, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// EMPTY HABITS CARD
// -----------------------------------------------------------------------------
@Composable
private fun EmptyHabitsCard(
    isEn: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PIcons.Flame,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (isEn) "No habits yet" else "कुनै बानी थपिएको छैन",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = if (isEn) "Build daily consistency by adding your first positive habit!"
                else "आफ्नो पहिलो सकारात्मक बानी थपेर दैनिक निरन्तरताको विकास गर्नुहोस्!",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(PIcons.Plus, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isEn) "Create First Habit" else "पहिलो बानी थप्नुहोस्")
            }
        }
    }
}

// -----------------------------------------------------------------------------
// MODAL: HABIT CREATOR / EDITOR BOTTOM SHEET
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitEditorBottomSheet(
    habit: Habit?,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        nameNp: String,
        nameEn: String,
        icon: String,
        colorHex: Long,
        type: HabitType,
        targetVal: Float,
        unitNp: String,
        unitEn: String,
        freq: List<Int>
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var nameNp by remember { mutableStateOf(habit?.nameNp ?: "") }
    var nameEn by remember { mutableStateOf(habit?.nameEn ?: "") }
    var selectedIcon by remember { mutableStateOf(habit?.icon ?: "TASK") }
    var selectedColor by remember { mutableLongStateOf(habit?.colorHex ?: COLOR_PALETTE[0]) }
    var selectedType by remember { mutableStateOf(habit?.type ?: HabitType.BOOLEAN) }
    var targetValueStr by remember { mutableStateOf(habit?.targetValue?.toInt()?.toString() ?: "1") }
    var unitNp by remember { mutableStateOf(habit?.unitNp ?: "पटक") }
    var unitEn by remember { mutableStateOf(habit?.unitEn ?: "times") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Text(
                text = if (habit == null) {
                    if (isEn) "Create New Habit" else "नयाँ बानी सिर्जना गर्नुहोस्"
                } else {
                    if (isEn) "Edit Habit" else "बानी सम्पादन गर्नुहोस्"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // PRESETS ROW (If new)
            if (habit == null) {
                Text(
                    text = if (isEn) "Quick Presets" else "तयारी नमुनाहरू",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HabitRepository.PRESET_HABITS) { preset ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable {
                                nameNp = preset.nameNp
                                nameEn = preset.nameEn
                                selectedIcon = preset.icon
                                selectedColor = preset.colorHex
                                selectedType = preset.type
                                targetValueStr = preset.targetValue.toInt().toString()
                                unitNp = preset.unitNp
                                unitEn = preset.unitEn
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = preset.icon, fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isEn) preset.nameEn else preset.nameNp,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Name inputs
            OutlinedTextField(
                value = nameNp,
                onValueChange = { nameNp = it },
                label = { Text(if (isEn) "Habit Name (Nepali)" else "बानीको नाम (नेपाली)") },
                placeholder = { Text("जस्तै: बिहानी हिँडाइ, पुस्तक अध्ययन...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = nameEn,
                onValueChange = { nameEn = it },
                label = { Text(if (isEn) "Habit Name (English)" else "बानीको नाम (अंग्रेजी)") },
                placeholder = { Text("e.g. Morning Walk, Read Book...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // EMOJI PICKER ROW
            Text(
                text = if (isEn) "Select Icon" else "चिन्ह / इमोजी रोज्नुहोस्",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORY_ICON_TAGS) { tag ->
                    val isSelected = selectedIcon == tag
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedIcon = tag }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // COLOR PICKER ROW
            Text(
                text = if (isEn) "Theme Color" else "रंग छनोट गर्नुहोस्",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                COLOR_PALETTE.forEach { colorVal ->
                    val isSelected = selectedColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                width = if (isSelected) 2.5.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorVal },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = PIcons.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // HABIT TYPE SELECTOR
            Text(
                text = if (isEn) "Tracking Type" else "ट्र्याकिङ प्रकार",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitType.values().forEach { t ->
                    val isSelected = selectedType == t
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedType = t
                                when (t) {
                                    HabitType.BOOLEAN -> {
                                        targetValueStr = "1"
                                        unitNp = "पटक"
                                        unitEn = "times"
                                    }
                                    HabitType.NUMERIC -> {
                                        targetValueStr = "8"
                                        unitNp = "गिलास"
                                        unitEn = "count"
                                    }
                                    HabitType.TIMER -> {
                                        targetValueStr = "20"
                                        unitNp = "मिनेट"
                                        unitEn = "mins"
                                    }
                                }
                            }
                    ) {
                        Text(
                            text = when (t) {
                                HabitType.BOOLEAN -> if (isEn) "Yes / No" else "हो / होइन"
                                HabitType.NUMERIC -> if (isEn) "Count" else "संख्या"
                                HabitType.TIMER -> if (isEn) "Duration" else "समय (मिनेट)"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // TARGET VALUE & UNIT (If numeric / timer)
            if (selectedType != HabitType.BOOLEAN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetValueStr,
                        onValueChange = { targetValueStr = it },
                        label = { Text(if (isEn) "Target Goal" else "दैनिक लक्ष्य") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = if (isEn) unitEn else unitNp,
                        onValueChange = {
                            if (isEn) unitEn = it else unitNp = it
                        },
                        label = { Text(if (isEn) "Unit" else "एकाइ") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    val finalNp = nameNp.ifBlank { nameEn }.ifBlank { "नयाँ बानी" }
                    val finalEn = nameEn.ifBlank { nameNp }.ifBlank { "New Habit" }
                    val targetNum = targetValueStr.toFloatOrNull() ?: 1f

                    onSave(
                        finalNp,
                        finalEn,
                        selectedIcon,
                        selectedColor,
                        selectedType,
                        targetNum,
                        unitNp,
                        unitEn,
                        listOf(0, 1, 2, 3, 4, 5, 6)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (habit == null) (if (isEn) "Create Habit" else "बानी सुरक्षित गर्नुहोस्")
                    else (if (isEn) "Save Changes" else "परिवर्तन सुरक्षित गर्नुहोस्"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// MODAL: DAY INSPECTION BOTTOM SHEET
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayInspectionBottomSheet(
    cell: DayHeatmapCell,
    habits: List<Habit>,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onToggleHabit: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val repo = remember { HabitRepository.get(context) }
    var logs by remember { mutableStateOf(repo.getLogsForDate(cell.dateIso)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with full date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${npNum(cell.bsDay)} ${NepaliNames.monthsNp[cell.bsMonth - 1]} ${npNum(cell.bsYear)}, ${NepaliNames.weekdaysNp[cell.weekdayIndex]}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${cell.dateIso} • " + (if (cell.isToday) (if (isEn) "Today" else "आज") else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${npNum(cell.completedCount)} / ${npNum(cell.totalCount)} " + (if (isEn) "Done" else "सम्पन्न"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (habits.isEmpty()) {
                Text(
                    text = if (isEn) "No habits configured" else "कुनै बानी थपिएको छैन",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                habits.forEach { habit ->
                    val isDone = logs[habit.id]?.completed == true
                    val habitColor = Color(habit.colorHex)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDone) habitColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable {
                                onToggleHabit(habit.id)
                                logs = repo.getLogsForDate(cell.dateIso)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = habit.icon, fontSize = 20.sp)
                            Text(
                                text = if (isEn) habit.nameEn else habit.nameNp,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDone) habitColor else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = PIcons.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper mutableLongStateOf
@Composable
private fun rememberMutableLongStateOf(initial: Long) = remember { mutableStateOf(initial) }
private fun mutableLongStateOf(initial: Long) = androidx.compose.runtime.mutableStateOf(initial)
