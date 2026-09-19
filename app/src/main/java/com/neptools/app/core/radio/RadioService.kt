package com.neptools.app.core.radio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.neptools.app.MainActivity
import com.neptools.app.R
import com.neptools.app.ui.navigation.Routes
import com.neptools.app.ui.theme.ThemePrefs

class RadioService : Service() {

    companion object {
        const val CHANNEL_ID = "neptools_radio_channel"
        const val NOTIFICATION_ID = 9610

        const val ACTION_PLAY = "com.neptools.app.radio.ACTION_PLAY"
        const val ACTION_TOGGLE = "com.neptools.app.radio.ACTION_TOGGLE"
        const val ACTION_STOP = "com.neptools.app.radio.ACTION_STOP"
        const val EXTRA_STATION = "extra_station"
        const val EXTRA_NAVIGATE_ROUTE = "navigate_to_route"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentStation: RadioStation? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NepTools::RadioWakeLock").apply {
            setReferenceCounted(false)
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiManager.WIFI_MODE_FULL_LOW_LATENCY
        } else {
            @Suppress("DEPRECATION")
            WifiManager.WIFI_MODE_FULL_HIGH_PERF
        }
        wifiLock = wifiManager.createWifiLock(wifiMode, "NepTools::RadioWifiLock").apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_PLAY -> {
                val station = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_STATION, RadioStation::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(EXTRA_STATION) as? RadioStation
                }
                if (station != null) {
                    currentStation = station
                    startPlayback(station)
                }
            }
            ACTION_TOGGLE -> {
                togglePlayback()
            }
            ACTION_STOP -> {
                stopPlayback()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startPlayback(station: RadioStation, useFallback: Boolean = false) {
        val targetUrl = if (useFallback && station.fallbackUrl != null) station.fallbackUrl else station.streamUrl

        wakeLock?.acquire(3600_000L) // 1 hr max lock
        wifiLock?.acquire()

        RadioManager.updateState(station = station, isPlaying = false, isBuffering = true)
        startForegroundWithNotification(buildNotification(station, isPlaying = false, isBuffering = true))

        releasePlayer()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
            mediaPlayer = player
            player.setDataSource(targetUrl)

            player.setOnPreparedListener { mp ->
                try {
                    mp.start()
                    RadioManager.updateState(station = station, isPlaying = true, isBuffering = false)
                    updateNotification(station, isPlaying = true, isBuffering = false)
                } catch (e: Exception) {
                    RadioManager.updateState(station = station, isPlaying = false, isBuffering = false, errorMessage = e.message)
                    updateNotification(station, isPlaying = false, isBuffering = false)
                }
            }

            player.setOnErrorListener { _, what, extra ->
                if (!useFallback && station.fallbackUrl != null) {
                    startPlayback(station, useFallback = true)
                } else {
                    RadioManager.updateState(
                        station = station,
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "Error ($what:$extra)"
                    )
                    updateNotification(station, isPlaying = false, isBuffering = false)
                }
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            if (!useFallback && station.fallbackUrl != null) {
                startPlayback(station, useFallback = true)
            } else {
                RadioManager.updateState(station = station, isPlaying = false, isBuffering = false, errorMessage = e.message)
                updateNotification(station, isPlaying = false, isBuffering = false)
            }
        }
    }

    private fun togglePlayback() {
        val player = mediaPlayer
        val station = currentStation ?: return

        if (player != null && player.isPlaying) {
            try {
                player.pause()
            } catch (e: Exception) {}
            RadioManager.updateState(station = station, isPlaying = false, isBuffering = false)
            updateNotification(station, isPlaying = false, isBuffering = false)
        } else if (player != null) {
            try {
                player.start()
                RadioManager.updateState(station = station, isPlaying = true, isBuffering = false)
                updateNotification(station, isPlaying = true, isBuffering = false)
            } catch (e: Exception) {
                startPlayback(station)
            }
        } else {
            startPlayback(station)
        }
    }

    private fun stopPlayback() {
        releasePlayer()
        RadioManager.updateState(station = null, isPlaying = false, isBuffering = false)
        if (wakeLock?.isHeld == true) wakeLock?.release()
        if (wifiLock?.isHeld == true) wifiLock?.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.reset()
                mp.release()
            }
        } catch (e: Exception) {}
        mediaPlayer = null
    }

    private fun startForegroundWithNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(station: RadioStation, isPlaying: Boolean, isBuffering: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(station, isPlaying, isBuffering))
    }

    private fun buildNotification(station: RadioStation, isPlaying: Boolean, isBuffering: Boolean): Notification {
        // Deep-link PendingIntent to open Radio page directly
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAVIGATE_ROUTE, Routes.RADIO)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            101,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle action
        val toggleIntent = Intent(this, RadioService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            102,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop action
        val stopIntent = Intent(this, RadioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            103,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isEn = ThemePrefs.lang.value == "en"
        val statusText = when {
            isBuffering -> if (isEn) "Connecting stream • ${station.frequency}" else "जडान हुँदैछ • ${station.frequency}"
            isPlaying -> if (isEn) "Live Broadcast • ${station.frequency} (${station.location})" else "प्रत्यक्ष प्रसारण • ${station.frequency} (${station.location})"
            else -> if (isEn) "Paused • Tap to resume" else "रोकिएको • पुनः बजाउन थिच्नुहोस्"
        }

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) (if (isEn) "Pause" else "रोक्नुहोस्") else (if (isEn) "Play" else "बजाउनुहोस्")

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setColor(0xFFE11D48.toInt())
            .setContentTitle(if (isEn) station.nameEn else station.nameNp)
            .setContentText(statusText)
            .setSubText(if (isEn) "Live FM Radio" else "लाइभ एफएम रेडियो")
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying || isBuffering)
            .setOnlyAlertOnce(true)
            .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, if (isEn) "Stop" else "बन्द", stopPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nepal Patro Live FM Radio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background streaming and playback controls for Live FM Radio"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
