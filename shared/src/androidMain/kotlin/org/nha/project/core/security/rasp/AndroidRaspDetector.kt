package org.nha.project.core.security.rasp

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Debug
import android.provider.Settings
import java.io.File
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

private val ROOT_BINARY_PATHS =
    listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/su",
        "/system/bin/.ext/.su",
        "/system/usr/we-need-root/su-backup",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/su/bin/su",
    )

private val ROOT_PACKAGE_NAMES =
    listOf(
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
    )

private val HOOKING_PACKAGE_NAMES =
    listOf(
        "de.robv.android.xposed.installer",
        "com.saurik.substrate",
    )

private val HOOKING_LIBRARY_NAMES =
    listOf("frida", "xposed", "substrate", "cydiasubstrate", "adbi", "epic", "whale")

private const val FRIDA_DEFAULT_PORT = 27042

class AndroidRaspDetector(
    private val context: Context,
) : RaspDetector {
    override fun isDebuggerAttached(): Boolean = Debug.isDebuggerConnected() || Debug.waitingForDebugger()

    override fun isDebugBuild(): Boolean {
        val isDebuggableFlag = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val isAdbEnabled =
            runCatching {
                Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
            }.getOrDefault(false)
        return isDebuggableFlag || isAdbEnabled
    }

    override fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()

        return fingerprint.startsWith("generic") ||
            fingerprint.startsWith("unknown") ||
            model.contains("google_sdk") ||
            model.contains("emulator") ||
            model.contains("android sdk built for") ||
            manufacturer.contains("genymotion") ||
            (brand.startsWith("generic") && product.startsWith("sdk")) ||
            product.contains("sdk_google") ||
            product.contains("sdk_x86") ||
            product.contains("vbox86p") ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu") ||
            hardware.contains("vbox86")
    }

    override fun isHookingDetected(): Boolean {
        val hasSuspiciousMapsEntry =
            runCatching {
                File("/proc/self/maps").readLines().any { line ->
                    HOOKING_LIBRARY_NAMES.any { line.contains(it, ignoreCase = true) }
                }
            }.getOrDefault(false)

        val hasFridaPortOpen =
            runCatching {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", FRIDA_DEFAULT_PORT), 200)
                    true
                }
            }.getOrDefault(false)

        val hasHookingPackage = HOOKING_PACKAGE_NAMES.any { isPackageInstalled(it) }

        return hasSuspiciousMapsEntry || hasFridaPortOpen || hasHookingPackage
    }

    override fun isDeviceRootedOrJailbroken(): Boolean {
        val hasSuBinary = ROOT_BINARY_PATHS.any { File(it).exists() }
        val hasTestKeys = Build.TAGS?.contains("test-keys") == true
        val hasRootPackage = ROOT_PACKAGE_NAMES.any { isPackageInstalled(it) }
        val canResolveSuOnPath =
            runCatching {
                Runtime
                    .getRuntime()
                    .exec(arrayOf("which", "su"))
                    .inputStream
                    .bufferedReader()
                    .readLine() != null
            }.getOrDefault(false)

        return hasSuBinary || hasTestKeys || hasRootPackage || canResolveSuOnPath
    }

    override fun isRunningUnderInstrumentation(): Boolean {
        val hasEspresso = isClassPresent("androidx.test.espresso.Espresso")
        val hasInstrumentationRegistry = isClassPresent("androidx.test.platform.app.InstrumentationRegistry")
        return hasEspresso || hasInstrumentationRegistry
    }

    override fun isVpnActive(): Boolean {
        val viaConnectivityManager =
            runCatching {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                        ?: return@runCatching false
                val network = connectivityManager.activeNetwork ?: return@runCatching false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@runCatching false
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            }.getOrDefault(false)

        val viaNetworkInterfaces =
            runCatching {
                Collections.list(NetworkInterface.getNetworkInterfaces()).any { networkInterface ->
                    networkInterface.isUp &&
                        (networkInterface.name.startsWith("tun") || networkInterface.name.startsWith("ppp"))
                }
            }.getOrDefault(false)

        return viaConnectivityManager || viaNetworkInterfaces
    }

    private fun isPackageInstalled(packageName: String): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)

    private fun isClassPresent(className: String): Boolean =
        runCatching {
            Class.forName(className)
            true
        }.getOrDefault(false)
}
