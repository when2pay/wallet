package com.example.when2pay

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


var sharedData: SharedViewModel? = null;
var navController: NavController? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(_navController: NavController, _sharedData: SharedViewModel) {
    navController = _navController
    sharedData = _sharedData

    checkForENS(handleENSCreation = {
        Handler(Looper.getMainLooper()).post {
            _navController.navigate("ENS")
        }
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sharedData!!.walletPageTitle) }, // TODO: replace this with ENS name if there's one
                actions = {
                    IconButton(onClick = { _navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { _navController.navigate("send") }) {
                        Text("Send")
                    }
                    Button(onClick = { _navController.navigate("receive") }) {
                        Text("Receive")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Text(
                    "Total Balance",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            // Add items for each chain and token
            items(getSupportedChains()) { chain ->
                ChainBalanceItem(chain)
            }
        }
    }
}

@Composable
fun ChainBalanceItem(chain: Chain) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(chain.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Balance: ${chain.balance} ${chain.symbol}")
        }
    }
}

fun checkForENS(handleENSCreation : () -> Unit) {
    val url = "https://wenpay.wenpay.workers.dev/address/" + sharedData?.walletAddress
    val client = OkHttpClient()

    val request = Request.Builder()
        .url(url)
        .build()

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            val data = response.body?.string() ?: ""
            val jsonArr = Json.parseToJsonElement(data).jsonArray
            if(jsonArr is JsonArray){
                val el = jsonArr[0];
                if(el is JsonObject){
                    val name = el["name"]?.jsonPrimitive?.contentOrNull
                    if (name != null) {
                        sharedData?.walletPageTitle  = name
                    } else {
                        handleENSCreation()
                    }
                }
            }
        } catch(e: Exception){
            println(e)
            handleENSCreation()
        }
    }

}

data class Chain(val name: String, val symbol: String, val balance: Double)

fun getSupportedChains(): List<Chain> {
    return listOf(
        Chain("Ethereum", "ETH", 0.5),
        Chain("Binance Smart Chain", "BNB", 1.2),
        Chain("Polygon", "MATIC", 100.0)
    )
}