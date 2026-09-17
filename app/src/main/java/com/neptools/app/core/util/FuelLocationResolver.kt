package com.neptools.app.core.util

import android.content.Context
import com.neptools.app.core.data.FuelRateSet
import com.neptools.app.core.data.FuelRepo
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ResolvedFuelLocation(
    val categoryKey: String, // "first", "second", "third"
    val categoryNameNp: String,
    val categoryNameEn: String,
    val locationNameNp: String,
    val locationNameEn: String,
    val petrolPrice: Double,
    val dieselPrice: Double,
    val kerosenePrice: Double,
    val lpgPrice: Double,
    val atfPrice: Double,
    val isGpsAccurate: Boolean
)

object FuelLocationResolver {

    // First Category Districts (NOC Depot & Tarai border corridor)
    private val firstCategoryDistricts = setOf(
        "jhapa", "morang", "sunsari", "saptari", "siraha", "dhanusha", "dhanusa",
        "mahottari", "sarlahi", "rautahat", "bara", "parsa", "chitwan",
        "nawalparasi", "nawalpur", "parasi", "rupandehi", "kapilvastu",
        "banke", "bardiya", "kailali", "kanchanpur"
    )

    // Second Category Districts (Mid-Hills depot zones)
    private val secondCategoryDistricts = setOf(
        "dang", "surkhet", "dailekh", "jajarkot", "salyan", "pyuthan",
        "rolpa", "arghakhanchi", "palpa", "tanahun", "tanahu", "gorkha",
        "lamjung", "syangja"
    )

    // Major NOC Depots for Geodesic Nearest Proximity Match
    private data class NocDepot(val name: String, val lat: Double, val lon: Double, val category: String)

    private val depots = listOf(
        NocDepot("Charali", 26.65, 88.05, "first"),
        NocDepot("Biratnagar", 26.45, 87.27, "first"),
        NocDepot("Janakpur", 26.72, 85.92, "first"),
        NocDepot("Amlekhgunj", 27.28, 84.98, "first"),
        NocDepot("Birgunj", 27.00, 84.87, "first"),
        NocDepot("Bhalwari", 27.57, 83.46, "first"),
        NocDepot("Nepalgunj", 28.05, 81.61, "first"),
        NocDepot("Dhangadhi", 28.68, 80.59, "first"),
        NocDepot("Surkhet", 28.60, 81.63, "second"),
        NocDepot("Dang", 28.03, 82.30, "second"),
        NocDepot("Kathmandu", 27.71, 85.32, "third"),
        NocDepot("Pokhara", 28.20, 83.98, "third"),
        NocDepot("Dipayal", 29.26, 80.94, "third")
    )

    /**
     * Resolves the exact fuel price category based on GPS / CityWeather state.
     */
    fun resolveForCurrentLocation(
        fuelRates: FuelRateSet = FuelRepo.defaultRates
    ): ResolvedFuelLocation {
        val weather = WeatherLocationManager.currentWeather.value
        val districtEn = weather.districtEn.lowercase().trim()
        val districtNp = weather.districtNp.trim()
        val nameEn = weather.nameEn.trim()
        val nameNp = weather.nameNp.trim()
        val lat = weather.lat
        val lon = weather.lon

        var resolvedCat = "third"

        // 1. Check direct district mapping
        when {
            firstCategoryDistricts.any { districtEn.contains(it) || nameEn.lowercase().contains(it) } -> {
                resolvedCat = "first"
            }
            secondCategoryDistricts.any { districtEn.contains(it) || nameEn.lowercase().contains(it) } -> {
                resolvedCat = "second"
            }
            else -> {
                // 2. Geodesic Proximity to nearest NOC depot
                if (lat > 20.0 && lon > 75.0) {
                    val nearestDepot = depots.minByOrNull { d ->
                        distanceKm(lat, lon, d.lat, d.lon)
                    }
                    if (nearestDepot != null) {
                        resolvedCat = nearestDepot.category
                    }
                }
            }
        }

        val prices = fuelRates.categoryPrices[resolvedCat] ?: FuelRepo.defaultRates.categoryPrices[resolvedCat]!!

        val (catNameNp, catNameEn) = when (resolvedCat) {
            "first" -> "पहिलो वर्ग (डिपो/तराई)" to "1st Category (Depot / Tarai)"
            "second" -> "दोस्रो वर्ग (सुर्खेत/दाङ)" to "2nd Category (Surkhet / Dang)"
            else -> "तेस्रो वर्ग (काठमाडौं/पोखरा/पहाड)" to "3rd Category (Valley / Hills)"
        }

        val locNp = if (districtNp.isNotBlank() && !districtNp.equals("नेपाल", ignoreCase = true)) {
            "$nameNp, $districtNp"
        } else nameNp

        val locEn = if (districtEn.isNotBlank() && !districtEn.equals("nepal", ignoreCase = true)) {
            "$nameEn, $districtEn"
        } else nameEn

        return ResolvedFuelLocation(
            categoryKey = resolvedCat,
            categoryNameNp = catNameNp,
            categoryNameEn = catNameEn,
            locationNameNp = locNp,
            locationNameEn = locEn,
            petrolPrice = prices["petrol"] ?: 200.00,
            dieselPrice = prices["diesel"] ?: 200.00,
            kerosenePrice = prices["kerosene"] ?: 200.00,
            lpgPrice = prices["lpg"] ?: 2060.00,
            atfPrice = prices["atf"] ?: 249.00,
            isGpsAccurate = WeatherLocationManager.locationPermissionGranted.value
        )
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
