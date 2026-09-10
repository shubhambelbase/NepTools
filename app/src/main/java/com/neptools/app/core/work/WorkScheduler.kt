package com.neptools.app.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val RATES_WORK = "rates_sync_12h"
    private const val SMART_ALERTS_WORK = "neptools_smart_alerts_4h"

    fun scheduleAll(context: Context) {
        scheduleRatesSync(context)
        scheduleSmartAlertsWorker(context)
    }

    fun scheduleRatesSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()
        val request = PeriodicWorkRequestBuilder<RatesSyncWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RATES_WORK, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun scheduleSmartAlertsWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        // 4-hour background cycle guarantees notifications for upcoming subscriptions, habit check-ins, and rain alerts
        val request = PeriodicWorkRequestBuilder<NepToolsSmartBackgroundWorker>(4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SMART_ALERTS_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
