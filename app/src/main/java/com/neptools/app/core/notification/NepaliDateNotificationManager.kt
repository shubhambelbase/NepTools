package com.neptools.app.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.neptools.app.MainActivity
import com.neptools.app.R
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.PanchangCalc
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.ui.theme.ThemePrefs
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Manages the continuous, high-fidelity daily Nepali Date status bar notification.
 * Remains visible across app swipe-close/backgrounding and automatically rolls over at midnight.
 * Dynamically respects the user's selected language (Nepali / English).
 */
object NepaliDateNotificationManager {

    const val NOTIFICATION_ID = 1001
    private const val CHANNEL_ID = "nepali_date_status_channel_v3"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Delete old channel to clear stale lockscreen system icon cache
            try {
                manager.deleteNotificationChannel("nepali_date_status_channel")
            } catch (e: Exception) {
                // Ignore
            }

            val name = "दैनिक नेपाली मिति / Daily Nepali Date"
            val desc = "Shows today's live Nepali date, Tithi, and festival in the status bar"
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW).apply {
                description = desc
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun updateNotification(context: Context) {
        try {
            val notification = buildNotification(context) ?: return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)

            // Schedule exact midnight update
            DailyDateAlarmScheduler.scheduleMidnightAlarm(context)

            // Synchronize homescreen widgets
            com.neptools.app.core.widget.NepToolsDateWidgetProvider.updateAllWidgets(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelNotification(context: Context) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {
        }
    }

    fun buildNotification(context: Context): Notification? = try {
        createNotificationChannel(context)

        // Ensure preferences and repos are loaded
        try {
            ThemePrefs.load(context)
            PatroRepo.init(context)
        } catch (e: Exception) {
            // Ignore if already initialized
        }

        val todayAd = LocalDate.now()
        val dataset = PatroRepo.d
        val todayNp = dataset.engine.today(todayAd)
        val isEn = ThemePrefs.lang.value == "en"

        val panchang = PanchangCalc.compute(todayAd)

        // Get month festivals
        val monthFestivals = dataset.festivalsFor(todayNp.year, todayNp.month)
        val todayFests = monthFestivals[todayNp.day] ?: emptyList()

        // Find next upcoming festival if none today
        val upcomingFest = if (todayFests.isEmpty()) {
            monthFestivals.entries
                .filter { it.key > todayNp.day }
                .minByOrNull { it.key }
                ?.let { entry ->
                    entry.value.firstOrNull()?.let { fest ->
                        entry.key to fest
                    }
                }
        } else null

        val dayOfWeek = todayAd.dayOfWeek
        val weekdayIdx = com.neptools.app.core.calendar.WeekdayMapper.fromJava(dayOfWeek)
        val weekdayNp = NepaliNames.weekdaysNp[weekdayIdx]
        val weekdayEn = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

        val monthNp = NepaliNames.monthsNp.getOrElse(todayNp.month - 1) { "" }
        val monthEn = NepaliNames.monthsEn.getOrElse(todayNp.month - 1) { "" }

        val yearStr = if (ThemePrefs.nepaliDigits.value && !isEn) NepaliNames.toDevanagari(todayNp.year) else todayNp.year.toString()
        val dayStr = if (ThemePrefs.nepaliDigits.value && !isEn) NepaliNames.toDevanagari(todayNp.day) else todayNp.day.toString()

        val adMonthEn = todayAd.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        val adStr = "${todayAd.dayOfMonth} $adMonthEn ${todayAd.year}"

        // Build bilingual Notification Content
        val titleText: String
        val contentText: String

        if (isEn) {
            val pName = panchang.pakshaEn.ifBlank { panchang.paksha }
            val tName = panchang.tithiNameEn.ifBlank { panchang.tithiName }

            titleText = "$dayStr $monthEn $yearStr, $weekdayEn"
            contentText = "$pName · $tName • $adStr"
        } else {
            titleText = "$monthNp $dayStr, $yearStr $weekdayNp"
            contentText = "${panchang.paksha} ${panchang.tithiName} • $adStr"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val collapsedView = android.widget.RemoteViews(context.packageName, R.layout.notification_date_collapsed).apply {
            setTextViewText(R.id.notif_title, titleText)
            setTextViewText(R.id.notif_subtitle, contentText)
        }

        val eventText: String? = when {
            todayFests.isNotEmpty() -> {
                val fest = todayFests.first()
                val holTag = if (fest.isPublicHoliday) if (isEn) " (Public Holiday)" else " (सार्वजनिक बिदा)" else ""
                if (isEn) "Today: ${fest.nameEn}$holTag" else "आजको पर्व: ${fest.nameNp}$holTag"
            }
            upcomingFest != null -> {
                val (upDay, upFest) = upcomingFest
                if (isEn) "Upcoming: $upDay $monthEn - ${upFest.nameEn}"
                else {
                    val upDayStr = if (ThemePrefs.nepaliDigits.value) NepaliNames.toDevanagari(upDay) else upDay.toString()
                    "आगामी पर्व: $upDayStr गते - ${upFest.nameNp}"
                }
            }
            else -> null
        }

        val expandedView = android.widget.RemoteViews(context.packageName, R.layout.notification_date_expanded).apply {
            setTextViewText(R.id.notif_title_exp, titleText)
            if (eventText != null) {
                setViewVisibility(R.id.notif_event_exp, android.view.View.VISIBLE)
                setTextViewText(R.id.notif_event_exp, eventText)
            } else {
                setViewVisibility(R.id.notif_event_exp, android.view.View.GONE)
            }
            setTextViewText(R.id.notif_panchang_exp, contentText)
        }

        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setColor(0xFFE11D48.toInt())
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setAutoCancel(false)
            .build()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
