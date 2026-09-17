package com.neptools.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neptools.app.ui.icons.PIcons
import com.neptools.app.ui.screens.AgeScreen
import com.neptools.app.ui.screens.CalendarScreen
import com.neptools.app.ui.screens.ConverterScreen
import com.neptools.app.ui.screens.CurrencyScreen
import com.neptools.app.ui.screens.DayDetailScreen
import com.neptools.app.ui.screens.HomeScreen
import com.neptools.app.ui.screens.RashifalScreen
import com.neptools.app.ui.screens.SettingsScreen
import com.neptools.app.ui.screens.ToolsScreen

object Routes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val CALENDAR_DATED = "calendar/{year}/{month}"
    const val DAY = "day/{year}/{month}/{day}"
    const val TOOLS = "tools"
    const val WEATHER = "weather"
    const val CONVERTER = "converter"
    const val CURRENCY = "currency"
    const val AGE = "age"
    const val RASHIFAL = "rashifal"
    const val VOICE = "voice"
    const val EMERGENCY = "emergency"
    const val BILL_CALC = "bill_calc"
    const val POSTAL = "postal"
    const val FUEL = "fuel"
    const val RADIO = "radio"
    const val QR = "qr"
    const val KALIMATI = "kalimati"
    const val TEMPLATES = "templates"
    const val LOAN_EMI = "loan_emi"
    const val DRIVING_LICENSE = "driving_license"
    const val IMAGE_COMPRESSOR = "image_compressor"
    const val LAN_DROP = "lan_drop"
    const val SPEED_TEST = "speed_test"
    const val DECISION_MAKER = "decision_maker"
    const val BILL_SPLITTER = "bill_splitter"
    const val PET_WHISTLE = "pet_whistle"
    const val COMPASS = "compass"
    const val BUBBLE_LEVEL = "bubble_level"
    const val VAULT = "password_vault"
    const val FILE_CONVERTER = "file_converter"
    const val MUHURAT = "muhurat"
    const val GUNA_MILAN = "guna_milan"
    const val EKADASHI = "ekadashi_list"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val TERMS = "terms"
    const val PRIVACY = "privacy"
    const val HABIT_TRACKER = "habit_tracker"
    const val SUBSCRIPTION_TRACKER = "subscription_tracker"
    const val UPDATER = "app_updater"
    const val SOUND_METER = "sound_meter"
    const val SPY_CAMERA = "spy_camera"
    const val ASTRO = "astrology"
    const val ASTRO_BIRTH = "astrology/birth"
    const val ASTRO_KUNDALI = "astrology/kundali"
    const val ASTRO_DASHA = "astrology/dasha"
    const val ASTRO_GOCHAR = "astrology/gochar"
    const val ASTRO_ANALYSIS = "astrology/analysis"
    const val VASTU_COMPASS = "vastu_compass"
    const val LAND_CONVERTER = "land_converter"

    fun calendar(year: Int, month: Int) = "calendar/$year/$month"
    fun day(year: Int, month: Int, day: Int) = "day/$year/$month/$day"
}

private data class TopLevel(val route: String, val key: String, val icon: ImageVector)

private val topLevelItems = listOf(
    TopLevel(Routes.HOME, "nav_home", PIcons.Home),
    TopLevel(Routes.CALENDAR, "nav_patro", PIcons.Calendar),
    TopLevel(Routes.TOOLS, "nav_tools", PIcons.Grid)
)

@Composable
fun PatroApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val isTopLevelRoute = currentRoute in setOf(Routes.HOME, Routes.CALENDAR, Routes.TOOLS)

    fun navigateTo(route: String) {
        if (route in setOf(Routes.HOME, Routes.CALENDAR, Routes.TOOLS)) {
            navController.navigateTab(route)
        } else {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        AppNavigator.events.collect { route ->
            navigateTo(route)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevelRoute) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    topLevelItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigateTab(item.route)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.key,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(com.neptools.app.ui.strings.T(item.key), style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
            enterTransition = {
                val isTabSwitch = initialState.destination.route in listOf(Routes.HOME, Routes.CALENDAR, Routes.TOOLS) &&
                                  targetState.destination.route in listOf(Routes.HOME, Routes.CALENDAR, Routes.TOOLS)
                if (isTabSwitch) {
                    fadeIn(animationSpec = tween(150)) + androidx.compose.animation.scaleIn(initialScale = 0.98f, animationSpec = tween(150))
                } else {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(180))
                }
            },
            exitTransition = {
                val isTabSwitch = initialState.destination.route in listOf(Routes.HOME, Routes.CALENDAR, Routes.TOOLS) &&
                                  targetState.destination.route in listOf(Routes.HOME, Routes.CALENDAR, Routes.TOOLS)
                if (isTabSwitch) {
                    fadeOut(animationSpec = tween(120)) + androidx.compose.animation.scaleOut(targetScale = 0.98f, animationSpec = tween(120))
                } else {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(180))
                }
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(180))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180))
            }
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenCalendar = { y, m -> navController.navigate(Routes.calendar(y, m)) },
                    onOpenTool = { navigateTo(it) }
                )
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(argYear = -1, argMonth = -1, onOpenDay = { y, m, d ->
                    navController.navigate(Routes.day(y, m, d))
                })
            }
            composable(
                Routes.CALENDAR_DATED,
                arguments = listOf(
                    navArgument("year") { defaultValue = -1 },
                    navArgument("month") { defaultValue = -1 }
                )
            ) { entry ->
                CalendarScreen(
                    argYear = entry.arguments?.getInt("year") ?: -1,
                    argMonth = entry.arguments?.getInt("month") ?: -1,
                    onOpenDay = { y, m, d -> navController.navigate(Routes.day(y, m, d)) }
                )
            }
            composable(
                Routes.DAY,
                arguments = listOf(
                    navArgument("year") { defaultValue = -1 },
                    navArgument("month") { defaultValue = -1 },
                    navArgument("day") { defaultValue = -1 }
                )
            ) { entry ->
                DayDetailScreen(
                    argYear = entry.arguments?.getInt("year") ?: -1,
                    argMonth = entry.arguments?.getInt("month") ?: -1,
                    argDay = entry.arguments?.getInt("day") ?: -1,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TOOLS) {
                ToolsScreen(onOpenTool = { navigateTo(it) })
            }
            composable(Routes.CONVERTER) { com.neptools.app.ui.screens.ConverterScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.CURRENCY) { com.neptools.app.ui.screens.CurrencyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.RASHIFAL) { com.neptools.app.ui.screens.RashifalScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.AGE) { com.neptools.app.ui.screens.AgeScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BILL_CALC) { com.neptools.app.ui.screens.BillCalculatorScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.EMERGENCY) { com.neptools.app.ui.screens.EmergencyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.POSTAL) { com.neptools.app.ui.screens.PostalCodeScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.RADIO) { com.neptools.app.ui.screens.RadioScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.FUEL) { com.neptools.app.ui.screens.FuelPriceScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.QR) { com.neptools.app.ui.screens.QrScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.VOICE) { com.neptools.app.ui.screens.VoiceScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.WEATHER) { com.neptools.app.ui.screens.WeatherScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.KALIMATI) { com.neptools.app.ui.screens.KalimatiScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.TEMPLATES) { com.neptools.app.ui.screens.ApplicationTemplatesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.LOAN_EMI) { com.neptools.app.ui.screens.LoanEmiScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.DRIVING_LICENSE) { com.neptools.app.ui.screens.DrivingLicenseScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.IMAGE_COMPRESSOR) { com.neptools.app.ui.screens.ImageCompressorScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.LAN_DROP) { com.neptools.app.ui.screens.LanDropScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SPEED_TEST) { com.neptools.app.ui.screens.SpeedTestScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.DECISION_MAKER) { com.neptools.app.ui.screens.DecisionMakerScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BILL_SPLITTER) { com.neptools.app.ui.screens.BillSplitterScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PET_WHISTLE) { com.neptools.app.ui.screens.PetWhistleScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.COMPASS) {
                com.neptools.app.ui.screens.CompassScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVastu = { navController.navigate(Routes.VASTU_COMPASS) }
                )
            }
            composable(Routes.VASTU_COMPASS) {
                com.neptools.app.ui.screens.VastuCompassScreen(
                    onBack = { navController.popBackStack() },
                    onOpenStandardCompass = {
                        navController.popBackStack()
                        navController.navigate(Routes.COMPASS)
                    }
                )
            }
            composable(Routes.BUBBLE_LEVEL) { com.neptools.app.ui.screens.BubbleLevelScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.LAND_CONVERTER) { com.neptools.app.ui.screens.LandConverterScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.VAULT) { com.neptools.app.ui.screens.PasswordVaultScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.FILE_CONVERTER) { com.neptools.app.ui.screens.FileConverterScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.MUHURAT) { com.neptools.app.ui.screens.MuhuratFinderScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.GUNA_MILAN) { com.neptools.app.ui.screens.GunaMilanScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.EKADASHI) { com.neptools.app.ui.screens.EkadashiListScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.ABOUT) { com.neptools.app.ui.screens.AboutScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.TERMS) { com.neptools.app.ui.screens.TermsOfServiceScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PRIVACY) { com.neptools.app.ui.screens.PrivacyPolicyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.HABIT_TRACKER) { com.neptools.app.ui.screens.HabitTrackerScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SUBSCRIPTION_TRACKER) { com.neptools.app.ui.screens.SubscriptionTrackerScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.UPDATER) { com.neptools.app.ui.screens.AppUpdaterScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SOUND_METER) { com.neptools.app.ui.screens.SoundMeterScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SPY_CAMERA) { com.neptools.app.ui.screens.SpyCameraDetectorScreen(onBack = { navController.popBackStack() }) }

            composable(Routes.ASTRO) {
                com.neptools.app.astrology.ui.AstrologyHomeScreen(
                    onOpen = { navController.navigate(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ASTRO_BIRTH) {
                com.neptools.app.astrology.ui.BirthDetailsScreen(
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ASTRO_KUNDALI) {
                com.neptools.app.astrology.ui.KundaliScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ASTRO_DASHA) {
                com.neptools.app.astrology.ui.DashaScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ASTRO_GOCHAR) {
                com.neptools.app.astrology.ui.GocharScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ASTRO_ANALYSIS) {
                com.neptools.app.astrology.ui.AnalysisScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDasha = { navController.navigate(Routes.ASTRO_DASHA) }
                )
            }
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    if (currentDestination?.route == route) return
    val startDest = graph.findStartDestination()
    if (route == startDest.route) {
        navigate(route) {
            popUpTo(startDest.id) {
                inclusive = false
                saveState = false
            }
            launchSingleTop = true
        }
    } else {
        navigate(route) {
            popUpTo(startDest.id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
