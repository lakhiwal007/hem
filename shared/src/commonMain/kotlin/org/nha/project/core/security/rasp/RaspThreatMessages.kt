package org.nha.project.core.security.rasp

data class RaspThreatMessage(
    val title: String,
    val instruction: String,
)

fun RaspThreatType.toMessage(): RaspThreatMessage =
    when (this) {
        RaspThreatType.DEBUGGER ->
            RaspThreatMessage(
                title = "Debugger attached",
                instruction = "Detach the debugger from this app to continue.",
            )
        RaspThreatType.DEBUG_BUILD ->
            RaspThreatMessage(
                title = "Developer mode detected",
                instruction = "Disable USB debugging / developer options, or install the official app build.",
            )
        RaspThreatType.EMULATOR ->
            RaspThreatMessage(
                title = "Emulator detected",
                instruction = "This app can only run on a physical device.",
            )
        RaspThreatType.HOOKING ->
            RaspThreatMessage(
                title = "Code injection tool detected",
                instruction = "Remove Frida, Xposed, or similar hooking tools to continue.",
            )
        RaspThreatType.ROOT_OR_JAILBREAK ->
            RaspThreatMessage(
                title = "Rooted / jailbroken device detected",
                instruction = "This app requires an unmodified device to continue.",
            )
        RaspThreatType.INSTRUMENTATION ->
            RaspThreatMessage(
                title = "Automated testing tool detected",
                instruction = "Close the automated testing / instrumentation framework to continue.",
            )
        RaspThreatType.VPN ->
            RaspThreatMessage(
                title = "VPN connection detected",
                instruction = "Disconnect your VPN to continue.",
            )
    }
