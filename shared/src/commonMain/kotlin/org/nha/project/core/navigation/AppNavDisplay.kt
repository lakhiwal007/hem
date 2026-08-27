package org.nha.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.nha.project.feature.auth.presentation.LoginScreen
import org.nha.project.feature.capture.presentation.CaptureScreen
import org.nha.project.feature.hospital.presentation.HospitalListScreen
import org.nha.project.feature.hospital.presentation.HospitalStatusScreen
import org.nha.project.feature.onboarding.presentation.OnboardingScreen
import org.nha.project.feature.permission.presentation.LocationPermissionScreen
import org.nha.project.feature.splash.presentation.SplashScreen

private val routeSavedStateConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(Route::class) {
                    subclass(Route.Splash::class, Route.Splash.serializer())
                    subclass(Route.LocationPermission::class, Route.LocationPermission.serializer())
                    subclass(Route.Onboarding::class, Route.Onboarding.serializer())
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
                    LocationPermissionScreen(onContinue = { backStack.add(Route.Onboarding) })
                }
                entry<Route.Onboarding> {
                    OnboardingScreen(onContinue = { backStack.add(Route.Login) })
                }
                entry<Route.Login> {
                    LoginScreen(onContinue = { backStack.add(Route.HospitalList) })
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
