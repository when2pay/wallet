package com.example.when2pay

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.when2pay.parser.NDEFTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request

class NFCReaderActivity : ComponentActivity() {
    private val TAG = "NFCReaderActivity"

    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null
    private var challenge: Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getChallengeHTTPRequest()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NFC ERROR! Adapter unavailable",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        } else {

            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Display NFC icon
                            Icon(
                                painter = painterResource(id = R.drawable.nfc), // Replace with your NFC icon resource
                                contentDescription = "NFC Icon",
                                modifier = Modifier.size(128.dp), // Adjust size as needed
                                tint = Color.Gray // Choose a color as per your theme
                            )

                            Spacer(modifier = Modifier.height(16.dp)) // Space between icon and text

                            // Display text "BTC Receiver"
                            Text(
                                text = "KYC\nReceiver",
                                textAlign = TextAlign.Center,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    private fun enableNfcForegroundDispatch() {
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    private fun disableNfcForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun getChallengeHTTPRequest() {
        val url = "https://hack.zurini.dev/getChallenge"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val data = response.body?.string() ?: ""
                val jsonElement = Json.parseToJsonElement(data)
                if (jsonElement is JsonObject) {
                    val chg = jsonElement["challenge"]?.jsonPrimitive?.intOrNull
                    if (chg != null) {
                        challenge = chg
                    }
                }

            } catch(e: Exception){
                println(e)
            }
        }


    }


    private fun processGottenData(address: String) {
        // At this point we have the address that the KYC should be verified for
        // TODO

    }

    val SelectAID: ByteArray = byteArrayOf(0xF0.toByte(), 0x39.toByte(), 0x41.toByte(), 0x48.toByte(), 0x14.toByte(), 0x81.toByte(), 0x00.toByte())

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val isoDep: IsoDep? = IsoDep.get(currentTag)

        if (isoDep != null) {
            try {
                isoDep.connect()
                var result = isoDep.transceive(selectApdu(SelectAID))
                if (!(result[0] == 0x6A.toByte() && result[1] == 0x82.toByte())) {
                    Log.wtf(TAG, "Error while authenticating with the app!")
                }

                // TODO: test this
                val sendChallenge: ByteArray = byteArrayOf(0x12.toByte(),0x34.toByte(),challenge.toByte())
                result = isoDep.transceive(sendChallenge)
                if (!(result[0] == 0x6A.toByte() && result[1] == 0x82.toByte())) {
                    Log.wtf(TAG, "Error while authenticating with the app!")
                }

                // Parse from result[2] onwards
                val numberString = result.sliceArray(2..6).toString(Charsets.UTF_8)
                var number=numberString.toInt();
                println(number)

                val readResult = isoDep.transceive(readBinaryAPDU())
                if (!(readResult[readResult.size - 2] == 0x90.toByte() && readResult[readResult.size - 1] == 0x00.toByte())) {
                    Log.wtf(TAG, "Error while reading memory")
                }
                val output = NDEFTools.ExtractTextFromNDEF(readResult)
                Log.i(TAG, "Output: $output")

                processGottenData(output)

            } catch (ex: IOException) {
                Log.e(TAG, "IOException: ${ex.message}")
            } finally {
                try {
                    isoDep.close()
                } catch (ignored: Exception) {
                    Log.w(TAG, "Ignored exception while closing IsoDep")
                }
            }
        } else {
            Toast.makeText(this, "ISO Dep is not supported on this tag", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectApdu(aid: ByteArray): ByteArray {
        val commandApdu = ByteArray(5 + aid.size)
        commandApdu[0] = 0x00.toByte() // CLA
        commandApdu[1] = 0xA4.toByte() // INS
        commandApdu[2] = 0x04.toByte() // P1
        commandApdu[3] = 0x00.toByte() // P2
        commandApdu[4] = (aid.size and 0x0FF).toByte() // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.size)
        return commandApdu
    }

    private fun readBinaryAPDU(): ByteArray {
        val commandApdu = ByteArray(5)
        commandApdu[0] = 0x00.toByte()
        commandApdu[1] = 0xB0.toByte()
        commandApdu[2] = 0x00.toByte()
        commandApdu[3] = 0x00.toByte()
        commandApdu[4] = 0xFE.toByte()
        return commandApdu
    }
}
