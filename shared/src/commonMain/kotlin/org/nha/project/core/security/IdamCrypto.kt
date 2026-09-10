package org.nha.project.core.security

import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.PBKDF2
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val KEY_SIZE_BYTES = 32
private const val IV_SIZE_BYTES = 16
private const val ITERATION_COUNT = 1989

@OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
object IdamCrypto {
    private val provider = CryptographyProvider.Default

    suspend fun encrypt(
        passphrase: String,
        plainText: String,
    ): String {
        val salt = CryptographyRandom.nextBytes(KEY_SIZE_BYTES)
        val iv = CryptographyRandom.nextBytes(IV_SIZE_BYTES)
        val key = deriveKey(passphrase, salt)
        val cipherText = cipherFor(key).encryptWithIv(iv, plainText.encodeToByteArray())
        return salt.toHex() + iv.toHex() + Base64.encode(cipherText)
    }

    suspend fun decrypt(
        passphrase: String,
        cipherText: String,
    ): String {
        val saltHexLen = KEY_SIZE_BYTES * 2
        val ivHexLen = IV_SIZE_BYTES * 2
        val saltHex = cipherText.substring(0, saltHexLen)
        val ivHex = cipherText.substring(saltHexLen, saltHexLen + ivHexLen)
        val encrypted = cipherText.substring(saltHexLen + ivHexLen)
        val key = deriveKey(passphrase, saltHex.hexToByteArray())
        val plain = cipherFor(key).decryptWithIv(ivHex.hexToByteArray(), Base64.decode(encrypted))
        return plain.decodeToString()
    }

    private suspend fun deriveKey(
        passphrase: String,
        salt: ByteArray,
    ): ByteArray {
        val pbkdf2 = provider.get(PBKDF2)
        val derivation =
            pbkdf2.secretDerivation(
                digest = SHA1,
                iterations = ITERATION_COUNT,
                outputSize = (KEY_SIZE_BYTES * 8).bits,
                salt = salt,
            )
        return derivation.deriveSecretToByteArray(passphrase.encodeToByteArray())
    }

    private suspend fun cipherFor(keyBytes: ByteArray) =
        provider
            .get(AES.CBC)
            .keyDecoder()
            .decodeFromByteArray(AES.Key.Format.RAW, keyBytes)
            .cipher()
}
