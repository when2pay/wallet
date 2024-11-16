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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(navController: NavController, onSendTransaction: (Double, String) -> Unit) {
    var recipient by remember { mutableStateOf("0x7c00dC7574605bb50ada16E75CC797eC7f17B7FE") }
    var amount by remember { mutableStateOf("") }
    var selectedChain by remember { mutableStateOf(getSupportedChains().first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send") },
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
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text("Recipient Address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
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

            Button(
                onClick = { onSendTransaction(amount.toDouble(), recipient) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Send")
            }
            Button(
                onClick = { /* Implement send functionality */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Sqan QR Code")
            }
            Button(
                onClick = { /* Implement send functionality */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Sqan NFC Tag")
            }
        }
    }
}