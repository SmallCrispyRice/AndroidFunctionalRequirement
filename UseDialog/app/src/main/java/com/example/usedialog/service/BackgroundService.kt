package com.example.usedialog.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.usedialog.R

/** 前台服务 */
class BackgroundService: Service() {
    companion object{
        val NOTIFICATION_BACKGROUND_ID = 9876
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_BACKGROUND_ID,buildNotification())
        return super.onStartCommand(intent, flags, startId)
    }

    private fun buildNotification(): Notification {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel =
                NotificationChannel("backgroundService-1", "backgroundService", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }//低于Android8.0不需要啥处理，通知渠道id直接写就行，高版本就不可以了，需要创建渠道
        val build = NotificationCompat.Builder(this, "backgroundService-1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("前台服务")
            .setContentText("运行中...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        return build
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}