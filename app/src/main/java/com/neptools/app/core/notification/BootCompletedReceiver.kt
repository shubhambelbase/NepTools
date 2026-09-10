package com.neptools.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Automatically restores the persistent daily Nepali date notification on device reboot.
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
                NepaliDateNotificationManager.updateNotification(context)
                NepaliDateStickyService.start(context)
                SmartAlertNotificationManager.createNotificationChannels(context)
                com.neptools.app.core.reminder.ReminderHelper.rescheduleAll(context)
                com.neptools.app.core.work.WorkScheduler.scheduleAll(context)
            }
        }
    }
}
