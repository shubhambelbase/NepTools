package com.neptools.app.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.PersistableBundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.neptools.app.core.vault.BiometricGateActivity
import com.neptools.app.core.vault.PasswordToolkit
import com.neptools.app.core.vault.VaultCrypto
import com.neptools.app.core.vault.VaultEntry
import com.neptools.app.core.vault.VaultStore
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private const val CLIPBOARD_CLEAR_MS = 45_000L

/** Unwraps the Activity from a possibly wrapped Compose context. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Marks clipboard content so Android 13+ hides it from the clipboard preview overlay and
 * excludes it from clipboard history.
 */
private fun sensitiveClip(label: String, text: String): ClipData {
    val clip = ClipData.newPlainText(label, text)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        clip.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    return clip
}

private fun clearClipboard(context: Context) {
    runCatching {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("", ""))
    }
}

@Composable
fun PasswordVaultScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"
    val store = remember { VaultStore.get(context) }
    val scope = rememberCoroutineScope()

    var stage by remember { mutableStateOf(if (VaultStore.Session.unlocked) 2 else if (store.exists()) 1 else 0) }
    var tick by remember { mutableStateOf(0) }
    fun refresh() { tick++ }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                clearClipboard(context)
                if (store.autoLockEnabled() && VaultStore.Session.unlocked) {
                    store.lock()
                    stage = 1
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Block screenshots, screen recording and the Recents thumbnail while the vault is open.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            clearClipboard(context)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolTopBar(
            title = if (isEn) "Password Vault" else "पासवर्ड भल्ट",
            subtitle = when (stage) {
                2 -> if (isEn) "Unlocked · AES-256 encrypted, on-device only" else "खुल्यो — यन्त्रमै सुरक्षित"
                else -> if (isEn) "Encrypted on this device only" else "यो यन्त्रमै गोप्य राखिएको"
            },
            onBack = onBack,
            actions = if (stage == 2) {
                {
                    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                            .clickable(
                                interactionSource = interaction,
                                indication = androidx.compose.material3.ripple(bounded = true, radius = 18.dp),
                                onClick = {
                                    store.lock()
                                    stage = 1
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(PIcons.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            } else null
        )

        key(tick) {
            when (stage) {
                0 -> SetupStage(store, isEn, onCreated = { stage = 2; refresh() })
                1 -> LockedStage(store, isEn, onUnlocked = { stage = 2; refresh() })
                else -> VaultListStage(store, scope, isEn, onChanged = { refresh() }, onLocked = { stage = 1 })
            }
        }
    }
}

@Composable
private fun SetupStage(store: VaultStore, isEn: Boolean, onCreated: () -> Unit) {
    var pw by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isEn) "Create your master password" else "आफ्नो मास्टर पासवर्ड बनाउनुहोस्",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = if (isEn)
                "All passwords are encrypted with AES-256-GCM using a key derived from this password. Nobody can recover it."
            else "सबै पासवर्डहरू यो पासवर्डबाट बनेको कुञ्जीद्वारा AES-256-GCM मा गोप्य हुन्छन्। कसैले पनि फेरि प्राप्त गर्न सक्दैन।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PasswordField(
            value = pw,
            onValueChange = { pw = it; error = null },
            label = if (isEn) "Master password" else "मास्टर पासवर्ड",
            show = show,
            onToggleShow = { show = !show }
        )
        StrengthMeter(pw)

        PasswordField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = if (isEn) "Confirm password" else "पासवर्ड पुष्टि",
            show = show,
            onToggleShow = { show = !show }
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Button(
            onClick = {
                when {
                    pw.length < 6 -> error = if (isEn) "Use at least 6 characters" else "कम्तीमा ६ अक्षर प्रयोग गर्नुहोस्"
                    pw != confirm -> error = if (isEn) "Passwords do not match" else "पासवर्ड मिलेनन्"
                    busy -> {}
                    else -> {
                        busy = true
                        val chars = pw.toCharArray()
                        scope.launch(Dispatchers.Default) {
                            val ok = runCatching { store.createVault(chars) }.getOrDefault(false)
                            withContext(Dispatchers.Main) {
                                busy = false
                                if (ok) onCreated()
                            }
                        }
                    }
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                if (busy) {
                    if (isEn) "Encrypting..." else "गोप्य बनाउँदै..."
                } else if (isEn) "Create Vault" else "भल्ट बनाउनुहोस्",
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LockedStage(store: VaultStore, isEn: Boolean, onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var pw by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var bioBusy by remember { mutableStateOf(false) }
    var unlockBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var lockoutRemaining by remember { mutableStateOf(store.lockoutRemainingMs()) }

    // Live countdown while the vault is rate limited after repeated wrong passwords.
    LaunchedEffect(lockoutRemaining > 0L) {
        while (lockoutRemaining > 0L) {
            delay(500L)
            lockoutRemaining = store.lockoutRemainingMs()
        }
        if (error != null) error = null
    }

    val bioAvailable = remember { BiometricGateActivity.canUseBiometrics(context) }

    val gateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        bioBusy = false
        when {
            res.resultCode == Activity.RESULT_OK && store.confirmSessionUnlock() -> onUnlocked()
            res.resultCode == BiometricGateActivity.RESULT_USER_CANCEL -> {}
            else -> Toast.makeText(
                context,
                if (isEn) "Fingerprint unlock failed" else "फिंगरप्रिन्ट अनलक असफल",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun tryBiometric() {
        val wrap = store.bioWrap() ?: run {
            Toast.makeText(context, if (isEn) "Fingerprint not set up" else "फिंगरप्रिन्ट सेट छैन", Toast.LENGTH_SHORT).show()
            return
        }
        if (bioBusy) return
        bioBusy = true
        gateLauncher.launch(
            BiometricGateActivity.createIntent(context, BiometricGateActivity.MODE_DECRYPT, wrap)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(
            PIcons.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = if (isEn) "Enter master password" else "मास्टर पासवर्ड प्रविष्ट गर्नुहोस्",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        OutlinedTextField(
            value = pw,
            onValueChange = { pw = it; error = null },
            label = { Text(if (isEn) "Master password" else "मास्टर पासवर्ड") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (unlockBusy) return@Button
                    unlockBusy = true
                    val chars = pw.toCharArray()
                    scope.launch(Dispatchers.Default) {
                        val ok = runCatching { store.unlockWithPassword(chars) }.getOrDefault(false)
                        withContext(Dispatchers.Main) {
                            unlockBusy = false
                            if (ok) {
                                onUnlocked()
                            } else {
                                lockoutRemaining = store.lockoutRemainingMs()
                                error = if (lockoutRemaining > 0L) {
                                    val seconds = ((lockoutRemaining + 999L) / 1000L)
                                    if (isEn) "Too many attempts. Try again in ${seconds}s"
                                    else "धेरै पटक गलत भयो। ${seconds} सेकेण्डपछि प्रयास गर्नुहोस्"
                                } else {
                                    if (isEn) "Wrong password" else "गलत पासवर्ड"
                                }
                            }
                        }
                    }
                },
                enabled = pw.isNotEmpty() && lockoutRemaining == 0L && !unlockBusy,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (unlockBusy) {
                        if (isEn) "Unlocking..." else "खोल्दै..."
                    } else if (isEn) "Unlock" else "खोल्नुहोस्",
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (store.biometricEnabled() && bioAvailable) {
                OutlinedButton(
                    onClick = { tryBiometric() },
                    enabled = !bioBusy,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(PIcons.Fingerprint, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun VaultListStage(
    store: VaultStore,
    scope: kotlinx.coroutines.CoroutineScope,
    isEn: Boolean,
    onChanged: () -> Unit,
    onLocked: () -> Unit
) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<VaultEntry>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var editorTarget by remember { mutableStateOf<VaultEntry?>(null) }
    var editorNew by remember { mutableStateOf(false) }
    var editorNonce by remember { mutableIntStateOf(0) }
    var generatorFor by remember { mutableStateOf<String?>(null) }
    var settingsOpen by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<VaultEntry?>(null) }

    var loaded by remember { mutableStateOf(false) }
    suspend fun reloadAsync() {
        val list = withContext(Dispatchers.IO) {
            runCatching { store.loadEntries() }.getOrDefault(emptyList())
        }
        entries = list
        loaded = true
    }
    LaunchedEffect(store) { reloadAsync() }

    val filtered = entries.filter {
        query.isBlank() ||
            it.title.contains(query, true) ||
            it.username.contains(query, true) ||
            it.url.contains(query, true) ||
            it.note.contains(query, true)
    }

    fun copySecret(text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(sensitiveClip("NepTools", text))
        Toast.makeText(context, if (isEn) "Copied (clears in 45s)" else "कपी भयो (४५ सेकेण्डमा हट्छ)", Toast.LENGTH_SHORT).show()
        scope.launch {
            delay(CLIPBOARD_CLEAR_MS)
            clearClipboard(context)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(if (isEn) "Search" else "खोज्नुहोस्", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.weight(1f)
            )
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable { settingsOpen = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.Gear, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(19.dp))
            }
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { editorNew = true; editorNonce++ },
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.Plus, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(21.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!loaded) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        PIcons.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (entries.isEmpty()) {
                            if (isEn) "No saved passwords yet.\nTap + to add your first entry." else "अझै पासवर्ड सुरक्षित छैन।\n+ थिचेर पहिलो प्रविष्टि थप्नुहोस्।"
                        } else {
                            if (isEn) "No matches found." else "कुनै मिल्ने भेटिएन।"
                        },
                        textAlign = TextAlignCenter,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(filtered, key = { it.id }) { entry ->
                    EntryCard(
                        entry = entry,
                        isEn = isEn,
                        onCopyUser = { copySecret(entry.username) },
                        onCopyPass = { copySecret(entry.password) },
                        onEdit = { editorTarget = entry; editorNonce++ },
                        onDelete = { deleteTarget = entry }
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }

    if (editorNew || editorTarget != null) {
        EditorDialog(
            existing = editorTarget,
            isEn = isEn,
            nonce = editorNonce,
            onDismiss = { editorNew = false; editorTarget = null },
            onSave = { e ->
                scope.launch(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    val list = store.loadEntries().toMutableList()
                    if (e.id.isBlank()) {
                        list.add(e.copy(id = UUID.randomUUID().toString(), createdMs = now, updatedMs = now))
                    } else {
                        val idx = list.indexOfFirst { it.id == e.id }
                        if (idx >= 0) list[idx] = e.copy(updatedMs = now) else list.add(e)
                    }
                    store.persistEntries(list)
                    withContext(Dispatchers.Main) {
                        editorNew = false
                        editorTarget = null
                        onChanged()
                    }
                    reloadAsync()
                }
            },
            onOpenGenerator = { generatorFor = if (editorNew) "__new__" else editorTarget?.id }
        )
    }

    generatorFor?.let { targetId ->
        GeneratorDialog(
            isEn = isEn,
            onDismiss = { generatorFor = null },
            onUse = { generated ->
                generatorFor = null
                pendingGeneratedPassword = generated
                editorNonce++
                if (targetId != "__new__") {
                    editorTarget = store.loadEntries().firstOrNull { it.id == targetId } ?: editorTarget
                }
                editorNew = targetId == "__new__"
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(if (isEn) "Delete entry?" else "प्रविष्टि मेट्ने?") },
            text = { Text("\"${target.title}\" " + if (isEn) "will be removed permanently." else "स्थायी रूपमा हट्नेछ।") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        store.persistEntries(store.loadEntries().filterNot { it.id == target.id })
                        withContext(Dispatchers.Main) {
                            deleteTarget = null
                            onChanged()
                        }
                        reloadAsync()
                    }
                }) {
                    Text(if (isEn) "Delete" else "मेट्नुहोस्", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(if (isEn) "Cancel" else "रद्द") }
            }
        )
    }

    if (settingsOpen) {
        SettingsDialog(
            store = store,
            scope = scope,
            isEn = isEn,
            onDismiss = { settingsOpen = false },
            onLockedOut = {
                settingsOpen = false
                store.lock()
                onLocked()
            }
        )
    }
}

private var pendingGeneratedPassword: String? = null

private val TextAlignCenter = androidx.compose.ui.text.style.TextAlign.Center

@Composable
private fun EntryCard(
    entry: VaultEntry,
    isEn: Boolean,
    onCopyUser: () -> Unit,
    onCopyPass: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var revealed by remember(entry.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                RoundedCornerShape(13.dp)
            )
            .clickable(onClick = onEdit)
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.title.trim().take(1).uppercase().ifEmpty { "?" },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (entry.username.isNotBlank()) {
                    Text(
                        entry.username,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                if (revealed) PIcons.EyeOff else PIcons.Eye,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(19.dp)
                    .clickable { revealed = !revealed }
            )
        }
        Spacer(Modifier.height(7.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (revealed) entry.password else "•".repeat(entry.password.length.coerceAtLeast(8)),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            CopyIcon(enabled = entry.username.isNotBlank(), onCopyUser)
            Spacer(Modifier.width(6.dp))
            CopyIcon(enabled = true, onCopyPass)
        }
    }
}

@Composable
private fun CopyIcon(enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            PIcons.Copy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    show: Boolean,
    onToggleShow: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Icon(
                if (show) PIcons.EyeOff else PIcons.Eye,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onToggleShow)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StrengthMeter(password: String) {
    val result = remember(password) { PasswordToolkit.strength(password) }
    val barColor = when (result.score) {
        1 -> Color(0xFFDC2626)
        2 -> WARN_AMBER_VAULT
        3 -> Color(0xFF65A30D)
        4 -> Color(0xFF16A34A)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) { i ->
            Box(
                Modifier
                    .height(5.dp)
                    .width(38.dp)
                    .background(
                        if (i < result.score) barColor
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
        Text(
            text = if (password.isEmpty()) "" else "${result.label} - ~${result.entropyBits} bits",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val WARN_AMBER_VAULT = Color(0xFFD97706)

@Composable
private fun EditorDialog(
    existing: VaultEntry?,
    isEn: Boolean,
    nonce: Int,
    onDismiss: () -> Unit,
    onSave: (VaultEntry) -> Unit,
    onOpenGenerator: () -> Unit
) {
    var title by remember(existing, nonce) { mutableStateOf(existing?.title ?: "") }
    var username by remember(existing, nonce) { mutableStateOf(existing?.username ?: "") }
    var password by remember(existing, nonce) {
        mutableStateOf(
            pendingGeneratedPassword.orEmpty().also { pendingGeneratedPassword = null }
                .ifEmpty { existing?.password ?: "" }
        )
    }
    var url by remember(existing, nonce) { mutableStateOf(existing?.url ?: "") }
    var note by remember(existing, nonce) { mutableStateOf(existing?.note ?: "") }
    var show by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) if (isEn) "Add Entry" else "प्रविष्टि थप्नुहोस्" else if (isEn) "Edit Entry" else "प्रविष्टि सम्पादन") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text(if (isEn) "Title *" else "शीर्षक *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(if (isEn) "Username / Email" else "प्रयोगकर्ता / इमेल") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEn) "Password" else "पासवर्ड") },
                    singleLine = true,
                    visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row {
                            Icon(
                                PIcons.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(19.dp)
                                    .clickable { onOpenGenerator() }
                            )
                            Spacer(Modifier.width(10.dp))
                            Icon(
                                if (show) PIcons.EyeOff else PIcons.Eye,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(19.dp)
                                    .clickable { show = !show }
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                StrengthMeter(password)
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(if (isEn) "Website (optional)" else "वेबसाइट (ऐच्छिक)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isEn) "Note (optional)" else "नोट (ऐच्छिक)") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank()) {
                    error = if (isEn) "Title is required" else "शीर्षक आवश्यक छ"
                    return@TextButton
                }
                onSave(
                    (existing ?: VaultEntry("", "", "", "", "", "", 0L, 0L)).copy(
                        title = title.trim(),
                        username = username.trim(),
                        password = password,
                        url = url.trim(),
                        note = note.trim()
                    )
                )
            }) {
                Text(if (isEn) "Save" else "सुरक्षित", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "रद्द") }
        }
    )
}

@Composable
private fun GeneratorDialog(
    isEn: Boolean,
    onDismiss: () -> Unit,
    onUse: (String) -> Unit
) {
    var length by remember { mutableStateOf(18f) }
    var upper by remember { mutableStateOf(true) }
    var lower by remember { mutableStateOf(true) }
    var digitsOn by remember { mutableStateOf(true) }
    var symbolsOn by remember { mutableStateOf(true) }
    var noAmbiguous by remember { mutableStateOf(true) }
    var generated by remember {
        mutableStateOf(PasswordToolkit.generate(length.toInt(), upper, lower, digitsOn, symbolsOn, noAmbiguous))
    }

    fun regenerate() {
        generated = PasswordToolkit.generate(length.toInt(), upper, lower, digitsOn, symbolsOn, noAmbiguous)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEn) "Password Generator" else "पासवर्ड जेनेरेटर") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    generated,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                )
                StrengthMeter(generated)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Length: ${length.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { regenerate() }) { Text(if (isEn) "Regenerate" else "फेरि बनाउनुहोस्") }
                }
                Slider(value = length, onValueChange = { length = it }, valueRange = 8f..48f, steps = 39)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = upper, onClick = { upper = !upper; regenerate() }, label = { Text(if (isEn) "A-Z" else "A-Z") })
                    FilterChip(selected = lower, onClick = { lower = !lower; regenerate() }, label = { Text(if (isEn) "a-z" else "a-z") })
                    FilterChip(selected = digitsOn, onClick = { digitsOn = !digitsOn; regenerate() }, label = { Text(if (isEn) "0-9" else "०-९") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = symbolsOn, onClick = { symbolsOn = !symbolsOn; regenerate() }, label = { Text(if (isEn) "#\$&" else "#\$&") })
                    FilterChip(
                        selected = noAmbiguous,
                        onClick = { noAmbiguous = !noAmbiguous; regenerate() },
                        label = { Text(if (isEn) "No look-alikes" else "उस्तै नदेखिने") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onUse(generated) }) {
                Text(if (isEn) "Use" else "प्रयोग", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "रद्द") }
        }
    )
}

@Composable
private fun SettingsDialog(
    store: VaultStore,
    scope: kotlinx.coroutines.CoroutineScope,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onLockedOut: () -> Unit
) {
    val context = LocalContext.current
    var changeMasterOpen by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }
    var autoLock by remember { mutableStateOf(store.autoLockEnabled()) }
    var message by remember { mutableStateOf<String?>(null) }
    var importConfirm by remember { mutableStateOf(false) }
    var importBytes by remember { mutableStateOf<ByteArray?>(null) }
    var importPw by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var importPwOpen by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(VaultStore.BACKUP_MIME)
    ) { uri ->
        if (uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val bytes = store.exportPayload()
                val ok = if (bytes != null) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } != null
                    }.getOrDefault(false)
                } else false
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    message = if (ok) {
                        if (isEn) "Backup exported - keep it private" else "ब्याकअप निर्यात भयो - सुरक्षित राख्नुहोस्"
                    } else {
                        if (isEn) "Export failed" else "निर्यात असफल"
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val bytes = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull()
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    if (bytes == null) {
                        message = if (isEn) "Could not read file" else "फाइल पढ्न सकिएन"
                    } else {
                        importBytes = bytes
                        importPw = ""
                        importError = null
                        importPwOpen = true
                    }
                }
            }
        }
    }

    val bioSupported = remember { BiometricGateActivity.canUseBiometrics(context) }
    var bioEnabled by remember { mutableStateOf(store.biometricEnabled() && bioSupported) }

    val gateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        when {
            res.resultCode == Activity.RESULT_OK -> {
                val sealed = BiometricGateActivity.lastSealedWrap
                if (sealed != null) {
                    store.saveBioWrap(sealed)
                    bioEnabled = true
                    message = if (isEn) "Fingerprint unlock enabled" else "फिंगरप्रिन्ट अनलक सक्रिय भयो"
                    BiometricGateActivity.lastSealedWrap = null
                } else {
                    message = if (isEn) "Could not enable biometrics" else "बायोमेट्रिक सक्रिय गर्न सकिएन"
                }
            }
            res.resultCode == BiometricGateActivity.RESULT_USER_CANCEL -> {}
            else -> message = if (isEn) "Could not enable biometrics" else "बायोमेट्रिक सक्रिय गर्न सकिएन"
        }
    }

    fun enableBiometric() {
        try {
            gateLauncher.launch(
                BiometricGateActivity.createIntent(
                    context,
                    BiometricGateActivity.MODE_ENCRYPT,
                    VaultCrypto.keyBytes(VaultStore.Session.dataKey!!)
                )
            )
        } catch (_: Throwable) {
            message = if (isEn) "Biometric unavailable" else "बायोमेट्रिक उपलब्ध छैन"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEn) "Vault Settings" else "भल्ट सेटिङ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingRow(
                    icon = PIcons.Refresh,
                    label = if (isEn) "Change master password" else "मास्टर पासवर्ड परिवर्तन",
                    onClick = { changeMasterOpen = true }
                )
                SettingRow(
                    icon = PIcons.Upload,
                    label = if (isEn) "Export encrypted backup" else "इन्क्रिप्टेड ब्याकअप निर्यात",
                    onClick = {
                        val name = "NepTools_Vault_" + java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.US)
                            .format(java.util.Date()) + "." + VaultStore.BACKUP_EXT
                        exportLauncher.launch(name)
                    }
                )
                SettingRow(
                    icon = PIcons.Download,
                    label = if (isEn) "Import backup (replaces vault)" else "ब्याकअप आयात (वर्तमान मेटिन्छ)",
                    danger = true,
                    onClick = { importConfirm = true }
                )
                if (bioSupported) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            PIcons.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (isEn) "Fingerprint unlock" else "फिंगरप्रिन्ट अनलक",
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = bioEnabled,
                            onCheckedChange = { want ->
                                if (want) enableBiometric()
                                else {
                                    store.disableBiometric()
                                    bioEnabled = false
                                }
                            }
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        PIcons.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isEn) "Auto-lock when app hides" else "एप लुकाउँदा अटो-लक",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = autoLock,
                        onCheckedChange = {
                            autoLock = it
                            store.setAutoLockEnabled(it)
                        }
                    )
                }
                SettingRow(
                    icon = PIcons.Trash,
                    label = if (isEn) "Delete vault permanently" else "भल्ट स्थायी रूपमा मेट्नुहोस्",
                    danger = true,
                    onClick = { deleteConfirm = true }
                )
                message?.let {
                    Text(it, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isEn) "Close" else "बन्द") }
        }
    )

    if (changeMasterOpen) {
        ChangeMasterDialog(
            store = store,
            isEn = isEn,
            onDismiss = { changeMasterOpen = false },
            onDone = { ok ->
                changeMasterOpen = false
                message = if (ok) {
                    if (isEn) "Master password changed" else "मास्टर पासवर्ड परिवर्तन भयो"
                } else {
                    if (isEn) "Old password was wrong" else "पुरानो पासवर्ड गलत थियो"
                }
            },
            onFailedAuth = onLockedOut
        )
    }

    if (importConfirm) {
        AlertDialog(
            onDismissRequest = { importConfirm = false },
            title = { Text(if (isEn) "Import backup?" else "ब्याकअप आयात गर्ने?") },
            text = {
                Text(
                    if (isEn)
                        "Your current vault will be replaced by the backup file. You will need the master password of that backup."
                    else "हालको भल्ट ब्याकअप फाइलले प्रतिस्थापन हुनेछ। त्यो ब्याकअपको मास्टर पासवर्ड चाहिनेछ।"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    importConfirm = false
                    importLauncher.launch(arrayOf("*/*"))
                }) { Text(if (isEn) "Choose file" else "फाइल छान्नुहोस्") }
            },
            dismissButton = {
                TextButton(onClick = { importConfirm = false }) { Text(if (isEn) "Cancel" else "रद्द") }
            }
        )
    }

    if (importPwOpen) {
        AlertDialog(
            onDismissRequest = { importPwOpen = false },
            title = { Text(if (isEn) "Backup master password" else "ब्याकअपको मास्टर पासवर्ड") },
            text = {
                Column {
                    Text(
                        if (isEn) "Enter the master password of the backup file." else "ब्याकअप फाइलको मास्टर पासवर्ड प्रविष्ट गर्नुहोस्।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importPw,
                        onValueChange = { importPw = it; importError = null },
                        label = { Text(if (isEn) "Password" else "पासवर्ड") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    importError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val bytes = importBytes
                    if (bytes == null) {
                        importPwOpen = false
                        return@TextButton
                    }
                    if (store.importPayload(bytes, importPw.toCharArray())) {
                        importPwOpen = false
                        importBytes = null
                        onDismiss()
                        onLockedOut()
                    } else {
                        importError = if (isEn) "Wrong password or invalid backup file" else "गलत पासवर्ड वा अमान्य ब्याकअप"
                    }
                }) { Text(if (isEn) "Import" else "आयात", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { importPwOpen = false }) { Text(if (isEn) "Cancel" else "रद्द") }
            }
        )
    }

    if (deleteConfirm) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = false },
            title = { Text(if (isEn) "Delete everything?" else "सबै मेट्ने?") },
            text = {
                Text(
                    if (isEn)
                        "All saved entries and the vault itself will be destroyed. This cannot be undone."
                    else "सबै प्रविष्टिहरू र भल्ट नै नष्ट हुनेछ। यो पूर्ववत हुन सक्दैन।"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    store.deleteVault()
                    deleteConfirm = false
                    onLockedOut()
                }) {
                    Text(if (isEn) "Delete all" else "सबै मेट्नुहोस्", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirm = false }) { Text(if (isEn) "Cancel" else "रद्द") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 9.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            fontSize = 14.sp,
            color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ChangeMasterDialog(
    store: VaultStore,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onDone: (Boolean) -> Unit,
    onFailedAuth: () -> Unit
) {
    var oldPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var confirmPw by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEn) "Change Master Password" else "मास्टर पासवर्ड परिवर्तन") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(
                    value = oldPw,
                    onValueChange = { oldPw = it; error = null },
                    label = { Text(if (isEn) "Current password" else "हालको पासवर्ड") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPw,
                    onValueChange = { newPw = it; error = null },
                    label = { Text(if (isEn) "New password" else "नयाँ पासवर्ड") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPw,
                    onValueChange = { confirmPw = it; error = null },
                    label = { Text(if (isEn) "Confirm new password" else "नयाँ पासवर्ड पुष्टि") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    when {
                        newPw.length < 6 -> error = if (isEn) "Use at least 6 characters" else "कम्तीमा ६ अक्षर"
                        newPw != confirmPw -> error = if (isEn) "Passwords do not match" else "पासवर्ड मिलेनन्"
                        !VaultStore.Session.unlocked -> {
                            onDismiss()
                            onFailedAuth()
                        }
                        busy -> {}
                        else -> {
                            busy = true
                            val oldChars = oldPw.toCharArray()
                            val newChars = newPw.toCharArray()
                            scope.launch(Dispatchers.Default) {
                                val ok = runCatching {
                                    store.changeMasterPassword(oldChars, newChars)
                                }.getOrDefault(false)
                                withContext(Dispatchers.Main) {
                                    busy = false
                                    onDone(ok)
                                }
                            }
                        }
                    }
                }
            ) {
                Text(
                    if (busy) {
                        if (isEn) "Working..." else "गर्दै..."
                    } else if (isEn) "Change" else "परिवर्तन",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "रद्द") }
        }
    )
}
