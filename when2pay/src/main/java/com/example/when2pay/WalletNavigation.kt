package com.example.when2pay

import ENSScreen
import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
@Composable
fun WalletNavigation(
    onSendTransaction: (Double, String) -> Unit,
    onSignIn: () -> Unit,
    isLoggedIn: Boolean,
    sharedData: SharedViewModel
) {
    val navController = rememberNavController()

    // Check if user is logged in and decide the start destination
    val startDestination = if (isLoggedIn) "wallet" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onSignIn = onSignIn)
        }
        composable("wallet") {
            WalletScreen(navController, sharedData)
        }
        composable("send") {
            SendScreen(navController, onSendTransaction)
        }
        composable("receive") {
            ReceiveScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("ENS") {
            ENSScreen(navController)
        }
    }
}

