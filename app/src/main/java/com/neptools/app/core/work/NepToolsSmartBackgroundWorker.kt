package com.neptools.app.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neptools.app.core.data.RatesRepo
import com.neptools.app.core.notification.NepaliDateNotificationManager
import com.neptools.app.core.notification.SmartAlertNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-reliability background worker that operates even when the app is killed or device is restarted.
 * 
 * Executes:
 * 1. Upcoming subscription renewal check
 * 2. Daily habit streak nudge
 * 3. Daily rain & severe weather check
 * 4. Background exchange rates sync
 * 5. Daily date status bar sync
 */
class NepToolsSmartBackgroundWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext

            // 1. Sync daily persistent Nepali date in notification bar
            NepaliDateNotificationManager.updateNotification(ctx)

            // 2. Subscription renewal reminder check (24h/due today)
            SmartAlertNotificationManager.checkAndNotifySubscriptions(ctx)

            // 3. Daily habit tracker streak reminder
            SmartAlertNotificationManager.checkAndNotifyHabits(ctx)

            // 4. Rain & weather alert check
            SmartAlertNotificationManager.checkAndNotifyWeather(ctx)

            // 5. Festival & Fasting reminder check (Ekadashi, Aunsi, Purnima, Parana)
            SmartAlertNotificationManager.checkAndNotifyFestivalsAndFasting(ctx)

            // 6. Background Rates Sync
            try {
                RatesRepo.refresh(ctx) {}
            } catch (_: Exception) {
                // Ignore network errors in background
            }

            // 6. Background Emergency Contacts Sync
            try {
                com.neptools.app.core.util.EmergencySyncManager.sync(ctx, force = false)
            } catch (_: Exception) {
                // Ignore network errors in background
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
