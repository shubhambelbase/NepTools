import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// ---------------------------------------------------------------------------
// Release signing configuration.
// Credentials live in keystore.properties (gitignored). Never hardcode them.
// ---------------------------------------------------------------------------
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val releaseKeystoreFile = keystoreProperties.getProperty("storeFile")?.let { rootProject.file(it) }
val hasReleaseKeystore = keystorePropertiesFile.exists() &&
        releaseKeystoreFile != null &&
        releaseKeystoreFile.exists()

android {
    namespace = "com.neptools.app"
    compileSdk = 36

    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "com.neptools.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 34
        versionName = "2.7.5"

        externalNativeBuild {
            cmake {
                // Ship the native security library for real device ABIs plus the
                // x86_64 emulator. It is only a few kilobytes.
                arguments += listOf("-DANDROID_STL=c++_static")
            }
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                logger.warn(
                    "NepTools: keystore.properties is missing or incomplete. " +
                        "Falling back to the DEBUG signing key, which is publicly known " +
                        "and must never be used for a distributed build."
                )
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "InvalidFragmentVersionForActivityResult"
        // Fail the build on errors so quality regressions cannot ship silently.
        abortOnError = true
        checkReleaseBuilds = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.media)
    implementation(libs.zxing.core)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.work.runtime)

    testImplementation(libs.junit)
    testImplementation(libs.org.json)
}
