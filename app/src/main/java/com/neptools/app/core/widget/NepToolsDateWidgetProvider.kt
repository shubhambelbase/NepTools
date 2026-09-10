package com.neptools.app.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.neptools.app.MainActivity
import com.neptools.app.R
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.PanchangCalc
import com.neptools.app.core.data.PatroRepo

class NepToolsDateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.intent.action.TIME_SET",
            ACTION_UPDATE_WIDGET -> {
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.neptools.app.action.UPDATE_WIDGET"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, NepToolsDateWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (widgetId in allWidgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            } catch (_: Exception) {
            }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val prefs = context.getSharedPreferences("patro_prefs", Context.MODE_PRIVATE)
                val lang = prefs.getString("lang", "np") ?: "np"
                val isEn = (lang == "en")
                val useNpDigits = prefs.getBoolean("np_digits", !isEn)

                val dataset = PatroRepo.d
                val engine = dataset.engine
                val today = engine.today()
                val todayAd = engine.bsToAd(today)
                val weekdayIdx = engine.weekdayIndexOf(today).coerceIn(0, 6)

                val panchang = PanchangCalc.compute(todayAd)
                val festivalToday = dataset.festivalsFor(today.year, today.month)[today.day]?.firstOrNull()

                val views = RemoteViews(context.packageName, R.layout.widget_nepali_date)

                val dayStr = if (useNpDigits) NepaliNames.toDevanagari(today.day) else today.day.toString()
                views.setTextViewText(R.id.widget_day_text, dayStr)

                val barStr = if (isEn) {
                    NepaliNames.weekdaysEn[weekdayIdx]
                } else {
                    NepaliNames.weekdaysNp[weekdayIdx]
                }
                views.setTextViewText(R.id.widget_bar_text, barStr)

                val monthYearStr = if (isEn) {
                    val mName = NepaliNames.monthsEn.getOrElse(today.month - 1) { "BS" }
                    "$mName ${today.year}"
                } else {
                    val mName = NepaliNames.monthsNp.getOrElse(today.month - 1) { "वि.सं." }
                    val yStr = if (useNpDigits) NepaliNames.toDevanagari(today.year) else today.year.toString()
                    "$mName $yStr"
                }
                views.setTextViewText(R.id.widget_month_year_text, monthYearStr)

                val adMonthName = todayAd.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                val adDateStr = "$adMonthName ${todayAd.dayOfMonth}, ${todayAd.year}"
                views.setTextViewText(R.id.widget_ad_date_text, adDateStr)

                val tithiFull = if (isEn) {
                    if (panchang.pakshaEn.isNotBlank()) "${panchang.tithiNameEn} (${panchang.pakshaEn})" else panchang.tithiNameEn
                } else {
                    if (panchang.paksha.isNotBlank()) "${panchang.tithiName}, ${panchang.paksha}" else panchang.tithiName
                }
                views.setTextViewText(R.id.widget_tithi_text, tithiFull)

                if (festivalToday != null) {
                    val festName = if (isEn) festivalToday.nameEn else festivalToday.nameNp
                    views.setTextViewText(R.id.widget_festival_text, festName)
                    views.setViewVisibility(R.id.widget_festival_text, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_festival_text, View.GONE)
                }

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {
            }
        }
    }
}
