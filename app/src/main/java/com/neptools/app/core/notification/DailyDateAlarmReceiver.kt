package com.neptools.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver triggered at midnight to roll over the Nepali Date notification.
 */
class DailyDateAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NepaliDateNotificationManager.updateNotification(context)
    }
}
