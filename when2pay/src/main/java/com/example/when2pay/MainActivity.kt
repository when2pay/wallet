package com.example.when2pay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger
import org.web3j.crypto.RawTransaction
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.http.HttpService
import java.util.concurrent.CompletableFuture

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
        // Define login parameters

        // Initialize Web3Auth session
        web3Auth.setResultUrl(intent?.data)
        val sessionResponse = web3Auth.initialize()
        sessionResponse.whenComplete { _, error ->
            if (error == null) {
                val privateKey = web3Auth.getPrivkey()
                Log.d(TAG, "Web3Auth Private Key: $privateKey")
                credentials = Credentials.create(privateKey)
                this.web3 = Web3j.build(HttpService(rpcUrl))
                setContent {
                    WalletTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            WalletNavigation(
                                onSendTransaction = { amount, recipient -> sendTransaction(amount, recipient) },
                                onSignIn = { signIn() },
                                isLoggedIn = true
                            )
                        }
                    }
                }
            } else {
                Log.e(TAG, "Web3Auth Error: ${error.message}")
            }
        }

        // Set content for Jetpack Compose
        setContent {
            WalletTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WalletNavigation(
                        onSendTransaction = { amount, recipient -> sendTransaction(amount, recipient) },
                        onSignIn = {signIn()},
                        isLoggedIn = true
                    )
                }
            }
        }
    }

    private fun signIn() {
        val email = "shahryarbahmeie@gmail.com"
        // IMP START - Login
        val selectedLoginProvider = Provider.EMAIL_PASSWORDLESS   // Can be GOOGLE, FACEBOOK, TWITCH etc.

        val loginParams = LoginParams(selectedLoginProvider, extraLoginOptions = ExtraLoginOptions(login_hint = email))
        val loginCompletableFuture: CompletableFuture<Web3AuthResponse> =
            web3Auth.login(loginParams)
        // IMP END - Login

        loginCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                // Set the sessionId from Web3Auth in App State
                // This will be used when making blockchain calls with Web3j
                Log.d("MainActivity_Web3Auth", "Login Success")
                credentials = Credentials.create(web3Auth.getPrivkey())
                web3 = Web3j.build(HttpService(rpcUrl))
                recreate()
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
            }
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