package com.neptools.app.core.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

/**
 * Foreground service that owns the persistent daily Nepali Date notification.
 * Being a foreground service keeps the process alive so the midnight alarm
 * always fires - even after the app is swiped away from Recents (MIUI/HyperOS
 * otherwise force-stops the app and the date gets stuck until reopen).
 */
class NepaliDateStickyService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        promoteToForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promoteToForeground()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // App was swiped away from Recent Apps list - re-assert the sticky notification
        NepaliDateNotificationManager.updateNotification(applicationContext)
        DailyDateAlarmScheduler.scheduleMidnightAlarm(applicationContext)
        promoteToForeground()
        super.onTaskRemoved(rootIntent)
    }

    private fun promoteToForeground() {
        val notification = NepaliDateNotificationManager.buildNotification(this)
        if (notification == null) {
            NepaliDateNotificationManager.updateNotification(this)
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NepaliDateNotificationManager.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                @Suppress("DEPRECATION")
                startForeground(NepaliDateNotificationManager.NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            // Foreground promotion restricted (e.g. background start) - fall back to plain notify
            NepaliDateNotificationManager.updateNotification(this)
        }
    }

    companion object {
        fun start(context: Context) {
            try {
                val intent = Intent(context, NepaliDateStickyService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                // Fallback direct notification update if background service start is restricted
                NepaliDateNotificationManager.updateNotification(context)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, NepaliDateStickyService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {
            }
        }
    }
}
