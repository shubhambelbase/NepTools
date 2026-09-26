package com.neptools.app.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neptools.app.core.data.RatesRepo

class RatesSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val result = fetchSync(applicationContext)
            if (result != null) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun fetchSync(context: Context): Any? {
        var conn: java.net.HttpURLConnection? = null
        return try {
            conn = (java.net.URL("https://open.er-api.com/v6/latest/NPR").openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 8000; readTimeout = 8000; requestMethod = "GET"
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = org.json.JSONObject(body)
            val ratesJson = root.getJSONObject("rates")
            val map = HashMap<String, Double>()
            val keys = ratesJson.keys()
            while (keys.hasNext()) { val k = keys.next(); map[k] = ratesJson.getDouble(k) }
            val now = System.currentTimeMillis()
            // Merge into whatever is already cached so a background sync never drops the NRB
            // detail rows, effective date or source label that only the in-app fetch writes.
            val target = java.io.File(context.filesDir, "rates_cache.json")
            val merged = if (target.exists()) {
                runCatching { org.json.JSONObject(target.readText()) }.getOrNull()
            } else {
                null
            } ?: org.json.JSONObject()
            merged.put("fetchedAt", now)
            merged.put("rates", org.json.JSONObject(map))
            com.neptools.app.core.util.SafeFileWriter.writeAtomic(target, merged.toString())
            map
        } catch (e: Exception) { null } finally { conn?.disconnect() }
    }
}
