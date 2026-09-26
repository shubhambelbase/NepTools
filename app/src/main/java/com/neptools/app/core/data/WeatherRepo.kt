package com.neptools.app.core.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

import androidx.compose.runtime.Immutable

@Immutable
data class HourlyForecast(
    val timeLabelNp: String,
    val timeLabelEn: String,
    val tempC: Int,
    val conditionNp: String,
    val conditionEn: String,
    val iconType: String, // "sun", "cloud", "rain"
    val rainProb: Int
)

@Immutable
data class DailyForecast(
    val dayNameNp: String,
    val dayNameEn: String,
    val dateNp: String,
    val dateEn: String,
    val conditionNp: String,
    val conditionEn: String,
    val iconType: String,
    val minTempC: Int,
    val maxTempC: Int,
    val rainProb: Int,
    val humidityPercent: Int
)

@Immutable
data class CityWeather(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val districtNp: String,
    val districtEn: String,
    val provinceNp: String,
    val provinceEn: String,
    val lat: Double,
    val lon: Double,
    val tempC: Int,
    val feelsLikeC: Int,
    val conditionNp: String,
    val conditionEn: String,
    val iconType: String,
    val minTempC: Int,
    val maxTempC: Int,
    val humidityPercent: Int,
    val windKmh: Int,
    val pressureHpa: Int,
    val uvIndex: Int,
    val visibilityKm: Int,
    val aqi: Int,
    val aqiLabelNp: String,
    val aqiLabelEn: String,
    val hourly: List<HourlyForecast>,
    val weekly: List<DailyForecast>
)

object WeatherRepo {

    private fun generateHourly(baseTemp: Int, baseCondition: String, baseIcon: String): List<HourlyForecast> {
        val offsets = listOf(
            Triple("१२ बजे", "12 PM", 1),
            Triple("१ बजे", "1 PM", 2),
            Triple("२ बजे", "2 PM", 3),
            Triple("३ बजे", "3 PM", 2),
            Triple("४ बजे", "4 PM", 1),
            Triple("५ बजे", "5 PM", 0),
            Triple("६ बजे", "6 PM", -2),
            Triple("७ बजे", "7 PM", -3)
        )
        return offsets.mapIndexed { idx, (np, en, diff) ->
            val isAfternoonRain = idx in 3..4 && baseIcon == "rain"
            HourlyForecast(
                timeLabelNp = np,
                timeLabelEn = en,
                tempC = baseTemp + diff,
                conditionNp = if (isAfternoonRain) "हल्का वर्षा" else baseCondition,
                conditionEn = if (isAfternoonRain) "Light Rain" else baseCondition,
                iconType = if (isAfternoonRain) "rain" else baseIcon,
                rainProb = if (isAfternoonRain) 60 else if (baseIcon == "rain") 45 else 15
            )
        }
    }

    private fun generateWeekly(baseTempMin: Int, baseTempMax: Int, baseCondition: String, baseIcon: String): List<DailyForecast> {
        val days = listOf(
            Triple("आज", "Today", "८ भदौ · 24 Aug"),
            Triple("मंगलबार", "Tue", "९ भदौ · 25 Aug"),
            Triple("बुधबार", "Wed", "१० भदौ · 26 Aug"),
            Triple("बिहीबार", "Thu", "११ भदौ · 27 Aug"),
            Triple("शुक्रबार", "Fri", "१२ भदौ · 28 Aug"),
            Triple("शनिबार", "Sat", "१३ भदौ · 29 Aug"),
            Triple("आइतबार", "Sun", "१४ भदौ · 30 Aug")
        )
        val variations = listOf(
            Triple(0, baseCondition, baseIcon),
            Triple(-1, "हल्का वर्षा / बदली", "rain"),
            Triple(1, "घमाइलो तथा सफा", "sun"),
            Triple(0, "आंशिक बदली", "cloud"),
            Triple(-1, "मेघगर्जन सहित वर्षा", "rain"),
            Triple(2, "घमाइलो", "sun"),
            Triple(1, "आंशिक घाम", "cloud")
        )
        return days.mapIndexed { idx, (np, en, dateStr) ->
            val v = variations[idx % variations.size]
            val dates = dateStr.split(" · ")
            DailyForecast(
                dayNameNp = np,
                dayNameEn = en,
                dateNp = dates[0],
                dateEn = dates.getOrElse(1) { "" },
                conditionNp = v.second,
                conditionEn = if (v.third == "rain") "Showers" else if (v.third == "cloud") "Partly Cloudy" else "Sunny",
                iconType = v.third,
                minTempC = baseTempMin + (v.first / 2),
                maxTempC = baseTempMax + v.first,
                rainProb = if (v.third == "rain") 65 else 20,
                humidityPercent = if (v.third == "rain") 80 else 68
            )
        }
    }

    private fun city(
        id: String,
        nameNp: String,
        nameEn: String,
        districtNp: String,
        districtEn: String,
        provinceNp: String,
        provinceEn: String,
        lat: Double,
        lon: Double,
        tempC: Int,
        minTempC: Int,
        maxTempC: Int,
        conditionNp: String,
        conditionEn: String,
        iconType: String,
        humidity: Int = 70,
        wind: Int = 9,
        aqi: Int = 45,
        aqiLabelNp: String = "राम्रो",
        aqiLabelEn: String = "Good"
    ): CityWeather {
        return CityWeather(
            id = id,
            nameNp = nameNp,
            nameEn = nameEn,
            districtNp = districtNp,
            districtEn = districtEn,
            provinceNp = provinceNp,
            provinceEn = provinceEn,
            lat = lat,
            lon = lon,
            tempC = tempC,
            feelsLikeC = tempC + 1,
            conditionNp = conditionNp,
            conditionEn = conditionEn,
            iconType = iconType,
            minTempC = minTempC,
            maxTempC = maxTempC,
            humidityPercent = humidity,
            windKmh = wind,
            pressureHpa = 1012,
            uvIndex = 6,
            visibilityKm = 10,
            aqi = aqi,
            aqiLabelNp = aqiLabelNp,
            aqiLabelEn = aqiLabelEn,
            hourly = generateHourly(tempC, conditionNp, iconType),
            weekly = generateWeekly(minTempC, maxTempC, conditionNp, iconType)
        )
    }

    val nepalCities = listOf(
        // Kathmandu Valley
        city("kathmandu", "काठमाडौँ", "Kathmandu", "काठमाडौँ", "Kathmandu", "बागमती प्रदेश", "Bagmati", 27.7172, 85.3240, 24, 19, 28, "आंशिक बदली", "Partly Cloudy", "cloud", 72, 9, 48, "राम्रो", "Good"),
        city("lalitpur", "ललितपुर (पाटन)", "Lalitpur (Patan)", "ललितपुर", "Lalitpur", "बागमती प्रदेश", "Bagmati", 27.6710, 85.3218, 24, 19, 28, "आंशिक बदली", "Partly Cloudy", "cloud", 70, 8, 46, "राम्रो", "Good"),
        city("bhaktapur", "भक्तपुर", "Bhaktapur", "भक्तपुर", "Bhaktapur", "बागमती प्रदेश", "Bagmati", 27.6710, 85.4298, 24, 18, 27, "आंशिक बदली", "Partly Cloudy", "cloud", 74, 8, 44, "राम्रो", "Good"),

        // Gandaki Province
        city("pokhara", "पोखरा", "Pokhara", "कास्की", "Kaski", "गण्डकी प्रदेश", "Gandaki", 28.2096, 83.9856, 27, 21, 30, "हल्का वर्षा", "Light Rain", "rain", 80, 7, 32, "उत्कृष्ट", "Clean"),
        city("damauli", "दमौली", "Damauli", "तनहुँ", "Tanahun", "गण्डकी प्रदेश", "Gandaki", 27.9714, 84.2861, 29, 23, 33, "आंशिक बदली", "Partly Cloudy", "cloud", 75, 8, 38),
        city("syangja", "स्याङ्जा (पुतलीबजार)", "Syangja", "स्याङ्जा", "Syangja", "गण्डकी प्रदेश", "Gandaki", 28.0967, 83.8742, 26, 20, 29, "हल्का वर्षा", "Light Rain", "rain", 78, 6, 30),
        city("gorkha", "गोरखा", "Gorkha", "गोरखा", "Gorkha", "गण्डकी प्रदेश", "Gandaki", 28.0053, 84.6294, 25, 19, 28, "आंशिक बदली", "Partly Cloudy", "cloud", 76, 7, 34),
        city("besisahar", "बेसीशहर", "Besisahar", "लमजुङ", "Lamjung", "गण्डकी प्रदेश", "Gandaki", 28.2294, 84.3769, 24, 18, 27, "हल्का वर्षा", "Light Rain", "rain", 79, 6, 28),
        city("baglung", "बागलुङ", "Baglung", "बागलुङ", "Baglung", "गण्डकी प्रदेश", "Gandaki", 28.2719, 83.5939, 24, 18, 27, "आंशिक बदली", "Partly Cloudy", "cloud", 74, 6, 28),
        city("beni", "बेनी", "Beni", "म्याग्दी", "Myagdi", "गण्डकी प्रदेश", "Gandaki", 28.3439, 83.5636, 23, 17, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 75, 5, 26),
        city("kusma", "कुश्मा", "Kusma", "पर्वत", "Parbat", "गण्डकी प्रदेश", "Gandaki", 28.2239, 83.6822, 25, 19, 28, "आंशिक बदली", "Partly Cloudy", "cloud", 75, 6, 30),
        city("kawasoti", "कावासोती", "Kawasoti", "नवलपुर", "Nawalpur", "गण्डकी प्रदेश", "Gandaki", 27.6533, 84.1242, 31, 24, 34, "घमाइलो", "Sunny", "sun", 72, 9, 44),
        city("jomsom", "जोमसोम", "Jomsom", "मुस्ताङ", "Mustang", "गण्डकी प्रदेश", "Gandaki", 28.7844, 83.7289, 16, 9, 19, "सफा तथा चिसो", "Cool & Clear", "sun", 48, 16, 18, "उत्कृष्ट", "Clean"),
        city("chame", "चामे", "Chame", "मनाङ", "Manang", "गण्डकी प्रदेश", "Gandaki", 28.5528, 84.2403, 15, 8, 18, "सफा तथा चिसो", "Cool & Clear", "sun", 50, 14, 16, "उत्कृष्ट", "Clean"),

        // Lumbini Province
        city("butwal", "बुटवल", "Butwal", "रुपन्देही", "Rupandehi", "लुम्बिनी प्रदेश", "Lumbini", 27.7006, 83.4484, 32, 25, 35, "तातो / घमाइलो", "Hot & Sunny", "sun", 65, 10, 58, "मध्यम", "Moderate"),
        city("bhairahawa", "भैरहवा (सिद्धार्थनगर)", "Bhairahawa", "रुपन्देही", "Rupandehi", "लुम्बिनी प्रदेश", "Lumbini", 27.5044, 83.4506, 33, 26, 36, "घमाइलो", "Sunny & Hot", "sun", 64, 11, 62, "मध्यम", "Moderate"),
        city("tansen", "तानसेन", "Tansen", "पाल्पा", "Palpa", "लुम्बिनी प्रदेश", "Lumbini", 27.8681, 83.5469, 25, 19, 28, "आंशिक बदली", "Partly Cloudy", "cloud", 74, 8, 34),
        city("ghorahi", "घोराही", "Ghorahi", "दाङ", "Dang", "लुम्बिनी प्रदेश", "Lumbini", 28.0417, 82.4933, 31, 24, 34, "घमाइलो", "Sunny", "sun", 66, 8, 48),
        city("tulsipur", "तुलसीपुर", "Tulsipur", "दाङ", "Dang", "लुम्बिनी प्रदेश", "Lumbini", 28.1311, 82.2961, 31, 24, 34, "घमाइलो", "Sunny", "sun", 65, 9, 46),
        city("lamahi", "लम्ही", "Lamahi", "दाङ", "Dang", "लुम्बिनी प्रदेश", "Lumbini", 27.8767, 82.5694, 32, 25, 35, "घमाइलो", "Sunny", "sun", 64, 9, 50),
        city("sandhikharka", "सन्धिखर्क", "Sandhikharka", "अर्घाखाँची", "Arghakhanchi", "लुम्बिनी प्रदेश", "Lumbini", 27.9733, 83.1311, 25, 19, 28, "आंशिक बदली", "Partly Cloudy", "cloud", 72, 7, 32),
        city("tamghas", "तम्घास", "Tamghas", "गुल्मी", "Gulmi", "लुम्बिनी प्रदेश", "Lumbini", 28.0678, 83.2500, 24, 18, 27, "आंशिक बदली", "Partly Cloudy", "cloud", 74, 6, 30),
        city("taulihawa", "तौलिहवा", "Taulihawa", "कपिलवस्तु", "Kapilvastu", "लुम्बिनी प्रदेश", "Lumbini", 27.5489, 83.0561, 33, 26, 36, "घमाइलो", "Sunny & Hot", "sun", 63, 10, 60),
        city("ramgram", "रामग्राम (परासी)", "Parasi", "नवलपरासी (प)", "Parasi", "लुम्बिनी प्रदेश", "Lumbini", 27.5333, 83.6667, 33, 25, 36, "घमाइलो", "Sunny & Hot", "sun", 66, 9, 56),
        city("pyuthan", "प्युठान", "Pyuthan", "प्युठान", "Pyuthan", "लुम्बिनी प्रदेश", "Lumbini", 28.0833, 82.8833, 26, 20, 29, "आंशिक बदली", "Partly Cloudy", "cloud", 70, 7, 34),
        city("liwang", "लिवाङ", "Liwang", "रोल्पा", "Rolpa", "लुम्बिनी प्रदेश", "Lumbini", 28.3000, 82.6333, 23, 17, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 72, 6, 28),
        city("nepalgunj", "नेपालगञ्ज", "Nepalgunj", "बाँके", "Banke", "लुम्बिनी प्रदेश", "Lumbini", 28.0500, 81.6167, 34, 27, 38, "घमाइलो", "Sunny & Hot", "sun", 58, 14, 65, "मध्यम", "Moderate"),
        city("kohalpur", "कोहलपुर", "Kohalpur", "बाँके", "Banke", "लुम्बिनी प्रदेश", "Lumbini", 28.1833, 81.6833, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 60, 12, 62),
        city("gulariya", "गुलरिया", "Gulariya", "बर्दिया", "Bardia", "लुम्बिनी प्रदेश", "Lumbini", 28.2333, 81.3333, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 62, 11, 58),

        // Bagmati Province
        city("chitwan", "चितवन (भरतपुर)", "Chitwan (Bharatpur)", "चितवन", "Chitwan", "बागमती प्रदेश", "Bagmati", 27.6833, 84.4333, 31, 24, 34, "घाम लागेको", "Mostly Sunny", "sun", 75, 6, 42, "राम्रो", "Good"),
        city("ratnanagar", "रत्ननगर (सौराह)", "Ratnanagar (Sauraha)", "चितवन", "Chitwan", "बागमती प्रदेश", "Bagmati", 27.6167, 84.5167, 31, 24, 34, "घमाइलो", "Sunny", "sun", 74, 6, 40),
        city("hetauda", "हेटौँडा", "Hetauda", "मकवानपुर", "Makwanpur", "बागमती प्रदेश", "Bagmati", 27.4289, 85.0333, 29, 23, 32, "आंशिक बदली", "Partly Cloudy", "cloud", 75, 7, 44),
        city("banepa", "बनेपा", "Banepa", "काभ्रे", "Kavre", "बागमती प्रदेश", "Bagmati", 27.6297, 85.5217, 24, 18, 27, "आंशिक बदली", "Partly Cloudy", "cloud", 72, 8, 42),
        city("dhulikhel", "धुलिखेल", "Dhulikhel", "काभ्रे", "Kavre", "बागमती प्रदेश", "Bagmati", 27.6253, 85.5561, 23, 17, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 73, 8, 40),
        city("panauti", "पनौती", "Panauti", "काभ्रे", "Kavre", "बागमती प्रदेश", "Bagmati", 27.5833, 85.5167, 23, 17, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 73, 7, 38),
        city("bidur", "विदुर (त्रिशूली)", "Bidur (Trishuli)", "नुवाकोट", "Nuwakot", "बागमती प्रदेश", "Bagmati", 27.9167, 85.1667, 28, 22, 31, "आंशिक बदली", "Partly Cloudy", "cloud", 74, 8, 38),
        city("dhading", "धादिङबेसी", "Dhadingbesi", "धादिङ", "Dhading", "बागमती प्रदेश", "Bagmati", 27.8667, 84.9000, 27, 21, 30, "आंशिक बदली", "Partly Cloudy", "cloud", 75, 7, 36),
        city("sindhuli", "सिन्धुलीमाढी", "Sindhulimadhi", "सिन्धुली", "Sindhuli", "बागमती प्रदेश", "Bagmati", 27.2500, 85.9667, 30, 23, 33, "घमाइलो", "Sunny", "sun", 71, 8, 42),
        city("charikot", "चरीकोट", "Charikot", "दोलखा", "Dolakha", "बागमती प्रदेश", "Bagmati", 27.6667, 86.0333, 19, 13, 22, "आंशिक बदली", "Partly Cloudy", "cloud", 78, 8, 24),
        city("manthali", "मन्थली", "Manthali", "रामेछाप", "Ramechhap", "बागमती प्रदेश", "Bagmati", 27.4833, 86.0667, 28, 22, 31, "घमाइलो", "Sunny", "sun", 70, 7, 36),
        city("chautara", "चौतारा", "Chautara", "सिन्धुपाल्चोक", "Sindhupalchok", "बागमती प्रदेश", "Bagmati", 27.7833, 85.7167, 22, 16, 25, "हल्का वर्षा", "Light Rain", "rain", 80, 7, 26),
        city("dhunche", "धुन्चे", "Dhunche", "रसुवा", "Rasuwa", "बागमती प्रदेश", "Bagmati", 28.1167, 85.3000, 18, 12, 21, "आंशिक बदली", "Partly Cloudy", "cloud", 76, 9, 22),

        // Koshi Province
        city("biratnagar", "विराटनगर", "Biratnagar", "मोरङ", "Morang", "कोशी प्रदेश", "Koshi", 26.4525, 87.2718, 32, 26, 36, "घमाइलो", "Sunny & Clear", "sun", 68, 12, 55, "मध्यम", "Moderate"),
        city("dharan", "धरान", "Dharan", "सुनसरी", "Sunsari", "कोशी प्रदेश", "Koshi", 26.8124, 87.2834, 29, 23, 32, "आंशिक बदली", "Partly Cloudy", "cloud", 74, 9, 36, "राम्रो", "Good"),
        city("itahari", "इटहरी", "Itahari", "सुनसरी", "Sunsari", "कोशी प्रदेश", "Koshi", 26.6667, 87.2833, 31, 25, 34, "घमाइलो", "Sunny", "sun", 70, 10, 48),
        city("birtamod", "बिर्तामोड", "Birtamod", "झापा", "Jhapa", "कोशी प्रदेश", "Koshi", 26.6333, 87.9833, 31, 25, 34, "घमाइलो", "Sunny", "sun", 72, 10, 46),
        city("damak", "दमक", "Damak", "झापा", "Jhapa", "कोशी प्रदेश", "Koshi", 26.6667, 87.7000, 31, 25, 34, "घमाइलो", "Sunny", "sun", 71, 9, 45),
        city("ilam", "इलाम", "Ilam", "इलाम", "Ilam", "कोशी प्रदेश", "Koshi", 26.9100, 87.9300, 23, 17, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 82, 6, 26),
        city("dhankuta", "धनकुटा", "Dhankuta", "धनकुटा", "Dhankuta", "कोशी प्रदेश", "Koshi", 26.9833, 87.3333, 24, 18, 27, "आंशिक बदली", "Partly Cloudy", "cloud", 76, 7, 30),
        city("gaighat", "गाईघाट", "Gaighat", "उदयपुर", "Udayapur", "कोशी प्रदेश", "Koshi", 26.7833, 86.7000, 31, 25, 35, "घमाइलो", "Sunny", "sun", 69, 9, 48),
        city("phidim", "फिदिम", "Phidim", "पाँचथर", "Panchthar", "कोशी प्रदेश", "Koshi", 27.1500, 87.7667, 23, 16, 25, "आंशिक बदली", "Partly Cloudy", "cloud", 80, 6, 24),
        city("taplejung", "ताप्लेजुङ (फुङलिङ)", "Taplejung", "ताप्लेजुङ", "Taplejung", "कोशी प्रदेश", "Koshi", 27.3500, 87.6667, 21, 14, 23, "हल्का वर्षा", "Light Rain", "rain", 82, 7, 22),
        city("namche", "नाम्चे बजार", "Namche Bazaar", "सोलुखुम्बु", "Solukhumbu", "कोशी प्रदेश", "Koshi", 27.8056, 86.7139, 14, 7, 16, "सफा तथा चिसो", "Cool & Clear", "sun", 62, 14, 12, "उत्कृष्ट", "Clean"),
        city("khandbari", "खाँदबारी", "Khandbari", "संखुवासभा", "Sankhuwasabha", "कोशी प्रदेश", "Koshi", 27.3742, 87.2039, 24, 18, 27, "आंशिक बदली", "Partly Cloudy", "cloud", 78, 6, 26),
        city("bhojpur", "भोजपुर", "Bhojpur", "भोजपुर", "Bhojpur", "कोशी प्रदेश", "Koshi", 27.1714, 87.0469, 23, 17, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 76, 6, 25),
        city("diktel", "दिक्तेल", "Diktel", "खोटाङ", "Khotang", "कोशी प्रदेश", "Koshi", 27.2167, 86.7833, 23, 17, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 77, 6, 26),
        city("okhaldhunga", "ओखलढुङ्गा", "Okhaldhunga", "ओखलढुङ्गा", "Okhaldhunga", "कोशी प्रदेश", "Koshi", 27.3167, 86.5000, 22, 16, 25, "आंशिक बदली", "Partly Cloudy", "cloud", 78, 7, 24),

        // Madhesh Province
        city("janakpur", "जनकपुरधाम", "Janakpur", "धनुषा", "Dhanusha", "मधेश प्रदेश", "Madhesh", 26.7288, 85.9244, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 66, 11, 58, "मध्यम", "Moderate"),
        city("birgunj", "वीरगञ्ज", "Birgunj", "पर्सा", "Parsa", "मधेश प्रदेश", "Madhesh", 27.0139, 84.8789, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 65, 12, 64, "मध्यम", "Moderate"),
        city("lahan", "लहान", "Lahan", "सिराहा", "Siraha", "मधेश प्रदेश", "Madhesh", 26.7167, 86.4833, 33, 26, 36, "घमाइलो", "Sunny", "sun", 67, 10, 56),
        city("rajbiraj", "राजविराज", "Rajbiraj", "सप्तरी", "Saptari", "मधेश प्रदेश", "Madhesh", 26.5417, 86.7556, 32, 26, 36, "घमाइलो", "Sunny", "sun", 68, 10, 54),
        city("kalaiya", "कलैया", "Kalaiya", "बारा", "Bara", "मधेश प्रदेश", "Madhesh", 27.0333, 85.0000, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 65, 11, 60),
        city("gaur", "गौर", "Gaur", "रौतहट", "Rautahat", "मधेश प्रदेश", "Madhesh", 26.7667, 85.2833, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 66, 10, 58),
        city("malangwa", "मलङ्गवा", "Malangwa", "सर्लाही", "Sarlahi", "मधेश प्रदेश", "Madhesh", 26.8667, 85.5500, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 66, 10, 56),
        city("jaleshwor", "जलेश्वर", "Jaleshwor", "महोत्तरी", "Mahottari", "मधेश प्रदेश", "Madhesh", 26.6500, 85.8000, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 67, 10, 58),

        // Karnali Province
        city("surkhet", "सुर्खेत (वीरेन्द्रनगर)", "Surkhet", "सुर्खेत", "Surkhet", "कर्णाली प्रदेश", "Karnali", 28.6000, 81.6333, 29, 22, 32, "घमाइलो", "Sunny", "sun", 68, 8, 38, "राम्रो", "Good"),
        city("jumla", "जुम्ला (खलङ्गा)", "Jumla", "जुम्ला", "Jumla", "कर्णाली प्रदेश", "Karnali", 29.2747, 82.1839, 20, 11, 23, "सफा तथा घमाइलो", "Sunny & Pleasant", "sun", 55, 10, 16, "उत्कृष्ट", "Clean"),
        city("dailekh", "दैलेख", "Dailekh", "दैलेख", "Dailekh", "कर्णाली प्रदेश", "Karnali", 28.8333, 81.7167, 26, 19, 29, "आंशिक बदली", "Partly Cloudy", "cloud", 70, 7, 30),
        city("salyan", "सल्यान (खलङ्गा)", "Salyan", "सल्यान", "Salyan", "कर्णाली प्रदेश", "Karnali", 28.3667, 82.1667, 26, 19, 29, "आंशिक बदली", "Partly Cloudy", "cloud", 71, 7, 32),
        city("simikot", "सिमिकोट", "Simikot", "हुम्ला", "Humla", "कर्णाली प्रदेश", "Karnali", 29.9667, 81.8333, 16, 8, 19, "सफा तथा चिसो", "Cool & Clear", "sun", 50, 15, 14, "उत्कृष्ट", "Clean"),

        // Sudurpashchim Province
        city("dhangadhi", "धनगढी", "Dhangadhi", "कैलाली", "Kailali", "सुदूरपश्चिम प्रदेश", "Sudurpashchim", 28.6833, 80.6000, 34, 26, 38, "घमाइलो", "Sunny & Hot", "sun", 60, 12, 58, "मध्यम", "Moderate"),
        city("tikapur", "टिकापुर", "Tikapur", "कैलाली", "Kailali", "सुदूरपश्चिम प्रदेश", "Sudurpashchim", 28.5000, 81.1333, 33, 26, 37, "घमाइलो", "Sunny", "sun", 62, 11, 55),
        city("mahendranagar", "महेन्द्रनगर (भीमदत्त)", "Mahendranagar", "कञ्चनपुर", "Kanchanpur", "सुदूरपश्चिम प्रदेश", "Sudurpashchim", 28.9667, 80.1833, 33, 26, 37, "घमाइलो", "Sunny & Hot", "sun", 61, 12, 56),
        city("dadeldhura", "डडेलधुरा (अमरगढी)", "Dadeldhura", "डडेलधुरा", "Dadeldhura", "सुदूरपश्चिम प्रदेश", "Sudurpashchim", 29.3000, 80.5833, 22, 15, 25, "आंशिक बदली", "Partly Cloudy", "cloud", 72, 8, 24),
        city("dipayal", "दिपायल सिलगढी", "Dipayal Silgadhi", "डोटी", "Doti", "सुदूरपश्चिम प्रदेश", "Sudurpashchim", 29.2667, 80.9333, 29, 22, 33, "घमाइलो", "Sunny", "sun", 66, 8, 34),
        city("baitadi", "बैतडी (दशरथचन्द)", "Baitadi", "बैतडी", "Baitadi", "सुदूरपश्चिम प्रदेश", "Sudurpashchim", 29.5333, 80.4667, 23, 16, 26, "आंशिक बदली", "Partly Cloudy", "cloud", 73, 7, 25),
        city("darchula", "दार्चुला (खलङ्गा)", "Darchula", "दार्चुला", "Darchula", "सुदूरपश्चिम प्रदेश", "Sudurpashchim", 29.8500, 80.5333, 24, 17, 27, "आंशिक बदली", "Partly Cloudy", "cloud", 74, 7, 24)
    )

    val defaultWeather: CityWeather
        get() = nepalCities.first()

    fun findClosestCity(lat: Double, lon: Double): CityWeather {
        var closest = nepalCities.first()
        var minDistance = Double.MAX_VALUE

        for (city in nepalCities) {
            val d = calculateDistanceKm(lat, lon, city.lat, city.lon)
            if (d < minDistance) {
                minDistance = d
                closest = city
            }
        }
        return closest
    }

    fun createDynamicWeatherForLocation(
        detectedName: String,
        district: String,
        province: String,
        lat: Double,
        lon: Double
    ): CityWeather {
        val closest = findClosestCity(lat, lon)
        return closest.copy(
            id = "custom_${System.currentTimeMillis()}",
            nameNp = detectedName,
            nameEn = detectedName,
            districtNp = district,
            districtEn = district,
            provinceNp = province,
            provinceEn = province,
            lat = lat,
            lon = lon
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // ------------------------------------------------------------------
    // Live weather (Open-Meteo) - real data with disk cache + fallback
    // ------------------------------------------------------------------
    data class LiveWeather(
        val weather: CityWeather,
        val fetchedAtMillis: Long,
        val stale: Boolean
    )

    private const val LIVE_CACHE_FILE = "weather_live_cache.json"
    private const val LIVE_TTL_MS = 30L * 60 * 1000L
    private val liveExecutor = Executors.newSingleThreadExecutor()
    private val liveHandler = Handler(Looper.getMainLooper())

    fun loadLiveCached(context: Context): LiveWeather? {
        val f = File(context.filesDir, LIVE_CACHE_FILE)
        if (!f.exists()) return null
        return runCatching {
            val o = JSONObject(f.readText())
            val city = cityFromJson(o.getJSONObject("weather"))
            val fetchedAt = o.getLong("fetchedAt")
            LiveWeather(city, fetchedAt, System.currentTimeMillis() - fetchedAt > LIVE_TTL_MS)
        }.getOrNull()
    }

    fun refreshLive(context: Context, city: CityWeather, onResult: (LiveWeather?) -> Unit) {
        val cached = loadLiveCached(context)
        if (cached != null && !cached.stale && samePlace(cached.weather, city)) {
            onResult(cached)
            return
        }
        liveExecutor.execute {
            val result = fetchLive(city)
            if (result != null) {
                runCatching {
                    com.neptools.app.core.util.SafeFileWriter.writeAtomic(
                        File(context.filesDir, LIVE_CACHE_FILE),
                        JSONObject()
                            .put("fetchedAt", result.fetchedAtMillis)
                            .put("weather", cityToJson(result.weather))
                            .toString()
                    )
                }
                com.neptools.app.core.updates.RecentUpdatesManager.recordSuccessfulUpdate(
                    context = context,
                    serviceId = "weather",
                    nameNp = "मौसम पूर्वानुमान",
                    nameEn = "Weather & Forecast",
                    route = com.neptools.app.ui.navigation.Routes.WEATHER,
                    iconType = "sun",
                    timestampMillis = result.fetchedAtMillis,
                    statusNp = "${NepaliNames.toDevanagari(result.weather.tempC)}°C · ${result.weather.conditionNp}",
                    statusEn = "${result.weather.tempC}°C · ${result.weather.conditionEn}"
                )
            }
            liveHandler.post { onResult(result ?: cached) }
        }
    }

    private fun samePlace(a: CityWeather, b: CityWeather): Boolean =
        Math.round(a.lat * 10.0) == Math.round(b.lat * 10.0) &&
                Math.round(a.lon * 10.0) == Math.round(b.lon * 10.0)

    private fun fetchLive(city: CityWeather): LiveWeather? {
        val now = System.currentTimeMillis()
        val weather = fetchForecast(city) ?: return null
        return LiveWeather(weather, now, false)
    }

    private fun fetchForecast(city: CityWeather): CityWeather? {
        var conn: HttpURLConnection? = null
        return try {
            val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${city.lat}&longitude=${city.lon}" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,surface_pressure,wind_speed_10m" +
                    "&hourly=temperature_2m,weather_code,precipitation_probability" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                    "&timezone=auto&forecast_days=7"
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)

            val current = root.getJSONObject("current")
            val code = current.optInt("weather_code", 0)
            val (condNp, condEn, icon) = wmo(code)
            val temp = current.optDouble("temperature_2m", city.tempC.toDouble()).roundToInt()
            val humidity = current.optInt("relative_humidity_2m", city.humidityPercent)

            val hourly = parseHourly(root.getJSONObject("hourly"), city)
            val weekly = parseWeekly(root.getJSONObject("daily"), humidity, city)

            val aqiData = fetchAirQuality(city.lat, city.lon)

            city.copy(
                tempC = temp,
                feelsLikeC = current.optDouble("apparent_temperature", temp.toDouble()).roundToInt(),
                conditionNp = condNp,
                conditionEn = condEn,
                iconType = icon,
                minTempC = weekly.firstOrNull()?.minTempC ?: city.minTempC,
                maxTempC = weekly.firstOrNull()?.maxTempC ?: city.maxTempC,
                humidityPercent = humidity,
                windKmh = current.optDouble("wind_speed_10m", city.windKmh.toDouble()).roundToInt(),
                pressureHpa = current.optDouble("surface_pressure", city.pressureHpa.toDouble()).roundToInt(),
                uvIndex = aqiData?.second ?: city.uvIndex,
                aqi = aqiData?.first ?: city.aqi,
                aqiLabelNp = aqiData?.first?.let { aqiLabelNp(it) } ?: city.aqiLabelNp,
                aqiLabelEn = aqiData?.first?.let { aqiLabelEn(it) } ?: city.aqiLabelEn,
                hourly = hourly,
                weekly = weekly
            )
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun fetchAirQuality(lat: Double, lon: Double): Pair<Int, Int>? {
        var conn: HttpURLConnection? = null
        return try {
            val url = "https://air-quality-api.open-meteo.com/v1/air-quality" +
                    "?latitude=$lat&longitude=$lon&current=us_aqi,uv_index&timezone=auto"
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            val aqi = current.optDouble("us_aqi", Double.NaN)
            val uv = current.optDouble("uv_index", Double.NaN)
            Pair(
                if (aqi.isNaN()) 45 else aqi.roundToInt(),
                if (uv.isNaN()) 6 else uv.roundToInt()
            )
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun parseHourly(hourly: JSONObject, city: CityWeather): List<HourlyForecast> {
        return runCatching {
            val times = hourly.getJSONArray("time")
            val temps = hourly.getJSONArray("temperature_2m")
            val codes = hourly.getJSONArray("weather_code")
            val rain = hourly.getJSONArray("precipitation_probability")
            val nowPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH"))
            var idx = (0 until times.length()).indexOfFirst { times.getString(it) >= nowPrefix }
            if (idx < 0) idx = 0
            val out = mutableListOf<HourlyForecast>()
            for (i in idx until minOf(idx + 8, times.length())) {
                val hour = times.getString(i).substring(11, 13).toIntOrNull() ?: continue
                val hour12 = if (hour % 12 == 0) 12 else hour % 12
                val amPm = if (hour < 12) "AM" else "PM"
                val (condNp, condEn, icon) = wmo(codes.optInt(i, 0))
                out.add(
                    HourlyForecast(
                        timeLabelNp = "${NepaliNames.toDevanagari(hour12)} बजे",
                        timeLabelEn = "$hour12 $amPm",
                        tempC = temps.optDouble(i, Double.NaN).roundToInt(),
                        conditionNp = condNp,
                        conditionEn = condEn,
                        iconType = icon,
                        rainProb = rain.optInt(i, 0)
                    )
                )
            }
            out
        }.getOrDefault(city.hourly)
    }

    private fun parseWeekly(daily: JSONObject, humidity: Int, city: CityWeather): List<DailyForecast> {
        return runCatching {
            val times = daily.getJSONArray("time")
            val codes = daily.getJSONArray("weather_code")
            val tMax = daily.getJSONArray("temperature_2m_max")
            val tMin = daily.getJSONArray("temperature_2m_min")
            val rain = daily.getJSONArray("precipitation_probability_max")
            val out = mutableListOf<DailyForecast>()
            for (i in 0 until minOf(7, times.length())) {
                val date = LocalDate.parse(times.getString(i))
                val dow = date.dayOfWeek
                val (condNp, condEn, icon) = wmo(codes.optInt(i, 0))
                val bs: NepaliDate? = runCatching { PatroRepo.d.engine.adToBs(date) }.getOrNull()
                out.add(
                    DailyForecast(
                        dayNameNp = NepaliNames.weekdaysNp[dow.value % 7],
                        dayNameEn = dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        dateNp = bs?.let { "${NepaliNames.toDevanagari(it.day)} ${NepaliNames.monthsNp[it.month - 1]}" } ?: "",
                        dateEn = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}",
                        conditionNp = condNp,
                        conditionEn = condEn,
                        iconType = icon,
                        minTempC = tMin.optDouble(i, Double.NaN).roundToInt(),
                        maxTempC = tMax.optDouble(i, Double.NaN).roundToInt(),
                        rainProb = rain.optInt(i, 20),
                        humidityPercent = humidity
                    )
                )
            }
            out
        }.getOrDefault(city.weekly)
    }

    private fun wmo(code: Int): Triple<String, String, String> = when (code) {
        0 -> Triple("सफा आकाश", "Clear Sky", "sun")
        1 -> Triple("प्रायः सफा", "Mainly Clear", "sun")
        2 -> Triple("आंशिक बदली", "Partly Cloudy", "cloud")
        3 -> Triple("बदली लागेको", "Overcast", "cloud")
        45, 48 -> Triple("कुहिरो", "Foggy", "cloud")
        51, 53, 55 -> Triple("सिमसिम पानी", "Drizzle", "rain")
        56, 57 -> Triple("चिसो सिमसिम पानी", "Freezing Drizzle", "rain")
        61 -> Triple("हल्का वर्षा", "Light Rain", "rain")
        63 -> Triple("मध्यम वर्षा", "Moderate Rain", "rain")
        65 -> Triple("भारी वर्षा", "Heavy Rain", "rain")
        66, 67 -> Triple("चिसो वर्षा", "Freezing Rain", "rain")
        71 -> Triple("हल्का हिमपात", "Light Snow", "cloud")
        73, 75 -> Triple("हिमपात", "Snowfall", "cloud")
        77 -> Triple("हिउँका कण", "Snow Grains", "cloud")
        80, 81 -> Triple("वर्षाका झरी", "Rain Showers", "rain")
        82 -> Triple("भीषण झरी", "Violent Showers", "rain")
        85, 86 -> Triple("हिउँका झरी", "Snow Showers", "cloud")
        95 -> Triple("चट्याङसहित वर्षा", "Thunderstorm", "rain")
        96, 99 -> Triple("असिनासहित आँधी", "Thunderstorm with Hail", "rain")
        else -> Triple("आंशिक बदली", "Partly Cloudy", "cloud")
    }

    private fun aqiLabelNp(aqi: Int): String = when {
        aqi <= 50 -> "राम्रो"
        aqi <= 100 -> "मध्यम"
        aqi <= 150 -> "प्रतिकूल"
        aqi <= 200 -> "अस्वस्थ"
        else -> "धेरै अस्वस्थ"
    }

    private fun aqiLabelEn(aqi: Int): String = when {
        aqi <= 50 -> "Good"
        aqi <= 100 -> "Moderate"
        aqi <= 150 -> "Unhealthy (Sensitive)"
        aqi <= 200 -> "Unhealthy"
        else -> "Very Unhealthy"
    }

    fun cityToJson(city: CityWeather): JSONObject = JSONObject().apply {
        put("id", city.id)
        put("nameNp", city.nameNp)
        put("nameEn", city.nameEn)
        put("districtNp", city.districtNp)
        put("districtEn", city.districtEn)
        put("provinceNp", city.provinceNp)
        put("provinceEn", city.provinceEn)
        put("lat", city.lat)
        put("lon", city.lon)
        put("tempC", city.tempC)
        put("feelsLikeC", city.feelsLikeC)
        put("conditionNp", city.conditionNp)
        put("conditionEn", city.conditionEn)
        put("iconType", city.iconType)
        put("minTempC", city.minTempC)
        put("maxTempC", city.maxTempC)
        put("humidityPercent", city.humidityPercent)
        put("windKmh", city.windKmh)
        put("pressureHpa", city.pressureHpa)
        put("uvIndex", city.uvIndex)
        put("visibilityKm", city.visibilityKm)
        put("aqi", city.aqi)
        put("aqiLabelNp", city.aqiLabelNp)
        put("aqiLabelEn", city.aqiLabelEn)
        put("hourly", JSONArray().apply {
            city.hourly.forEach { h ->
                put(JSONObject().apply {
                    put("timeLabelNp", h.timeLabelNp)
                    put("timeLabelEn", h.timeLabelEn)
                    put("tempC", h.tempC)
                    put("conditionNp", h.conditionNp)
                    put("conditionEn", h.conditionEn)
                    put("iconType", h.iconType)
                    put("rainProb", h.rainProb)
                })
            }
        })
        put("weekly", JSONArray().apply {
            city.weekly.forEach { w ->
                put(JSONObject().apply {
                    put("dayNameNp", w.dayNameNp)
                    put("dayNameEn", w.dayNameEn)
                    put("dateNp", w.dateNp)
                    put("dateEn", w.dateEn)
                    put("conditionNp", w.conditionNp)
                    put("conditionEn", w.conditionEn)
                    put("iconType", w.iconType)
                    put("minTempC", w.minTempC)
                    put("maxTempC", w.maxTempC)
                    put("rainProb", w.rainProb)
                    put("humidityPercent", w.humidityPercent)
                })
            }
        })
    }

    fun cityFromJson(json: JSONObject): CityWeather {
        val hourlyList = mutableListOf<HourlyForecast>()
        json.optJSONArray("hourly")?.let { arr ->
            for (i in 0 until arr.length()) {
                val h = arr.getJSONObject(i)
                hourlyList.add(
                    HourlyForecast(
                        timeLabelNp = h.getString("timeLabelNp"),
                        timeLabelEn = h.getString("timeLabelEn"),
                        tempC = h.getInt("tempC"),
                        conditionNp = h.getString("conditionNp"),
                        conditionEn = h.getString("conditionEn"),
                        iconType = h.getString("iconType"),
                        rainProb = h.getInt("rainProb")
                    )
                )
            }
        }
        val weeklyList = mutableListOf<DailyForecast>()
        json.optJSONArray("weekly")?.let { arr ->
            for (i in 0 until arr.length()) {
                val w = arr.getJSONObject(i)
                weeklyList.add(
                    DailyForecast(
                        dayNameNp = w.getString("dayNameNp"),
                        dayNameEn = w.getString("dayNameEn"),
                        dateNp = w.getString("dateNp"),
                        dateEn = w.getString("dateEn"),
                        conditionNp = w.getString("conditionNp"),
                        conditionEn = w.getString("conditionEn"),
                        iconType = w.getString("iconType"),
                        minTempC = w.getInt("minTempC"),
                        maxTempC = w.getInt("maxTempC"),
                        rainProb = w.getInt("rainProb"),
                        humidityPercent = w.getInt("humidityPercent")
                    )
                )
            }
        }
        val def = defaultWeather
        return CityWeather(
            id = json.getString("id"),
            nameNp = json.getString("nameNp"),
            nameEn = json.getString("nameEn"),
            districtNp = json.getString("districtNp"),
            districtEn = json.getString("districtEn"),
            provinceNp = json.getString("provinceNp"),
            provinceEn = json.getString("provinceEn"),
            lat = json.getDouble("lat"),
            lon = json.getDouble("lon"),
            tempC = json.getInt("tempC"),
            feelsLikeC = json.getInt("feelsLikeC"),
            conditionNp = json.getString("conditionNp"),
            conditionEn = json.getString("conditionEn"),
            iconType = json.getString("iconType"),
            minTempC = json.getInt("minTempC"),
            maxTempC = json.getInt("maxTempC"),
            humidityPercent = json.getInt("humidityPercent"),
            windKmh = json.getInt("windKmh"),
            pressureHpa = json.getInt("pressureHpa"),
            uvIndex = json.getInt("uvIndex"),
            visibilityKm = json.getInt("visibilityKm"),
            aqi = json.getInt("aqi"),
            aqiLabelNp = json.getString("aqiLabelNp"),
            aqiLabelEn = json.getString("aqiLabelEn"),
            hourly = hourlyList.ifEmpty { def.hourly },
            weekly = weeklyList.ifEmpty { def.weekly }
        )
    }
}
