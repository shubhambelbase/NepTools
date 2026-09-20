package com.neptools.app.core.data

data class TemplateField(
    val key: String,
    val labelNp: String,
    val labelEn: String,
    val hintNp: String,
    val hintEn: String,
    val isNumber: Boolean = false
)

data class AppTemplate(
    val id: String,
    val titleNp: String,
    val titleEn: String,
    val category: String, // "ward", "legal", "bank", "leave"
    val descriptionNp: String,
    val descriptionEn: String,
    val recipientNp: String,
    val recipientEn: String,
    val subjectNp: String,
    val subjectEn: String,
    val fields: List<TemplateField>,
    val generateLetter: (values: Map<String, String>) -> String
)

/**
 * Standard administrative application templates compliant with:
 * - Local Government Operation Act, 2074 (स्थानीय सरकार सञ्चालन ऐन, २०७४)
 * - Nepal Citizenship Act 2063 & Rules (नेपाल नागरिकता ऐन, २०६३ र नियमावली)
 * - Muluki Civil Code, 2074 (मुलुकी देवानी संहिता, २०७४)
 * - Vital Event Registration Act, 2076 (व्यक्तिगत घटना दर्ता ऐन, २०७६)
 * - Nepal Police Lost Document SOP (नेपाल प्रहरी खोजतलास सिफारिस कार्यविधि)
 * - Nepal Rastra Bank Unified Banking Directives (नेपाल राष्ट्र बैंक एकीकृत निर्देशन)
 */
object ApplicationTemplatesRepo {
    val templates = listOf(
        // 1. Citizenship Recommendation (नागरिकता सिफारिस निवेदन)
        AppTemplate(
            id = "citizenship",
            titleNp = "नागरिकता सिफारिस निवेदन",
            titleEn = "Citizenship Recommendation",
            category = "ward",
            descriptionNp = "नयाँ नागरिकता प्रमाणपत्र लिन वडा कार्यालयमा दिइने सिफारिस निवेदन",
            descriptionEn = "Application to Ward office for citizenship certificate recommendation",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "नेपाली नागरिकता प्रमाणपत्रको सिफारिस पाउँ।",
            subjectEn = "Application for Citizenship Recommendation Certificate.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "निवेदकको पूरा नाम", "Applicant Full Name", "उदा: सन्तोष श्रेष्ठ", "e.g. Santosh Shrestha"),
                TemplateField("dob", "जन्म मिति (वि.सं.)", "Date of Birth (B.S.)", "उदा: २०६२/०५/१५", "e.g. 2062/05/15"),
                TemplateField("grandfather_name", "बाजेको पूरा नाम", "Grandfather's Full Name", "उदा: कृष्ण बहादुर श्रेष्ठ", "e.g. Krishna Bahadur Shrestha"),
                TemplateField("father_name", "बुबाको पूरा नाम", "Father's Full Name", "उदा: राम बहादुर श्रेष्ठ", "e.g. Ram Bahadur Shrestha"),
                TemplateField("mother_name", "आमाको पूरा नाम", "Mother's Full Name", "उदा: सीता देवी श्रेष्ठ", "e.g. Sita Devi Shrestha"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality / Rural Muni", "उदा: काठमाडौँ महानगरपालिका", "e.g. Kathmandu Metro"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: १०", "e.g. 10", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: काठमाडौँ", "e.g. Kathmandu"),
                TemplateField("phone", "सम्पर्क मोबाइल नं.", "Contact Phone No.", "उदा: ९८४१२३४५६७", "e.g. 9841234567", isNumber = true)
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val dob = v["dob"]?.ifBlank { "..........................." } ?: "..........................."
                val grandfather = v["grandfather_name"]?.ifBlank { "..........................." } ?: "..........................."
                val father = v["father_name"]?.ifBlank { "..........................." } ?: "..........................."
                val mother = v["mother_name"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: नेपाली नागरिकता प्रमाणपत्रको सिफारिस पाउँ।

महोदय,
उपरोक्त विषयमा म निवेदक यस $muni वडा नं. $ward को स्थायी बासिन्दा श्री $grandfather को नाति/नातिना, श्री $father तथा श्रीमती $mother को कोखबाट मिति $dob मा जन्म भई १६ वर्ष उमेर पूरा गरेको नेपाली नागरिक हुँ।

मलाई वंशजको आधारमा नेपाली नागरिकताको प्रमाणपत्र आवश्यक परेको हुँदा आवश्यक अनुसूची-१ फारम तथा प्रमाण कागजातहरू यसै निवेदनसाथ संलग्न गरी पेश गरेको छु। मेरो नाममा नागरिकता प्रमाणपत्र जारी गर्न जिल्ला प्रशासन कार्यालयलाई आवश्यक वडा सिफारिस पत्र प्रदान गरिदिनुहुन सादर अनुरोध गर्दछु।

माथि उल्लिखित तीन पुस्ते विवरण तथा व्यहोरा सत्य साँचो हो, कुनै कुरा ढाँटेको वा लुकाएको छैन। झुट्टा ठहरे कानुन बमोजिम सहुँला बुझाउँला।

संलग्न प्रमाण कागजातहरू:
१. तोकिएको अनुसूची-१ नेपाली नागरिकता दरखास्त फारम (फोटोसहित)
२. जन्म दर्ता प्रमाणपत्रको प्रमाणित प्रतिलिपि
३. बाबु तथा आमाको नेपाली नागरिकता प्रमाणपत्रको प्रतिलिपि
४. शैक्षिक योग्यताको प्रमाणपत्र प्रतिलिपि (८ कक्षा वा एस.ई.ई. को चारित्रिक प्रमाणपत्र)
५. चालु आर्थिक वर्षको स्थानीय कर / सम्पत्ति कर चुक्ता रसिद
६. सनाखत गर्ने बाबु/आमा वा एकाघरका व्यक्तिको नागरिकता प्रतिलिपि

निवेदक:
नाम, थर: $name
जन्म मिति: $dob
स्थायी ठेगाना: $muni-$ward, $dist
सम्पर्क फोन नं.: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 2. Relationship Verification (नाता प्रमाणित सिफारिस निवेदन)
        AppTemplate(
            id = "nata_pramanit",
            titleNp = "नाता प्रमाणित सिफारिस निवेदन",
            titleEn = "Relationship Verification (Nata Pramanit)",
            category = "ward",
            descriptionNp = "परिवारका सदस्यहरूसँगको नाता प्रमाणित गर्न वडा कार्यालयमा दिइने निवेदन",
            descriptionEn = "Application for official relationship verification certificate from Ward",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "नाता प्रमाणित सिफारिस पाउँ।",
            subjectEn = "Application for Relationship Verification Certificate.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "निवेदकको पूरा नाम", "Applicant Full Name", "उदा: दिपेश अधिकारी", "e.g. Dipesh Adhikari"),
                TemplateField("citizenship_no", "निवेदकको नागरिकता नं.", "Applicant Citizenship No.", "उदा: २७-०१-७५-०१२३४", "e.g. 27-01-75-01234"),
                TemplateField("rel_members", "नाता प्रमाणित व्यक्तिहरू र नाता", "Members & Relationship", "उदा: बाबु: राम अधिकारी, आमा: सीता अधिकारी", "e.g. Father: Ram, Mother: Sita"),
                TemplateField("purpose", "प्रमाणित गराउनुको प्रयोजन", "Purpose of Certificate", "उदा: राहदानी / अध्ययन / बैंक / विदेश प्रयोजन", "e.g. Passport / Visa / Banking"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality", "उदा: पोखरा महानगरपालिका", "e.g. Pokhara"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ८", "e.g. 8", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: कास्की", "e.g. Kaski"),
                TemplateField("phone", "सम्पर्क फोन नं.", "Contact Phone", "उदा: ९८५६००११२२", "e.g. 9856001122")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val ctzNo = v["citizenship_no"]?.ifBlank { "..........................." } ?: "..........................."
                val relMembers = v["rel_members"]?.ifBlank { "......................................................................................" } ?: "......................................................................................"
                val purpose = v["purpose"]?.ifBlank { "अत्यावश्यक प्रशासनिक कार्य" } ?: "अत्यावश्यक प्रशासनिक कार्य"
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: नाता प्रमाणित सिफारिस पाउँ।

महोदय,
उपरोक्त विषयमा म निवेदक यस $muni वडा नं. $ward को स्थायी बासिन्दा हुँ। मलाई $purpose प्रयोजनका लागि देहाय बमोजिमका व्यक्तिहरूसँगको मेरो नाता सम्बन्ध प्रमाणित गरी आधिकारिक सिफारिस पत्र आवश्यक परेको छ।

नाता प्रमाणित गर्नुपर्ने व्यक्तिहरूको विवरण:
$relMembers

माथि उल्लिखित व्यक्तिहरू मेरो एकाघरका वास्तविक नातादार हुन्। उल्लिखित नाता सम्बन्धको विवरण सत्य तथ्य हो। कुनै कुरा ढाँटेको वा फरक परेमा कानुन बमोजिम सहुँला बुझाउँला।

अतः आवश्यक जाँचबुझ तथा सर्जमिन गरी नाता प्रमाणित सिफारिस पत्र उपलब्ध गराई दिनुहुन सादर अनुरोध गर्दछु।

संलग्न प्रमाण कागजातहरू:
१. निवेदक तथा नाता प्रमाणित हुने व्यक्तिहरूको नेपाली नागरिकताको प्रतिलिपि
२. नाबालिग सदस्यको हकमा जन्म दर्ता प्रमाणपत्रको प्रतिलिपि
३. विवाहित महिलाको हकमा विवाह दर्ता प्रमाणपत्र
४. सम्बन्धित सबै व्यक्तिको पासपोर्ट साइजको फोटो (२/२ प्रति)
५. चालु आर्थिक वर्षको स्थानीय कर चुक्ता रसिद

निवेदक:
नाम: $name
नागरिकता प्रमाणपत्र नं.: $ctzNo
स्थायी ठेगाना: $muni-$ward, $dist
सम्पर्क मोबाइल नं.: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 3. Land Transfer / Mutation (जग्गा नामसारी सिफारिस निवेदन)
        AppTemplate(
            id = "land_transfer",
            titleNp = "जग्गा नामसारी सिफारिस निवेदन",
            titleEn = "Land Transfer / Mutation",
            category = "ward",
            descriptionNp = "मृतकको नाममा रहेको जग्गा हकवालाको नाममा नामसारी गर्न वडा सिफारिस",
            descriptionEn = "Application for land inheritance and title transfer",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "हकवालाको नाममा जग्गा नामसारी सिफारिस पाउँ।",
            subjectEn = "Application for Land Inheritance/Transfer Recommendation.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "हकवाला निवेदकको नाम", "Applicant (Legal Heir)", "उदा: रमेश अधिकारी", "e.g. Ramesh Adhikari"),
                TemplateField("deceased_name", "मृतक जग्गाधनीको नाम", "Deceased Owner Name", "उदा: स्व. हरिप्रसाद अधिकारी", "e.g. Late Hari Prasad Adhikari"),
                TemplateField("death_date", "मृत्यु भएको मिति (वि.सं.)", "Date of Death (B.S.)", "उदा: २०८०/०८/१५", "e.g. 2080/08/15"),
                TemplateField("relationship", "हकवालाको नाता", "Relationship with Deceased", "उदा: छोरा / श्रीमती", "e.g. Son / Wife"),
                TemplateField("kitta_no", "कित्ता नम्बर", "Plot / Kitta Number", "उदा: ५२४", "e.g. 524"),
                TemplateField("sheet_no", "सिट नम्बर (यदि भए)", "Sheet Number", "उदा: २ 'क'", "e.g. 2 Ka"),
                TemplateField("area", "जग्गाको क्षेत्रफल", "Land Area", "उदा: ०-४-२-० (४ आना २ दाम)", "e.g. 4 Aana"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality", "उदा: पोखरा महानगरपालिका", "e.g. Pokhara"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ६", "e.g. 6", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: कास्की", "e.g. Kaski"),
                TemplateField("phone", "सम्पर्क फोन नं.", "Contact Phone", "उदा: ९८४६००००००", "e.g. 9846000000")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val deceased = v["deceased_name"]?.ifBlank { "..........................." } ?: "..........................."
                val deathDate = v["death_date"]?.ifBlank { "..........................." } ?: "..........................."
                val rel = v["relationship"]?.ifBlank { "..........................." } ?: "..........................."
                val kitta = v["kitta_no"]?.ifBlank { "..........................." } ?: "..........................."
                val sheet = v["sheet_no"]?.ifBlank { "-" } ?: "-"
                val area = v["area"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: हकवालाको नाममा जग्गा नामसारी सिफारिस पाउँ।

महोदय,
उपरोक्त विषयमा मेरा पूज्य $rel $deceased को मिति $deathDate मा स्वर्गारोहण हुनुभएको हुँदा निजको नाममा दर्ता स्रेस्ता कायम रहेको यस $muni वडा नं. $ward स्थित सिट नं. $sheet, कित्ता नं. $kitta को क्षेत्रफल $area जग्गा हकवाला म निवेदकको नाममा नामसारी गर्नुपर्ने भएको छ।

उक्त मृतकका अन्य हकवालाहरू भएमा कानुन बमोजिम मञ्जुरी लिइएको छ। मृतकको हकदाबी सम्बन्धमा पछि कुनै विवाद वा उजुरबाजुर परेमा कानुन बमोजिम म निवेदक नै पूर्ण जिम्मेवार रहनेछु।

अतः उक्त जग्गा नामसारी प्रक्रियाका लागि आवश्यक वडा कार्यालयको स्थलगत सर्जमिन मुचुल्का तथा मालपोत कार्यालयको नाममा सिफारिस पत्र उपलब्ध गराई दिनुहुन सादर अनुरोध गर्दछु।

संलग्न प्रमाण कागजातहरू:
१. मृतकको मृत्यु दर्ता प्रमाणपत्रको प्रमाणित प्रतिलिपि
२. सक्कल जग्गाधनी दर्ता प्रमाण पुर्जा (लालपुर्जा) को प्रतिलिपि
३. वडा कार्यालयबाट जारी नाता प्रमाणित प्रमाणपत्रको प्रतिलिपि
४. सम्पूर्ण हकवालाहरूको नेपाली नागरिकता प्रमाणपत्रको प्रतिलिपि
५. चालु आर्थिक वर्षको मालपोत तथा एकीकृत सम्पत्ति कर चुक्ता रसिद

निवेदक:
नाम: $name (नाता: $rel)
ठेगाना: $muni-$ward, $dist
सम्पर्क मोबाइल नं.: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 4. Four Boundaries Verification (चार किल्ला प्रमाणित निवेदन)
        AppTemplate(
            id = "four_boundaries",
            titleNp = "चार किल्ला प्रमाणित निवेदन",
            titleEn = "Four Boundaries (Char Killa)",
            category = "ward",
            descriptionNp = "जग्गाको पूर्व, पश्चिम, उत्तर, दक्षिण साँध-सिमाना प्रमाणित सिफारिस",
            descriptionEn = "Application to certify land boundary details (Char Killa)",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "चार किल्ला प्रमाणित सिफारिस गरिपाउँ।",
            subjectEn = "Application for Four Boundaries Verification.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "जग्गाधनीको नाम", "Land Owner Name", "उदा: विष्णु शर्मा", "e.g. Bishnu Sharma"),
                TemplateField("kitta_no", "कित्ता नम्बर", "Kitta / Plot No.", "उदा: १२८", "e.g. 128"),
                TemplateField("sheet_no", "सिट नम्बर (यदि भए)", "Sheet Number", "उदा: १ 'ख'", "e.g. 1 Kha"),
                TemplateField("area", "जग्गाको क्षेत्रफल", "Land Area", "उदा: ०-५-१-० (५ आना १ पैसा)", "e.g. 5 Aana"),
                TemplateField("east", "पूर्व साँध", "East Boundary", "उदा: बाटो / रामको कित्ता", "e.g. Road / Ram's plot"),
                TemplateField("west", "पश्चिम साँध", "West Boundary", "उदा: श्यामको कित्ता नं. १२९", "e.g. Shyam's plot 129"),
                TemplateField("north", "उत्तर साँध", "North Boundary", "उदा: मूल सडक (८ मिटर)", "e.g. Main 8m Road"),
                TemplateField("south", "दक्षिण साँध", "South Boundary", "उदा: खोला / हरिबहादुरको घर", "e.g. Stream / Hari's house"),
                TemplateField("purpose", "प्रमाणित गराउनुको प्रयोजन", "Purpose", "उदा: बैंक कर्जा / नक्सा पास / खरिद-बिक्री", "e.g. Bank Loan / Construction"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality", "उदा: भरतपुर महानगरपालिका", "e.g. Bharatpur"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ४", "e.g. 4", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: चितवन", "e.g. Chitwan"),
                TemplateField("phone", "सम्पर्क फोन नं.", "Contact Phone", "उदा: ९८४५००००००", "e.g. 9845000000")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val kitta = v["kitta_no"]?.ifBlank { "..........................." } ?: "..........................."
                val sheet = v["sheet_no"]?.ifBlank { "-" } ?: "-"
                val area = v["area"]?.ifBlank { "..........................." } ?: "..........................."
                val east = v["east"]?.ifBlank { "..........................." } ?: "..........................."
                val west = v["west"]?.ifBlank { "..........................." } ?: "..........................."
                val north = v["north"]?.ifBlank { "..........................." } ?: "..........................."
                val south = v["south"]?.ifBlank { "..........................." } ?: "..........................."
                val purpose = v["purpose"]?.ifBlank { "घर नक्सा पास तथा बैंक प्रयोजन" } ?: "घर नक्सा पास तथा बैंक प्रयोजन"
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: चार किल्ला प्रमाणित सिफारिस गरिपाउँ।

महोदय,
उपरोक्त विषयमा मेरो नाममा मालपोत कार्यालयमा दर्ता स्रेस्ता कायम रहेको यस $muni वडा नं. $ward स्थित सिट नं. $sheet, कित्ता नं. $kitta को क्षेत्रफल $area जग्गाको चार किल्ला साँध-सिमाना देहाय बमोजिम रहेको छ। 

मलाई $purpose का लागि सो जग्गाको चार किल्ला प्रमाणित पत्र आवश्यक परेकोले स्थलगत प्राविधिक जाँचबुझ तथा सर्जमिन गरी प्रमाणित सिफारिस पत्र उपलब्ध गराइदिनुहुन यो निवेदन पेश गरेको छु।

तपसिल (चार किल्ला साँध-सिमाना विवरण):
• पूर्व: $east
• पश्चिम: $west
• उत्तर: $north
• दक्षिण: $south

संलग्न कागजातहरू:
१. जग्गाधनी दर्ता प्रमाण पुर्जा (लालपुर्जा) को प्रमाणित प्रतिलिपि
२. नापी शाखाबाट प्रमाणित ट्रेस नक्सा / फाइल नक्सा
३. चालु आर्थिक वर्षको मालपोत वा एकीकृत सम्पत्ति कर तिरेको रसिद
४. जग्गाधनीको नेपाली नागरिकता प्रमाणपत्रको प्रतिलिपि

निवेदक:
नाम: $name
ठेगाना: $muni-$ward, $dist
सम्पर्क मोबाइल: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 5. Internal Migration / Basaisarai (बसाइँसराइ दर्ता तथा सिफारिस)
        AppTemplate(
            id = "migration",
            titleNp = "बसाइँसराइ दर्ता तथा सिफारिस",
            titleEn = "Migration (Basaisarai) Application",
            category = "ward",
            descriptionNp = "स्थायी रूपमा अन्यत्र बसाइँ सरी जाँदा वडाबाट लिइने बसाइँसराइ सिफारिस",
            descriptionEn = "Application for internal migration registration certificate",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "बसाइँसराइ दर्ता तथा प्रमाणपत्र पाउँ।",
            subjectEn = "Application for Migration Certificate.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "घरमूलीको पूरा नाम", "Family Head Name", "उदा: केशव प्रसाद दाहाल", "e.g. Keshav Dahal"),
                TemplateField("citizenship_no", "घरमूलीको नागरिकता नं.", "Citizenship No.", "उदा: ०६-०१-७०-०९८७६", "e.g. 06-01-70-09876"),
                TemplateField("members_count", "परिवार सदस्य संख्या", "Total Family Members", "उदा: ४ जना", "e.g. 4 members"),
                TemplateField("from_addr", "साविक ठेगाना", "Origin Address", "उदा: इलाम नगरपालिका-३, इलाम", "e.g. Ilam-3"),
                TemplateField("to_addr", "बसाइँ सरी जाने नयाँ ठेगाना", "Destination Address", "उदा: दमक नगरपालिका-५, झापा", "e.g. Damak-5, Jhapa"),
                TemplateField("effective_date", "बसाइँ सरेको मिति", "Migration Date", "उदा: २०८१/१२/०१", "e.g. 2081/12/01"),
                TemplateField("municipality", "हालको गाउँ/नगरपालिका", "Current Municipality", "उदा: इलाम नगरपालिका", "e.g. Ilam"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ३", "e.g. 3", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: इलाम", "e.g. Ilam"),
                TemplateField("phone", "सम्पर्क फोन नं.", "Contact Phone", "उदा: ९८४२६०००००", "e.g. 9842600000")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val ctzNo = v["citizenship_no"]?.ifBlank { "..........................." } ?: "..........................."
                val count = v["members_count"]?.ifBlank { "..." } ?: "..."
                val fromAddr = v["from_addr"]?.ifBlank { "..........................." } ?: "..........................."
                val toAddr = v["to_addr"]?.ifBlank { "..........................." } ?: "..........................."
                val mDate = v["effective_date"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: बसाइँसराइ दर्ता तथा प्रमाणपत्र पाउँ।

महोदय,
उपरोक्त विषयमा म निवेदक तथा मेरो परिवारका जम्मा $count जना सदस्यहरू यस साविक ठेगाना $fromAddr बाट मिति $mDate देखि नयाँ ठेगाना $toAddr मा स्थायी रूपमा बसोबास गर्ने गरी बसाइँ सरी जाने निर्णय गरेका छौं।

उल्लिखित विवरण साँचो हो, कुनै झुट्टा व्यहोरा उल्लेख भएको पाइएमा कानुन बमोजिम कारबाही भोग्न मञ्जुर छु।

अतः व्यक्तिगत घटना दर्ता ऐन तथा स्थानीय सरकार सञ्चालन ऐन बमोजिम बसाइँसराइको अभिलेख दर्ता गरी आधिकारिक बसाइँसराइ प्रमाणपत्र एवं सिफारिस पत्र प्रदान गरिदिनुहुन सादर अनुरोध गर्दछु।

संलग्न प्रमाण कागजातहरू:
१. घरमूली तथा बसाइँ सर्ने सदस्यहरूको नागरिकता / जन्मदर्ताको प्रतिलिपि
२. चालु आर्थिक वर्षको स्थानीय कर, सरसफाइ तथा खानेपानी महसुल चुक्ता रसिद
३. गन्तव्य स्थानको बसोबास प्रमाण (जग्गाधनी पुर्जा वा घरबहाल सम्झौतापत्र)
४. बसाइँसराइको तोकिएको अनुसूची दरखास्त फारम

निवेदक:
नाम: $name (घरमूली)
नागरिकता प्रमाणपत्र नं.: $ctzNo
साविक ठेगाना: $fromAddr
गन्तव्य ठेगाना: $toAddr
सम्पर्क मोबाइल: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 6. Lost Document Police Report (कागजात हराएको प्रहरी खोजी निवेदन)
        AppTemplate(
            id = "lost_docs",
            titleNp = "कागजात हराएको प्रहरी खोजी निवेदन",
            titleEn = "Lost Document Police Report",
            category = "legal",
            descriptionNp = "नागरिकता, लाइसेन्स, ब्लुबुक वा प्रमाणपत्र हराउँदा प्रहरीमा दिइने निवेदन",
            descriptionEn = "Police application for lost citizenship, license, bluebook or certificates",
            recipientNp = "श्रीमान् कार्यालय प्रमुखज्यू,\nजिल्ला प्रहरी परिसर / ट्राफिक प्रहरी प्रभाग, {district}।",
            recipientEn = "To Officer-in-Charge, District Police Office / Traffic Division.",
            subjectNp = "कागजात हराएको बारे खोजतलास एवं सिफारिस पाउँ।",
            subjectEn = "Police report for lost official documents.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "निवेदकको पूरा नाम", "Applicant Full Name", "उदा: प्रकाश थापा", "e.g. Prakash Thapa"),
                TemplateField("citizenship_no", "नागरिकता नम्बर", "Citizenship Number", "उदा: २७-०१-७०-०५४३२", "e.g. 27-01-70-05432"),
                TemplateField("doc_type", "हराएको कागजातको नाम", "Lost Document Name", "उदा: सवारी चालक अनुमतिपत्र (लाइसेन्स) / ब्लुबुक", "e.g. Driving License / Bluebook"),
                TemplateField("doc_no", "कागजात नम्बर", "Document Number", "उदा: ०१-०६-००१२३४५", "e.g. 01-06-0012345"),
                TemplateField("issuing_authority", "जारी गर्ने कार्यालय", "Issuing Office", "उदा: यातायात व्यवस्था कार्यालय, एकान्तकुना", "e.g. Transport Office Ekantakuna"),
                TemplateField("lost_place", "हराएको मिति र स्थान", "Place & Date Lost", "उदा: रत्नपार्क बसपार्क, २०८१/११/२५ मा", "e.g. Ratnapark on 2081/11/25"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality", "उदा: ललितपुर महानगरपालिका", "e.g. Lalitpur"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ५", "e.g. 5", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: ललितपुर", "e.g. Lalitpur"),
                TemplateField("phone", "सम्पर्क मोबाइल नं.", "Contact Phone", "उदा: ९८४१११२२३३", "e.g. 9841112233")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val ctzNo = v["citizenship_no"]?.ifBlank { "..........................." } ?: "..........................."
                val docType = v["doc_type"]?.ifBlank { "..........................." } ?: "..........................."
                val docNo = v["doc_no"]?.ifBlank { "..........................." } ?: "..........................."
                val issuer = v["issuing_authority"]?.ifBlank { "सम्बन्धित सरकारी कार्यालय" } ?: "सम्बन्धित सरकारी कार्यालय"
                val lostPlace = v["lost_place"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् कार्यालय प्रमुखज्यू,
जिल्ला प्रहरी परिसर / ट्राफिक प्रहरी प्रभाग,
$dist ।

विषय: कागजात हराएको बारे खोजतलास एवं सिफारिस पाउँ।

महोदय,
उपरोक्त विषयमा म निवेदकको नाममा जारी भएको निम्न विवरणको सक्कल कागजात मिति तथा स्थान $lostPlace मा हिँड्डुल गर्ने क्रममा कतै खसी हराई हालसम्म फेला नपरेकाले खोजतलासका लागि यो निवेदन पेश गरेको छु।

हराएको कागजातको विस्तृत विवरण:
• कागजातको प्रकार: $docType
• कागजात नम्बर: $docNo
• जारी गर्ने कार्यालय: $issuer

उक्त कागजात कसैले फेला पारेमा वा दुरुपयोग हुन नदिन आवश्यक अभिलेख राखी सम्बन्धित कार्यालयबाट नयाँ प्रतिलिपि (Duplicate Copy) बनाउनका लागि आवश्यक प्रहरी सिफारिस पत्र प्रदान गरिदिनुहुन सादर अनुरोध गर्दछु।

संलग्न प्रमाण:
१. निवेदकको नेपाली नागरिकता प्रमाणपत्रको प्रतिलिपि
२. हराएको कागजातको फोटोकपी (यदि भए)

निवेदक:
नाम: $name
नागरिकता प्रमाणपत्र नं.: $ctzNo
ठेगाना: $muni-$ward, $dist
सम्पर्क मोबाइल: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 7. Bank Account Closure & Settlement (बैंक खाता बन्द तथा भुक्तानी निवेदन)
        AppTemplate(
            id = "bank_account",
            titleNp = "बैंक खाता बन्द / भुक्तानी निवेदन",
            titleEn = "Bank Account Closure & Settlement",
            category = "bank",
            descriptionNp = "बैंक खाता बन्द गरी बाँकी रकम भुक्तानी लिन बैंक शाखालाई निवेदन",
            descriptionEn = "Application for Bank account closure and remaining balance settlement",
            recipientNp = "श्रीमान् शाखा प्रबन्धकज्यू,\n........... बैंक लिमिटेड, {branch} शाखा।",
            recipientEn = "To Branch Manager, Bank Ltd, {branch} Branch.",
            subjectNp = "बैंक खाता बन्द गरी बाँकी रकम भुक्तानी पाउँ बारे।",
            subjectEn = "Application for Account Closure & Balance Payment.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "खातावालाको पूरा नाम", "Account Holder Name", "उदा: सुनिता गुरुङ", "e.g. Sunita Gurung"),
                TemplateField("citizenship_no", "नागरिकता नम्बर", "Citizenship Number", "उदा: ४५-०१-७२-०३४५६", "e.g. 45-01-72-03456"),
                TemplateField("bank_name", "बैंकको नाम", "Bank Name", "उदा: नबिल बैंक लिमिटेड", "e.g. Nabil Bank Ltd"),
                TemplateField("branch", "शाखा कार्यालय", "Branch Name", "उदा: न्युरोड शाखा, काठमाडौँ", "e.g. New Road Branch"),
                TemplateField("acc_no", "खाता नम्बर", "Account Number", "उदा: ०१२०१०००५४३२१", "e.g. 0120100054321"),
                TemplateField("acc_type", "खाताको प्रकार", "Account Type", "उदा: बचत खाता / चालु खाता", "e.g. Savings / Current"),
                TemplateField("reason", "खाता बन्द गर्नुको कारण", "Reason for Closure", "उदा: विदेश जान लागेकोले / व्यक्तिगत कारण", "e.g. Relocating / Personal"),
                TemplateField("phone", "सम्पर्क फोन नम्बर", "Phone Number", "उदा: ९८५१००००००", "e.g. 9851000000")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val ctzNo = v["citizenship_no"]?.ifBlank { "..........................." } ?: "..........................."
                val bank = v["bank_name"]?.ifBlank { "........... बैंक लिमिटेड" } ?: "........... बैंक लिमिटेड"
                val branch = v["branch"]?.ifBlank { "........... शाखा" } ?: "........... शाखा"
                val accNo = v["acc_no"]?.ifBlank { "..........................." } ?: "..........................."
                val accType = v["acc_type"]?.ifBlank { "बचत खाता" } ?: "बचत खाता"
                val reason = v["reason"]?.ifBlank { "व्यक्तिगत कारणवश" } ?: "व्यक्तिगत कारणवश"
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् शाखा प्रबन्धकज्यू,
$bank,
$branch ।

विषय: बैंक खाता बन्द गरी बाँकी रकम भुक्तानी पाउँ बारे।

महोदय,
उपरोक्त विषयमा यस बैंक शाखामा मेरो नाममा सञ्चालनमा रहेको $accType नं. $accNo $reason ले गर्दा निरन्तर सञ्चालन गर्न असमर्थ भएको छु।

अतः उक्त खाता तत्काल बन्द गरी खातामा मौज्दात रहेको सम्पूर्ण रकम मलाई नगद वा मेरो अर्को बैंक खातामा रकमान्तर गरी भुक्तानी उपलब्ध गराइदिनुहुन हार्दिक अनुरोध गर्दछु।

यस खातासँग सम्बन्धित प्रयोग नभएका चेक पानाहरू तथा डेबिट/एटीएम कार्ड यसै निवेदनसाथ बैंकलाई फिर्ता बुझाएको छु।

खातावालाको विवरण:
नाम: $name
नागरिकता प्रमाणपत्र नं.: $ctzNo
खाता नं.: $accNo
सम्पर्क मोबाइल नं.: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 8. Office Casual / Sick Leave (कार्यालय आकस्मिक / बिरामी बिदा निवेदन)
        AppTemplate(
            id = "office_leave",
            titleNp = "कार्यालय आकस्मिक / बिरामी बिदा",
            titleEn = "Office Casual / Sick Leave",
            category = "leave",
            descriptionNp = "कार्यालय वा संस्थामा आकस्मिक वा बिरामी बिदा स्वीकृत गराउन निवेदन",
            descriptionEn = "Formal casual/sick leave request letter for office employees",
            recipientNp = "श्रीमान् कार्यालय प्रमुखज्यू,\n........... कार्यालय / संस्था, {location}।",
            recipientEn = "To Office Head / Manager, Office/Company.",
            subjectNp = "आकस्मिक / बिरामी बिदा स्वीकृत गरिपाउँ बारे।",
            subjectEn = "Application for Casual / Emergency Leave.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "कर्मचारीको नाम", "Employee Name", "उदा: मनिष श्रेष्ठ", "e.g. Manish Shrestha"),
                TemplateField("designation", "पद र विभाग", "Designation & Department", "उदा: अधिकृत, लेखा शाखा", "e.g. Officer, Accounts"),
                TemplateField("leave_days", "बिदा दिन संख्या", "Total Days", "उदा: ३ दिन", "e.g. 3 Days"),
                TemplateField("leave_dates", "बिदा मिति अवधि (देखि-सम्म)", "Leave Duration (From-To)", "उदा: २०८१/१२/१२ देखि २०८१/१२/१४ सम्म", "e.g. 2081/12/12 to 2081/12/14"),
                TemplateField("reason", "बिदा बस्नुको कारण", "Reason for Leave", "उदा: घरेलु अत्यावश्यक काम / अस्वस्थता", "e.g. Family emergency / Illness"),
                TemplateField("handover_person", "कार्यभार सम्हाल्ने सहकर्मी", "Colleague Handover", "उदा: श्री सन्तोष शर्मा", "e.g. Mr. Santosh Sharma"),
                TemplateField("company", "कार्यालय / संस्थाको नाम", "Company / Office Name", "उदा: नेपाल हाइड्रोपावर लिमिटेड, काठमाडौँ", "e.g. Nepal Hydro Ltd")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val desig = v["designation"]?.ifBlank { "..........................." } ?: "..........................."
                val days = v["leave_days"]?.ifBlank { "....." } ?: "....."
                val dates = v["leave_dates"]?.ifBlank { "..........................." } ?: "..........................."
                val reason = v["reason"]?.ifBlank { "घरेलु तथा व्यक्तिगत अत्यावश्यक काम" } ?: "घरेलु तथा व्यक्तिगत अत्यावश्यक काम"
                val handover = v["handover_person"]?.ifBlank { "सम्बन्धित सहकर्मी" } ?: "सम्बन्धित सहकर्मी"
                val comp = v["company"]?.ifBlank { "........... कार्यालय" } ?: "........... कार्यालय"

                """
मिति: $date

श्रीमान् कार्यालय प्रमुखज्यू,
$comp ।

विषय: आकस्मिक / बिरामी बिदा स्वीकृत गरिपाउँ बारे।

महोदय,
उपरोक्त विषयमा म यस संस्थाको $desig पदमा कार्यरत कर्मचारी हुँ। मलाई $reason परेकाले मिति $dates सम्म (जम्मा $days) कार्यालयमा उपस्थित हुन नसक्ने भएको छु।

अतः उल्लिखित अवधिको लागि मेरो सञ्चित आकस्मिक/बिरामी बिदाबाट कट्टा हुने गरी बिदा स्वीकृत गरिदिनुहुन सादर अनुरोध गर्दछु।

मेरो अनुपस्थितिमा कार्यालयको दैनिक अत्यावश्यक कार्य सम्पादन गर्न सहकर्मी $handover लाई जिम्मेवारी हस्तान्तरण गरेको छु।

निवेदक:
नाम: $name
पद तथा विभाग: $desig
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),

        // 9. School / College Leave Application (विद्यालय / क्याम्पस बिदा निवेदन)
        AppTemplate(
            id = "school_leave",
            titleNp = "विद्यालय / क्याम्पस बिदा निवेदन",
            titleEn = "School / College Leave Application",
            category = "leave",
            descriptionNp = "विद्यालय वा कलेजमा अनुपस्थित हुँदा बिदा स्वीकृत गराउने औपचारिक निवेदन",
            descriptionEn = "Application for student absence leave to Principal/Head",
            recipientNp = "श्रीमान् प्रधानाध्यापकज्यू,\n........... विद्यालय / क्याम्पस।",
            recipientEn = "To Principal, School / College.",
            subjectNp = "बिदा पाउँ बारे निवेदन।",
            subjectEn = "Application for Student Leave of Absence.",
            fields = listOf(
                TemplateField("app_date", "दरखास्त मिति (वि.सं.)", "Application Date (B.S.)", "उदा: २०८१/१२/१०", "e.g. 2081/12/10"),
                TemplateField("name", "विद्यार्थीको पूरा नाम", "Student Full Name", "उदा: आरभ पौडेल", "e.g. Aarav Poudel"),
                TemplateField("grade", "कक्षा र सेक्सन", "Class & Section", "उदा: कक्षा १०, सेक्सन 'क'", "e.g. Grade 10 (A)"),
                TemplateField("roll_no", "रोल नम्बर", "Roll Number", "उदा: १५", "e.g. 15", isNumber = true),
                TemplateField("school_name", "विद्यालय / कलेजको नाम", "School / College Name", "उदा: सिद्धार्थ सेकेण्डरी स्कूल, बुटवल", "e.g. Siddhartha School"),
                TemplateField("leave_dates", "बिदा मिति अवधि", "Leave Dates (From-To)", "उदा: २०८१/१२/१५ देखि २०८१/१२/१७ सम्म (३ दिन)", "e.g. 3 days"),
                TemplateField("reason", "बिदाको कारण", "Reason for Absence", "उदा: अचानक बिरामी परेकोले / पारिवारिक कार्य", "e.g. Fever / Family event"),
                TemplateField("guardian_phone", "अभिभावकको सम्पर्क नम्बर", "Guardian Contact", "उदा: ९८४७००००००", "e.g. 9847000000")
            ),
            generateLetter = { v ->
                val date = v["app_date"]?.ifBlank { "..........................." } ?: "..........................."
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val grade = v["grade"]?.ifBlank { "....." } ?: "....."
                val roll = v["roll_no"]?.ifBlank { "....." } ?: "....."
                val school = v["school_name"]?.ifBlank { "........... विद्यालय" } ?: "........... विद्यालय"
                val dates = v["leave_dates"]?.ifBlank { "..........................." } ?: "..........................."
                val reason = v["reason"]?.ifBlank { "अचानक अस्वस्थ भएकोले" } ?: "अचानक अस्वस्थ भएकोले"
                val gPhone = v["guardian_phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $date

श्रीमान् प्रधानाध्यापकज्यू / क्याम्पस प्रमुखज्यू,
$school ।

विषय: बिदा पाउँ बारे निवेदन।

महोदय,
सविनय निवेदन छ कि म यस विद्यालयको $grade (रोल नं. $roll) मा अध्ययनरत नियमित छात्र/छात्रा हुँ। मलाई $reason कारणले गर्दा मिति $dates विद्यालयमा उपस्थित भई पठनपाठनमा सहभागी हुन नसक्ने भएको छु।

अतः उल्लिखित दिनहरूको बिदा स्वीकृत गरिदिनुहुन विनम्र अनुरोध गर्दछु। बिदाको अवधिमा छुटेका सम्पूर्ण गृहकार्य तथा पाठ्यभार नियमित साथीहरूको सहयोगमा पूरा गर्ने प्रतिबद्धता व्यक्त गर्दछु।

आज्ञाकारी छात्र / छात्रा:
नाम: $name
कक्षा: $grade  |  रोल नं.: $roll

अभिभावकको विवरण:
सम्पर्क फोन नं.: $gPhone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        )
    )
}
