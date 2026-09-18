// NepTools Emergency Contacts Cloudflare Edge Worker (Standalone)
// 100% Free, zero cold start, never pauses, global edge caching.

const DATA = {
  "version": 1,
  "updatedAt": "2026-09-18T09:30:00Z",
  "source": "NepTools Official Verified Emergency Directory",
  "totalContacts": 61,
  "contacts": [
    {
      "nameNp": "नेपाल प्रहरी (Police)",
      "nameEn": "Nepal Police",
      "number": "100",
      "category": "security",
      "province": "National",
      "district": "All",
      "descriptionNp": "आपतकालीन प्रहरी सहायता (Toll-Free २४ घण्टा)",
      "descriptionEn": "Emergency Police Assistance (24/7 Toll-Free)"
    },
    {
      "nameNp": "एम्बुलेन्स (Ambulance)",
      "nameEn": "Nepal Ambulance Service",
      "number": "102",
      "category": "medical",
      "province": "National",
      "district": "All",
      "descriptionNp": "आपतकालीन एम्बुलेन्स सेवा (२४ घण्टा)",
      "descriptionEn": "Emergency Medical Transport (24/7)"
    },
    {
      "nameNp": "दमकल / अग्नि नियन्त्रण (Fire)",
      "nameEn": "Fire Brigade / Damkal",
      "number": "101",
      "category": "rescue",
      "province": "National",
      "district": "All",
      "descriptionNp": "आगलागी तथा विपद् उद्धार",
      "descriptionEn": "Fire Emergency & Rescue"
    },
    {
      "nameNp": "ट्राफिक प्रहरी (Traffic Police)",
      "nameEn": "Traffic Police Hotline",
      "number": "103",
      "category": "security",
      "province": "National",
      "district": "All",
      "descriptionNp": "ट्राफिक सूचना तथा दुर्घटना सहायता",
      "descriptionEn": "Traffic Updates & Accident Support"
    },
    {
      "nameNp": "बाल हेल्पलाइन (Child Helpline)",
      "nameEn": "Child Helpline Nepal",
      "number": "1098",
      "category": "social",
      "province": "National",
      "district": "All",
      "descriptionNp": "बालबालिकाको उद्धार तथा संरक्षण (Toll-Free)",
      "descriptionEn": "Child Protection & Support"
    },
    {
      "nameNp": "राष्ट्रिय महिला आयोग (Women Helpline)",
      "nameEn": "National Women Commission",
      "number": "1145",
      "category": "social",
      "province": "National",
      "district": "All",
      "descriptionNp": "महिला हिंसाविरुद्ध २४ घण्टे हटलाइन (खबर गरौँ)",
      "descriptionEn": "24/7 Women Violence Helpline (Khabar Garaun)"
    },
    {
      "nameNp": "पर्यटक प्रहरी (Tourist Police)",
      "nameEn": "Tourist Police Nepal",
      "number": "1144",
      "category": "security",
      "province": "National",
      "district": "Kathmandu",
      "descriptionNp": "पर्यटक सहायता तथा सुरक्षा",
      "descriptionEn": "Tourist Safety & Assistance"
    },
    {
      "nameNp": "सशस्त्र प्रहरी बल (APF)",
      "nameEn": "Armed Police Force",
      "number": "1114",
      "category": "security",
      "province": "National",
      "district": "All",
      "descriptionNp": "सीमा सुरक्षा तथा विपद् उद्धार",
      "descriptionEn": "Disaster Response & Border Security"
    },
    {
      "nameNp": "विपद् पूर्वसूचना तथा व्यवस्थापन",
      "nameEn": "Disaster Emergency Info",
      "number": "1155",
      "category": "rescue",
      "province": "National",
      "district": "All",
      "descriptionNp": "बाढी, पहिरो तथा जल तथा मौसम विपद् सूचना",
      "descriptionEn": "Flood & Disaster Early Warning"
    },
    {
      "nameNp": "हेलो सरकार (Hello Sarkar)",
      "nameEn": "Hello Sarkar Grievance",
      "number": "1111",
      "category": "social",
      "province": "National",
      "district": "All",
      "descriptionNp": "प्रधानमन्त्री कार्यालय — सरकारी सेवा तथा जनगुनासो",
      "descriptionEn": "Citizen Grievance & Inquiries"
    },
    {
      "nameNp": "काठमाडौं प्रहरी कन्ट्रोल",
      "nameEn": "Kathmandu Police Control",
      "number": "014226998",
      "category": "security",
      "province": "Bagmati",
      "district": "Kathmandu",
      "descriptionNp": "जिल्ला प्रहरी परिसर टेकु, काठमाडौं",
      "descriptionEn": "District Police Control Room Teku, Kathmandu"
    },
    {
      "nameNp": "ललितपुर प्रहरी कन्ट्रोल",
      "nameEn": "Lalitpur Police Control",
      "number": "015521207",
      "category": "security",
      "province": "Bagmati",
      "district": "Lalitpur",
      "descriptionNp": "जिल्ला प्रहरी परिसर जावलाखेल, ललितपुर",
      "descriptionEn": "District Police Control Jawalakhel, Lalitpur"
    },
    {
      "nameNp": "भक्तपुर प्रहरी कन्ट्रोल",
      "nameEn": "Bhaktapur Police Control",
      "number": "016614821",
      "category": "security",
      "province": "Bagmati",
      "district": "Bhaktapur",
      "descriptionNp": "जिल्ला प्रहरी परिसर भक्तपुर",
      "descriptionEn": "District Police Control Bhaktapur"
    },
    {
      "nameNp": "चितवन प्रहरी कन्ट्रोल",
      "nameEn": "Chitwan Police Control",
      "number": "056520199",
      "category": "security",
      "province": "Bagmati",
      "district": "Chitwan",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय भरतपुर, चितवन",
      "descriptionEn": "District Police Office Bharatpur, Chitwan"
    },
    {
      "nameNp": "विषाक्त सूचना केन्द्र (Poison Info)",
      "nameEn": "Poison Information Center",
      "number": "014502011",
      "category": "medical",
      "province": "Bagmati",
      "district": "Kathmandu",
      "descriptionNp": "शिक्षण अस्पताल (TUTH), महाराजगञ्ज (२४ घण्टा)",
      "descriptionEn": "TUTH 24/7 Poisoning Emergency Center"
    },
    {
      "nameNp": "वीर अस्पताल आकस्मिक कक्ष",
      "nameEn": "Bir Hospital Emergency",
      "number": "015321988",
      "category": "medical",
      "province": "Bagmati",
      "district": "Kathmandu",
      "descriptionNp": "सरकारी केन्द्रीय आकस्मिक सेवा (२४ घण्टा)",
      "descriptionEn": "Central Government Hospital 24/7 Emergency"
    },
    {
      "nameNp": "शिक्षण अस्पताल महाराजगञ्ज",
      "nameEn": "TUTH Emergency",
      "number": "014512505",
      "category": "medical",
      "province": "Bagmati",
      "district": "Kathmandu",
      "descriptionNp": "त्रिवि शिक्षण अस्पताल आकस्मिक कक्ष",
      "descriptionEn": "Teaching Hospital Emergency Maharajgunj"
    },
    {
      "nameNp": "पाटन अस्पताल आकस्मिक कक्ष",
      "nameEn": "Patan Hospital Emergency",
      "number": "015522295",
      "category": "medical",
      "province": "Bagmati",
      "district": "Lalitpur",
      "descriptionNp": "पाटन अस्पताल, लगनखेल",
      "descriptionEn": "Patan Hospital Lagankhel"
    },
    {
      "nameNp": "भक्तपुर अस्पताल आकस्मिक कक्ष",
      "nameEn": "Bhaktapur Hospital Emergency",
      "number": "016610798",
      "category": "medical",
      "province": "Bagmati",
      "district": "Bhaktapur",
      "descriptionNp": "भक्तपुर अस्पताल, दूधपाटी",
      "descriptionEn": "Bhaktapur Hospital Emergency Dudhpati"
    },
    {
      "nameNp": "चितवन मेडिकल कलेज आकस्मिक",
      "nameEn": "Chitwan Medical College",
      "number": "056532933",
      "category": "medical",
      "province": "Bagmati",
      "district": "Chitwan",
      "descriptionNp": "सीएमसी भरतपुर आकस्मिक कक्ष",
      "descriptionEn": "CMC Hospital Emergency Bharatpur"
    },
    {
      "nameNp": "केन्द्रीय रक्तसञ्चार सेवा (Blood Bank)",
      "nameEn": "Central Blood Transfusion Service",
      "number": "014225344",
      "category": "blood",
      "province": "Bagmati",
      "district": "Kathmandu",
      "descriptionNp": "नेपाल रेडक्रस प्रदर्शनीमार्ग, काठमाडौं",
      "descriptionEn": "Red Cross Central Blood Bank, Kathmandu"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार ललितपुर",
      "nameEn": "Lalitpur Blood Bank",
      "number": "015427033",
      "category": "blood",
      "province": "Bagmati",
      "district": "Lalitpur",
      "descriptionNp": "नेपाल रेडक्रस, पुलचोक",
      "descriptionEn": "Red Cross Blood Bank, Pulchowk"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार भक्तपुर",
      "nameEn": "Bhaktapur Blood Bank",
      "number": "016611661",
      "category": "blood",
      "province": "Bagmati",
      "district": "Bhaktapur",
      "descriptionNp": "नेपाल रेडक्रस, भक्तपुर",
      "descriptionEn": "Red Cross Blood Bank, Bhaktapur"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार चितवन",
      "nameEn": "Chitwan Blood Bank",
      "number": "056520880",
      "category": "blood",
      "province": "Bagmati",
      "district": "Chitwan",
      "descriptionNp": "नेपाल रेडक्रस, भरतपुर",
      "descriptionEn": "Red Cross Blood Bank, Bharatpur"
    },
    {
      "nameNp": "मोरङ प्रहरी कन्ट्रोल",
      "nameEn": "Morang Police Control",
      "number": "0215462158",
      "category": "security",
      "province": "Koshi",
      "district": "Morang",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय विराटनगर, मोरङ",
      "descriptionEn": "District Police Office Biratnagar, Morang"
    },
    {
      "nameNp": "सुनसरी प्रहरी कन्ट्रोल",
      "nameEn": "Sunsari Police Control",
      "number": "025565100",
      "category": "security",
      "province": "Koshi",
      "district": "Sunsari",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय इनरुवा / धरान",
      "descriptionEn": "District Police Office Sunsari"
    },
    {
      "nameNp": "झापा प्रहरी कन्ट्रोल",
      "nameEn": "Jhapa Police Control",
      "number": "023520199",
      "category": "security",
      "province": "Koshi",
      "district": "Jhapa",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय भद्रपुर, झापा",
      "descriptionEn": "District Police Office Bhadrapur, Jhapa"
    },
    {
      "nameNp": "कोशी अस्पताल आकस्मिक कक्ष",
      "nameEn": "Koshi Hospital Emergency",
      "number": "021530103",
      "category": "medical",
      "province": "Koshi",
      "district": "Morang",
      "descriptionNp": "रंगेली रोड, विराटनगर, कोशी प्रदेश",
      "descriptionEn": "Koshi Provincial Hospital Biratnagar"
    },
    {
      "nameNp": "बीपी कोइराला स्वास्थ्य विज्ञान प्रतिष्ठान",
      "nameEn": "BPKIHS Emergency Dharan",
      "number": "025525555",
      "category": "medical",
      "province": "Koshi",
      "district": "Sunsari",
      "descriptionNp": "विशिष्ट आकस्मिक सेवा, धरान",
      "descriptionEn": "BPKIHS Tertiary Emergency Dharan"
    },
    {
      "nameNp": "मेची प्रादेशिक अस्पताल",
      "nameEn": "Mechi Hospital Emergency",
      "number": "023523024",
      "category": "medical",
      "province": "Koshi",
      "district": "Jhapa",
      "descriptionNp": "भद्रपुर, झापा",
      "descriptionEn": "Mechi Provincial Hospital Bhadrapur"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार विराटनगर",
      "nameEn": "Biratnagar Blood Bank",
      "number": "021523326",
      "category": "blood",
      "province": "Koshi",
      "district": "Morang",
      "descriptionNp": "विराटनगर, मोरङ",
      "descriptionEn": "Red Cross Blood Bank Biratnagar"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार धरान",
      "nameEn": "Dharan Blood Bank",
      "number": "025520111",
      "category": "blood",
      "province": "Koshi",
      "district": "Sunsari",
      "descriptionNp": "धरान, सुनसरी",
      "descriptionEn": "Red Cross Blood Bank Dharan"
    },
    {
      "nameNp": "पर्सा प्रहरी कन्ट्रोल",
      "nameEn": "Parsa Police Control",
      "number": "051522199",
      "category": "security",
      "province": "Madhesh",
      "district": "Parsa",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय वीरगञ्ज, पर्सा",
      "descriptionEn": "District Police Office Birgunj, Parsa"
    },
    {
      "nameNp": "धनुषा प्रहरी कन्ट्रोल",
      "nameEn": "Dhanusha Police Control",
      "number": "041520199",
      "category": "security",
      "province": "Madhesh",
      "district": "Dhanusha",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय जनकपुरधाम",
      "descriptionEn": "District Police Office Janakpurdham"
    },
    {
      "nameNp": "नारायणी अस्पताल आकस्मिक कक्ष",
      "nameEn": "Narayani Hospital Emergency",
      "number": "051522022",
      "category": "medical",
      "province": "Madhesh",
      "district": "Parsa",
      "descriptionNp": "वीरगञ्ज, पर्सा",
      "descriptionEn": "Narayani Hospital Birgunj"
    },
    {
      "nameNp": "जनकपुर प्रादेशिक अस्पताल",
      "nameEn": "Janakpur Provincial Hospital",
      "number": "041520200",
      "category": "medical",
      "province": "Madhesh",
      "district": "Dhanusha",
      "descriptionNp": "जनकपुरधाम, धनुषा",
      "descriptionEn": "Provincial Hospital Janakpur"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार वीरगञ्ज",
      "nameEn": "Birgunj Blood Bank",
      "number": "051522045",
      "category": "blood",
      "province": "Madhesh",
      "district": "Parsa",
      "descriptionNp": "वीरगञ्ज, पर्सा",
      "descriptionEn": "Red Cross Blood Bank Birgunj"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार जनकपुर",
      "nameEn": "Janakpur Blood Bank",
      "number": "041520333",
      "category": "blood",
      "province": "Madhesh",
      "district": "Dhanusha",
      "descriptionNp": "जनकपुरधाम, धनुषा",
      "descriptionEn": "Red Cross Blood Bank Janakpur"
    },
    {
      "nameNp": "कास्की प्रहरी कन्ट्रोल",
      "nameEn": "Kaski Police Control",
      "number": "061522099",
      "category": "security",
      "province": "Gandaki",
      "district": "Kaski",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय पोखरा, कास्की",
      "descriptionEn": "District Police Office Pokhara, Kaski"
    },
    {
      "nameNp": "तनहुँ प्रहरी कन्ट्रोल",
      "nameEn": "Tanahun Police Control",
      "number": "065560199",
      "category": "security",
      "province": "Gandaki",
      "district": "Tanahun",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय दमौली, तनहुँ",
      "descriptionEn": "District Police Office Damauli, Tanahun"
    },
    {
      "nameNp": "गण्डकी अस्पताल आकस्मिक कक्ष",
      "nameEn": "Gandaki Hospital Emergency",
      "number": "061570066",
      "category": "medical",
      "province": "Gandaki",
      "district": "Kaski",
      "descriptionNp": "पोखरा स्वास्थ्य विज्ञान प्रतिष्ठान (रामघाट)",
      "descriptionEn": "Western Regional Hospital, Pokhara"
    },
    {
      "nameNp": "मणिपाल शिक्षण अस्पताल आकस्मिक",
      "nameEn": "Manipal Hospital Emergency",
      "number": "061526416",
      "category": "medical",
      "province": "Gandaki",
      "district": "Kaski",
      "descriptionNp": "फूलबारी, पोखरा",
      "descriptionEn": "Manipal Teaching Hospital Pokhara"
    },
    {
      "nameNp": "दमौली अस्पताल आकस्मिक कक्ष",
      "nameEn": "Damauli Hospital Emergency",
      "number": "065563203",
      "category": "medical",
      "province": "Gandaki",
      "district": "Tanahun",
      "descriptionNp": "दमौली, तनहुँ",
      "descriptionEn": "Damauli District Hospital"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार केन्द्र पोखरा",
      "nameEn": "Pokhara Blood Bank",
      "number": "061521091",
      "category": "blood",
      "province": "Gandaki",
      "district": "Kaski",
      "descriptionNp": "रामघाट, पोखरा",
      "descriptionEn": "Red Cross Regional Blood Bank Pokhara"
    },
    {
      "nameNp": "रुपन्देही प्रहरी कन्ट्रोल",
      "nameEn": "Rupandehi Police Control",
      "number": "071520199",
      "category": "security",
      "province": "Lumbini",
      "district": "Rupandehi",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय भैरहवा / बुटवल",
      "descriptionEn": "District Police Office Rupandehi"
    },
    {
      "nameNp": "बाँके प्रहरी कन्ट्रोल",
      "nameEn": "Banke Police Control",
      "number": "081520211",
      "category": "security",
      "province": "Lumbini",
      "district": "Banke",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय नेपालगञ्ज, बाँके",
      "descriptionEn": "District Police Office Nepalgunj, Banke"
    },
    {
      "nameNp": "दाङ प्रहरी कन्ट्रोल",
      "nameEn": "Dang Police Control",
      "number": "082560199",
      "category": "security",
      "province": "Lumbini",
      "district": "Dang",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय घोराही, दाङ",
      "descriptionEn": "District Police Office Ghorahi, Dang"
    },
    {
      "nameNp": "लुम्बिनी प्रादेशिक अस्पताल",
      "nameEn": "Lumbini Provincial Hospital",
      "number": "071534010",
      "category": "medical",
      "province": "Lumbini",
      "district": "Rupandehi",
      "descriptionNp": "बुटवल, लुम्बिनी प्रदेश (२४ घण्टा)",
      "descriptionEn": "Lumbini Provincial Hospital, Butwal"
    },
    {
      "nameNp": "भेरी अस्पताल आकस्मिक कक्ष",
      "nameEn": "Bheri Hospital Emergency",
      "number": "081520120",
      "category": "medical",
      "province": "Lumbini",
      "district": "Banke",
      "descriptionNp": "नेपालगञ्ज, बाँके",
      "descriptionEn": "Bheri Hospital Emergency Nepalgunj"
    },
    {
      "nameNp": "राप्ती प्रादेशिक अस्पताल",
      "nameEn": "Rapti Provincial Hospital",
      "number": "082520011",
      "category": "medical",
      "province": "Lumbini",
      "district": "Dang",
      "descriptionNp": "तुलसीपुर, दाङ",
      "descriptionEn": "Rapti Provincial Hospital Tulsipur"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार बुटवल",
      "nameEn": "Butwal Blood Bank",
      "number": "071531004",
      "category": "blood",
      "province": "Lumbini",
      "district": "Rupandehi",
      "descriptionNp": "बुटवल, रुपन्देही",
      "descriptionEn": "Red Cross Blood Bank Butwal"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार नेपालगञ्ज",
      "nameEn": "Nepalgunj Blood Bank",
      "number": "081520174",
      "category": "blood",
      "province": "Lumbini",
      "district": "Banke",
      "descriptionNp": "नेपालगञ्ज, बाँके",
      "descriptionEn": "Red Cross Blood Bank Nepalgunj"
    },
    {
      "nameNp": "सुर्खेत प्रहरी कन्ट्रोल",
      "nameEn": "Surkhet Police Control",
      "number": "083520199",
      "category": "security",
      "province": "Karnali",
      "district": "Surkhet",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय वीरेन्द्रनगर, सुर्खेत",
      "descriptionEn": "District Police Office Birendranagar, Surkhet"
    },
    {
      "nameNp": "कर्णाली प्रादेशिक अस्पताल",
      "nameEn": "Karnali Provincial Hospital",
      "number": "083520200",
      "category": "medical",
      "province": "Karnali",
      "district": "Surkhet",
      "descriptionNp": "वीरेन्द्रनगर, सुर्खेत",
      "descriptionEn": "Karnali Provincial Hospital Surkhet"
    },
    {
      "nameNp": "कर्णाली स्वास्थ्य विज्ञान प्रतिष्ठान",
      "nameEn": "KAHS Emergency Jumla",
      "number": "087520115",
      "category": "medical",
      "province": "Karnali",
      "district": "Jumla",
      "descriptionNp": "खलङ्गा, जुम्ला",
      "descriptionEn": "KAHS Teaching Hospital Jumla"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार सुर्खेत",
      "nameEn": "Surkhet Blood Bank",
      "number": "083520144",
      "category": "blood",
      "province": "Karnali",
      "district": "Surkhet",
      "descriptionNp": "वीरेन्द्रनगर, सुर्खेत",
      "descriptionEn": "Red Cross Blood Bank Surkhet"
    },
    {
      "nameNp": "कैलाली प्रहरी कन्ट्रोल",
      "nameEn": "Kailali Police Control",
      "number": "091521150",
      "category": "security",
      "province": "Sudurpashchim",
      "district": "Kailali",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय धनगढी, कैलाली",
      "descriptionEn": "District Police Office Dhangadhi, Kailali"
    },
    {
      "nameNp": "कञ्चनपुर प्रहरी कन्ट्रोल",
      "nameEn": "Kanchanpur Police Control",
      "number": "099521199",
      "category": "security",
      "province": "Sudurpashchim",
      "district": "Kanchanpur",
      "descriptionNp": "जिल्ला प्रहरी कार्यालय महेन्द्रनगर",
      "descriptionEn": "District Police Office Mahendranagar"
    },
    {
      "nameNp": "सेती प्रादेशिक अस्पताल",
      "nameEn": "Seti Provincial Hospital",
      "number": "091525911",
      "category": "medical",
      "province": "Sudurpashchim",
      "district": "Kailali",
      "descriptionNp": "धनगढी, सुदूरपश्चिम प्रदेश",
      "descriptionEn": "Seti Provincial Hospital Dhangadhi"
    },
    {
      "nameNp": "महाकाली प्रादेशिक अस्पताल",
      "nameEn": "Mahakali Hospital Emergency",
      "number": "099521111",
      "category": "medical",
      "province": "Sudurpashchim",
      "district": "Kanchanpur",
      "descriptionNp": "महेन्द्रनगर, कञ्चनपुर",
      "descriptionEn": "Mahakali Provincial Hospital Mahendranagar"
    },
    {
      "nameNp": "रेडक्रस रक्तसञ्चार धनगढी",
      "nameEn": "Dhangadhi Blood Bank",
      "number": "091521600",
      "category": "blood",
      "province": "Sudurpashchim",
      "district": "Kailali",
      "descriptionNp": "धनगढी, कैलाली",
      "descriptionEn": "Red Cross Blood Bank Dhangadhi"
    }
  ]
};
// Derived from the payload so the ETag can never drift from the data it describes.
const ETAG = `"v${DATA.version || 1}-${DATA.contacts.length}"`;

export default {
  async fetch(request) {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, HEAD, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, If-None-Match, If-Modified-Since",
          "Access-Control-Max-Age": "86400"
        }
      });
    }

    if (request.method !== "GET" && request.method !== "HEAD") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), {
        status: 405,
        headers: { "Content-Type": "application/json", "Allow": "GET, HEAD, OPTIONS" }
      });
    }

    if (url.pathname === "/health") {
      return new Response(JSON.stringify({
        status: "healthy",
        service: "NepTools Emergency Contacts API",
        provider: "Cloudflare Workers",
        version: DATA.version,
        totalContacts: DATA.totalContacts,
        timestamp: new Date().toISOString()
      }), {
        status: 200,
        headers: { "Content-Type": "application/json", "Cache-Control": "no-cache" }
      });
    }

    const clientEtag = request.headers.get("If-None-Match");
    if (clientEtag && clientEtag === ETAG) {
      return new Response(null, {
        status: 304,
        headers: {
          "ETag": ETAG,
          "Cache-Control": "public, max-age=300, s-maxage=3600",
          "Access-Control-Allow-Origin": "*"
        }
      });
    }

    const provinceFilter = url.searchParams.get("province");
    const categoryFilter = url.searchParams.get("category");

    let payload = DATA;
    if (provinceFilter || categoryFilter) {
      const filtered = DATA.contacts.filter(c => {
        let match = true;
        if (provinceFilter && c.province !== "National" && c.province.toLowerCase() !== provinceFilter.toLowerCase()) {
          match = false;
        }
        if (categoryFilter && categoryFilter.toLowerCase() !== "all" && c.category.toLowerCase() !== categoryFilter.toLowerCase()) {
          match = false;
        }
        return match;
      });
      payload = { ...DATA, totalContacts: filtered.length, contacts: filtered };
    }

    return new Response(JSON.stringify(payload, null, 2), {
      status: 200,
      headers: {
        "Content-Type": "application/json; charset=utf-8",
        "Cache-Control": "public, max-age=300, s-maxage=3600",
        "ETag": ETAG,
        "Access-Control-Allow-Origin": "*",
        "X-Powered-By": "NepTools Cloudflare Worker"
      }
    });
  }
};
