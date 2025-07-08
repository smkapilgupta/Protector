package com.example.spyware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.spyware.common.Constants.Companion.timerSeconds

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        AlarmManger(context).start(System.currentTimeMillis()+ timerSeconds *1000)
        TimeoutHandler(context).alarm()
    }
}