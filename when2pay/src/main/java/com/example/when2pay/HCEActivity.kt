package com.example.when2pay

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.when2pay.cardEmulation.KHostApduService

class HCEActivity : ComponentActivity() {
    private val TAG = "HCEActivity"

    private var nfcAdapter: NfcAdapter? = null

    private var nfcMessage: String by mutableStateOf("Hello")

    private var address: String by mutableStateOf("GBXU6KDGPVMYRMG5CQ7FRX7HP2XCDVH3SQH3JNS4YQJ7HCVD2HI25PPK")
    private var amount: String by mutableStateOf("10")
    private var chainID: String by mutableStateOf("0x18e")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val b = intent.extras
            val data = b!!.getString("data").toString()
            if (data.length > 0) {
                if (data != null) {
                    nfcMessage = data;
                    setNFCMessage(data)
                }
            }
        } catch(e: Exception) {}


        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null || !supportNfcHceFeature()) {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Can't get NFCAdapter")
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
                                text = "KYC\nTransmitter",
                                textAlign = TextAlign.Center,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Gray
                            )

                            TextField(
                                value = nfcMessage,
                                onValueChange = { nfcMessage = it },
                                label = { Text("message") }
                            )

                            Button(
                                onClick={
                                    setNFCMessage()
                                }
                            ) {
                                Text("Set the message")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun supportNfcHceFeature() =
        checkNFCEnable() && packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)

    private fun checkNFCEnable(): Boolean {
        return if (nfcAdapter == null) {
            false
        } else {
            nfcAdapter?.isEnabled == true
        }
    }

    private fun setNFCMessage(message: String = "") {
        // Combine all the data into a metamask url
        var urlToCast: String = ""

        if(message.length == 0) {
            urlToCast = nfcMessage; //"web+stellar:$address?amount=$amount";
        } else {
            urlToCast = message
        }

        Log.i(TAG, urlToCast)
        if (TextUtils.isEmpty(urlToCast)) {
            Toast.makeText(
                this,
                "The message has not to be empty",
                Toast.LENGTH_LONG,
            ).show()
        } else {
            Toast.makeText(
                this,
                urlToCast,
                Toast.LENGTH_LONG,
            ).show()
            val intent = Intent(this, KHostApduService::class.java)
            intent.putExtra("ndefMessage", urlToCast)
            startService(intent)
        }

        // TODO: Shazam after this point should check on BlockScout for the transaction to be made or add an event listener
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

}