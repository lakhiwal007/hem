package org.nha.project.core.security.rasp

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.Foundation.NSBundle
import platform.Foundation.NSClassFromString
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.lastPathComponent
import platform.darwin.CTL_KERN
import platform.darwin.KERN_PROC
import platform.darwin.KERN_PROC_PID
import platform.darwin.P_TRACED
import platform.darwin.freeifaddrs
import platform.darwin.getifaddrs
import platform.darwin.ifaddrs
import platform.darwin.kinfo_proc
import platform.darwin.sysctl
import platform.posix.getpid

private val JAILBREAK_PATHS =
    listOf(
        "/Applications/Cydia.app",
        "/Library/MobileSubstrate/MobileSubstrate.dylib",
        "/bin/bash",
        "/usr/sbin/sshd",
        "/etc/apt",
        "/private/var/lib/apt",
        "/private/var/lib/cydia",
        "/private/var/stash",
        "/usr/libexec/cydia",
    )

private val VPN_INTERFACE_PREFIXES = listOf("tap", "tun", "ppp", "ipsec", "utun")

class IosRaspDetector : RaspDetector {
    @OptIn(ExperimentalForeignApi::class)
    override fun isDebuggerAttached(): Boolean =
        memScoped {
            val mib = allocArray<IntVar>(4)
            mib[0] = CTL_KERN
            mib[1] = KERN_PROC
            mib[2] = KERN_PROC_PID
            mib[3] = getpid()

            val info = alloc<kinfo_proc>()
            val size = alloc<ULongVar>()
            size.value = sizeOf<kinfo_proc>().convert()

            val result = sysctl(mib, 4u, info.ptr, size.ptr, null, 0u)
            if (result != 0) return@memScoped false

            (info.kp_proc.p_flag and P_TRACED) != 0
        }

    override fun isDebugBuild(): Boolean {
        val receiptUrl = NSBundle.mainBundle.appStoreReceiptURL
        val isSandboxReceipt = receiptUrl?.lastPathComponent == "sandboxReceipt"
        return isSandboxReceipt || isDebuggerAttached()
    }

    override fun isEmulator(): Boolean = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null

    override fun isHookingDetected(): Boolean {
        val insertedLibraries = NSProcessInfo.processInfo.environment["DYLD_INSERT_LIBRARIES"] as? String
        return !insertedLibraries.isNullOrBlank()
    }

    override fun isDeviceRootedOrJailbroken(): Boolean =
        JAILBREAK_PATHS.any { path -> NSFileManager.defaultManager.fileExistsAtPath(path) }

    @OptIn(BetaInteropApi::class)
    override fun isRunningUnderInstrumentation(): Boolean = NSClassFromString("XCTestCase") != null

    @OptIn(ExperimentalForeignApi::class)
    override fun isVpnActive(): Boolean =
        memScoped {
            val ifaddrPtr = alloc<CPointerVar<ifaddrs>>()
            if (getifaddrs(ifaddrPtr.ptr) != 0) return@memScoped false

            try {
                var current = ifaddrPtr.value
                while (current != null) {
                    val name =
                        current.pointed.ifa_name
                            ?.toKString()
                            .orEmpty()
                    if (VPN_INTERFACE_PREFIXES.any { name.startsWith(it) }) {
                        return@memScoped true
                    }
                    current = current.pointed.ifa_next
                }
                false
            } finally {
                freeifaddrs(ifaddrPtr.value)
            }
        }
}
