package com.neptools.app.core.level

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2

class BubbleLevelEngine(
    context: Context,
    private val listener: Listener
) : SensorEventListener {

    interface Listener {
        fun onUpdate(pitchDeg: Float, rollDeg: Float, slopeDeg: Float)
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val prefs = context.getSharedPreferences("bubble_level", Context.MODE_PRIVATE)

    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val hasSensor: Boolean get() = gravitySensor != null

    private var sx = 0f
    private var sy = 0f
    private var sz = 0f
    private var initialized = false

    private var zeroPitch = 0f
    private var zeroRoll = 0f
    private var zeroSlope = 0f

    private var lastEmitMs = 0L
    private var lastPitch = Float.NaN
    private var lastRoll = Float.NaN

    init {
        zeroPitch = prefs.getFloat("zero_pitch", 0f)
        zeroRoll = prefs.getFloat("zero_roll", 0f)
        zeroSlope = prefs.getFloat("zero_slope", 0f)
    }

    fun start() {
        initialized = false
        gravitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setZero(pitchDeg: Float, rollDeg: Float, slopeDeg: Float) {
        zeroPitch = pitchDeg
        zeroRoll = rollDeg
        zeroSlope = slopeDeg
        prefs.edit()
            .putFloat("zero_pitch", pitchDeg)
            .putFloat("zero_roll", rollDeg)
            .putFloat("zero_slope", slopeDeg)
            .apply()
    }

    fun clearZero() = setZero(0f, 0f, 0f)

    fun hasZero(): Boolean = abs(zeroPitch) > 0.01f || abs(zeroRoll) > 0.01f || abs(zeroSlope) > 0.01f

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GRAVITY && event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        if (event.values.size < 3) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!initialized) {
            sx = x; sy = y; sz = z
            initialized = true
        } else {
            val delta = abs(x - sx) + abs(y - sy) + abs(z - sz)
            val alpha = adaptiveAlpha(delta)
            sx += (x - sx) * alpha
            sy += (y - sy) * alpha
            sz += (z - sz) * alpha
        }

        val norm = kotlin.math.sqrt(sx * sx + sy * sy + sz * sz)
        if (norm < 1e-3f) return

        val pitchRaw = Math.toDegrees(atan2(sy.toDouble(), sz.toDouble())).toFloat()
        val rollRaw = Math.toDegrees(atan2(sx.toDouble(), sz.toDouble())).toFloat()
        val slopeRaw = Math.toDegrees(
            acos((sz / norm).toDouble().coerceIn(-1.0, 1.0))
        ).toFloat()

        val pitch = wrap180(pitchRaw - zeroPitch)
        val roll = wrap180(rollRaw - zeroRoll)
        val slope = (slopeRaw - zeroSlope).coerceIn(-90f, 180f)

        val now = System.currentTimeMillis()
        val moved = if (lastPitch.isNaN()) 999f
        else maxOf(abs(deltaAngle(lastPitch, pitch)), abs(deltaAngle(lastRoll, roll)))

        if (now - lastEmitMs >= EMIT_INTERVAL_MS || moved > 0.4f) {
            lastEmitMs = now
            lastPitch = pitch
            lastRoll = roll
            listener.onUpdate(pitch, roll, slope)
        }
    }

    companion object {
        const val EMIT_INTERVAL_MS = 33L

        fun wrap180(v: Float): Float {
            var r = v
            while (r > 180f) r -= 360f
            while (r < -180f) r += 360f
            return r
        }

        fun deltaAngle(a: Float, b: Float): Float = b - a

        fun adaptiveAlpha(delta: Float): Float = when {
            delta < 0.15f -> 0.10f
            delta < 0.5f -> 0.20f
            delta < 2f -> 0.40f
            else -> 0.65f
        }

        fun beepLevel(context: Context) {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                android.os.Handler(context.mainLooper).postDelayed({
                    try { tg.release() } catch (_: Throwable) {}
                }, 250)
            } catch (_: Throwable) {}
        }
    }
}
