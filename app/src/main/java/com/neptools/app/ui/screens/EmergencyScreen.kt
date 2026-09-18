package com.neptools.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.data.EmergencyContact
import com.neptools.app.core.data.EmergencyRepo
import com.neptools.app.core.util.DistrictItem
import com.neptools.app.core.util.EmergencyLocationResolver
import com.neptools.app.core.util.EmergencySyncManager
import com.neptools.app.core.util.ResolvedEmergencyLocation
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.strings.tt
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val isEn = ThemePrefs.lang.value == "en"

    LaunchedEffect(Unit) {
        EmergencyLocationResolver.init(context)
        EmergencySyncManager.init(context)
    }

    val currentLoc by EmergencyLocationResolver.currentLocation.collectAsState()
    val isLocating by EmergencyLocationResolver.isLocating.collectAsState()
    val allContacts by EmergencyRepo.liveContacts.collectAsState()
    val isSyncing by EmergencySyncManager.isSyncing.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showLocationSheet by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.values.any { it }
        if (granted) {
            EmergencyLocationResolver.requestGpsLocation(context) { success ->
                val msg = if (success) {
                    if (isEn) "Location updated via GPS" else "GPS स्थान अद्यावधिक भयो"
                } else {
                    if (isEn) "Could not determine GPS location" else "GPS स्थान प्राप्त हुन सकेन"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                context,
                if (isEn) "Location permission required for GPS auto-detection" else "GPS का लागि स्थान अनुमति चाहिन्छ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val contacts = remember(query, selectedCategory, currentLoc, allContacts) {
        EmergencyRepo.search(
            query = query,
            category = selectedCategory,
            province = currentLoc.provinceEn,
            district = currentLoc.districtEn,
            sourceContacts = allContacts
        )
    }

    val categoryLabels = listOf(
        "all" to T("emg_cat_all"),
        "security" to T("emg_cat_sec"),
        "medical" to T("emg_cat_med"),
        "rescue" to T("emg_cat_res"),
        "social" to T("emg_cat_soc"),
        "blood" to T("emg_cat_bld")
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
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
                    T("emg_title"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            // Dynamic API Sync Button
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(enabled = !isSyncing) {
                        coroutineScope.launch {
                            EmergencySyncManager.sync(context, force = true) { success, count, msg ->
                                val toastMsg = if (success) {
                                    if (msg.contains("Already", ignoreCase = true)) {
                                        tt("emg_sync_latest")
                                    } else {
                                        "${tt("emg_sync_success")} ($count)"
                                    }
                                } else {
                                    tt("emg_sync_failed")
                                }
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val rotation = rememberInfiniteTransition(label = "sync_rot")
                    .animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "sync_spin"
                    )
                Icon(
                    PIcons.Refresh,
                    contentDescription = T("emg_sync_btn"),
                    tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(17.dp)
                        .graphicsLayer {
                            if (isSyncing) rotationZ = rotation.value
                        }
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "24/7",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD32F2F)
                )
            }
        }

        // Location Selector Card
        LocationStatusBanner(
            currentLoc = currentLoc,
            isLocating = isLocating,
            isEn = isEn,
            onGpsClick = {
                if (EmergencyLocationResolver.hasLocationPermission(context)) {
                    EmergencyLocationResolver.requestGpsLocation(context) { success ->
                        val msg = if (success) {
                            if (isEn) "Location updated via GPS" else "GPS स्थान अद्यावधिक भयो"
                        } else {
                            if (isEn) "Could not determine GPS location" else "GPS स्थान प्राप्त हुन सकेन"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            onChangeClick = { showLocationSheet = true }
        )

        Spacer(Modifier.height(10.dp))

        // Search Bar
        Box(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(T("emg_search")) },
                leadingIcon = { Icon(PIcons.Search, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { query = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryLabels, key = { it.first }) { (key, label) ->
                val isSelected = selectedCategory == key
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = key }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Contact Cards List
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(contacts, key = { it.number + "_" + it.nameEn + "_" + it.district }) { contact ->
                val isLocal = EmergencyRepo.isLocal(contact, currentLoc.districtEn, currentLoc.provinceEn)
                EmergencyCard(
                    contact = contact,
                    isLocal = isLocal,
                    isEn = isEn,
                    onCall = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.number}"))
                        context.startActivity(dialIntent)
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(contact.number))
                        Toast.makeText(context, if (isEn) "Copied: ${contact.number}" else "नम्बर कपी भयो: ${contact.number}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    T("emg_note"),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showLocationSheet) {
        LocationSelectionSheet(
            currentLoc = currentLoc,
            isEn = isEn,
            onDismiss = { showLocationSheet = false },
            onSelectDistrict = { dItem ->
                EmergencyLocationResolver.setManualLocation(
                    context = context,
                    districtEn = dItem.nameEn,
                    districtNp = dItem.nameNp,
                    provinceEn = dItem.provinceEn,
                    provinceNp = dItem.provinceNp
                )
                showLocationSheet = false
            },
            onSelectAllNepal = {
                EmergencyLocationResolver.setManualLocation(
                    context = context,
                    districtEn = "All",
                    districtNp = "सबै नेपाल",
                    provinceEn = "National",
                    provinceNp = "राष्ट्रिय"
                )
                showLocationSheet = false
            },
            onGpsDetect = {
                showLocationSheet = false
                if (EmergencyLocationResolver.hasLocationPermission(context)) {
                    EmergencyLocationResolver.requestGpsLocation(context) { success ->
                        val msg = if (success) {
                            if (isEn) "Location updated via GPS" else "GPS स्थान अद्यावधिक भयो"
                        } else {
                            if (isEn) "Could not determine GPS location" else "GPS स्थान प्राप्त हुन सकेन"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun LocationStatusBanner(
    currentLoc: ResolvedEmergencyLocation,
    isLocating: Boolean,
    isEn: Boolean,
    onGpsClick: () -> Unit,
    onChangeClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsSpin"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PIcons.Pin,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val locationTitle = if (currentLoc.districtEn == "All") {
                        if (isEn) "All Nepal (National)" else "सबै नेपाल (राष्ट्रिय)"
                    } else {
                        if (isEn) "${currentLoc.districtEn}, ${currentLoc.provinceEn}" else "${currentLoc.districtNp}, ${currentLoc.provinceNp}"
                    }
                    Text(
                        locationTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .background(
                                if (currentLoc.isAutoGps) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (currentLoc.isAutoGps) "GPS Auto" else (if (isEn) "Manual" else "म्यानुअल"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = if (currentLoc.isAutoGps) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isLocating) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            T("emg_loc_locating"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // GPS Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onGpsClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            PIcons.Crosshair,
                            "GPS",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Change Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .clickable(onClick = onChangeClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            T("emg_loc_change"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            PIcons.ChevronDown,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyCard(
    contact: EmergencyContact,
    isLocal: Boolean,
    isEn: Boolean,
    onCall: () -> Unit,
    onCopy: () -> Unit
) {
    val borderColor = if (isLocal) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }
    val bgColor = if (isLocal) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.035f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onCall)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prominent Phone Badge
        Box(
            Modifier
                .size(46.dp)
                .background(
                    if (isLocal) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                PIcons.Phone,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Contact Info
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isEn) contact.nameEn else contact.nameNp,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isLocal) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (isEn) "Your Area" else "नजिकको सेवा",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (contact.province == "National") {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .background(Color(0xFFEDE7F6), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Toll-Free",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF512DA8)
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            Text(
                if (isEn) contact.descriptionEn else contact.descriptionNp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                contact.number,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.width(10.dp))

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable(onClick = onCopy),
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.Copy, "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onCall),
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.Phone, "Call", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSelectionSheet(
    currentLoc: ResolvedEmergencyLocation,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onSelectDistrict: (DistrictItem) -> Unit,
    onSelectAllNepal: () -> Unit,
    onGpsDetect: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvinceFilter by remember { mutableStateOf("All") }

    val provinces = listOf(
        "All" to (if (isEn) "All" else "सबै"),
        "Koshi" to (if (isEn) "Koshi" else "कोशी"),
        "Madhesh" to (if (isEn) "Madhesh" else "मधेश"),
        "Bagmati" to (if (isEn) "Bagmati" else "बागमती"),
        "Gandaki" to (if (isEn) "Gandaki" else "गण्डकी"),
        "Lumbini" to (if (isEn) "Lumbini" else "लुम्बिनी"),
        "Karnali" to (if (isEn) "Karnali" else "कर्णाली"),
        "Sudurpashchim" to (if (isEn) "Sudurpashchim" else "सुदूरपश्चिम")
    )

    val filteredDistricts = remember(searchQuery, selectedProvinceFilter) {
        val q = searchQuery.trim().lowercase()
        EmergencyLocationResolver.ALL_DISTRICTS.filter { d ->
            val matchProv = selectedProvinceFilter == "All" || d.provinceEn.equals(selectedProvinceFilter, ignoreCase = true)
            val matchQuery = q.isEmpty() ||
                d.nameEn.lowercase().contains(q) ||
                d.nameNp.lowercase().contains(q) ||
                d.provinceEn.lowercase().contains(q)
            matchProv && matchQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                T("emg_loc_title"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))

            // Quick Options Row (GPS Auto-Detect & All Nepal)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // GPS Auto Detect Button
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onGpsDetect)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PIcons.Crosshair, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            T("emg_loc_gps"),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // All Nepal Button
                val isAllActive = currentLoc.districtEn == "All"
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAllActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onSelectAllNepal)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        T("emg_loc_all"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isAllActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // District Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(T("emg_loc_search_hint")) },
                leadingIcon = { Icon(PIcons.Search, null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // Province Tabs Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(provinces) { (key, label) ->
                    val selected = selectedProvinceFilter == key
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { selectedProvinceFilter = key }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Districts List
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredDistricts, key = { it.nameEn }) { dItem ->
                    val isSelected = currentLoc.districtEn.equals(dItem.nameEn, ignoreCase = true)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent)
                            .clickable { onSelectDistrict(dItem) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isEn) dItem.nameEn else dItem.nameNp,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isEn) dItem.provinceEn else dItem.provinceNp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                PIcons.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
