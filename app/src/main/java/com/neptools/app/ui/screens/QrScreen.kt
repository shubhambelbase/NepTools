package com.neptools.app.ui.screens

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.util.QrCodeGenerator
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun QrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"

    var qrType by remember { mutableIntStateOf(0) } // 0 = URL, 1 = Text, 2 = Phone, 3 = Wi-Fi, 4 = eSewa
    var inputText by remember { mutableStateOf("https://neptools.app") }
    var wifiSsid by remember { mutableStateOf("Home_WiFi") }
    var wifiPassword by remember { mutableStateOf("Namaste@123") }
    var phoneInput by remember { mutableStateOf("9800000000") }
    var esewaId by remember { mutableStateOf("9848028445") }
    var esewaName by remember { mutableStateOf("Subham Belbase") }

    val payload = remember(qrType, inputText, wifiSsid, wifiPassword, phoneInput, esewaId, esewaName) {
        when (qrType) {
            0 -> if (inputText.startsWith("http")) inputText else "https://$inputText"
            1 -> inputText
            2 -> "tel:$phoneInput"
            3 -> "WIFI:S:$wifiSsid;T:WPA;P:$wifiPassword;;"
            else -> {
                val cleanId = esewaId.trim()
                val cleanName = esewaName.trim()
                if (cleanName.isNotBlank()) {
                    """{"eSewa_id":"$cleanId","name":"$cleanName"}"""
                } else {
                    """{"eSewa_id":"$cleanId"}"""
                }
            }
        }
    }

    // Generate real scannable QR bitmap via ZXing
    val qrBitmap = remember(payload) {
        QrCodeGenerator.generateQrBitmap(content = payload, size = 512)
    }

    val typeOptions = listOf(
        T("qr_type_web"),
        T("qr_type_txt"),
        T("qr_type_ph"),
        T("qr_type_wifi"),
        T("qr_type_esewa")
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolTopBar(
            title = T("qr_title"),
            subtitle = if (isEn) "Instant scan & code generator" else "द्रुत स्क्यान तथा क्युआर कोड जेनेरेटर",
            onBack = onBack
        )

        // Type Switcher Row
        Box(Modifier.padding(horizontal = 16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(3.dp)
            ) {
                typeOptions.forEachIndexed { index, name ->
                    val sel = qrType == index
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (sel) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { qrType = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    when (qrType) {
                        0 -> {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                label = { Text(T("qr_hint_web")) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        1 -> {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                label = { Text(T("qr_hint_txt")) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        2 -> {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text(T("qr_hint_ph")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        3 -> {
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = { wifiSsid = it },
                                label = { Text(T("qr_hint_wifi_s")) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = wifiPassword,
                                onValueChange = { wifiPassword = it },
                                label = { Text(T("qr_hint_wifi_p")) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> {
                            OutlinedTextField(
                                value = esewaId,
                                onValueChange = { esewaId = it },
                                label = { Text(if (isEn) "eSewa ID / Mobile Number" else "eSewa ID / मोबाइल नम्बर") },
                                placeholder = { Text("98xxxxxxxx") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = esewaName,
                                onValueChange = { esewaName = it },
                                label = { Text(if (isEn) "Account Holder Name" else "खातावालाको नाम") },
                                placeholder = { Text(if (isEn) "e.g. Subham Belbase" else "जस्तै: शुभम बेलबासे") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // QR Code Display Card (real scannable QR via ZXing)
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(210.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val bmp = qrBitmap
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = T("qr_scannable"),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = if (isEn) "Enter content to generate QR"
                                    else "QR बनाउन सामग्री लेख्नुहोस्",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            T("qr_scannable"),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            payload.take(45) + if (payload.length > 45) "..." else "",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ONLY Save QR Image Button (share data and copy qr data removed per user request)
            item {
                Button(
                    onClick = {
                        qrBitmap?.let { bmp -> saveQrBitmap(context, bmp, isEn) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = PIcons.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isEn) "Save QR Image" else "QR तस्विर सेभ गर्नुहोस्",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/** Saves the QR bitmap to the device Pictures/NepTools folder and registers with MediaStore. */
private fun saveQrBitmap(
    context: android.content.Context,
    bitmap: android.graphics.Bitmap,
    isEn: Boolean
) {
    try {
        val filename = "NepTools_QR_${System.currentTimeMillis()}.png"
        val mime = "image/png"
        var savedUri: android.net.Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mime)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NepTools")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                savedUri = uri
            }
        } else {
            val dir = java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "NepTools"
            )
            if (!dir.exists()) dir.mkdirs()
            val file = java.io.File(dir, filename)
            java.io.FileOutputStream(file).use { stream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            }
            savedUri = android.net.Uri.fromFile(file)
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf(mime),
                null
            )
        }

        if (savedUri != null) {
            Toast.makeText(
                context,
                if (isEn) "QR image saved to Pictures/NepTools"
                else "QR तस्विर Pictures/NepTools मा सेभ भयो",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                if (isEn) "Failed to save QR image" else "QR सेभ गर्न सकिएन",
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            if (isEn) "Error: ${e.localizedMessage}" else "QR सेभ गर्न समस्या भयो",
            Toast.LENGTH_SHORT
        ).show()
    }
}
