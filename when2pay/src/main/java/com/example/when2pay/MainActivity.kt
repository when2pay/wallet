package com.example.when2pay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.when2pay.ui.theme.WalletTheme

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // val action: String? = intent?.action
        try {
            val data: Uri? = intent?.data
            val dataToSend = data.toString().substring(14)
            Log.i(TAG, dataToSend)
            if (dataToSend.length > 0) {
                // Switch to the HCE activity and set the variable
                val intent = Intent(this, HCEActivity::class.java)
                intent.putExtra("data", dataToSend)
                startActivity(intent)
            }
        } catch (e: Exception) {}

        setContent {
            WalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Hello World")
    }
}