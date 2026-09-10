package com.neptools.app.core.updater

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class GitHubUpdateManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("patro_github_updater_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_OWNER = "shubhambelbase"
        const val DEFAULT_REPO = "NepTools"
        private const val KEY_AUTO_CHECK = "auto_check"
        private const val KEY_LAST_CHECK = "last_checked"

        @Volatile
        private var instance: GitHubUpdateManager? = null

        fun get(context: Context): GitHubUpdateManager {
            return instance ?: synchronized(this) {
                instance ?: GitHubUpdateManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getConfig(): GitHubUpdaterConfig {
        return GitHubUpdaterConfig(
            owner = DEFAULT_OWNER,
            repo = DEFAULT_REPO,
            token = "",
            autoCheckOnStartup = prefs.getBoolean(KEY_AUTO_CHECK, true),
            lastCheckedTimestamp = prefs.getLong(KEY_LAST_CHECK, 0L)
        )
    }

    fun saveConfig(config: GitHubUpdaterConfig) {
        prefs.edit()
            .putBoolean(KEY_AUTO_CHECK, config.autoCheckOnStartup)
            .putLong(KEY_LAST_CHECK, config.lastCheckedTimestamp)
            .apply()
    }

    fun getCurrentVersionName(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "2.5.2"
        } catch (e: Exception) {
            "2.5.2"
        }
    }

    fun getCurrentVersionCode(): Long {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            11L
        }
    }

    /**
     * Checks for updates from GitHub releases with fallback and hash extraction.
     */
    suspend fun checkForUpdates(): Result<GitHubReleaseInfo?> = withContext(Dispatchers.IO) {
        val currentVer = getCurrentVersionName()

        // 1. Try GitHub REST API
        try {
            val apiUrl = "https://api.github.com/repos/$DEFAULT_OWNER/$DEFAULT_REPO/releases/latest"
            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "NepTools-Android-App")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            if (conn.responseCode in 200..299) {
                val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val tagName = root.getString("tag_name")
                val releaseName = root.optString("name", tagName)
                val releaseNotes = root.optString("body", "Bug fixes and performance improvements.")
                val publishedAt = root.optString("published_at", "")

                val assets = root.optJSONArray("assets")
                var apkDownloadUrl = ""
                var apkFileName = "app-release.apk"
                var apkAssetId = 0L
                var apkSize = 0L

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkFileName = name
                            apkDownloadUrl = asset.optString("browser_download_url", "")
                            apkAssetId = asset.optLong("id", 0L)
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                if (apkDownloadUrl.isBlank()) {
                    apkDownloadUrl = "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO/releases/download/$tagName/app-release.apk"
                }

                // Extract SHA-256 checksum if mentioned in release notes body (e.g. SHA256: 64_HEX_CHARS)
                val shaRegex = Regex("""(?:sha-?256|hash)\s*[:=]\s*([a-fA-F0-9]{64})""", RegexOption.IGNORE_CASE)
                val match = shaRegex.find(releaseNotes)
                val foundHash = match?.groupValues?.get(1)?.lowercase() ?: ""

                val latestVerClean = tagName.trimStart('v', 'V')
                val isNewer = isVersionNewer(latestVerClean, currentVer.trimStart('v', 'V'))

                return@withContext Result.success(
                    GitHubReleaseInfo(
                        tagName = tagName,
                        releaseName = releaseName,
                        releaseNotes = releaseNotes,
                        publishedAt = publishedAt,
                        apkFileName = apkFileName,
                        apkDownloadUrl = apkDownloadUrl,
                        apkAssetId = apkAssetId,
                        apkSize = apkSize,
                        isNewerVersion = isNewer,
                        currentVersionName = currentVer,
                        latestVersionName = latestVerClean,
                        sha256Checksum = foundHash
                    )
                )
            }
        } catch (_: Exception) {
            // Fall through to web redirect method
        }

        // 2. Fallback: Direct Web Release redirect (100% reliable, zero rate limit, zero token)
        try {
            val webUrl = "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO/releases/latest"
            val conn = (URL(webUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            }

            val location = conn.getHeaderField("Location")
            if (location != null && location.contains("/releases/tag/")) {
                val tagName = location.substringAfterLast("/releases/tag/").trim()
                val latestVerClean = tagName.trimStart('v', 'V')
                val isNewer = isVersionNewer(latestVerClean, currentVer.trimStart('v', 'V'))
                val downloadUrl = "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO/releases/download/$tagName/app-release.apk"

                return@withContext Result.success(
                    GitHubReleaseInfo(
                        tagName = tagName,
                        releaseName = "NepTools $tagName",
                        releaseNotes = "New update available with performance improvements and security hardening.",
                        publishedAt = "",
                        apkFileName = "app-release.apk",
                        apkDownloadUrl = downloadUrl,
                        apkAssetId = 0L,
                        apkSize = 6700000L,
                        isNewerVersion = isNewer,
                        currentVersionName = currentVer,
                        latestVersionName = latestVerClean
                    )
                )
            }

            return@withContext Result.failure(Exception("Unable to reach update server. Please check your internet connection."))
        } catch (e: Exception) {
            return@withContext Result.failure(Exception(e.localizedMessage ?: "Failed to check for updates"))
        }
    }

    /**
     * Checks if the APK for this release is already downloaded and verified in cache.
     */
    fun getCachedDownloadedApk(releaseInfo: GitHubReleaseInfo): File? {
        return try {
            val updatesDir = File(context.cacheDir, "updates")
            val targetFile = File(updatesDir, "${releaseInfo.tagName}_${releaseInfo.apkFileName}")
            val legacyFile = File(updatesDir, releaseInfo.apkFileName)

            val fileToTest = if (targetFile.exists() && targetFile.length() > 500_000) targetFile else legacyFile
            if (fileToTest.exists() && fileToTest.length() > 500_000) {
                if (releaseInfo.apkSize <= 0 || kotlin.math.abs(fileToTest.length() - releaseInfo.apkSize) < 10000 || fileToTest.length() > 1_000_000) {
                    val integrity = verifyApkIntegrity(fileToTest, releaseInfo.sha256Checksum)
                    if (integrity.isSuccess) fileToTest else null
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Downloads the APK file from GitHub release with high performance 32KB buffer and integrity verification.
     */
    suspend fun downloadApk(
        releaseInfo: GitHubReleaseInfo,
        onProgress: (percent: Int, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val outputFile = File(updatesDir, "${releaseInfo.tagName}_${releaseInfo.apkFileName}")

            // If already fully downloaded & verified, return cached file immediately
            if (outputFile.exists() && outputFile.length() > 500_000) {
                val cachedIntegrity = verifyApkIntegrity(outputFile, releaseInfo.sha256Checksum)
                if (cachedIntegrity.isSuccess) {
                    onProgress(100, outputFile.length(), outputFile.length())
                    return@withContext Result.success(outputFile)
                }
            }

            val downloadUrl = releaseInfo.apkDownloadUrl.ifBlank {
                "https://github.com/$DEFAULT_OWNER/$DEFAULT_REPO/releases/download/${releaseInfo.tagName}/app-release.apk"
            }

            var connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("User-Agent", "NepTools-Android-App")
                instanceFollowRedirects = true
            }

            // Handle GitHub asset redirect (302 Found)
            var responseCode = connection.responseCode
            var redirects = 0
            while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER) && redirects < 5) {
                val newUrl = connection.getHeaderField("Location")
                connection = (URL(newUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "NepTools-Android-App")
                }
                responseCode = connection.responseCode
                redirects++
            }

            if (responseCode !in 200..299) {
                return@withContext Result.failure(Exception("Download failed: HTTP $responseCode"))
            }

            val totalBytes = if (connection.contentLengthLong > 0) connection.contentLengthLong else releaseInfo.apkSize
            var downloadedBytes = 0L

            val inputStream: InputStream = connection.inputStream
            val tempFile = File(updatesDir, "${releaseInfo.tagName}_${releaseInfo.apkFileName}.tmp")
            val outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(32768) // 32KB high throughput buffer
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                val percent = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                onProgress(percent, downloadedBytes, totalBytes)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Verify downloaded temp file before activating
            val integrity = verifyApkIntegrity(tempFile, releaseInfo.sha256Checksum)
            if (integrity.isFailure) {
                tempFile.delete()
                return@withContext Result.failure(integrity.exceptionOrNull() ?: Exception("APK integrity validation failed"))
            }

            // Atomically rename temp file to completed target file
            if (outputFile.exists()) outputFile.delete()
            tempFile.renameTo(outputFile)

            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Calculates the streaming SHA-256 checksum of an APK file on disk.
     */
    fun calculateFileSha256(file: File): String {
        return try {
            if (!file.exists() || !file.isFile) return ""
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(65536) // 64KB digest buffer
            file.inputStream().use { input ->
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Performs multi-point APK validation:
     * 1. Minimum file size threshold
     * 2. SHA-256 checksum match (if provided)
     * 3. Package archive header structure & package name verification
     */
    fun verifyApkIntegrity(apkFile: File, expectedSha256: String? = null): Result<Unit> {
        if (!apkFile.exists()) {
            return Result.failure(Exception("APK file does not exist."))
        }
        if (apkFile.length() < 500_000) {
            return Result.failure(Exception("APK file is incomplete or corrupted."))
        }

        // 1. SHA-256 Checksum Validation (if specified)
        if (!expectedSha256.isNullOrBlank()) {
            val calculated = calculateFileSha256(apkFile)
            if (!calculated.equals(expectedSha256.trim(), ignoreCase = true)) {
                return Result.failure(Exception("SHA-256 checksum mismatch. The downloaded file may be tampered with or incomplete."))
            }
        }

        // 2. Package Archive Integrity & Package Name Check
        try {
            val pm = context.packageManager
            val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
                ?: return Result.failure(Exception("Invalid APK format: Android Package Manager could not parse archive header."))

            if (archiveInfo.packageName != context.packageName) {
                return Result.failure(Exception("Package identity mismatch! Expected: ${context.packageName}, Received: ${archiveInfo.packageName}"))
            }
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to verify package structure: ${e.localizedMessage}"))
        }

        return Result.success(Unit)
    }

    /**
     * Triggers the Android package installer to install the downloaded APK.
     * Safe installation through FileProvider after security validation.
     */
    fun installApk(apkFile: File, expectedSha256: String? = null): Result<Unit> {
        return try {
            val verifyRes = verifyApkIntegrity(apkFile, expectedSha256)
            if (verifyRes.isFailure) {
                return verifyRes
            }

            // Android 8.0+ unknown sources permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return Result.failure(Exception("Please grant permission to install unknown apps, then try again."))
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Compares semantic versions (e.g. "1.3" > "1.1", "1.0.1" > "1.0.0").
     */
    fun isVersionNewer(latestVer: String, currentVer: String): Boolean {
        try {
            val latestParts = latestVer.split('.').mapNotNull { it.toIntOrNull() }
            val currentParts = currentVer.split('.').mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        } catch (_: Exception) {
            return latestVer != currentVer
        }
    }
}
