package com.example.spyware

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Location
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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    private val REQUEST_CHECK_SETTINGS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
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

        // Step 1: Create location request
        locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        settingsClient = LocationServices.getSettingsClient(this)

        // Step 2: Check location settings
        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                // Location is already enabled
                // Proceed with location-dependent logic
                val locationReader:LocationReader=LocationReader(this)
                locationReader.getLastLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        // Step 3: Prompt user to turn on location
                        exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Handle error
                        sendEx.printStackTrace()
                    }
                }
            }

        var encryptedFileIo:EncryptedFileIo=EncryptedFileIo(this)
        encryptedFileIo.appendEncryptedFile("secrets4.txt","hello world")
    }

    // Step 4: Handle result of user's decision
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // User turned on location
                println("halo")
                val locationReader:LocationReader= LocationReader(this)
                locationReader.getLastLocation()
            } else {
                // User declined to turn on location
            }
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