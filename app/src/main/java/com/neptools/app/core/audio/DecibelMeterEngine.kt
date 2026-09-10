package com.neptools.app.core.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import kotlin.math.log10
import kotlin.math.sqrt

@Immutable
data class SoundMeterState(
    val currentDb: Float = 0f,
    val minDb: Float = 0f,
    val maxDb: Float = 0f,
    val avgDb: Float = 0f,
    val history: List<Float> = emptyList(),
    val isRunning: Boolean = false,
    val errorMessage: String? = null
)

class DecibelMeterEngine {

    private val _state = MutableStateFlow(SoundMeterState())
    val state: StateFlow<SoundMeterState> = _state.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var sampleSum = 0.0
    private var sampleCount = 0L
    private var currentMin = Float.MAX_VALUE
    private var currentMax = 0f
    private var smoothedDb = 0f

    private val historyPoints = ArrayDeque<Float>(MAX_HISTORY_SIZE)

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val MAX_HISTORY_SIZE = 60
        private const val CALIBRATION_GAIN = 20.0
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (_state.value.isRunning) return

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _state.value = _state.value.copy(
                    isRunning = false,
                    errorMessage = "Microphone failed to initialize"
                )
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _state.value = _state.value.copy(isRunning = true, errorMessage = null)

            recordingJob = scope.launch {
                val buffer = ShortArray(bufferSize / 2)
                while (isActive && _state.value.isRunning) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readCount > 0) {
                        var sumSquares = 0.0
                        for (i in 0 until readCount) {
                            val sample = buffer[i].toDouble()
                            sumSquares += sample * sample
                        }
                        val rms = sqrt(sumSquares / readCount)
                        val rawDb = if (rms > 1.0) {
                            (20.0 * log10(rms) + CALIBRATION_GAIN).toFloat().coerceIn(0f, 120f)
                        } else {
                            0f
                        }

                        // Exponential moving average for pleasant needle movement
                        smoothedDb = if (smoothedDb == 0f) rawDb else (smoothedDb * 0.65f + rawDb * 0.35f)

                        if (smoothedDb > 5f) {
                            if (smoothedDb < currentMin) currentMin = smoothedDb
                            if (smoothedDb > currentMax) currentMax = smoothedDb
                            sampleSum += smoothedDb
                            sampleCount++
                        }

                        val runningAvg = if (sampleCount > 0) (sampleSum / sampleCount).toFloat() else smoothedDb

                        if (historyPoints.size >= MAX_HISTORY_SIZE) {
                            historyPoints.removeFirst()
                        }
                        historyPoints.addLast(smoothedDb)

                        _state.value = _state.value.copy(
                            currentDb = smoothedDb,
                            minDb = if (currentMin == Float.MAX_VALUE) 0f else currentMin,
                            maxDb = currentMax,
                            avgDb = runningAvg,
                            history = historyPoints.toList(),
                            isRunning = true
                        )
                    }
                    delay(50) // Update UI at ~20 FPS for responsive yet battery-friendly updates
                }
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isRunning = false,
                errorMessage = e.localizedMessage ?: "Audio error"
            )
            stop()
        }
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (_: Exception) {
        } finally {
            audioRecord = null
        }
        _state.value = _state.value.copy(isRunning = false)
    }

    fun reset() {
        sampleSum = 0.0
        sampleCount = 0L
        currentMin = Float.MAX_VALUE
        currentMax = 0f
        smoothedDb = 0f
        historyPoints.clear()
        _state.value = _state.value.copy(
            currentDb = 0f,
            minDb = 0f,
            maxDb = 0f,
            avgDb = 0f,
            history = emptyList()
        )
    }
}
