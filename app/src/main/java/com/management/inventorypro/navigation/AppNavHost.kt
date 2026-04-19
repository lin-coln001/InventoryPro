package com.management.inventorypro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.management.inventorypro.ui.theme.screens.add.AddProductScreen
import com.management.inventorypro.ui.theme.screens.dashboard.DashboardScreen
import com.management.inventorypro.ui.theme.screens.login.LoginScreen

import com.management.inventorypro.ui.theme.screens.register.RegisterScreen
import com.management.inventorypro.ui.theme.screens.update.UpdateProductScreen
import com.management.inventorypro.ui.theme.screens.view.ViewInventoryScreen


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
        composable("add_product"){
            AddProductScreen(navController)
        }
        composable("view_inventory"){
            ViewInventoryScreen(navController)
        }
        composable(
            route = "update_product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            UpdateProductScreen(navController, productId)
        }

}}