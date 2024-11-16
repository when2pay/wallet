package com.example.when2pay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.when2pay.ui.theme.WalletTheme
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.*
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private lateinit var web3Auth: Web3Auth

    private val gson = Gson()
    private lateinit var web3: Web3j
    private lateinit var credentials: Credentials
    private lateinit var loginParams: LoginParams
    private val rpcUrl = "https://rpc.ankr.com/eth_sepolia"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    WalletNavigation()
                }
            }
        }
    }
}