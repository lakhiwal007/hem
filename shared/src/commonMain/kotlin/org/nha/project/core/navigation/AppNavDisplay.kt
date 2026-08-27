package org.nha.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject
import org.nha.project.core.location.LocationGuard
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.presentation.LoginScreen
import org.nha.project.feature.capture.presentation.CaptureScreen
import org.nha.project.feature.hospital.presentation.HospitalListScreen
import org.nha.project.feature.hospital.presentation.HospitalStatusScreen
import org.nha.project.feature.permission.presentation.LocationPermissionScreen
import org.nha.project.feature.splash.presentation.SplashScreen

private val routeSavedStateConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Route.Splash::class, Route.Splash.serializer())
                    subclass(Route.LocationPermission::class, Route.LocationPermission.serializer())
                    subclass(Route.Login::class, Route.Login.serializer())
                    subclass(Route.HospitalList::class, Route.HospitalList.serializer())
                    subclass(Route.Capture::class, Route.Capture.serializer())
                    subclass(Route.Status::class, Route.Status.serializer())
                }
            }
    }

@Composable
fun AppNavDisplay() {
    val backStack = rememberNavBackStack(routeSavedStateConfig, Route.Splash)
    val sessionStorage = koinInject<SessionStorage>()
    val coroutineScope = rememberCoroutineScope()

    LocationGuard(currentRoute = backStack.lastOrNull() as? Route) {
        NavDisplay(
            backStack = backStack,
            entryProvider =
                entryProvider {
                    entry<Route.Splash> {
                        SplashScreen(
                            onTimeout = {
                                backStack.clear()
                                backStack.add(Route.LocationPermission)
                            },
                        )
                    }
                    entry<Route.LocationPermission> {
                        LocationPermissionScreen(
                            onContinue = {
                                coroutineScope.launch {
                                    val isLoggedIn = sessionStorage.isLoggedIn.first()
                                    backStack.clear()
                                    backStack.add(if (isLoggedIn) Route.HospitalList else Route.Login)
                                }
                            },
                        )
                    }
                    entry<Route.Login> {
                        LoginScreen(onLoginSuccess = { backStack.add(Route.HospitalList) })
                    }
                    entry<Route.HospitalList> {
                        HospitalListScreen(onContinue = { backStack.add(Route.Capture) })
                    }
                    entry<Route.Capture> {
                        CaptureScreen(onContinue = { backStack.add(Route.Status) })
                    }
                    entry<Route.Status> {
                        HospitalStatusScreen()
                    }
                },
        )
    }
}
