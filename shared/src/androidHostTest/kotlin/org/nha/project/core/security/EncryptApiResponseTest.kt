package org.nha.project.core.security

import kotlinx.coroutines.runBlocking
import org.nha.project.core.secrets.AppSecrets
import java.io.File
import kotlin.test.Test

class EncryptApiResponseTest {
    @Test
    fun encrypt(): Unit =
        runBlocking {
            val plainText = "{\n  \"userId\": \"USER14762\"\n}"

            val encrypted = IdamCrypto.encrypt(AppSecrets.TEST_1, plainText)

            val outputFile = File("build/encrypted-output.txt")
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(encrypted)

            error("Encrypted output: $encrypted")
        }
}
