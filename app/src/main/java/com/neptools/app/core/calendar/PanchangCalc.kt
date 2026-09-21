package com.neptools.app.core.calendar

import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.ephemeris.AyanamsaType
import com.neptools.app.astrology.ephemeris.HighPrecisionEphemeris
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.floor

@androidx.compose.runtime.Immutable
data class Panchang(
    val tithiName: String,
    val tithiNameEn: String = "",
    val paksha: String,
    val pakshaEn: String = "",
    val tithiEndsPercent: Int,
    val nakshatraName: String,
    val nakshatraNameEn: String = "",
    val yogaName: String,
    val yogaNameEn: String = "",
    val lunarMasaIndex: Int = 0,
    val lunarMasaName: String = "",
    val lunarMasaNameEn: String = ""
)

object PanchangCalc {

    private val tithiNames = listOf(
        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पञ्चमी", "षष्ठी", "सप्तमी",
        "अष्टमी", "नवमी", "दशमी", "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी"
    )
    private val tithiNamesEn = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami", "Shasthi", "Saptami",
        "Ashtami", "Navami", "Dashami", "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi"
    )
    private val nakshatraNames = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु",
        "पुष्य", "अश्लेषा", "मघा", "पूर्वा फाल्गुनी", "उत्तरा फाल्गुनी", "हस्त",
        "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा", "मूल", "पूर्वाषाढा",
        "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वा भाद्रपद", "उत्तरा भाद्रपद", "रेवती"
    )
    private val nakshatraNamesEn = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashirsha", "Ardra", "Punarvasu",
        "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
        "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
        "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )
    private val yogaNames = listOf(
        "विष्कम्भ", "प्रीति", "आयुष्मान्", "सौभाग्य", "शोभन", "अतिगण्ड", "सुकर्मा",
        "धृति", "शूल", "गण्ड", "वृद्धि", "ध्रुव", "व्याघात", "हर्षण", "वज्र",
        "सिद्धि", "व्यतिपात", "वरीयान्", "परिघ", "शिव", "सिद्ध", "साध्य", "शुभ",
        "शुक्ल", "ब्रह्म", "इन्द्र", "वैधृति"
    )
    private val yogaNamesEn = listOf(
        "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda", "Sukarma",
        "Dhriti", "Shula", "Ganda", "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
        "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva", "Siddha", "Sadhya", "Shubha",
        "Shukla", "Brahma", "Indra", "Vaidhriti"
    )
    private val masaNames = listOf(
        "वैशाख", "ज्येष्ठ", "आषाढ", "श्रावण", "भाद्रपद", "आश्विन",
        "कार्तिक", "मार्गशीर्ष", "पौष", "माघ", "फाल्गुन", "चैत्र"
    )
    private val masaNamesEn = listOf(
        "Vaishakha", "Jyeshtha", "Ashadha", "Shravana", "Bhadrapada", "Ashvina",
        "Kartika", "Margashirsha", "Pausha", "Magha", "Phalguna", "Chaitra"
    )

    private val ephemeris = HighPrecisionEphemeris()

    fun compute(date: LocalDate): Panchang {
        // High-precision VSOP87 + NOVAS C 3.1 + ELP-2000 calculations
        // Sampled at 06:00 NPT (00:15 UT) near sunrise for Nepal standard observation
        val jdUt = ephemeris.julianDay(date, LocalTime.of(6, 0), 5.75)
        val ayanamsa = ephemeris.ayanamsa(jdUt, AyanamsaType.LAHIRI)
        val tropical = ephemeris.tropicalLongitudes(jdUt)

        val (sunTrop, _) = tropical.getValue(Planet.SUN)
        val (moonTrop, _) = tropical.getValue(Planet.MOON)

        // Sidereal coordinates (Nirayana) for authentic Vedic Panchang
        val sunSid = HighPrecisionEphemeris.normalizeDeg(sunTrop - ayanamsa)
        val moonSid = HighPrecisionEphemeris.normalizeDeg(moonTrop - ayanamsa)

        // Tithi based on Moon-Sun elongation (12 deg per tithi)
        val elong = HighPrecisionEphemeris.normalizeDeg(moonTrop - sunTrop)
        val tithiIdx = floor(elong / 12.0).toInt().coerceIn(0, 29)
        val frac = (elong - tithiIdx * 12.0) / 12.0
        val paksha = if (tithiIdx < 15) "शुक्ल पक्ष" else "कृष्ण पक्ष"
        val pakshaEn = if (tithiIdx < 15) "Shukla Paksha" else "Krishna Paksha"
        val within = tithiIdx % 15
        val tithiName = if (within == 14) {
            if (tithiIdx < 15) "पूर्णिमा" else "औंसी"
        } else tithiNames[within]
        val tithiNameEn = if (within == 14) {
            if (tithiIdx < 15) "Purnima" else "Aunsi"
        } else tithiNamesEn[within]

        // Nakshatra strictly based on Sidereal Moon (13 deg 20 min per nakshatra)
        val naksIdx = floor(moonSid / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        // Yoga strictly based on Sidereal (Sun + Moon) (13 deg 20 min per yoga)
        val yogaRaw = HighPrecisionEphemeris.normalizeDeg(sunSid + moonSid)
        val yogaIdx = floor(yogaRaw / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        // Lunar Masa (Amanta for Shukla, Purnimanta for Krishna Paksha)
        val daysOffset = if (tithiIdx < 15) {
            -(elong / 12.190749)
        } else {
            (360.0 - elong) / 12.190749
        }
        val sunAtNewMoon = HighPrecisionEphemeris.normalizeDeg(sunSid + daysOffset * 0.9856)
        val masaIdx = floor(sunAtNewMoon / 30.0).toInt().coerceIn(0, 11)

        return Panchang(
            tithiName = tithiName,
            tithiNameEn = tithiNameEn,
            paksha = paksha,
            pakshaEn = pakshaEn,
            tithiEndsPercent = (frac * 100).toInt().coerceIn(0, 99),
            nakshatraName = nakshatraNames[naksIdx],
            nakshatraNameEn = nakshatraNamesEn[naksIdx],
            yogaName = yogaNames[yogaIdx],
            yogaNameEn = yogaNamesEn[yogaIdx],
            lunarMasaIndex = masaIdx,
            lunarMasaName = masaNames[masaIdx],
            lunarMasaNameEn = masaNamesEn[masaIdx]
        )
    }
}
