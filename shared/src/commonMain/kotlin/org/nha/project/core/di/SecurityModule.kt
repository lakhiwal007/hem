package org.nha.project.core.di

import org.koin.dsl.module
import org.nha.project.core.security.rasp.RaspThreatObserver

val securityModule =
    module {
        single { RaspThreatObserver(get()) }
    }
