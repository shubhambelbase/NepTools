package com.neptools.app.core.vastu

import androidx.compose.runtime.Immutable

@Immutable
data class VastuDirection(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val zoneNp: String,
    val zoneEn: String,
    val deityNp: String,
    val deityEn: String,
    val elementNp: String,
    val elementEn: String,
    val centerAngle: Float,
    val minAngle: Float,
    val maxAngle: Float,
    val bestRoomsNp: List<String>,
    val bestRoomsEn: List<String>,
    val avoidRoomsNp: List<String>,
    val avoidRoomsEn: List<String>,
    val adviceNp: String,
    val adviceEn: String
)

@Immutable
data class VastuRoomRule(
    val id: String,
    val roomNp: String,
    val roomEn: String,
    val bestDirectionNp: String,
    val bestDirectionEn: String,
    val alternativeDirectionNp: String,
    val alternativeDirectionEn: String,
    val strictlyAvoidNp: String,
    val strictlyAvoidEn: String,
    val guidelineNp: String,
    val guidelineEn: String
)

object VastuEngine {

    val directions: List<VastuDirection> = listOf(
        VastuDirection(
            id = "north",
            nameNp = "उत्तर (कुबेर)",
            nameEn = "North (Kubera)",
            zoneNp = "उत्तर दिशा",
            zoneEn = "North Zone",
            deityNp = "कुबेर (धनका देवता)",
            deityEn = "Lord Kubera (God of Wealth)",
            elementNp = "जल / आकाश तत्व",
            elementEn = "Water / Ether Element",
            centerAngle = 0f,
            minAngle = 337.5f,
            maxAngle = 22.5f,
            bestRoomsNp = listOf("धन / सेफ / तिजोरी", "बैठक कोठा (लिभिङ रुम)", "अध्ययन टेबल (उत्तर फर्केर पढ्ने)", "खुला बरण्डा / बगैंचा"),
            bestRoomsEn = listOf("Cash Locker / Safe", "Living Room", "Study Desk (Facing North)", "Open Balcony / Garden"),
            avoidRoomsNp = listOf("शौचालय / सेप्टिक ट्याङ्क", "भान्सा कोठा", "गह्रौं भण्डार (स्टोर)", "मास्टर बेडरुम"),
            avoidRoomsEn = listOf("Toilet / Septic Tank", "Kitchen", "Heavy Storage", "Master Bedroom"),
            adviceNp = "उत्तर दिशा धन र अवसरको प्रवेशद्वार हो। यो भाग जहिले पनि सफा, उज्यालो र हलुका राख्नुपर्छ।",
            adviceEn = "North represents wealth and opportunities. Keep this sector clean, illuminated, and clutter-free."
        ),
        VastuDirection(
            id = "northeast",
            nameNp = "ईशान (शिव)",
            nameEn = "North-East (Ishanya)",
            zoneNp = "ईशान कोण",
            zoneEn = "North-East Zone",
            deityNp = "भगवान शिव / ईशान",
            deityEn = "Lord Shiva / Ishana",
            elementNp = "जल तत्व (पवित्र ऊर्जा)",
            elementEn = "Water Element (Divine Energy)",
            centerAngle = 45f,
            minAngle = 22.5f,
            maxAngle = 67.5f,
            bestRoomsNp = listOf("पूजा कोठा / मन्दिर (सर्वश्रेष्ठ)", "ध्यान तथा योग कक्ष", "भूमिगत पानी ट्याङ्की / इनार", "मुख्य प्रवेशद्वार (सिंहद्वार)"),
            bestRoomsEn = listOf("Pooja / Prayer Room (Best)", "Meditation & Yoga Room", "Underground Water Tank / Well", "Main Entrance"),
            avoidRoomsNp = listOf("शौचालय (महावास्तु दोष)", "भान्सा कोठा (अग्नि र जलको द्वन्द्व)", "मास्टर बेडरुम", "गह्रौं भर्याङ"),
            avoidRoomsEn = listOf("Toilet (Severe Vastu Dosha)", "Kitchen (Fire & Water Conflict)", "Master Bedroom", "Heavy Staircase"),
            adviceNp = "ईशान कोण वास्तु पुरुषको शिर मानिन्छ। यस भागमा शौचालय वा फोहोर भए परिवारमा मानसिक अशान्ति र स्वास्थ्य समस्या आउन सक्छ।",
            adviceEn = "North-East is the head of Vastu Purusha. Never place toilets or heavy scrap here; maintain utmost purity and lightness."
        ),
        VastuDirection(
            id = "east",
            nameNp = "पूर्व (इन्द्र / सूर्य)",
            nameEn = "East (Indra / Surya)",
            zoneNp = "पूर्व दिशा",
            zoneEn = "East Zone",
            deityNp = "इन्द्र / सूर्यदेव",
            deityEn = "Lord Indra / Sun God",
            elementNp = "अग्नि / वायु तत्व",
            elementEn = "Fire / Air Element",
            centerAngle = 90f,
            minAngle = 67.5f,
            maxAngle = 112.5f,
            bestRoomsNp = listOf("मुख्य ढोका (सिंहद्वार)", "अध्ययन कोठा", "बैठक कोठा", "बिहानी घाम छिर्ने ठूला झ्यालहरू"),
            bestRoomsEn = listOf("Main Entrance (Singhdwar)", "Study Room", "Living Room", "Large Windows for Morning Sun"),
            avoidRoomsNp = listOf("शौचालय", "अध्यारो स्टोर रुम", "गह्रौं पर्खाल वा अग्लो निर्माण"),
            avoidRoomsEn = listOf("Toilet", "Dark Store Room", "Heavy Walls / Overhanging Structures"),
            adviceNp = "पूर्व दिशाबाट सकारात्मक सौर्य ऊर्जा घरभित्र प्रवेश गर्छ। यो भाग खुला र स्वच्छ हुनुपर्छ।",
            adviceEn = "East brings solar energy, health, and vitality into the home. Keep it welcoming and bright."
        ),
        VastuDirection(
            id = "southeast",
            nameNp = "आग्नेय (अग्नि)",
            nameEn = "South-East (Agni)",
            zoneNp = "आग्नेय कोण",
            zoneEn = "South-East Zone",
            deityNp = "अग्निदेव",
            deityEn = "Lord Agni (Fire God)",
            elementNp = "अग्नि तत्व",
            elementEn = "Fire Element",
            centerAngle = 135f,
            minAngle = 112.5f,
            maxAngle = 157.5f,
            bestRoomsNp = listOf("भान्सा कोठा (सर्वश्रेष्ठ स्थान)", "विद्युत मिटर / ट्रान्सफर्मर", "इन्भर्टर / ब्याट्री / जेनेरेटर", "ग्यास सिलिन्डर / गिजर"),
            bestRoomsEn = listOf("Kitchen (Best Location)", "Electric Meter Board", "Inverter / Battery / Generator", "Gas Cylinder / Geyser"),
            avoidRoomsNp = listOf("पूजा कोठा", "भूमिगत पानी ट्याङ्की", "मास्टर बेडरुम", "शौचालय / सेप्टिक ट्याङ्क"),
            avoidRoomsEn = listOf("Pooja Room", "Underground Water Tank", "Master Bedroom", "Toilet / Septic Tank"),
            adviceNp = "आग्नेय कोण अग्निको वासस्थान हो। यहाँ भान्सा बनाउँदा खाना पकाउने व्यक्तिको मुख पूर्व फर्केको हुनु शुभ मानिन्छ।",
            adviceEn = "South-East is the home of Fire. Place cooking stove here, ensuring the cook faces East while preparing meals."
        ),
        VastuDirection(
            id = "south",
            nameNp = "दक्षिण (यम)",
            nameEn = "South (Yama)",
            zoneNp = "दक्षिण दिशा",
            zoneEn = "South Zone",
            deityNp = "यमराज",
            deityEn = "Lord Yama",
            elementNp = "पृथ्वी तत्व",
            elementEn = "Earth Element",
            centerAngle = 180f,
            minAngle = 157.5f,
            maxAngle = 202.5f,
            bestRoomsNp = listOf("शयनकक्ष (दक्षिण तर्फ सिरानी गरेर सुत्ने)", "गह्रौं सामान राख्ने स्टोर", "भर्याङ (Staircase)", "डाइनिङ रुम"),
            bestRoomsEn = listOf("Bedroom (Head towards South)", "Heavy Storage / Granary", "Staircase", "Dining Room"),
            avoidRoomsNp = listOf("पूजा कोठा", "भूमिगत पानी ट्याङ्की", "मुख्य प्रवेशद्वार (अशुभ पद)", "खुला ठूलो बरण्डा"),
            avoidRoomsEn = listOf("Pooja Room", "Underground Water Tank", "Main Entrance (Inauspicious Pada)", "Open Low Terrace"),
            adviceNp = "दक्षिण दिशा स्थिर र भारी हुनुपर्छ। यहाँ सुत्दा सिरानी दक्षिण तर्फ राख्दा गहिरो निन्द्रा र दीर्घायु प्राप्त हुन्छ।",
            adviceEn = "South represents stability and rest. Sleeping with head towards South aligns with Earth's magnetic field for restful sleep."
        ),
        VastuDirection(
            id = "southwest",
            nameNp = "नैऋत्य (नैऋत)",
            nameEn = "South-West (Nairritya)",
            zoneNp = "नैऋत्य कोण",
            zoneEn = "South-West Zone",
            deityNp = "नैऋत (पृथ्वी / स्थिरताका अधिपति)",
            deityEn = "Nirriti (Lord of Stability & Earth)",
            elementNp = "पृथ्वी तत्व (सबैभन्दा भारी र अग्लो)",
            elementEn = "Earth Element (Heaviest & Highest)",
            centerAngle = 225f,
            minAngle = 202.5f,
            maxAngle = 247.5f,
            bestRoomsNp = listOf("मुख्य घरमूलीको शयनकक्ष (Master Bed)", "गह्रौं दराज / दराजको ढोका उत्तर खुल्ने", "माथिल्लो तलाको ओभरहेड पानी ट्याङ्की", "गह्रौं निर्माण"),
            bestRoomsEn = listOf("Master Bedroom (Head of Family)", "Heavy Wardrobes / Safe Opening North", "Overhead Water Tank (On Roof)", "Highest Structure"),
            avoidRoomsNp = listOf("पूजा कोठा", "भान्सा कोठा", "भूमिगत पानी ट्याङ्की / इनार (महादोष)", "मुख्य प्रवेशद्वार", "सेप्टिक ट्याङ्क"),
            avoidRoomsEn = listOf("Pooja Room", "Kitchen", "Underground Water / Well (Severe Defect)", "Main Gate", "Septic Tank"),
            adviceNp = "नैऋत्य कोण घरको आधार हो। यो कुना सबैभन्दा अग्लो, भारी र ठोस हुनुपर्छ। यहाँ खाडल वा इनार खन्नु हुँदैन।",
            adviceEn = "South-West provides leadership and grounding. It should be the heaviest, highest, and most solid zone in the building."
        ),
        VastuDirection(
            id = "west",
            nameNp = "पश्चिम (वरुण)",
            nameEn = "West (Varuna)",
            zoneNp = "पश्चिम दिशा",
            zoneEn = "West Zone",
            deityNp = "वरुणदेव (जलका अधिपति)",
            deityEn = "Lord Varuna (Lord of Waters)",
            elementNp = "वायु / जल तत्व",
            elementEn = "Air / Water Element",
            centerAngle = 270f,
            minAngle = 247.5f,
            maxAngle = 292.5f,
            bestRoomsNp = listOf("डाइनिङ रुम (भोजन कक्ष)", "बालबालिकाको अध्ययन तथा सुत्ने कोठा", "छतको ओभरहेड पानी ट्याङ्की", "शौचालय"),
            bestRoomsEn = listOf("Dining Room", "Children's Bedroom / Study", "Overhead Water Tank", "Toilet"),
            avoidRoomsNp = listOf("पूजा कोठा", "भूमिगत पानीको इनार", "घरको मुख्य ढोका (केही पद बाहेक)"),
            avoidRoomsEn = listOf("Pooja Room", "Underground Well", "Main Entrance (Except specific Pada)"),
            adviceNp = "पश्चिम दिशा लाभ र समृद्धिसँग सम्बन्धित छ। यहाँ भोजन कक्ष वा बच्चाहरूको अध्ययन कोठा उपयुक्त मानिन्छ।",
            adviceEn = "West governs profits and fulfillment. It is well-suited for dining rooms and study areas."
        ),
        VastuDirection(
            id = "northwest",
            nameNp = "वायव्य (वायु)",
            nameEn = "North-West (Vayavya)",
            zoneNp = "वायव्य कोण",
            zoneEn = "North-West Zone",
            deityNp = "वायुदेव",
            deityEn = "Lord Vayu (Wind God)",
            elementNp = "वायु तत्व (गतिशीलता)",
            elementEn = "Air Element (Movement)",
            centerAngle = 315f,
            minAngle = 292.5f,
            maxAngle = 337.5f,
            bestRoomsNp = listOf("पाहुना कोठा (Guest Room)", "अविवाहित छोरीको कोठा", "बिक्री हुने तयारी सामान / भण्डार", "सवारी साधन पार्किङ / ग्यारेज", "शौचालय तथा सेप्टिक ट्याङ्क"),
            bestRoomsEn = listOf("Guest Bedroom", "Daughter's Bedroom", "Finished Goods / Inventory", "Vehicle Parking / Garage", "Toilet & Septic Tank"),
            avoidRoomsNp = listOf("मास्टर बेडरुम (अस्थिरता ल्याउँछ)", "पूजा कोठा", "भान्सा (वैकल्पिक बाहेक नराख्ने)"),
            avoidRoomsEn = listOf("Master Bedroom (Causes Instability)", "Pooja Room", "Kitchen (Unless as secondary fallback)"),
            adviceNp = "वायव्य कोण परिवर्तन र चलायमान प्रकृतिको हुन्छ। यहाँ पाहुना कोठा राख्दा पाहुनाहरू लामो समय अड्किएर बस्दैनन्।",
            adviceEn = "North-West governs mobility and movement. Placing guest rooms here ensures guests enjoy their stay without overstaying."
        )
    )

    val roomRules: List<VastuRoomRule> = listOf(
        VastuRoomRule(
            id = "pooja",
            roomNp = "पूजा कोठा / मन्दिर",
            roomEn = "Pooja / Prayer Room",
            bestDirectionNp = "ईशान (उत्तर-पूर्व)",
            bestDirectionEn = "North-East (Ishanya)",
            alternativeDirectionNp = "पूर्व वा उत्तर",
            alternativeDirectionEn = "East or North",
            strictlyAvoidNp = "दक्षिण, दक्षिण-पश्चिम (नैऋत्य), शौचालय माथि वा मुनि",
            strictlyAvoidEn = "South, South-West, Directly above or below toilets",
            guidelineNp = "भगवानको मूर्ति पूर्व वा पश्चिम फर्काएर राख्ने। पूजा गर्दा व्यक्तिको अनुहार पूर्व वा उत्तर हुनुपर्छ। मन्दिर भुइँतला वा खुला स्थानमा हुनु उत्तम हुन्छ।",
            guidelineEn = "Idols should face East or West. The worshipper should face East or North during prayers. Keep this space spotlessly clean."
        ),
        VastuRoomRule(
            id = "kitchen",
            roomNp = "भान्सा कोठा",
            roomEn = "Kitchen",
            bestDirectionNp = "आग्नेय (दक्षिण-पूर्व)",
            bestDirectionEn = "South-East (Agni)",
            alternativeDirectionNp = "वायव्य (उत्तर-पश्चिम)",
            alternativeDirectionEn = "North-West (Vayavya)",
            strictlyAvoidNp = "ईशान (उत्तर-पूर्व), दक्षिण-पश्चिम (नैऋत्य), पूजा कोठा मुनि",
            strictlyAvoidEn = "North-East, South-West, Adjacent to or under pooja room",
            guidelineNp = "ग्यास चुल्हो पूर्वतर्फ फर्केर पकाउने गरी राख्ने। सिंक (पानी) र ग्यास (अग्नि) एक अर्काको सिधा अगाडि वा सँगै नराख्ने। फ्रिज पश्चिम वा दक्षिणमा राख्ने।",
            guidelineEn = "The cook should face East while preparing meals. Keep water sink and fire stove at a safe distance from each other."
        ),
        VastuRoomRule(
            id = "master_bed",
            roomNp = "मुख्य शयनकक्ष (घरमूलीको कोठा)",
            roomEn = "Master Bedroom",
            bestDirectionNp = "नैऋत्य (दक्षिण-पश्चिम)",
            bestDirectionEn = "South-West (Nairritya)",
            alternativeDirectionNp = "दक्षिण वा पश्चिम",
            alternativeDirectionEn = "South or West",
            strictlyAvoidNp = "ईशान (उत्तर-पूर्व), वायव्य (उत्तर-पश्चिम)",
            strictlyAvoidEn = "North-East (Health issues), North-West (Instability)",
            guidelineNp = "सुत्दा सिरानी सधैं दक्षिण वा पूर्व तर्फ हुनुपर्छ। उत्तर तर्फ सिरानी गरेर कहिल्यै नसुत्ने। खाटको सिधा अगाडि ऐना नराख्ने।",
            guidelineEn = "Head should point towards South or East while sleeping. Never sleep with head towards North. Avoid mirrors directly reflecting the bed."
        ),
        VastuRoomRule(
            id = "entrance",
            roomNp = "मुख्य प्रवेशद्वार (सिंहद्वार)",
            roomEn = "Main Entrance (Singhdwar)",
            bestDirectionNp = "उत्तर, ईशान (उत्तर-पूर्व), पूर्व",
            bestDirectionEn = "North, North-East, East",
            alternativeDirectionNp = "पश्चिम (केही निश्चित पदहरूमा)",
            alternativeDirectionEn = "West (In favorable padas)",
            strictlyAvoidNp = "दक्षिण-पश्चिम (नैऋत्य), दक्षिण (मध्य बाहेक)",
            strictlyAvoidEn = "South-West (Adverse energy), South (Unless auspicious pada)",
            guidelineNp = "मुख्य ढोका घरका अन्य ढोकाहरू भन्दा ठूलो र आकर्षक हुनुपर्छ। ढोका भित्र खुल्ने हुनुपर्छ र आवाज नआउने गरी कब्जाहरू ठीक राख्ने।",
            guidelineEn = "The main door should be the largest in the home, opening inward clockwise without creaking sounds."
        ),
        VastuRoomRule(
            id = "study",
            roomNp = "अध्ययन तथा कार्यालय कोठा",
            roomEn = "Study / Home Office",
            bestDirectionNp = "पूर्व, उत्तर वा ईशान",
            bestDirectionEn = "East, North or North-East",
            alternativeDirectionNp = "पश्चिम",
            alternativeDirectionEn = "West",
            strictlyAvoidNp = "दक्षिण-पश्चिम, शौचालय छेउमा",
            strictlyAvoidEn = "South-West, Facing dark corners or adjacent to toilets",
            guidelineNp = "पढ्दा वा काम गर्दा अनुहार पूर्व वा उत्तर फर्किनुपर्छ। अध्ययन टेबल पछाडि ठोस पर्खाल हुनु आत्मविश्वासको लागि राम्रो मानिन्छ।",
            guidelineEn = "Students should face East or North while studying. A solid wall behind the chair promotes concentration and support."
        ),
        VastuRoomRule(
            id = "cash_locker",
            roomNp = "धन / तिजोरी / सेफ",
            roomEn = "Cash Locker & Valuables",
            bestDirectionNp = "उत्तर (कुबेर स्थान)",
            bestDirectionEn = "North (Kubera Direction)",
            alternativeDirectionNp = "दक्षिण-पश्चिम (ढोका उत्तर खुल्ने गरी)",
            alternativeDirectionEn = "South-West (With door opening North)",
            strictlyAvoidNp = "दक्षिण तर्फ ढोका खुल्ने गरी, ईशान कुना",
            strictlyAvoidEn = "Door opening South, Inside toilets or humid corners",
            guidelineNp = "तिजोरीलाई दक्षिण वा पश्चिमको पर्खालमा टाँसेर राख्ने जसले गर्दा यसको ढोका उत्तर (कुबेर) वा पूर्व तर्फ खुलोस्।",
            guidelineEn = "Place the locker against the South wall so that its door opens towards the North (Kubera direction)."
        ),
        VastuRoomRule(
            id = "toilet",
            roomNp = "शौचालय तथा सेप्टिक ट्याङ्क",
            roomEn = "Toilet & Septic Tank",
            bestDirectionNp = "वायव्य (उत्तर-पश्चिम) वा पश्चिम",
            bestDirectionEn = "North-West or West",
            alternativeDirectionNp = "दक्षिण (मध्य भाग)",
            alternativeDirectionEn = "South (Middle section)",
            strictlyAvoidNp = "ईशान (उत्तर-पूर्व - महादोष), नैऋत्य (दक्षिण-पश्चिम)",
            strictlyAvoidEn = "North-East (Major spiritual & financial dosha), South-West",
            guidelineNp = "कमोडमा बस्दा अनुहार उत्तर वा दक्षिण हुनुपर्छ। शौचालयको ढोका सधैं बन्द राख्ने। पूजा कोठा वा भान्साको भित्तासँग शौचालय नजोड्ने।",
            guidelineEn = "Commode should be aligned North-South. Never share a common wall between toilet and kitchen or prayer altar."
        ),
        VastuRoomRule(
            id = "water_underground",
            roomNp = "भूमिगत पानी ट्याङ्की / इनार",
            roomEn = "Underground Water Tank / Well",
            bestDirectionNp = "ईशान (उत्तर-पूर्व)",
            bestDirectionEn = "North-East (Ishanya)",
            alternativeDirectionNp = "उत्तर वा पूर्व",
            alternativeDirectionEn = "North or East",
            strictlyAvoidNp = "नैऋत्य (दक्षिण-पश्चिम), आग्नेय (दक्षिण-पूर्व), दक्षिण",
            strictlyAvoidEn = "South-West, South-East, South",
            guidelineNp = "जमिन खनेर बनाइने पानीको स्रोत (इनार, बोरिङ, रिजर्भ्वायर) सधैं उत्तर-पूर्वमा हुनुपर्छ। यसले घरमा शान्ति र समृद्धि बढाउँछ।",
            guidelineEn = "All underground excavations for water (well, boring, reservoir) must be in the North-East for peace and prosperity."
        ),
        VastuRoomRule(
            id = "water_overhead",
            roomNp = "छतको ओभरहेड पानी ट्याङ्की",
            roomEn = "Overhead Roof Water Tank",
            bestDirectionNp = "पश्चिम वा दक्षिण-पश्चिम (नैऋत्य)",
            bestDirectionEn = "West or South-West (Nairritya)",
            alternativeDirectionNp = "दक्षिण",
            alternativeDirectionEn = "South",
            strictlyAvoidNp = "ईशान (उत्तर-पूर्व - छत भारी बनाउनु हुँदैन), मध्य (ब्रह्मस्थान)",
            strictlyAvoidEn = "North-East (Must stay light), Center (Brahmasthan)",
            guidelineNp = "छतको पानी ट्याङ्की गह्रौं हुने भएकाले यसलाई घरको सबैभन्दा अग्लो र भारी कुना (नैऋत्य वा पश्चिम) मा राख्नुपर्छ।",
            guidelineEn = "Overhead tanks are heavy loads and should sit atop the highest and most stable corner (West or South-West)."
        ),
        VastuRoomRule(
            id = "staircase",
            roomNp = "भर्याङ (Staircase)",
            roomEn = "Staircase",
            bestDirectionNp = "दक्षिण, पश्चिम वा दक्षिण-पश्चिम",
            bestDirectionEn = "South, West or South-West",
            alternativeDirectionNp = "वायव्य (उत्तर-पश्चिम)",
            alternativeDirectionEn = "North-West",
            strictlyAvoidNp = "ईशान (उत्तर-पूर्व), घरको केन्द्र (ब्रह्मस्थान)",
            strictlyAvoidEn = "North-East, Center of home (Brahmasthan)",
            guidelineNp = "भर्याङ चढ्दा घडीको दिशा (Clockwise) मा घुम्नुपर्छ। सिँढीको संख्या सामान्यतया बिजोर (१५, १७, १९, २१) हुनु शुभ मानिन्छ।",
            guidelineEn = "Stairs should ascend in a clockwise direction. The total step count is traditionally preferred to be an odd number."
        ),
        VastuRoomRule(
            id = "brahmasthan",
            roomNp = "ब्रह्मस्थान (घरको केन्द्र भाग)",
            roomEn = "Brahmasthan (House Center)",
            bestDirectionNp = "घरको ठ्याक्कै बीचको भाग",
            bestDirectionEn = "Center of the Building",
            alternativeDirectionNp = "खुला आँगन / हल",
            alternativeDirectionEn = "Open Courtyard / Living Hall",
            strictlyAvoidNp = "पिलर / खम्बा, भर्याङ, शौचालय, भान्सा, गह्रौं सामान",
            strictlyAvoidEn = "Pillars / Beams, Staircase, Toilets, Kitchen, Heavy weight",
            guidelineNp = "ब्रह्मस्थान घरको नाभिकेन्द्र हो। यसलाई सधैं खुला, हलुका र स्वच्छ राख्नुपर्छ। यहाँ कुनै पनि भारी संरचना वा खम्बा हुनुहुँदैन।",
            guidelineEn = "The center of the property must remain unobstructed by columns, heavy walls, or plumbing, allowing energy flow."
        )
    )

    fun getVastuDirection(headingDeg: Float): VastuDirection {
        val norm = ((headingDeg % 360f) + 360f) % 360f
        return directions.firstOrNull { d ->
            if (d.minAngle > d.maxAngle) {
                // Spans over 0 degrees (North: 337.5 to 22.5)
                norm >= d.minAngle || norm < d.maxAngle
            } else {
                norm >= d.minAngle && norm < d.maxAngle
            }
        } ?: directions[0]
    }
}
