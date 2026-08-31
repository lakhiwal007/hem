package org.nha.project.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject

private class LocationAuthorizationDelegate(
    private val onResult: (Boolean) -> Unit,
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        val granted =
            status == platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways ||
                status == platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
        onResult(granted)
    }
}

@Composable
actual fun rememberRequestLocationPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val manager = remember { CLLocationManager() }
    val delegate = remember { LocationAuthorizationDelegate(onResult) }
    manager.delegate = delegate

    return {
        val status = manager.authorizationStatus
        val permanentlyDenied = status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted
        if (permanentlyDenied) {
            val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
            if (settingsUrl != null) {
                UIApplication.sharedApplication.openURL(settingsUrl)
            }
        } else {
            manager.requestWhenInUseAuthorization()
        }
    }
}
