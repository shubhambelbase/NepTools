package com.neptools.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.neptools.app.MainActivity
import com.neptools.app.R
import com.neptools.app.core.data.WeatherRepo
import com.neptools.app.core.habit.HabitRepository
import com.neptools.app.core.subscription.SubscriptionRepository
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Smart Alert Notification Manager
 * Manages background notifications for:
 * 1. Subscription & Bill Renewals (24h/due today)
 * 2. Daily Habit Nudges & Streak Protection
 * 3. Rain & Severe Weather Forecast Alerts
 */
object SmartAlertNotificationManager {

    const val CHANNEL_SUBSCRIPTIONS = "neptools_subs_channel"
    const val CHANNEL_HABITS = "neptools_habits_channel"
    const val CHANNEL_WEATHER = "neptools_weather_channel"

    private const val NOTIF_PREFS = "smart_alerts_prefs"

    private const val NOTIF_ID_SUBS = 2001
    private const val NOTIF_ID_HABITS = 2002
    private const val NOTIF_ID_WEATHER = 2003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // 1. Subscription Channel (High Priority)
            val subChannel = NotificationChannel(
                CHANNEL_SUBSCRIPTIONS,
                "Subscription & Bill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for upcoming subscription renewals and bills"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            // 2. Habit Channel (Default Priority)
            val habitChannel = NotificationChannel(
                CHANNEL_HABITS,
                "Daily Habit & Streak Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily motivation and reminders to complete your active habits"
                enableLights(true)
                lightColor = Color.BLUE
            }

            // 3. Weather Channel (High Priority for rain alerts)
            val weatherChannel = NotificationChannel(
                CHANNEL_WEATHER,
                "Weather & Rain Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Precipitation, rain, and extreme temperature forecast alerts"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
            }

            nm.createNotificationChannel(subChannel)
            nm.createNotificationChannel(habitChannel)
            nm.createNotificationChannel(weatherChannel)
        }
    }

    /**
     * Checks and notifies for upcoming subscription renewals due within 48 hours.
     */
    fun checkAndNotifySubscriptions(context: Context) {
        val prefs = context.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("notif_sub", true) && ThemePrefs.subNotification.value
        if (!isEnabled) return

        try {
            val subRepo = SubscriptionRepository.get(context)
            val activeSubs = subRepo.getAllSubscriptions().filter { !it.isPaused }
            val today = LocalDate.now()
            val isEn = ThemePrefs.lang.value == "en"

            for (sub in activeSubs) {
                val nextBilling = try {
                    LocalDate.parse(sub.nextBillingDateIso)
                } catch (_: Exception) {
                    continue
                }

                val daysRemaining = ChronoUnit.DAYS.between(today, nextBilling)

                // Notify if due today (0 days) or tomorrow (1 day)
                if (daysRemaining in 0..1) {
                    val dedupeKey = "sub_notified_${sub.id}_${nextBilling}_$daysRemaining"
                    if (prefs.getBoolean(dedupeKey, false)) continue

                    val name = if (isEn) sub.nameEn else sub.nameNp
                    val priceStr = "${sub.currency.symbol} ${sub.price.toInt()}"

                    val title = if (daysRemaining == 0L) {
                        if (isEn) "Subscription Due Today: $name" else "आज नवीकरण हुने सदस्यता: $name"
                    } else {
                        if (isEn) "Renewal Tomorrow: $name" else "भोलि नवीकरण हुने सदस्यता: $name"
                    }

                    val message = if (daysRemaining == 0L) {
                        if (isEn) "$name is renewing today ($priceStr). Please check payment method."
                        else "$name आज नवीकरण हुँदैछ ($priceStr)। भुक्तानी स्थिति जाँच गर्नुहोस्।"
                    } else {
                        if (isEn) "$name will renew tomorrow ($priceStr)."
                        else "$name भोलि नवीकरण हुनेछ ($priceStr)।"
                    }

                    sendNotification(
                        context = context,
                        channelId = CHANNEL_SUBSCRIPTIONS,
                        notificationId = NOTIF_ID_SUBS + sub.id.hashCode() % 1000,
                        title = title,
                        message = message
                    )

                    prefs.edit().putBoolean(dedupeKey, true).apply()
                }
            }
        } catch (_: Exception) {
            // Ignore background check errors
        }
    }

    /**
     * Checks and notifies for incomplete habits to maintain active streaks.
     */
    fun checkAndNotifyHabits(context: Context) {
        val prefs = context.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("notif_habit", true) && ThemePrefs.habitNotification.value
        if (!isEnabled) return

        try {
            val habitRepo = HabitRepository.get(context)
            val stats = habitRepo.computeStats()
            if (stats.todayTotalCount == 0) return

            val todayIso = LocalDate.now().toString()
            val dedupeKey = "habit_notified_$todayIso"
            if (prefs.getBoolean(dedupeKey, false)) return

            val incompleteCount = stats.todayTotalCount - stats.todayCompletedCount

            if (incompleteCount > 0) {
                val isEn = ThemePrefs.lang.value == "en"
                val title = if (isEn) "Daily Habit Check-in" else "दैनिक बानी ट्र्याकर"
                val message = if (isEn) {
                    "You have $incompleteCount habit(s) left for today. Keep your daily streak going!"
                } else {
                    "तपाईंका आज $incompleteCount वटा बानीहरू बाँकी छन्। आफ्नो दैनिक निरन्तरता कायम राख्नुहोस्!"
                }

                sendNotification(
                    context = context,
                    channelId = CHANNEL_HABITS,
                    notificationId = NOTIF_ID_HABITS,
                    title = title,
                    message = message
                )

                prefs.edit().putBoolean(dedupeKey, true).apply()
            }
        } catch (_: Exception) {
            // Ignore background check errors
        }
    }

    /**
     * Checks local weather forecast and sends a precipitation/rain alert if rain probability >= 40%.
     */
    fun checkAndNotifyWeather(context: Context) {
        val prefs = context.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("notif_weather", true) && ThemePrefs.weatherNotification.value
        if (!isEnabled) return

        try {
            val todayIso = LocalDate.now().toString()
            val dedupeKey = "weather_rain_notified_$todayIso"
            if (prefs.getBoolean(dedupeKey, false)) return

            val defaultCity = WeatherRepo.loadLiveCached(context)?.weather ?: WeatherRepo.defaultWeather
            val isEn = ThemePrefs.lang.value == "en"

            val cityName = if (isEn) defaultCity.nameEn else defaultCity.nameNp
            val isRainExpected = defaultCity.iconType == "rain" ||
                    defaultCity.weekly.firstOrNull()?.iconType == "rain" ||
                    (defaultCity.weekly.firstOrNull()?.rainProb ?: 0) >= 40 ||
                    defaultCity.hourly.any { it.iconType == "rain" && it.rainProb >= 40 }

            if (isRainExpected) {
                val rainProb = maxOf(
                    defaultCity.weekly.firstOrNull()?.rainProb ?: 0,
                    defaultCity.hourly.maxOfOrNull { it.rainProb } ?: 50
                )

                val title = if (isEn) "Rain Alert for $cityName" else "$cityName मा वर्षाको सम्भावना"
                val message = if (isEn) {
                    "Rain or showers expected today ($rainProb% chance, ${defaultCity.tempC}°C). Don't forget your umbrella!"
                } else {
                    "आज वर्षा हुने सम्भावना छ ($rainProb% सम्भावना, ${defaultCity.tempC}°C)। छाता साथमा राख्न नबिर्सनुहोस्!"
                }

                sendNotification(
                    context = context,
                    channelId = CHANNEL_WEATHER,
                    notificationId = NOTIF_ID_WEATHER,
                    title = title,
                    message = message
                )

                prefs.edit().putBoolean(dedupeKey, true).apply()
            }
        } catch (_: Exception) {
            // Ignore background check errors
        }
    }

    /**
     * Sends a test alert for a specific channel to verify notification sound and appearance.
     */
    fun sendTestAlert(context: Context, channelType: String) {
        val isEn = ThemePrefs.lang.value == "en"
        createNotificationChannels(context)

        when (channelType) {
            "subs" -> {
                sendNotification(
                    context = context,
                    channelId = CHANNEL_SUBSCRIPTIONS,
                    notificationId = NOTIF_ID_SUBS + 999,
                    title = if (isEn) "Test: Netflix Renewal Reminder" else "परीक्षण: Netflix नवीकरण रिमाइन्डर",
                    message = if (isEn) "Netflix renews tomorrow for NPR 1,500." else "Netflix भोलि रु १,५०० मा नवीकरण हुनेछ।"
                )
            }
            "habits" -> {
                sendNotification(
                    context = context,
                    channelId = CHANNEL_HABITS,
                    notificationId = NOTIF_ID_HABITS + 999,
                    title = if (isEn) "Test: Daily Habit Nudge" else "परीक्षण: दैनिक बानी रिमाइन्डर",
                    message = if (isEn) "Morning Walk & 3L Water remaining today. 5-day streak active!" else "बिहानी हिँडाइ तथा पानी पिउन बाँकी छ। ५ दिने स्ट्रिक जारी छ!"
                )
            }
            "weather" -> {
                sendNotification(
                    context = context,
                    channelId = CHANNEL_WEATHER,
                    notificationId = NOTIF_ID_WEATHER + 999,
                    title = if (isEn) "Test: Rain Alert (Kathmandu)" else "परीक्षण: वर्षाको सूचना (काठमाडौँ)",
                    message = if (isEn) "Light to moderate rain expected (70% chance). Carry an umbrella!" else "हल्का देखि मध्यम वर्षाको सम्भावना (७०%)। छाता साथमा राख्नुहोस्!"
                )
            }
        }
    }

    private fun sendNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        } catch (_: Exception) {
            // General notification error
        }
    }
}
