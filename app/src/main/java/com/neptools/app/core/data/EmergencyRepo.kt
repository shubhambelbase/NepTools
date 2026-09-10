package com.neptools.app.core.data

data class EmergencyContact(
    val nameNp: String,
    val nameEn: String,
    val number: String,
    val category: String, // "security", "medical", "rescue", "social", "blood"
    val province: String = "National", // "National", "Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"
    val district: String = "All",
    val descriptionNp: String,
    val descriptionEn: String
)

object EmergencyRepo {

    val categories = listOf(
        "all" to "सबै (All)",
        "security" to "सुरक्षा (Security)",
        "medical" to "स्वास्थ्य (Medical)",
        "rescue" to "उद्धार (Rescue & Fire)",
        "social" to "महिला/बालबालिका (Social Help)",
        "blood" to "रक्तसञ्चार (Blood Banks)"
    )

    val provinces = listOf(
        "All", "National", "Bagmati", "Koshi", "Madhesh",
        "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"
    )

    val allContacts = listOf(
        // National Toll-Free
        EmergencyContact("नेपाल प्रहरी (Police)", "Nepal Police", "100", "security", "National", "All", "आपतकालीन प्रहरी सहायता (Toll-Free)", "Emergency Police Assistance"),
        EmergencyContact("एम्बुलेन्स (Ambulance)", "Nepal Ambulance Service", "102", "medical", "National", "All", "आपतकालीन एम्बुलेन्स सेवा", "Emergency Medical Transport"),
        EmergencyContact("दमकल / अग्नि नियन्त्रण (Fire)", "Fire Brigade / Damkal", "101", "rescue", "National", "All", "आगलागी तथा विपद् उद्धार", "Fire Emergency & Rescue"),
        EmergencyContact("ट्राफिक प्रहरी (Traffic Police)", "Traffic Police Hotline", "103", "security", "National", "All", "ट्राफिक सूचना तथा दुर्घटना सहायता", "Traffic Updates & Accident Support"),
        EmergencyContact("बाल हेल्पलाइन (Child Helpline)", "Child Helpline Nepal", "1098", "social", "National", "All", "बालबालिकाको उद्धार तथा संरक्षण", "Child Protection & Support"),
        EmergencyContact("राष्ट्रिय महिला आयोग (Women Helpline)", "National Women Commission", "1145", "social", "National", "All", "महिला हिंसाविरुद्धको २४ घण्टे हटलाइन (Khabar Garaun)", "24/7 Women Violence Helpline"),
        EmergencyContact("पर्यटक प्रहरी (Tourist Police)", "Tourist Police Nepal", "1144", "security", "National", "Kathmandu", "पर्यटक सहायता तथा सुरक्षा", "Tourist Safety & Assistance"),
        EmergencyContact("सशस्त्र प्रहरी बल (APF)", "Armed Police Force", "1114", "security", "National", "All", "सीमा सुरक्षा तथा विपद् उद्धार", "Disaster Response & Border Security"),
        EmergencyContact("विपद् पूर्वसूचना तथा व्यवस्थापन", "Disaster Emergency Info", "1155", "rescue", "National", "All", "बाढी, पहिरो तथा मौसम विपद् सूचना", "Flood & Disaster Early Warning"),
        EmergencyContact("हेलो सरकार (Hello Sarkar)", "Hello Sarkar Grievance", "1111", "social", "National", "All", "सरकारी सेवा तथा जनगुनासो", "Citizen Grievance & Inquiries"),
        EmergencyContact("केन्द्रीय रक्तसञ्चार सेवा (Blood Bank)", "Central Blood Transfusion Service", "014225344", "blood", "Bagmati", "Kathmandu", "नेपाल रेडक्रस प्रदर्शनीमार्ग, काठमाडौं", "Red Cross Central Blood Bank, Kathmandu"),
        EmergencyContact("विषाक्त सूचना केन्द्र (Poison Info)", "Poison Information Center", "014217112", "medical", "Bagmati", "Kathmandu", "शिक्षण अस्पताल (TUTH), महाराजगञ्ज", "TUTH Poisoning Emergency Center"),

        // Major Hospital Hotlines
        EmergencyContact("वीर अस्पताल आकस्मिक कक्ष", "Bir Hospital Emergency", "014221119", "medical", "Bagmati", "Kathmandu", "सरकारी केन्द्रीय आकस्मिक सेवा", "Central Government Hospital"),
        EmergencyContact("शिक्षण अस्पताल महाराजगञ्ज", "TUTH Emergency", "014412303", "medical", "Bagmati", "Kathmandu", "त्रिवि शिक्षण अस्पताल आकस्मिक कक्ष", "Teaching Hospital Emergency"),
        EmergencyContact("पाटन अस्पताल आकस्मिक कक्ष", "Patan Hospital Emergency", "015522295", "medical", "Bagmati", "Lalitpur", "पाटन अस्पताल, लगनखेल", "Patan Hospital Lagankhel"),
        EmergencyContact("कोशी अस्पताल आकस्मिक कक्ष", "Koshi Hospital Emergency", "021530138", "medical", "Koshi", "Morang", "विराटनगर, कोशी प्रदेश", "Koshi Hospital Biratnagar"),
        EmergencyContact("गण्डकी अस्पताल आकस्मिक कक्ष", "Gandaki Hospital Emergency", "061520055", "medical", "Gandaki", "Kaski", "पोखरा स्वास्थ्य विज्ञान प्रतिष्ठान", "Western Regional Hospital, Pokhara"),
        EmergencyContact("लुम्बिनी प्रादेशिक अस्पताल", "Lumbini Provincial Hospital", "071540200", "medical", "Lumbini", "Rupandehi", "बुटवल, लुम्बिनी प्रदेश", "Lumbini Provincial Hospital, Butwal"),
        EmergencyContact("कर्णाली प्रादेशिक अस्पताल", "Karnali Provincial Hospital", "083520120", "medical", "Karnali", "Surkhet", "वीरेन्द्रनगर, सुर्खेत", "Provincial Hospital Surkhet"),
        EmergencyContact("सेती प्रादेशिक अस्पताल", "Seti Provincial Hospital", "091524200", "medical", "Sudurpashchim", "Kailali", "धनगढी, सुदूरपश्चिम प्रदेश", "Seti Hospital Dhangadhi"),

        // Regional Blood Banks
        EmergencyContact("रेडक्रस रक्तसञ्चार केन्द्र पोखरा", "Pokhara Blood Bank", "061521091", "blood", "Gandaki", "Kaski", "रामघाट, पोखरा", "Red Cross Blood Bank Pokhara"),
        EmergencyContact("रेडक्रस रक्तसञ्चार विराटनगर", "Biratnagar Blood Bank", "021523333", "blood", "Koshi", "Morang", "विराटनगर", "Red Cross Blood Bank Biratnagar"),
        EmergencyContact("रेडक्रस रक्तसञ्चार बुटवल", "Butwal Blood Bank", "071540300", "blood", "Lumbini", "Rupandehi", "बुटवल", "Red Cross Blood Bank Butwal"),
        EmergencyContact("रेडक्रस रक्तसञ्चार नेपालगञ्ज", "Nepalgunj Blood Bank", "081520152", "blood", "Lumbini", "Banke", "नेपालगञ्ज", "Red Cross Blood Bank Nepalgunj"),
        EmergencyContact("रेडक्रस रक्तसञ्चार धनगढी", "Dhangadhi Blood Bank", "091521155", "blood", "Sudurpashchim", "Kailali", "धनगढी", "Red Cross Blood Bank Dhangadhi")
    )

    fun search(query: String, category: String, province: String): List<EmergencyContact> {
        val q = query.trim().lowercase()
        return allContacts.filter { c ->
            val matchCat = category == "all" || c.category == category
            val matchProv = province == "All" || c.province == "National" || c.province.equals(province, ignoreCase = true)
            val matchQuery = q.isEmpty() ||
                c.nameNp.lowercase().contains(q) ||
                c.nameEn.lowercase().contains(q) ||
                c.number.contains(q) ||
                c.district.lowercase().contains(q) ||
                c.descriptionNp.lowercase().contains(q)
            matchCat && matchProv && matchQuery
        }
    }
}
