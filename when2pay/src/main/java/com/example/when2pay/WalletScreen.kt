package com.example.when2pay

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(navController: NavController, sharedData: SharedViewModel) {
    checkForENS(sharedData)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sharedData.walletPageTitle) }, // TODO: replace this with ENS name if there's one
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
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
                    Button(onClick = { navController.navigate("send") }) {
                        Text("Send")
                    }
                    Button(onClick = { navController.navigate("receive") }) {
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

fun checkForENS(sharedData: SharedViewModel) {
    val url = "https://wenpay.wenpay.workers.dev/address/0x798eC9984Cb047b9429809eDf35b8994822a3E3A" // TODO: put address here
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
                        sharedData.walletPageTitle = name
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

fun handleENSCreation(){
    println("Ciao")
}

data class Chain(val name: String, val symbol: String, val balance: Double)

fun getSupportedChains(): List<Chain> {
    return listOf(
        Chain("Ethereum", "ETH", 0.5),
        Chain("Binance Smart Chain", "BNB", 1.2),
        Chain("Polygon", "MATIC", 100.0)
    )
}