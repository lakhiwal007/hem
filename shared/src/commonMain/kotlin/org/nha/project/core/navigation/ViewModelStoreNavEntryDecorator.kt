package org.nha.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntryDecorator

@Composable
fun <T : Any> rememberViewModelStoreNavEntryDecorator(): NavEntryDecorator<T> {
    val owners = remember { mutableMapOf<Any, ViewModelStoreOwner>() }
    return remember {
        NavEntryDecorator(
            onPop = { contentKey -> owners.remove(contentKey)?.viewModelStore?.clear() },
        ) { entry ->
            val owner =
                owners.getOrPut(entry.contentKey) {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                entry.Content()
            }
        }
    }
}
