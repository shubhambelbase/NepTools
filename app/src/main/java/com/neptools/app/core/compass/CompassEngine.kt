package com.neptools.app.core.compass

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.acos

class CompassEngine(
    private val context: Context,
    private val listener: Listener
) : SensorEventListener {

    interface Listener {
        fun onHeading(headingDeg: Float, tiltDeg: Float)
        fun onUnreliable(unreliable: Boolean)
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val fusedSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasSensor: Boolean
        get() = fusedSensor != null || (accelSensor != null && magSensor != null)

    private var usingLegacyPair = false

    private val rMatrix = FloatArray(9)
    private val orientationVals = FloatArray(3)

    private val accelValues = FloatArray(3)
    private val magValues = FloatArray(3)
    private var hasAccel = false
    private var hasMag = false

    private var smoothedHeading = Float.NaN
    private var smoothedTilt = 0f
    private var lastEmitMs = 0L
    private var lastEmittedHeading = Float.NaN
    private var wasUnreliable = false

    fun start() {
        smoothedHeading = Float.NaN
        lastEmittedHeading = Float.NaN
        hasAccel = false
        hasMag = false
        if (fusedSensor != null) {
            usingLegacyPair = false
            sensorManager.registerListener(this, fusedSensor, SensorManager.SENSOR_DELAY_GAME)
        } else if (accelSensor != null && magSensor != null) {
            usingLegacyPair = true
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val unreliable = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD ||
            sensor?.type == Sensor.TYPE_ROTATION_VECTOR ||
            sensor?.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR
        ) {
            if (unreliable != wasUnreliable) {
                wasUnreliable = unreliable
                listener.onUnreliable(unreliable)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when {
            !usingLegacyPair -> {
                val vec = event.values
                if (vec.size < 3) return
                SensorManager.getRotationMatrixFromVector(rMatrix, vec)
            }
            event.sensor.type == Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelValues, 0, 3)
                hasAccel = true
                if (!hasMag) return
                if (!SensorManager.getRotationMatrix(rMatrix, null, accelValues, magValues)) return
            }
            event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magValues, 0, 3)
                hasMag = true
                if (!hasAccel) return
                if (!SensorManager.getRotationMatrix(rMatrix, null, accelValues, magValues)) return
            }
            else -> return
        }

        SensorManager.getOrientation(rMatrix, orientationVals)
        val azimuthRad = orientationVals[0]
        val rawDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
        val heading = ((rawDeg % 360f) + 360f) % 360f

        val cosTilt = rMatrix[8].coerceIn(-1f, 1f)
        val tiltDeg = Math.toDegrees(acos(cosTilt).toDouble()).toFloat()

        val now = System.currentTimeMillis()
        val previous = smoothedHeading
        smoothedHeading = if (previous.isNaN()) {
            heading
        } else {
            smoothAngle(previous, heading, adaptiveAlpha(abs(deltaAngle(previous, heading))))
        }
        smoothedTilt += (tiltDeg - smoothedTilt) * 0.25f

        val swing = !lastEmittedHeading.isNaN() &&
            abs(deltaAngle(lastEmittedHeading, smoothedHeading)) > 1.5f
        if (now - lastEmitMs >= EMIT_INTERVAL_MS || swing) {
            lastEmitMs = now
            lastEmittedHeading = smoothedHeading
            listener.onHeading(((smoothedHeading % 360f) + 360f) % 360f, smoothedTilt)
        }
    }

    fun declination(lat: Double, lon: Double, altitude: Double): Float =
        GeomagneticField(
            lat.toFloat(), lon.toFloat(), altitude.toFloat(), System.currentTimeMillis()
        ).declination

    companion object {

        const val EMIT_INTERVAL_MS = 33L

        fun declinationFor(lat: Double, lon: Double): Float =
            GeomagneticField(lat.toFloat(), lon.toFloat(), 0f, System.currentTimeMillis()).declination

        fun deltaAngle(from: Float, to: Float): Float {
            var d = to - from
            while (d > 180f) d -= 360f
            while (d < -180f) d += 360f
            return d
        }

        fun smoothAngle(previous: Float, target: Float, alpha: Float): Float =
            previous + deltaAngle(previous, target) * alpha

        fun adaptiveAlpha(deltaDeg: Float): Float = when {
            deltaDeg < 0.5f -> 0.10f
            deltaDeg < 2f -> 0.22f
            deltaDeg < 8f -> 0.40f
            deltaDeg < 30f -> 0.60f
            else -> 0.85f
        }

        fun lastKnownCoords(context: Context): Pair<Double, Double>? {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }
            }
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            var best: Location? = null
            for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)) {
                try {
                    val loc = lm.getLastKnownLocation(provider) ?: continue
                    if (best == null || loc.time > best!!.time) best = loc
                } catch (_: SecurityException) {
                } catch (_: IllegalArgumentException) {
                }
            }
            return best?.let { Pair(it.latitude, it.longitude) }
        }
    }
}
