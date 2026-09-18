package com.neptools.app.ui.screens

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.neptools.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current

    val areAnimationsDisabled = remember {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Throwable) {
            false
        }
    }

    // Fail-safe single-fire guard
    val finishedGuard = remember { AtomicBoolean(false) }
    val currentFinish by rememberUpdatedState(onSplashFinished)
    fun safeFinish() {
        if (finishedGuard.compareAndSet(false, true)) {
            currentFinish()
        }
    }

    // Instant route on back press
    BackHandler {
        safeFinish()
    }

    // Static display delay and wall-clock fallback watchdog
    LaunchedEffect(Unit) {
        if (areAnimationsDisabled) {
            delay(150L)
            safeFinish()
            return@LaunchedEffect
        }

        // Hardware wall-clock fallback on Dispatchers.Default (never blocked by Compose frame clock)
        launch(Dispatchers.Default) {
            delay(750L)
            withContext(Dispatchers.Main) {
                safeFinish()
            }
        }

        // Clean static display duration
        delay(600L)
        safeFinish()
    }

    // Full-screen splash background image (no animation)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Instant 100% reliable tap-to-skip anywhere on screen
                detectTapGestures(onTap = { safeFinish() })
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = "NepTools Splash",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
