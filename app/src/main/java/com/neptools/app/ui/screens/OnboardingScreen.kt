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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.strings.T
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"
    val selectedLang = ThemePrefs.lang.value

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ने",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = T("onb_choose_lang"),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = T("onb_lang_hint"),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LanguageCard(
                    titleNp = "नेपाली",
                    titleEn = "Nepali",
                    subtitle = "१२३ • पात्रो, तिथि, पर्व नेपालीमा",
                    isSelected = selectedLang == "np",
                    onClick = { ThemePrefs.saveLang(context, "np") }
                )
                LanguageCard(
                    titleNp = "English",
                    titleEn = "English",
                    subtitle = "123 • Calendar, tithi, festivals in English",
                    isSelected = selectedLang == "en",
                    onClick = { ThemePrefs.saveLang(context, "en") }
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(PIcons.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = if (isEn) "Nepali shows Devanagari digits (१२३) and English shows 123 everywhere"
                        else "नेपालीमा १२३ र अंग्रेजीमा 123 देखिन्छ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = T("onb_continue"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = T("onb_change_later"),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun LanguageCard(
    titleNp: String,
    titleEn: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateFloatAsState(targetValue = if (isSelected) 1f else 0f, label = "border")
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface
    val border = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (titleNp == "नेपाली") "अ" else "A",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column {
                Text(
                    text = if (titleNp == "नेपाली") "नेपाली" else "English",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(PIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
