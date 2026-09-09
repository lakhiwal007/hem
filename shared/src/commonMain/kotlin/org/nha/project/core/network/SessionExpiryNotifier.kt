package org.nha.project.core.network

import org.nha.project.core.ui.toast.ToastController

private const val SESSION_EXPIRED_MESSAGE = "Your session has expired. Please logout and login again."

/**
 * Surfaces a single toast the first time a 401 is seen for a given session token, so a screen
 * that fires several API calls in parallel (or a background loop retrying repeatedly) doesn't
 * spam the user with the same message. A new session token (fresh login) allows it to fire again.
 */
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
