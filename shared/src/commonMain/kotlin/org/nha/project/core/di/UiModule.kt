package org.nha.project.core.di

import org.koin.dsl.module
import org.nha.project.core.ui.toast.ToastController

val uiModule =
    module {
        single { ToastController() }
    }
