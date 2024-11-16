import android.annotation.SuppressLint
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
                        println("ciao")
                    }
                ) {
                    Text("Set name")
                }
            }
        }
    }

    /*
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "You are not logged in.", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
    }*/
}
