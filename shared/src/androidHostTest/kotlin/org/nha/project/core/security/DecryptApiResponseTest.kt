package org.nha.project.core.security

import kotlinx.coroutines.runBlocking
import org.nha.project.core.secrets.AppSecrets
import java.io.File
import kotlin.test.Test

/**
 * Scratch tool for inspecting an encrypted API payload captured during dev (e.g. via a proxy).
 * Paste the raw value below and run this single test - the decrypted plaintext (or each
 * scheme's failure reason) is written to shared/build/decrypted-output.txt.
 *
 * Run with:
 *   ./gradlew :shared:testAndroidHostTest --tests "*DecryptApiResponseTest*"
 *
 * IDAM_KEY  -> generateCaptcha/init/resendCaptcha captcha images, and the login validate flow
 * IDAM_KEY2 -> the /decrypt endpoint's post-login profile payload
 * Both are tried; whichever key doesn't apply to this value will just report its failure.
 */
class DecryptApiResponseTest {
    @Test
    fun decrypt(): Unit =
        runBlocking {
            val encrypted = "PASTE_ENCRYPTED_VALUE_HERE"

            val viaIdamKey = runCatching { IdamCrypto.decrypt(AppSecrets.IDAM_KEY, encrypted) }
            val viaIdamKey2 = runCatching { IdamCrypto.decrypt(AppSecrets.IDAM_KEY2, encrypted) }
            val viaSessionKey = runCatching { SessionCrypto.decrypt(AppSecrets.IDAM_KEY2, encrypted) }

            val report =
                buildString {
                    appendLine("IdamCrypto + IDAM_KEY:")
                    appendLine(viaIdamKey.getOrElse { "failed: ${it.message}" })
                    appendLine()
                    appendLine("IdamCrypto + IDAM_KEY2:")
                    appendLine(viaIdamKey2.getOrElse { "failed: ${it.message}" })
                    appendLine()
                    appendLine("SessionCrypto + IDAM_KEY2:")
                    appendLine(viaSessionKey.getOrElse { "failed: ${it.message}" })
                }

            val outputFile = File("build/decrypted-output.txt")
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(report)

            error("Decrypted output written to ${outputFile.absolutePath}")
        }
}
