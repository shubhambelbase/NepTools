package com.neptools.app.core.util

import android.content.Context
import android.content.SharedPreferences
import com.neptools.app.core.data.EmergencyContact
import com.neptools.app.core.data.EmergencyRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Intelligent Remote API Sync Manager for Emergency Contacts.
 *
 * Capabilities:
 * 1. Synchronizes contacts with remote JSON API endpoint (GitHub raw CDN or custom backend).
 * 2. Caches validated contacts in emergency_contacts_cache.json for 100% offline access.
 * 3. Gracefully preserves hardcoded defaults as baseline safety net if offline or unreachable.
 * 4. Automatic background sync throttling (6 hours) with manual force-sync support.
 */
object EmergencySyncManager {

    const val DEFAULT_API_URL = "https://neptools-emergency-api.neptools.workers.dev/"
    const val FALLBACK_API_URL = "https://gist.githubusercontent.com/shubhambelbase/14f043af721a459deb2604572b44b401/raw/emergency_contacts.json"
    private const val CACHE_FILE = "emergency_contacts_cache.json"
    private const val PREFS_NAME = "neptools_emergency_sync_prefs"
    private const val KEY_LAST_SYNC = "last_sync_timestamp"
    private const val KEY_CUSTOM_URL = "custom_api_url"
    private const val KEY_DATA_VERSION = "data_version"
    private const val MIN_SYNC_INTERVAL_MS = 6L * 60 * 60 * 1000 // 6 hours

    /**
     * A remote feed must contain at least this many valid contacts before it is trusted enough
     * to be merged. The compiled-in hotlines remain authoritative regardless.
     */
    private const val MIN_REMOTE_CONTACTS = 10

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getApiUrl(context: Context): String {
        val custom = getPrefs(context).getString(KEY_CUSTOM_URL, "")?.trim()
        return if (!custom.isNullOrEmpty()) custom else DEFAULT_API_URL
    }

    fun setApiUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_CUSTOM_URL, url.trim()).apply()
    }

    /**
     * Initializes emergency contacts from disk cache on app cold-start.
     * Triggers background silent update check if cache is older than 24 hours.
     */
    fun init(context: Context) {
        val cached = loadCachedContacts(context)
        if (cached.isNotEmpty()) {
            // Merge, never replace: the compiled-in hotlines stay authoritative.
            EmergencyRepo.mergeRemoteContacts(cached)
        }

        val prefs = getPrefs(context)
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        _lastSyncTime.value = lastSync

        val age = System.currentTimeMillis() - lastSync
        if (age > 24L * 60 * 60 * 1000) {
            CoroutineScope(Dispatchers.IO).launch {
                sync(context, force = false)
            }
        }
    }

    fun loadCachedContacts(context: Context): List<EmergencyContact> {
        val file = File(context.filesDir, CACHE_FILE)
        if (!file.exists()) return emptyList()

        return try {
            parseContactsJson(file.readText())
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Only dialable numbers are accepted: digits with optional +, -, spaces, 3-20 characters. */
    private val NUMBER_PATTERN = Regex("^[0-9+\\-\\s]{3,20}$")

    /** Payload version from the API, used to reject rolled-back or stale feeds. */
    data class ParsedFeed(
        val version: Int,
        val contacts: List<EmergencyContact>
    )

    fun parseContactsJson(jsonStr: String): List<EmergencyContact> = parseFeed(jsonStr).contacts

    fun parseFeed(jsonStr: String): ParsedFeed {
        val root = JSONObject(jsonStr)
        val version = root.optInt("version", 0)
        val array = root.optJSONArray("contacts") ?: return ParsedFeed(version, emptyList())
        val list = ArrayList<EmergencyContact>()

        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val nameNp = obj.optString("nameNp", "").trim()
            val nameEn = obj.optString("nameEn", "").trim()
            val number = obj.optString("number", "").trim()
            val category = obj.optString("category", "security").trim()
            val province = obj.optString("province", "National").trim()
            val district = obj.optString("district", "All").trim()
            val descNp = obj.optString("descriptionNp", "").trim()
            val descEn = obj.optString("descriptionEn", "").trim()

            if (number.isEmpty() || (nameNp.isEmpty() && nameEn.isEmpty())) continue
            if (!NUMBER_PATTERN.matches(number)) continue
            if (number.count { it.isDigit() } < 3) continue
            if (category !in EmergencyRepo.categories.map { it.first }) continue

            list.add(
                EmergencyContact(
                    nameNp = nameNp,
                    nameEn = nameEn,
                    number = number,
                    category = category,
                    province = province,
                    district = district,
                    descriptionNp = descNp,
                    descriptionEn = descEn
                )
            )
        }
        return ParsedFeed(version, list)
    }

    /**
     * Executes remote HTTP API synchronization.
     */
    suspend fun sync(
        context: Context,
        force: Boolean = false,
        onComplete: ((success: Boolean, count: Int, message: String) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        val prefs = getPrefs(context)
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        val now = System.currentTimeMillis()

        if (!force && (now - lastSync) < MIN_SYNC_INTERVAL_MS) {
            val currentCount = EmergencyRepo.allContacts().size
            withContext(Dispatchers.Main) {
                onComplete?.invoke(true, currentCount, "Already up to date")
            }
            return@withContext
        }

        if (_isSyncing.value) return@withContext
        _isSyncing.value = true

        var lastError: String? = null
        try {
            val targetUrl = getApiUrl(context)
            var (responseCode, body) = try {
                fetchUrl(targetUrl, context, force, lastSync)
            } catch (e: Exception) {
                lastError = e.message
                if (targetUrl == DEFAULT_API_URL) {
                    try {
                        fetchUrl(FALLBACK_API_URL, context, force, lastSync)
                    } catch (fbEx: Exception) {
                        Pair(0, "")
                    }
                } else Pair(0, "")
            }

            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED && targetUrl == DEFAULT_API_URL) {
                try {
                    val fallbackRes = fetchUrl(FALLBACK_API_URL, context, force, lastSync)
                    if (fallbackRes.first == HttpURLConnection.HTTP_OK || fallbackRes.first == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        responseCode = fallbackRes.first
                        body = fallbackRes.second
                    }
                } catch (_: Exception) {}
            }

            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
                _lastSyncTime.value = now
                val currentCount = EmergencyRepo.allContacts().size
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true, currentCount, "Already up to date")
                }
                return@withContext
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false, 0, lastError ?: "HTTP error: $responseCode")
                }
                return@withContext
            }

            val feed = parseFeed(body)

            // Reject rolled-back feeds so an older payload cannot silently undo a newer one.
            val cachedVersion = prefs.getInt(KEY_DATA_VERSION, 0)
            if (feed.version in 1..<cachedVersion) {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false, 0, "Ignored stale contact feed (v${feed.version} < v$cachedVersion)")
                }
                return@withContext
            }

            if (feed.contacts.size >= MIN_REMOTE_CONTACTS) {
                val merge = EmergencyRepo.mergeRemoteContacts(feed.contacts)

                // Only persist payloads that passed validation and the merge.
                File(context.filesDir, CACHE_FILE).writeText(body)

                prefs.edit()
                    .putLong(KEY_LAST_SYNC, now)
                    .putInt(KEY_DATA_VERSION, feed.version)
                    .apply()
                _lastSyncTime.value = now

                withContext(Dispatchers.Main) {
                    onComplete?.invoke(
                        true,
                        merge.totals,
                        "${merge.added} new contact(s) added, ${merge.skippedCurated} verified entr(y/ies) kept"
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false, 0, "Invalid payload received")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onComplete?.invoke(false, 0, e.message ?: "Connection error")
            }
        } finally {
            _isSyncing.value = false
        }
    }

    private fun fetchUrl(targetUrl: String, context: Context, force: Boolean, lastSync: Long): Pair<Int, String> {
        val url = URL(targetUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "NepTools-Android/${context.packageName}")
            setRequestProperty("Accept", "application/json")
            if (!force && lastSync > 0L) {
                val httpDate = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).format(Date(lastSync))
                setRequestProperty("If-Modified-Since", httpDate)
            }
        }
        try {
            val code = conn.responseCode
            val body = if (code == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else ""
            return Pair(code, body)
        } finally {
            conn.disconnect()
        }
    }
}
