package com.example.when2pay

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun WalletNavigation(
    onSendTransaction: (Double, String) -> Unit,
    onSignIn: () -> Unit,
    isLoggedIn: () -> Boolean
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "wallet") {
        composable("wallet") { WalletScreen(navController) }
        composable("send") { SendScreen(navController,onSendTransaction) }
        composable("receive") { ReceiveScreen(navController) }
        composable("settings") { SettingsScreen(navController)}
    }
}