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
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger
import org.web3j.crypto.Hash
import org.web3j.crypto.RawTransaction
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.crypto.TransactionEncoder

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
        web3Auth = Web3Auth(
            Web3AuthOptions(
                context = this,
                clientId = getString(R.string.web3auth_project_id), // Your Web3Auth Client ID
                network = Network.SAPPHIRE_MAINNET, // MAINNET/TESTNET/CYAN
                buildEnv = BuildEnv.PRODUCTION,
                redirectUrl = Uri.parse("com.example.when2pay://auth"),
                whiteLabel = WhiteLabelData(
                    "Web3Auth Integration",
                    null,
                    "https://cryptologos.cc/logos/ethereum-eth-logo.png",
                    "https://cryptologos.cc/logos/ethereum-eth-logo.png",
                    Language.EN,
                    ThemeModes.LIGHT,
                    true,
                    hashMapOf("primary" to "#eb5424")
                )
            )
        )

        // Define login parameters
        loginParams = LoginParams(
            Provider.JWT,
            extraLoginOptions = ExtraLoginOptions(
                domain = "https://web3auth.au.auth0.com",
                verifierIdField = "sub"
            )
        )

        // Initialize Web3Auth session
        web3Auth.setResultUrl(intent?.data)
        val sessionResponse = web3Auth.initialize()
        sessionResponse.whenComplete { _, error ->
            if (error == null) {
                val privateKey = web3Auth.getPrivkey()
                credentials = Credentials.create(privateKey)
                Log.d(TAG, "Web3Auth Private Key: $privateKey")
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
                        onSignIn = { signIn() },
                        isLoggedIn = ::isLoggedIn
                    )
                }
            }
        }
    }

    private fun signIn() {
        val loginFuture = web3Auth.login(loginParams)
        loginFuture.whenComplete { response, error ->
            if (error == null) {
                credentials = Credentials.create(web3Auth.getPrivkey())
                Log.d(TAG, "Login Successful: ${gson.toJson(response)}")
            } else {
                Log.e(TAG, "Login Failed: ${error.message}")
            }
        }
    }
    private fun isLoggedIn(): Boolean {
        return web3Auth.getPrivkey()?.isNotEmpty() == true
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