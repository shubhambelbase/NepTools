package com.neptools.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.neptools.app.core.data.CityWeather
import com.neptools.app.core.data.DailyForecast
import com.neptools.app.core.data.HourlyForecast
import com.neptools.app.core.data.WeatherRepo
import com.neptools.app.core.util.WeatherLocationManager
import com.neptools.app.ui.components.npNum
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEn = ThemePrefs.lang.value == "en"
    val scope = rememberCoroutineScope()

    val currentWeather by WeatherLocationManager.currentWeather.collectAsState()
    val isLocating by WeatherLocationManager.isGpsLocating.collectAsState()

    var showCitySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Location Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        WeatherLocationManager.updatePermissionStatus(context, granted)
        if (granted) {
            Toast.makeText(context, if (isEn) "Detecting location..." else "स्थान खोज्दै...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                if (isEn) "Location permission denied. Showing selected city." else "स्थान अनुमति अस्वीकार भयो। चयन गरिएको शहर देखाउँदै।",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun requestGpsWeather() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            WeatherLocationManager.requestLocationWeather(context, force = true)
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(PIcons.ChevronLeft, if (isEn) "Back" else "फिर्ता", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isEn) "Weather & Forecast" else "मौसम तथा पूर्वानुमान",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                Modifier
                    .background(Color(0xFFE0F2FE), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
                    .clickable { showCitySheet = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(PIcons.Pin, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isEn) currentWeather.nameEn else currentWeather.nameNp,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0369A1)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                com.neptools.app.ui.components.AnimatedRefreshIconButton(
                    onClick = { WeatherLocationManager.requestLocationWeather(context, force = true) },
                    isRefreshing = isLocating,
                    iconSize = 18.dp,
                    tint = if (isLocating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Hero Weather Card
            item {
                HeroWeatherCard(
                    weather = currentWeather,
                    isEn = isEn,
                    isLocating = isLocating,
                    onRequestLocation = { requestGpsWeather() },
                    onChangeCity = { showCitySheet = true }
                )
            }

            // 24-Hour Forecast
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isEn) "⏱️ Hourly Forecast" else "⏱️ २४ घण्टे पूर्वानुमान",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isEn) "Today" else "आज",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentWeather.hourly) { h ->
                                HourlyCard(forecast = h, isEn = isEn)
                            }
                        }
                    }
                }
            }

            // 7-Day Weekly Forecast
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PIcons.Calendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "7-Day Weekly Forecast" else "७ दिने साप्ताहिक पूर्वानुमान",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        currentWeather.weekly.forEachIndexed { idx, day ->
                            WeeklyRow(day = day, isEn = isEn)
                            if (idx < currentWeather.weekly.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Environmental Details Metrics
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PIcons.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "Detailed Conditions" else "मौसम सूचकांक तथा विवरण",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = if (isEn) "Humidity" else "आर्द्रता (हावामा पानी)",
                                value = "${currentWeather.humidityPercent}%",
                                sub = if (isEn) "Normal" else "सामान्य",
                                icon = PIcons.Droplet,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = if (isEn) "Wind Speed" else "हावाको गति",
                                value = "${currentWeather.windKmh} km/h",
                                sub = if (isEn) "North-West" else "उत्तर–पश्चिम",
                                icon = PIcons.SunUp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = if (isEn) "UV Index" else "पराबैजनी किरण",
                                value = "${currentWeather.uvIndex} / 10",
                                sub = if (isEn) "Moderate" else "मध्यम",
                                icon = PIcons.Sun,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = if (isEn) "Air Quality (AQI)" else "वायु गुणस्तर",
                                value = "${currentWeather.aqi}",
                                sub = if (isEn) currentWeather.aqiLabelEn else currentWeather.aqiLabelNp,
                                icon = PIcons.Cloud,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // City Selector Bottom Sheet
        if (showCitySheet) {
            var citySearch by remember { mutableStateOf("") }
            val filteredCities = remember(citySearch) {
                if (citySearch.isBlank()) WeatherRepo.nepalCities
                else WeatherRepo.nepalCities.filter {
                    it.nameNp.contains(citySearch, ignoreCase = true) ||
                    it.nameEn.contains(citySearch, ignoreCase = true) ||
                    it.districtNp.contains(citySearch, ignoreCase = true) ||
                    it.districtEn.contains(citySearch, ignoreCase = true) ||
                    it.provinceNp.contains(citySearch, ignoreCase = true) ||
                    it.provinceEn.contains(citySearch, ignoreCase = true)
                }
            }

            ModalBottomSheet(
                onDismissRequest = { showCitySheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (isEn) "Select City / District (${WeatherRepo.nepalCities.size})" else "शहर वा जिल्ला छान्नुहोस् (${WeatherRepo.nepalCities.size})",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isEn) "All districts & major hubs across Nepal" else "नेपालका सम्पूर्ण जिल्ला तथा प्रमुख शहरहरू",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                requestGpsWeather()
                                scope.launch { sheetState.hide() }
                                showCitySheet = false
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(PIcons.Pin, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isEn) "My GPS" else "मेरो स्थान", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Search input
                    androidx.compose.material3.OutlinedTextField(
                        value = citySearch,
                        onValueChange = { citySearch = it },
                        placeholder = {
                            Text(
                                if (isEn) "Search district, city or province..." else "जिल्ला, शहर वा प्रदेश खोज्नुहोस्...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(PIcons.Search, null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (citySearch.isNotEmpty()) {
                                Text(
                                    "✕",
                                    modifier = Modifier
                                        .clickable { citySearch = "" }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCities) { city ->
                            val isSel = city.id == currentWeather.id
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        WeatherLocationManager.selectCity(context, city)
                                        scope.launch { sheetState.hide() }
                                        showCitySheet = false
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            if (isEn) city.nameEn else city.nameNp,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "${if (isEn) city.districtEn else city.districtNp} (${if (isEn) city.provinceEn else city.provinceNp}) · ${if (isEn) city.conditionEn else city.conditionNp}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "${city.tempC}°C",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (isSel) {
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                PIcons.CheckCircle,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredCities.isEmpty()) {
                            item {
                                Spacer(Modifier.height(30.dp))
                                Column(
                                    Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        if (isEn) "No matching districts or cities found" else "कुनै जिल्ला वा शहर भेटिएन",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroWeatherCard(
    weather: CityWeather,
    isEn: Boolean,
    isLocating: Boolean,
    onRequestLocation: () -> Unit,
    onChangeCity: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onChangeCity)
                ) {
                    Icon(PIcons.Pin, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${if (isEn) weather.nameEn else weather.nameNp}, ${if (isEn) weather.districtEn else weather.districtNp}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("▾", color = Color(0xFFBAE6FD), style = MaterialTheme.typography.titleSmall)
                }

                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onRequestLocation)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isLocating) (if (isEn) "Locating..." else "खोज्दै...")
                            else (if (isEn) "Detect GPS" else "मेरो स्थान"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${if (isEn) weather.tempC else npNum(weather.tempC)}°C",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 46.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        if (isEn) weather.conditionEn else weather.conditionNp,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFE0F2FE)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${if (isEn) "Feels like" else "महसुस हुने"} ${weather.feelsLikeC}°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBAE6FD)
                    )
                }

                Box(
                    Modifier
                        .size(76.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (weather.iconType == "rain") PIcons.Droplet
                        else if (weather.iconType == "cloud") PIcons.Cloud
                        else PIcons.Sun,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "H: ${weather.maxTempC}°C  L: ${weather.minTempC}°C",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PIcons.Droplet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${weather.humidityPercent}% ${if (isEn) "Humidity" else "आर्द्रता"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Box(
                    Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "AQI ${weather.aqi} · ${if (isEn) weather.aqiLabelEn else weather.aqiLabelNp}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF15803D)
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyCard(forecast: HourlyForecast, isEn: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                if (isEn) forecast.timeLabelEn else forecast.timeLabelNp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                if (forecast.iconType == "rain") PIcons.Droplet
                else if (forecast.iconType == "cloud") PIcons.Cloud
                else PIcons.Sun,
                contentDescription = null,
                tint = if (forecast.iconType == "rain") Color(0xFF0284C7) else Color(0xFFD97706),
                modifier = Modifier.size(24.dp)
            )

            Text(
                "${if (isEn) forecast.tempC else npNum(forecast.tempC)}°",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (forecast.rainProb > 0) {
                Text(
                    "💧${forecast.rainProb}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF0284C7)
                )
            }
        }
    }
}

@Composable
private fun WeeklyRow(day: DailyForecast, isEn: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(90.dp)) {
            Text(
                if (isEn) day.dayNameEn else day.dayNameNp,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                if (isEn) day.dateEn else day.dateNp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (day.iconType == "rain") PIcons.Droplet
                else if (day.iconType == "cloud") PIcons.Cloud
                else PIcons.Sun,
                contentDescription = null,
                tint = if (day.iconType == "rain") Color(0xFF0284C7) else Color(0xFFD97706),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isEn) day.conditionEn else day.conditionNp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (day.rainProb >= 30) {
                Box(
                    Modifier
                        .background(Color(0xFFE0F2FE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        "${day.rainProb}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF0284C7)
                    )
                }
            }

            Text(
                "${day.minTempC}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mini temp bar
            Box(
                Modifier
                    .width(42.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF60A5FA), Color(0xFFF97316))
                        )
                    )
            )

            Text(
                "${day.maxTempC}°",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
