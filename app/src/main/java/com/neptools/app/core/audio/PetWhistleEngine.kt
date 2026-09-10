package com.neptools.app.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class WhistlePulsePattern(val labelEn: String, val labelNp: String) {
    CONTINUOUS("Continuous", "निरन्तर ध्वनी"),
    SLOW_PULSE("Slow Pulse (500ms)", "सुस्त पल्स (५००ms)"),
    RAPID_BEEP("Rapid Beep (150ms)", "द्रुत बीप (१५०ms)"),
    STROBE("Strobe (60ms)", "स्ट्रोब (६०ms)"),
    SOS("SOS Pattern", "SOS संकेत")
}

object PetWhistleEngine {
    private const val SAMPLE_RATE = 48000
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isPlaying: Boolean = false
        private set

    @Volatile
    var frequencyHz: Int = 16000
        set(value) {
            field = value.coerceIn(1000, 24000)
        }

    @Volatile
    var volume: Float = 0.85f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
            try {
                audioTrack?.setVolume(field)
            } catch (_: Exception) {}
        }

    @Volatile
    var pulsePattern: WhistlePulsePattern = WhistlePulsePattern.CONTINUOUS

    fun startPlayback(onStopCallback: (() -> Unit)? = null) {
        if (isPlaying) return
        isPlaying = true

        playbackJob?.cancel()
        playbackJob = scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(SAMPLE_RATE / 10)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.setVolume(volume)
                track.play()

                val bufferSize = SAMPLE_RATE / 20 // 50ms chunk (2400 samples)
                val buffer = ShortArray(bufferSize)
                var phase = 0.0

                var patternTimerMs = 0L
                val chunkDurationMs = (bufferSize * 1000L) / SAMPLE_RATE

                while (isActive && isPlaying) {
                    val currentFreq = frequencyHz.toDouble()
                    val currentVol = volume
                    val pattern = pulsePattern

                    // Determine if currently in active sound phase or silent phase based on pattern
                    val isSoundActive = when (pattern) {
                        WhistlePulsePattern.CONTINUOUS -> true
                        WhistlePulsePattern.SLOW_PULSE -> (patternTimerMs % 1000) < 500
                        WhistlePulsePattern.RAPID_BEEP -> (patternTimerMs % 300) < 150
                        WhistlePulsePattern.STROBE -> (patternTimerMs % 120) < 60
                        WhistlePulsePattern.SOS -> {
                            val cycle = patternTimerMs % 2800
                            when {
                                cycle < 200 -> true
                                cycle in 300..500 -> true
                                cycle in 600..800 -> true
                                cycle in 1000..1500 -> true
                                cycle in 1600..2100 -> true
                                cycle in 2200..2700 -> true
                                else -> false
                            }
                        }
                    }

                    val targetAmplitude = if (isSoundActive) (Short.MAX_VALUE * currentVol * 0.95).toInt() else 0

                    for (i in buffer.indices) {
                        val sample = if (targetAmplitude > 0) {
                            (sin(phase) * targetAmplitude).toInt().toShort()
                        } else {
                            0.toShort()
                        }
                        buffer[i] = sample
                        phase += 2.0 * PI * currentFreq / SAMPLE_RATE
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }

                    track.write(buffer, 0, buffer.size)
                    patternTimerMs += chunkDurationMs
                    if (patternTimerMs > 1000000L) patternTimerMs = 0L
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cleanUpTrack()
                isPlaying = false
                onStopCallback?.invoke()
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        cleanUpTrack()
    }

    private fun cleanUpTrack() {
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        audioTrack = null
    }
}
