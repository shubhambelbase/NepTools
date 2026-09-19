package com.neptools.app.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.RemoteViews
import com.neptools.app.MainActivity
import com.neptools.app.R
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.data.PatroRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class NepToolsMonthWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        widgetScope.launch {
            try {
                val action = intent.action
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

                if (action == ACTION_PREV_MONTH && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val curOffset = prefs.getInt(KEY_OFFSET_PREFIX + widgetId, 0)
                    prefs.edit().putInt(KEY_OFFSET_PREFIX + widgetId, curOffset - 1).apply()
                    buildAndPush(appContext, AppWidgetManager.getInstance(appContext), widgetId)
                } else if (action == ACTION_NEXT_MONTH && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val curOffset = prefs.getInt(KEY_OFFSET_PREFIX + widgetId, 0)
                    prefs.edit().putInt(KEY_OFFSET_PREFIX + widgetId, curOffset + 1).apply()
                    buildAndPush(appContext, AppWidgetManager.getInstance(appContext), widgetId)
                } else if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
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
        const val ACTION_PREV_MONTH = "com.neptools.app.action.MONTH_WIDGET_PREV"
        const val ACTION_NEXT_MONTH = "com.neptools.app.action.MONTH_WIDGET_NEXT"
        private const val PREFS_NAME = "month_widget_prefs"
        private const val KEY_OFFSET_PREFIX = "offset_"

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
            val component = ComponentName(context, NepToolsMonthWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                buildAndPush(context, manager, id)
            }
        }

        private suspend fun buildAndPush(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            PatroRepo.init(context)
            val repo = PatroRepo.d

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val offset = prefs.getInt(KEY_OFFSET_PREFIX + appWidgetId, 0)

            val todayAd = LocalDate.now()
            val todayBs = repo.engine.adToBs(todayAd)

            // Compute target BS year and month with offset
            var targetYear = todayBs.year
            var targetMonth = todayBs.month + offset
            while (targetMonth < 1) {
                targetMonth += 12
                targetYear -= 1
            }
            while (targetMonth > 12) {
                targetMonth -= 12
                targetYear += 1
            }

            // Month length
            val monthDays = runCatching { repo.engine.monthLength(targetYear, targetMonth) }.getOrDefault(30)
            val firstDayAd = runCatching { repo.engine.bsToAd(NepaliDate(targetYear, targetMonth, 1)) }.getOrDefault(todayAd)
            // Day of week: 1=Monday..7=Sunday -> mapped to Sunday=0..Saturday=6
            val startDayOfWeek = firstDayAd.dayOfWeek.value % 7

            val monthNameNp = NepaliNames.monthsNp.getOrElse(targetMonth - 1) { "" }
            val yearNp = NepaliNames.toDevanagari(targetYear)

            val views = RemoteViews(context.packageName, R.layout.widget_nepali_month)
            views.setTextViewText(R.id.widget_month_title, "$monthNameNp $yearNp")

            // Render crisp month matrix bitmap
            val matrixBitmap = renderMonthMatrix(
                targetYear = targetYear,
                targetMonth = targetMonth,
                monthDays = monthDays,
                startDayOfWeek = startDayOfWeek,
                todayBs = todayBs
            )
            views.setImageViewBitmap(R.id.widget_month_grid_image, matrixBitmap)

            // Prev month button intent
            val prevIntent = Intent(context, NepToolsMonthWidgetProvider::class.java).apply {
                action = ACTION_PREV_MONTH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val prevPending = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 1,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_prev_month, prevPending)

            // Next month button intent
            val nextIntent = Intent(context, NepToolsMonthWidgetProvider::class.java).apply {
                action = ACTION_NEXT_MONTH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val nextPending = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_next_month, nextPending)

            // Click on grid launches Calendar in app
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPending = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 3,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_month_grid_image, appPending)

            manager.updateAppWidget(appWidgetId, views)
        }

        private fun renderMonthMatrix(
            targetYear: Int,
            targetMonth: Int,
            monthDays: Int,
            startDayOfWeek: Int,
            todayBs: NepaliDate
        ): Bitmap {
            val width = 500
            val height = 300
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.textAlign = Paint.Align.CENTER

            // Days of week header (आइत..शनि)
            val headers = listOf("आ", "सो", "मं", "बु", "बि", "शु", "श")
            val colWidth = width / 7f
            val headerY = 32f

            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            headers.forEachIndexed { i, h ->
                val x = (i + 0.5f) * colWidth
                paint.color = if (i == 6) Color.rgb(199, 62, 46) else Color.rgb(100, 116, 139)
                canvas.drawText(h, x, headerY, paint)
            }

            // Divider line
            paint.color = Color.argb(40, 34, 32, 27)
            paint.strokeWidth = 1.5f
            canvas.drawLine(10f, 44f, width - 10f, 44f, paint)

            // Render Day Numbers
            val rowHeight = 42f
            val startY = 50f
            paint.textSize = 17f

            val isCurrentMonthAndYear = (targetYear == todayBs.year && targetMonth == todayBs.month)

            for (day in 1..monthDays) {
                val index = (day - 1) + startDayOfWeek
                val col = index % 7
                val row = index / 7

                val cx = (col + 0.5f) * colWidth
                val cy = startY + (row + 0.5f) * rowHeight

                val isToday = isCurrentMonthAndYear && day == todayBs.day
                val isSaturday = col == 6

                if (isToday) {
                    // Accent background circle for today
                    paint.style = Paint.Style.FILL
                    paint.color = Color.rgb(199, 62, 46)
                    canvas.drawCircle(cx, cy - 6f, 16f, paint)

                    paint.color = Color.WHITE
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    paint.color = if (isSaturday) Color.rgb(199, 62, 46) else Color.rgb(34, 32, 27)
                    paint.typeface = if (isSaturday) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
                }

                val dayText = NepaliNames.toDevanagari(day)
                canvas.drawText(dayText, cx, cy, paint)
            }

            return bitmap
        }
    }
}
