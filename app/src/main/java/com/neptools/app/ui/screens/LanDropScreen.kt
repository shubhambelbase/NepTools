package com.neptools.app.ui.screens

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.neptools.app.core.server.LanDropServer
import com.neptools.app.core.server.SharedFileInfo
import com.neptools.app.core.util.QrCodeGenerator
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.io.File
import java.text.DecimalFormat
import kotlinx.coroutines.Dispatchers

@Composable
fun LanDropScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"
    val serverState by LanDropServer.state.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Host/Share, 1 = Received Files
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var isZipping by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            LanDropServer.addSharedFile(context, uri)
        }
    }

    // FIX: Single refresh trigger (was double: LaunchedEffect + DisposableEffect)
    LaunchedEffect(activeTab) {
        LanDropServer.refreshReceivedFiles()
    }

    val serverUrl = if (serverState.ipAddress.isNotEmpty()) {
        "http://${serverState.ipAddress}:${serverState.port}"
    } else "Connecting..."

    // The QR carries the session code so scanning it opens an already-authorised session,
    // while the visible URL stays clean for people typing the address by hand.
    val qrUrl = if (serverState.isRunning && serverState.sessionCode.isNotEmpty()) {
        "$serverUrl/?k=${serverState.sessionCode}"
    } else serverUrl

    val qrBitmap = remember(serverState.isRunning, qrUrl) {
        if (serverState.isRunning && serverState.ipAddress.isNotEmpty()) {
            QrCodeGenerator.generateQrBitmap(qrUrl, size = 480)
        } else null
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
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
                Icon(PIcons.ChevronLeft, if (isEn) "Back" else "फिर्ता", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "High-Speed LAN File Drop" else "द्रुत वाईफाई फाइल ट्रान्सफर",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Mode Tabs
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { activeTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) "Share & Host (${serverState.sharedFiles.size})" else "सेयर तथा सर्भर (${serverState.sharedFiles.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable {
                        activeTab = 1
                        LanDropServer.refreshReceivedFiles()
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEn) "Received (${serverState.receivedFiles.size})" else "प्राप्त फाइलहरू (${serverState.receivedFiles.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (activeTab == 0) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Session code card - the browser cannot list or move files without it.
                if (serverState.isRunning && serverState.sessionCode.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    if (isEn) "Session code" else "सेसन कोड",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    serverState.sessionCode,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 8.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    if (isEn)
                                        "Type this code in the browser before any file can be listed or transferred. The server stops by itself after 10 minutes of inactivity."
                                    else "कुनै फाइल हेर्न वा सार्नु अघि ब्राउजरमा यो कोड टाइप गर्नुहोस्। १० मिनेट निष्क्रिय भएपछि सर्भर आफै बन्द हुन्छ।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (isEn)
                                        "Only enable this on a network you trust. Anyone on the same Wi-Fi who knows the code can transfer files."
                                    else "विश्वास गर्न सकिने नेटवर्कमा मात्र चालु गर्नुहोस्। कोड थाहा पाउने जो कोहीले फाइल सार्न सक्छ।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Server Switch Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!serverState.isRunning) {
                                    LanDropServer.startServer(context)
                                    Toast.makeText(context, if (isEn) "Starting LAN Server..." else "सर्भर सुरु हुँदैछ...", Toast.LENGTH_SHORT).show()
                                } else {
                                    LanDropServer.stopServer()
                                    Toast.makeText(context, if (isEn) "LAN Server Stopped" else "सर्भर बन्द भयो", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (serverState.isRunning) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (serverState.isRunning) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .background(
                                                if (serverState.isRunning) Color(0xFF22C55E).copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            PIcons.WifiDrop,
                                            contentDescription = null,
                                            tint = if (serverState.isRunning) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            if (serverState.isRunning) (if (isEn) "Wi-Fi Server Running" else "सर्भर चालु छ")
                                            else (if (isEn) "Wi-Fi Server Offline" else "सर्भर बन्द छ"),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (serverState.isRunning) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            if (serverState.isRunning) (if (isEn) "Ready for phone or PC pairing" else "कम्प्युटर वा फोनबाट जोड्न तयार")
                                            else (if (isEn) "Tap to start wireless transfer" else "फाइल पठाउन यहाँ थिच्नुहोस्"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = serverState.isRunning,
                                    onCheckedChange = { start ->
                                        if (start) {
                                            LanDropServer.startServer(context)
                                            Toast.makeText(context, if (isEn) "Starting LAN Server..." else "सर्भर सुरु हुँदैछ...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            LanDropServer.stopServer()
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF16A34A)
                                    )
                                )
                            }

                            // Active QR Code & URL
                            AnimatedVisibility(
                                visible = serverState.isRunning,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(
                                    Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (qrBitmap != null) {
                                        Box(
                                            Modifier
                                                .size(190.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White)
                                                .border(2.dp, Color(0xFF22C55E), RoundedCornerShape(14.dp))
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                bitmap = qrBitmap.asImageBitmap(),
                                                contentDescription = if (isEn) "Scan QR to Connect" else "QR स्क्यान गरेर जडान गर्नुहोस्",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Text(
                                            if (isEn) "Scan QR with other phone to connect & exchange files"
                                            else "अर्को फोन वा क्यामेराले यो QR स्क्यान गर्नुहोस्",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color(0xFF15803D)
                                        )
                                    }

                                    // URL Box
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                serverUrl,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color(0xFF15803D)
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("LanURL", serverUrl))
                                                        Toast.makeText(context, if (isEn) "URL Copied!" else "लिंक कपी गरियो!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text(if (isEn) "Copy" else "कपी", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Files Button
                item {
                    Button(
                        onClick = { filePicker.launch(arrayOf("*/*")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(PIcons.Upload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEn) "+ Select Files to Share (Phone/PC)" else "+ सेयर गर्न फाइल थप्नुहोस्",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Share All as ZIP (for shared files)
                if (serverState.sharedFiles.size > 1) {
                    item {
                        OutlinedButton(
                            onClick = {
                                if (isZipping) return@OutlinedButton
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        isZipping = true
                                        val zip = LanDropServer.createSharedZip(context)
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isZipping = false
                                            if (zip != null && zip.exists()) {
                                                try {
                                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zip)
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/zip"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, if (isEn) "Share ${serverState.sharedFiles.size} files as ZIP" else "${serverState.sharedFiles.size} फाइल ZIP सेयर"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, if (isEn) "Zip failed: ${e.message}" else "जिप असफल: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            } else Toast.makeText(context, if (isEn) "No files to zip" else "जिप गर्न फाइल छैन", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isZipping = false
                                            Toast.makeText(context, if (isEn) "Error: ${e.message}" else "त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isZipping
                        ) {
                            if (isZipping) {
                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(if (isEn) "Preparing ZIP..." else "ZIP बनाउँदै...")
                            } else {
                                Icon(PIcons.Download, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (isEn) "Share All as ZIP (${serverState.sharedFiles.size})" else "सबै ZIP सेयर (${serverState.sharedFiles.size})", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                // Shared Files List
                if (serverState.sharedFiles.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isEn) "No files queued yet. Tap '+ Select Files' to let other devices download them."
                                else "कुनै फाइल थपिएको छैन। फाइल पठाउन माथि थिच्नुहोस्।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(serverState.sharedFiles) { file ->
                        SharedFileRow(
                            file = file,
                            isEn = isEn,
                            onRemove = { LanDropServer.removeSharedFile(file.id) }
                        )
                    }
                }
            }
        } else {
            // Received Files Tab
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (isEn) "Download Directory" else "फाइल डाउनलोड स्थान",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isEn) "Download/NepTools/" else "Download/NepTools/",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF16A34A)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, if (isEn) "Open Downloads folder in File Manager" else "फाइल म्यानेजरमा डाउनलोड फोल्डर खोल्नुहोस्", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(if (isEn) "Open Folder" else "फोल्डर खोल्नुहोस्", style = MaterialTheme.typography.labelSmall)
                            }
                            com.neptools.app.ui.components.AnimatedRefreshIconButton(
                                onClick = { LanDropServer.refreshReceivedFiles() },
                                iconSize = 20.dp,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (serverState.receivedFiles.isNotEmpty()) {
                    item {
                        Button(
                            onClick = {
                                if (isZipping) return@Button
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        isZipping = true
                                        val zip = LanDropServer.createReceivedZip(context)
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isZipping = false
                                            if (zip != null && zip.exists()) {
                                                try {
                                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zip)
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/zip"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, if (isEn) "Share ZIP (${serverState.receivedFiles.size} files)" else "जिप फाइल सेयर (${serverState.receivedFiles.size} फाइल)"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, if (isEn) "Zip failed: ${e.message}" else "जिप असफल: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, if (isEn) "No files to zip" else "जिप गर्न फाइल छैन", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isZipping = false
                                            Toast.makeText(context, if (isEn) "Error: ${e.message}" else "त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            enabled = !isZipping
                        ) {
                            if (isZipping) {
                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(if (isEn) "Preparing ZIP..." else "ZIP बनाउँदै...", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            } else {
                                Icon(PIcons.Download, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (isEn) "Download All as ZIP (${serverState.receivedFiles.size})" else "सबै ZIP मा डाउनलोड (${serverState.receivedFiles.size})", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                if (serverState.receivedFiles.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isEn) "No files received yet. Files sent from PC or other phones are automatically downloaded to Download/NepTools/."
                                else "कुनै फाइल प्राप्त भएको छैन। कम्प्युटर वा फोनबाट पठाएका फाइल सिधै Download/NepTools मा सेभ हुन्छन्।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(serverState.receivedFiles) { file ->
                        ReceivedFileRow(file = file, isEn = isEn, context = context)
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedFileRow(file: SharedFileInfo, isEn: Boolean, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                file.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                formatFileSize(file.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(
            onClick = onRemove,
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(if (isEn) "Remove" else "हटाउनुहोस्", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
        }
    }
}

@Composable
private fun ReceivedFileRow(file: SharedFileInfo, isEn: Boolean, context: Context) {
    val ext = file.name.substringAfterLast('.', "").lowercase().take(4)
    val typeColor = when (ext) {
        "pdf" -> Color(0xFFDC2626)
        "jpg", "jpeg", "png", "webp", "heic" -> Color(0xFF0EA5E9)
        "mp4", "mkv", "mov" -> Color(0xFF9333EA)
        "mp3", "m4a", "wav" -> Color(0xFFF59E0B)
        "zip", "rar", "7z" -> Color(0xFF6366F1)
        "doc", "docx", "txt" -> Color(0xFF2563EB)
        else -> MaterialTheme.colorScheme.primary
    }
    val typeBg = typeColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(typeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ext.uppercase().take(3).ifEmpty { "FILE" }, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp), color = typeColor)
                }
                Column(Modifier.weight(1f)) {
                    Text(file.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFDCFCE7).copy(alpha = 0.9f)).padding(horizontal = 7.dp, vertical = 2.dp)) {
                            Text(formatFileSize(file.size), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = Color(0xFF15803D))
                        }
                        Text(if (isEn) "Download/NepTools" else "Download/NepTools", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Enhanced Action Buttons: Download / Open / Share
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download - tonal green with icon badge
                Box(
                    Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF16A34A)).clickable {
                            val local = file.localFile ?: File(LanDropServer.getStorageDir(), file.name)
                            if (local.exists()) {
                                MediaScannerConnection.scanFile(context, arrayOf(local.absolutePath), null, null)
                                Toast.makeText(context, if (isEn) "✓ Saved to Download/NepTools/${file.name}" else "✓ Download/NepTools मा सेभ भयो", Toast.LENGTH_LONG).show()
                            } else Toast.makeText(context, if (isEn) "File not found" else "फाइल भेटिएन", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(22.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                            Icon(PIcons.Download, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                        Text(if (isEn) "Save" else "सेभ", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp), color = Color.White)
                    }
                }

                // Open - primary filled with elevation
                Box(
                    Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary).clickable {
                            val local = file.localFile ?: File(LanDropServer.getStorageDir(), file.name)
                            if (local.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", local)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, if (isEn) "Open File" else "फाइल खोल्नुहोस्"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, if (isEn) "Cannot open: ${e.message}" else "खोल्न सकिएन: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else Toast.makeText(context, if (isEn) "File not found" else "फाइल भेटिएन", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(PIcons.Upload, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(if (isEn) "Open" else "खोल्नुहोस्", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp), color = Color.White)
                    }
                }

                // Share - outlined with tint
                Box(
                    Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable {
                            val local = file.localFile ?: File(LanDropServer.getStorageDir(), file.name)
                            if (local.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", local)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = context.contentResolver.getType(uri) ?: "*/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, if (isEn) "Share File" else "फाइल सेयर गर्नुहोस्"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, if (isEn) "Cannot share: ${e.message}" else "सेयर गर्न सकिएन: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else Toast.makeText(context, if (isEn) "File not found" else "फाइल भेटिएन", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(PIcons.Share, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text(if (isEn) "Share" else "सेयर", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 0) return "Unknown"
    val df = DecimalFormat("#.##")
    return when {
        bytes >= 1024 * 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        bytes >= 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
        bytes >= 1024 -> "${df.format(bytes / 1024.0)} KB"
        else -> "$bytes B"
    }
}
