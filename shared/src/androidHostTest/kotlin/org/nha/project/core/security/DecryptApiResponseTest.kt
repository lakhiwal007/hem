package org.nha.project.core.security

import kotlinx.coroutines.runBlocking
import org.nha.project.core.secrets.AppSecrets
import java.io.File
import kotlin.test.Test

class DecryptApiResponseTest {
    @Test
    fun decrypt(): Unit =
        runBlocking {
            val encrypted = "/GeAPbseNCBGaePvrw5j/zSiplQgM0kgTkqnkaFOPd2YiXIyWt9v5OF30KtfTLZg"

            val viaIdamKey = runCatching { IdamCrypto.decrypt(AppSecrets.IDAM_KEY, encrypted) }
            val viaIdamKey2 = runCatching { IdamCrypto.decrypt(AppSecrets.IDAM_KEY2, encrypted) }
            val viaSessionKey1 = runCatching { SessionCrypto.decrypt(AppSecrets.IDAM_KEY, encrypted) }
            val viaSessionKey2 = runCatching { SessionCrypto.decrypt(AppSecrets.IDAM_KEY2, encrypted) }
            val viaLogoutSessionKey =
                runCatching { SessionCrypto.decrypt(AppSecrets.LOGOUT_SESSION_KEY, encrypted) }

            val report =
                buildString {
                    appendLine("IdamCrypto + IDAM_KEY:")
                    appendLine(viaIdamKey.getOrElse { "failed: ${it.message}" })
                    appendLine()
                    appendLine("IdamCrypto + IDAM_KEY2:")
                    appendLine(viaIdamKey2.getOrElse { "failed: ${it.message}" })
                    appendLine()
                    appendLine("SessionCrypto + IDAM_KEY:")
                    appendLine(viaSessionKey1.getOrElse { "failed: ${it.message}" })
                    appendLine()
                    appendLine("SessionCrypto + IDAM_KEY2:")
                    appendLine(viaSessionKey2.getOrElse { "failed: ${it.message}" })
                    appendLine()
                    appendLine("SessionCrypto + LOGOUT_SESSION_KEY:")
                    appendLine(viaLogoutSessionKey.getOrElse { "failed: ${it.message}" })
                }

            val outputFile = File("build/decrypted-output.txt")
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(report)

            error("Decrypted output written to ${outputFile.absolutePath}")
        }
}
