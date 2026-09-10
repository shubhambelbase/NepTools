package com.neptools.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.neptools.app.core.radio.RadioService
import com.neptools.app.core.util.OnboardingPrefs
import com.neptools.app.core.util.WeatherLocationManager
import com.neptools.app.ui.navigation.AppNavigator
import com.neptools.app.ui.navigation.PatroApp
import com.neptools.app.ui.screens.OnboardingScreen
import com.neptools.app.ui.screens.SplashScreen
import com.neptools.app.ui.theme.NepToolsTheme

class MainActivity : ComponentActivity() {

    private val splashHandler = Handler(Looper.getMainLooper())
    private var hasRequestedPermissions = false
    private var dismissSplashAction: (() -> Unit)? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (locationGranted) {
                WeatherLocationManager.updatePermissionStatus(applicationContext, true)
            }
        }

    private fun requestAppPermissions() {
        if (hasRequestedPermissions) return
        hasRequestedPermissions = true
        try {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= 33) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } catch (_: Exception) {
            // Protect against unexpected permission dispatch issues on customized OEM ROMs
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent cache & auto-fetch GPS weather in background
        WeatherLocationManager.initAndAutoFetch(applicationContext)

        handleIntent(intent)

        val initiallyNeedsOnboarding = !OnboardingPrefs.isCompleted(applicationContext)
        val isExplicitDeepLink = intent?.action != null && intent.action != Intent.ACTION_MAIN
        val shouldShowSplash = (savedInstanceState == null) && !isExplicitDeepLink

        setContent {
            NepToolsTheme {
                var isSplashActive by rememberSaveable { mutableStateOf(shouldShowSplash) }
                var showOnboarding by remember { mutableStateOf(initiallyNeedsOnboarding) }

                fun finishSplash() {
                    splashHandler.removeCallbacksAndMessages(null)
                    if (isSplashActive) {
                        isSplashActive = false
                        // Defer permission prompt slightly so Compose crossfade finishes smoothly without freezing
                        splashHandler.postDelayed({
                            requestAppPermissions()
                        }, 350L)
                    }
                }

                // If user presses back during splash, immediately route to the app
                BackHandler(enabled = isSplashActive) {
                    finishSplash()
                }

                // Native Hardware Wall-Clock Failsafe:
                // Guarantees splash NEVER blocks or gets stuck on any Android device
                // regardless of frame clock pause, ANR detection, or power management.
                DisposableEffect(Unit) {
                    val fallbackRunnable = Runnable { finishSplash() }
                    splashHandler.postDelayed(fallbackRunnable, 850L)
                    onDispose {
                        splashHandler.removeCallbacks(fallbackRunnable)
                    }
                }

                dismissSplashAction = { finishSplash() }

                Crossfade(
                    targetState = when {
                        isSplashActive -> 0
                        showOnboarding -> 1
                        else -> 2
                    },
                    animationSpec = tween(durationMillis = 180),
                    label = "splash_to_app_crossfade"
                ) { state ->
                    when (state) {
                        0 -> SplashScreen(onSplashFinished = { finishSplash() })
                        1 -> OnboardingScreen(onDone = {
                            OnboardingPrefs.setCompleted(applicationContext)
                            showOnboarding = false
                        })
                        else -> PatroApp()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If app resumes from lock or background while splash was active, immediately route to main
        dismissSplashAction?.let { action ->
            splashHandler.postDelayed({
                action()
            }, 120L)
        }
    }

    override fun onStop() {
        super.onStop()
        // When user backgrounds the app, bypass splash on next interaction
        dismissSplashAction?.invoke()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        splashHandler.removeCallbacksAndMessages(null)
        dismissSplashAction = null
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent?) {
        val route = intent?.getStringExtra(RadioService.EXTRA_NAVIGATE_ROUTE)
        if (route != null) {
            AppNavigator.navigateTo(route)
        }
    }
}
