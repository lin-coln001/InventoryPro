package com.management.inventorypro.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.management.inventorypro.ui.theme.screens.add.AddProductScreen

import com.management.inventorypro.ui.theme.screens.dashboard.DashboardScreen
import com.management.inventorypro.ui.theme.screens.landing.OnboardingScreen

import com.management.inventorypro.ui.theme.screens.login.LoginScreen
import com.management.inventorypro.ui.theme.screens.profile.ProfileScreen

import com.management.inventorypro.ui.theme.screens.register.RegisterScreen
import com.management.inventorypro.ui.theme.screens.settings.SettingsScreen
import com.management.inventorypro.ui.theme.screens.tips.TipsScreen
import com.management.inventorypro.ui.theme.screens.update.UpdateProductScreen
import com.management.inventorypro.ui.theme.screens.view.ViewInventoryScreen


@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    // 1. Get the preference and user session
    val sharedPref = remember { context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE) }
    val shouldRemember = sharedPref.getBoolean("remember", false)
    val currentUser = auth.currentUser

    // 2. Determine the starting point
    // We determine this immediately to avoid screen flickering
    val startRoute = if (currentUser != null && shouldRemember) "dashboard" else "login"

    // 3. Side Effect: If we have a session but "Remember Me" is OFF, sign out in the background
    LaunchedEffect(Unit) {
        if (currentUser != null && !shouldRemember) {
            auth.signOut()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable("register") {
            RegisterScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("dashboard") {
            DashboardScreen(navController)
        }
        composable("add_product") {
            AddProductScreen(navController)
        }
        composable("view_inventory") {
            ViewInventoryScreen(navController)
        }
        composable(
            route = "update_product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            UpdateProductScreen(navController, productId)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("tips") {
            TipsScreen(navController)
        }
//        composable("landing") {
//            OnboardingScreen(navController)
//        }
        composable("profile") {
             ProfileScreen(navController)
        }
        composable("update_product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            UpdateProductScreen(navController, productId)
        }
    }

    }
