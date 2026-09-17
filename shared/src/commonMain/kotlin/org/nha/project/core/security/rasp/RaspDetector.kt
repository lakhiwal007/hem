package org.nha.project.core.security.rasp

enum class RaspThreatType {
    DEBUGGER,
    DEBUG_BUILD,
    EMULATOR,
    HOOKING,
    ROOT_OR_JAILBREAK,
    INSTRUMENTATION,
    VPN,
}

data class RaspCheckResult(
    val type: RaspThreatType,
    val detected: Boolean,
    val details: String? = null,
)

interface RaspDetector {
    fun isDebuggerAttached(): Boolean

    fun isDebugBuild(): Boolean

    fun isEmulator(): Boolean

    fun isHookingDetected(): Boolean

    fun isDeviceRootedOrJailbroken(): Boolean

    fun isRunningUnderInstrumentation(): Boolean

    fun isVpnActive(): Boolean

    fun runAllChecks(): List<RaspCheckResult> =
        listOf(
            RaspCheckResult(RaspThreatType.DEBUGGER, isDebuggerAttached()),
            RaspCheckResult(RaspThreatType.DEBUG_BUILD, isDebugBuild()),
            RaspCheckResult(RaspThreatType.EMULATOR, isEmulator()),
            RaspCheckResult(RaspThreatType.HOOKING, isHookingDetected()),
            RaspCheckResult(RaspThreatType.ROOT_OR_JAILBREAK, isDeviceRootedOrJailbroken()),
            RaspCheckResult(RaspThreatType.INSTRUMENTATION, isRunningUnderInstrumentation()),
            RaspCheckResult(RaspThreatType.VPN, isVpnActive()),
        )
}
