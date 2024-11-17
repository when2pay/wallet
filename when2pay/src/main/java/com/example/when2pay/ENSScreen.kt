import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.when2pay.ChainBalanceItem
import com.example.when2pay.R
import com.example.when2pay.SharedViewModel
import com.example.when2pay.getSupportedChains
import com.example.when2pay.sharedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ENSScreen(navController: NavController, sharedData: SharedViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create ENS") }
            )
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
/*
                TextField(
                    value = sharedData.ensName,
                    onValueChange = { sharedData.ensName = it },
                    label = { Text("ENS name") }
                )
*/
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = sharedData.ensName,
                        onValueChange = { sharedData.ensName = it },
                        label = { Text("ENS name") }
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Optional spacer for padding between the fields
                    Text(
                        text = ".wenpay.eth",
                        modifier = Modifier.padding(start = 8.dp), // Optional padding
                        style = MaterialTheme.typography.bodyMedium // Customize the text style if needed
                    )
                }

                Button(
                    onClick = {
                        GlobalScope.launch(Dispatchers.IO) {
                            setENSName(sharedData.privateKey, sharedData.ensName)
                        }
                    }
                ) {
                    Text("Set name")
                }
            }
        }
    }
}

fun setENSName(privateKey: String, ensName: String): String? {
    val ownerAddress = sharedData?.walletAddress// Replace with actual owner address if dynamic
    val expirationTime = System.currentTimeMillis() + 60 * 60 * 1000 // 1 hour from now
    val message = """
        {
            "name": "$ensName.wenpay.eth",
            "owner": "$ownerAddress",
            "addresses": {
                "60": "$ownerAddress",
                "2147492101": "$ownerAddress",
                "2147525809": "$ownerAddress"
            },
            "texts": {}
        }
    """.trimIndent()

    // Hash the message
    val messageHash = Hash.sha3String(message)

    // Sign the message hash with the private key
    val credentials = Credentials.create(privateKey)
    val signatureData = Sign.signMessage(Numeric.hexStringToByteArray(messageHash), credentials.ecKeyPair, false)

    val r = Numeric.toHexString(signatureData.r)
    val s = Numeric.toHexString(signatureData.s)
    val v = Numeric.toHexString(signatureData.v)

    val signatureHash = "0x" + r.substring(2) + s.substring(2) + v.substring(2)

    // Prepare the JSON payload
    val payload = """
        {
            "signature": {
                "hash": "$signatureHash",
                "message": $message
            },
            "expiration": $expirationTime
        }
    """.trimIndent()

    // Make the API call
    val client = OkHttpClient()
    val mediaType = "application/json".toMediaTypeOrNull()
    val body = payload.toRequestBody(mediaType)
    val request = Request.Builder()
        .url("https://wenpay.wenpay.workers.dev/set")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw RuntimeException("Unexpected code $response")
        }
        val data = response.body?.string()
        Log.i("ENSScreen", "" + data)
        return data
    }
}

