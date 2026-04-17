package com.management.inventorypro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.management.inventorypro.ui.theme.screens.dashboard.DashboardScreen
import com.management.inventorypro.ui.theme.screens.login.LoginScreen
import com.management.inventorypro.ui.theme.screens.profile.ProfileScreen
import com.management.inventorypro.ui.theme.screens.register.RegisterScreen


@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "register"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("register"){RegisterScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("dashboard") {
            DashboardScreen(navController)

    }
        composable("profile"){
            ProfileScreen(navController)

        }
}}