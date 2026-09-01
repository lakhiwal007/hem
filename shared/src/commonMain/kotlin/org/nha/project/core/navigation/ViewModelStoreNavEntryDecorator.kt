package org.nha.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * Gives each distinct [androidx.navigation3.runtime.NavEntry.contentKey] its own [ViewModelStore],
 * cleared when that entry is popped off the backstack for good.
 *
 * Without this, [NavDisplay]'s default decorators leave [LocalViewModelStoreOwner] pointing at
 * whatever's above the nav graph (the Activity), so every screen of the same route type shares one
 * ViewModelStore - a Koin/AndroidX ViewModel already created for one instance of a route (e.g. one
 * hospital) gets silently reused for the next instance (a different hospital) instead of being
 * recreated with its new constructor parameters.
 */
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
