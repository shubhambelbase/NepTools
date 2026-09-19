package com.neptools.app.core.data

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
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
        "all" to "सबै",
        "security" to "सुरक्षा",
        "medical" to "स्वास्थ्य",
        "rescue" to "उद्धार",
        "social" to "महिला तथा बालबालिका",
        "blood" to "रक्तसञ्चार"
    )

    val provinces = listOf(
        "All", "National", "Bagmati", "Koshi", "Madhesh",
        "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"
    )

    val defaultContacts = listOf(
        // ==================== National Toll-Free Lifelines ====================
        EmergencyContact("नेपाल प्रहरी", "Nepal Police", "100", "security", "National", "All", "आपतकालीन प्रहरी सहायता (२४ घण्टा निःशुल्क)", "Emergency Police Assistance (24/7 Toll-Free)"),
        EmergencyContact("एम्बुलेन्स सेवा", "Nepal Ambulance Service", "102", "medical", "National", "All", "आपतकालीन एम्बुलेन्स सेवा (२४ घण्टा)", "Emergency Medical Transport (24/7)"),
        EmergencyContact("दमकल / अग्नि नियन्त्रण", "Fire Brigade / Damkal", "101", "rescue", "National", "All", "आगलागी तथा विपद् उद्धार", "Fire Emergency & Rescue"),
        EmergencyContact("ट्राफिक प्रहरी", "Traffic Police Hotline", "103", "security", "National", "All", "ट्राफिक सूचना तथा दुर्घटना सहायता", "Traffic Updates & Accident Support"),
        EmergencyContact("बाल हेल्पलाइन", "Child Helpline Nepal", "1098", "social", "National", "All", "बालबालिकाको उद्धार तथा संरक्षण (निःशुल्क)", "Child Protection & Support"),
        EmergencyContact("राष्ट्रिय महिला आयोग", "National Women Commission", "1145", "social", "National", "All", "महिला हिंसाविरुद्ध २४ घण्टे हटलाइन (खबर गरौँ)", "24/7 Women Violence Helpline (Khabar Garaun)"),
        EmergencyContact("पर्यटक प्रहरी", "Tourist Police Nepal", "1144", "security", "National", "Kathmandu", "पर्यटक सहायता तथा सुरक्षा", "Tourist Safety & Assistance"),
        EmergencyContact("सशस्त्र प्रहरी बल", "Armed Police Force", "1114", "security", "National", "All", "सीमा सुरक्षा तथा विपद् उद्धार", "Disaster Response & Border Security"),
        EmergencyContact("विपद् पूर्वसूचना तथा व्यवस्थापन", "Disaster Emergency Info", "1155", "rescue", "National", "All", "बाढी, पहिरो तथा जल तथा मौसम विपद् सूचना", "Flood & Disaster Early Warning"),
        EmergencyContact("हेलो सरकार", "Hello Sarkar Grievance", "1111", "social", "National", "All", "प्रधानमन्त्री कार्यालय — सरकारी सेवा तथा जनगुनासो", "Citizen Grievance & Inquiries"),

        // ==================== Bagmati Province & Kathmandu Valley ====================
        EmergencyContact("काठमाडौं प्रहरी कन्ट्रोल", "Kathmandu Police Control", "014226998", "security", "Bagmati", "Kathmandu", "जिल्ला प्रहरी परिसर टेकु, काठमाडौं", "District Police Control Room Teku, Kathmandu"),
        EmergencyContact("ललितपुर प्रहरी कन्ट्रोल", "Lalitpur Police Control", "015521207", "security", "Bagmati", "Lalitpur", "जिल्ला प्रहरी परिसर जावलाखेल, ललितपुर", "District Police Control Jawalakhel, Lalitpur"),
        EmergencyContact("भक्तपुर प्रहरी कन्ट्रोल", "Bhaktapur Police Control", "016614821", "security", "Bagmati", "Bhaktapur", "जिल्ला प्रहरी परिसर भक्तपुर", "District Police Control Bhaktapur"),
        EmergencyContact("चितवन प्रहरी कन्ट्रोल", "Chitwan Police Control", "056520199", "security", "Bagmati", "Chitwan", "जिल्ला प्रहरी कार्यालय भरतपुर, चितवन", "District Police Office Bharatpur, Chitwan"),
        EmergencyContact("विषाक्त सूचना केन्द्र", "Poison Information Center", "014502011", "medical", "Bagmati", "Kathmandu", "त्रिवि शिक्षण अस्पताल, महाराजगञ्ज (२४ घण्टा)", "TUTH 24/7 Poisoning Emergency Center"),
        EmergencyContact("वीर अस्पताल आकस्मिक कक्ष", "Bir Hospital Emergency", "015321988", "medical", "Bagmati", "Kathmandu", "सरकारी केन्द्रीय आकस्मिक सेवा (२४ घण्टा)", "Central Government Hospital 24/7 Emergency"),
        EmergencyContact("शिक्षण अस्पताल महाराजगञ्ज", "TUTH Emergency", "014512505", "medical", "Bagmati", "Kathmandu", "त्रिवि शिक्षण अस्पताल आकस्मिक कक्ष", "Teaching Hospital Emergency Maharajgunj"),
        EmergencyContact("पाटन अस्पताल आकस्मिक कक्ष", "Patan Hospital Emergency", "015522295", "medical", "Bagmati", "Lalitpur", "पाटन अस्पताल, लगनखेल", "Patan Hospital Lagankhel"),
        EmergencyContact("भक्तपुर अस्पताल आकस्मिक कक्ष", "Bhaktapur Hospital Emergency", "016610798", "medical", "Bagmati", "Bhaktapur", "भक्तपुर अस्पताल, दूधपाटी", "Bhaktapur Hospital Emergency Dudhpati"),
        EmergencyContact("चितवन मेडिकल कलेज आकस्मिक", "Chitwan Medical College", "056532933", "medical", "Bagmati", "Chitwan", "सीएमसी भरतपुर आकस्मिक कक्ष", "CMC Hospital Emergency Bharatpur"),
        EmergencyContact("केन्द्रीय रक्तसञ्चार सेवा", "Central Blood Transfusion Service", "014225344", "blood", "Bagmati", "Kathmandu", "नेपाल रेडक्रस प्रदर्शनीमार्ग, काठमाडौं", "Red Cross Central Blood Bank, Kathmandu"),
        EmergencyContact("रेडक्रस रक्तसञ्चार ललितपुर", "Lalitpur Blood Bank", "015427033", "blood", "Bagmati", "Lalitpur", "नेपाल रेडक्रस, पुलचोक", "Red Cross Blood Bank, Pulchowk"),
        EmergencyContact("रेडक्रस रक्तसञ्चार भक्तपुर", "Bhaktapur Blood Bank", "016611661", "blood", "Bagmati", "Bhaktapur", "नेपाल रेडक्रस, भक्तपुर", "Red Cross Blood Bank, Bhaktapur"),
        EmergencyContact("रेडक्रस रक्तसञ्चार चितवन", "Chitwan Blood Bank", "056520880", "blood", "Bagmati", "Chitwan", "नेपाल रेडक्रस, भरतपुर", "Red Cross Blood Bank, Bharatpur"),

        // ==================== Koshi Province ====================
        EmergencyContact("मोरङ प्रहरी कन्ट्रोल", "Morang Police Control", "0215462158", "security", "Koshi", "Morang", "जिल्ला प्रहरी कार्यालय विराटनगर, मोरङ", "District Police Office Biratnagar, Morang"),
        EmergencyContact("सुनसरी प्रहरी कन्ट्रोल", "Sunsari Police Control", "025565100", "security", "Koshi", "Sunsari", "जिल्ला प्रहरी कार्यालय इनरुवा / धरान", "District Police Office Sunsari"),
        EmergencyContact("झापा प्रहरी कन्ट्रोल", "Jhapa Police Control", "023520199", "security", "Koshi", "Jhapa", "जिल्ला प्रहरी कार्यालय भद्रपुर, झापा", "District Police Office Bhadrapur, Jhapa"),
        EmergencyContact("कोशी अस्पताल आकस्मिक कक्ष", "Koshi Hospital Emergency", "021530103", "medical", "Koshi", "Morang", "रंगेली रोड, विराटनगर, कोशी प्रदेश", "Koshi Provincial Hospital Biratnagar"),
        EmergencyContact("बीपी कोइराला स्वास्थ्य विज्ञान प्रतिष्ठान", "BPKIHS Emergency Dharan", "025525555", "medical", "Koshi", "Sunsari", "विशिष्ट आकस्मिक सेवा, धरान", "BPKIHS Tertiary Emergency Dharan"),
        EmergencyContact("मेची प्रादेशिक अस्पताल", "Mechi Hospital Emergency", "023523024", "medical", "Koshi", "Jhapa", "भद्रपुर, झापा", "Mechi Provincial Hospital Bhadrapur"),
        EmergencyContact("रेडक्रस रक्तसञ्चार विराटनगर", "Biratnagar Blood Bank", "021523326", "blood", "Koshi", "Morang", "विराटनगर, मोरङ", "Red Cross Blood Bank Biratnagar"),
        EmergencyContact("रेडक्रस रक्तसञ्चार धरान", "Dharan Blood Bank", "025520111", "blood", "Koshi", "Sunsari", "धरान, सुनसरी", "Red Cross Blood Bank Dharan"),

        // ==================== Madhesh Province ====================
        EmergencyContact("पर्सा प्रहरी कन्ट्रोल", "Parsa Police Control", "051522199", "security", "Madhesh", "Parsa", "जिल्ला प्रहरी कार्यालय वीरगञ्ज, पर्सा", "District Police Office Birgunj, Parsa"),
        EmergencyContact("धनुषा प्रहरी कन्ट्रोल", "Dhanusha Police Control", "041520199", "security", "Madhesh", "Dhanusha", "जिल्ला प्रहरी कार्यालय जनकपुरधाम", "District Police Office Janakpurdham"),
        EmergencyContact("नारायणी अस्पताल आकस्मिक कक्ष", "Narayani Hospital Emergency", "051522022", "medical", "Madhesh", "Parsa", "वीरगञ्ज, पर्सा", "Narayani Hospital Birgunj"),
        EmergencyContact("जनकपुर प्रादेशिक अस्पताल", "Janakpur Provincial Hospital", "041520200", "medical", "Madhesh", "Dhanusha", "जनकपुरधाम, धनुषा", "Provincial Hospital Janakpur"),
        EmergencyContact("रेडक्रस रक्तसञ्चार वीरगञ्ज", "Birgunj Blood Bank", "051522045", "blood", "Madhesh", "Parsa", "वीरगञ्ज, पर्सा", "Red Cross Blood Bank Birgunj"),
        EmergencyContact("रेडक्रस रक्तसञ्चार जनकपुर", "Janakpur Blood Bank", "041520333", "blood", "Madhesh", "Dhanusha", "जनकपुरधाम, धनुषा", "Red Cross Blood Bank Janakpur"),

        // ==================== Gandaki Province ====================
        EmergencyContact("कास्की प्रहरी कन्ट्रोल", "Kaski Police Control", "061522099", "security", "Gandaki", "Kaski", "जिल्ला प्रहरी कार्यालय पोखरा, कास्की", "District Police Office Pokhara, Kaski"),
        EmergencyContact("तनहुँ प्रहरी कन्ट्रोल", "Tanahun Police Control", "065560199", "security", "Gandaki", "Tanahun", "जिल्ला प्रहरी कार्यालय दमौली, तनहुँ", "District Police Office Damauli, Tanahun"),
        EmergencyContact("गण्डकी अस्पताल आकस्मिक कक्ष", "Gandaki Hospital Emergency", "061570066", "medical", "Gandaki", "Kaski", "पोखरा स्वास्थ्य विज्ञान प्रतिष्ठान (रामघाट)", "Western Regional Hospital, Pokhara"),
        EmergencyContact("मणिपाल शिक्षण अस्पताल आकस्मिक", "Manipal Hospital Emergency", "061526416", "medical", "Gandaki", "Kaski", "फूलबारी, पोखरा", "Manipal Teaching Hospital Pokhara"),
        EmergencyContact("दमौली अस्पताल आकस्मिक कक्ष", "Damauli Hospital Emergency", "065563203", "medical", "Gandaki", "Tanahun", "दमौली, तनहुँ", "Damauli District Hospital"),
        EmergencyContact("रेडक्रस रक्तसञ्चार केन्द्र पोखरा", "Pokhara Blood Bank", "061521091", "blood", "Gandaki", "Kaski", "रामघाट, पोखरा", "Red Cross Regional Blood Bank Pokhara"),

        // ==================== Lumbini Province ====================
        EmergencyContact("रुपन्देही प्रहरी कन्ट्रोल", "Rupandehi Police Control", "071520199", "security", "Lumbini", "Rupandehi", "जिल्ला प्रहरी कार्यालय भैरहवा / बुटवल", "District Police Office Rupandehi"),
        EmergencyContact("बाँके प्रहरी कन्ट्रोल", "Banke Police Control", "081520211", "security", "Lumbini", "Banke", "जिल्ला प्रहरी कार्यालय नेपालगञ्ज, बाँके", "District Police Office Nepalgunj, Banke"),
        EmergencyContact("दाङ प्रहरी कन्ट्रोल", "Dang Police Control", "082560199", "security", "Lumbini", "Dang", "जिल्ला प्रहरी कार्यालय घोराही, दाङ", "District Police Office Ghorahi, Dang"),
        EmergencyContact("लुम्बिनी प्रादेशिक अस्पताल", "Lumbini Provincial Hospital", "071534010", "medical", "Lumbini", "Rupandehi", "बुटवल, लुम्बिनी प्रदेश (२४ घण्टा)", "Lumbini Provincial Hospital, Butwal"),
        EmergencyContact("भेरी अस्पताल आकस्मिक कक्ष", "Bheri Hospital Emergency", "081520120", "medical", "Lumbini", "Banke", "नेपालगञ्ज, बाँके", "Bheri Hospital Emergency Nepalgunj"),
        EmergencyContact("राप्ती प्रादेशिक अस्पताल", "Rapti Provincial Hospital", "082520011", "medical", "Lumbini", "Dang", "तुलसीपुर, दाङ", "Rapti Provincial Hospital Tulsipur"),
        EmergencyContact("रेडक्रस रक्तसञ्चार बुटवल", "Butwal Blood Bank", "071531004", "blood", "Lumbini", "Rupandehi", "बुटवल, रुपन्देही", "Red Cross Blood Bank Butwal"),
        EmergencyContact("रेडक्रस रक्तसञ्चार नेपालगञ्ज", "Nepalgunj Blood Bank", "081520174", "blood", "Lumbini", "Banke", "नेपालगञ्ज, बाँके", "Red Cross Blood Bank Nepalgunj"),

        // ==================== Karnali Province ====================
        EmergencyContact("सुर्खेत प्रहरी कन्ट्रोल", "Surkhet Police Control", "083520199", "security", "Karnali", "Surkhet", "जिल्ला प्रहरी कार्यालय वीरेन्द्रनगर, सुर्खेत", "District Police Office Birendranagar, Surkhet"),
        EmergencyContact("कर्णाली प्रादेशिक अस्पताल", "Karnali Provincial Hospital", "083520200", "medical", "Karnali", "Surkhet", "वीरेन्द्रनगर, सुर्खेत", "Karnali Provincial Hospital Surkhet"),
        EmergencyContact("कर्णाली स्वास्थ्य विज्ञान प्रतिष्ठान", "KAHS Emergency Jumla", "087520115", "medical", "Karnali", "Jumla", "खलङ्गा, जुम्ला", "KAHS Teaching Hospital Jumla"),
        EmergencyContact("रेडक्रस रक्तसञ्चार सुर्खेत", "Surkhet Blood Bank", "083520144", "blood", "Karnali", "Surkhet", "वीरेन्द्रनगर, सुर्खेत", "Red Cross Blood Bank Surkhet"),

        // ==================== Sudurpashchim Province ====================
        EmergencyContact("कैलाली प्रहरी कन्ट्रोल", "Kailali Police Control", "091521150", "security", "Sudurpashchim", "Kailali", "जिल्ला प्रहरी कार्यालय धनगढी, कैलाली", "District Police Office Dhangadhi, Kailali"),
        EmergencyContact("कञ्चनपुर प्रहरी कन्ट्रोल", "Kanchanpur Police Control", "099521199", "security", "Sudurpashchim", "Kanchanpur", "जिल्ला प्रहरी कार्यालय महेन्द्रनगर", "District Police Office Mahendranagar"),
        EmergencyContact("सेती प्रादेशिक अस्पताल", "Seti Provincial Hospital", "091525911", "medical", "Sudurpashchim", "Kailali", "धनगढी, सुदूरपश्चिम प्रदेश", "Seti Provincial Hospital Dhangadhi"),
        EmergencyContact("महाकाली प्रादेशिक अस्पताल", "Mahakali Hospital Emergency", "099521111", "medical", "Sudurpashchim", "Kanchanpur", "महेन्द्रनगर, कञ्चनपुर", "Mahakali Provincial Hospital Mahendranagar"),
        EmergencyContact("रेडक्रस रक्तसञ्चार धनगढी", "Dhangadhi Blood Bank", "091521600", "blood", "Sudurpashchim", "Kailali", "धनगढी, कैलाली", "Red Cross Blood Bank Dhangadhi")
    )
 
    private val _liveContacts = MutableStateFlow<List<EmergencyContact>>(defaultContacts)
    val liveContacts: StateFlow<List<EmergencyContact>> = _liveContacts.asStateFlow()

    val allContacts: List<EmergencyContact> get() = _liveContacts.value

    fun allContacts(): List<EmergencyContact> = _liveContacts.value

    data class MergeResult(
        val added: Int,
        val totals: Int,
        val skippedCurated: Int
    )

    /**
     * Applies a remotely fetched contact list.
     *
     * Trust model: the contacts compiled into the app are the verified baseline and the numbers
     * a user may be dialling in an emergency, so remote data is merged rather than substituted.
     * A remote entry can add new contacts, but it can never overwrite or remove a curated
     * hotline, and it can never attach a new name to a curated number. That keeps a stale or
     * tampered feed from redirecting an emergency call.
     */
    fun mergeRemoteContacts(remote: List<EmergencyContact>): MergeResult {
        if (remote.isEmpty()) return MergeResult(0, _liveContacts.value.size, 0)

        val curatedKeys = defaultContacts.map { identityKey(it) }.toHashSet()
        val curatedNumbers = defaultContacts.map { normalizeNumber(it.number) }.toHashSet()

        val merged = defaultContacts.toMutableList()
        val seen = curatedKeys.toMutableSet()
        var added = 0
        var skippedCurated = 0

        for (contact in remote) {
            val key = identityKey(contact)
            if (key in curatedKeys) {
                skippedCurated++
                continue
            }
            // Never let remote data re-label a verified hotline number.
            if (normalizeNumber(contact.number) in curatedNumbers) {
                skippedCurated++
                continue
            }
            if (!seen.add(key)) continue
            merged.add(contact)
            added++
        }

        _liveContacts.value = merged
        return MergeResult(added = added, totals = merged.size, skippedCurated = skippedCurated)
    }

    private fun identityKey(contact: EmergencyContact): String =
        contact.nameEn.trim().lowercase() + "|" + contact.district.trim().lowercase()

    private fun normalizeNumber(number: String): String = number.filter { it.isDigit() }

    fun isLocal(contact: EmergencyContact, userDistrict: String, userProvince: String): Boolean {
        if (contact.province == "National") return false
        val distMatch = userDistrict != "All" && contact.district.equals(userDistrict, ignoreCase = true)
        val provMatch = userProvince != "All" && userProvince != "National" && contact.province.equals(userProvince, ignoreCase = true)
        return distMatch || provMatch
    }

    /**
     * Prioritized search:
     * 1. Exact local district & province matches pinned first
     * 2. National 24/7 lifelines
     * 3. Other matching contacts
     */
    fun search(
        query: String,
        category: String,
        province: String = "All",
        district: String = "All",
        sourceContacts: List<EmergencyContact> = _liveContacts.value
    ): List<EmergencyContact> {
        val q = query.trim().lowercase()

        val filtered = sourceContacts.filter { c ->
            val matchCat = category == "all" || c.category == category
            val matchQuery = q.isEmpty() ||
                c.nameNp.lowercase().contains(q) ||
                c.nameEn.lowercase().contains(q) ||
                c.number.contains(q) ||
                c.district.lowercase().contains(q) ||
                c.province.lowercase().contains(q) ||
                c.descriptionNp.lowercase().contains(q) ||
                c.descriptionEn.lowercase().contains(q)
            matchCat && matchQuery
        }

        if (province == "All" && district == "All") {
            return filtered.sortedWith(
                compareBy<EmergencyContact> { c ->
                    if (c.province == "National") 0 else 1
                }.thenBy { it.nameEn }
            )
        }

        // Sorting priority:
        // Priority 0: Exact District match (local area)
        // Priority 1: Exact Province match (regional area)
        // Priority 2: National Lifeline (applies everywhere)
        // Priority 3: Other districts/provinces
        return filtered.sortedWith(
            compareBy<EmergencyContact> { c ->
                when {
                    district != "All" && c.district.equals(district, ignoreCase = true) -> 0
                    province != "All" && province != "National" && c.province.equals(province, ignoreCase = true) -> 1
                    c.province == "National" -> 2
                    else -> 3
                }
            }.thenBy { it.nameEn }
        )
    }
}
