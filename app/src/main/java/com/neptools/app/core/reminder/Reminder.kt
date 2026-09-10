package com.neptools.app.core.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

private const val CHANNEL = "patro_reminders"

object ReminderHelper {

    fun schedule(context: Context, title: String, triggerAtMillis: Long): Boolean {
        if (triggerAtMillis <= System.currentTimeMillis()) return false
        try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val requestCode = ReminderStore.nextId(title, triggerAtMillis)
            val pi = pending(context, title, requestCode)
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
            ReminderStore.add(context, StoredReminder(requestCode, title, triggerAtMillis))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun cancel(context: Context, id: Int) {
        try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.cancel(pi)
            pi.cancel()
            ReminderStore.remove(context, id)
        } catch (e: Exception) { }
    }

    fun rescheduleAll(context: Context) {
        ReminderStore.clearPast(context)
        for (r in ReminderStore.all(context)) {
            try {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pi = pending(context, r.title, r.id)
                if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.triggerAtMillis, pi)
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.triggerAtMillis, pi)
                }
            } catch (e: Exception) { }
        }
    }

    private fun pending(context: Context, title: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("id", requestCode)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "सम्झना"
        val id = intent.getIntExtra("id", title.hashCode())
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "पात्रो सम्झना", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val launch = PendingIntent.getActivity(
            context, 0, Intent(context, com.neptools.app.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(com.neptools.app.R.drawable.ic_stat_notify)
            .setColor(0xFFC73E2E.toInt())
            .setContentTitle(context.getString(com.neptools.app.R.string.app_name))
            .setContentText(title)
            .setContentIntent(launch)
            .setAutoCancel(true)
            .build()
        nm.notify(id, notif)
        // Remove one-shot from store after firing
        try { ReminderStore.remove(context, id) } catch (e: Exception) { }
    }
}
