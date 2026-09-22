package com.neptools.app.core.calendar

data class SacredTithiInfo(
    val canonicalNameNp: String,
    val canonicalNameEn: String,
    val significanceNp: String,
    val significanceEn: String,
    val isMajor: Boolean = false
)

object SacredTithiResolver {

    fun resolveEkadashi(lunarMasaIndex: Int, isShukla: Boolean): SacredTithiInfo {
        return if (isShukla) {
            when (lunarMasaIndex) {
                0 -> SacredTithiInfo("मोहिनी एकादशी", "Mohini Ekadashi", "मोहादि सांसारिक बन्धनबाट मुक्ति र सद्गति", "Liberation from illusions & attachments")
                1 -> SacredTithiInfo("निर्जला एकादशी", "Nirjala Ekadashi", "भीमसेनी एकादशी • जल विना कठोर व्रत • तुलसीको बीउ छर्ने दिन", "Waterless fast • Tulsi seed sowing day", true)
                2 -> SacredTithiInfo("हरिशयनी एकादशी", "Harishayani Ekadashi", "चतुर्मास प्रारम्भ • भगवान विष्णुको शयन • तुलसी रोप्ने दिन", "Chaturmas commences • Tulsi plantation day", true)
                3 -> SacredTithiInfo("पुत्रदा (पवित्रा) एकादशी", "Putrada (Pavitra) Ekadashi", "सन्तान सुख, शान्ति र आत्मिक पवित्रता", "Blessings of offspring & spiritual purity")
                4 -> SacredTithiInfo("परिवर्तिनी एकादशी", "Hariparivartini Ekadashi", "वामन एकादशी • भगवान विष्णुले कोल्टे फेर्ने पवित्र तिथि", "Lord Vishnu shifts sleeping side • Vamana Jayanti")
                5 -> SacredTithiInfo("पापाङ्कुशा एकादशी", "Papankusha Ekadashi", "पापमाथि अङ्कुश लगाउने एवं सद्बुद्धि प्राप्ति", "Cleansing of transgressions & righteousness")
                6 -> SacredTithiInfo("हरिबोधिनी एकादशी", "Haribodhini Ekadashi", "ठूलो एकादशी • चतुर्मास व्रत समापन • तुलसी विवाह", "Lord Vishnu awakens • Tulsi Vivah", true)
                7 -> SacredTithiInfo("मोक्षदा एकादशी", "Mokshada Ekadashi", "श्रीमद्भगवद्गीता जयन्ती • परम मोक्ष प्राप्ति", "Gita Jayanti • Path to eternal liberation", true)
                8 -> SacredTithiInfo("पुत्रदा (बैकुण्ठ) एकादशी", "Putrada (Vaikuntha) Ekadashi", "पुत्रदा एकादशी • बैकुण्ठ लोक प्राप्ति", "Ascension to Vaikuntha & progeny blessings")
                9 -> SacredTithiInfo("जया (भैमी) एकादशी", "Jaya (Bhaimi) Ekadashi", "अधोगतिबाट मुक्ति एवं जीवनका सङ्घर्षमा विजय", "Liberation from lower realms & triumph")
                10 -> SacredTithiInfo("आमलकी एकादशी", "Amalaki Ekadashi", "पवित्र आँवला वृक्षको पूजा • आरोग्य एवं समृद्धि", "Veneration of sacred Amla tree & health")
                11 -> SacredTithiInfo("कामदा एकादशी", "Kamada Ekadashi", "नव संवत्सरको पहिलो एकादशी • मनोकामना पूर्ण", "Fulfillment of righteous desires")
                else -> SacredTithiInfo("एकादशी व्रत", "Ekadashi Vrata", "पवित्र एकादशी व्रत एवं विष्णु आराधना", "Sacred fast & devotion to Lord Vishnu")
            }
        } else {
            when (lunarMasaIndex) {
                0 -> SacredTithiInfo("वरुथिनी एकादशी", "Varuthini Ekadashi", "अपार पुण्य फल, सुख-सौभाग्य एवं ऐश्वर्य प्राप्ति", "Virtue, prosperity and boundless fortunes")
                1 -> SacredTithiInfo("अपरा एकादशी", "Apara Ekadashi", "अपार कीर्ति प्राप्ति एवं समस्त पाप निवारण", "Dispels misdeeds and grants spiritual fame")
                2 -> SacredTithiInfo("योगिनी एकादशी", "Yogini Ekadashi", "रोग-व्याधिबाट मुक्ति एवं आध्यात्मिक शुद्धि", "Alleviates ailments and purifies karmas")
                3 -> SacredTithiInfo("कामिका एकादशी", "Kamika Ekadashi", "पितृ उद्धार एवं समस्त अभीष्ट सिद्धि", "Ancestral peace & wish fulfillment")
                4 -> SacredTithiInfo("अजा एकादशी", "Aja Ekadashi", "पूर्वजन्मका पापकर्म निवारण एवं सुख-शान्ति", "Pardon from past karma and inner peace")
                5 -> SacredTithiInfo("इन्दिरा एकादशी", "Indira Ekadashi", "पितृपक्ष/श्राद्ध कालमा पितृ मोक्ष प्राप्ति", "Ancestral deliverance during Pitri Paksha", true)
                6 -> SacredTithiInfo("रमा एकादशी", "Rama Ekadashi", "महालक्ष्मी कृपा, धन-धान्य एवं ऐश्वर्य वृद्धि", "Blessings of Goddess Lakshmi & abundance")
                7 -> SacredTithiInfo("उत्पन्ना एकादशी", "Utpanna Ekadashi", "एकादशी माताको प्राकट्य • व्रत परम्परा प्रारम्भ", "Appearance of Goddess Ekadashi")
                8 -> SacredTithiInfo("सफला एकादशी", "Saphala Ekadashi", "सबै सत्कर्म सफल हुने एवं सिद्धि प्राप्ति", "Success in noble pursuits & accomplishments")
                9 -> SacredTithiInfo("षट्तिला एकादशी", "Shattila Ekadashi", "तिलको ६ प्रकारले प्रयोग एवं दान • आरोग्यता", "Offering of sesame in 6 ways & health")
                10 -> SacredTithiInfo("विजया एकादशी", "Vijaya Ekadashi", "कठिन परिस्थिति तथा शत्रु बाधामाथि विजय", "Victory over adversities & obstacles")
                11 -> SacredTithiInfo("पापमोचिनी एकादशी", "Papamochani Ekadashi", "पाप-ताप विमोचन एवं आत्मिक शान्ति", "Cleansing of negative influences")
                else -> SacredTithiInfo("एकादशी व्रत", "Ekadashi Vrata", "पवित्र एकादशी व्रत एवं विष्णु आराधना", "Sacred fast & devotion to Lord Vishnu")
            }
        }
    }

    fun resolveAunsi(lunarMasaIndex: Int): SacredTithiInfo {
        return when (lunarMasaIndex) {
            0 -> SacredTithiInfo("मातातीर्थ औंसी", "Matatirtha Aunsi", "आमाको मुख हेर्ने दिन • मातृ सम्मान", "Nepali Mother's Day • Honoring mothers", true)
            4 -> SacredTithiInfo("कुशे औंसी / गोकर्णे औंसी", "Kushe Aunsi / Gokarna Aunsi", "बुबाको मुख हेर्ने दिन • कुश संकलन", "Nepali Father's Day • Kushe Aunsi", true)
            5 -> SacredTithiInfo("सोह्र श्राद्ध औंसी (सर्वपितृ मोक्ष)", "Mahalaya Amavasya", "पितृ विसर्जन एवं सोह्र श्राद्ध समापन", "Final day of Sorha Shraddha / Mahalaya", true)
            6 -> SacredTithiInfo("दर्श औंसी / लक्ष्मीपूजा", "Lakshmi Puja / Gai Tihar", "तिहारको औंसी • महालक्ष्मी पूजा एवं सुखरात्री", "Lakshmi Puja / Festival of Lights", true)
            9 -> SacredTithiInfo("मौनी औंसी", "Mauni Amavasya", "मौन व्रत, आत्मसंयम एवं पवित्र नदी स्नान", "Silent meditation & sacred river ablution")
            11 -> SacredTithiInfo("घोडेजात्रा औंसी", "Ghode Jatra Aunsi", "काठमाडौं उपत्यकामा घोडेजात्रा पर्व", "Ghode Jatra Festival in Kathmandu", true)
            else -> SacredTithiInfo("दर्श श्राद्ध / औंसी", "Amavasya (Aunsi)", "पितृ तर्पण, दर्श श्राद्ध एवं मौन चिन्तन", "Ancestral remembrance & Amavasya oblations")
        }
    }

    fun resolvePurnima(lunarMasaIndex: Int): SacredTithiInfo {
        return when (lunarMasaIndex) {
            0 -> SacredTithiInfo("बुद्ध जयन्ती तथा उभौली पूर्णिमा", "Buddha Jayanti & Ubhauli", "भगवान बुद्ध प्राकट्य एवं किराँत उभौली पर्व", "Buddha Jayanti & Kirat Ubhauli Festival", true)
            1 -> SacredTithiInfo("ज्येष्ठ पूर्णिमा / पनौती पुन्ही", "Jyeshtha Purnima", "वट सावित्री व्रत एवं पनौती पुन्ही", "Vat Savitri Vrata & Panauti Punhi")
            2 -> SacredTithiInfo("गुरु पूर्णिमा / व्यास जयन्ती", "Guru Purnima / Vyasa Jayanti", "गुरुप्रति श्रद्धा-आदर समर्पण एवं व्यास पूजा", "Reverence to spiritual guides & teachers", true)
            3 -> SacredTithiInfo("जनै पूर्णिमा / रक्षाबन्धन", "Janai Purnima & Rakshabandhan", "पवित्र डोरो-धागो धारण, क्वाँटी खाने दिन", "Sacred thread ceremony & Rakshabandhan", true)
            4 -> SacredTithiInfo("भाद्र पूर्णिमा / इन्द्रदह स्नान", "Bhadra Purnima", "इन्द्रजात्रा उत्सव समापन एवं पवित्र स्नान", "Indra Jatra concluding celebrations")
            5 -> SacredTithiInfo("कोजाग्रत पूर्णिमा", "Kojagrat Purnima", "दशैं पर्वको समापन एवं जाग्राम पूजा", "Conclusion of Dashain & Lakshmi Jagran", true)
            6 -> SacredTithiInfo("कार्तिक पूर्णिमा / गुरु नानक जयन्ती", "Kartik Purnima", "दामोदर पूजा एवं त्रिपुरारी पूर्णिमा", "Tripurari Purnima & sacred dip")
            7 -> SacredTithiInfo("धान्य पूर्णिमा / योमरी पुन्ही / उधौली", "Yomari Punhi & Udhauli", "नेवार समुदायको योमरी पुन्ही एवं किराँत उधौली", "Harvest celebration, Yomari & Udhauli", true)
            8 -> SacredTithiInfo("पौष पूर्णिमा / स्वस्थानी प्रारम्भ", "Paush Purnima (Swasthani Begins)", "श्री स्वस्थानी व्रत कथा प्रारम्भ", "Sri Swasthani Vrata begins", true)
            9 -> SacredTithiInfo("माघ पूर्णिमा / स्वस्थानी समापन", "Magh Purnima (Swasthani Concludes)", "श्री स्वस्थानी व्रत साङ्गे एवं माघ स्नान", "Sri Swasthani Vrata concludes", true)
            10 -> SacredTithiInfo("फागु पूर्णिमा / होली", "Holi (Fagu Purnima)", "रङहरूको पर्व होली (पहाडी भेग)", "Festival of Colours - Holi", true)
            11 -> SacredTithiInfo("चैते पूर्णिमा / ल्हुति पुन्ही", "Chaitra Purnima (Balaju Snan)", "बालाजु बाइसधारा स्नान एवं चैते पूर्णिमा", "Balaju Baise Dhara sacred festival")
            else -> SacredTithiInfo("पूर्णिमा व्रत", "Purnima Vrata", "सत्यनारायण पूजा एवं चन्द्र दर्शन", "Satyanarayan puja & full moon worship")
        }
    }
}
