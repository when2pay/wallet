package com.sbz.web3authdemoapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.*
import org.web3j.crypto.Credentials
import org.web3j.crypto.Hash
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.Sign
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthChainId
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture


class MainActivity : AppCompatActivity() {

    private lateinit var web3Auth: Web3Auth

    private val gson = Gson()
    private lateinit var web3: Web3j
    private lateinit var credentials: Credentials
    private lateinit var loginParams: LoginParams
    private val rpcUrl = "https://rpc.ankr.com/eth_sepolia"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web3 = Web3j.build(HttpService(rpcUrl))
        loginParams = LoginParams(Provider.JWT, extraLoginOptions = ExtraLoginOptions(domain = "https://web3auth.au.auth0.com", verifierIdField = "sub"))

        web3Auth = Web3Auth(
           Web3AuthOptions(
               context = this,
               clientId = getString(R.string.web3auth_project_id), // pass over your Web3Auth Client ID from Developer Dashboard
               network = Network.SAPPHIRE_MAINNET, // pass over the network you want to use (MAINNET or TESTNET or CYAN)
               buildEnv = BuildEnv.PRODUCTION,
               redirectUrl = Uri.parse("com.sbz.web3authdemoapp://auth"), // your app's redirect URL
               // Optional parameters
               whiteLabel = WhiteLabelData(
                   "Web3Auth Android Auth0 Example",
                   null,
                   "https://cryptologos.cc/logos/ethereum-eth-logo.png",
                   "https://cryptologos.cc/logos/ethereum-eth-logo.png",
                   Language.EN,
                   ThemeModes.LIGHT,
                   true,
                   hashMapOf(
                       "primary" to "#eb5424"
                   )
               ),
               mfaSettings = MfaSettings(
                   deviceShareFactor = MfaSetting(true, 1, true),
                   socialBackupFactor = MfaSetting(true, 2, true),
                   passwordFactor = MfaSetting(true, 3, false),
                   backUpShareFactor = MfaSetting(true, 4, false),
               ),
               loginConfig = hashMapOf("jwt" to LoginConfigItem(
                   verifier = "w3a-auth0-demo",
                   typeOfLogin = TypeOfLogin.JWT,
                   name = "Auth0 Login",
                   clientId = getString(R.string.web3auth_auth0_client_id)
               ))
           )
       )

        // Handle user signing in when app is not alive
        web3Auth.setResultUrl(intent?.data)

        // Call initialize() in onCreate() to check for any existing session.
        val sessionResponse: CompletableFuture<Void> = web3Auth.initialize()
        sessionResponse.whenComplete { _, error ->
            if (error == null) {
                reRender()
                println("PrivKey: " + web3Auth.getPrivkey())
                println("ed25519PrivKey: " + web3Auth.getEd25519PrivKey())
                println("Web3Auth UserInfo" + web3Auth.getUserInfo())
                credentials = Credentials.create(web3Auth.getPrivkey())
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
                // Ideally, you should initiate the login function here.
            }
        }

        // Setup UI and event handlers
        val signInButton = findViewById<Button>(R.id.signInButton)
        signInButton.setOnClickListener { signIn() }

        val signOutButton = findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener { signOut() }
        signOutButton.visibility = View.GONE

        val getAddressButton = findViewById<Button>(R.id.getAddress)
        getAddressButton.setOnClickListener { getAddress() }
        getAddressButton.visibility = View.GONE

        val getBalanceButton = findViewById<Button>(R.id.getBalance)
        getBalanceButton.setOnClickListener { getBalance() }
        getBalanceButton.visibility = View.GONE

        val getMessageButton = findViewById<Button>(R.id.getMessage)
        getMessageButton.setOnClickListener { signMessage("Welcome to Web3Auth") }
        getMessageButton.visibility = View.GONE

        val getTransactionButton = findViewById<Button>(R.id.getTransaction)
        getTransactionButton.setOnClickListener { sendTransaction(0.001, "0xeaA8Af602b2eDE45922818AE5f9f7FdE50cFa1A8") }
        getTransactionButton.visibility = View.GONE

        val getEnableMFAButton = findViewById<Button>(R.id.enableMFA)
        getEnableMFAButton.setOnClickListener { enableMFA() }
        getEnableMFAButton.visibility = View.GONE

        val getLaunchWalletServicesButton = findViewById<Button>(R.id.launchWalletServices)
        getLaunchWalletServicesButton.setOnClickListener { launchWalletServices() }
        getLaunchWalletServicesButton.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Handle user signing in when app is active
        web3Auth.setResultUrl(intent?.data)
    }

    override fun onResume() {
        super.onResume()
        if (Web3Auth.getCustomTabsClosed()) {
            Toast.makeText(this, "User closed the browser.", Toast.LENGTH_SHORT).show()
            web3Auth.setResultUrl(null)
            Web3Auth.setCustomTabsClosed(false)
        }
    }

    private fun signIn() {
        val loginCompletableFuture: CompletableFuture<Web3AuthResponse> = web3Auth.login(loginParams)

    //    For Email Passwordless, use the below code and pass email id into extraLoginOptions of LoginParams.
    //    val selectedLoginProvider = Provider.EMAIL_PASSWORDLESS
    //    val loginCompletableFuture: CompletableFuture<Web3AuthResponse> = web3Auth.login(LoginParams(selectedLoginProvider, extraLoginOptions = ExtraLoginOptions(login_hint = "shahbaz.web3@gmail.com")))

    //    For login with Custom JWT, use the below code and pass email id into extraLoginOptions of LoginParams.
    //    val selectedLoginProvider = Provider.JWT
    //    val loginCompletableFuture: CompletableFuture<Web3AuthResponse> = web3Auth.login(LoginParams(selectedLoginProvider, extraLoginOptions = ExtraLoginOptions(id_token = "<id-token>", domain: "your-domain")))

        loginCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                credentials = Credentials.create(web3Auth.getPrivkey())
                reRender()
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong" )
            }
        }
    }

    private fun enableMFA() {

        val completableFuture = web3Auth.enableMFA(loginParams)

        completableFuture.whenComplete{_, error ->
            if (error == null) {
                Log.d("MainActivity_Web3Auth", "Launched successfully")
                // Add your logic
            } else {
                // Add your logic on error
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
            }
        }
    }

    private fun launchWalletServices() {
        val completableFuture = web3Auth.launchWalletServices(
            ChainConfig(
                chainId = "0x1",
                rpcTarget = "https://rpc.ankr.com/eth",
                ticker = "ETH",
                chainNamespace = ChainNamespace.EIP155
            )
        )

        completableFuture.whenComplete{_, error ->
            if(error == null) {
                // Add your logic
                Log.d("MainActivity_Web3Auth", "Wallet services launched successfully")
            } else {
                // Add your logic for error
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
            }
        }
    }

    private fun getAddress(): String {
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val publicAddress = credentials.address
        contentTextView.text = publicAddress
        println("Address:, $publicAddress")
        Log.d("MainActivity_Web3Auth", "Address: $publicAddress")
        return publicAddress
    }

    private fun getBalance(): BigInteger? {
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val publicAddress = credentials.address
        val ethBalance: EthGetBalance = web3.ethGetBalance(publicAddress, DefaultBlockParameterName.LATEST).sendAsync().get()
        contentTextView.text = ethBalance.balance.toString()
        println("Balance: ${ethBalance.balance}")
        return ethBalance.balance
    }

    private fun signMessage(message: String): String {
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val hashedData = Hash.sha3(message.toByteArray(StandardCharsets.UTF_8))
        val signature = Sign.signMessage(hashedData, credentials.ecKeyPair)
        val r = Numeric.toHexString(signature.r)
        val s = Numeric.toHexString(signature.s).substring(2)
        val v = Numeric.toHexString(signature.v).substring(2)
        val signHash = StringBuilder(r).append(s).append(v).toString()
        contentTextView.text = signHash
        println("Signed Hash: $signHash")
        return signHash
    }

    private fun sendTransaction(amount: Double, recipientAddress: String): String? {
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val ethGetTransactionCount: EthGetTransactionCount = web3.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST).sendAsync().get()
        val nonce: BigInteger = ethGetTransactionCount.transactionCount
        val value: BigInteger = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger()
        val gasLimit: BigInteger = BigInteger.valueOf(21000)
        val chainId: EthChainId = web3.ethChainId().sendAsync().get()

        // Raw Transaction
        val rawTransaction: RawTransaction = RawTransaction.createTransaction(
            chainId.chainId.toLong(),
            nonce,
            gasLimit,
            recipientAddress,
            value,
            "",
            BigInteger.valueOf(5000000000),
            BigInteger.valueOf(6000000000000)
        )

        val signedMessage: ByteArray = TransactionEncoder.signMessage(rawTransaction, credentials)
        val hexValue: String = Numeric.toHexString(signedMessage)
        val ethSendTransaction: EthSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get()
        return if(ethSendTransaction.error != null) {
            println("Tx Error: ${ethSendTransaction.error.message}")
            contentTextView.text = "Tx Error: ${ethSendTransaction.error.message}"
            ethSendTransaction.error.message
        } else {
            println("Tx Hash: ${ethSendTransaction.transactionHash}")
            contentTextView.text = "Tx Hash: ${ethSendTransaction.transactionHash}"
            ethSendTransaction.transactionHash
        }
    }

    private fun signOut() {
        val logoutCompletableFuture =  web3Auth.logout()
        logoutCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                reRender()
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong" )
            }
        }
        recreate()
    }

    private fun reRender() {
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val signOutButton = findViewById<Button>(R.id.signOutButton)
        val getAddressButton = findViewById<Button>(R.id.getAddress)
        val getBalanceButton = findViewById<Button>(R.id.getBalance)
        val getMessageButton = findViewById<Button>(R.id.getMessage)
        val getTransactionButton = findViewById<Button>(R.id.getTransaction)
        val getEnableMFAButton = findViewById<Button>(R.id.enableMFA)
        val getLaunchWalletServicesButton = findViewById<Button>(R.id.launchWalletServices)

        var key: String? = null
        var userInfo: UserInfo? = null
        try {
            key = web3Auth.getPrivkey()
            userInfo = web3Auth.getUserInfo()
        } catch (ex: Exception) {
            print(ex)
        }
        println(userInfo)
        if (key is String && key.isNotEmpty()) {
            contentTextView.text = gson.toJson(userInfo) + "\n Private Key: " + key
            contentTextView.visibility = View.VISIBLE
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            getAddressButton.visibility = View.VISIBLE
            getBalanceButton.visibility = View.VISIBLE
            getMessageButton.visibility = View.VISIBLE
            getTransactionButton.visibility = View.VISIBLE
            getEnableMFAButton.visibility = View.VISIBLE
            getLaunchWalletServicesButton.visibility = View.VISIBLE
        } else {
            contentTextView.text = getString(R.string.not_logged_in)
            contentTextView.visibility = View.GONE
            signInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
            getAddressButton.visibility = View.GONE
            getBalanceButton.visibility = View.GONE
            getMessageButton.visibility = View.GONE
            getTransactionButton.visibility = View.GONE
            getEnableMFAButton.visibility = View.GONE
            getLaunchWalletServicesButton.visibility = View.GONE
        }
    }
}