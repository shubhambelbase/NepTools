package com.neptools.app.core.calendar

import java.time.LocalDate

/**
 * Dynamic Autonomous Nepali Festival & Holiday Engine.
 * Automatically computes authentic cultural, religious, and national festivals and public holidays
 * for ANY Bikram Sambat year (1975 BS to 2099 BS) using astronomical Panchang & BS Solar date calculation.
 * Zero manual updates required for future or past months/years.
 */
object DynamicFestivalEngine {

    /**
     * Computes all festivals and public holidays for a given BS Year and BS Month.
     */
    fun computeForMonth(
        engine: BsCalendarEngine,
        bsYear: Int,
        bsMonth: Int
    ): Map<Int, List<Festival>> {
        val totalDays = try {
            engine.monthLength(bsYear, bsMonth)
        } catch (e: Exception) {
            return emptyMap()
        }

        val result = HashMap<Int, List<Festival>>()

        for (day in 1..totalDays) {
            val npDate = NepaliDate(bsYear, bsMonth, day)
            val adDate = try {
                engine.bsToAd(npDate)
            } catch (e: Exception) {
                null
            } ?: continue

            val panchang = PanchangCalc.compute(adDate)
            val dayFestivals = computeForDay(bsYear, bsMonth, day, adDate, panchang)

            if (dayFestivals.isNotEmpty()) {
                result[day] = dayFestivals
            }
        }

        return result
    }

    /**
     * Determines festivals and holidays for an individual day based on solar date and astronomical Panchang.
     */
    fun computeForDay(
        bsYear: Int,
        bsMonth: Int,
        bsDay: Int,
        adDate: LocalDate,
        panchang: Panchang
    ): List<Festival> {
        val list = mutableListOf<Festival>()
        val tName = panchang.tithiName
        val isShukla = panchang.paksha == "शुक्ल पक्ष"
        val isKrishna = !isShukla

        // =====================================================================
        // 1. FIXED SOLAR / BS CALENDAR NATIONAL HOLIDAYS & OBSERVANCES
        // =====================================================================
        when (bsMonth) {
            1 -> { // बैशाख (Baisakh)
                if (bsDay == 1) {
                    list.add(Festival(bsDay, "नयाँ वर्ष", "Nepali New Year", true))
                } else if (bsDay == 11) {
                    list.add(Festival(bsDay, "लोकतन्त्र दिवस", "Loktantra Diwas (Democracy Day)", true))
                } else if (bsDay == 18) {
                    list.add(Festival(bsDay, "अन्तर्राष्ट्रिय मजदुर दिवस", "International Labour Day", true))
                }
            }
            2 -> { // जेठ (Jestha)
                if (bsDay == 15) {
                    list.add(Festival(bsDay, "गणतन्त्र दिवस", "Republic Day (Ganatantra Diwas)", true))
                }
            }
            3 -> { // असार (Ashadh)
                if (bsDay == 15) {
                    list.add(Festival(bsDay, "राष्ट्रिय धान दिवस (दही चिउरा खाने दिन)", "National Paddy Day (Ropain Diwas)", false))
                }
            }
            4 -> { // साउन (Shrawan)
                if (bsDay == 1) {
                    list.add(Festival(bsDay, "साउने सङ्क्रान्ति (लुतो फाल्ने दिन)", "Saune Sankranti", false))
                }
            }
            5 -> { // भदौ (Bhadra)
                if (bsDay == 22) {
                    list.add(Festival(bsDay, "निजामती सेवा दिवस", "Civil Service Day", false))
                }
            }
            6 -> { // असोज (Ashoj)
                if (bsDay == 3) {
                    list.add(Festival(bsDay, "राष्ट्रिय संविधान दिवस", "National Constitution Day", true))
                }
            }
            8 -> { // मंसिर (Mangsir)
                if (bsDay == 16) {
                    list.add(Festival(bsDay, "अन्तर्राष्ट्रिय अपाङ्गता दिवस", "International Disability Day", false))
                }
            }
            9 -> { // पुस (Poush)
                if (bsDay == 15) {
                    list.add(Festival(bsDay, "तमु ल्होसार", "Tamu Lhosar (Gurung New Year)", true))
                } else if (bsDay == 27) {
                    list.add(Festival(bsDay, "राष्ट्रिय एकता दिवस / पृथ्वी जयन्ती", "Prithvi Jayanti / National Unity Day", true))
                }
            }
            10 -> { // माघ (Magh)
                if (bsDay == 1) {
                    list.add(Festival(bsDay, "माघे सङ्क्रान्ति / माघी पर्व", "Maghe Sankranti / Maghi", true))
                } else if (bsDay == 16) {
                    list.add(Festival(bsDay, "सहिद दिवस", "Martyrs' Day (Sahid Diwas)", false))
                }
            }
            11 -> { // फागुन (Falgun)
                if (bsDay == 7) {
                    list.add(Festival(bsDay, "राष्ट्रिय प्रजातन्त्र दिवस", "National Democracy Day", true))
                } else if (bsDay == 24) {
                    list.add(Festival(bsDay, "अन्तर्राष्ट्रिय नारी दिवस", "International Women's Day", true))
                }
            }
        }

        // =====================================================================
        // 2. ASTRONOMICAL / TITHI-BASED RELIGIOUS & CULTURAL FESTIVALS
        // =====================================================================
        when (panchang.lunarMasaIndex) {
            0 -> { // वैशाख (Vaishakha)
                if (isShukla && tName == "तृतीया") {
                    list.add(Festival(bsDay, "अक्षय तृतीया", "Akshaya Tritiya", false))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "बुद्ध जयन्ती तथा उभौली पर्व", "Buddha Jayanti & Ubhauli", true))
                } else if (isKrishna && tName == "औंसी") {
                    list.add(Festival(bsDay, "मातातीर्थ औंसी (आमाको मुख हेर्ने दिन)", "Mother's Day (Matatirtha Aunsi)", false))
                }
            }
            1 -> { // ज्येष्ठ (Jyeshtha)
                if (isShukla && tName == "दशमी") {
                    list.add(Festival(bsDay, "गङ्गा दशहरा", "Ganga Dussehra", false))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "ज्येष्ठ पूर्णिमा / पनौती जात्रा", "Jyeshtha Purnima", false))
                }
            }
            2 -> { // आषाढ (Ashadha)
                if (isShukla && tName == "एकादशी") {
                    list.add(Festival(bsDay, "हरिशयनी एकादशी (तुलसी रोप्ने दिन)", "Harishayani Ekadashi", false))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "गुरु पूर्णिमा / ब्यास जयन्ती", "Guru Purnima / Vyas Jayanti", false))
                }
            }
            3 -> { // श्रावण (Shravana)
                if (isShukla && tName == "पञ्चमी") {
                    list.add(Festival(bsDay, "नाग पञ्चमी", "Nag Panchami", false))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "जनै पूर्णिमा / रक्षाबन्धन / क्वाँटी खाने दिन", "Janai Purnima / Raksha Bandhan", true))
                }
            }
            4 -> { // भाद्रपद (Bhadrapada)
                if (isKrishna && tName == "प्रतिपदा") {
                    list.add(Festival(bsDay, "गाईजात्रा", "Gai Jatra", true))
                } else if (isKrishna && tName == "अष्टमी") {
                    list.add(Festival(bsDay, "श्रीकृष्ण जन्माष्टमी", "Krishna Janmashtami", true))
                } else if (isKrishna && tName == "औंसी") {
                    list.add(Festival(bsDay, "कुशे औंसी (बाबुको मुख हेर्ने दिन)", "Father's Day (Kushe Aunsi)", false))
                } else if (isShukla && tName == "तृतीया") {
                    list.add(Festival(bsDay, "हरितालिका तीज", "Hartalika Teej", true))
                } else if (isShukla && tName == "चतुर्थी") {
                    list.add(Festival(bsDay, "गणेश चतुर्थी", "Ganesh Chaturthi", false))
                } else if (isShukla && tName == "पञ्चमी") {
                    list.add(Festival(bsDay, "ऋषि पञ्चमी", "Rishi Panchami", false))
                } else if (isShukla && tName == "चतुर्दशी") {
                    list.add(Festival(bsDay, "इन्द्रजात्रा", "Indra Jatra", true))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "अनन्त चतुर्दशी / सोह्र श्राद्ध प्रारम्भ", "Sohra Shraddha Begins", false))
                }
            }
            5 -> { // आश्विन (Ashvina) - बडा दशैं (Bada Dashain)
                if (isKrishna && tName == "औंसी") {
                    list.add(Festival(bsDay, "सोह्र श्राद्ध समापन (महालया)", "Mahalaya (Sohra Shraddha End)", false))
                } else if (isShukla && tName == "प्रतिपदा") {
                    list.add(Festival(bsDay, "घटस्थापना (बडा दशैं प्रारम्भ)", "Ghatasthapana (Dashain Begins)", true))
                } else if (isShukla && tName == "सप्तमी") {
                    list.add(Festival(bsDay, "फूलपाती (दशैं)", "Phulpati (Dashain Day 7)", true))
                } else if (isShukla && tName == "अष्टमी") {
                    list.add(Festival(bsDay, "महाअष्टमी (कालरात्रि)", "Maha Ashtami (Dashain Day 8)", true))
                } else if (isShukla && tName == "नवमी") {
                    list.add(Festival(bsDay, "महानवमी (दशैं)", "Maha Navami (Dashain Day 9)", true))
                } else if (isShukla && tName == "दशमी") {
                    list.add(Festival(bsDay, "विजया दशमी (दशैंको मुख्य टीका)", "Vijaya Dashami (Main Tika)", true))
                } else if (isShukla && tName == "एकादशी") {
                    list.add(Festival(bsDay, "पापाङ्कुशा एकादशी (दशैं टीका)", "Dashain Tika Day 2", true))
                } else if (isShukla && tName == "द्वादशी") {
                    list.add(Festival(bsDay, "दशैं टीका", "Dashain Tika Day 3", true))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "कोजाग्रत पूर्णिमा (दशैं समापन)", "Kojagrat Purnima (Dashain Ends)", true))
                }
            }
            6 -> { // कार्तिक (Kartika) - यमपञ्चक / तिहार र छठ (Tihar & Chhath)
                if (isKrishna && tName == "त्रयोदशी") {
                    list.add(Festival(bsDay, "काग तिहार तथा धनतेरस", "Kag Tihar & Dhanteras", false))
                } else if (isKrishna && tName == "चतुर्दशी") {
                    list.add(Festival(bsDay, "कुकुर तिहार तथा नरक चतुर्दशी", "Kukur Tihar", true))
                } else if (isKrishna && tName == "औंसी") {
                    list.add(Festival(bsDay, "लक्ष्मी पूजा (दीपावली)", "Laxmi Puja (Deepawali)", true))
                } else if (isShukla && tName == "प्रतिपदा") {
                    list.add(Festival(bsDay, "गोवर्धन पूजा / म्ह: पूजा / नेपाल संवत् नयाँ वर्ष", "Govardhan Puja / Mha Puja / Nepal Sambat", true))
                } else if (isShukla && tName == "द्वितीया") {
                    list.add(Festival(bsDay, "भाइटीका (तिहारको मुख्य दिन)", "Bhai Tika (Main Tihar)", true))
                } else if (isShukla && tName == "षष्ठी") {
                    list.add(Festival(bsDay, "छठ पर्व (सूर्य पूजा)", "Chhath Puja (Sun Festival)", true))
                } else if (isShukla && tName == "एकादशी") {
                    list.add(Festival(bsDay, "हरिबोधिनी एकादशी (तुलसी विवाह)", "Haribodhini Ekadashi", false))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "कार्तिक पूर्णिमा / गुरु नानक जयन्ती", "Kartik Purnima", false))
                }
            }
            7 -> { // मार्गशीर्ष (Margashirsha)
                if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "उधौली पर्व / योमरी पुन्ही / ज्यापू दिवस", "Udhauli Parva & Yomari Punhi", true))
                } else if (isShukla && tName == "एकादशी") {
                    list.add(Festival(bsDay, "मोक्षदा एकादशी / गीता जयन्ती", "Mokshada Ekadashi / Gita Jayanti", false))
                }
            }
            8 -> { // पौष (Pausha)
                if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "श्री स्वस्थानी व्रत प्रारम्भ / पूर्णिमा व्रत", "Swasthani Brata Begins", false))
                }
            }
            9 -> { // माघ (Magha)
                if (isShukla && tName == "पञ्चमी") {
                    list.add(Festival(bsDay, "वसन्त पञ्चमी / श्रीपञ्चमी (सरस्वती पूजा)", "Saraswati Puja / Basanta Panchami", false))
                } else if (isShukla && tName == "प्रतिपदा") {
                    list.add(Festival(bsDay, "सोनाम ल्होसार", "Sonam Lhosar (Tamang New Year)", true))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "माघ पूर्णिमा", "Magh Purnima", false))
                }
            }
            10 -> { // फाल्गुन (Phalguna)
                if (isKrishna && tName == "चतुर्दशी") {
                    list.add(Festival(bsDay, "महाशिवरात्रि", "Maha Shivaratri", true))
                } else if (isShukla && tName == "प्रतिपदा") {
                    list.add(Festival(bsDay, "ग्याल्पो ल्होसार", "Gyalpo Lhosar (Sherpa New Year)", true))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "फागु पूर्णिमा / होली", "Holi Festival (Fagu Purnima)", true))
                }
            }
            11 -> { // चैत्र (Chaitra)
                if (isKrishna && tName == "औंसी") {
                    list.add(Festival(bsDay, "घोडेजात्रा", "Ghode Jatra", true))
                } else if (isShukla && tName == "अष्टमी") {
                    list.add(Festival(bsDay, "चैते दशैं", "Chaite Dashain", false))
                } else if (isShukla && tName == "नवमी") {
                    list.add(Festival(bsDay, "रामनवमी", "Ram Navami", true))
                } else if (isShukla && tName == "पूर्णिमा") {
                    list.add(Festival(bsDay, "चैते पूर्णिमा / स्वस्थानी व्रत समापन", "Swasthani Brata Concludes", false))
                }
            }
        }

        // Monthly Ekadashi / Purnima / Aunsi fast markers if no other major festival exists on that day
        if (list.isEmpty()) {
            if (tName == "एकादशी") {
                list.add(Festival(bsDay, "एकादशी व्रत", "Ekadashi Vrata", false))
            } else if (tName == "पूर्णिमा") {
                list.add(Festival(bsDay, "पूर्णिमा व्रत", "Purnima Vrata", false))
            } else if (tName == "औंसी") {
                list.add(Festival(bsDay, "दर्श श्राद्ध / औंसी", "Aunsi (Amavasya)", false))
            }
        }

        return list
    }
}
