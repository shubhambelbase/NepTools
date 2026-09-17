package com.neptools.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.neptools.app.core.tools.FavoriteToolsManager
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.navigation.Routes
import com.neptools.app.ui.theme.ThemePrefs

private data class ToolGridItem(
    val titleNp: String,
    val titleEn: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBgColor: Color,
    val route: String
)

@Composable
fun ToolsScreen(onOpenTool: (String) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        FavoriteToolsManager.load(context)
    }

    val isEn = ThemePrefs.lang.value == "en"

    val dailyTools = remember {
        listOf(
            ToolGridItem(
                titleNp = "मौसम पूर्वानुमान",
                titleEn = "Weather & Forecast",
                icon = PIcons.Sun,
                iconTint = Color(0xFFD97706),
                iconBgColor = Color(0xFFFEF3C7),
                route = Routes.WEATHER
            ),
            ToolGridItem(
                titleNp = "लाइभ रेडियो",
                titleEn = "Live FM Radio",
                icon = PIcons.Radio,
                iconTint = Color(0xFF0284C7),
                iconBgColor = Color(0xFFE0F2FE),
                route = Routes.RADIO
            ),
            ToolGridItem(
                titleNp = "पेट्रोलियम भाउ",
                titleEn = "Fuel Prices",
                icon = PIcons.Fuel,
                iconTint = Color(0xFFEA580C),
                iconBgColor = Color(0xFFFFEDD5),
                route = Routes.FUEL
            ),
            ToolGridItem(
                titleNp = "कालिमाटी बजार",
                titleEn = "Kalimati Market",
                icon = PIcons.Leaf,
                iconTint = Color(0xFF16A34A),
                iconBgColor = Color(0xFFDCFCE7),
                route = Routes.KALIMATI
            ),
            ToolGridItem(
                titleNp = "दैनिक राशिफल",
                titleEn = "Daily Horoscope",
                icon = PIcons.Sparkle,
                iconTint = Color(0xFF9333EA),
                iconBgColor = Color(0xFFF3E8FF),
                route = Routes.RASHIFAL
            )
        )
    }

    val citizenTools = remember {
        listOf(
            ToolGridItem(
                titleNp = "लाइसेन्स तयारी",
                titleEn = "Driving License",
                icon = PIcons.Car,
                iconTint = Color(0xFFDC2626),
                iconBgColor = Color(0xFFFEE2E2),
                route = Routes.DRIVING_LICENSE
            ),
            ToolGridItem(
                titleNp = "सरकारी निवेदन",
                titleEn = "Govt Templates",
                icon = PIcons.Doc,
                iconTint = Color(0xFF059669),
                iconBgColor = Color(0xFFD1FAE5),
                route = Routes.TEMPLATES
            ),
            ToolGridItem(
                titleNp = "आपतकालीन सम्पर्क",
                titleEn = "Emergency Numbers",
                icon = PIcons.Phone,
                iconTint = Color(0xFFE11D48),
                iconBgColor = Color(0xFFFFE4E6),
                route = Routes.EMERGENCY
            ),
            ToolGridItem(
                titleNp = "महसुल क्यालकुलेटर",
                titleEn = "Bill Calculator",
                icon = PIcons.Zap,
                iconTint = Color(0xFFEA580C),
                iconBgColor = Color(0xFFFFEDD5),
                route = Routes.BILL_CALC
            ),
            ToolGridItem(
                titleNp = "हुलाक कोड",
                titleEn = "Postal / Zip Codes",
                icon = PIcons.Mail,
                iconTint = Color(0xFF2563EB),
                iconBgColor = Color(0xFFDBEAFE),
                route = Routes.POSTAL
            ),
            ToolGridItem(
                titleNp = "गोप्य क्यामेरा डिटेक्टर",
                titleEn = "Spy Cam Detector",
                icon = PIcons.CameraSpy,
                iconTint = Color(0xFFDC2626),
                iconBgColor = Color(0xFFFEE2E2),
                route = Routes.SPY_CAMERA
            )
        )
    }

    val calcTools = remember {
        listOf(
            ToolGridItem(
                titleNp = "सदस्यता तथा बिल ट्र्याकर",
                titleEn = "Subscription Tracker",
                icon = PIcons.Coin,
                iconTint = Color(0xFF4F46E5),
                iconBgColor = Color(0xFFEEF2FF),
                route = Routes.SUBSCRIPTION_TRACKER
            ),
            ToolGridItem(
                titleNp = "बैंक ऋण EMI",
                titleEn = "Loan EMI & FD",
                icon = PIcons.Bank,
                iconTint = Color(0xFF0284C7),
                iconBgColor = Color(0xFFE0F2FE),
                route = Routes.LOAN_EMI
            ),
            ToolGridItem(
                titleNp = "मिति रूपान्तरण",
                titleEn = "Date Converter",
                icon = PIcons.Swap,
                iconTint = Color(0xFF7C3AED),
                iconBgColor = Color(0xFFEDE9FE),
                route = Routes.CONVERTER
            ),
            ToolGridItem(
                titleNp = "विदेशी मुद्रा",
                titleEn = "Forex Currency",
                icon = PIcons.Coin,
                iconTint = Color(0xFF16A34A),
                iconBgColor = Color(0xFFDCFCE7),
                route = Routes.CURRENCY
            ),
            ToolGridItem(
                titleNp = "उमेर क्यालकुलेटर",
                titleEn = "Age Calculator",
                icon = PIcons.Hourglass,
                iconTint = Color(0xFFE11D48),
                iconBgColor = Color(0xFFFFE4E6),
                route = Routes.AGE
            ),
            ToolGridItem(
                titleNp = "बिल तथा टिप स्प्लिटर",
                titleEn = "Bill & Tip Splitter",
                icon = PIcons.Receipt,
                iconTint = Color(0xFF059669),
                iconBgColor = Color(0xFFD1FAE5),
                route = Routes.BILL_SPLITTER
            ),
            ToolGridItem(
                titleNp = "जग्गा नाप रूपान्तरण",
                titleEn = "Land Area Converter",
                icon = PIcons.Ruler,
                iconTint = Color(0xFF16A34A),
                iconBgColor = Color(0xFFDCFCE7),
                route = Routes.LAND_CONVERTER
            )
        )
    }

    val jyotishTools = remember {
        listOf(
            ToolGridItem(
                titleNp = "ज्योतिष तथा कुण्डली",
                titleEn = "Kundali & Vedic",
                icon = PIcons.Stars,
                iconTint = Color(0xFF4F46E5),
                iconBgColor = Color(0xFFEEF2FF),
                route = Routes.ASTRO
            ),
            ToolGridItem(
                titleNp = "सजीव गोचर चक्र",
                titleEn = "Transit Wheel",
                icon = PIcons.SunUp,
                iconTint = Color(0xFF0284C7),
                iconBgColor = Color(0xFFE0F2FE),
                route = Routes.ASTRO_GOCHAR
            ),
            ToolGridItem(
                titleNp = "वास्तु कम्पास",
                titleEn = "Vastu Compass",
                icon = PIcons.Compass,
                iconTint = Color(0xFFD97706),
                iconBgColor = Color(0xFFFEF3C7),
                route = Routes.VASTU_COMPASS
            ),
            ToolGridItem(
                titleNp = "साइत खोजकर्ता",
                titleEn = "Muhurat Finder",
                icon = PIcons.Stars,
                iconTint = Color(0xFF4F46E5),
                iconBgColor = Color(0xFFEEF2FF),
                route = Routes.MUHURAT
            ),
            ToolGridItem(
                titleNp = "गुण मिलान",
                titleEn = "Guna Milan",
                icon = PIcons.Users,
                iconTint = Color(0xFFE11D48),
                iconBgColor = Color(0xFFFFE4E6),
                route = Routes.GUNA_MILAN
            ),
            ToolGridItem(
                titleNp = "एकादशी / औंसी",
                titleEn = "Ekadashi List",
                icon = PIcons.Timer,
                iconTint = Color(0xFF0D9488),
                iconBgColor = Color(0xFFD1FAE5),
                route = Routes.EKADASHI
            )
        )
    }

    val prodTools = remember {
        listOf(
            ToolGridItem(
                titleNp = "ध्वनि मापक (डेसिबल)",
                titleEn = "Sound Level Meter",
                icon = PIcons.SoundMeter,
                iconTint = Color(0xFF0284C7),
                iconBgColor = Color(0xFFE0F2FE),
                route = Routes.SOUND_METER
            ),
            ToolGridItem(
                titleNp = "बानी ट्र्याकर",
                titleEn = "Habit Tracker",
                icon = PIcons.Flame,
                iconTint = Color(0xFFEA580C),
                iconBgColor = Color(0xFFFFEDD5),
                route = Routes.HABIT_TRACKER
            ),
            ToolGridItem(
                titleNp = "फाइल कन्भर्टर",
                titleEn = "File Converter",
                icon = PIcons.FileConvert,
                iconTint = Color(0xFF7C3AED),
                iconBgColor = Color(0xFFEDE9FE),
                route = Routes.FILE_CONVERTER
            ),
            ToolGridItem(
                titleNp = "पासवर्ड भल्ट",
                titleEn = "Password Vault",
                icon = PIcons.Lock,
                iconTint = Color(0xFF4F46E5),
                iconBgColor = Color(0xFFEEF2FF),
                route = Routes.VAULT
            ),
            ToolGridItem(
                titleNp = "बबल लेभल",
                titleEn = "Bubble Level",
                icon = PIcons.Level,
                iconTint = Color(0xFF16697A),
                iconBgColor = Color(0xFFD6EEF2),
                route = Routes.BUBBLE_LEVEL
            ),
            ToolGridItem(
                titleNp = "कम्पास",
                titleEn = "Compass",
                icon = PIcons.Compass,
                iconTint = Color(0xFFD97706),
                iconBgColor = Color(0xFFFEF3C7),
                route = Routes.COMPASS
            ),
            ToolGridItem(
                titleNp = "निर्णय चक्र र पासा",
                titleEn = "Decision & Dice",
                icon = PIcons.DecisionWheel,
                iconTint = Color(0xFFD97706),
                iconBgColor = Color(0xFFFEF3C7),
                route = Routes.DECISION_MAKER
            ),
            ToolGridItem(
                titleNp = "फोटो KB घटाउने",
                titleEn = "Image Resizer & KB",
                icon = PIcons.ImageCompress,
                iconTint = Color(0xFF059669),
                iconBgColor = Color(0xFFD1FAE5),
                route = Routes.IMAGE_COMPRESSOR
            ),
            ToolGridItem(
                titleNp = "वाईफाई फाइल ड्रप",
                titleEn = "LAN File Drop",
                icon = PIcons.WifiDrop,
                iconTint = Color(0xFF2563EB),
                iconBgColor = Color(0xFFDBEAFE),
                route = Routes.LAN_DROP
            ),
            ToolGridItem(
                titleNp = "स्पीड टेस्ट तथा वाईफाई",
                titleEn = "Speed Test & Wi-Fi",
                icon = PIcons.Speedometer,
                iconTint = Color(0xFFDC2626),
                iconBgColor = Color(0xFFFEE2E2),
                route = Routes.SPEED_TEST
            ),
            ToolGridItem(
                titleNp = "अल्ट्रासोनिक सिट्टी",
                titleEn = "Pet Whistle & Tone",
                icon = PIcons.Whistle,
                iconTint = Color(0xFF7C3AED),
                iconBgColor = Color(0xFFEDE9FE),
                route = Routes.PET_WHISTLE
            ),
            ToolGridItem(
                titleNp = "क्युआर जेनेरेटर",
                titleEn = "QR Generator",
                icon = PIcons.QrCode,
                iconTint = Color(0xFF4F46E5),
                iconBgColor = Color(0xFFEEF2FF),
                route = Routes.QR
            ),
            ToolGridItem(
                titleNp = "भ्वाइस नोट",
                titleEn = "Voice Notes",
                icon = PIcons.Mic,
                iconTint = Color(0xFFEA580C),
                iconBgColor = Color(0xFFFFEDD5),
                route = Routes.VOICE
            )
        )
    }

    val allTools = remember(dailyTools, citizenTools, calcTools, jyotishTools, prodTools) {
        dailyTools + citizenTools + calcTools + jyotishTools + prodTools
    }

    val favoriteRoutesList = FavoriteToolsManager.favoriteRoutes.toList()
    val favoriteItems = remember(favoriteRoutesList, allTools) {
        allTools.filter { FavoriteToolsManager.isFavorite(it.route) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(bottom = 2.dp)) {
                Text(
                    text = if (isEn) "Services & Tools" else "सेवा तथा टूल्स",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Section: Favorite Tools (appears dynamically at top when user favorites any tool)
        if (favoriteItems.isNotEmpty()) {
            item {
                ToolSectionHeader(
                    title = if (isEn) "Favorite Tools" else "मनपर्ने सेवाहरू",
                    icon = PIcons.StarFilled
                )
                Spacer(Modifier.height(8.dp))
                ToolsGrid(
                    items = favoriteItems,
                    isEn = isEn,
                    onOpenTool = onOpenTool,
                    onToggleFavorite = { route ->
                        FavoriteToolsManager.toggleFavorite(context, route)
                    }
                )
            }
        }

        // Section 1: Daily Services
        item {
            ToolSectionHeader(
                title = if (isEn) "Daily Services" else "दैनिक सेवाहरू",
                icon = PIcons.Sun
            )
            Spacer(Modifier.height(8.dp))
            ToolsGrid(
                items = dailyTools,
                isEn = isEn,
                onOpenTool = onOpenTool,
                onToggleFavorite = { route ->
                    FavoriteToolsManager.toggleFavorite(context, route)
                }
            )
        }

        // Section 2: Citizen Services
        item {
            ToolSectionHeader(
                title = if (isEn) "Citizen Services" else "नागरिक सेवा",
                icon = PIcons.Shield
            )
            Spacer(Modifier.height(8.dp))
            ToolsGrid(
                items = citizenTools,
                isEn = isEn,
                onOpenTool = onOpenTool,
                onToggleFavorite = { route ->
                    FavoriteToolsManager.toggleFavorite(context, route)
                }
            )
        }

        // Section 3: Calculators & Finance
        item {
            ToolSectionHeader(
                title = if (isEn) "Finance & Calculators" else "वित्तीय तथा क्यालकुलेटर",
                icon = PIcons.Bank
            )
            Spacer(Modifier.height(8.dp))
            ToolsGrid(
                items = calcTools,
                isEn = isEn,
                onOpenTool = onOpenTool,
                onToggleFavorite = { route ->
                    FavoriteToolsManager.toggleFavorite(context, route)
                }
            )
        }

        // Section 4: Panchang & Jyotish
        item {
            ToolSectionHeader(
                title = if (isEn) "Panchang & Jyotish" else "पञ्चाङ्ग तथा ज्योतिष",
                icon = PIcons.Stars
            )
            Spacer(Modifier.height(8.dp))
            ToolsGrid(
                items = jyotishTools,
                isEn = isEn,
                onOpenTool = onOpenTool,
                onToggleFavorite = { route ->
                    FavoriteToolsManager.toggleFavorite(context, route)
                }
            )
        }

        // Section 5: Productivity, Network & Media
        item {
            ToolSectionHeader(
                title = if (isEn) "Digital Utilities & Network" else "डिजिटल युटिलिटी तथा नेटवर्क",
                icon = PIcons.Sparkle
            )
            Spacer(Modifier.height(8.dp))
            ToolsGrid(
                items = prodTools,
                isEn = isEn,
                onOpenTool = onOpenTool,
                onToggleFavorite = { route ->
                    FavoriteToolsManager.toggleFavorite(context, route)
                }
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToolsGrid(
    items: List<ToolGridItem>,
    isEn: Boolean,
    onOpenTool: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val chunked = items.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    ToolGridCard(
                        item = item,
                        isEn = isEn,
                        isFavorite = FavoriteToolsManager.isFavorite(item.route),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenTool(item.route) },
                        onToggleFavorite = { onToggleFavorite(item.route) }
                    )
                }
                // If odd number in row, fill with empty spacer
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ToolGridCard(
    item: ToolGridItem,
    isEn: Boolean,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "toolCardScale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(58.dp)
            .clip(RoundedCornerShape(13.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = true,
                    color = item.iconTint.copy(alpha = 0.2f)
                ),
                onClick = onClick
            ),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 0.dp else 0.5.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 26.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon in soft circular badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(item.iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.iconTint,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = if (isEn) item.titleEn else item.titleNp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        lineHeight = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite toggle star button in top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false, radius = 12.dp),
                        onClick = onToggleFavorite
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) PIcons.StarFilled else PIcons.Star,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
