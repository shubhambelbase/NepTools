package com.neptools.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neptools.app.BuildConfig
import com.neptools.app.core.backup.BackupManager
import com.neptools.app.core.backup.BackupSummary
import com.neptools.app.core.data.RatesRepo
import com.neptools.app.core.security.NepToolsSecurityGuard
import kotlinx.coroutines.launch
import com.neptools.app.ui.components.HairLabel
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val cached = remember { RatesRepo.loadCached(context) }
    val isEn = ThemePrefs.lang.value == "en"

    var restoreSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var restoreError by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val ok = context.contentResolver.openOutputStream(uri)?.use { stream ->
                BackupManager.exportToStream(context, stream)
            } ?: false
            if (ok) {
                Toast.makeText(
                    context,
                    if (isEn) "Backup saved successfully" else "ब्याकअप सफलतापूर्वक सुरक्षित भयो",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to export backup" else "ब्याकअप सुरक्षित गर्न सकिएन",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val result = context.contentResolver.openInputStream(uri)?.use { stream ->
                BackupManager.restoreFromStream(context, stream)
            } ?: Result.failure(Exception("Unable to open file"))

            result.onSuccess { summary ->
                restoreSummary = summary
            }.onFailure { error ->
                restoreError = error.localizedMessage ?: "Unknown error"
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Top Bar
        ToolTopBar(
            title = T("settings"),
            subtitle = if (isEn) "Preferences, theme, backup & notifications" else "प्राथमिकता, थिम, ब्याकअप तथा सूचना सेटिङ",
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 0.dp)
        )

        Spacer(Modifier.height(14.dp))

        HairLabel(T("sec_prefs"))
        Spacer(Modifier.height(8.dp))

        // language switcher
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Text(T("language"), style = MaterialTheme.typography.titleSmall)
            Text(
                T("lang_sub"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LangOption(context, "नेपाली", "np", Modifier.weight(1f))
                LangOption(context, "English", "en", Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
        SettingRow(
            title = T("dark_t"),
            subtitle = T("dark_s"),
            checked = ThemePrefs.darkTheme.value,
            onToggle = { ThemePrefs.saveDark(context, it) }
        )

        Spacer(Modifier.height(14.dp))
        HairLabel(if (isEn) "Notifications & Smart Alerts" else "सूचना तथा स्मार्ट अलर्टहरू")
        Spacer(Modifier.height(8.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .padding(14.dp)
        ) {
            SettingRow(
                title = if (isEn) "Daily Nepali Date Notification" else "दैनिक नेपाली मिति नोटिफिकेसन",
                subtitle = if (isEn) "Persistent status bar display" else "स्ट्याटस बारमा निरन्तर मिति प्रदर्शन",
                checked = ThemePrefs.dailyDateNotification.value,
                onToggle = { ThemePrefs.saveDailyDateNotification(context, it) }
            )

            Spacer(Modifier.height(10.dp))
            SettingRow(
                title = if (isEn) "Habit Tracker Nudges" else "दैनिक बानी रिमाइन्डर",
                subtitle = if (isEn) "Daily reminder to complete habits and protect streaks" else "स्ट्रिक कायम राख्न दैनिक बानी सम्झना",
                checked = ThemePrefs.habitNotification.value,
                onToggle = { ThemePrefs.saveHabitNotification(context, it) }
            )

            Spacer(Modifier.height(10.dp))
            SettingRow(
                title = if (isEn) "Subscription & Bill Alerts" else "सदस्यता तथा बिल अलर्ट",
                subtitle = if (isEn) "Timely reminder before subscription renewal date" else "नवीकरण हुनु अघि पूर्व सूचना",
                checked = ThemePrefs.subNotification.value,
                onToggle = { ThemePrefs.saveSubNotification(context, it) }
            )

            Spacer(Modifier.height(10.dp))
            SettingRow(
                title = if (isEn) "Rain & Weather Alerts" else "वर्षा तथा मौसम अलर्ट",
                subtitle = if (isEn) "Precipitation and rain probability notifications" else "वर्षा हुने सम्भावनाको पूर्व सूचना",
                checked = ThemePrefs.weatherNotification.value,
                onToggle = { ThemePrefs.saveWeatherNotification(context, it) }
            )

            Spacer(Modifier.height(10.dp))
            SettingRow(
                title = if (isEn) "Festival & Fasting Alerts" else "चाडपर्व तथा एकादशी व्रत अलर्ट",
                subtitle = if (isEn) "Reminders for Ekadashi fasts, Parana timings & major festivals" else "एकादशी व्रत, पारणा समय तथा मुख्य चाडपर्वको पूर्व सूचना",
                checked = ThemePrefs.festivalNotification.value,
                onToggle = { ThemePrefs.saveFestivalNotification(context, it) }
            )
        }

        Spacer(Modifier.height(14.dp))
        HairLabel(if (isEn) "Data Backup & Restore" else "डेटा ब्याकअप र रिस्टोर")
        Spacer(Modifier.height(8.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .padding(14.dp)
        ) {
            Text(
                text = if (isEn) "Offline Local Backup" else "अफलाइन लोकल ब्याकअप",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isEn) "Safely export your habits, subscriptions, notes, and events into a local backup file or restore from a previous backup."
                       else "बानी ट्र्याकर, सदस्यता, नोट र क्यालेन्डर रिमाइन्डरहरू सुरक्षित रूपमा सुरक्षित गर्नुहोस् वा पुनर्स्थापना गर्नुहोस्।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val fileName = "neptools_backup_${java.time.LocalDate.now()}.json"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (isEn) "Export Backup" else "ब्याकअप सुरक्षित",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        restoreLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (isEn) "Restore Backup" else "पुनर्स्थापना",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        HairLabel(if (isEn) "Security & Integrity" else "सुरक्षा तथा अखण्डता")
        Spacer(Modifier.height(8.dp))
        SecuritySection(isEn)

        Spacer(Modifier.height(14.dp))
        HairLabel(if (isEn) "About NepTools" else "हाम्रो बारे")
        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { com.neptools.app.ui.navigation.AppNavigator.navigateTo(com.neptools.app.ui.navigation.Routes.ABOUT) }
                .padding(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PIcons.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NepTools",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Shubham Belbase · v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    PIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        restoreSummary?.let { summary ->
            AlertDialog(
                onDismissRequest = { restoreSummary = null },
                title = {
                    Text(
                        text = if (isEn) "Backup Restored Successfully" else "ब्याकअप सफलतापूर्वक पुनर्स्थापना भयो",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isEn) "Habits: ${summary.habitCount}" else "बानीहरू: ${summary.habitCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (isEn) "Subscriptions: ${summary.subscriptionCount}" else "सदस्यताहरू: ${summary.subscriptionCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (isEn) "Notes: ${summary.notesCount}" else "नोटहरू: ${summary.notesCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (isEn) "Calendar Events: ${summary.eventCount}" else "क्यालेन्डर कार्यक्रमहरू: ${summary.eventCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { restoreSummary = null }) {
                        Text(if (isEn) "OK" else "ठीक छ")
                    }
                }
            )
        }

        restoreError?.let { err ->
            AlertDialog(
                onDismissRequest = { restoreError = null },
                title = {
                    Text(
                        text = if (isEn) "Restore Error" else "पुनर्स्थापना त्रुटि",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(text = err, style = MaterialTheme.typography.bodyMedium)
                },
                confirmButton = {
                    Button(onClick = { restoreError = null }) {
                        Text(if (isEn) "OK" else "बन्द")
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SecuritySection(isEn: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val status by NepToolsSecurityGuard.status.collectAsStateWithLifecycle()
    val report = status.report

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        Text(
            text = if (isEn) "Device integrity check" else "यन्त्र अखण्डता जाँच",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isEn)
                "NepTools verifies its own signature and scans for debugging or hooking tools. Rooted devices are allowed and simply reported here."
            else "नेपटूल्सले आफ्नै हस्ताक्षर जाँच्छ र डिबगिङ वा हुकिङ उपकरण खोज्छ। रुट गरिएका यन्त्रहरू अनुमति छन् र यहाँ मात्र जानकारी दिइन्छ।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        if (report == null) {
            Text(
                text = if (isEn) "Scanning..." else "जाँच हुँदैछ...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            IntegrityRow(
                label = if (isEn) "App signature" else "एप हस्ताक्षर",
                state = if (report.isSignatureValid) IntegrityState.PASS else IntegrityState.FAIL,
                isEn = isEn
            )
            IntegrityRow(
                label = if (isEn) "Debugger" else "डिबगर",
                state = if (report.isDebuggerDetected) IntegrityState.FAIL else IntegrityState.PASS,
                isEn = isEn
            )
            IntegrityRow(
                label = if (isEn) "Hooking tools" else "हुकिङ उपकरण",
                state = if (report.isHookingDetected) IntegrityState.FAIL else IntegrityState.PASS,
                isEn = isEn
            )
            IntegrityRow(
                label = if (isEn) "Root access" else "रुट पहुँच",
                state = if (report.isRootDetected) IntegrityState.INFO else IntegrityState.PASS,
                isEn = isEn
            )
            IntegrityRow(
                label = if (isEn) "Native tracer check" else "नेटिभ ट्रेसर जाँच",
                state = when (report.nativeIntegrityOk) {
                    true -> IntegrityState.PASS
                    false -> IntegrityState.FAIL
                    null -> IntegrityState.INFO
                },
                isEn = isEn
            )
        }

        Spacer(Modifier.height(10.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { scope.launch { NepToolsSecurityGuard.refresh(context) } },
            enabled = !status.isAuditing,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                if (status.isAuditing) {
                    if (isEn) "Checking..." else "जाँच हुँदैछ..."
                } else {
                    if (isEn) "Re-check now" else "पुनः जाँच"
                },
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private enum class IntegrityState { PASS, FAIL, INFO }

@Composable
private fun IntegrityRow(label: String, state: IntegrityState, isEn: Boolean) {
    val color = when (state) {
        IntegrityState.PASS -> MaterialTheme.colorScheme.primary
        IntegrityState.FAIL -> MaterialTheme.colorScheme.error
        IntegrityState.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val valueText = when (state) {
        IntegrityState.PASS -> if (isEn) "Pass" else "ठीक"
        IntegrityState.FAIL -> if (isEn) "Fail" else "समस्या"
        IntegrityState.INFO -> if (isEn) "Info" else "जानकारी"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valueText, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LangOption(context: android.content.Context, label: String, code: String, modifier: Modifier = Modifier) {
    val selected = ThemePrefs.lang.value == code
    Box(
        modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.small
            )
            .clickable { ThemePrefs.saveLang(context, code) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
