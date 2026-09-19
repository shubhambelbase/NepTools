package com.neptools.app.astrology.vedic

import kotlin.math.abs

/**
 * Authentic 36-Point Ashta Koota Vedic Marriage Compatibility Engine.
 * Implements classical rules from Brihat Parashara Hora Shastra and Muhurta Chintamani.
 * 100% offline, deterministic, zero third-party dependencies.
 */
object AshtakootaGunaMilan {

    val NAKSHATRAS_NP = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु",
        "पुष्य", "अश्लेषा", "मघा", "पूर्वा फाल्गुनी", "उत्तरा फाल्गुनी", "हस्त",
        "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा", "मूल", "पूर्वाषाढा",
        "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वा भाद्रपद", "उत्तरा भाद्रपद", "रेवती"
    )

    val NAKSHATRAS_EN = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashirsha", "Ardra", "Punarvasu",
        "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
        "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
        "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    val RASHIS_NP = listOf(
        "मेष", "वृष", "मिथुन", "कर्कट", "सिंह", "कन्या",
        "तुला", "वृश्चिक", "धनु", "मकर", "कुम्भ", "मीन"
    )

    val RASHIS_EN = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    // Primary Rashi index (0..11) mapped to each Nakshatra
    val DEFAULT_RASHI_FOR_NAKSHATRA = intArrayOf(
        0, 0, 1, 1, 1, 2, 2, 3, 3, // 0..8
        4, 4, 5, 5, 6, 6, 6, 7, 7, // 9..17
        8, 8, 9, 9, 10, 10, 10, 11, 11 // 18..26
    )

    // Nadi: 0 = Adi, 1 = Madhya, 2 = Antya
    private val NADI_MAP = intArrayOf(
        0, 1, 2, 2, 1, 0, 0, 1, 2, // Ashwini(0)..Ashlesha(8)
        2, 1, 0, 0, 1, 2, 2, 1, 0, // Magha(9)..Jyeshtha(17)
        0, 1, 2, 2, 1, 0, 0, 1, 2  // Mula(18)..Revati(26)
    )

    // Gana: 0 = Deva, 1 = Manushya, 2 = Rakshasa
    private val GANA_MAP = intArrayOf(
        0, 1, 2, 1, 0, 1, 0, 0, 2, // 0..8
        2, 1, 1, 0, 2, 0, 2, 0, 2, // 9..17
        2, 1, 1, 0, 2, 2, 1, 1, 0  // 18..26
    )

    // Yoni animals: 14 types
    // 0 Horse, 1 Elephant, 2 Sheep, 3 Serpent, 4 Dog, 5 Cat, 6 Rat,
    // 7 Cow, 8 Buffalo, 9 Tiger, 10 Deer, 11 Monkey, 12 Mongoose, 13 Lion
    private val YONI_MAP = intArrayOf(
        0, 1, 2, 3, 3, 4, 5, 2, 5,  // 0..8
        6, 6, 7, 8, 9, 8, 9, 10, 10, // 9..17
        4, 11, 12, 11, 13, 0, 13, 7, 1 // 18..26
    )

    val YONI_NAMES_NP = listOf(
        "अश्व", "गज", "मेष", "सर्प", "श्वान", "मार्जार",
        "मूषक", "गौ", "महिष", "व्याघ्र", "मृग", "वानर",
        "नकुल", "सिंह"
    )

    val YONI_NAMES_EN = listOf(
        "Horse", "Elephant", "Sheep", "Serpent", "Dog", "Cat",
        "Rat", "Cow", "Buffalo", "Tiger", "Deer", "Monkey",
        "Mongoose", "Lion"
    )

    val NADI_NAMES_NP = listOf("आदि", "मध्य", "अन्त्य")
    val NADI_NAMES_EN = listOf("Adi", "Madhya", "Antya")

    val GANA_NAMES_NP = listOf("देव", "मनुष्य", "राक्षस")
    val GANA_NAMES_EN = listOf("Deva", "Manushya", "Rakshasa")

    // Rashi planetary lords:
    // 0 Aries (Mars), 1 Taurus (Venus), 2 Gemini (Mercury), 3 Cancer (Moon),
    // 4 Leo (Sun), 5 Virgo (Mercury), 6 Libra (Venus), 7 Scorpio (Mars),
    // 8 Sagittarius (Jupiter), 9 Capricorn (Saturn), 10 Aquarius (Saturn), 11 Pisces (Jupiter)
    private val RASHI_LORD = intArrayOf(2, 5, 3, 1, 0, 3, 5, 2, 4, 6, 6, 4)
    // 0 Sun, 1 Moon, 2 Mars, 3 Mercury, 4 Jupiter, 5 Venus, 6 Saturn

    // Natural Planetary Relationship matrix:
    // 2 = Friend, 1 = Neutral, 0 = Enemy
    private val PLANET_FRIENDSHIP = arrayOf(
        intArrayOf(2, 2, 2, 1, 2, 0, 0), // 0 Sun: Moon, Mars, Jup (F); Merc (N); Ven, Sat (E)
        intArrayOf(2, 2, 1, 2, 1, 1, 1), // 1 Moon: Sun, Merc (F); Mars, Jup, Ven, Sat (N); None (E)
        intArrayOf(2, 2, 2, 0, 2, 1, 1), // 2 Mars: Sun, Moon, Jup (F); Ven, Sat (N); Merc (E)
        intArrayOf(2, 0, 1, 2, 1, 2, 1), // 3 Mercury: Sun, Ven (F); Mars, Jup, Sat (N); Moon (E)
        intArrayOf(2, 2, 2, 0, 2, 0, 1), // 4 Jupiter: Sun, Moon, Mars (F); Sat (N); Merc, Ven (E)
        intArrayOf(0, 0, 1, 2, 1, 2, 2), // 5 Venus: Merc, Sat (F); Mars, Jup (N); Sun, Moon (E)
        intArrayOf(0, 0, 0, 2, 1, 2, 2)  // 6 Saturn: Merc, Ven (F); Jup (N); Sun, Moon, Mars (E)
    )

    // Sworn enemy Yoni pairs
    private val YONI_ENEMIES = setOf(
        0 to 8, 8 to 0,   // Horse vs Buffalo
        1 to 13, 13 to 1, // Elephant vs Lion
        2 to 11, 11 to 2, // Sheep vs Monkey
        3 to 12, 12 to 3, // Serpent vs Mongoose
        4 to 10, 10 to 4, // Dog vs Deer
        5 to 6, 6 to 5,   // Cat vs Rat
        7 to 9, 9 to 7    // Cow vs Tiger
    )

    data class KootaScore(
        val key: String,
        val nameNp: String,
        val nameEn: String,
        val maxPoints: Double,
        val earnedPoints: Double,
        val boyValueNp: String,
        val boyValueEn: String,
        val girlValueNp: String,
        val girlValueEn: String,
        val category: String, // "Mental", "Health", "Prosperity", "Physical"
        val explanationNp: String,
        val explanationEn: String,
        val hasDosha: Boolean = false,
        val doshaNameNp: String? = null,
        val doshaNameEn: String? = null
    ) {
        val boyValue: String get() = boyValueNp
        val girlValue: String get() = girlValueNp
        fun boyValue(isEn: Boolean): String = if (isEn) boyValueEn else boyValueNp
        fun girlValue(isEn: Boolean): String = if (isEn) girlValueEn else girlValueNp
        fun name(isEn: Boolean): String = if (isEn) nameEn else nameNp
        fun explanation(isEn: Boolean): String = if (isEn) explanationEn else explanationNp
        fun doshaName(isEn: Boolean): String? = if (isEn) doshaNameEn else doshaNameNp
    }

    data class MilanResult(
        val totalPoints: Double,
        val maxPoints: Double = 36.0,
        val percentage: Int,
        val verdictNp: String,
        val verdictEn: String,
        val verdictLevel: VerdictLevel,
        val kootas: List<KootaScore>,
        val hasNadiDosha: Boolean,
        val hasBhakootDosha: Boolean,
        val hasGanaDosha: Boolean,
        val summaryByDomain: DomainSummary
    )

    enum class VerdictLevel { EXCELLENT, GOOD, AVERAGE, LOW }

    data class DomainSummary(
        val mentalHarmony: Double, // Maitri + Gana (11 max)
        val healthAndProgeny: Double, // Nadi + Tara (11 max)
        val familyAndProsperity: Double, // Bhakoot + Vashya + Varna (10 max)
        val physicalCompatibility: Double // Yoni (4 max)
    )

    fun calculate(
        boyNakIdx: Int,
        girlNakIdx: Int,
        boyRashiIdx: Int = DEFAULT_RASHI_FOR_NAKSHATRA[boyNakIdx.coerceIn(0, 26)],
        girlRashiIdx: Int = DEFAULT_RASHI_FOR_NAKSHATRA[girlNakIdx.coerceIn(0, 26)]
    ): MilanResult {
        val bNak = boyNakIdx.coerceIn(0, 26)
        val gNak = girlNakIdx.coerceIn(0, 26)
        val bRashi = boyRashiIdx.coerceIn(0, 11)
        val gRashi = girlRashiIdx.coerceIn(0, 11)

        val varna = computeVarna(bRashi, gRashi)
        val vashya = computeVashya(bRashi, gRashi)
        val tara = computeTara(bNak, gNak)
        val yoni = computeYoni(bNak, gNak)
        val maitri = computeMaitri(bRashi, gRashi)
        val gana = computeGana(bNak, gNak)
        val bhakoot = computeBhakoot(bRashi, gRashi)
        val nadi = computeNadi(bNak, gNak)

        val list = listOf(varna, vashya, tara, yoni, maitri, gana, bhakoot, nadi)
        val total = list.sumOf { it.earnedPoints }
        val pct = ((total / 36.0) * 100).toInt().coerceIn(0, 100)

        val verdictLevel = when {
            total >= 28.0 -> VerdictLevel.EXCELLENT
            total >= 18.0 -> VerdictLevel.GOOD
            total >= 12.0 -> VerdictLevel.AVERAGE
            else -> VerdictLevel.LOW
        }

        val verdictNp = when (verdictLevel) {
            VerdictLevel.EXCELLENT -> "उत्कृष्ट जोडी (२८+ गुण) — वैवाहिक जीवन अति सुखद, समृद्ध र सफल रहनेछ।"
            VerdictLevel.GOOD -> "राम्रो जोडी (१८+ गुण) — परम्परागत रूपमा विवाहका लागि पूर्ण उपयुक्त र फलदायी।"
            VerdictLevel.AVERAGE -> "मध्यम जोडी (१२-१७ गुण) — केही क्षेत्रमा मतभेद सम्भव; ज्योतिषीय शान्ति/परामर्श उपयोगी।"
            VerdictLevel.LOW -> "न्यून मेल (१२ भन्दा कम) — गम्भीर दोषहरू उपस्थित; विद्धान ज्योतिषीसँग विस्तृत परामर्श लिनुहोला।"
        }

        val verdictEn = when (verdictLevel) {
            VerdictLevel.EXCELLENT -> "Excellent Match (28+ Points) — Highly auspicious, deep harmony across life domains."
            VerdictLevel.GOOD -> "Good Match (18+ Points) — Auspicious and traditionally recommended for marriage."
            VerdictLevel.AVERAGE -> "Average Match (12-17 Points) — Certain doshas present; astrological remedies advised."
            VerdictLevel.LOW -> "Low Compatibility (<12 Points) — Significant doshas present; consultation advised."
        }

        val domain = DomainSummary(
            mentalHarmony = maitri.earnedPoints + gana.earnedPoints,
            healthAndProgeny = nadi.earnedPoints + tara.earnedPoints,
            familyAndProsperity = bhakoot.earnedPoints + vashya.earnedPoints + varna.earnedPoints,
            physicalCompatibility = yoni.earnedPoints
        )

        return MilanResult(
            totalPoints = total,
            maxPoints = 36.0,
            percentage = pct,
            verdictNp = verdictNp,
            verdictEn = verdictEn,
            verdictLevel = verdictLevel,
            kootas = list,
            hasNadiDosha = nadi.hasDosha,
            hasBhakootDosha = bhakoot.hasDosha,
            hasGanaDosha = gana.hasDosha,
            summaryByDomain = domain
        )
    }

    // 1. Varna (1 point max)
    private fun computeVarna(bRashi: Int, gRashi: Int): KootaScore {
        // Brahmin: 3, 7, 11 (Cancer, Scorpio, Pisces) -> rank 4
        // Kshatriya: 0, 4, 8 (Aries, Leo, Sagittarius) -> rank 3
        // Vaishya: 1, 5, 9 (Taurus, Virgo, Capricorn) -> rank 2
        // Shudra: 2, 6, 10 (Gemini, Libra, Aquarius) -> rank 1
        fun rank(r: Int): Int = when (r) {
            3, 7, 11 -> 4
            0, 4, 8 -> 3
            1, 5, 9 -> 2
            else -> 1
        }
        fun nameNp(rank: Int) = when (rank) { 4 -> "ब्राह्मण"; 3 -> "क्षत्रिय"; 2 -> "वैश्य"; else -> "शूद्र" }
        fun nameEn(rank: Int) = when (rank) { 4 -> "Brahmin"; 3 -> "Kshatriya"; 2 -> "Vaishya"; else -> "Shudra" }

        val bRank = rank(bRashi)
        val gRank = rank(gRashi)
        val pts = if (bRank >= gRank) 1.0 else 0.0

        return KootaScore(
            key = "varna",
            nameNp = "वर्ण",
            nameEn = "Varna",
            maxPoints = 1.0,
            earnedPoints = pts,
            boyValueNp = nameNp(bRank),
            boyValueEn = nameEn(bRank),
            girlValueNp = nameNp(gRank),
            girlValueEn = nameEn(gRank),
            category = "Prosperity",
            explanationNp = if (pts > 0) "वर र वधु बीच कार्यशैली र आध्यात्मिक अहंको राम्रो सन्तुलन छ।" else "वर र वधुको कार्यशैली र दृष्टिकोणमा भिन्नता देखिन्छ।",
            explanationEn = if (pts > 0) "Favorable ego balance and cooperative work orientation." else "Difference in temperament and ego perspective."
        )
    }

    // 2. Vashya (2 points max)
    private fun computeVashya(bRashi: Int, gRashi: Int): KootaScore {
        // 0 Chatushpada (Aries, Taurus, Sag-2nd, Cap-1st)
        // 1 Manava (Gemini, Virgo, Libra, Aquarius, Sag-1st)
        // 2 Jalachara (Cancer, Pisces, Cap-2nd)
        // 3 Vanachara (Leo)
        // 4 Keeta (Scorpio)
        fun vType(r: Int): Int = when (r) {
            0, 1 -> 0
            2, 5, 6, 10 -> 1
            3, 11 -> 2
            4 -> 3
            7 -> 4
            8 -> 1 // default first half
            9 -> 0 // default first half
            else -> 1
        }
        val typeNamesNp = listOf("चतुष्पद", "मानव", "जलचर", "वनचर", "कीट")
        val typeNamesEn = listOf("Chatushpada", "Manava", "Jalachara", "Vanachara", "Keeta")
        val bT = vType(bRashi)
        val gT = vType(gRashi)

        val pts = when {
            bT == gT -> 2.0
            (bT == 1 && gT == 0) || (bT == 0 && gT == 1) -> 1.0
            (bT == 1 && gT == 2) || (bT == 2 && gT == 1) -> 1.0
            (bT == 3 || gT == 3) -> 0.0 // Lion preys on others
            else -> 0.5
        }

        return KootaScore(
            key = "vashya",
            nameNp = "वश्य",
            nameEn = "Vashya",
            maxPoints = 2.0,
            earnedPoints = pts,
            boyValueNp = typeNamesNp[bT],
            boyValueEn = typeNamesEn[bT],
            girlValueNp = typeNamesNp[gT],
            girlValueEn = typeNamesEn[gT],
            category = "Prosperity",
            explanationNp = if (pts >= 1.5) "आपसी आकर्षण, प्रभाव र सम्मान उत्कृष्ट रहनेछ।" else if (pts >= 1.0) "मध्यम आपसी आकर्षण र प्रभाव।" else "एक-अर्कामा नियन्त्रण गर्ने प्रयासले मतभेद हुन सक्छ।",
            explanationEn = if (pts >= 1.5) "Strong mutual attraction and mutual respect." else if (pts >= 1.0) "Moderate mutual influence and bonding." else "Possibility of dominance struggle in relationship."
        )
    }

    // 3. Tara (3 points max)
    private fun computeTara(bNak: Int, gNak: Int): KootaScore {
        val countGtoB = ((bNak - gNak + 27) % 27) + 1
        val countBtoG = ((gNak - bNak + 27) % 27) + 1
        val rem1 = countGtoB % 9
        val rem2 = countBtoG % 9

        val inauspicious = setOf(3, 5, 7) // Vipat, Pratyak, Naidhana
        val bBad = rem1 in inauspicious
        val gBad = rem2 in inauspicious

        val pts = when {
            !bBad && !gBad -> 3.0
            bBad != gBad -> 1.5
            else -> 0.0
        }

        val taraNamesNp = listOf("परम मित्र", "जन्म", "सम्पत्", "विपत्", "क्षेम", "प्रत्यक्", "साधक", "वध", "मित्र")
        val taraNamesEn = listOf("Parama Mitra", "Janma", "Sampat", "Vipat", "Kshema", "Pratyak", "Sadhaka", "Vadha", "Mitra")

        return KootaScore(
            key = "tara",
            nameNp = "तारा",
            nameEn = "Tara",
            maxPoints = 3.0,
            earnedPoints = pts,
            boyValueNp = taraNamesNp[rem1],
            boyValueEn = taraNamesEn[rem1],
            girlValueNp = taraNamesNp[rem2],
            girlValueEn = taraNamesEn[rem2],
            category = "Health",
            explanationNp = when (pts) {
                3.0 -> "भाग्य, दीर्घायु र स्वास्थ्यमा पूर्ण शुभ योग।"
                1.5 -> "सामान्य स्वास्थ्य अनुकूलता; मिश्रित प्रभाव।"
                else -> "तारा प्रतिकूल; एक-अर्काको स्वास्थ्यमा ध्यान दिनुपर्ने।"
            },
            explanationEn = when (pts) {
                3.0 -> "Highly auspicious for health, longevity, and destiny."
                1.5 -> "Moderate health and destiny compatibility."
                else -> "Challenging Tara; caution advised regarding health."
            }
        )
    }

    // 4. Yoni (4 points max)
    private fun computeYoni(bNak: Int, gNak: Int): KootaScore {
        val bY = YONI_MAP[bNak]
        val gY = YONI_MAP[gNak]

        val isEnemy = (bY to gY) in YONI_ENEMIES || (gY to bY) in YONI_ENEMIES
        val pts = when {
            bY == gY -> 4.0
            isEnemy -> 0.0
            abs(bY - gY) in listOf(1, 3, 5) -> 3.0
            abs(bY - gY) in listOf(2, 4) -> 2.0
            else -> 1.0
        }

        return KootaScore(
            key = "yoni",
            nameNp = "योनि",
            nameEn = "Yoni",
            maxPoints = 4.0,
            earnedPoints = pts,
            boyValueNp = YONI_NAMES_NP[bY],
            boyValueEn = YONI_NAMES_EN[bY],
            girlValueNp = YONI_NAMES_NP[gY],
            girlValueEn = YONI_NAMES_EN[gY],
            category = "Physical",
            explanationNp = when {
                pts == 4.0 -> "शारीरिक र जैविक अनुकूलता पूर्ण रूपमा उत्तम।"
                pts >= 2.0 -> "सन्तोषजनक शारीरिक तालमेल र समझदारी।"
                else -> "शत्रु योनि; आपसी आकर्षण र जैविक तालमेलमा कमी।"
            },
            explanationEn = when {
                pts == 4.0 -> "Flawless physical, biological, and intimacy harmony."
                pts >= 2.0 -> "Satisfactory physiological compatibility."
                else -> "Inimical Yonis; emotional adjustment needed in intimacy."
            }
        )
    }

    // 5. Graha Maitri (5 points max)
    private fun computeMaitri(bRashi: Int, gRashi: Int): KootaScore {
        val bLord = RASHI_LORD[bRashi]
        val gLord = RASHI_LORD[gRashi]

        val planetNamesNp = listOf("सूर्य", "चन्द्र", "मंगल", "बुध", "बृहस्पति", "शुक्र", "शनि")
        val planetNamesEn = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")

        val bRel = PLANET_FRIENDSHIP[bLord][gLord]
        val gRel = PLANET_FRIENDSHIP[gLord][bLord]

        val pts = when {
            bLord == gLord -> 5.0
            bRel == 2 && gRel == 2 -> 5.0
            (bRel == 2 && gRel == 1) || (bRel == 1 && gRel == 2) -> 4.0
            bRel == 1 && gRel == 1 -> 3.0
            (bRel == 2 && gRel == 0) || (bRel == 0 && gRel == 2) -> 1.0
            (bRel == 1 && gRel == 0) || (bRel == 0 && gRel == 1) -> 0.5
            else -> 0.0
        }

        return KootaScore(
            key = "maitri",
            nameNp = "ग्रह मैत्री",
            nameEn = "Graha Maitri",
            maxPoints = 5.0,
            earnedPoints = pts,
            boyValueNp = "${RASHIS_NP[bRashi]} (${planetNamesNp[bLord]})",
            boyValueEn = "${RASHIS_EN[bRashi]} (${planetNamesEn[bLord]})",
            girlValueNp = "${RASHIS_NP[gRashi]} (${planetNamesNp[gLord]})",
            girlValueEn = "${RASHIS_EN[gRashi]} (${planetNamesEn[gLord]})",
            category = "Mental",
            explanationNp = when {
                pts >= 4.0 -> "राशि स्वामीहरू बीच मित्रता; मानसिक सोच, विचार र मित्रता उत्कृष्ट।"
                pts >= 2.0 -> "सामान्य विचार मिलान; व्यावहारिक समझदारी रहने।"
                else -> "राशि स्वामी शत्रु; विचार र स्वभावमा असहमति आउन सक्छ।"
            },
            explanationEn = when {
                pts >= 4.0 -> "Friendly planetary lords; excellent mental wavelength and friendship."
                pts >= 2.0 -> "Moderate mental compatibility; daily cooperation possible."
                else -> "Inimical planetary lords; communication differences expected."
            }
        )
    }

    // 6. Gana (6 points max)
    private fun computeGana(bNak: Int, gNak: Int): KootaScore {
        val bG = GANA_MAP[bNak]
        val gG = GANA_MAP[gNak]

        // 0 Deva, 1 Manushya, 2 Rakshasa
        val (pts, hasDosha) = when {
            bG == gG -> 6.0 to false
            bG == 0 && gG == 1 -> 6.0 to false // Boy Deva, Girl Manushya
            bG == 1 && gG == 0 -> 5.0 to false // Boy Manushya, Girl Deva
            bG == 0 && gG == 2 -> 1.0 to true  // Boy Deva, Girl Rakshasa
            bG == 2 && gG == 0 -> 1.0 to true  // Boy Rakshasa, Girl Deva
            bG == 1 && gG == 2 -> 0.0 to true  // Boy Manushya, Girl Rakshasa
            bG == 2 && gG == 1 -> 0.0 to true  // Boy Rakshasa, Girl Manushya
            else -> 0.0 to true
        }

        return KootaScore(
            key = "gana",
            nameNp = "गण",
            nameEn = "Gana",
            maxPoints = 6.0,
            earnedPoints = pts,
            boyValueNp = GANA_NAMES_NP[bG],
            boyValueEn = GANA_NAMES_EN[bG],
            girlValueNp = GANA_NAMES_NP[gG],
            girlValueEn = GANA_NAMES_EN[gG],
            category = "Mental",
            hasDosha = hasDosha,
            doshaNameNp = if (hasDosha) "गण दोष" else null,
            doshaNameEn = if (hasDosha) "Gana Dosha" else null,
            explanationNp = when {
                pts >= 5.0 -> "स्वभाव र जीवनशैली पूर्ण रूपमा मेल खान्छ।"
                pts > 0.0 -> "स्वभावमा केही भिन्नता; सहनशीलता आवश्यक।"
                else -> "गण दोष उपस्थित; रिस र स्वभावमा ठूलो भिन्नता हुन सक्छ।"
            },
            explanationEn = when {
                pts >= 5.0 -> "Harmonious temperament and mutual understanding."
                pts > 0.0 -> "Moderate temperamental differences; patience required."
                else -> "Gana Dosha present; conflicting temperamental instincts."
            }
        )
    }

    // 7. Bhakoot (7 points max)
    private fun computeBhakoot(bRashi: Int, gRashi: Int): KootaScore {
        val dist = ((bRashi - gRashi + 12) % 12) + 1
        val bLord = RASHI_LORD[bRashi]
        val gLord = RASHI_LORD[gRashi]

        // 1/1, 3/11, 4/10, 7/7 are auspicious
        // 2/12, 6/8, 9/5 have Bhakoot Dosha UNLESS lords are same or mutual friends
        val isDoshaPosition = dist in listOf(2, 12, 6, 8, 5, 9)
        val cancellation = bLord == gLord || (PLANET_FRIENDSHIP[bLord][gLord] == 2 && PLANET_FRIENDSHIP[gLord][bLord] == 2)

        val (pts, hasDosha) = when {
            !isDoshaPosition -> 7.0 to false
            cancellation -> 7.0 to false // Bhakoot Dosha cancelled by lord friendship
            else -> 0.0 to true
        }

        val doshaDescNp = when (dist) {
            6, 8 -> "षडाष्टक (६/८) भकूट दोष"
            2, 12 -> "द्विर्द्वादश (२/१२) भकूट दोष"
            5, 9 -> "नवपञ्चम (५/९) भकूट दोष"
            else -> if (hasDosha) "भकूट दोष" else "शुभ भकूट"
        }

        val doshaDescEn = when (dist) {
            6, 8 -> "Shadashtak (6/8) Bhakoot Dosha"
            2, 12 -> "Dwidwadash (2/12) Bhakoot Dosha"
            5, 9 -> "Navapancham (5/9) Bhakoot Dosha"
            else -> if (hasDosha) "Bhakoot Dosha" else "Auspicious Bhakoot"
        }

        return KootaScore(
            key = "bhakoot",
            nameNp = "भकूट",
            nameEn = "Bhakoot",
            maxPoints = 7.0,
            earnedPoints = pts,
            boyValueNp = RASHIS_NP[bRashi],
            boyValueEn = RASHIS_EN[bRashi],
            girlValueNp = RASHIS_NP[gRashi],
            girlValueEn = RASHIS_EN[gRashi],
            category = "Prosperity",
            hasDosha = hasDosha,
            doshaNameNp = if (hasDosha) doshaDescNp else null,
            doshaNameEn = if (hasDosha) doshaDescEn else null,
            explanationNp = when {
                pts == 7.0 && cancellation && isDoshaPosition -> "भकूट दोष राशि स्वामीको मित्रताले परिहार (निष्प्रभावी) भएको छ।"
                pts == 7.0 -> "वैवाहिक सुख, प्रेम र पारिवारिक आर्थिक समृद्धिका लागि पूर्ण शुभ।"
                else -> "$doshaDescNp उपस्थित; पारिवारिक समृद्धि र समझदारीमा सचेत रहनुपर्ने।"
            },
            explanationEn = when {
                pts == 7.0 && cancellation && isDoshaPosition -> "Bhakoot Dosha cancelled due to friendship between sign lords."
                pts == 7.0 -> "Strong marital happiness, love, and long-term family prosperity."
                else -> "$doshaDescEn present; extra care needed for family harmony."
            }
        )
    }

    // 8. Nadi (8 points max - Most crucial)
    private fun computeNadi(bNak: Int, gNak: Int): KootaScore {
        val bN = NADI_MAP[bNak]
        val gN = NADI_MAP[gNak]

        // Different Nadi = 8 pts
        // Same Nadi = 0 pts (Nadi Dosha), with classical exception:
        // Same Nakshatra allowed if specific nakshatras or different charanas
        val isSameNak = bNak == gNak
        val isSameNadi = bN == gN
        val cancellation = isSameNak && (bNak in listOf(0, 3, 5, 6, 7, 9, 12, 14, 16))

        val (pts, hasDosha) = when {
            !isSameNadi -> 8.0 to false
            cancellation -> 8.0 to false // Cancelled
            else -> 0.0 to true // Nadi Dosha!
        }

        return KootaScore(
            key = "nadi",
            nameNp = "नाडी",
            nameEn = "Nadi",
            maxPoints = 8.0,
            earnedPoints = pts,
            boyValueNp = NADI_NAMES_NP[bN],
            boyValueEn = NADI_NAMES_EN[bN],
            girlValueNp = NADI_NAMES_NP[gN],
            girlValueEn = NADI_NAMES_EN[gN],
            category = "Health",
            hasDosha = hasDosha,
            doshaNameNp = if (hasDosha) "नाडी दोष" else null,
            doshaNameEn = if (hasDosha) "Nadi Dosha" else null,
            explanationNp = when {
                pts == 8.0 && cancellation -> "समान नक्षत्र भए तापनि शास्त्रीय नियम अनुसार नाडी दोष निष्प्रभावी।"
                pts == 8.0 -> "भिन्न नाडी; सन्तान सुख, स्वास्थ्य र जैविक अनुकूलता अत्यन्त उत्तम।"
                else -> "एउटै नाडी (नाडी दोष); स्वास्थ्य र सन्तान योजनामा सावधानी तथा ज्योतिषीय सल्लाह आवश्यक।"
            },
            explanationEn = when {
                pts == 8.0 && cancellation -> "Nadi Dosha cancelled per classical same-nakshatra exemption."
                pts == 8.0 -> "Different Nadis; exceptional genetic, biological, and progeny health."
                else -> "Same Nadi (Nadi Dosha); caution advised regarding progeny and genetic health."
            }
        )
    }
}
