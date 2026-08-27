package org.nha.project.core.location

import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosLocationAccessChecker : LocationAccessChecker {
    private val manager = CLLocationManager()

    override fun currentStatus(): LocationAccessStatus {
        val granted = isGranted(manager.authorizationStatus)
        val enabled = CLLocationManager.locationServicesEnabled()
        return LocationAccessStatus(permissionGranted = granted, servicesEnabled = enabled)
    }

    override fun openLocationSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    private fun isGranted(status: CLAuthorizationStatus): Boolean =
        status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse
}
