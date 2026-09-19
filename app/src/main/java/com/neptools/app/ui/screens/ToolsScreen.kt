package com.neptools.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ripple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.tools.FavoriteToolsManager
import com.neptools.app.core.tools.RecentToolsManager
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

private val TOOL_SEARCH_KEYWORDS: Map<String, List<String>> = mapOf(
    Routes.WEATHER to listOf("weather", "forecast", "rain", "temperature", "temp", "hawa", "badal", "मौसम", "पूर्वानुमान", "पानी", "तापक्रम"),
    Routes.RADIO to listOf("fm", "radio", "music", "audio", "stations", "stream", "live", "रेडियो", "एफएम", "गीत", "संगीत"),
    Routes.FUEL to listOf("fuel", "petrol", "diesel", "gas", "lpg", "noc", "price", "पेट्रोलियम", "पेट्रोल", "डिजेल", "भाउ", "तेल", "मूल्य"),
    Routes.KALIMATI to listOf("kalimati", "vegetables", "fruits", "market", "tarkari", "price", "कालिमाटी", "तरकारी", "फलफूल", "बजार", "भाउ"),
    Routes.RASHIFAL to listOf("rashifal", "horoscope", "zodiac", "astrology", "rashi", "राशिफल", "राशि", "दैनिक राशिफल"),
    Routes.DRIVING_LICENSE to listOf("driving", "license", "licence", "likhit", "trial", "quiz", "dotm", "यातायात", "सवारी", "लाइसेन्स", "लिखित"),
    Routes.TEMPLATES to listOf("templates", "nibedan", "application", "letter", "govt", "सरकारी", "निवेदन", "दरखास्त", "पत्र"),
    Routes.EMERGENCY to listOf("emergency", "police", "ambulance", "hospital", "helpline", "sos", "call", "आपतकालीन", "प्रहरी", "एम्बुलेन्स", "सम्पर्क"),
    Routes.BILL_CALC to listOf("bill", "electricity", "nea", "water", "khanepani", "tariff", "महसुल", "विद्युत", "खानेपानी", "क्यालकुलेटर", "बत्ती"),
    Routes.POSTAL to listOf("postal", "zip", "pin", "post office", "code", "हुलाक", "पिन कोड", "जिप"),
    Routes.SPY_CAMERA to listOf("spy", "camera", "detector", "hidden", "infrared", "गोप्य", "क्यामेरा", "डिटेक्टर"),
    Routes.SUBSCRIPTION_TRACKER to listOf("subscription", "tracker", "expense", "bill", "recurring", "सदस्यता", "बिल ट्र्याकर", "खर्च"),
    Routes.LOAN_EMI to listOf("loan", "emi", "interest", "fd", "fixed deposit", "bank", "finance", "ऋण", "ईएमआई", "किस्ता", "मुद्दती", "ब्याज"),
    Routes.CONVERTER to listOf("date", "converter", "bs to ad", "ad to bs", "bikram sambat", "gregorian", "मिति", "रूपान्तरण", "पात्रो"),
    Routes.CURRENCY to listOf("currency", "forex", "exchange", "nrb", "dollar", "rate", "विदेशी", "मुद्रा", "विनिमय", "डलर"),
    Routes.AGE to listOf("age", "calculator", "birthday", "birth", "years", "उमेर", "जन्ममिति", "क्यालकुलेटर"),
    Routes.BILL_SPLITTER to listOf("bill", "split", "splitter", "tip", "group", "restaurant", "share", "बिल", "टिप", "स्प्लिटर", "बाँडफाँड"),
    Routes.LAND_CONVERTER to listOf("land", "area", "converter", "ropani", "ana", "paisa", "daam", "bigha", "kattha", "dhur", "जग्गा", "नाप", "क्षेत्रफल", "रोपनी", "आना", "बिघा", "कट्ठा"),
    Routes.ASTRO to listOf("kundali", "vedic", "astrology", "jyotish", "chart", "horoscope", "कुण्डली", "ज्योतिष"),
    Routes.ASTRO_GOCHAR to listOf("gochar", "transit", "wheel", "planets", "graha", "सजीव", "गोचर", "ग्रह"),
    Routes.VASTU_COMPASS to listOf("vastu", "compass", "direction", "home", "वास्तु", "कम्पास", "दिशा"),
    Routes.MUHURAT to listOf("muhurat", "sait", "auspicious", "time", "लग्न", "साइत", "शुभ साइत", "मुहूर्त"),
    Routes.GUNA_MILAN to listOf("guna", "milan", "marriage", "matchmaking", "vivah", "गुण", "मिलान", "विवाह"),
    Routes.EKADASHI to listOf("ekadashi", "aunsi", "purnima", "fasting", "brata", "एकादशी", "औंसी", "पूर्णिमा", "व्रत"),
    Routes.SOUND_METER to listOf("sound", "meter", "decibel", "db", "noise", "microphone", "ध्वनि", "मापक", "डेसिबल", "आवाज"),
    Routes.HABIT_TRACKER to listOf("habit", "tracker", "streak", "daily", "routine", "बानी", "ट्र्याकर", "दैनिक"),
    Routes.FILE_CONVERTER to listOf("file", "converter", "pdf", "image", "document", "format", "फाइल", "कन्भर्टर"),
    Routes.VAULT to listOf("vault", "password", "secure", "pin", "credential", "पासवर्ड", "भल्ट", "सुरक्षा"),
    Routes.BUBBLE_LEVEL to listOf("bubble", "level", "spirit", "angle", "surface", "बबल", "लेभल", "समतल"),
    Routes.COMPASS to listOf("compass", "direction", "north", "heading", "कम्पास", "दिशा"),
    Routes.DECISION_MAKER to listOf("decision", "wheel", "dice", "coin", "flip", "roll", "spin", "निर्णय", "चक्र", "पासा", "सिक्का"),
    Routes.IMAGE_COMPRESSOR to listOf("image", "compress", "resize", "photo", "kb", "mb", "reduce", "तस्बिर", "साइज", "फोटो"),
    Routes.LAN_DROP to listOf("lan", "wifi", "file", "drop", "share", "transfer", "local", "वाईफाई", "ड्रप", "साझेदारी"),
    Routes.SPEED_TEST to listOf("speed", "test", "wifi", "internet", "mbps", "ping", "नेट", "इन्टरनेट", "स्पीड"),
    Routes.PET_WHISTLE to listOf("pet", "whistle", "dog", "ultrasonic", "frequency", "tone", "कुकुर", "सिट्टी", "अल्ट्रासोनिक"),
    Routes.QR to listOf("qr", "generator", "scanner", "code", "barcode", "क्युआर", "जेनेरेटर", "स्क्यानर"),
    Routes.VOICE to listOf("voice", "notes", "recorder", "audio", "memo", "भ्वाइस", "नोट", "रेकर्डर")
)

@Composable
fun ToolsScreen(onOpenTool: (String) -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FavoriteToolsManager.load(context)
        RecentToolsManager.load(context)
    }

    val handleOpenTool: (String) -> Unit = { route ->
        RecentToolsManager.recordToolUsed(context, route)
        onOpenTool(route)
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
                titleNp = "बैंक ऋण ईएमआई",
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
                titleNp = "तस्बिर साइज घटाउने",
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

    val recentRoutesList = RecentToolsManager.recentRoutes.toList()
    val recentItems = remember(recentRoutesList, allTools) {
        recentRoutesList.mapNotNull { route -> allTools.find { it.route == route } }
    }

    val filteredTools = remember(searchQuery, allTools) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) emptyList()
        else {
            allTools.filter { tool ->
                tool.titleEn.lowercase().contains(q) ||
                tool.titleNp.lowercase().contains(q) ||
                (TOOL_SEARCH_KEYWORDS[tool.route]?.any { it.contains(q) } == true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEn) "Services & Tools" else "सेवा तथा टूल्स",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = if (isEn) "Search tools (e.g. EMI, QR, Weather)..." else "टूल्स खोज्नुहोस् (जस्तै: ईएमआई, क्युआर, मौसम)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = PIcons.Search,
                            contentDescription = if (isEn) "Search" else "खोज्नुहोस्",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = PIcons.Cross,
                                    contentDescription = if (isEn) "Clear" else "खाली गर्नुहोस्",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }
        }

        if (searchQuery.isNotBlank()) {
            // Search Results Mode
            if (filteredTools.isNotEmpty()) {
                item {
                    ToolSectionHeader(
                        title = if (isEn) "${filteredTools.size} Tools Found" else "${NepaliNames.toDevanagari(filteredTools.size)} वटा सेवा फेला परे",
                        icon = PIcons.Search
                    )
                    Spacer(Modifier.height(8.dp))
                    ToolsGrid(
                        items = filteredTools,
                        isEn = isEn,
                        onOpenTool = handleOpenTool,
                        onToggleFavorite = { route ->
                            FavoriteToolsManager.toggleFavorite(context, route)
                        }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PIcons.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = if (isEn) "No tools found" else "कुनै सेवा फेला परेन",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn)
                                    "No tools match \"$searchQuery\". Try searching for EMI, Date, QR, Weather, or Calendar."
                                else
                                    "\"$searchQuery\" सँग मिल्ने कुनै सेवा भेटिएन। ईएमआई, मिति, क्युआर वा मौसम खोज्नुहोस्।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // Normal Categorized View

            // Section: Recently Used Tools
            if (recentItems.isNotEmpty()) {
                item {
                    ToolSectionHeader(
                        title = if (isEn) "Recently Used" else "भर्खरै प्रयोग गरिएका",
                        icon = PIcons.Hourglass,
                        actionText = if (isEn) "Clear" else "हटाउनुहोस्",
                        onAction = { RecentToolsManager.clearRecents(context) }
                    )
                    Spacer(Modifier.height(8.dp))
                    ToolsGrid(
                        items = recentItems,
                        isEn = isEn,
                        onOpenTool = handleOpenTool,
                        onToggleFavorite = { route ->
                            FavoriteToolsManager.toggleFavorite(context, route)
                        }
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
                        onOpenTool = handleOpenTool,
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
                    onOpenTool = handleOpenTool,
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
                    onOpenTool = handleOpenTool,
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
                    onOpenTool = handleOpenTool,
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
                    onOpenTool = handleOpenTool,
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
                    onOpenTool = handleOpenTool,
                    onToggleFavorite = { route ->
                        FavoriteToolsManager.toggleFavorite(context, route)
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolSectionHeader(
    title: String,
    icon: ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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

        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
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
                indication = ripple(
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
                        indication = ripple(bounded = false, radius = 12.dp),
                        onClick = onToggleFavorite
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) PIcons.StarFilled else PIcons.Star,
                    contentDescription = if (isFavorite) (if (isEn) "Remove from favorites" else "मनपर्नेबाट हटाउनुहोस्") else (if (isEn) "Add to favorites" else "मनपर्नेमा थप्नुहोस्"),
                    tint = if (isFavorite) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
