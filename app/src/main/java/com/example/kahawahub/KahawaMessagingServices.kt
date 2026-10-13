package com.example.kahawahub

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class KahawaMessagingServices: FirebaseMessagingService() {

    override fun onMessageReceived(message:RemoteMessage){
        super.onMessageReceived(message)

        val title=message.notification?.title
        ?: message.data["title"]
        ?: "Kahawa Hub"

        val body=message.notification?.body
            ?: message.data["body"]
            ?: "You have a new notification"

        createNotificationChannel()

        val notification= NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val permissionGranted=
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS

                    ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted){
            NotificationManagerCompat.from(this).notify(
                1001,
                notification
            )
        } else {
            Log.w(
                TAG,
                "Notification permission has not been granted"
            )
        }


    }
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kahawa Hub Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply{
                description=
                    "Coffee price updates and cooperative announcements"
            }
            val manager=getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }
    override fun onNewToken(token: String){
        super.onNewToken(token)

        Log.d(TAG,"Firebase messaging token refreshed")

    }
    companion object{
        private const val CHANNEL_ID="kahawa_hub_updates"
        private const val TAG="KahawaMessagingService"
    }
}