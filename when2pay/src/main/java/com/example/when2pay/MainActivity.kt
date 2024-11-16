package com.example.when2pay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.example.when2pay.ui.theme.WalletTheme
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.*
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger
import org.web3j.crypto.RawTransaction
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.http.HttpService
import java.util.concurrent.CompletableFuture

class SharedViewModel : ViewModel() {
    var walletPageTitle: String by mutableStateOf("My Wallet")
}

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private lateinit var web3Auth: Web3Auth

    private val gson = Gson()
    private lateinit var web3: Web3j
    private lateinit var credentials: Credentials
    private lateinit var loginParams: LoginParams
    private val rpcUrl = "https://rpc.ankr.com/eth_sepolia"
    val sharedViewModel: SharedViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Web3Auth
        web3 = Web3j.build(HttpService(rpcUrl))
        web3Auth = Web3Auth(
            Web3AuthOptions(
                context = this,
                clientId = getString(R.string.web3auth_project_id),
                network = Network.SAPPHIRE_DEVNET, // pass over the network you want to use (MAINNET or TESTNET or CYAN, AQUA, SAPPHIRE_MAINNET or SAPPHIRE_TESTNET)
                buildEnv = BuildEnv.PRODUCTION,
                redirectUrl = Uri.parse("com.example.when2pay://auth"),
                sessionTime = 172800
            )
        )
        // IMP END - Initialize Web3Auth

        // Handle user signing in when app is not alive
        web3Auth.setResultUrl(intent?.data)
        // Call initialize() in onCreate() to check for any existing session.
        val sessionResponse: CompletableFuture<Void> = web3Auth.initialize()
        sessionResponse.whenComplete { _, error ->
            if (error == null) {
                credentials = Credentials.create(web3Auth.getPrivkey())
                web3 = Web3j.build(HttpService(rpcUrl))
                setContent {
                    WalletTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            WalletNavigation(
                                onSendTransaction = { amount, recipient -> sendTransaction(amount, recipient) },
                                onSignIn = { signIn(it) },
                                isLoggedIn = web3Auth.getPrivkey().isNotEmpty()
                                sharedData = sharedViewModel
                            )
                        }
                    }
                }
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
                // Ideally, you should initiate the login function here.
            }
        }

        // Set content for Jetpack Compose
        setContent {
            WalletTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WalletNavigation(
                        onSendTransaction = { amount, recipient -> sendTransaction(amount, recipient) },
                        onSignIn = {signIn(it)},
                        isLoggedIn = web3Auth.getPrivkey().isNotEmpty()
                        sharedData = sharedViewModel
                    )
                }
            }
        }
    }

    private fun signIn(email: String): CompletableFuture<Unit> {
        val selectedLoginProvider = Provider.EMAIL_PASSWORDLESS   // Can be GOOGLE, FACEBOOK, TWITCH etc.

        val loginParams = LoginParams(selectedLoginProvider, extraLoginOptions = ExtraLoginOptions(login_hint = email), redirectUrl = Uri.parse("com.example.when2pay://auth"))

        return web3Auth.login(loginParams).thenApply { response ->
            Log.d(TAG, "Login Successful: ${gson.toJson(response)}")

            // Set the sessionId from Web3Auth in App State
            // This will be used when making blockchain calls with Web3j
            Log.d("MainActivity_Web3Auth", "Login Success")
            credentials = Credentials.create(web3Auth.getPrivkey())
            web3 = Web3j.build(HttpService(rpcUrl))
            recreate()
        }.exceptionally { error ->
            Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
        }
    }
    override fun onNewIntent(intent: Intent) {
        if (intent != null) {
            super.onNewIntent(intent)
        }

        Log.d(TAG, "onNewIntent called with intent: $intent")

        intent?.data?.let { uri ->
            Log.d(TAG, "Received URI in onNewIntent: $uri")
            web3Auth.setResultUrl(uri)
        }
    }
    override fun onResume() {
        super.onResume()
        if (Web3Auth.getCustomTabsClosed()) {
            Toast.makeText(this, "User closed the browser.", Toast.LENGTH_SHORT).show()
            web3Auth.setResultUrl(null)
            Web3Auth.setCustomTabsClosed(false)
        }
    }
    private fun sendTransaction(amount: Double, recipientAddress: String): String? {
        return try {
            Log.e("SendTransaction", "Tx Error: ${amount}: $recipientAddress")
            val nonce = web3.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST)
                .sendAsync().get().transactionCount
            val value = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger()
            val gasLimit = BigInteger.valueOf(21000)
            val chainId = web3.ethChainId().sendAsync().get().chainId

            val rawTransaction = RawTransaction.createTransaction(
                chainId.toLong(),
                nonce,
                gasLimit,
                recipientAddress,
                value,
                "",
                BigInteger.valueOf(5000000000), // Gas price
                BigInteger.valueOf(6000000000000) // Max priority fee per gas
            )

            val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)
            val ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get()

            if (ethSendTransaction.error != null) {
                Log.e("SendTransaction", "Tx Error: ${ethSendTransaction.error.message}")
                null
            } else {
                Log.d("SendTransaction", "Tx Hash: ${ethSendTransaction.transactionHash}")
                ethSendTransaction.transactionHash
            }
        } catch (e: Exception) {
            Log.e("SendTransaction", "Exception: ${e.message}")
            null
        }
    }
}