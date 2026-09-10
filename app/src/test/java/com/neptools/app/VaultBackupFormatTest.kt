package com.neptools.app

import com.neptools.app.core.vault.VaultCrypto
import com.neptools.app.core.vault.VaultStore
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class VaultBackupFormatTest {

    private fun sample(): VaultStore.BackupPayload {
        val salt = VaultCrypto.newSalt()
        val wrap1 = ByteArray(60) { (it * 3).toByte() }
        val blob = ByteArray(197) { (it * 11 + 5).toByte() }
        return VaultStore.BackupPayload(salt, 210_000, wrap1, blob)
    }

    @Test
    fun buildThenParseRoundTrips() {
        val p = sample()
        val bytes = VaultStore.buildBackupPayload(p.salt, p.iterations, p.wrap1, p.blob)
        val parsed = VaultStore.parseBackupPayload(bytes)
        assertNotNull(parsed)
        parsed!!
        assertArrayEquals(p.salt, parsed.salt)
        assertEquals(p.iterations, parsed.iterations)
        assertArrayEquals(p.wrap1, parsed.wrap1)
        assertArrayEquals(p.blob, parsed.blob)
    }

    @Test
    fun truncatedFileRejected() {
        val bytes = VaultStore.buildBackupPayload(
            VaultCrypto.newSalt(), 210_000, ByteArray(60), ByteArray(100)
        )
        for (cut in intArrayOf(1, 5, 20, 45, bytes.size - 1)) {
            assertNull("cut=$cut", VaultStore.parseBackupPayload(bytes.copyOf(cut)))
        }
    }

    @Test
    fun wrongMagicRejected() {
        val bytes = VaultStore.buildBackupPayload(
            VaultCrypto.newSalt(), 210_000, ByteArray(60), ByteArray(100)
        )
        val corrupted = bytes.copyOf()
        corrupted[3] = 'X'.code.toByte()
        assertNull(VaultStore.parseBackupPayload(corrupted))
        assertNull(VaultStore.parseBackupPayload(ByteArray(10)))
        assertNull(VaultStore.parseBackupPayload(ByteArray(0)))
    }

    @Test
    fun badVersionRejected() {
        val bytes = VaultStore.buildBackupPayload(
            VaultCrypto.newSalt(), 210_000, ByteArray(60), ByteArray(100)
        )
        bytes[8] = 9
        assertNull(VaultStore.parseBackupPayload(bytes))
    }

    @Test
    fun insaneIterationBoundsRejected() {
        assertNull(VaultStore.parseBackupPayload(
            VaultStore.buildBackupPayload(VaultCrypto.newSalt(), 100, ByteArray(60), ByteArray(50))
        ))
        assertNull(VaultStore.parseBackupPayload(
            VaultStore.buildBackupPayload(VaultCrypto.newSalt(), 5_000_000, ByteArray(60), ByteArray(50))
        ))
    }

    @Test
    fun trailingGarbageRejected() {
        val bytes = VaultStore.buildBackupPayload(
            VaultCrypto.newSalt(), 210_000, ByteArray(60), ByteArray(100)
        )
        val withExtra = bytes + byteArrayOf(1, 2, 3)
        assertNull(VaultStore.parseBackupPayload(withExtra))
    }

    @Test
    fun fullCryptoChainVerifyLikeImport() {
        val master = "Backup-Master-9!".toCharArray()
        val salt = VaultCrypto.newSalt()
        val iterations = 210_000
        val dataKeyBytes = VaultCrypto.newDataKey().encoded

        val kdf = VaultCrypto.deriveKeyWithIterations(master, salt, iterations)
        val enc = Cipher.getInstance("AES/GCM/NoPadding")
        enc.init(Cipher.ENCRYPT_MODE, kdf)
        val ct = enc.doFinal(dataKeyBytes)
        val wrap1 = enc.iv + ct

        val bytes = VaultStore.buildBackupPayload(salt, iterations, wrap1, ByteArray(50) { 7 })
        val parsed = VaultStore.parseBackupPayload(bytes)!!

        val decKey = VaultCrypto.deriveKeyWithIterations(master, parsed.salt, parsed.iterations)
        val dec = Cipher.getInstance("AES/GCM/NoPadding")
        dec.init(Cipher.DECRYPT_MODE, decKey, GCMParameterSpec(128, parsed.wrap1.copyOfRange(0, 12)))
        val unwrapped = dec.doFinal(parsed.wrap1.copyOfRange(12, parsed.wrap1.size))
        assertArrayEquals(dataKeyBytes, unwrapped)

        val badKey = VaultCrypto.deriveKeyWithIterations("nope".toCharArray(), parsed.salt, parsed.iterations)
        val dec2 = Cipher.getInstance("AES/GCM/NoPadding")
        dec2.init(Cipher.DECRYPT_MODE, badKey, GCMParameterSpec(128, parsed.wrap1.copyOfRange(0, 12)))
        var threw = false
        try {
            dec2.doFinal(parsed.wrap1.copyOfRange(12, parsed.wrap1.size))
        } catch (_: javax.crypto.AEADBadTagException) {
            threw = true
        }
        assertTrue(threw)
    }
}
