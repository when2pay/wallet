package com.example.when2pay

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.concurrent.CompletableFuture

@Composable
fun WalletNavigation(
    onSendTransaction: (Double, String) -> Unit,
    onSignIn: (email: String) -> CompletableFuture<Unit>,
    isLoggedIn: Boolean
) {
    val navController = rememberNavController()

    // Check if user is logged in and decide the start destination
    val startDestination = if (isLoggedIn) "wallet" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onSignIn)
        }
        composable("wallet") {
            WalletScreen(navController)
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
    }
}

