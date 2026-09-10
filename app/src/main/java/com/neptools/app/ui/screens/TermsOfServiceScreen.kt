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
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    val isEn = ThemePrefs.lang.value == "en"

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    PIcons.ChevronLeft,
                    contentDescription = if (isEn) "Back" else "पछाडि",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = if (isEn) "Terms of Service" else "सेवाका सर्तहरू",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

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
                                PIcons.Doc,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NepTools Terms & Conditions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isEn) "Effective Date: August 2026 · Version 2.5.0" else "लागू मिति: भदौ २०८३ · संस्करण २.५.०",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isEn)
                            "Please read these Terms of Service carefully before utilizing the NepTools mobile application. By accessing or downloading NepTools, you agree to be bound by all clauses below."
                        else
                            "NepTools मोबाइल एप्लिकेशन प्रयोग गर्नुअघि कृपया यी सेवा सर्तहरू ध्यानपूर्वक पढ्नुहोस्। यो एप प्रयोग गर्नुभएमा तपाईँ यी सर्तहरू पालना गर्न सहमत हुनुभएको मानिनेछ।",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Section 1
            LegalSectionCard(
                sectionNumber = "1",
                title = if (isEn) "Acceptance & Permitted Use" else "स्वीकृति तथा उपयोग अनुमति",
                content = if (isEn)
                    "NepTools is granted as a personal, non-exclusive, non-transferable, and revocable utility tool for individual productivity, date conversion, Nepali calendar exploration, petroleum rate checks, and civic assistance. Commercial redistribution, automated data scraping, decompilation, or reverse engineering of the application codebase without prior written consent is strictly prohibited."
                else
                    "NepTools तपाईँलाई व्यक्तिगत प्रयोजन, नेपाली पात्रो अवलोकन, मिति रूपान्तरण, इन्धन दर जानकारी र दैनिक नागरिक सेवाहरूका लागि प्रदान गरिएको हो। विकासकर्ताको लिखित अनुमति बिना यस एपलाई व्यावसायिक पुनर्वितरण गर्न, कोड रिभर्स-इन्जिनियरिङ गर्न वा अनधिकृत रूपमा प्रयोग गर्न निषेध गरिएको छ।"
            )

            // Section 2
            LegalSectionCard(
                sectionNumber = "2",
                title = if (isEn) "Local On-Device Processing" else "स्थानीय उपकरण प्रशोधन प्रणाली",
                content = if (isEn)
                    "NepTools performs major computational workflows (including Bikram Sambat date conversions, Vedic astrological kundali generation, image compression, high-frequency pet recall tones, loan EMI calculations, and WiFi LAN file drops) entirely locally on your device hardware without routing sensitive data to third-party cloud servers."
                else
                    "यस एपका प्रमुख कार्यहरू (जस्तै: विक्रम संवत् मिति रूपान्तरण, कुण्डली निर्माण, फोटो साइज घटाउने, सिट्टी टोन, बैंक ऋण किस्ता हिसाब र वाइफाइ फाइल ट्रान्सफर) पूर्ण रूपमा तपाईँकै फोनको हार्डवेयरमा प्रशोधन हुन्छन् र कुनै बाह्य सर्भरमा पठाइँदैन।"
            )

            // Section 3
            LegalSectionCard(
                sectionNumber = "3",
                title = if (isEn) "Government & Public Data Disclaimers" else "सार्वजनिक तथा सरकारी दर सूचना",
                content = if (isEn)
                    "All petroleum tariffs (Nepal Oil Corporation), foreign currency exchange rates (Nepal Rastra Bank), vegetable market rates (Kalimati Fruit and Vegetable Market Development Board), and electricity/water billing tariffs are synchronized from official public records. While every effort is made to maintain real-time fidelity, NepTools is not an official government entity and does not assume financial liability for commercial transactions conducted solely upon these referenced figures."
                else
                    "इन्धनको मूल्य (नेपाल आयल निगम), विदेशी विनिमय दर (नेपाल राष्ट्र बैंक), तरकारी बजार भाउ (कालिमाटी समिति) र विद्युत महसुल सम्बन्धित सरकारी तथा सार्वजनिक निकायहरूबाट प्राप्त गरिन्छ। यी सूचनाहरू सहयोगार्थ मात्र भएकाले ठूला वित्तीय वा व्यावसायिक कारोबार गर्नुअघि सम्बन्धित निकायबाट आधिकारिक पुष्टि गर्न सिफारिस गरिन्छ।"
            )

            // Section 4
            LegalSectionCard(
                sectionNumber = "4",
                title = if (isEn) "Astrological & Religious Calculations" else "धार्मिक पञ्चाङ्ग तथा ज्योतिषीय परामर्श",
                content = if (isEn)
                    "Horoscope predictions, panchang timings (Tithi, Nakshatra, Rahu Kaal, Sunrise, Sunset), and astrological charts are generated based on mathematical models and classical Vedic astrological principles. They are provided solely for cultural, traditional, and entertainment purposes."
                else
                    "दैनिक राशिफल, तिथि, नक्षत्र, राहुकाल, सूर्योदय-सूर्यास्त तथा कुण्डली शास्त्रीय सिद्धान्त र गणितीय अल्गोरिदममा आधारित छन्। यी जानकारीहरू सांस्कृतिक तथा परम्परागत प्रयोजनका लागि मात्र हुन्।"
            )

            // Section 5
            LegalSectionCard(
                sectionNumber = "5",
                title = if (isEn) "Civic Utilities & Quiz Preparations" else "नागरिक सेवा, आवेदन तथा परीक्षा तयारी",
                content = if (isEn)
                    "The Driving License preparation modules, mock quizzes, official letter templates, and postal code directories are structured in compliance with Department of Transport Management (DoTM) and Nepal Government civic standards. NepTools does not guarantee examination success or official governmental approval of generated letters."
                else
                    "सवारी चालक अनुमतिपत्र (लाइसेन्स) परीक्षा तयारी, ट्राफिक संकेत, निवेदन ढाँचा र हुलाक कोडहरू सम्बन्धित सरकारी मापदण्ड अनुसार तयार पारिएका हुन्। यसले परीक्षामा सफलताको कानुनी ग्यारेन्टी भने दिँदैन।"
            )

            // Section 6
            LegalSectionCard(
                sectionNumber = "6",
                title = if (isEn) "Intellectual Property & Development" else "बौद्धिक सम्पत्ति तथा निर्माणकर्ता",
                content = if (isEn)
                    "NepTools, including its visual architecture, custom algorithms, user interfaces, branding, and proprietary assets, is built by Shubham Belbase. All rights, title, and intellectual property remain the sole property of Shubham Belbase under applicable international copyright and intellectual property treaties."
                else
                    "NepTools एपको सम्पूर्ण सफ्टवेयर संरचना, डिजाइन, अल्गोरिदम, इन्टरफेस तथा ब्रान्डिङ शुभम बेलबासे (Shubham Belbase) द्वारा निर्माण गरिएको हो। यसको सम्पूर्ण प्रतिलिपि अधिकार सुरक्षित छ।"
            )

            // Section 7
            LegalSectionCard(
                sectionNumber = "7",
                title = if (isEn) "Limitation of Liability" else "दायित्वको सीमा",
                content = if (isEn)
                    "Under no circumstances shall the developer or NepTools be liable for any indirect, punitive, incidental, special, or consequential damages resulting from the use or inability to use this application, network interruptions, hardware malfunctions, or data discrepancies."
                else
                    "एपको प्रयोग वा प्रयोग गर्न नसक्दा हुने कुनै पनि अप्रत्यक्ष क्षति, इन्टरनेट समस्या वा उपकरण गडबडीका लागि विकासकर्ता उत्तरदायी हुनेछैन।"
            )

            // Section 8
            LegalSectionCard(
                sectionNumber = "8",
                title = if (isEn) "Modifications & Inquiries" else "सर्त परिमार्जन तथा सम्पर्क",
                content = if (isEn)
                    "We reserve the right to modify these Terms of Service at any time. Continued use of the application after modifications constitutes acceptance of the revised terms. For questions regarding these terms, contact Shubham Belbase."
                else
                    "यी सेवा सर्तहरू समय-समयमा परिमार्जन हुन सक्छन्। एपको निरन्तर प्रयोगले परिमार्जित सर्तहरूको स्वीकृति जनाउँछ। कुनै जिज्ञासा भएमा निर्माणकर्ता शुभम बेलबासे (Shubham Belbase) लाई सम्पर्क गर्न सक्नुहुन्छ।"
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
private fun LegalSectionCard(
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
