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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.neptools.app.core.data.CityWeather
import com.neptools.app.core.data.DailyForecast
import com.neptools.app.core.data.HourlyForecast
import com.neptools.app.core.data.WeatherRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object WeatherLocationManager {

    private const val PREFS_NAME = "nepal_weather_cache"
    private const val KEY_CACHED_WEATHER_JSON = "cached_weather_json"
    private const val KEY_LAST_FETCH_TIME = "last_weather_fetch_time"
    private const val CACHE_VALIDITY_MS = 15 * 60 * 1000L // 15 minutes cache freshness

    private val _currentWeather = MutableStateFlow<CityWeather>(WeatherRepo.defaultWeather)
    val currentWeather: StateFlow<CityWeather> = _currentWeather.asStateFlow()

    private val _isGpsLocating = MutableStateFlow(false)
    val isGpsLocating: StateFlow<Boolean> = _isGpsLocating.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var isInitialized = false

    /**
     * Initializes cache immediately on app start and triggers background GPS auto-fetch.
     */
    fun initAndAutoFetch(context: Context) {
        if (!isInitialized) {
            isInitialized = true
            loadFromCache(context)
        }

        val hasPermission = checkPermission(context)
        _locationPermissionGranted.value = hasPermission

        if (hasPermission) {
            requestLocationWeather(context, force = false)
        } else {
            fetchLive(context, _currentWeather.value)
        }
    }

    private fun fetchLive(context: Context, city: CityWeather) {
        WeatherRepo.refreshLive(context, city) { live ->
            if (live != null && live.weather != _currentWeather.value) {
                _currentWeather.value = live.weather
                saveToCache(context, live.weather)
            }
        }
    }

    fun selectCity(context: Context, city: CityWeather) {
        _currentWeather.value = city
        saveToCache(context, city)
        fetchLive(context, city)
    }

    fun updatePermissionStatus(context: Context, granted: Boolean) {
        _locationPermissionGranted.value = granted
        if (granted) {
            requestLocationWeather(context, force = true)
        }
    }

    private fun checkPermission(context: Context): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasFine || hasCoarse
    }

    @SuppressLint("MissingPermission")
    fun requestLocationWeather(context: Context, force: Boolean = false) {
        if (!checkPermission(context)) {
            _locationPermissionGranted.value = false
            return
        }

        _locationPermissionGranted.value = true

        // Cache optimization: Skip redundant GPS polling if cache is fresh and not forced
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastFetch = prefs.getLong(KEY_LAST_FETCH_TIME, 0L)
        val now = System.currentTimeMillis()
        if (!force && (now - lastFetch) < CACHE_VALIDITY_MS && _currentWeather.value.id != WeatherRepo.defaultWeather.id) {
            return
        }

        _isGpsLocating.value = true

        val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locManager == null) {
            _isGpsLocating.value = false
            return
        }

        try {
            // Check all providers for fast last known location
            val lastGps = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val lastPassive = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            val bestLoc: Location? = listOfNotNull(lastGps, lastNetwork, lastPassive)
                .maxByOrNull { it.time }

            if (bestLoc != null) {
                processLocation(context, bestLoc)
            }

            // Register active listener for fresh location fix
            val activeListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    processLocation(context, location)
                    _isGpsLocating.value = false
                    try {
                        locManager.removeUpdates(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onProviderDisabled(provider: String) {
                    _isGpsLocating.value = false
                }
            }

            val looper = Looper.getMainLooper()
            if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 5f, activeListener, looper)
            }
            if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 5f, activeListener, looper)
            }

            // Safety timeout (8 seconds)
            Handler(Looper.getMainLooper()).postDelayed({
                if (_isGpsLocating.value) {
                    _isGpsLocating.value = false
                    try {
                        locManager.removeUpdates(activeListener)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, 8000L)

        } catch (e: Exception) {
            e.printStackTrace()
            _isGpsLocating.value = false
        }
    }

    private fun processLocation(context: Context, location: Location) {
        val lat = location.latitude
        val lon = location.longitude

        mainScope.launch(Dispatchers.IO) {
            var resolvedCity: CityWeather? = null

            // Try Android Geocoder for precise local community name
            if (Geocoder.isPresent()) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses: List<Address>? = geocoder.getFromLocation(lat, lon, 1)
                    val addr = addresses?.firstOrNull()

                    if (addr != null) {
                        val locality = addr.locality
                            ?: addr.subAdminArea
                            ?: addr.subLocality
                            ?: addr.featureName
                        val district = addr.subAdminArea ?: addr.adminArea ?: "Nepal"
                        val province = addr.adminArea ?: "Nepal"

                        if (!locality.isNullOrBlank()) {
                            resolvedCity = WeatherRepo.createDynamicWeatherForLocation(
                                detectedName = locality,
                                district = district,
                                province = province,
                                lat = lat,
                                lon = lon
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback to nearest city hub across all 77 districts
            val finalWeather = resolvedCity ?: WeatherRepo.findClosestCity(lat, lon)

            mainScope.launch(Dispatchers.Main) {
                _currentWeather.value = finalWeather
                _isGpsLocating.value = false
                saveToCache(context, finalWeather)
                fetchLive(context, finalWeather)
            }
        }
    }

    // -------------------------------------------------------------
    // Persistent Disk Caching Optimization
    // -------------------------------------------------------------
    private fun saveToCache(context: Context, city: CityWeather) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = JSONObject().apply {
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

                // Hourly JSON Array
                val hourlyArr = JSONArray()
                city.hourly.forEach { h ->
                    hourlyArr.put(JSONObject().apply {
                        put("timeLabelNp", h.timeLabelNp)
                        put("timeLabelEn", h.timeLabelEn)
                        put("tempC", h.tempC)
                        put("conditionNp", h.conditionNp)
                        put("conditionEn", h.conditionEn)
                        put("iconType", h.iconType)
                        put("rainProb", h.rainProb)
                    })
                }
                put("hourly", hourlyArr)

                // Weekly JSON Array
                val weeklyArr = JSONArray()
                city.weekly.forEach { w ->
                    weeklyArr.put(JSONObject().apply {
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
                put("weekly", weeklyArr)
            }

            prefs.edit()
                .putString(KEY_CACHED_WEATHER_JSON, json.toString())
                .putLong(KEY_LAST_FETCH_TIME, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFromCache(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_CACHED_WEATHER_JSON, null) ?: return
            val json = JSONObject(jsonStr)

            val hourlyList = mutableListOf<HourlyForecast>()
            val hourlyArr = json.optJSONArray("hourly")
            if (hourlyArr != null) {
                for (i in 0 until hourlyArr.length()) {
                    val hObj = hourlyArr.getJSONObject(i)
                    hourlyList.add(
                        HourlyForecast(
                            timeLabelNp = hObj.getString("timeLabelNp"),
                            timeLabelEn = hObj.getString("timeLabelEn"),
                            tempC = hObj.getInt("tempC"),
                            conditionNp = hObj.getString("conditionNp"),
                            conditionEn = hObj.getString("conditionEn"),
                            iconType = hObj.getString("iconType"),
                            rainProb = hObj.getInt("rainProb")
                        )
                    )
                }
            }

            val weeklyList = mutableListOf<DailyForecast>()
            val weeklyArr = json.optJSONArray("weekly")
            if (weeklyArr != null) {
                for (i in 0 until weeklyArr.length()) {
                    val wObj = weeklyArr.getJSONObject(i)
                    weeklyList.add(
                        DailyForecast(
                            dayNameNp = wObj.getString("dayNameNp"),
                            dayNameEn = wObj.getString("dayNameEn"),
                            dateNp = wObj.getString("dateNp"),
                            dateEn = wObj.getString("dateEn"),
                            conditionNp = wObj.getString("conditionNp"),
                            conditionEn = wObj.getString("conditionEn"),
                            iconType = wObj.getString("iconType"),
                            minTempC = wObj.getInt("minTempC"),
                            maxTempC = wObj.getInt("maxTempC"),
                            rainProb = wObj.getInt("rainProb"),
                            humidityPercent = wObj.getInt("humidityPercent")
                        )
                    )
                }
            }

            val cachedCity = CityWeather(
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
                hourly = hourlyList.ifEmpty { WeatherRepo.defaultWeather.hourly },
                weekly = weeklyList.ifEmpty { WeatherRepo.defaultWeather.weekly }
            )

            _currentWeather.value = cachedCity
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
