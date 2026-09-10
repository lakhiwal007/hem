package org.nha.project.core.network

import org.nha.project.core.ui.toast.ToastController

private const val SESSION_EXPIRED_MESSAGE = "Your session has expired. Please logout and login again."

class SessionExpiryNotifier(
    private val toastController: ToastController,
) {
    private var lastNotifiedToken: String? = null

    fun notifyUnauthorized(sessionToken: String?) {
        if (sessionToken == null || sessionToken == lastNotifiedToken) return
        lastNotifiedToken = sessionToken
        toastController.error(SESSION_EXPIRED_MESSAGE, durationMillis = 6000L)
    }
}
