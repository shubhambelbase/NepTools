package com.neptools.app.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.neptools.app.core.data.PlacesRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ResolvedEmergencyLocation(
    val districtEn: String,     // e.g. "Kaski", "Kathmandu", "All"
    val districtNp: String,     // e.g. "कास्की", "काठमाडौं", "सबै"
    val provinceEn: String,     // e.g. "Gandaki", "Bagmati", "National"
    val provinceNp: String,     // e.g. "गण्डकी प्रदेश", "बागमती प्रदेश", "राष्ट्रिय"
    val isAutoGps: Boolean      // true if auto-detected via GPS, false if selected manually
)

object EmergencyLocationResolver {

    private const val PREFS = "emergency_location_prefs"
    private const val KEY_DISTRICT_EN = "district_en"
    private const val KEY_DISTRICT_NP = "district_np"
    private const val KEY_PROVINCE_EN = "province_en"
    private const val KEY_PROVINCE_NP = "province_np"
    private const val KEY_IS_AUTO_GPS = "is_auto_gps"

    val DEFAULT_LOCATION = ResolvedEmergencyLocation(
        districtEn = "All",
        districtNp = "सबै नेपाल",
        provinceEn = "National",
        provinceNp = "राष्ट्रिय",
        isAutoGps = false
    )

    val PROVINCE_MAP = mapOf(
        "Koshi" to "कोशी प्रदेश",
        "Madhesh" to "मधेश प्रदेश",
        "Bagmati" to "बागमती प्रदेश",
        "Gandaki" to "गण्डकी प्रदेश",
        "Lumbini" to "लुम्बिनी प्रदेश",
        "Karnali" to "कर्णाली प्रदेश",
        "Sudurpashchim" to "सुदूरपश्चिम प्रदेश"
    )

    private val _currentLocation = MutableStateFlow(DEFAULT_LOCATION)
    val currentLocation: StateFlow<ResolvedEmergencyLocation> = _currentLocation.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        loadFromPrefs(context)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    private fun loadFromPrefs(context: Context) {
        val p = getPrefs(context)
        val dEn = p.getString(KEY_DISTRICT_EN, null)
        val dNp = p.getString(KEY_DISTRICT_NP, null)
        val prEn = p.getString(KEY_PROVINCE_EN, null)
        val prNp = p.getString(KEY_PROVINCE_NP, null)
        val auto = p.getBoolean(KEY_IS_AUTO_GPS, false)

        if (!dEn.isNullOrBlank() && !prEn.isNullOrBlank()) {
            _currentLocation.value = ResolvedEmergencyLocation(
                districtEn = dEn,
                districtNp = dNp ?: dEn,
                provinceEn = prEn,
                provinceNp = prNp ?: prEn,
                isAutoGps = auto
            )
        }
    }

    fun setManualLocation(
        context: Context,
        districtEn: String,
        districtNp: String,
        provinceEn: String,
        provinceNp: String
    ) {
        val loc = ResolvedEmergencyLocation(
            districtEn = districtEn,
            districtNp = districtNp,
            provinceEn = provinceEn,
            provinceNp = provinceNp,
            isAutoGps = false
        )
        _currentLocation.value = loc
        getPrefs(context).edit()
            .putString(KEY_DISTRICT_EN, districtEn)
            .putString(KEY_DISTRICT_NP, districtNp)
            .putString(KEY_PROVINCE_EN, provinceEn)
            .putString(KEY_PROVINCE_NP, provinceNp)
            .putBoolean(KEY_IS_AUTO_GPS, false)
            .apply()
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun requestGpsLocation(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        if (!hasLocationPermission(context)) {
            onComplete?.invoke(false)
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onComplete?.invoke(false)
            return
        }

        _isLocating.value = true

        try {
            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val lastPassive = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            val bestLoc = listOfNotNull(lastGps, lastNetwork, lastPassive).maxByOrNull { it.time }
            if (bestLoc != null) {
                processResolvedLocation(context, bestLoc)
            }

            var listener: LocationListener? = null
            val handler = Handler(Looper.getMainLooper())

            listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    processResolvedLocation(context, loc)
                    _isLocating.value = false
                    listener?.let { try { lm.removeUpdates(it) } catch (_: Exception) {} }
                    onComplete?.invoke(true)
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    _isLocating.value = false
                    listener?.let { try { lm.removeUpdates(it) } catch (_: Exception) {} }
                    onComplete?.invoke(false)
                }
            }

            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10f, listener, Looper.getMainLooper())
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 10f, listener, Looper.getMainLooper())
            }

            handler.postDelayed({
                if (_isLocating.value) {
                    _isLocating.value = false
                    listener?.let { try { lm.removeUpdates(it) } catch (_: Exception) {} }
                    onComplete?.invoke(bestLoc != null)
                }
            }, 7500L)

        } catch (e: Exception) {
            _isLocating.value = false
            onComplete?.invoke(false)
        }
    }

    private fun processResolvedLocation(context: Context, location: Location) {
        val lat = location.latitude
        val lon = location.longitude

        mainScope.launch(Dispatchers.IO) {
            var matchedDistrictEn: String? = null
            var matchedDistrictNp: String? = null
            var matchedProvinceEn: String? = null

            // 1. Try Geocoder for administrative boundaries
            if (Geocoder.isPresent()) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val list: List<Address>? = geocoder.getFromLocation(lat, lon, 1)
                    val addr = list?.firstOrNull()
                    if (addr != null) {
                        val subAdmin = addr.subAdminArea
                        val admin = addr.adminArea
                        if (!subAdmin.isNullOrBlank()) {
                            matchedDistrictEn = subAdmin.replace("District", "", ignoreCase = true).trim()
                        }
                        if (!admin.isNullOrBlank()) {
                            matchedProvinceEn = cleanProvinceName(admin)
                        }
                    }
                } catch (_: Exception) {}
            }

            // 2. Offline nearest distance fallback using PlacesRepo (852 locations)
            if (matchedDistrictEn.isNullOrBlank() || matchedProvinceEn.isNullOrBlank()) {
                val nearest = findNearestDistrict(lat, lon)
                if (nearest != null) {
                    if (matchedDistrictEn.isNullOrBlank()) {
                        matchedDistrictEn = nearest.districtEn
                        matchedDistrictNp = nearest.districtNp
                    }
                    if (matchedProvinceEn.isNullOrBlank()) {
                        matchedProvinceEn = nearest.provinceEn
                    }
                }
            }

            val finalDistEn = matchedDistrictEn ?: "Kathmandu"
            val finalProvEn = matchedProvinceEn ?: "Bagmati"
            val finalDistNp = matchedDistrictNp ?: getDistrictNpName(finalDistEn)
            val finalProvNp = PROVINCE_MAP[finalProvEn] ?: finalProvEn

            val result = ResolvedEmergencyLocation(
                districtEn = finalDistEn,
                districtNp = finalDistNp,
                provinceEn = finalProvEn,
                provinceNp = finalProvNp,
                isAutoGps = true
            )

            launch(Dispatchers.Main) {
                _currentLocation.value = result
                getPrefs(context).edit()
                    .putString(KEY_DISTRICT_EN, finalDistEn)
                    .putString(KEY_DISTRICT_NP, finalDistNp)
                    .putString(KEY_PROVINCE_EN, finalProvEn)
                    .putString(KEY_PROVINCE_NP, finalProvNp)
                    .putBoolean(KEY_IS_AUTO_GPS, true)
                    .apply()
            }
        }
    }

    private fun cleanProvinceName(raw: String): String {
        val lower = raw.lowercase()
        return when {
            lower.contains("koshi") || lower.contains("province 1") -> "Koshi"
            lower.contains("madhesh") || lower.contains("province 2") -> "Madhesh"
            lower.contains("bagmati") || lower.contains("province 3") -> "Bagmati"
            lower.contains("gandaki") || lower.contains("province 4") -> "Gandaki"
            lower.contains("lumbini") || lower.contains("province 5") -> "Lumbini"
            lower.contains("karnali") || lower.contains("province 6") -> "Karnali"
            lower.contains("sudurpashchim") || lower.contains("sudur") || lower.contains("province 7") -> "Sudurpashchim"
            else -> raw
        }
    }

    private data class NearestResult(val districtEn: String, val districtNp: String, val provinceEn: String)

    private fun findNearestDistrict(lat: Double, lon: Double): NearestResult? {
        val all = PlacesRepo.all()
        if (all.isEmpty()) return null

        var bestDist = Double.MAX_VALUE
        var bestPlace: com.neptools.app.core.data.Place? = null

        for (p in all) {
            val dist = haversineDistanceKm(lat, lon, p.lat, p.lon)
            if (dist < bestDist) {
                bestDist = dist
                bestPlace = p
            }
        }

        val chosen = bestPlace ?: return null
        val dEn = if (chosen.type == "district" || chosen.district.isBlank()) chosen.en else chosen.district
        val dNp = if (chosen.type == "district" || chosen.district.isBlank()) chosen.np else getDistrictNpName(dEn)
        val prov = chosen.province.ifBlank { "Bagmati" }

        return NearestResult(districtEn = dEn, districtNp = dNp, provinceEn = prov)
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun getDistrictNpName(districtEn: String): String {
        return DISTRICT_NAME_MAP[districtEn.lowercase().trim()] ?: districtEn
    }

    val DISTRICT_NAME_MAP = mapOf(
        "kathmandu" to "काठमाडौं",
        "lalitpur" to "ललितपुर",
        "bhaktapur" to "भक्तपुर",
        "kaski" to "कास्की",
        "chitwan" to "चितवन",
        "morang" to "मोरङ",
        "sunsari" to "सुनसरी",
        "jhapa" to "झापा",
        "ilam" to "इलाम",
        "panchthar" to "पाँचथर",
        "taplejung" to "ताप्लेजुङ",
        "dhankuta" to "धनकुटा",
        "bhojpur" to "भोजपुर",
        "sankhuwasabha" to "संखुवासभा",
        "terhathum" to "तेह्रथुम",
        "okhaldhunga" to "ओखलढुङ्गा",
        "khotang" to "खोटाङ",
        "solukhumbu" to "सोलुखुम्बु",
        "udayapur" to "उदयपुर",
        "dhanusha" to "धनुषा",
        "dhanusa" to "धनुषा",
        "parsa" to "पर्सा",
        "bara" to "बारा",
        "rautahat" to "रौतहट",
        "sarlahi" to "सर्लाही",
        "mahottari" to "महोत्तरी",
        "siraha" to "सिराहा",
        "saptari" to "सप्तरी",
        "makwanpur" to "मकवानपुर",
        "kavrepalanchok" to "काभ्रेपलाञ्चोक",
        "sindhupalchok" to "सिन्धुपाल्चोक",
        "nuwakot" to "नुवाकोट",
        "dhading" to "धादिङ",
        "ramechhap" to "रामेछाप",
        "dolakha" to "दोलखा",
        "sindhuli" to "सिन्धुली",
        "rasuwa" to "रसुवा",
        "tanahun" to "तनहुँ",
        "tanahu" to "तनहुँ",
        "syangja" to "स्याङ्जा",
        "gorkha" to "गोरखा",
        "lamjung" to "लमजुङ",
        "baglung" to "बागलुङ",
        "parbat" to "पर्वत",
        "myagdi" to "म्याग्दी",
        "nawalpur" to "नवलपुर",
        "mustang" to "मुस्ताङ",
        "manang" to "मनाङ",
        "rupandehi" to "रुपन्देही",
        "kapilvastu" to "कपिलवस्तु",
        "banke" to "बाँके",
        "bardiya" to "बर्दिया",
        "dang" to "दाङ",
        "palpa" to "पाल्पा",
        "gulmi" to "गुल्मी",
        "arghakhanchi" to "अर्घाखाँची",
        "pyuthan" to "प्युठान",
        "rolpa" to "रोल्पा",
        "rukum east" to "पूर्वी रुकुम",
        "parasi" to "पश्चिम नवलपरासी",
        "surkhet" to "सुर्खेत",
        "dailekh" to "दैलेख",
        "jajarkot" to "जाजरकोट",
        "salyan" to "सल्यान",
        "rukum west" to "पश्चिम रुकुम",
        "jumla" to "जुम्ला",
        "kalikot" to "कालिकोट",
        "mugu" to "मुगु",
        "humla" to "हुम्ला",
        "dolpa" to "डोल्पा",
        "kailali" to "कैलाली",
        "kanchanpur" to "कञ्चनपुर",
        "dadeldhura" to "डडेल्धुरा",
        "doti" to "डोटी",
        "achham" to "अछाम",
        "bajhang" to "बझाङ",
        "bajura" to "बाजुरा",
        "baitadi" to "बैतडी",
        "darchula" to "दार्चुला"
    )

    val ALL_DISTRICTS: List<DistrictItem> = listOf(
        // Koshi (14)
        DistrictItem("Bhojpur", "भोजपुर", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Dhankuta", "धनकुटा", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Ilam", "इलाम", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Jhapa", "झापा", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Khotang", "खोटाङ", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Morang", "मोरङ", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Okhaldhunga", "ओखलढुङ्गा", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Panchthar", "पाँचथर", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Sankhuwasabha", "संखुवासभा", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Solukhumbu", "सोलुखुम्बु", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Sunsari", "सुनसरी", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Taplejung", "ताप्लेजुङ", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Terhathum", "तेह्रथुम", "Koshi", "कोशी प्रदेश"),
        DistrictItem("Udayapur", "उदयपुर", "Koshi", "कोशी प्रदेश"),

        // Madhesh (8)
        DistrictItem("Bara", "बारा", "Madhesh", "मधेश प्रदेश"),
        DistrictItem("Dhanusha", "धनुषा", "Madhesh", "मधेश प्रदेश"),
        DistrictItem("Mahottari", "महोत्तरी", "Madhesh", "मधेश प्रदेश"),
        DistrictItem("Parsa", "पर्सा", "Madhesh", "मधेश प्रदेश"),
        DistrictItem("Rautahat", "रौतहट", "Madhesh", "मधेश प्रदेश"),
        DistrictItem("Saptari", "सप्तरी", "Madhesh", "मधेश प्रदेश"),
        DistrictItem("Sarlahi", "सर्लाही", "Madhesh", "मधेश प्रदेश"),
        DistrictItem("Siraha", "सिराहा", "Madhesh", "मधेश प्रदेश"),

        // Bagmati (13)
        DistrictItem("Bhaktapur", "भक्तपुर", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Chitwan", "चितवन", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Dhading", "धादिङ", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Dolakha", "दोलखा", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Kathmandu", "काठमाडौं", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Kavrepalanchok", "काभ्रेपलाञ्चोक", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Lalitpur", "ललितपुर", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Makwanpur", "मकवानपुर", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Nuwakot", "नुवाकोट", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Ramechhap", "रामेछाप", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Rasuwa", "रसुवा", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Sindhuli", "सिन्धुली", "Bagmati", "बागमती प्रदेश"),
        DistrictItem("Sindhupalchok", "सिन्धुपाल्चोक", "Bagmati", "बागमती प्रदेश"),

        // Gandaki (11)
        DistrictItem("Baglung", "बागलुङ", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Gorkha", "गोरखा", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Kaski", "कास्की", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Lamjung", "लमजुङ", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Manang", "मनाङ", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Mustang", "मुस्ताङ", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Myagdi", "म्याग्दी", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Nawalpur", "नवलपुर", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Parbat", "पर्वत", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Syangja", "स्याङ्जा", "Gandaki", "गण्डकी प्रदेश"),
        DistrictItem("Tanahun", "तनहुँ", "Gandaki", "गण्डकी प्रदेश"),

        // Lumbini (12)
        DistrictItem("Arghakhanchi", "अर्घाखाँची", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Banke", "बाँके", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Bardiya", "बर्दिया", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Dang", "दाङ", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Gulmi", "गुल्मी", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Kapilvastu", "कपिलवस्तु", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Palpa", "पाल्पा", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Parasi", "पश्चिम नवलपरासी", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Pyuthan", "प्युठान", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Rolpa", "रोल्पा", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Rukum East", "पूर्वी रुकुम", "Lumbini", "लुम्बिनी प्रदेश"),
        DistrictItem("Rupandehi", "रुपन्देही", "Lumbini", "लुम्बिनी प्रदेश"),

        // Karnali (10)
        DistrictItem("Dailekh", "दैलेख", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Dolpa", "डोल्पा", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Humla", "हुम्ला", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Jajarkot", "जाजरकोट", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Jumla", "जुम्ला", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Kalikot", "कालिकोट", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Mugu", "मुगु", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Rukum West", "पश्चिम रुकुम", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Salyan", "सल्यान", "Karnali", "कर्णाली प्रदेश"),
        DistrictItem("Surkhet", "सुर्खेत", "Karnali", "कर्णाली प्रदेश"),

        // Sudurpashchim (9)
        DistrictItem("Achham", "अछाम", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Baitadi", "बैतडी", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Bajhang", "बझाङ", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Bajura", "बाजुरा", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Dadeldhura", "डडेल्धुरा", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Darchula", "दार्चुला", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Doti", "डोटी", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Kailali", "कैलाली", "Sudurpashchim", "सुदूरपश्चिम प्रदेश"),
        DistrictItem("Kanchanpur", "कञ्चनपुर", "Sudurpashchim", "सुदूरपश्चिम प्रदेश")
    )
}

data class DistrictItem(
    val nameEn: String,
    val nameNp: String,
    val provinceEn: String,
    val provinceNp: String
)

