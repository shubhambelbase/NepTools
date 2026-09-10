package com.neptools.app.core.updater

data class GitHubUpdaterConfig(
    val owner: String = "shubhambelbase",
    val repo: String = "NepTools",
    val token: String = "", // GitHub Personal Access Token (for private repo access)
    val autoCheckOnStartup: Boolean = true,
    val lastCheckedTimestamp: Long = 0L
)

data class GitHubReleaseInfo(
    val tagName: String,
    val releaseName: String,
    val releaseNotes: String,
    val publishedAt: String,
    val apkFileName: String,
    val apkDownloadUrl: String,
    val apkAssetId: Long,
    val apkSize: Long,
    val isNewerVersion: Boolean,
    val currentVersionName: String,
    val latestVersionName: String,
    val sha256Checksum: String = ""
)

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    object Checking : UpdateDownloadState()
    data class UpdateAvailable(val info: GitHubReleaseInfo) : UpdateDownloadState()
    object UpToDate : UpdateDownloadState()
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : UpdateDownloadState()
    data class DownloadComplete(val apkPath: String, val info: GitHubReleaseInfo) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}
