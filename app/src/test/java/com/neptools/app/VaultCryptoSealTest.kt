package com.neptools.app

import com.neptools.app.core.vault.VaultCrypto
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class VaultCryptoSealTest {

    @Test
    fun sealFormatIvPrefixedRoundTrip() {
        val salt = VaultCrypto.newSalt()
        val master = "Test123!".toCharArray()
        val kdfKey = VaultCrypto.deriveKey(master, salt)
        val dataKeyBytes = ByteArray(32) { i -> i.toByte() }

        val enc = Cipher.getInstance("AES/GCM/NoPadding")
        enc.init(Cipher.ENCRYPT_MODE, kdfKey)
        val ct = enc.doFinal(dataKeyBytes)
        val iv = enc.iv
        assertEquals(12, iv.size)

        val sealed = ByteArray(iv.size + ct.size)
        System.arraycopy(iv, 0, sealed, 0, iv.size)
        System.arraycopy(ct, 0, sealed, iv.size, ct.size)

        val dec = Cipher.getInstance("AES/GCM/NoPadding")
        dec.init(Cipher.DECRYPT_MODE, kdfKey, GCMParameterSpec(128, sealed.copyOfRange(0, 12)))
        val out = dec.doFinal(sealed.copyOfRange(12, sealed.size))

        assertArrayEquals(dataKeyBytes, out)
    }

    @Test
    fun wrongPasswordDerivedKeyCannotDecrypt() {
        val salt = VaultCrypto.newSalt()
        val dataKeyBytes = ByteArray(32) { i -> (i * 7).toByte() }
        val goodKey = VaultCrypto.deriveKey("Correct-Horse".toCharArray(), salt)

        val enc = Cipher.getInstance("AES/GCM/NoPadding")
        enc.init(Cipher.ENCRYPT_MODE, goodKey)
        val sealed = enc.iv + enc.doFinal(dataKeyBytes)

        val badKey = VaultCrypto.deriveKey("wrong".toCharArray(), salt)
        try {
            val d2 = Cipher.getInstance("AES/GCM/NoPadding")
            d2.init(Cipher.DECRYPT_MODE, badKey, GCMParameterSpec(128, sealed.copyOfRange(0, 12)))
            d2.doFinal(sealed.copyOfRange(12, sealed.size))
            throw AssertionError("Expected AEADBadTagException")
        } catch (expected: javax.crypto.AEADBadTagException) {
        }
    }

    @Test
    fun dataKeyRandomAndDistinct() {
        val a = VaultCrypto.newDataKey()
        val b = VaultCrypto.newDataKey()
        assertFalse(a.encoded.contentEquals(b.encoded))
        assertEquals(32, a.encoded.size)
    }
}
