package org.nha.project.core.ui.toast

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING,
}

data class ToastMessage(
    val id: Long,
    val text: String,
    val type: ToastType,
    val durationMillis: Long,
)

class ToastController {
    private val _toasts = MutableStateFlow<List<ToastMessage>>(emptyList())
    val toasts: StateFlow<List<ToastMessage>> = _toasts.asStateFlow()

    private var nextId = 0L

    fun success(
        text: String,
        durationMillis: Long = 3000L,
    ) = show(text, ToastType.SUCCESS, durationMillis)

    fun error(
        text: String,
        durationMillis: Long = 4000L,
    ) = show(text, ToastType.ERROR, durationMillis)

    fun info(
        text: String,
        durationMillis: Long = 3000L,
    ) = show(text, ToastType.INFO, durationMillis)

    fun warning(
        text: String,
        durationMillis: Long = 3500L,
    ) = show(text, ToastType.WARNING, durationMillis)

    private fun show(
        text: String,
        type: ToastType,
        durationMillis: Long,
    ) {
        val message = ToastMessage(id = nextId++, text = text, type = type, durationMillis = durationMillis)
        _toasts.update { it + message }
    }

    fun dismiss(id: Long) {
        _toasts.update { list -> list.filterNot { it.id == id } }
    }
}
