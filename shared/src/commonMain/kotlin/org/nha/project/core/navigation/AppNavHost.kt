package org.nha.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.nha.project.feature.auth.presentation.LoginScreen
import org.nha.project.feature.capture.presentation.CaptureScreen
import org.nha.project.feature.hospital.presentation.HospitalListScreen
import org.nha.project.feature.hospital.presentation.HospitalStatusScreen
import org.nha.project.feature.onboarding.presentation.OnboardingScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Onboarding) {
        composable<Route.Onboarding> {
            OnboardingScreen(onContinue = { navController.navigate(Route.Login) })
        }
        composable<Route.Login> {
            LoginScreen(onContinue = { navController.navigate(Route.HospitalList) })
        }
        composable<Route.HospitalList> {
            HospitalListScreen(onContinue = { navController.navigate(Route.Capture) })
        }
        composable<Route.Capture> {
            CaptureScreen(onContinue = { navController.navigate(Route.Status) })
        }
        composable<Route.Status> {
            HospitalStatusScreen()
        }
    }
}
