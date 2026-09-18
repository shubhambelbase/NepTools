package com.neptools.app.core.vault

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.crypto.SecretKey

data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val url: String,
    val note: String,
    val createdMs: Long,
    val updatedMs: Long
)

class VaultStore private constructor(private val context: Context) {

    object Session {
        @Volatile
        var dataKey: SecretKey? = null

        val unlocked: Boolean get() = dataKey != null

        fun clear() { dataKey = null }
    }

    private val prefs = context.getSharedPreferences("vault_meta", Context.MODE_PRIVATE)
    private val dataFile: File get() = File(context.filesDir, "vault_data.bin")

    fun exists(): Boolean = prefs.contains(KEY_SALT) && dataFile.exists()

    fun biometricEnabled(): Boolean {
        if (!prefs.getBoolean(KEY_BIO_ENABLED, false) || !exists()) return false
        return bioWrap() != null
    }

    fun autoLockEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_LOCK, true)

    fun setAutoLockEnabled(v: Boolean) = prefs.edit().putBoolean(KEY_AUTO_LOCK, v).apply()

    fun createVault(masterPassword: CharArray): Boolean {
        if (exists()) return false
        val salt = VaultCrypto.newSalt()
        val dataKey = VaultCrypto.newDataKey()
        val derivedKey = VaultCrypto.deriveKey(masterPassword, salt)
        val wrap1 = VaultCrypto.encrypt(derivedKey, VaultCrypto.keyBytes(dataKey))
        prefs.edit()
            .putString(KEY_SALT, b64(salt))
            .putString(KEY_WRAP1, b64(wrap1))
            .putBoolean(KEY_BIO_ENABLED, false)
            .putBoolean(KEY_AUTO_LOCK, true)
            .apply()
        Session.dataKey = dataKey
        persistEntries(emptyList())
        return true
    }

    /**
     * Attempts to unlock the vault with the master password.
     *
     * Failed attempts are rate limited with exponential backoff so an attacker with physical
     * access to the device cannot brute force the master password at full speed.
     */
    fun unlockWithPassword(masterPassword: CharArray): Boolean {
        if (!exists()) return false
        if (isLockedOut()) return false

        val unlocked = try {
            val salt = unb64(prefs.getString(KEY_SALT, null)!!)
            val wrap1 = unb64(prefs.getString(KEY_WRAP1, null)!!)
            val derivedKey = VaultCrypto.deriveKey(masterPassword, salt)
            val dataKeyBytes = VaultCrypto.decrypt(derivedKey, wrap1)
            Session.dataKey = VaultCrypto.keyFromBytes(dataKeyBytes)
            true
        } catch (_: Throwable) {
            false
        }

        if (unlocked) resetFailedAttempts() else registerFailedAttempt()
        return unlocked
    }

    /** Milliseconds remaining before another password attempt is accepted. */
    fun lockoutRemainingMs(): Long =
        (prefs.getLong(KEY_LOCKOUT_UNTIL, 0L) - System.currentTimeMillis()).coerceAtLeast(0L)

    fun isLockedOut(): Boolean = lockoutRemainingMs() > 0L

    fun failedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

    private fun registerFailedAttempt() {
        val failures = failedAttempts() + 1
        val backoffMs = if (failures < FREE_ATTEMPTS) {
            0L
        } else {
            val steps = (failures - FREE_ATTEMPTS).coerceAtMost(6)
            (BASE_BACKOFF_MS shl steps).coerceAtMost(MAX_BACKOFF_MS)
        }
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, failures)
            .putLong(KEY_LOCKOUT_UNTIL, System.currentTimeMillis() + backoffMs)
            .apply()
    }

    private fun resetFailedAttempts() {
        if (failedAttempts() == 0 && prefs.getLong(KEY_LOCKOUT_UNTIL, 0L) == 0L) return
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    fun changeMasterPassword(oldPassword: CharArray, newPassword: CharArray): Boolean {
        if (!unlockWithPassword(oldPassword)) return false
        return try {
            val salt = VaultCrypto.newSalt()
            val derivedKey = VaultCrypto.deriveKey(newPassword, salt)
            val wrap1 = VaultCrypto.encrypt(derivedKey, VaultCrypto.keyBytes(Session.dataKey!!))
            prefs.edit()
                .putString(KEY_SALT, b64(salt))
                .putString(KEY_WRAP1, b64(wrap1))
                .apply()
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun bioWrap(): ByteArray? {
        return try {
            val w = prefs.getString(KEY_WRAP2, null)?.let(::unb64) ?: return null
            if (w.size != 60) {
                prefs.edit().remove(KEY_WRAP2).putBoolean(KEY_BIO_ENABLED, false).apply()
                null
            } else {
                w
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun bioWrapIv(): ByteArray? = try {
        val w = prefs.getString(KEY_WRAP2, null)?.let(::unb64) ?: return null
        w.copyOfRange(0, 12)
    } catch (_: Throwable) { null }

    fun saveBioWrap(sealedDataKey: ByteArray) {
        prefs.edit()
            .putString(KEY_WRAP2, b64(sealedDataKey))
            .putBoolean(KEY_BIO_ENABLED, true)
            .apply()
    }

    fun disableBiometric() {
        prefs.edit().remove(KEY_WRAP2).putBoolean(KEY_BIO_ENABLED, false).apply()
        VaultCrypto.deleteBioKey()
    }

    fun unlockWithBioResult(sealedCipherOutput: ByteArray): Boolean = try {
        Session.dataKey = VaultCrypto.keyFromBytes(sealedCipherOutput)
        loadEntriesRaw() != null
    } catch (_: Throwable) {
        Session.clear()
        false
    }

    fun confirmSessionUnlock(): Boolean = try {
        Session.unlocked && loadEntriesRaw() != null
    } catch (_: Throwable) {
        Session.clear()
        false
    }

    fun lock() = Session.clear()

    fun exportPayload(): ByteArray? {
        if (!Session.unlocked || !exists()) return null
        return try {
            val salt = unb64(prefs.getString(KEY_SALT, null)!!)
            val wrap1 = unb64(prefs.getString(KEY_WRAP1, null)!!)
            val blob = dataFile.readBytes()
            buildBackupPayload(salt, VaultCrypto.PBKDF2_ITERATIONS, wrap1, blob)
        } catch (_: Throwable) {
            null
        }
    }

    fun importPayload(bytes: ByteArray, backupMasterPassword: CharArray): Boolean {
        return try {
            val payload = parseBackupPayload(bytes) ?: return false
            val derivedKey = VaultCrypto.deriveKeyWithIterations(
                backupMasterPassword, payload.salt, payload.iterations
            )
            val dataKeyBytes = VaultCrypto.decrypt(derivedKey, payload.wrap1)
            val dataKey = VaultCrypto.keyFromBytes(dataKeyBytes)
            val json = String(VaultCrypto.decrypt(dataKey, payload.blob), Charsets.UTF_8)
            parseEntries(json)

            Session.clear()
            prefs.edit()
                .putString(KEY_SALT, b64(payload.salt))
                .putString(KEY_WRAP1, b64(payload.wrap1))
                .remove(KEY_WRAP2)
                .putBoolean(KEY_BIO_ENABLED, false)
                .remove(KEY_FAILED_ATTEMPTS)
                .remove(KEY_LOCKOUT_UNTIL)
                .apply()
            dataFile.writeBytes(payload.blob)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun deleteVault() {
        Session.clear()
        dataFile.delete()
        prefs.edit().clear().apply()
        VaultCrypto.deleteBioKey()
    }

    fun loadEntries(): List<VaultEntry> {
        checkUnlocked()
        val json = loadEntriesRaw() ?: return emptyList()
        return parseEntries(String(json))
    }

    fun persistEntries(entries: List<VaultEntry>) {
        checkUnlocked()
        val arr = JSONArray()
        for (e in entries) {
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("username", e.username)
                put("password", e.password)
                put("url", e.url)
                put("note", e.note)
                put("created", e.createdMs)
                put("updated", e.updatedMs)
            })
        }
        val sealed = VaultCrypto.encrypt(Session.dataKey!!, arr.toString().toByteArray(Charsets.UTF_8))
        val tmp = File(dataFile.absolutePath + ".tmp")
        tmp.writeBytes(sealed)
        if (!tmp.renameTo(dataFile)) {
            dataFile.writeBytes(sealed)
            tmp.delete()
        }
    }

    private fun loadEntriesRaw(): ByteArray? {
        if (!dataFile.exists()) return null
        return VaultCrypto.decrypt(Session.dataKey!!, dataFile.readBytes())
    }

    private fun checkUnlocked() {
        if (!Session.unlocked) throw IllegalStateException("Vault locked")
    }

    data class BackupPayload(
        val salt: ByteArray,
        val iterations: Int,
        val wrap1: ByteArray,
        val blob: ByteArray
    )

    companion object {
        private const val KEY_SALT = "salt"
        private const val KEY_WRAP1 = "wrap1"
        private const val KEY_WRAP2 = "wrap2"
        private const val KEY_BIO_ENABLED = "bio_enabled"
        private const val KEY_AUTO_LOCK = "auto_lock"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"

        /** Failed attempts allowed before backoff starts. */
        private const val FREE_ATTEMPTS = 5
        private const val BASE_BACKOFF_MS = 15_000L
        private const val MAX_BACKOFF_MS = 5 * 60_000L

        const val BACKUP_MAGIC = "NPTVAULT"
        const val BACKUP_VERSION = 1
        const val BACKUP_MIME = "application/octet-stream"
        const val BACKUP_EXT = "nptvault"

        fun buildBackupPayload(salt: ByteArray, iterations: Int, wrap1: ByteArray, blob: ByteArray): ByteArray {
            val out = java.io.ByteArrayOutputStream()
            out.write(BACKUP_MAGIC.toByteArray(Charsets.US_ASCII))
            out.write(BACKUP_VERSION)
            out.write(salt)
            writeInt32(out, iterations)
            writeInt16(out, wrap1.size)
            out.write(wrap1)
            writeInt32(out, blob.size)
            out.write(blob)
            return out.toByteArray()
        }

        fun parseBackupPayload(bytes: ByteArray): BackupPayload? {
            return try {
                val buf = java.io.ByteArrayInputStream(bytes)
                val magicBytes = BACKUP_MAGIC.toByteArray(Charsets.US_ASCII)
                val magic = ByteArray(magicBytes.size)
                if (buf.read(magic) != magic.size) return null
                if (!magic.contentEquals(magicBytes)) return null
                if (buf.read() != BACKUP_VERSION) return null
                val salt = ByteArray(32)
                if (buf.read(salt) != 32) return null
                val iterations = readInt32(buf)
                if (iterations < 50_000 || iterations > 2_000_000) return null
                val wrap1Len = readInt16(buf)
                if (wrap1Len <= 0 || wrap1Len > 512) return null
                val wrap1 = ByteArray(wrap1Len)
                if (buf.read(wrap1) != wrap1Len) return null
                val blobLen = readInt32(buf)
                if (blobLen <= 0 || blobLen > 16_000_000) return null
                val blob = ByteArray(blobLen)
                if (buf.read(blob) != blobLen) return null
                if (buf.read() != -1) return null
                BackupPayload(salt, iterations, wrap1, blob)
            } catch (_: Throwable) {
                null
            }
        }

        private fun writeInt32(out: java.io.ByteArrayOutputStream, v: Int) {
            out.write((v ushr 24) and 0xFF)
            out.write((v ushr 16) and 0xFF)
            out.write((v ushr 8) and 0xFF)
            out.write(v and 0xFF)
        }

        private fun writeInt16(out: java.io.ByteArrayOutputStream, v: Int) {
            out.write((v ushr 8) and 0xFF)
            out.write(v and 0xFF)
        }

        private fun readInt32(input: java.io.ByteArrayInputStream): Int {
            val b = ByteArray(4)
            if (input.read(b) != 4) return -1
            return ((b[0].toInt() and 0xFF) shl 24) or ((b[1].toInt() and 0xFF) shl 16) or
                ((b[2].toInt() and 0xFF) shl 8) or (b[3].toInt() and 0xFF)
        }

        private fun readInt16(input: java.io.ByteArrayInputStream): Int {
            val b = ByteArray(2)
            if (input.read(b) != 2) return -1
            return ((b[0].toInt() and 0xFF) shl 8) or (b[1].toInt() and 0xFF)
        }

        @Volatile
        private var instance: VaultStore? = null

        fun get(context: Context): VaultStore =
            instance ?: synchronized(this) {
                instance ?: VaultStore(context.applicationContext).also { instance = it }
            }

        fun parseEntries(json: String): List<VaultEntry> {
            val out = ArrayList<VaultEntry>()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    VaultEntry(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title"),
                        username = o.optString("username"),
                        password = o.optString("password"),
                        url = o.optString("url"),
                        note = o.optString("note"),
                        createdMs = o.optLong("created", System.currentTimeMillis()),
                        updatedMs = o.optLong("updated", System.currentTimeMillis())
                    )
                )
            }
            return out.sortedByDescending { it.updatedMs }
        }

        private fun b64(bytes: ByteArray): String =
            Base64.encodeToString(bytes, Base64.NO_WRAP)

        private fun unb64(s: String): ByteArray =
            Base64.decode(s, Base64.NO_WRAP)
    }
}
