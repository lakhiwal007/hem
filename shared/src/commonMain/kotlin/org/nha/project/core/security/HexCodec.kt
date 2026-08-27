package org.nha.project.core.security

internal fun ByteArray.toHex(): String {
    val hexChars = "0123456789abcdef"
    val result = StringBuilder(size * 2)
    for (byte in this) {
        val i = byte.toInt() and 0xFF
        result.append(hexChars[i shr 4])
        result.append(hexChars[i and 0x0F])
    }
    return result.toString()
}

internal fun String.hexToByteArray(): ByteArray {
    val result = ByteArray(length / 2)
    for (i in indices step 2) {
        result[i / 2] = ((this[i].digitToInt(16) shl 4) + this[i + 1].digitToInt(16)).toByte()
    }
    return result
}
