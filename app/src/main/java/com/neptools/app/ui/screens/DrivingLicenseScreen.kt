package com.neptools.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import com.neptools.app.ui.screens.license.TrafficSignGraphic
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.data.license.DrivingLicenseQuestionBank
import com.neptools.app.core.data.license.LicenseCategory
import com.neptools.app.core.data.license.LicenseQuestion
import com.neptools.app.core.data.license.MockTestResult
import com.neptools.app.core.data.license.QuestionTopic
import com.neptools.app.core.data.license.SignCategory
import com.neptools.app.core.data.license.TrafficSignItem
import com.neptools.app.core.data.license.TrialGuide
import com.neptools.app.core.util.LicenseStorageManager
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.delay

private enum class LicenseMainTab(
    val titleNp: String,
    val titleEn: String,
    val icon: ImageVector
) {
    MOCK_EXAM("नमुना परीक्षा", "Mock Exam", PIcons.Award),
    PRACTICE("अभ्यास", "Practice", PIcons.BookOpen),
    SIGNS("ट्राफिक संकेत", "Road Signs", PIcons.TrafficLight),
    TRIAL_GUIDE("ट्रायल गाइड", "Trial Guide", PIcons.Shield)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingLicenseScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentLang by remember { mutableStateOf(ThemePrefs.lang.value) }
    val isEn = currentLang == "en"

    var selectedTab by remember { mutableStateOf(LicenseMainTab.MOCK_EXAM) }
    var selectedCategory by remember { mutableStateOf(LicenseCategory.CATEGORY_A) }

    Scaffold(
        topBar = {
            ToolTopBar(
                title = if (isEn) "Driving License Prep" else "लाइसेन्स परीक्षा तयारी",
                subtitle = if (isEn) "DOTM question bank, signs & mock test" else "यातायात व्यवस्था विभाग प्रश्नोत्तर तथा नमुना परीक्षा",
                onBack = onBack,
                actions = {
                    // Clean 1-Tap Language Switcher
                    androidx.compose.material3.FilledTonalButton(
                        onClick = {
                            val nextLang = if (isEn) "np" else "en"
                            currentLang = nextLang
                            ThemePrefs.saveLang(context, nextLang)
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isEn) "नेपाली" else "English",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented Category Switcher Bar (Bike/Scooter vs Car/Jeep)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Category A (Bike/Scooter)
                CategorySegmentTab(
                    isSelected = selectedCategory == LicenseCategory.CATEGORY_A,
                    title = if (isEn) "Category A (Bike/Scooter)" else "वर्ग 'क' (मोटरसाइकल/स्कुटर)",
                    icon = PIcons.Bike,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedCategory = LicenseCategory.CATEGORY_A }
                )

                // Category B (Car/Jeep)
                CategorySegmentTab(
                    isSelected = selectedCategory == LicenseCategory.CATEGORY_B,
                    title = if (isEn) "Category B (Car/Jeep)" else "वर्ग 'ख' (कार/जिप/भ्यान)",
                    icon = PIcons.Car,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedCategory = LicenseCategory.CATEGORY_B }
                )
            }

            // Main Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            ) {
                LicenseMainTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isEn) tab.titleEn else tab.titleNp,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content Body
            Crossfade(targetState = selectedTab, label = "license_tab_fade") { currentTab ->
                when (currentTab) {
                    LicenseMainTab.MOCK_EXAM -> MockExamScreen(category = selectedCategory, isEn = isEn)
                    LicenseMainTab.PRACTICE -> PracticeQuestionsScreen(category = selectedCategory, isEn = isEn)
                    LicenseMainTab.SIGNS -> TrafficSignsScreen(isEn = isEn)
                    LicenseMainTab.TRIAL_GUIDE -> TrialGuideScreen(category = selectedCategory, isEn = isEn)
                }
            }
        }
    }
}

@Composable
private fun CategorySegmentTab(
    isSelected: Boolean,
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(200),
        label = "cat_tab_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "cat_tab_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

// =============================================================================
// 1. MOCK EXAM SIMULATOR (नमुना लिखित परीक्षा)
// =============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MockExamScreen(category: LicenseCategory, isEn: Boolean) {
    val context = LocalContext.current

    var examState by remember { mutableStateOf("IDLE") } // "IDLE", "RUNNING", "RESULT", "REVIEW"
    var questions by remember { mutableStateOf(emptyList<LicenseQuestion>()) }
    var currentQIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionId -> selectedOptionIndex

    var timeRemainingSeconds by remember { mutableIntStateOf(30 * 60) } // 30 minutes
    var isTimerActive by remember { mutableStateOf(false) }

    // Timer Effect
    LaunchedEffect(isTimerActive) {
        while (isTimerActive && timeRemainingSeconds > 0) {
            delay(1000L)
            timeRemainingSeconds--
            if (timeRemainingSeconds <= 0) {
                isTimerActive = false
                examState = "RESULT"
            }
        }
    }

    when (examState) {
        "IDLE" -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PIcons.Award,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                item {
                    Text(
                        text = if (isEn) "DoTM Official Mock Exam Simulator" else "यातायात व्यवस्था विभाग नमुना लिखित परीक्षा",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isEn)
                            "Test your knowledge with 20 real exam questions with a 30-minute timer."
                        else
                            "२० वटा आधिकारिक प्रश्नहरू, ३० मिनेटको समय र तत्काल नतिजा सहित वास्तविक परीक्षाको अनुभव लिनुहोस्।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Guidelines Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isEn) "Exam Rules & Instructions:" else "परीक्षाका नियम तथा निर्देशनहरू:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            InstructionRow(
                                icon = PIcons.Timer,
                                text = if (isEn) "Total Time: 30 Minutes" else "कुल समय: ३० मिनेट"
                            )
                            InstructionRow(
                                icon = PIcons.Doc,
                                text = if (isEn) "Total Questions: 20 Multiple Choice Questions" else "कुल प्रश्न: २० वटा वस्तुगत प्रश्नहरू"
                            )
                            InstructionRow(
                                icon = PIcons.CheckCircle,
                                text = if (isEn) "Passing Score: 10 / 20 (50%)" else "उत्तीर्णाङ्क: १० / २० (५० प्रतिशत)"
                            )
                            InstructionRow(
                                icon = PIcons.Shield,
                                text = if (isEn) "No Negative Marking for wrong answers" else "गलत उत्तरको लागि कुनै नेगेटिभ मार्किङ छैन"
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            questions = DrivingLicenseQuestionBank.generateMockExam(context, category)
                            userAnswers.clear()
                            currentQIndex = 0
                            timeRemainingSeconds = 30 * 60
                            isTimerActive = true
                            examState = "RUNNING"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (isEn) "Start Mock Exam Now" else "परीक्षा सुरु गर्नुहोस्",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        "RUNNING" -> {
            if (questions.isEmpty()) return
            val currentQ = questions[currentQIndex]
            val minutes = timeRemainingSeconds / 60
            val seconds = timeRemainingSeconds % 60
            val timeStr = "%02d:%02d".format(minutes, seconds)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Timer & Question Counter Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (timeRemainingSeconds < 300) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PIcons.Timer,
                                contentDescription = null,
                                tint = if (timeRemainingSeconds < 300) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (timeRemainingSeconds < 300) Color(0xFFDC2626) else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = "${if (isEn) "Question" else "प्रश्न"} ${if (isEn) currentQIndex + 1 else npNum(currentQIndex + 1)} / ${if (isEn) questions.size else npNum(questions.size)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (currentQIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Question Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isEn) currentQ.topic.titleEn else currentQ.topic.titleNp,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (isEn) currentQ.questionEn else currentQ.questionNp,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 24.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (currentQ.signSymbol != null) {
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TrafficSignGraphic(
                                    signKey = currentQ.signSymbol,
                                    category = SignCategory.MANDATORY,
                                    size = 80.dp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Options List
                val options = if (isEn) currentQ.optionsEn else currentQ.optionsNp
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(options) { index, optionText ->
                        val isSelected = userAnswers[currentQ.id] == index
                        val optionPrefix = when (index) {
                            0 -> if (isEn) "A" else "क"
                            1 -> if (isEn) "B" else "ख"
                            2 -> if (isEn) "C" else "ग"
                            else -> if (isEn) "D" else "घ"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    userAnswers[currentQ.id] = index
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    if (isSelected) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
                                    else listOf(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionPrefix,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentQIndex > 0) currentQIndex--
                        },
                        enabled = currentQIndex > 0,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isEn) "Previous" else "अघिल्लो")
                    }

                    if (currentQIndex < questions.size - 1) {
                        Button(
                            onClick = { currentQIndex++ },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isEn) "Next" else "पछिल्लो")
                        }
                    } else {
                        Button(
                            onClick = {
                                isTimerActive = false
                                examState = "RESULT"
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text(if (isEn) "Submit Exam" else "परीक्षा बुझाउनुहोस्")
                        }
                    }
                }
            }
        }

        "RESULT" -> {
            var correctCount = 0
            questions.forEach { q ->
                if (userAnswers[q.id] == q.correctIndex) correctCount++
            }
            val isPassed = correctCount >= 10
            val timeTaken = (30 * 60) - timeRemainingSeconds

            LaunchedEffect(Unit) {
                LicenseStorageManager.saveTestResult(
                    context,
                    MockTestResult(
                        timestamp = System.currentTimeMillis(),
                        category = category,
                        totalQuestions = questions.size,
                        correctAnswers = correctCount,
                        wrongAnswers = questions.size - correctCount,
                        timeTakenSeconds = timeTaken,
                        isPassed = isPassed
                    )
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (isPassed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPassed) PIcons.CheckCircle else PIcons.Alert,
                            contentDescription = null,
                            tint = if (isPassed) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                item {
                    Text(
                        text = if (isPassed)
                            (if (isEn) "Congratulations! You Passed!" else "बधाई छ! तपाईं उत्तीर्ण हुनुभयो!")
                        else
                            (if (isEn) "Keep Practicing! You Failed." else "पुनः अभ्यास गर्नुहोस्! अनुत्तीर्ण हुनुभयो।"),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPassed) Color(0xFF15803D) else Color(0xFFDC2626),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${if (isEn) "Score:" else "प्राप्ताङ्क:"} $correctCount / ${questions.size} (${(correctCount * 100) / questions.size}%)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }

                // Score Card Metrics
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricBadge(
                            title = if (isEn) "Correct" else "सही उत्तर",
                            value = "$correctCount",
                            color = Color(0xFF16A34A),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            title = if (isEn) "Wrong" else "गलत उत्तर",
                            value = "${questions.size - correctCount}",
                            color = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            title = if (isEn) "Time Taken" else "लागेको समय",
                            value = "%02d:%02d".format(timeTaken / 60, timeTaken % 60),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { examState = "REVIEW" },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(PIcons.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEn) "Review Questions & Answers" else "सबै प्रश्न र सही उत्तर हेर्नुहोस्")
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            questions = DrivingLicenseQuestionBank.generateMockExam(context, category)
                            userAnswers.clear()
                            currentQIndex = 0
                            timeRemainingSeconds = 30 * 60
                            isTimerActive = true
                            examState = "RUNNING"
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(PIcons.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEn) "Retake Exam" else "पुनः परीक्षा दिनुहोस्")
                    }
                }
            }
        }

        "REVIEW" -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Question Review" else "प्रश्न समीक्षा",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedButton(
                            onClick = { examState = "RESULT" },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(if (isEn) "Back to Result" else "नतिजामा फर्कनुहोस्")
                        }
                    }
                }

                itemsIndexed(questions) { index, q ->
                    val selectedOpt = userAnswers[q.id]
                    val isCorrect = selectedOpt == q.correctIndex

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                if (isCorrect) listOf(Color(0xFF86EFAC), Color(0xFF86EFAC))
                                else listOf(Color(0xFFFCA5A5), Color(0xFFFCA5A5))
                            )
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (isEn) "Q" else "प्रश्न"} ${if (isEn) index + 1 else npNum(index + 1)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCorrect) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCorrect) PIcons.Check else PIcons.Cross,
                                        contentDescription = null,
                                        tint = if (isCorrect) Color(0xFF15803D) else Color(0xFFB91C1C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isCorrect) (if (isEn) "Correct" else "सही") else (if (isEn) "Wrong" else "गलत"),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCorrect) Color(0xFF15803D) else Color(0xFFB91C1C)
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isEn) q.questionEn else q.questionNp,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "${if (isEn) "Correct Answer:" else "सही उत्तर:"} ${if (isEn) q.optionsEn[q.correctIndex] else q.optionsNp[q.correctIndex]}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF15803D)
                            )
                            if (!isCorrect && selectedOpt != null) {
                                Text(
                                    text = "${if (isEn) "Your Answer:" else "तपाईंको उत्तर:"} ${if (isEn) q.optionsEn[selectedOpt] else q.optionsNp[selectedOpt]}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row {
                                    Icon(
                                        imageVector = PIcons.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) q.explanationEn else q.explanationNp,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 2. PRACTICE QUESTIONS & FLASHCARDS (अध्यायगत अभ्यास)
// =============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticeQuestionsScreen(category: LicenseCategory, isEn: Boolean) {
    val context = LocalContext.current

    var selectedTopic by remember { mutableStateOf(QuestionTopic.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyBookmarks by remember { mutableStateOf(false) }

    var bookmarkedIds by remember { mutableStateOf(LicenseStorageManager.getBookmarks(context)) }
    val revealedExplanations = remember { mutableStateMapOf<Int, Boolean>() }
    val selectedOptions = remember { mutableStateMapOf<Int, Int>() }

    val allQuestions = remember(category) { DrivingLicenseQuestionBank.getAllQuestions(context) }

    val filteredQuestions = remember(selectedTopic, searchQuery, showOnlyBookmarks, bookmarkedIds, category, allQuestions) {
        allQuestions.filter { q ->
            val matchCat = q.category == category || q.category == LicenseCategory.CATEGORY_A
            val matchTopic = selectedTopic == QuestionTopic.ALL || q.topic == selectedTopic
            val matchSearch = searchQuery.isBlank() ||
                    q.questionNp.contains(searchQuery, ignoreCase = true) ||
                    q.questionEn.contains(searchQuery, ignoreCase = true)
            val matchBookmark = !showOnlyBookmarks || bookmarkedIds.contains(q.id)

            matchCat && matchTopic && matchSearch && matchBookmark
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isEn) "Search questions or keywords..." else "प्रश्न वा शब्द खोज्नुहोस्...") },
            leadingIcon = { Icon(PIcons.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        )

        // Topic Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            items(QuestionTopic.values()) { topic ->
                FilterChip(
                    selected = selectedTopic == topic,
                    onClick = { selectedTopic = topic },
                    label = {
                        Text(
                            text = if (isEn) topic.titleEn else topic.titleNp,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTopic == topic) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Bookmark and Count Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredQuestions.size} ${if (isEn) "Questions" else "प्रश्नहरू"}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showOnlyBookmarks = !showOnlyBookmarks }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PIcons.Bookmark,
                    contentDescription = null,
                    tint = if (showOnlyBookmarks) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isEn) "Saved" else "सुरक्षित",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (showOnlyBookmarks) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Questions List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredQuestions, key = { it.id }) { question ->
                val isBookmarked = bookmarkedIds.contains(question.id)
                val isExplanationOpen = revealedExplanations[question.id] ?: false
                val selectedOpt = selectedOptions[question.id]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (isEn) "Question" else "प्रश्न"} ${if (isEn) question.id else npNum(question.id)} · ${if (isEn) question.topic.titleEn else question.topic.titleNp}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = {
                                    LicenseStorageManager.toggleBookmark(context, question.id)
                                    bookmarkedIds = LicenseStorageManager.getBookmarks(context)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = PIcons.Bookmark,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isEn) question.questionEn else question.questionNp,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (question.signSymbol != null) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TrafficSignGraphic(
                                    signKey = question.signSymbol,
                                    category = SignCategory.MANDATORY,
                                    size = 80.dp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Interactive Options
                        val options = if (isEn) question.optionsEn else question.optionsNp
                        options.forEachIndexed { index, optionText ->
                            val isCorrectAnswer = index == question.correctIndex
                            val isUserSelected = selectedOpt == index

                            val optBgColor = when {
                                selectedOpt == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                isCorrectAnswer -> Color(0xFFDCFCE7)
                                isUserSelected && !isCorrectAnswer -> Color(0xFFFEE2E2)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }

                            val optBorderColor = when {
                                selectedOpt == null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                isCorrectAnswer -> Color(0xFF16A34A)
                                isUserSelected && !isCorrectAnswer -> Color(0xFFDC2626)
                                else -> Color.Transparent
                            }

                            val optionPrefix = when (index) {
                                0 -> if (isEn) "A" else "क"
                                1 -> if (isEn) "B" else "ख"
                                2 -> if (isEn) "C" else "ग"
                                else -> if (isEn) "D" else "घ"
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(optBgColor)
                                    .border(1.dp, optBorderColor, RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedOptions[question.id] = index
                                        revealedExplanations[question.id] = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$optionPrefix.",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (selectedOpt != null && isCorrectAnswer) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selectedOpt != null && isCorrectAnswer) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (selectedOpt != null && isCorrectAnswer) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Explanation Toggle
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    revealedExplanations[question.id] = !isExplanationOpen
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = PIcons.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "View Official Explanation" else "सही उत्तरको व्याख्या हेर्नुहोस्",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                imageVector = if (isExplanationOpen) PIcons.ChevronLeft else PIcons.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        AnimatedVisibility(visible = isExplanationOpen) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = if (isEn) question.explanationEn else question.explanationNp,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 3. ROAD SIGNS & MARKINGS LIBRARY (ट्राफिक संकेतहरू - 100% Vector DoTM Standard)
// =============================================================================
@Composable
private fun TrafficSignsScreen(isEn: Boolean) {
    var selectedCat by remember { mutableStateOf<SignCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val allSigns = remember { DrivingLicenseQuestionBank.trafficSigns }

    val filteredSigns = remember(selectedCat, searchQuery, allSigns) {
        allSigns.filter { sign ->
            val matchCat = selectedCat == null || sign.category == selectedCat
            val matchSearch = searchQuery.isBlank() ||
                    sign.titleNp.contains(searchQuery, ignoreCase = true) ||
                    sign.titleEn.contains(searchQuery, ignoreCase = true) ||
                    sign.meaningNp.contains(searchQuery, ignoreCase = true) ||
                    sign.meaningEn.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isEn) "Search road signs (e.g. Stop, Speed, Turn)..." else "संकेत खोज्नुहोस् (जस्तै: रोक्नुहोस्, गति, मोड)...") },
            leadingIcon = { Icon(PIcons.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        )

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCat == null,
                    onClick = { selectedCat = null },
                    label = {
                        Text(
                            text = "${if (isEn) "All" else "सबै"} (${allSigns.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedCat == null) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            items(SignCategory.values()) { cat ->
                val count = remember(cat, allSigns) { allSigns.count { it.category == cat } }
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat },
                    label = {
                        Text(
                            text = "${if (isEn) cat.titleEn else cat.titleNp} ($count)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedCat == cat) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Signs List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSigns, key = { it.id }) { sign ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official DoTM Traffic Sign Frame
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(androidx.compose.ui.graphics.Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TrafficSignGraphic(
                                signKey = sign.signKey,
                                category = sign.category,
                                size = 68.dp,
                                drawableRes = sign.drawableRes
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) sign.titleEn else sign.titleNp,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) sign.meaningEn else sign.meaningNp,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                            if (sign.penaltyNp != "—" && sign.penaltyNp.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFEF2F2))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${if (isEn) "Penalty: " else "कारबाही: "}${if (isEn) sign.penaltyEn else sign.penaltyNp}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 4. 70-MARKS PRACTICAL TRIAL ASSESSMENT GUIDE (DoTM Standard)
// =============================================================================
@Composable
private fun TrialGuideScreen(category: LicenseCategory, isEn: Boolean) {
    val guide = remember(category) {
        if (category == LicenseCategory.CATEGORY_A) DrivingLicenseQuestionBank.bikeTrialGuide
        else DrivingLicenseQuestionBank.carTrialGuide
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEn) guide.titleEn else guide.titleNp,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF16A34A))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${if (isEn) "Pass Mark:" else "उत्तीर्णाङ्क:"} ${guide.passMarks} / ${guide.totalMarks}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Trial Steps List
        items(guide.steps, key = { it.stepNumber }) { step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${step.stepNumber}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (isEn) step.nameEn else step.nameNp,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${step.totalMarks} ${if (isEn) "Marks" else "अङ्क"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (isEn) "Mark Deductions & Faults:" else "अङ्क कट्टा हुने अवस्था:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFDC2626)
                    )
                    val deductions = if (isEn) step.deductionsEn else step.deductionsNp
                    deductions.forEach { deduction ->
                        Text(
                            text = "• $deduction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row {
                            Icon(
                                imageVector = PIcons.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${if (isEn) "Pro Tip: " else "सुझाव: "}${if (isEn) step.tipsEn else step.tipsNp}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MetricBadge(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
