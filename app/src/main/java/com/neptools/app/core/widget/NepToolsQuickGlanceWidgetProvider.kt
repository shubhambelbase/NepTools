package com.neptools.app.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.neptools.app.MainActivity
import com.neptools.app.R
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.FuelRepo
import com.neptools.app.core.data.RatesRepo
import com.neptools.app.core.radio.RadioService
import com.neptools.app.ui.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NepToolsQuickGlanceWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        widgetScope.launch {
            try {
                if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
                    val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: IntArray(0)
                    val manager = AppWidgetManager.getInstance(appContext)
                    for (id in ids) {
                        buildAndPush(appContext, manager, id)
                    }
                } else {
                    updateAllWidgetsBlocking(appContext)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val appContext = context.applicationContext
        widgetScope.launch {
            for (id in appWidgetIds) {
                buildAndPush(appContext, appWidgetManager, id)
            }
        }
    }

    companion object {
        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        fun updateAllWidgets(context: Context) {
            val appContext = context.applicationContext
            widgetScope.launch {
                try {
                    updateAllWidgetsBlocking(appContext)
                } catch (_: Exception) {
                }
            }
        }

        private suspend fun updateAllWidgetsBlocking(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, NepToolsQuickGlanceWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                buildAndPush(context, manager, id)
            }
        }

        private fun buildAndPush(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_glance)

            // Read Fuel rates
            val fuel = FuelRepo.loadCached(context)
            val petrolPrice = fuel.petrol.toInt()
            val dieselPrice = fuel.diesel.toInt()
            val petrolStr = "पेट्रोल: रु " + NepaliNames.toDevanagari(petrolPrice)
            val dieselStr = "डिजेल: रु " + NepaliNames.toDevanagari(dieselPrice)

            views.setTextViewText(R.id.widget_petrol_text, petrolStr)
            views.setTextViewText(R.id.widget_diesel_text, dieselStr)

            // Read Forex rates
            val forex = RatesRepo.loadCached(context)
            val usdBuy = forex.nrbDetails["USD"]?.buy ?: RatesRepo.defaultDetails["USD"]?.buy ?: 134.50
            val usdFormatted = String.format("%.2f", usdBuy)
            val usdStr = "USD: रु " + NepaliNames.toDevanagari(usdFormatted)
            val inrStr = "INR: रु " + NepaliNames.toDevanagari("1.60")

            views.setTextViewText(R.id.widget_usd_text, usdStr)
            views.setTextViewText(R.id.widget_inr_text, inrStr)

            // Deep-link PendingIntents
            views.setOnClickPendingIntent(
                R.id.widget_fuel_section,
                createRoutePendingIntent(context, appWidgetId * 10 + 1, Routes.FUEL)
            )

            views.setOnClickPendingIntent(
                R.id.widget_forex_section,
                createRoutePendingIntent(context, appWidgetId * 10 + 2, Routes.CURRENCY)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_voice,
                createRoutePendingIntent(context, appWidgetId * 10 + 3, Routes.VOICE)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_land,
                createRoutePendingIntent(context, appWidgetId * 10 + 4, Routes.LAND_CONVERTER)
            )

            manager.updateAppWidget(appWidgetId, views)
        }

        private fun createRoutePendingIntent(context: Context, requestCode: Int, route: String): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(RadioService.EXTRA_NAVIGATE_ROUTE, route)
            }
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
