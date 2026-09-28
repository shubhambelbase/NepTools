package com.neptools.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.neptools.app.core.reminder.ReminderHelper
import com.neptools.app.core.work.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Restores the persistent daily Nepali date notification after reboot, clock
 * change, timezone change or app update.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Every step here touches disk: the first PatroRepo read parses the
                // calendar and festival datasets, the panchang solve is a full
                // ephemeris evaluation, and reminder rescheduling re-reads its
                // store. Running that on the broadcast main thread of a process the
                // system just cold-started added seconds to every boot, so hand off
                // to IO and hold the broadcast open until it finishes.
                val pendingResult = goAsync()
                val appContext = context.applicationContext
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        NepaliDateNotificationManager.updateNotification(appContext)
                        NepaliDateStickyService.start(appContext)
                        SmartAlertNotificationManager.createNotificationChannels(appContext)
                        ReminderHelper.rescheduleAll(appContext)
                        WorkScheduler.scheduleAll(appContext)
                    } catch (_: Exception) {
                        // Never let a failed restore crash the boot broadcast; the
                        // midnight alarm and the next app launch will retry.
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
