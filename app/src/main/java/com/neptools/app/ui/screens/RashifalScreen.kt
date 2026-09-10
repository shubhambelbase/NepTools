package com.neptools.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.components.HairLabel
import com.neptools.app.ui.components.npNum
import java.time.LocalDate
import kotlin.random.Random

private data class Rashi(val glyph: String, val nameNp: String, val nameEn: String)

private val rashis = listOf(
    Rashi("♈", "मेष", "Aries"), Rashi("♉", "वृष", "Taurus"),
    Rashi("♊", "मिथुन", "Gemini"), Rashi("♋", "कर्कट", "Cancer"),
    Rashi("♌", "सिंह", "Leo"), Rashi("♍", "कन्या", "Virgo"),
    Rashi("♎", "तुला", "Libra"), Rashi("♏", "वृश्चिक", "Scorpio"),
    Rashi("♐", "धनु", "Sagittarius"), Rashi("♑", "मकर", "Capricorn"),
    Rashi("♒", "कुम्भ", "Aquarius"), Rashi("♓", "मीन", "Pisces")
)

private val openersNp = listOf(
    "आज आत्मविश्वास उच्च रहनेछ।",
    "दिनभर सकारात्मक ऊर्जा रहनेछ।",
    "धैर्यता नै आजको मुख्य हतियार हुनेछ।",
    "नयाँ कुरा सिक्ने इच्छा जाग्नेछ।",
    "पुराना कामहरू पूरा गर्ने उत्तम दिन।",
    "सोचभन्दा छिटो प्रगति हुनेछ।",
    "आजको दिन सन्तुलनको दिन हो।"
)
private val openersEn = listOf(
    "High confidence and enthusiasm will guide your day.",
    "Positive energy and mental clarity surround you throughout the day.",
    "Patience and steady focus will be your greatest asset today.",
    "A strong desire to learn new concepts and explore ideas awakens.",
    "An ideal day to wrap up pending tasks and organize your schedule.",
    "Progress will happen more smoothly and quickly than anticipated.",
    "Today brings balance, harmony, and well-rounded perspective."
)
private val careersNp = listOf(
    "कार्यक्षेत्रमा प्रशंसा मिल्ने योग छ।",
    "अफिसमा थप जिम्मेवारी आउन सक्छ।",
    "रोकिएका भुक्तानी अघि बढ्नेछन्।",
    "सहकर्मीसँगको सहयोगले काम सजिलो हुनेछ।",
    "नयाँ प्रस्तावलाई गम्भीरतापूर्वक विचार गर्नुहोस्।",
    "वित्तीय निर्णयमा ढिलाइ बेहोस नगर्नुहोस्।",
    "समय व्यवस्थापनले फल दिनेछ।"
)
private val careersEn = listOf(
    "Recognition and appreciation are likely in your workplace.",
    "New responsibilities may come your way at work or business.",
    "Pending payments and proposals will move forward positively.",
    "Supportive teamwork and collaboration make your tasks easier.",
    "Consider new proposals carefully for long-term benefits.",
    "Financial decisions require prudent judgment and balance.",
    "Disciplined time management will yield fruitful results."
)
private val lovesNp = listOf(
    "प्रियजनसँग समय बिताउनु उत्तम हुनेछ।",
    "पारिवारिक सहयोग मिल्ने योग छ।",
    "सानो गल्तीलाई क्षमा गर्दा दिन रमाइलो हुनेछ।",
    "मित्रहरूबाट खुशीको खबर आउन सक्छ।",
    "संवादले सम्बन्ध अझ गाढा बनाउनेछ।",
    "एक्लोपन घट्ने दिन हो।"
)
private val lovesEn = listOf(
    "Spending quality time with loved ones brings deep joy.",
    "Warm domestic support and family harmony are highlighted.",
    "Overlooking minor flaws makes interactions pleasant and joyful.",
    "Encouraging news from close friends or partners is likely.",
    "Open and sincere communication strengthens meaningful bonds.",
    "A feeling of connection, warmth and mutual understanding prevails."
)
private val healthsNp = listOf(
    "स्वास्थ्य राम्रो रहनेछ, तर पानी प्रशस्त पिउनुहोस्।",
    "साँझपख हिँडडुल गर्दा ऊर्जा बढ्नेछ।",
    "निद्राको कमीले थकान महसुस हुन सक्छ।",
    "खानपानमा ध्यान दिँदा फाइदा छ।",
    "मानसिक शान्तिका लागि छोतो ध्यान राम्रो हुनेछ।"
)
private val healthsEn = listOf(
    "Vitality remains steady; stay well-hydrated throughout the day.",
    "An evening walk or gentle exercise will recharge your stamina.",
    "Ensure restful sleep to prevent any late afternoon fatigue.",
    "Mindful eating and nutritious choices will keep digestion light.",
    "A brief moment of quiet meditation enhances inner tranquility."
)
private val luckyColors = listOf(
    "पिरो · Yellow", "रातो · Red", "हरियो · Green", "निलो · Blue",
    "सेतो · White", "गुलाबी · Pink", "सुनौलो · Golden"
)

@Composable
fun RashifalScreen(onBack: () -> Unit) {
    val engine = PatroRepo.d.engine
    val today = remember { engine.today() }
    var selected by remember { mutableIntStateOf(0) }
    val rashi = rashis[selected]
    var refreshKey by remember { mutableIntStateOf(0) }
    val seed = remember(selected, refreshKey) { LocalDate.now().toEpochDay() * 31 + selected + refreshKey }
    val rng = remember(seed) { Random(seed) }
    val isEn = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"

    val reading = remember(seed, isEn) {
        val op = if (isEn) openersEn else openersNp
        val car = if (isEn) careersEn else careersNp
        val lov = if (isEn) lovesEn else lovesNp
        val hlt = if (isEn) healthsEn else healthsNp
        "${op[rng.nextInt(op.size)]} ${car[rng.nextInt(car.size)]} ${lov[rng.nextInt(lov.size)]} ${hlt[rng.nextInt(hlt.size)]}"
    }
    val lovePct = remember(seed) { 45 + rng.nextInt(51) }
    val workPct = remember(seed) { 45 + rng.nextInt(51) }
    val healthPct = remember(seed) { 45 + rng.nextInt(51) }
    val luckPct = remember(seed) { 45 + rng.nextInt(51) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
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
            Spacer(Modifier.size(width = 14.dp, height = 0.dp))
            Column(Modifier.weight(1f)) {
                Text(T("daily_rashifal"), style = MaterialTheme.typography.titleLarge)
                Text(
                    if (isEn) "${today.day} ${NepaliNames.monthsEn[today.month - 1]} ${today.year} BS · ${T("today")}"
                    else "${NepaliNames.monthsNp[today.month - 1]} ${npNum(today.day)}, ${npNum(today.year)} · ${T("today")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                com.neptools.app.ui.components.AnimatedRefreshIconButton(
                    onClick = { refreshKey++ },
                    iconSize = 18.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rashis.chunked(4).forEach { rowRashis ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowRashis.forEach { r ->
                        val i = rashis.indexOf(r)
                        Box(Modifier.weight(1f)) {
                            RashiTile(r, selected == i) { selected = i }
                        }
                    }
                    repeat(4 - rowRashis.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isEn) rashi.nameEn else "${rashi.nameNp} — ${rashi.nameEn}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    if (isEn) "Daily Astrological Insight" else "दैनिक राशिफल",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(reading, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            MeterBar(if (isEn) "Love & Romance" else "प्रेम Love", lovePct)
            MeterBar(if (isEn) "Career & Work" else "करियर Work", workPct)
            MeterBar(if (isEn) "Health & Vitality" else "स्वास्थ्य Health", healthPct)
            MeterBar(if (isEn) "Luck & Fortune" else "भाग्य Luck", luckPct)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LuckyChip(if (isEn) "Lucky Color" else "शुभ रङ", luckyColors[selected % luckyColors.size], Modifier.weight(1.3f))
                LuckyChip(if (isEn) "Lucky No." else "शुभ अङ्क", if (isEn) "${selected % 9 + 1}" else npNum(selected % 9 + 1), Modifier.weight(0.7f))
                LuckyChip(if (isEn) "Good Hours" else "शुभ समय", if (isEn) enTimeWindow(selected) else npTimeWindow(selected), Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(110.dp))
    }
}

private fun enTimeWindow(idx: Int): String {
    val windows = listOf("7:00–8:30 AM", "11:00 AM–12:30 PM", "2:00–3:30 PM", "4:00–5:30 PM")
    return windows[idx % windows.size]
}

private fun npTimeWindow(idx: Int): String {
    val windows = listOf("७–८:३०", "११–१२:३०", "१४–१५:३०", "१६–१७:३०")
    return windows[idx % windows.size]
}

@Composable
private fun RashiTile(r: Rashi, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
    val fg = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onBackground
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bg, MaterialTheme.shapes.small)
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            r.glyph,
            style = MaterialTheme.typography.headlineMedium,
            color = if (isSelected) fg else MaterialTheme.colorScheme.secondary
        )
        Text(r.nameNp, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
private fun MeterBar(label: String, percent: Int) {
    val animated by animateFloatAsState(targetValue = percent / 100f, label = "meter")
    Column(Modifier.padding(vertical = 5.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = animated.coerceIn(0f, 1f))
                    .height(5.dp)
                    .background(
                        if (label.startsWith("करियर")) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary,
                        MaterialTheme.shapes.extraSmall
                    )
            )
        }
    }
}

@Composable
private fun LuckyChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            .padding(vertical = 7.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
