package com.example.spyware

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    private val REQUEST_CHECK_SETTINGS = 1001
    private var mobileDialogOpen=false
    private lateinit var gpsReader: GpsReader;

    override fun onCreate(savedInstanceState: Bundle?) {
        println("Created MainActivity")
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
//        gpsReader.getLastLocation()
        gpsReader.grantLocationPermission(this)
        gpsReader.turnOnLocation(this)


        var encryptedFileIo:EncryptedFileIo=EncryptedFileIo(this)
        encryptedFileIo.appendEncryptedFile("secrets4.txt","hello world")


        if(!TimerService.isServiceActive) {
            this.startForegroundService(
                Intent(
                    this,
                    TimerService::class.java
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