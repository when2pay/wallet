package com.example.when2pay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.LoginParams
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(web3Auth: Web3Auth, onLoginSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    lateinit var loginParams: LoginParams
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to When2Pay",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    try {
                        val loginResponse = web3Auth.login(loginParams)
                        if (loginResponse != null) {
                            onLoginSuccess()
                        } else {
                            // Handle login failure
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        // Handle login error
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Logging in..." else "Login with Web3Auth")
        }
    }
}