package com.neptools.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.ui.components.ToolTopBar
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        ToolTopBar(
            title = if (isEn) "Privacy Policy" else "गोपनीयता नीति",
            subtitle = if (isEn) "On-device processing & zero tracking" else "अन-डिभाइस गोपनीयता तथा शून्य ट्र्याकिङ",
            onBack = onBack
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                PIcons.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NepTools Privacy Architecture",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isEn) "Zero Tracking · Device-Only Computations" else "शून्य ट्र्याकिङ · १००% उपकरणमा आधारित प्रशोधन",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isEn)
                            "Your privacy is our utmost priority. NepTools operates under a strict privacy-first architecture. We do not harvest, track, sell, or commercialize your personal identity, contacts, photos, or location data."
                        else
                            "तपाईँको गोपनीयता हाम्रो पहिलो प्राथमिकता हो। NepTools ले तपाईँको कुनै पनि व्यक्तिगत विवरण, फोटो, सम्पर्क वा जीपीएस स्थान डेटा संकलन वा बिक्री गर्दैन।",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Section 1
            PrivacySectionCard(
                sectionNumber = "1",
                title = if (isEn) "Zero Personal Data Collection" else "शून्य व्यक्तिगत डेटा संकलन",
                content = if (isEn)
                    "NepTools does not require user account registration, phone numbers, email addresses, or personal profiling. You can use 100% of the application features anonymously and immediately upon installation without creating a profile."
                else
                    "NepTools प्रयोग गर्न कुनै पनि खाता, फोन नम्बर वा इमेल दर्ता गर्नु पर्दैन। एप इन्स्टल गर्नासाथ सम्पूर्ण सुविधाहरू बिना कुनै व्यक्तिगत विवरण पूर्ण रूपमा प्रयोग गर्न सकिन्छ।"
            )

            // Section 2
            PrivacySectionCard(
                sectionNumber = "2",
                title = if (isEn) "100% Local Device Computing" else "पूर्ण स्थानीय उपकरण प्रशोधन",
                content = if (isEn)
                    "All computations—including Nepali calendar Bikram Sambat conversions (1975–2099 BS), Vedic astrology kundali generation, photo resizing and compression, high-frequency pet recall audio generation, loan EMI amortization, and Wi-Fi LAN file bridge—execute entirely inside your phone's memory. Your sensitive files and inputs never leave your device."
                else
                    "पात्रो तथा मिति रूपान्तरण (वि.सं. १९७५–२०९९), कुण्डली निर्माण, फोटो कम्प्रेस, सिट्टी टोन, बैंक ऋण हिसाब र वाइफाइ फाइल ट्रान्सफर जस्ता सबै कार्यहरू तपाईँको फोनमै स्थानीय रूपमा प्रशोधन हुन्छन्। तपाईँका फाइलहरू कहिल्यै फोन बाहिर जाँदैनन्।"
            )

            // Section 3
            PrivacySectionCard(
                sectionNumber = "3",
                title = if (isEn) "Location Permissions & Privacy" else "स्थान अनुमति तथा गोपनीयता",
                content = if (isEn)
                    "When GPS location permission is granted, coordinates are processed strictly in real-time on-device to determine your local weather forecast and match your Nepal Oil Corporation (NOC) geographic petroleum price category (1st, 2nd, or 3rd category). Coordinates are never logged, stored in databases, or shared with external analytics vendors."
                else
                    "स्थान अनुमति दिइएमा सो विवरण केवल तपाईँको स्थानीय मौसम र नेपाल आयल निगमको इन्धन मूल्य वर्ग (पहिलो, दोस्रो वा तेस्रो वर्ग) पहिचान गर्न मात्र तत्काल फोनभित्रै प्रयोग हुन्छ। यो विवरण कतै सुरक्षित वा आदानप्रदान गरिँदैन।"
            )

            // Section 4
            PrivacySectionCard(
                sectionNumber = "4",
                title = if (isEn) "Microphone & Voice Permissions" else "माइक तथा आवाज पहिचान",
                content = if (isEn)
                    "The microphone permission is utilized exclusively when you actively press the voice recording button in the Voice Notes tool for speech-to-text transcription. NepTools does not record background conversations, store voice logs, or transmit audio data to third parties."
                else
                    "माइक अनुमति केवल भ्वाइस नोट टुलमा आवाजबाट नेपालीमा टाइप गर्दा मात्र प्रयोग हुन्छ। एपले पृष्ठभूमिमा कुनै आवाज सुन्दैन वा अडियो रेकर्ड गरेर कतै पठाउँदैन।"
            )

            // Section 5
            PrivacySectionCard(
                sectionNumber = "5",
                title = if (isEn) "Local Storage & User Preferences" else "स्थानीय सेटिङ तथा मेमोरी",
                content = if (isEn)
                    "Application preferences (such as Language selection, Dark Mode theme, and offline cached public market rates) are saved locally in Android SharedPreferences on your device. Uninstalling the application or clearing app cache completely removes all saved preferences."
                else
                    "तपाईँले चयन गर्नुभएको भाषा, डार्क मोड थिम तथा अफलाइन सेभ गरिएका बजार भाउहरू तपाईँकै फोनको आन्तरिक मेमोरीमा सुरक्षित रहन्छन्। एप अनइन्स्टल गर्दा ती सबै स्वतः मेटिन्छन्।"
            )

            // Section 6
            PrivacySectionCard(
                sectionNumber = "6",
                title = if (isEn) "Public Network Data Synchronisation" else "सार्वजनिक नेटवर्क डेटा सिङ्क्रोनाइजेसन",
                content = if (isEn)
                    "Network connectivity is utilized only to fetch read-only public market data (Nepal Oil Corporation rates, Kalimati vegetable prices, and Nepal Rastra Bank forex rates). These HTTP requests do not contain any unique user identifiers, hardware serials, or advertising identifiers."
                else
                    "इन्टरनेटको प्रयोग केवल सार्वजनिक बजार दर (इन्धन, कालिमाटी तरकारी, विदेशी मुद्रा) डाउनलोड गर्न मात्र गरिन्छ। यसमा प्रयोगकर्ताको कुनै पनि व्यक्तिगत पहिचान पठाइँदैन।"
            )

            // Section 7
            PrivacySectionCard(
                sectionNumber = "7",
                title = if (isEn) "Zero Third-Party Ad Trackers" else "विज्ञापन ट्र्याकिङ रहित",
                content = if (isEn)
                    "NepTools is free from intrusive third-party behavioral advertising trackers, user profiling SDKs, and data brokers. We do not build commercial behavioral profiles of our users."
                else
                    "यस एपमा कुनै पनि अनावश्यक विज्ञापन ट्र्याकिङ वा प्रयोगकर्ताको गतिविधि ट्र्याक गर्ने सफ्टवेयर समावेश छैन।"
            )

            // Section 8
            PrivacySectionCard(
                sectionNumber = "8",
                title = if (isEn) "Developer Contact & Inquiries" else "विकासकर्ता सम्पर्क तथा सुझाव",
                content = if (isEn)
                    "NepTools is built by Shubham Belbase with a strong commitment to user privacy and open utility engineering. For privacy inquiries, developer feedback, or security disclosures, contact Shubham Belbase."
                else
                    "NepTools शुभम बेलबासे द्वारा गोपनीयता-मैत्री प्रविधिमा आधारित भई निर्माण गरिएको हो। कुनै पनि सुझाव वा जिज्ञासाका लागि विकासकर्तालाई सम्पर्क गर्न सक्नुहुन्छ।"
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Copyright (c) 2026 Shubham Belbase. All Rights Reserved.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacySectionCard(
    sectionNumber: String,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sectionNumber,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp, fontSize = 12.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
