package org.nha.project.core.di

import org.koin.dsl.module
import org.nha.project.core.location.LocationAccessObserver

val locationModule =
    module {
        single { LocationAccessObserver(get()) }
    }
