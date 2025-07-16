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
    private var startId:Int=0

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand called")
        createNotificationChannel();
        this.startId=startId
        startForeground(1, createNotification("Make sure location and internet are available. Tap to open app."));
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        println("onDestroy called")
        super.onDestroy()
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
            .setContentTitle("Protector")
            .setContentText(message)
            .setSmallIcon(R.mipmap.helmet_notification)
            .setContentIntent(pendingIntent) // 👈 This line makes the notification clickable
            .setAutoCancel(true) // Optional: dismiss notification when clicked
            .build()
    }
}