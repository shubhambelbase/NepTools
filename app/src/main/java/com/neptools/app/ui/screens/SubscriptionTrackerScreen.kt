package com.neptools.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.neptools.app.core.subscription.BillingCycle
import com.neptools.app.core.subscription.CurrencyType
import com.neptools.app.core.subscription.PaymentMethod
import com.neptools.app.core.subscription.Subscription
import com.neptools.app.core.subscription.SubscriptionCategory
import com.neptools.app.core.subscription.SubscriptionRepository
import com.neptools.app.core.subscription.SubscriptionSummary
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.text.DecimalFormat
import java.time.LocalDate

private val SUB_ACCENT_COLORS = listOf(
    0xFF4F46E5, // Indigo
    0xFF0284C7, // Sky Blue
    0xFF16A34A, // Emerald
    0xFFEA580C, // Amber-Orange
    0xFFE11D48, // Crimson
    0xFF7C3AED  // Violet
)

private enum class SubFilterTab(val labelNp: String, val labelEn: String) {
    ALL("सबै", "All"),
    ACTIVE("सक्रिय", "Active"),
    DUE_SOON("७ दिनभित्र", "Due Soon"),
    PAUSED("रोकिएको", "Paused")
}

private enum class SubSortOrder(val labelNp: String, val labelEn: String) {
    DUE_DATE("नवीकरण मिति", "Due Date"),
    PRICE_HIGH("उच्च खर्च", "Highest Price"),
    NAME("नाम (A-Z)", "Name")
}

private fun getCategoryIcon(cat: SubscriptionCategory): ImageVector {
    return when (cat) {
        SubscriptionCategory.STREAMING -> PIcons.Play
        SubscriptionCategory.INTERNET_MOBILE -> PIcons.WifiDrop
        SubscriptionCategory.SOFTWARE_AI -> PIcons.Doc
        SubscriptionCategory.UTILITIES -> PIcons.Zap
        SubscriptionCategory.FITNESS_HEALTH -> PIcons.Award
        SubscriptionCategory.HOUSING_RENT -> PIcons.Home
        SubscriptionCategory.EDUCATION_WORK -> PIcons.BookOpen
        SubscriptionCategory.OTHER -> PIcons.CreditCard
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionTrackerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEn = ThemePrefs.lang.value == "en"
    val repo = remember { SubscriptionRepository.get(context) }

    var updateTick by remember { mutableIntStateOf(0) }
    var subscriptions by remember { mutableStateOf(repo.getAllSubscriptions()) }

    var selectedTab by remember { mutableStateOf(SubFilterTab.ALL) }
    var selectedSort by remember { mutableStateOf(SubSortOrder.DUE_DATE) }
    var searchQuery by remember { mutableStateOf("") }

    val summary by remember(updateTick, subscriptions) {
        derivedStateOf { repo.getSummary() }
    }

    var showCreateModal by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<Subscription?>(null) }

    fun refresh() {
        subscriptions = repo.getAllSubscriptions()
        updateTick++
    }

    val filteredList by remember(subscriptions, selectedTab, selectedSort, searchQuery) {
        derivedStateOf {
            val list = subscriptions.filter { sub ->
                val matchesSearch = searchQuery.isBlank() ||
                        sub.nameNp.contains(searchQuery, ignoreCase = true) ||
                        sub.nameEn.contains(searchQuery, ignoreCase = true) ||
                        sub.category.labelNp.contains(searchQuery, ignoreCase = true) ||
                        sub.category.labelEn.contains(searchQuery, ignoreCase = true)

                val matchesTab = when (selectedTab) {
                    SubFilterTab.ALL -> true
                    SubFilterTab.ACTIVE -> !sub.isPaused
                    SubFilterTab.DUE_SOON -> !sub.isPaused && repo.daysUntilRenewal(sub) in 0..7
                    SubFilterTab.PAUSED -> sub.isPaused
                }
                matchesSearch && matchesTab
            }

            when (selectedSort) {
                SubSortOrder.DUE_DATE -> list.sortedBy {
                    try { LocalDate.parse(it.nextBillingDateIso) } catch (e: Exception) { LocalDate.now() }
                }
                SubSortOrder.PRICE_HIGH -> list.sortedByDescending { it.monthlyCostInNpr() }
                SubSortOrder.NAME -> list.sortedBy { if (isEn) it.nameEn else it.nameNp }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEn) "Subscription & Bill Tracker" else "सदस्यता तथा बिल ट्र्याकर",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isEn) "Recurring expenses & renewals" else "नियमित खर्च तथा नवीकरण व्यवस्थापक",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = PIcons.ChevronLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            editingSubscription = null
                            showCreateModal = true
                        }
                    ) {
                        Icon(
                            imageVector = PIcons.Plus,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
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
                    editingSubscription = null
                    showCreateModal = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(PIcons.Plus, contentDescription = "Add", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. SLEEK EXPENSE SUMMARY CARD
            item {
                CleanExpenseCard(
                    summary = summary,
                    isEn = isEn
                )
            }

            // 2. SEARCH & FILTER CHIPS ROW
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isEn) "Search subscriptions..." else "सदस्यता वा शीर्षक खोज्नुहोस्...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = PIcons.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    // Tabs Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(SubFilterTab.values()) { tab ->
                            val isSelected = selectedTab == tab
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                label = {
                                    Text(
                                        text = if (isEn) tab.labelEn else tab.labelNp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // 3. SUBSCRIPTIONS LIST
            if (filteredList.isEmpty()) {
                item {
                    CleanEmptyCard(
                        isEn = isEn,
                        onAddClick = {
                            editingSubscription = null
                            showCreateModal = true
                        }
                    )
                }
            } else {
                items(filteredList, key = { it.id }) { sub ->
                    CleanSubscriptionCard(
                        sub = sub,
                        repo = repo,
                        isEn = isEn,
                        onMarkPaid = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            repo.markAsPaidAndAdvance(sub.id)
                            refresh()
                        },
                        onTogglePause = {
                            repo.togglePause(sub.id)
                            refresh()
                        },
                        onEdit = {
                            editingSubscription = sub
                            showCreateModal = true
                        },
                        onDelete = {
                            repo.deleteSubscription(sub.id)
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

    // MODAL: ADD / EDIT SUBSCRIPTION
    if (showCreateModal) {
        CleanSubscriptionEditorModal(
            sub = editingSubscription,
            isEn = isEn,
            onDismiss = { showCreateModal = false },
            onSave = { nameNp, nameEn, price, currency, cycle, cat, pm, firstDate, nextDate, colorHex, notes ->
                if (editingSubscription != null) {
                    val updated = editingSubscription!!.copy(
                        nameNp = nameNp,
                        nameEn = nameEn,
                        price = price,
                        currency = currency,
                        billingCycle = cycle,
                        category = cat,
                        paymentMethod = pm,
                        firstBillingDateIso = firstDate,
                        nextBillingDateIso = nextDate,
                        colorHex = colorHex,
                        notes = notes
                    )
                    repo.saveSubscription(updated)
                } else {
                    repo.createNewSubscription(
                        nameNp = nameNp,
                        nameEn = nameEn,
                        price = price,
                        currency = currency,
                        billingCycle = cycle,
                        category = cat,
                        paymentMethod = pm,
                        firstBillingDateIso = firstDate,
                        nextBillingDateIso = nextDate,
                        colorHex = colorHex,
                        notes = notes
                    )
                }
                showCreateModal = false
                refresh()
            }
        )
    }
}

// -----------------------------------------------------------------------------
// CLEAN EXPENSE SUMMARY CARD
// -----------------------------------------------------------------------------
@Composable
private fun CleanExpenseCard(
    summary: SubscriptionSummary,
    isEn: Boolean
) {
    val formatter = remember { DecimalFormat("#,##,###") }
    val formattedMonthly = formatter.format(summary.totalMonthlyNpr.toInt())
    val formattedYearly = formatter.format(summary.totalYearlyNpr.toInt())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = if (isEn) "Total Monthly Outflow" else "मासिक कुल खर्च",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "रू $formattedMonthly",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${npNum(summary.activeCount)} " + (if (isEn) "Active" else "सक्रिय"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (isEn) "Annual estimate: " else "वार्षिक अनुमान: ") + "रू $formattedYearly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (summary.dueIn7DaysCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${npNum(summary.dueIn7DaysCount)} " + (if (isEn) "due this week" else "यस हप्ता नवीकरण"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CLEAN SUBSCRIPTION CARD
// -----------------------------------------------------------------------------
@Composable
private fun CleanSubscriptionCard(
    sub: Subscription,
    repo: SubscriptionRepository,
    isEn: Boolean,
    onMarkPaid: () -> Unit,
    onTogglePause: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val daysLeft = repo.daysUntilRenewal(sub)

    val engine = PatroRepo.d.engine
    val nextAd = try { LocalDate.parse(sub.nextBillingDateIso) } catch (e: Exception) { LocalDate.now() }
    val nextBs = try { engine.adToBs(nextAd) } catch (e: Exception) { NepaliDate(2083, 1, 1) }

    val statusBadgeColor = when {
        sub.isPaused -> MaterialTheme.colorScheme.onSurfaceVariant
        daysLeft < 0 -> MaterialTheme.colorScheme.error
        daysLeft in 0..3 -> Color(0xFFEA580C)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusBadgeText = when {
        sub.isPaused -> if (isEn) "Paused" else "रोकिएको"
        daysLeft < 0 -> if (isEn) "Overdue" else "म्याद सकियो"
        daysLeft == 0L -> if (isEn) "Due Today" else "आज नवीकरण"
        daysLeft == 1L -> if (isEn) "Tomorrow" else "भोलि"
        else -> "${npNum(daysLeft.toInt())} " + (if (isEn) "days left" else "दिन बाँकी")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (sub.isPaused) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CATEGORY ICON BADGE
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(sub.colorHex).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(sub.category),
                    contentDescription = null,
                    tint = Color(sub.colorHex),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // INFO
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) sub.nameEn else sub.nameNp,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = (if (isEn) sub.category.labelEn else sub.category.labelNp) + " • " +
                            (if (isEn) sub.paymentMethod.labelEn else sub.paymentMethod.labelNp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${npNum(nextBs.day)} ${NepaliNames.monthsNp[nextBs.month - 1]} (${nextAd.dayOfMonth} ${nextAd.month.name.take(3)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = statusBadgeColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusBadgeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // PRICE & MENU
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${sub.currency.symbol} ${sub.price}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isEn) sub.billingCycle.labelEn else sub.billingCycle.labelNp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
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
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isEn) "Mark as Paid" else "भुक्तानी भयो (अर्को मिति)") },
                            leadingIcon = { Icon(PIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onMarkPaid()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (sub.isPaused) (if (isEn) "Resume" else "सक्रिय गर्नुहोस्") else (if (isEn) "Pause" else "रोक्नुहोस्")) },
                            leadingIcon = { Icon(PIcons.Timer, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onTogglePause()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isEn) "Edit" else "सम्पादन गर्नुहोस्") },
                            leadingIcon = { Icon(PIcons.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isEn) "Delete" else "हटाउनुहोस्", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(PIcons.Trash, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
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
}

// -----------------------------------------------------------------------------
// CLEAN EMPTY CARD
// -----------------------------------------------------------------------------
@Composable
private fun CleanEmptyCard(
    isEn: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PIcons.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = if (isEn) "No subscriptions added" else "कुनै सदस्यता थपिएको छैन",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isEn) "Add your recurring subscriptions, utilities, and bills to stay on top of renewals."
                else "आफ्ना मासिक बिल, इन्टरनेट, घरभाडा वा सफ्टवेयर सदस्यताहरू यहाँ थप्नुहोस्।",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(PIcons.Plus, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isEn) "Add Subscription" else "सदस्यता थप्नुहोस्")
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CLEAN ADD / EDIT MODAL
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CleanSubscriptionEditorModal(
    sub: Subscription?,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        nameNp: String,
        nameEn: String,
        price: Double,
        currency: CurrencyType,
        cycle: BillingCycle,
        category: SubscriptionCategory,
        paymentMethod: PaymentMethod,
        firstDate: String,
        nextDate: String,
        colorHex: Long,
        notes: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nameNp by remember { mutableStateOf(sub?.nameNp ?: "") }
    var nameEn by remember { mutableStateOf(sub?.nameEn ?: "") }
    var priceStr by remember { mutableStateOf(sub?.price?.toString() ?: "") }
    var selectedCurrency by remember { mutableStateOf(sub?.currency ?: CurrencyType.NPR) }
    var selectedCycle by remember { mutableStateOf(sub?.billingCycle ?: BillingCycle.MONTHLY) }
    var selectedCategory by remember { mutableStateOf(sub?.category ?: SubscriptionCategory.UTILITIES) }
    var selectedPaymentMethod by remember { mutableStateOf(sub?.paymentMethod ?: PaymentMethod.ESEWA) }
    var nextDateIso by remember { mutableStateOf(sub?.nextBillingDateIso ?: LocalDate.now().plusMonths(1).toString()) }
    var selectedColor by remember { mutableLongStateOf(sub?.colorHex ?: SUB_ACCENT_COLORS[0]) }
    var notes by remember { mutableStateOf(sub?.notes ?: "") }

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
            Text(
                text = if (sub == null) {
                    if (isEn) "Add Subscription" else "नयाँ सदस्यता थप्नुहोस्"
                } else {
                    if (isEn) "Edit Subscription" else "सदस्यता सम्पादन गर्नुहोस्"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Name inputs
            OutlinedTextField(
                value = nameNp,
                onValueChange = { nameNp = it },
                label = { Text(if (isEn) "Service Name (Nepali)" else "सेवाको नाम (नेपाली)") },
                placeholder = { Text("जस्तै: वर्ल्डलिंक, घरभाडा, विद्युत...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = nameEn,
                onValueChange = { nameEn = it },
                label = { Text(if (isEn) "Service Name (English)" else "सेवाको नाम (अंग्रेजी)") },
                placeholder = { Text("e.g. WorldLink, House Rent, Netflix...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Price & Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text(if (isEn) "Price" else "रकम / शुल्क") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1.3f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Currency picker
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Currency" else "मुद्रा",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(CurrencyType.values()) { cur ->
                            val isSelected = selectedCurrency == cur
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedCurrency = cur }
                            ) {
                                Text(
                                    text = cur.symbol,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Billing Cycle
            Text(
                text = if (isEn) "Billing Cycle" else "भुक्तानी चक्र",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(BillingCycle.values()) { cycle ->
                    val isSelected = selectedCycle == cycle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedCycle = cycle }
                    ) {
                        Text(
                            text = if (isEn) cycle.labelEn else cycle.labelNp,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Category Selector
            Text(
                text = if (isEn) "Category" else "शीर्षक / विधा",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SubscriptionCategory.values()) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isEn) cat.labelEn else cat.labelNp,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Payment Method Selector
            Text(
                text = if (isEn) "Payment Method" else "भुक्तानी माध्यम / खाता",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(PaymentMethod.values()) { pm ->
                    val isSelected = selectedPaymentMethod == pm
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedPaymentMethod = pm }
                    ) {
                        Text(
                            text = if (isEn) pm.labelEn else pm.labelNp,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Next Billing Date Input (ISO YYYY-MM-DD)
            OutlinedTextField(
                value = nextDateIso,
                onValueChange = { nextDateIso = it },
                label = { Text(if (isEn) "Next Renewal Date (YYYY-MM-DD)" else "अर्को नवीकरण मिति (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Accent Color Selector
            Text(
                text = if (isEn) "Accent Color" else "रंग",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SUB_ACCENT_COLORS.forEach { colorVal ->
                    val isSelected = selectedColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(32.dp)
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
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(if (isEn) "Notes (Optional)" else "टिप्पणी / विवरण (ऐच्छिक)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    val finalNp = nameNp.ifBlank { nameEn }.ifBlank { "नयाँ सदस्यता" }
                    val finalEn = nameEn.ifBlank { nameNp }.ifBlank { "New Subscription" }
                    val p = priceStr.toDoubleOrNull() ?: 0.0

                    onSave(
                        finalNp,
                        finalEn,
                        p,
                        selectedCurrency,
                        selectedCycle,
                        selectedCategory,
                        selectedPaymentMethod,
                        LocalDate.now().toString(),
                        nextDateIso,
                        selectedColor,
                        notes
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (sub == null) (if (isEn) "Save Subscription" else "सदस्यता सुरक्षित गर्नुहोस्")
                    else (if (isEn) "Save Changes" else "परिवर्तन सुरक्षित गर्नुहोस्"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
