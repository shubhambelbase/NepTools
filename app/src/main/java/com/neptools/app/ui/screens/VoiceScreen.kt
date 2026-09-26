package com.neptools.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.ripple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import com.neptools.app.core.util.setPlainText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
//
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch
import com.neptools.app.core.notes.Note
import com.neptools.app.core.notes.NotesStore
import com.neptools.app.ui.components.InkButton
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoiceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val isEn = ThemePrefs.lang.value == "en"

    var hasAudioPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasAudioPermission = granted }

    var selectedLang by remember { mutableStateOf("ne-NP") }
    var listening by remember { mutableStateOf(false) }
    var partial by remember { mutableStateOf("") }
    var transcript by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var rmsLevel by remember { mutableFloatStateOf(0f) }
    var copyNotice by remember { mutableStateOf<String?>(null) }
    var savedNotice by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    // Saved voice notes list
    var savedNotes by remember { mutableStateOf<List<Note>>(emptyList()) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(context) {
        savedNotes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            NotesStore.load(context).filter { it.kind == "voice" }
        }
    }

    LaunchedEffect(copyNotice) {
        if (copyNotice != null) {
            kotlinx.coroutines.delay(2000)
            copyNotice = null
        }
    }

    LaunchedEffect(savedNotice) {
        if (savedNotice) {
            kotlinx.coroutines.delay(2500)
            savedNotice = false
        }
    }

    LaunchedEffect(confirmClear) {
        if (confirmClear) {
            kotlinx.coroutines.delay(3500)
            confirmClear = false
        }
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Throwable) {}
        }
    }

    fun startListening() {
        if (speechRecognizer == null || !SpeechRecognizer.isRecognitionAvailable(context)) {
            error = if (isEn) {
                "Speech recognizer not available. Please install Google Speech Services."
            } else {
                "यस फोनमा आवाज पहिचान उपलब्ध छैन। कृपया गुगल स्पिच सेवा स्थापना गर्नुहोस्।"
            }
            return
        }
        error = null
        partial = ""
        savedNotice = false
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLang)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                rmsLevel = rmsdB.coerceIn(0f, 10f)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                listening = false
                rmsLevel = 0f
            }
            override fun onError(err: Int) {
                listening = false
                rmsLevel = 0f
                error = when (err) {
                    SpeechRecognizer.ERROR_NO_MATCH -> if (isEn) "No speech detected. Please speak closer to microphone." else "केही सुनिएन — प्रस्टसँग फेरि बोल्नुहोस्"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> if (isEn) "Speech timed out. Please try again." else "आवाज आएन — माइक नजिक बोल्नुहोस्"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> if (isEn) "Microphone permission required." else "माइक अनुमति चाहिन्छ"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> if (isEn) "Network error. Check internet connection." else "इन्टरनेट समस्या — नेटवर्क जाँच गर्नुहोस्"
                    else -> if (isEn) "Recognition error ($err). Check Google Speech settings." else "सुन्न सकिएन (त्रुटि कोड $err) — कृपया आवाज पहिचान सेवा जाँच गर्नुहोस्"
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val p = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!p.isNullOrBlank()) partial = p
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onResults(results: Bundle?) {
                listening = false
                rmsLevel = 0f
                val best = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!best.isNullOrBlank()) {
                    transcript = if (transcript.isBlank()) best else "$transcript $best"
                    partial = ""
                }
            }
        })
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Throwable) {}
        listening = false
        rmsLevel = 0f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (listening) 1.28f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (listening) 0.06f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Bar & Title
        item {
            ToolTopBar(
                title = if (isEn) "Voice Notes" else "नेपाली भ्वाइस नोट",
                subtitle = if (isEn) "Speech-to-text live transcription" else "नेपाली आवाजबाट पाठ रूपान्तरण",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 0.dp)
            )
        }

        // Language Mode Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Speech Language:" else "बोल्ने भाषा:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedLang == "ne-NP",
                            onClick = {
                                if (!listening) selectedLang = "ne-NP"
                            },
                            label = { Text("नेपाली") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedLang == "en-US",
                            onClick = {
                                if (!listening) selectedLang = "en-US"
                            },
                            label = { Text("English") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Microphone Permission Missing Card
        if (!hasAudioPermission) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isEn) "Microphone Access Required" else "माइक अनुमति आवश्यक छ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = if (isEn) {
                                "NepTools uses microphone permission solely to transcribe your speech into text on-demand. Voice data is not stored on external servers."
                            } else {
                                "भ्वाइस नोट प्रयोग गर्नका लागि माइक अनुमति चाहिन्छ। यसले तपाईंको बोलीलाई अक्षरमा रूपान्तरण गर्दछ।"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        InkButton(
                            text = if (isEn) "Grant Microphone Access" else "माइक अनुमति दिनुहोस्",
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Hero Mic Dictation Console Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pulsing Mic Circle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            if (listening) {
                                Box(
                                    modifier = Modifier
                                        .size(105.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                            CircleShape
                                        )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (listening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false, radius = 38.dp)
                                    ) {
                                        if (listening) {
                                            stopListening()
                                        } else {
                                            startListening()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PIcons.Mic,
                                    contentDescription = "Microphone",
                                    tint = if (listening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Status indicator
                        if (listening) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFDC2626), CircleShape)
                                )
                                Text(
                                    text = if (isEn) "Listening... speak now" else "सुन्दैछ... प्रस्ट बोल्नुहोस्",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // Audio wave bars
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.height(24.dp)
                            ) {
                                val factors = listOf(0.4f, 0.8f, 1.1f, 0.7f, 0.5f)
                                factors.forEach { f ->
                                    val dynamicHeight = (6.dp + (rmsLevel * 2.2f * f).dp).coerceIn(6.dp, 24.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(dynamicHeight)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Surface(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { stopListening() },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        text = if (isEn) "Stop Dictating" else "रोक्नुहोस्",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = if (isEn) "Tap microphone to dictate" else "बोल्नका लागि माइक थिच्नुहोस्",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Real-time partial transcription bubble
                        if (partial.isNotBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = partial,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Error message
                        if (error != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Live Transcript Box & Editor Card
        if (transcript.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = PIcons.Doc,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = if (isEn) "Dictated Transcript" else "प्रतिलिपि (सम्पादन गर्न मिल्छ)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Character count pill badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${transcript.length} ${if (isEn) "chars" else "अक्षर"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = transcript,
                            onValueChange = { transcript = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp
                            ),
                            placeholder = {
                                Text(
                                    text = if (isEn) "Dictated text appears here..." else "बोलेको पाठ यहाँ देखिनेछ...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        )

                        // Action Row 1: Balanced Tactile Copy & Clear Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isCopied = copyNotice != null

                            // Copy Button with tactile spring, custom colors, and in-place feedback
                            VoiceActionButton(
                                text = if (isCopied) {
                                    if (isEn) "Copied!" else "कपी भयो!"
                                } else {
                                    if (isEn) "Copy" else "प्रतिलिपि"
                                },
                                icon = if (isCopied) PIcons.Check else PIcons.Copy,
                                tint = if (isCopied) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                                bgColor = if (isCopied) {
                                    Color(0xFF16A34A).copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                },
                                borderColor = if (isCopied) {
                                    Color(0xFF16A34A).copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                },
                                onClick = {
                                    scope.launch { clipboard.setPlainText(transcript) }
                                    copyNotice = if (isEn) "Transcript copied" else "प्रतिलिपि गरियो"
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Clear Button with soft rose styling & two-step safety for long text
                            VoiceActionButton(
                                text = if (confirmClear) {
                                    if (isEn) "Confirm?" else "पक्का खाली?"
                                } else {
                                    if (isEn) "Clear" else "खाली गर्नुहोस्"
                                },
                                icon = if (confirmClear) PIcons.Alert else PIcons.Trash,
                                tint = MaterialTheme.colorScheme.error,
                                bgColor = if (confirmClear) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                },
                                borderColor = MaterialTheme.colorScheme.error.copy(alpha = if (confirmClear) 0.45f else 0.22f),
                                onClick = {
                                    if (transcript.length > 20 && !confirmClear) {
                                        confirmClear = true
                                    } else {
                                        transcript = ""
                                        partial = ""
                                        confirmClear = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Action Row 2: Full-Width Primary Save Button
                        VoiceSaveButton(
                            text = if (savedNotice) {
                                if (isEn) "Saved to Notes" else "नोट सुरक्षित गरियो"
                            } else {
                                if (isEn) "Save Note" else "नोट सेभ गर्नुहोस्"
                            },
                            isSaved = savedNotice,
                            onClick = {
                                if (transcript.isNotBlank()) {
                                    val text = transcript.trim()
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        NotesStore.save(context, Note(text, System.currentTimeMillis(), "voice"))
                                        val list = NotesStore.load(context).filter { it.kind == "voice" }
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            savedNotes = list
                                            savedNotice = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Section: Saved Voice Notes Header
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = PIcons.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isEn) "Saved Voice Notes" else "सेभ गरिएका भ्वाइस नोटहरू",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${savedNotes.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Empty state if no saved voice notes
        if (savedNotes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isEn) "No saved voice notes yet" else "अहिलेसम्म कुनै भ्वाइस नोट सेभ गरिएको छैन",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isEn) {
                                "Dictate your thoughts above and tap 'Save Note' to preserve them here."
                            } else {
                                "माथि माइक थिचेर बोल्नुहोस् र 'सेभ गर्नुहोस्' थिचेर यहाँ सुरक्षित राख्नुहोस्।"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // List of saved voice notes
            items(savedNotes, key = { it.createdAt }) { note ->
                SavedVoiceNoteCard(
                    note = note,
                    isEn = isEn,
                    onCopy = {
                        scope.launch { clipboard.setPlainText(note.text) }
                        copyNotice = if (isEn) "Note copied to clipboard" else "नोट प्रतिलिपि गरियो"
                    },
                    onDelete = {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            NotesStore.delete(context, note.createdAt)
                            val list = NotesStore.load(context).filter { it.kind == "voice" }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                savedNotes = list
                            }
                        }
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VoiceActionButton(
    text: String,
    icon: ImageVector,
    tint: Color,
    bgColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "voiceActionBtnScale"
    )
    val haptics = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = tint),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                ),
                color = tint,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun VoiceSaveButton(
    text: String,
    isSaved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "voiceSaveBtnScale"
    )
    val haptics = LocalHapticFeedback.current
    val primaryBg = if (isSaved) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.onPrimary),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = primaryBg,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSaved) PIcons.Check else PIcons.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun SavedVoiceNoteCard(
    note: Note,
    isEn: Boolean,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(note.createdAt) {
        val sdf = SimpleDateFormat("yyyy-MM-dd • hh:mm a", Locale.getDefault())
        sdf.format(Date(note.createdAt))
    }
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(1800)
            isCopied = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Copy button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCopied) Color(0xFF16A34A).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 16.dp),
                                onClick = {
                                    onCopy()
                                    isCopied = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCopied) PIcons.Check else PIcons.Copy,
                            contentDescription = "Copy note",
                            tint = if (isCopied) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Delete button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 16.dp),
                                onClick = onDelete
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PIcons.Trash,
                            contentDescription = "Delete note",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
