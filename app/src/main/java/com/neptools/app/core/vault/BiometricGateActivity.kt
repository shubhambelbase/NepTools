package com.neptools.app.core.vault

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

class BiometricGateActivity : FragmentActivity() {

    companion object {
        const val MODE_ENCRYPT = "encrypt"
        const val MODE_DECRYPT = "decrypt"
        const val EXTRA_MODE = "vault_gate_mode"
        const val EXTRA_PAYLOAD = "vault_gate_payload"

        const val RESULT_USER_CANCEL = Activity.RESULT_FIRST_USER

        @Volatile
        var lastSealedWrap: ByteArray? = null

        fun createIntent(context: android.content.Context, mode: String, payload: ByteArray?): Intent =
            Intent(context, BiometricGateActivity::class.java)
                .putExtra(EXTRA_MODE, mode)
                .putExtra(EXTRA_PAYLOAD, payload)

        fun canUseBiometrics(context: android.content.Context): Boolean {
            val bm = BiometricManager.from(context)
            val result = if (android.os.Build.VERSION.SDK_INT >= 30) {
                bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            } else {
                @Suppress("DEPRECATION")
                bm.canAuthenticate()
            }
            return result == BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra(EXTRA_MODE)
        val data = intent.getByteArrayExtra(EXTRA_PAYLOAD)

        if (mode == null || data == null || data.size < 12) {
            finishWith(Activity.RESULT_CANCELED)
            return
        }

        try {
            val cipher: Cipher = when (mode) {
                MODE_ENCRYPT -> VaultCrypto.newBioEncryptCipher()
                MODE_DECRYPT -> VaultCrypto.newBioDecryptCipher(data.copyOfRange(0, 12))
                else -> { finishWith(Activity.RESULT_CANCELED); return }
            }
            val prompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        try {
                            val outCipher = result.cryptoObject?.cipher
                            when (mode) {
                                MODE_ENCRYPT -> {
                                    val ct = outCipher?.doFinal(data)
                                    val iv = outCipher?.iv
                                    if (ct != null && iv != null && iv.size == 12) {
                                        val sealed = ByteArray(iv.size + ct.size)
                                        System.arraycopy(iv, 0, sealed, 0, iv.size)
                                        System.arraycopy(ct, 0, sealed, iv.size, ct.size)
                                        lastSealedWrap = sealed
                                        finishWith(Activity.RESULT_OK)
                                    } else {
                                        finishWith(Activity.RESULT_CANCELED)
                                    }
                                }
                                else -> {
                                    val keyBytes = outCipher?.doFinal(data.copyOfRange(12, data.size))
                                    if (keyBytes != null) {
                                        VaultStore.Session.dataKey = VaultCrypto.keyFromBytes(keyBytes)
                                        finishWith(Activity.RESULT_OK)
                                    } else {
                                        finishWith(Activity.RESULT_CANCELED)
                                    }
                                }
                            }
                        } catch (_: Throwable) {
                            finishWith(Activity.RESULT_CANCELED)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) {
                            finishWith(RESULT_USER_CANCEL)
                        } else {
                            finishWith(Activity.RESULT_CANCELED)
                        }
                    }
                }
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(if (mode == MODE_ENCRYPT) "Enable fingerprint unlock" else "Unlock Password Vault")
                .setNegativeButtonText("Cancel")
                .build()
            prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
        } catch (t: Throwable) {
            if (mode == MODE_DECRYPT && isPermanentInvalidation(t)) {
                VaultStore.get(applicationContext).disableBiometric()
            }
            finishWith(Activity.RESULT_CANCELED)
        }
    }

    private fun isPermanentInvalidation(t: Throwable): Boolean {
        var cur: Throwable? = t
        var depth = 0
        while (cur != null && depth < 6) {
            if (cur is android.security.keystore.KeyPermanentlyInvalidatedException) return true
            cur = cur.cause
            depth++
        }
        return false
    }

    private fun finishWith(code: Int) {
        setResult(code)
        finish()
    }
}
