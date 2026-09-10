package com.neptools.app.core.radio

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RadioPlayerState(
    val activeStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null
)

object RadioManager {
    private val _state = MutableStateFlow(RadioPlayerState())
    val state: StateFlow<RadioPlayerState> = _state.asStateFlow()

    fun updateState(
        station: RadioStation? = _state.value.activeStation,
        isPlaying: Boolean = _state.value.isPlaying,
        isBuffering: Boolean = _state.value.isBuffering,
        errorMessage: String? = null
    ) {
        _state.value = RadioPlayerState(
            activeStation = station,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            errorMessage = errorMessage
        )
    }

    fun play(context: Context, station: RadioStation) {
        val intent = Intent(context, RadioService::class.java).apply {
            action = RadioService.ACTION_PLAY
            putExtra(RadioService.EXTRA_STATION, station)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun togglePlayPause(context: Context) {
        val intent = Intent(context, RadioService::class.java).apply {
            action = RadioService.ACTION_TOGGLE
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stop(context: Context) {
        val intent = Intent(context, RadioService::class.java).apply {
            action = RadioService.ACTION_STOP
        }
        context.startService(intent)
    }
}
