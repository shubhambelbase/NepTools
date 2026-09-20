package com.neptools.app.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Immutable
data class EmfSensorState(
    val magnitudeUt: Float = 0f,
    val xUt: Float = 0f,
    val yUt: Float = 0f,
    val zUt: Float = 0f,
    val peakUt: Float = 0f,
    val isAnomalyDetected: Boolean = false,
    val threatLevel: EmfThreatLevel = EmfThreatLevel.NORMAL,
    val history: List<Float> = emptyList()
)

enum class EmfThreatLevel {
    NORMAL,      // < 55 uT (ambient Earth field)
    SUSPICIOUS,  // 55 - 90 uT (mild electrical emission)
    HIGH_ALERT   // > 90 uT (strong electromagnetic emitter nearby)
}

class SpyCameraDetectorEngine(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _emfState = MutableStateFlow(EmfSensorState())
    val emfState: StateFlow<EmfSensorState> = _emfState.asStateFlow()

    private var magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val hasSensor: Boolean get() = magneticSensor != null
    private var isEmfRunning = false
    private var peakMagnitude = 0f
    private val emfHistory = ArrayDeque<Float>(50)

    // Strobe Torch Controller
    private var strobeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var primaryCameraId: String? = null
    private var isTorchOn = false

    init {
        try {
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                    primaryCameraId = id
                    break
                }
            }
        } catch (_: Exception) {
        }
    }

    fun startEmfScan() {
        if (isEmfRunning || magneticSensor == null) return
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI)
        isEmfRunning = true
    }

    fun stopEmfScan() {
        if (!isEmfRunning) return
        sensorManager.unregisterListener(this)
        isEmfRunning = false
    }

    fun resetEmfPeak() {
        peakMagnitude = 0f
        emfHistory.clear()
        _emfState.value = _emfState.value.copy(peakUt = 0f, history = emptyList())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val mag = sqrt(x * x + y * y + z * z)

        if (mag > peakMagnitude) {
            peakMagnitude = mag
        }

        if (emfHistory.size >= 50) {
            emfHistory.removeFirst()
        }
        emfHistory.addLast(mag)

        val threat = when {
            mag > 90f -> EmfThreatLevel.HIGH_ALERT
            mag > 58f -> EmfThreatLevel.SUSPICIOUS
            else -> EmfThreatLevel.NORMAL
        }

        // Haptic feedback on high alert
        if (threat == EmfThreatLevel.HIGH_ALERT) {
            vibrateAlert()
        }

        _emfState.value = EmfSensorState(
            magnitudeUt = mag,
            xUt = x,
            yUt = y,
            zUt = z,
            peakUt = peakMagnitude,
            isAnomalyDetected = threat != EmfThreatLevel.NORMAL,
            threatLevel = threat,
            history = emfHistory.toList()
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private var lastVibrateTime = 0L
    private fun vibrateAlert() {
        val now = System.currentTimeMillis()
        if (now - lastVibrateTime > 600) {
            lastVibrateTime = now
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            } catch (_: Exception) {}
        }
    }

    // --- Strobe Light Controller ---
    fun startStrobe(frequencyHz: Int) {
        stopStrobe()
        val camId = primaryCameraId ?: return

        strobeJob = scope.launch {
            val periodMs = (1000L / frequencyHz.coerceIn(1, 15)).coerceAtLeast(60L)
            val halfPeriod = periodMs / 2
            try {
                while (isActive) {
                    setTorch(camId, true)
                    delay(halfPeriod)
                    setTorch(camId, false)
                    delay(halfPeriod)
                }
            } finally {
                setTorch(camId, false)
            }
        }
    }

    fun setSolidTorch(enabled: Boolean) {
        stopStrobe()
        val camId = primaryCameraId ?: return
        setTorch(camId, enabled)
    }

    private fun setTorch(camId: String, enabled: Boolean) {
        try {
            cameraManager.setTorchMode(camId, enabled)
            isTorchOn = enabled
        } catch (_: Exception) {}
    }

    fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        val camId = primaryCameraId ?: return
        try {
            cameraManager.setTorchMode(camId, false)
            isTorchOn = false
        } catch (_: Exception) {}
    }

    fun release() {
        stopEmfScan()
        stopStrobe()
    }
}
