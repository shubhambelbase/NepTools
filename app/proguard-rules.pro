# ============================================================================
# NepTools - High Security Production R8 / ProGuard Rules
# Multi-Layer Defense-in-Depth Configuration
# ============================================================================

# ----------------------------------------------------------------------------
# 1. CODE OBFUSCATION & SHRINKING
# ----------------------------------------------------------------------------
-allowaccessmodification

# Obfuscate source file names & line numbers for crash reporting
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Keep essential attributes for Compose, reflection, and annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# ----------------------------------------------------------------------------
# 2. LOG STRIPPING (Remove all android.util.Log calls in release)
# ----------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static int println(...);
}

# ----------------------------------------------------------------------------
# 3. JETPACK COMPOSE & KOTLIN COROUTINES
# ----------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.animation.core.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.navigation.** { *; }
-keep class kotlinx.coroutines.** { *; }

-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

-dontwarn androidx.compose.**
-dontwarn kotlinx.coroutines.**

# ----------------------------------------------------------------------------
# 4. CUSTOM ICONS (PIcons / PatroIcons)
# ----------------------------------------------------------------------------
-keep class com.neptools.app.ui.icons.** { *; }
-keepclassmembers class com.neptools.app.ui.icons.PIcons {
    public static <fields>;
}

# ----------------------------------------------------------------------------
# 5. DATA MODELS & SERIALIZATION
# ----------------------------------------------------------------------------
-keepclassmembers class * {
    ** Companion;
}

# Keep offline datasets and core model properties
-keep class com.neptools.app.core.data.** { *; }
-keep class com.neptools.app.core.calendar.** { *; }
-keep class com.neptools.app.core.habit.** { *; }
-keep class com.neptools.app.core.subscription.** { *; }
-keep class com.neptools.app.core.updater.** { *; }
-keep class com.neptools.app.core.vault.** { *; }
-keep class com.neptools.app.core.astrology.** { *; }
-keep class com.neptools.app.astrology.** { *; }
-keep class com.neptools.app.core.notes.** { *; }
-keep class com.neptools.app.core.tools.** { *; }
-keep class com.neptools.app.core.converter.** { *; }
-keep class com.neptools.app.core.vastu.** { *; }
-keep class com.neptools.app.core.util.** { *; }

# ----------------------------------------------------------------------------
# 6. SECURITY & NATIVE JNI BRIDGE (Keep dynamic JNI targets)
# ----------------------------------------------------------------------------
-keep class com.neptools.app.core.security.** { *; }
-keepclasseswithmembers class com.neptools.app.core.security.NativeSecurityBridge {
    native <methods>;
}

# ----------------------------------------------------------------------------
# 7. BIOMETRIC & CRYPTO PROVIDERS
# ----------------------------------------------------------------------------
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# ----------------------------------------------------------------------------
# 8. BACKGROUND SERVICES, NOTIFICATIONS & ACTIVITIES
# ----------------------------------------------------------------------------
-keep class com.neptools.app.MainActivity { *; }
-keep class com.neptools.app.PatroApp { *; }
-keep class com.neptools.app.ui.screens.SplashScreenKt { *; }
-keep class com.neptools.app.core.notification.** { *; }
-keep class com.neptools.app.core.work.** { *; }
-keep class com.neptools.app.core.radio.** { *; }
-keep class com.neptools.app.core.server.** { *; }
-keep class androidx.work.** { *; }

# Suppress harmless java.time warnings for API < 26 desugaring
-dontwarn java.time.**
