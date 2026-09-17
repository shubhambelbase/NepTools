package com.neptools.app.core.data.license

import androidx.compose.runtime.Immutable

enum class LicenseCategory(val code: String, val nameNp: String, val nameEn: String) {
    CATEGORY_A("A", "वर्ग 'क' (मोटरसाइकल/स्कुटर)", "Category A (Bike/Scooter)"),
    CATEGORY_B("B", "वर्ग 'ख' (कार/जिप/भ्यान)", "Category B (Car/Jeep/Van)")
}

enum class QuestionTopic(val id: String, val titleNp: String, val titleEn: String) {
    ALL("all", "सबै प्रश्नहरू", "All Questions"),
    VEHICLE_OPERATION("operation", "सवारी सञ्चालन ज्ञान", "Vehicle Operation"),
    TRAFFIC_SIGNS("signs", "ट्राफिक सङ्केत तथा नियम", "Traffic Signs & Rules"),
    MECHANICAL_KNOWLEDGE("mechanical", "सवारी प्राविधिक तथा यान्त्रिक", "Mechanical Knowledge"),
    ACCIDENT_POLLUTION("safety", "प्रदूषण तथा दुर्घटना सचेतना", "Pollution & Safety"),
    LEGAL_FINES("law", "कानूनी प्रावधान तथा जरिवाना", "Traffic Laws & Fines")
}

@Immutable
data class LicenseQuestion(
    val id: Int,
    val category: LicenseCategory,
    val topic: QuestionTopic,
    val questionNp: String,
    val questionEn: String,
    val optionsNp: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val explanationNp: String,
    val explanationEn: String,
    val signSymbol: String? = null
)

enum class SignCategory(val titleNp: String, val titleEn: String, val shapeDesc: String, val colorHex: Long) {
    MANDATORY("अनिवार्य संकेतहरू (Mandatory)", "Mandatory Signs", "रातो गोलो घेरा (Red Circle)", 0xFFDC2626),
    CAUTIONARY("सचेतनामूलक संकेतहरू (Warning)", "Cautionary Signs", "रातो त्रिकोण (Red Triangle)", 0xFFEA580C),
    INFORMATORY("सूचनामूलक संकेतहरू (Informatory)", "Informatory Signs", "नीलो/हरियो आयत (Blue Rectangle)", 0xFF0284C7),
    ROAD_MARKING("सडक रेखांकन (Road Markings)", "Road Surface Markings", "सडक सतहमा कोरिएका रेखा", 0xFF64748B),
    TRAFFIC_LIGHT("ट्राफिक लाइट तथा इशारा", "Traffic Lights & Signals", "बत्ती तथा ट्राफिक प्रहरीको इशारा", 0xFF16A34A)
}

@Immutable
data class TrafficSignItem(
    val id: String,
    val signKey: String,
    val category: SignCategory,
    val titleNp: String,
    val titleEn: String,
    val meaningNp: String,
    val meaningEn: String,
    val penaltyNp: String,
    val penaltyEn: String,
    val shapeType: String, // "circle", "triangle", "square", "light"
    val drawableRes: Int? = null
)

@Immutable
data class TrialStep(
    val stepNumber: Int,
    val nameNp: String,
    val nameEn: String,
    val totalMarks: Int,
    val deductionsNp: List<String>,
    val deductionsEn: List<String>,
    val tipsNp: String,
    val tipsEn: String
)

data class TrialGuide(
    val category: LicenseCategory,
    val titleNp: String,
    val titleEn: String,
    val totalMarks: Int = 100,
    val passMarks: Int = 70,
    val steps: List<TrialStep>
)

data class MockTestResult(
    val timestamp: Long,
    val category: LicenseCategory,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val timeTakenSeconds: Int,
    val isPassed: Boolean
)
