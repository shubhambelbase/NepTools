package com.neptools.app.core.data

import androidx.compose.runtime.Immutable

@Immutable
data class PostalEntry(
    val district: String,
    val districtNp: String,
    val postOffice: String,
    val postOfficeNp: String,
    val code: String,
    val isDpo: Boolean = false // District Post Office
)

object PostalCodeRepo {

    val entries: List<PostalEntry> = listOf(
        // Kathmandu Valley
        PostalEntry("Kathmandu", "काठमाडौं", "Kathmandu GPO (Sundhara)", "गोश्वारा हुलाक कार्यालय, सुन्धारा", "44600", true),
        PostalEntry("Kathmandu", "काठमाडौं", "Thamel", "ठमेल", "44601"),
        PostalEntry("Kathmandu", "काठमाडौं", "Baneshwor", "बानेश्वर", "44613"),
        PostalEntry("Kathmandu", "काठमाडौं", "Kirtipur", "कीर्तिपुर", "44618"),
        PostalEntry("Kathmandu", "काठमाडौं", "Budhanilkantha", "बुढानीलकण्ठ", "44606"),
        PostalEntry("Kathmandu", "काठमाडौं", "Chabahil", "चावहिल", "44602"),
        PostalEntry("Kathmandu", "काठमाडौं", "Kalanki", "कलङ्की", "44614"),
        PostalEntry("Kathmandu", "काठमाडौं", "Sankhu", "साँखु", "44619"),
        PostalEntry("Kathmandu", "काठमाडौं", "Dillibazar", "डिल्लीबजार", "44605"),
        PostalEntry("Lalitpur", "ललितपुर", "Patan DPO (Lagankhel)", "जिल्ला हुलाक कार्यालय, लगनखेल", "44700", true),
        PostalEntry("Lalitpur", "ललितपुर", "Godawari", "गोदावरी", "44709"),
        PostalEntry("Lalitpur", "ललितपुर", "Lubhu", "लुभू", "44710"),
        PostalEntry("Lalitpur", "ललितपुर", "Sanepa", "सानेपा", "44701"),
        PostalEntry("Bhaktapur", "भक्तपुर", "Bhaktapur DPO", "जिल्ला हुलाक कार्यालय, भक्तपुर", "44800", true),
        PostalEntry("Bhaktapur", "भक्तपुर", "Thimi", "थिमी", "44801"),
        PostalEntry("Bhaktapur", "भक्तपुर", "Nagarkot", "नगरकोट", "44812"),

        // Koshi Province
        PostalEntry("Morang", "मोरङ", "Biratnagar DPO", "जिल्ला हुलाक कार्यालय, विराटनगर", "56600", true),
        PostalEntry("Morang", "मोरङ", "Urlabari", "उर्लाबारी", "56604"),
        PostalEntry("Morang", "मोरङ", "Belbari", "बेलबारी", "56602"),
        PostalEntry("Sunsari", "सुनसरी", "Dharan", "धरान", "56700"),
        PostalEntry("Sunsari", "सुनसरी", "Inaruwa DPO", "जिल्ला हुलाक कार्यालय, इनरुवा", "56701", true),
        PostalEntry("Sunsari", "सुनसरी", "Itahari", "इटहरी", "56705"),
        PostalEntry("Jhapa", "झापा", "Bhadrapur DPO", "जिल्ला हुलाक कार्यालय, भद्रपुर", "57200", true),
        PostalEntry("Jhapa", "झापा", "Birtamod", "बिर्तामोड", "57204"),
        PostalEntry("Jhapa", "झापा", "Damak", "दमक", "57217"),
        PostalEntry("Ilam", "इलाम", "Ilam DPO", "जिल्ला हुलाक कार्यालय, इलाम", "57300", true),
        PostalEntry("Panchthar", "पाँचथर", "Phidim DPO", "जिल्ला हुलाक कार्यालय, फिदिम", "57400", true),
        PostalEntry("Taplejung", "ताप्लेजुङ", "Taplejung DPO", "जिल्ला हुलाक कार्यालय, ताप्लेजुङ", "57500", true),
        PostalEntry("Dhankuta", "धनकुटा", "Dhankuta DPO", "जिल्ला हुलाक कार्यालय, धनकुटा", "56800", true),
        PostalEntry("Bhojpur", "भोजपुर", "Bhojpur DPO", "जिल्ला हुलाक कार्यालय, भोजपुर", "57000", true),
        PostalEntry("Sankhuwasabha", "संखुवासभा", "Khandbari DPO", "जिल्ला हुलाक कार्यालय, खाँदबारी", "56900", true),
        PostalEntry("Terhathum", "तेह्रथुम", "Myanglung DPO", "जिल्ला हुलाक कार्यालय, म्याङलुङ", "57100", true),
        PostalEntry("Okhaldhunga", "ओखलढुङ्गा", "Okhaldhunga DPO", "जिल्ला हुलाक कार्यालय, ओखलढुङ्गा", "56100", true),
        PostalEntry("Khotang", "खोटाङ", "Diktel DPO", "जिल्ला हुलाक कार्यालय, दिक्तेल", "56200", true),
        PostalEntry("Solukhumbu", "सोलुखुम्बु", "Salleri DPO", "जिल्ला हुलाक कार्यालय, सल्लेरी", "56000", true),
        PostalEntry("Udayapur", "उदयपुर", "Gaighat DPO", "जिल्ला हुलाक कार्यालय, गाईघाट", "56300", true),

        // Madhesh Province
        PostalEntry("Dhanusha", "धनुषा", "Janakpur DPO", "जिल्ला हुलाक कार्यालय, जनकपुरधाम", "45600", true),
        PostalEntry("Parsa", "पर्सा", "Birgunj DPO", "जिल्ला हुलाक कार्यालय, वीरगञ्ज", "44300", true),
        PostalEntry("Bara", "बारा", "Kalaiya DPO", "जिल्ला हुलाक कार्यालय, कलैया", "44400", true),
        PostalEntry("Rautahat", "रौतहट", "Gaur DPO", "जिल्ला हुलाक कार्यालय, गौर", "44500", true),
        PostalEntry("Sarlahi", "सर्लाही", "Malangwa DPO", "जिल्ला हुलाक कार्यालय, मलंगवा", "45800", true),
        PostalEntry("Mahottari", "महोत्तरी", "Jaleshwar DPO", "जिल्ला हुलाक कार्यालय, जलेश्वर", "45700", true),
        PostalEntry("Siraha", "सिराहा", "Siraha DPO", "जिल्ला हुलाक कार्यालय, सिराहा", "56500", true),
        PostalEntry("Saptari", "सप्तरी", "Rajbiraj DPO", "जिल्ला हुलाक कार्यालय, राजविराज", "56400", true),

        // Bagmati Province (Rest)
        PostalEntry("Chitwan", "चितवन", "Bharatpur DPO", "जिल्ला हुलाक कार्यालय, भरतपुर", "44200", true),
        PostalEntry("Chitwan", "चितवन", "Ratnanagar (Tandi)", "रत्ननगर, टाँडी", "44204"),
        PostalEntry("Chitwan", "चितवन", "Madi", "माडी", "44207"),
        PostalEntry("Makwanpur", "मकवानपुर", "Hetauda DPO", "जिल्ला हुलाक कार्यालय, हेटौंडा", "44100", true),
        PostalEntry("Kavrepalanchok", "काभ्रेपलाञ्चोक", "Dhulikhel DPO", "जिल्ला हुलाक कार्यालय, धुलिखेल", "45200", true),
        PostalEntry("Kavrepalanchok", "काभ्रेपलाञ्चोक", "Banepa", "बनेपा", "45210"),
        PostalEntry("Sindhupalchok", "सिन्धुपाल्चोक", "Chautara DPO", "जिल्ला हुलाक कार्यालय, चौतारा", "45300", true),
        PostalEntry("Nuwakot", "नुवाकोट", "Bidur DPO", "जिल्ला हुलाक कार्यालय, विदुर", "44900", true),
        PostalEntry("Dhading", "धादिङ", "Dhadingbesi DPO", "जिल्ला हुलाक कार्यालय, धादिङबेसी", "45100", true),
        PostalEntry("Ramechhap", "रामेछाप", "Manthali DPO", "जिल्ला हुलाक कार्यालय, मन्थली", "45400", true),
        PostalEntry("Dolakha", "दोलखा", "Charikot DPO", "जिल्ला हुलाक कार्यालय, चरिकोट", "45500", true),
        PostalEntry("Sindhuli", "सिन्धुली", "Sindhulimadhi DPO", "जिल्ला हुलाक कार्यालय, सिन्धुलीमाढी", "45900", true),
        PostalEntry("Rasuwa", "रसुवा", "Dhunche DPO", "जिल्ला हुलाक कार्यालय, धुन्चे", "45000", true),

        // Gandaki Province
        PostalEntry("Kaski", "कास्की", "Pokhara DPO", "जिल्ला हुलाक कार्यालय, पोखरा", "33700", true),
        PostalEntry("Kaski", "कास्की", "Lekhnath", "लेखनाथ", "33706"),
        PostalEntry("Tanahun", "तनहुँ", "Damauli DPO", "जिल्ला हुलाक कार्यालय, दमौली", "33900", true),
        PostalEntry("Syangja", "स्याङ्जा", "Syangja DPO", "जिल्ला हुलाक कार्यालय, स्याङ्जा", "33800", true),
        PostalEntry("Gorkha", "गोरखा", "Gorkha DPO", "जिल्ला हुलाक कार्यालय, गोरखा", "34000", true),
        PostalEntry("Lamjung", "लमजुङ", "Besisahar DPO", "जिल्ला हुलाक कार्यालय, बेसीसहर", "33600", true),
        PostalEntry("Baglung", "बागलुङ", "Baglung DPO", "जिल्ला हुलाक कार्यालय, बागलुङ", "33300", true),
        PostalEntry("Parbat", "पर्वत", "Kusma DPO", "जिल्ला हुलाक कार्यालय, कुश्मा", "33400", true),
        PostalEntry("Myagdi", "म्याग्दी", "Beni DPO", "जिल्ला हुलाक कार्यालय, बेनी", "33200", true),
        PostalEntry("Nawalpur", "नवलपुर", "Kawasoti DPO", "जिल्ला हुलाक कार्यालय, कावासोती", "33000", true),
        PostalEntry("Mustang", "मुस्ताङ", "Jomsom DPO", "जिल्ला हुलाक कार्यालय, जोमसोम", "33100", true),
        PostalEntry("Manang", "मनाङ", "Chame DPO", "जिल्ला हुलाक कार्यालय, चामे", "33500", true),

        // Lumbini Province
        PostalEntry("Rupandehi", "रुपन्देही", "Bhairahawa DPO", "जिल्ला हुलाक कार्यालय, भैरहवा", "32900", true),
        PostalEntry("Rupandehi", "रुपन्देही", "Butwal", "बुटवल", "32907"),
        PostalEntry("Kapilvastu", "कपिलवस्तु", "Taulihawa DPO", "जिल्ला हुलाक कार्यालय, तौलिहवा", "32800", true),
        PostalEntry("Banke", "बाँके", "Nepalgunj DPO", "जिल्ला हुलाक कार्यालय, नेपालगञ्ज", "21900", true),
        PostalEntry("Bardiya", "बर्दिया", "Gulariya DPO", "जिल्ला हुलाक कार्यालय, गुलरिया", "21800", true),
        PostalEntry("Dang", "दाङ", "Ghorahi DPO", "जिल्ला हुलाक कार्यालय, घोराही", "22400", true),
        PostalEntry("Dang", "दाङ", "Tulsipur", "तुलसीपुर", "22412"),
        PostalEntry("Palpa", "पाल्पा", "Tansen DPO", "जिल्ला हुलाक कार्यालय, तानसेन", "32500", true),
        PostalEntry("Gulmi", "गुल्मी", "Tamghas DPO", "जिल्ला हुलाक कार्यालय, तम्घास", "32600", true),
        PostalEntry("Arghakhanchi", "अर्घाखाँची", "Sandhikharka DPO", "जिल्ला हुलाक कार्यालय, सन्धिखर्क", "32700", true),
        PostalEntry("Pyuthan", "प्युठान", "Pyuthan DPO", "जिल्ला हुलाक कार्यालय, प्युठान", "22300", true),
        PostalEntry("Rolpa", "रोल्पा", "Liwang DPO", "जिल्ला हुलाक कार्यालय, लिवाङ", "22100", true),
        PostalEntry("Rukum East", "पूर्वी रुकुम", "Rukumkot DPO", "जिल्ला हुलाक कार्यालय, रुकुमकोट", "22000", true),
        PostalEntry("Parasi", "पश्चिम नवलपरासी", "Ramgram DPO", "जिल्ला हुलाक कार्यालय, रामग्राम", "33016", true),

        // Karnali Province
        PostalEntry("Surkhet", "सुर्खेत", "Birendranagar DPO", "जिल्ला हुलाक कार्यालय, वीरेन्द्रनगर", "21700", true),
        PostalEntry("Dailekh", "दैलेख", "Dailekh DPO", "जिल्ला हुलाक कार्यालय, दैलेख", "21600", true),
        PostalEntry("Jajarkot", "जाजरकोट", "Khalanga DPO", "जिल्ला हुलाक कार्यालय, खलङ्गा", "21500", true),
        PostalEntry("Salyan", "सल्यान", "Salyan DPO", "जिल्ला हुलाक कार्यालय, सल्यान", "22200", true),
        PostalEntry("Rukum West", "पश्चिम रुकुम", "Musikot DPO", "जिल्ला हुलाक कार्यालय, मुसीकोट", "22005", true),
        PostalEntry("Jumla", "जुम्ला", "Jumla DPO", "जिल्ला हुलाक कार्यालय, जुम्ला", "21200", true),
        PostalEntry("Kalikot", "कालिकोट", "Manma DPO", "जिल्ला हुलाक कार्यालय, मान्म", "21300", true),
        PostalEntry("Mugu", "मुगु", "Gamgadhi DPO", "जिल्ला हुलाक कार्यालय, गमगढी", "21100", true),
        PostalEntry("Humla", "हुम्ला", "Simikot DPO", "जिल्ला हुलाक कार्यालय, सिमिकोट", "21000", true),
        PostalEntry("Dolpa", "डोल्पा", "Dunai DPO", "जिल्ला हुलाक कार्यालय, दुनै", "21400", true),

        // Sudurpashchim Province
        PostalEntry("Kailali", "कैलाली", "Dhangadhi DPO", "जिल्ला हुलाक कार्यालय, धनगढी", "10900", true),
        PostalEntry("Kailali", "कैलाली", "Tikapur", "टिकापुर", "10901"),
        PostalEntry("Kanchanpur", "कञ्चनपुर", "Mahendranagar DPO", "जिल्ला हुलाक कार्यालय, महेन्द्रनगर", "10400", true),
        PostalEntry("Dadeldhura", "डडेल्धुरा", "Dadeldhura DPO", "जिल्ला हुलाक कार्यालय, डडेल्धुरा", "10500", true),
        PostalEntry("Doti", "डोटी", "Silgadhi DPO", "जिल्ला हुलाक कार्यालय, सिलगढी", "10600", true),
        PostalEntry("Achham", "अछाम", "Mangalsen DPO", "जिल्ला हुलाक कार्यालय, मंगलसेन", "10700", true),
        PostalEntry("Bajhang", "बझाङ", "Chainpur DPO", "जिल्ला हुलाक कार्यालय, चैनपुर", "10800", true),
        PostalEntry("Bajura", "बाजुरा", "Martadi DPO", "जिल्ला हुलाक कार्यालय, मार्तडी", "10100", true),
        PostalEntry("Baitadi", "बैतडी", "Gothalapani DPO", "जिल्ला हुलाक कार्यालय, गोठालापानी", "10200", true),
        PostalEntry("Darchula", "दार्चुला", "Khalanga DPO", "जिल्ला हुलाक कार्यालय, खलङ्गा", "10300", true)
    )

    fun search(query: String): List<PostalEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return entries
        return entries.filter {
            it.district.lowercase().contains(q) ||
            it.districtNp.lowercase().contains(q) ||
            it.postOffice.lowercase().contains(q) ||
            it.postOfficeNp.lowercase().contains(q) ||
            it.code.contains(q)
        }
    }
}
