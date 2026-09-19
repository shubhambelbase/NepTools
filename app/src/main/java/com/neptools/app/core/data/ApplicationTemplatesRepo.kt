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

object ApplicationTemplatesRepo {
    val templates = listOf(
        AppTemplate(
            id = "citizenship",
            titleNp = "नागरिकता सिफारिस निवेदन",
            titleEn = "Citizenship Recommendation",
            category = "ward",
            descriptionNp = "नयाँ नागरिकता प्रमाणपत्र लिन वडा कार्यालयमा दिइने सिफारिस निवेदन",
            descriptionEn = "Application to Ward office for citizenship certificate recommendation",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "नागरिकता प्रमाणपत्रको सिफारिस पाउँ बारे।",
            subjectEn = "Application for Citizenship Recommendation Certificate.",
            fields = listOf(
                TemplateField("name", "निवेदकको नाम", "Applicant Full Name", "उदा: सन्तोष श्रेष्ठ", "e.g. Santosh Shrestha"),
                TemplateField("dob", "जन्म मिति (वि.सं.)", "Date of Birth (B.S.)", "उदा: २०६२/०५/१५", "e.g. 2062/05/15"),
                TemplateField("father_name", "बुबा / आमाको नाम", "Father / Mother's Name", "उदा: राम बहादुर श्रेष्ठ", "e.g. Ram Bahadur Shrestha"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality / Rural Muni", "उदा: काठमाडौँ महानगरपालिका", "e.g. Kathmandu Metro"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: १०", "e.g. 10", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: काठमाडौँ", "e.g. Kathmandu"),
                TemplateField("phone", "सम्पर्क मोबाइल नं.", "Contact Phone No.", "उदा: ९८४१२३४५६७", "e.g. 9841234567", isNumber = true)
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val dob = v["dob"]?.ifBlank { "..........................." } ?: "..........................."
                val father = v["father_name"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
मिति: $dob (दरखास्त मिति)

श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: नेपाली नागरिकता प्रमाणपत्रको सिफारिस पाउँ बारे।

महोदय,
उपरोक्त विषयमा म निवेदक यस $muni वडा नं. $ward (साविक $dist) को स्थायी बासिन्दा श्री $father को छोरा/छोरी हुँ। मेरो जन्म मिति $dob मा भएको र हाल उमेर १६ वर्ष पूरा भई नेपाली नागरिकताको प्रमाणपत्र बनाउनुपर्ने भएको छ।

यसर्थ नागरिकता प्रमाणपत्र प्राप्तिका लागि आवश्यक पर्ने जन्मदर्ता प्रमाणपत्र, बुबा/आमाको नागरिकताको प्रतिलिपि यसै निवेदनसाथ संलग्न गरी पेश गरेको छु। व्यहोरा प्रमाणित गरी आवश्यक वडा सिफारिस पत्र उपलब्ध गराई दिनुहुन सादर अनुरोध गर्दछु।

संलग्न प्रमाणहरू:
१. जन्म दर्ता प्रमाणपत्रको प्रमाणित प्रतिलिपि
२. बाबु तथा आमाको नेपाली नागरिकता प्रमाणपत्रको प्रतिलिपि
३. शैक्षिक योग्यताको प्रमाणपत्र प्रतिलिपि

निवेदक:
नाम: $name
ठेगाना: $muni-$ward, $dist
सम्पर्क फोन नं.: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),
        AppTemplate(
            id = "land_transfer",
            titleNp = "जग्गा नामसारी सिफारिस निवेदन",
            titleEn = "Land Transfer / Mutation",
            category = "ward",
            descriptionNp = "मृतकको नाममा रहेको जग्गा हकवालाको नाममा नामसारी गर्न वडा सिफारिस",
            descriptionEn = "Application for land inheritance and title transfer",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "जग्गा नामसारी सिफारिस पाउँ बारे।",
            subjectEn = "Application for Land Inheritance/Transfer Recommendation.",
            fields = listOf(
                TemplateField("name", "हकवाला निवेदकको नाम", "Applicant (Legal Heir)", "उदा: रमेश अधिकारी", "e.g. Ramesh Adhikari"),
                TemplateField("deceased_name", "मृतक जग्गाधनीको नाम", "Deceased Owner Name", "उदा: स्व. हरिप्रसाद अधिकारी", "e.g. Late Hari Prasad Adhikari"),
                TemplateField("relationship", "हकवालाको नाता", "Relationship with Deceased", "उदा: छोरा / श्रीमती", "e.g. Son / Wife"),
                TemplateField("kitta_no", "कित्ता नम्बर", "Plot / Kitta Number", "उदा: ५२४", "e.g. 524"),
                TemplateField("area", "जग्गाको क्षेत्रफल", "Land Area", "उदा: ०-४-२-० (४ आना २ दाम)", "e.g. 4 Aana"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality", "उदा: पोखरा महानगरपालिका", "e.g. Pokhara"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ६", "e.g. 6", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: कास्की", "e.g. Kaski")
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val deceased = v["deceased_name"]?.ifBlank { "..........................." } ?: "..........................."
                val rel = v["relationship"]?.ifBlank { "..........................." } ?: "..........................."
                val kitta = v["kitta_no"]?.ifBlank { "..........................." } ?: "..........................."
                val area = v["area"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."

                """
श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: जग्गा नामसारी सिफारिस पाउँ बारे।

महोदय,
उपरोक्त विषयमा मेरा पूज्य $rel $deceased को स्वर्गारोहण भइसकेको हुँदा निजको नाममा दर्ता स्रेस्ता कायम रहेको यस $muni वडा नं. $ward स्थित कित्ता नं. $kitta को क्षेत्रफल $area जग्गा हकवाला म निवेदकको नाममा नामसारी गर्नुपर्ने भएको छ।

उक्त जग्गाको हकदाबी नामसारी प्रक्रियाका लागि आवश्यक वडा कार्यालयको स्थलगत सर्जमिन तथा सिफारिस पत्र उपलब्ध गराई दिनुहुन सादर अनुरोध गर्दछु।

संलग्न प्रमाण कागजातहरू:
१. मृतकको मृत्यु दर्ता प्रमाणपत्रको प्रतिलिपि
२. सक्कल जग्गाधनी प्रमाण पुर्जा (लालपुर्जा)
३. नाता प्रमाणित प्रमाणपत्र
४. हकवालाहरूको नागरिकता प्रमाणपत्रको प्रतिलिपि

निवेदक:
नाम: $name (नाता: $rel)
ठेगाना: $muni-$ward, $dist
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),
        AppTemplate(
            id = "four_boundaries",
            titleNp = "चार किल्ला प्रमाणित निवेदन",
            titleEn = "Four Boundaries (Char Killa)",
            category = "ward",
            descriptionNp = "जग्गाको पूर्व, पश्चिम, उत्तर, दक्षिण साँध-सिमाना प्रमाणित सिफारिस",
            descriptionEn = "Application to certify land boundary details (Char Killa)",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "चार किल्ला प्रमाणित गरिपाउँ बारे।",
            subjectEn = "Application for Four Boundaries Verification.",
            fields = listOf(
                TemplateField("name", "जग्गाधनीको नाम", "Land Owner Name", "उदा: विष्णु शर्मा", "e.g. Bishnu Sharma"),
                TemplateField("kitta_no", "कित्ता नम्बर", "Kitta / Plot No.", "उदा: १२८", "e.g. 128"),
                TemplateField("east", "पूर्व साँध", "East Boundary", "उदा: बाटो / रामको जग्गा", "e.g. Road / Ram's plot"),
                TemplateField("west", "पश्चिम साँध", "West Boundary", "उदा: श्यामको कित्ता नं. १२९", "e.g. Shyam's plot 129"),
                TemplateField("north", "उत्तर साँध", "North Boundary", "उदा: मूल सडक (८ मिटर)", "e.g. Main 8m Road"),
                TemplateField("south", "दक्षिण साँध", "South Boundary", "उदा: खोला / हरिबहादुरको घर", "e.g. Stream / Hari's house"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality", "उदा: भरतपुर महानगरपालिका", "e.g. Bharatpur"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ४", "e.g. 4", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: चितवन", "e.g. Chitwan")
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val kitta = v["kitta_no"]?.ifBlank { "..........................." } ?: "..........................."
                val east = v["east"]?.ifBlank { "..........................." } ?: "..........................."
                val west = v["west"]?.ifBlank { "..........................." } ?: "..........................."
                val north = v["north"]?.ifBlank { "..........................." } ?: "..........................."
                val south = v["south"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."

                """
श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: चार किल्ला प्रमाणित गरिपाउँ बारे।

महोदय,
उपरोक्त विषयमा मेरो नाममा मालपोत कार्यालयमा दर्ता स्रेस्ता कायम रहेको यस $muni वडा नं. $ward स्थित कित्ता नं. $kitta को जग्गाको चार किल्ला साँध सिमाना तपसिल बमोजिम रहेको छ। 

बैंक कर्जा / भवन निर्माण / किनबेच प्रयोजनका लागि सो चार किल्ला प्रमाणित पत्र आवश्यक परेकोले स्थलगत प्राविधिक जाँचबुझ गरी प्रमाणित पत्र उपलब्ध गराइदिनुहुन यो निवेदन पेश गरेको छु।

तपसिल (चार किल्ला विवरण):
• पूर्व: $east
• पश्चिम: $west
• उत्तर: $north
• दक्षिण: $south

निवेदक:
नाम: $name
ठेगाना: $muni-$ward, $dist
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),
        AppTemplate(
            id = "migration",
            titleNp = "बसाइँसराइ दर्ता तथा सिफारिस",
            titleEn = "Migration (Basaisarai) Application",
            category = "ward",
            descriptionNp = "स्थायी रूपमा अन्यत्र बसाइँ सरी जाँदा वडाबाट लिइने बसाइँसराइ सिफारिस",
            descriptionEn = "Application for internal migration registration certificate",
            recipientNp = "श्रीमान् वडा अध्यक्षज्यू,\nवडा कार्यालय, {municipality}-{ward}, {district}।",
            recipientEn = "To Ward Chairperson, Ward Office, {municipality}-{ward}, {district}.",
            subjectNp = "बसाइँसराइ दर्ता तथा सिफारिस पाउँ बारे।",
            subjectEn = "Application for Migration Certificate.",
            fields = listOf(
                TemplateField("name", "घरमूलीको नाम", "Family Head Name", "उदा: केशव प्रसाद दाहाल", "e.g. Keshav Dahal"),
                TemplateField("members_count", "परिवार सदस्य संख्या", "Total Family Members", "उदा: ४ जना", "e.g. 4 members"),
                TemplateField("from_addr", "साविक ठेगाना", "Origin Address", "उदा: इलाम नगरपालिका-३, इलाम", "e.g. Ilam-3"),
                TemplateField("to_addr", "बसाइँ सरी जाने नयाँ ठेगाना", "Destination Address", "उदा: दमक नगरपालिका-५, झापा", "e.g. Damak-5, Jhapa"),
                TemplateField("effective_date", "बसाइँ सरेको मिति", "Migration Date", "उदा: २०८३/०४/०१", "e.g. 2083/04/01"),
                TemplateField("municipality", "हालको गाउँ/नगरपालिका", "Current Municipality", "उदा: इलाम नगरपालिका", "e.g. Ilam"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ३", "e.g. 3", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: इलाम", "e.g. Ilam")
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val count = v["members_count"]?.ifBlank { "..." } ?: "..."
                val fromAddr = v["from_addr"]?.ifBlank { "..........................." } ?: "..........................."
                val toAddr = v["to_addr"]?.ifBlank { "..........................." } ?: "..........................."
                val mDate = v["effective_date"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."

                """
श्रीमान् वडा अध्यक्षज्यू,
$muni वडा नं. $ward को कार्यालय,
$dist ।

विषय: बसाइँसराइ दर्ता तथा सिफारिस पाउँ बारे।

महोदय,
उपरोक्त विषयमा म निवेदक तथा मेरो परिवारका जम्मा $count जना सदस्यहरू यस साविक ठेगाना $fromAddr बाट मिति $mDate देखि नयाँ ठेगाना $toAddr मा स्थायी रूपमा बसोबास गर्न बसाइँ सरी जाने निर्णय गरेका छौं।

अतः कानून बमोजिम बसाइँसराइको अभिलेख दर्ता गरी आधिकारिक बसाइँसराइ प्रमाणपत्र एवं सिफारिस पत्र प्रदान गरिदिनुहुन सादर अनुरोध गर्दछु।

संलग्न कागजातहरू:
१. घरमूली तथा सदस्यहरूको नागरिकता / जन्मदर्ता प्रतिलिपि
२. स्थानीय कर तथा महसुल चुक्ता प्रमाण
३. गन्तव्य स्थानको बसोबास प्रमाण / जग्गाधनी पुर्जा

निवेदक:
नाम: $name (घरमूली)
साविक ठेगाना: $fromAddr
गन्तव्य ठेगाना: $toAddr
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),
        AppTemplate(
            id = "bank_account",
            titleNp = "बैंक खाता बन्द / नयाँ चेकबुक निवेदन",
            titleEn = "Bank Account / Chequebook Request",
            category = "bank",
            descriptionNp = "बैंक खाता बन्द गरी बाँकी रकम लिन वा चेकबुक जारी गर्न बैंकलाई निवेदन",
            descriptionEn = "Application for Bank account closure or new chequebook request",
            recipientNp = "श्रीमान् शाखा प्रबन्धकज्यू,\n........... बैंक लिमिटेड, {district} शाखा।",
            recipientEn = "To Branch Manager, Bank Ltd, {district} Branch.",
            subjectNp = "बैंक खाता बन्द गरी बाँकी रकम भुक्तानी पाउँ बारे।",
            subjectEn = "Application for Account Closure & Payment.",
            fields = listOf(
                TemplateField("name", "खातावालाको पूरा नाम", "Account Holder Name", "उदा: सुनिता गुरुङ", "e.g. Sunita Gurung"),
                TemplateField("bank_name", "बैंकको नाम", "Bank Name", "उदा: नबिल बैंक लिमिटेड", "e.g. Nabil Bank Ltd"),
                TemplateField("branch", "शाखा कार्यालय", "Branch Name", "उदा: न्युरोड शाखा, काठमाडौँ", "e.g. New Road Branch"),
                TemplateField("acc_no", "खाता नम्बर", "Account Number", "उदा: ०१२०१०००५४३२१", "e.g. 0120100054321"),
                TemplateField("reason", "खाता बन्द गर्नुको कारण", "Reason for Closure", "उदा: विदेश जान लागेकोले / व्यक्तिगत कारण", "e.g. Relocating"),
                TemplateField("phone", "सम्पर्क फोन नम्बर", "Phone Number", "उदा: ९८५१००००००", "e.g. 9851000000")
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val bank = v["bank_name"]?.ifBlank { "........... बैंक लिमिटेड" } ?: "........... बैंक लिमिटेड"
                val branch = v["branch"]?.ifBlank { "........... शाखा" } ?: "........... शाखा"
                val accNo = v["acc_no"]?.ifBlank { "..........................." } ?: "..........................."
                val reason = v["reason"]?.ifBlank { "व्यक्तिगत कारणवश" } ?: "व्यक्तिगत कारणवश"
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
श्रीमान् शाखा प्रबन्धकज्यू,
$bank,
$branch ।

विषय: बैंक खाता बन्द गरी बाँकी रकम भुक्तानी पाउँ बारे।

महोदय,
उपरोक्त विषयमा यस बैंक शाखामा मेरो नाममा सञ्चालनमा रहेको बचत खाता नं. $accNo $reason ले गर्दा निरन्तर सञ्चालन गर्न असमर्थ भएको छु।

अतः उक्त खाता तत्काल बन्द गरी खातामा मौज्दात रहेको सम्पूर्ण रकम मलाई नगद वा मेरो अर्को बैंक खातामा भुक्तानी उपलब्ध गराइदिनुहुन हार्दिक अनुरोध गर्दछु। यस खातासँग सम्बन्धित चेकबुक र डेविट कार्ड यसै निवेदनसाथ फिर्ता बुझाएको छु।

खातावालाको विवरण:
नाम: $name
खाता नं.: $accNo
सम्पर्क मोबाइल नं.: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),
        AppTemplate(
            id = "lost_docs",
            titleNp = "कागजात हराएको प्रहरी खोजी निवेदन",
            titleEn = "Lost Document Police Report",
            category = "legal",
            descriptionNp = "नागरिकता, लाइसेन्स, लालपुर्जा वा प्रमाणपत्र हराउँदा प्रहरी चौकीमा दिइने निवेदन",
            descriptionEn = "Police application for lost citizenship, license or certificates",
            recipientNp = "श्रीमान् कार्यालय प्रमुखज्यू,\nजिल्ला प्रहरी परिसर / ट्राफिक प्रहरी प्रभाग।",
            recipientEn = "To Officer-in-Charge, District Police Office.",
            subjectNp = "कागजात हराएको बारे खोजतलास एवं सिफारिस गरिपाउँ।",
            subjectEn = "Police report for lost official documents.",
            fields = listOf(
                TemplateField("name", "निवेदकको नाम", "Applicant Full Name", "उदा: प्रकाश थापा", "e.g. Prakash Thapa"),
                TemplateField("doc_type", "हराएको कागजातको नाम", "Lost Document Name", "उदा: सवारी चालक अनुमतिपत्र", "e.g. Driving License"),
                TemplateField("doc_no", "कागजातको नम्बर (यदि थाहा भए)", "Document Number", "उदा: ०१-०६-००१२३४५", "e.g. 01-06-0012345"),
                TemplateField("lost_place", "हराएको स्थान र मिति", "Place & Date Lost", "उदा: रत्नपार्क बसपार्क, २०८३/०४/१० मा", "e.g. Ratnapark on 2083/04/10"),
                TemplateField("municipality", "गाउँ / नगरपालिका", "Municipality", "उदा: ललितपुर महानगरपालिका", "e.g. Lalitpur"),
                TemplateField("ward", "वडा नं.", "Ward No.", "उदा: ५", "e.g. 5", isNumber = true),
                TemplateField("district", "जिल्ला", "District", "उदा: ललितपुर", "e.g. Lalitpur"),
                TemplateField("phone", "सम्पर्क फोन नं.", "Contact Phone", "उदा: ९८४१११२२३३", "e.g. 9841112233")
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val docType = v["doc_type"]?.ifBlank { "..........................." } ?: "..........................."
                val docNo = v["doc_no"]?.ifBlank { "..........................." } ?: "..........................."
                val lostPlace = v["lost_place"]?.ifBlank { "..........................." } ?: "..........................."
                val muni = v["municipality"]?.ifBlank { "..........................." } ?: "..........................."
                val ward = v["ward"]?.ifBlank { "..." } ?: "..."
                val dist = v["district"]?.ifBlank { "..........................." } ?: "..........................."
                val phone = v["phone"]?.ifBlank { "..........................." } ?: "..........................."

                """
श्रीमान् कार्यालय प्रमुखज्यू,
जिल्ला प्रहरी परिसर / प्रहरी वृत्त,
$dist ।

विषय: कागजात हराएको बारे खोजतलास एवं सिफारिस गरिपाउँ।

महोदय,
उपरोक्त विषयमा म निवेदकको नाममा जारी भएको निम्न बमोजिमको सक्कल कागजात मिति तथा स्थान $lostPlace मा हिँड्डुल गर्ने क्रममा कतै खसी हराई फेला नपरेकाले खोजतलासका लागि यो निवेदन पेश गरेको छु।

हराएको कागजातको विवरण:
• कागजातको प्रकार: $docType
• कागजात नम्बर: $docNo
• जारी गर्ने निकाय: $dist

उक्त कागजात कसैले दुरुपयोग नगरोस् भनी अभिलेख राखी नयाँ प्रतिलिपि (Duplicate Copy) बनाउनका लागि आवश्यक प्रहरी सिफारिस पत्र प्रदान गरिदिनुहुन हार्दिक अनुरोध गर्दछु।

निवेदक:
नाम: $name
ठेगाना: $muni-$ward, $dist
सम्पर्क मोबाइल: $phone
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),
        AppTemplate(
            id = "office_leave",
            titleNp = "कार्यालय / जागिर आकस्मिक बिदा निवेदन",
            titleEn = "Office Casual Leave Application",
            category = "leave",
            descriptionNp = "कार्यालय वा संस्थामा आकस्मिक वा बिरामी बिदा स्वीकृत गराउन निवेदन",
            descriptionEn = "Formal casual/sick leave request letter for office employees",
            recipientNp = "श्रीमान् कार्यालय प्रमुखज्यू,\n........... कार्यालय / संस्था।",
            recipientEn = "To Department Head / Manager, Office/Company.",
            subjectNp = "आकस्मिक बिदा स्वीकृत गरिपाउँ बारे।",
            subjectEn = "Application for Casual / Emergency Leave.",
            fields = listOf(
                TemplateField("name", "कर्मचारीको नाम", "Employee Name", "उदा: मनिष श्रेष्ठ", "e.g. Manish Shrestha"),
                TemplateField("designation", "पद / विभाग", "Designation / Dept", "उदा: सिनियर अधिकृत / लेखा शाखा", "e.g. Senior Officer"),
                TemplateField("leave_days", "बिदा दिन संख्या", "Total Days", "उदा: ३ दिन", "e.g. 3 Days"),
                TemplateField("leave_dates", "बिदा मिति अवधि", "Leave Duration (From-To)", "उदा: २०८३/०५/१० देखि २०८३/०५/१२ सम्म", "e.g. 2083/05/10 to 2083/05/12"),
                TemplateField("reason", "बिदा बस्नुको कारण", "Reason for Leave", "उदा: घरेलु अत्यावश्यक काम / अस्वस्थता", "e.g. Family emergency"),
                TemplateField("company", "कार्यालय / संस्थाको नाम", "Company / Office Name", "उदा: हाइड्रोपावर लिमिटेड, काठमाडौँ", "e.g. Nepal Corp Ltd")
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val desig = v["designation"]?.ifBlank { "..........................." } ?: "..........................."
                val days = v["leave_days"]?.ifBlank { "....." } ?: "....."
                val dates = v["leave_dates"]?.ifBlank { "..........................." } ?: "..........................."
                val reason = v["reason"]?.ifBlank { "घरेलु तथा व्यक्तिगत अत्यावश्यक काम" } ?: "घरेलु तथा व्यक्तिगत अत्यावश्यक काम"
                val comp = v["company"]?.ifBlank { "........... कार्यालय" } ?: "........... कार्यालय"

                """
श्रीमान् कार्यालय प्रमुखज्यू,
$comp ।

विषय: आकस्मिक बिदा स्वीकृत गरिपाउँ बारे।

महोदय,
उपरोक्त विषयमा म यस संस्थामा $desig पदमा कार्यरत छु। मलाई $reason परेकाले मिति $dates (जम्मा $days) सम्म कार्यालयमा उपस्थित हुन नसक्ने भएको छु।

अतः सो अवधिको लागि मेरो सञ्चित/आकस्मिक बिदाबाट कट्टा हुने गरी बिदा स्वीकृत गरिदिनुहुन सादर अनुरोध गर्दछु। मेरो अनुपस्थितिमा मेरा अत्यावश्यक कामहरू सम्बन्धित सहकर्मीलाई जिम्मेवारी हस्तान्तरण गरेको छु।

निवेदक:
नाम: $name
पद: $desig
हस्ताक्षर: .......................................
                """.trimIndent()
            }
        ),
        AppTemplate(
            id = "school_leave",
            titleNp = "विद्यालय / कलेज बिदा निवेदन",
            titleEn = "School / College Leave Application",
            category = "leave",
            descriptionNp = "विद्यालय वा कलेजमा अनुपस्थित हुँदा बिदा स्वीकृत गराउने औपचारिक निवेदन",
            descriptionEn = "Application for student absence leave to Principal/Head",
            recipientNp = "श्रीमान् प्रधानाध्यापकज्यू,\n........... विद्यालय / क्याम्पस।",
            recipientEn = "To Principal, School / College.",
            subjectNp = "बिदा पाउँ बारे निवेदन।",
            subjectEn = "Application for Student Leave of Absence.",
            fields = listOf(
                TemplateField("name", "विद्यार्थीको नाम", "Student Full Name", "उदा: आरभ पौडेल", "e.g. Aarav Poudel"),
                TemplateField("grade", "कक्षा र सेक्सन", "Class & Section", "उदा: कक्षा १०, सेक्सन 'क'", "e.g. Grade 10 (A)"),
                TemplateField("roll_no", "रोल नम्बर", "Roll Number", "उदा: १५", "e.g. 15", isNumber = true),
                TemplateField("school_name", "विद्यालय / कलेजको नाम", "School / College Name", "उदा: सिद्धार्थ सेकेण्डरी स्कूल", "e.g. Siddhartha School"),
                TemplateField("leave_dates", "बिदा मिति र दिन", "Leave Dates & Days", "उदा: २०८३/०५/१५ देखि २०८३/०५/१७ सम्म (३ दिन)", "e.g. 3 days"),
                TemplateField("reason", "बिदाको कारण", "Reason for Absence", "उदा: अचानक बिरामी परेकोले / पारिवारिक कार्य", "e.g. Fever / Family event")
            ),
            generateLetter = { v ->
                val name = v["name"]?.ifBlank { "..........................." } ?: "..........................."
                val grade = v["grade"]?.ifBlank { "....." } ?: "....."
                val roll = v["roll_no"]?.ifBlank { "....." } ?: "....."
                val school = v["school_name"]?.ifBlank { "........... विद्यालय" } ?: "........... विद्यालय"
                val dates = v["leave_dates"]?.ifBlank { "..........................." } ?: "..........................."
                val reason = v["reason"]?.ifBlank { "अचानक अस्वस्थ भएकोले" } ?: "अचानक अस्वस्थ भएकोले"

                """
श्रीमान् प्रधानाध्यापकज्यू / क्याम्पस प्रमुखज्यू,
$school ।

विषय: बिदा पाउँ बारे निवेदन।

महोदय,
विनम्र अनुरोध छ कि म यस विद्यालयको $grade (रोल नं. $roll) मा अध्ययनरत विद्यार्थी हुँ। मलाई $reason कारण मिति $dates विद्यालयमा उपस्थित हुन नसक्ने भएको छु।

अतः उक्त दिनहरूको बिदा स्वीकृत गरिदिनुहुन हार्दिक अनुरोध गर्दछु। बिदाको समयमा छुटेको पाठ्यभार साथीहरूको सहयोगमा पूरा गर्ने विश्वास दिलाउँदछु।

आज्ञाकारी छात्र / छात्रा:
नाम: $name
कक्षा: $grade  |  रोल नं.: $roll
अभिभावकको हस्ताक्षर: .......................................
                """.trimIndent()
            }
        )
    )
}
