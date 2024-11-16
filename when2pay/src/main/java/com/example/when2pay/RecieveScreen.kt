package com.example.when2pay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(navController: NavController) {
    var selectedChain by remember { mutableStateOf(getSupportedChains().first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receive") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Scan QR Code or Tap NFC", style = MaterialTheme.typography.headlineSmall)

            // Placeholder for QR code
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.LightGray)
            )

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = selectedChain.name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select Chain") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    getSupportedChains().forEach { chain ->
                        DropdownMenuItem(
                            text = { Text(chain.name) },
                            onClick = { selectedChain = chain }
                        )
                    }
                }
            }

            Text("Your ${selectedChain.name} Address:", style = MaterialTheme.typography.titleMedium)
            Text("0x1234...5678", style = MaterialTheme.typography.bodyLarge) // Replace with actual address

            Button(onClick = { /* Implement NFC receive functionality */ }) {
                Text("Enable NFC Receive")
            }
        }
    }
}