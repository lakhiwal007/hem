package org.nha.project.core.security

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
object SessionCrypto {
    private val provider = CryptographyProvider.Default

    suspend fun encrypt(
        passphrase: String,
        data: String,
    ): String {
        val cipher = cipherFor(passphrase)
        return Base64.encode(cipher.encrypt(data.encodeToByteArray()))
    }

    suspend fun decrypt(
        passphrase: String,
        encryptedData: String,
    ): String {
        val cipher = cipherFor(passphrase)
        return cipher.decrypt(Base64.decode(encryptedData)).decodeToString()
    }

    private suspend fun cipherFor(passphrase: String) =
        provider
            .get(AES.ECB)
            .keyDecoder()
            .decodeFromByteArray(AES.Key.Format.RAW, sha256(passphrase))
            .cipher()

    private suspend fun sha256(input: String): ByteArray = provider.get(SHA256).hasher().hash(input.encodeToByteArray())
}
