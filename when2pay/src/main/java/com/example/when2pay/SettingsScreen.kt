package com.example.when2pay

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.core.content.ContextCompat.startActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                val intent = Intent(navController.context, HCEActivity::class.java)
                intent.putExtra("data", "0x7A8E79dE63c29c3ee2375Cd3D2e90FEaA5aAf322") // TODO: put here the address to be verified
                navController.context.startActivity(intent)
            }) {
                Text("Get my KYC")
            }

            Button(onClick = {
                val intent = Intent(navController.context, NFCReaderActivity::class.java)
                navController.context.startActivity(intent)
            }) {
                Text("Verify somebody")
            }

            Button(onClick = { /* Implement backup functionality */ }) {
                Text("Backup Wallet")
            }

            Button(onClick = { /* Implement restore functionality */ }) {
                Text("Restore Wallet")
            }

            Button(onClick = { /* Implement change password functionality */ }) {
                Text("Change Password")
            }

            Button(onClick = {  }) {
                Text("Logout")
            }
        }
    }
}