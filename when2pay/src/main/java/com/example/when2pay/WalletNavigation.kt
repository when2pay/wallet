package com.example.when2pay

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.web3auth.core.Web3Auth

@Composable
fun WalletNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "wallet") {
        composable("wallet") { WalletScreen(navController) }
        composable("send") { SendScreen(navController) }
        composable("receive") { ReceiveScreen(navController) }
        composable("settings") { SettingsScreen(navController)}
    }
}